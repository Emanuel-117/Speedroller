package com.rollerspeed.rollerspeed.Service;

import com.rollerspeed.rollerspeed.Repository.userRepository;
import com.rollerspeed.rollerspeed.model.RolUsuario;
import com.rollerspeed.rollerspeed.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private userRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Inyectamos el encriptador de contraseñas

    public List<Usuario> getUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }
    
    public Optional<Usuario> getUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario guardaUsuario(Usuario usuario) {
        // Encriptar la contraseña antes de guardarla
        // Solo la encriptamos si es un nuevo usuario o si se ha cambiado la contraseña
        if (usuario.getId() == null || usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }
    
    public void deleteUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalStateException("El usuario con ID " + id + " no existe.");
        }
        usuarioRepository.deleteById(id);
    }

    public List<Usuario> getUsuariosByRol(RolUsuario rol) {
        return usuarioRepository.findByRol(rol);
    }
}