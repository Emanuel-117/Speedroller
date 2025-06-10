package com.rollerspeed.rollerspeed.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rollerspeed.rollerspeed.Repository.ClaseRepository;
import com.rollerspeed.rollerspeed.Repository.userRepository;
import com.rollerspeed.rollerspeed.model.Clase;
import com.rollerspeed.rollerspeed.model.RolUsuario;
import com.rollerspeed.rollerspeed.model.Usuario;

import jakarta.transaction.Transactional;

@Service
public class UsuarioService {

    @Autowired
    private userRepository usuarioRepository;
    @Autowired
    private ClaseRepository claseRepository;

    public List<Usuario> getUsuarios() {
        return usuarioRepository.findAll();
    }

   
    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuario guardaUsuario(Usuario usuario) {
        // Lógica para manejar la contraseña en caso de edición
        if (usuario.getId() != null && (usuario.getPassword() == null || usuario.getPassword().isEmpty())) {
            // Si es una actualización y la contraseña viene vacía, recupera la contraseña existente
            Optional<Usuario> existingUser = usuarioRepository.findById(usuario.getId());
            if (existingUser.isPresent()) {
                usuario.setPassword(existingUser.get().getPassword()); // Mantener la contraseña existente
            }
        }
        // Si el usuario.getId() es nulo (es decir, es un nuevo registro),
        // se espera que la contraseña venga en el objeto 'usuario'
        // y se guardará tal cual (¡recuerda, hashing de contraseñas es CRÍTICO para producción!)
        return usuarioRepository.save(usuario);
    }

    @Transactional // Asegura que toda la operación sea atómica
    public void deleteUsuario(Long id) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(id);
        if (optionalUsuario.isPresent()) {
            Usuario usuario = optionalUsuario.get();

            // Lógica específica para alumnos: desinscribirlos de las clases
            if (usuario.getRol() == RolUsuario.ALUMNO) {
                // Crear una copia para evitar ConcurrentModificationException
                Set<Clase> clasesCopia = new HashSet<>(usuario.getClasesInscritas());
                for (Clase clase : clasesCopia) {
                    usuario.removeClase(clase); // Asegúrate de que el método 'removeClase' en Usuario
                                                // también maneje la relación inversa en 'Clase' si es bidireccional.
                }
                // Guardar los cambios en las clases desinscritas antes de eliminar el usuario
                // Esto es importante si la relación es bidireccional y se maneja en el lado del usuario
                // (ej. @ManyToMany con Usuario como propietario)
                usuarioRepository.save(usuario); // Persiste los cambios de desinscripción
            }

            // Lógica específica para profesores: verificar clases impartidas
            List<Clase> clasesImpartidas = claseRepository.findByProfesor(usuario);
            if (!clasesImpartidas.isEmpty()) {
                throw new IllegalStateException("No se puede eliminar el profesor porque tiene clases asignadas. Reasigne o elimine las clases primero.");
            }

            usuarioRepository.delete(usuario); // Finalmente, elimina el usuario
        } else {
            // Podrías lanzar una excepción aquí también, o simplemente loggear
            throw new IllegalArgumentException("Usuario con ID " + id + " no encontrado para eliminación.");
            // System.out.println("Usuario con ID " + id + " no encontrado para eliminación."); // Ya no es necesario si lanzas excepción
        }
    }

    @Transactional
    public boolean inscribirUsuarioAClase(Long usuarioId, Long claseId) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(usuarioId);
        Optional<Clase> optionalClase = claseRepository.findById(claseId);

        if (optionalUsuario.isPresent() && optionalClase.isPresent()) {
            Usuario usuario = optionalUsuario.get();
            Clase clase = optionalClase.get();

            if (usuario.getRol() != RolUsuario.ALUMNO) {
                System.out.println("Error: El usuario " + usuario.getNombre() + " no es un alumno.");
                return false;
            }

            if (!usuario.getClasesInscritas().contains(clase)) {
                usuario.addClase(clase);
                usuarioRepository.save(usuario); // Guardar el usuario para persistir la relación
                return true;
            } else {
                System.out.println("El usuario " + usuario.getNombre() + " ya está inscrito en la clase " + clase.getNombre());
                return false;
            }
        }
        return false;
    }

    @Transactional
    public boolean desinscribirUsuarioDeClase(Long usuarioId, Long claseId) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(usuarioId);
        Optional<Clase> optionalClase = claseRepository.findById(claseId);

        if (optionalUsuario.isPresent() && optionalClase.isPresent()) {
            Usuario usuario = optionalUsuario.get();
            Clase clase = optionalClase.get();

            if (usuario.getClasesInscritas().contains(clase)) {
                usuario.removeClase(clase);
                usuarioRepository.save(usuario); // Guardar el usuario para persistir la relación
                return true;
            } else {
                System.out.println("El usuario " + usuario.getNombre() + " no está inscrito en la clase " + clase.getNombre());
                return false;
            }
        }
        return false;
    }

    public List<Usuario> getUsuariosByRol(RolUsuario rol) {
        return usuarioRepository.findByRol(rol);
    }
}