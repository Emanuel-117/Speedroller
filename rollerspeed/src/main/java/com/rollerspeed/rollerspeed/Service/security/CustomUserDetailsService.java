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

        // Construir el objeto UserDetails de Spring Security con los datos del usuario
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword()) // La contraseña debe estar ya encriptada
                .roles(usuario.getRol().name()) // El rol de tu enum RolUsuario
                .build();
    }
}