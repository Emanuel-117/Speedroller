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
    private userRepository usuarioRepository;;
    @Autowired
    private ClaseRepository claseRepository;

    public List<Usuario> getUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarById(Long id) {
        return usuarioRepository.findById(id);
    }

   public Usuario guardaUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public void eliminarById(Long id) {
        usuarioRepository.deleteById(id);
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
                usuarioRepository.save(usuario); 
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
                usuarioRepository.save(usuario); 
                return true;
            } else {
                System.out.println("El usuario " + usuario.getNombre() + " no está inscrito en la clase " + clase.getNombre());
                return false;
            }
        }
        return false;
    }

    @Transactional
    public void deleteUsuario(Long id) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(id);
        if (optionalUsuario.isPresent()) {
            Usuario usuario = optionalUsuario.get();

            if (usuario.getRol() == RolUsuario.ALUMNO) {
                
                Set<Clase> clasesCopia = new HashSet<>(usuario.getClasesInscritas());
                for (Clase clase : clasesCopia) {
                    usuario.removeClase(clase); 
                }
            }

            
            List<Clase> clasesImpartidas = claseRepository.findByProfesor(usuario);
            if (!clasesImpartidas.isEmpty()) {
                throw new IllegalStateException("No se puede eliminar el profesor porque tiene clases asignadas. Reasigne o elimine las clases primero.");
            }

            
            usuarioRepository.delete(usuario);
        } else {
            System.out.println("Usuario con ID " + id + " no encontrado para eliminación.");
        }
    }
}