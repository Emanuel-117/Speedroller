package com.rollerspeed.rollerspeed.Service.security;

import com.rollerspeed.rollerspeed.Repository.userRepository;
import com.rollerspeed.rollerspeed.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private userRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Buscar el usuario por su email en la base de datos
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Verificar que el usuario tenga un rol asignado
        if (usuario.getRol() == null) {
            throw new UsernameNotFoundException("Usuario sin rol asignado: " + email);
        }

        // Verificar que la contraseña no sea nula
        if (usuario.getPassword() == null) {
            throw new UsernameNotFoundException("Usuario sin contraseña: " + email);
        }

        // Construir el objeto UserDetails de Spring Security con los datos del usuario
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false) // Por defecto, todos los usuarios están activos
                .build();
    }
}