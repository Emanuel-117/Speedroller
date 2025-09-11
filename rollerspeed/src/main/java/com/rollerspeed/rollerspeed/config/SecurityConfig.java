package com.rollerspeed.rollerspeed.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita las anotaciones de seguridad como @PreAuthorize
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilita CSRF, esencial para APIs REST
            .authorizeHttpRequests(authorize -> authorize
                // Rutas públicas que no requieren autenticación
                .requestMatchers("/", "/login", "/users/registrar-alumno", "/guardar-publico").permitAll()

                // Rutas que requieren roles específicos
                .requestMatchers("/dashboard/admin", "/users/listar", "/users/eliminar/**", "/clases/nueva", "/clases/guardar", "/clases/eliminar/**", "/clases/admin/**", "/pagos/**").hasRole("ADMIN")
                .requestMatchers("/dashboard/profesor", "/users/listar/alumnos", "/clases/listarClases", "/clases/profesor/**").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers("/dashboard/alumno", "/users/mi-perfil", "/users/alumno/**").hasRole("ALUMNO")
                .requestMatchers("/reportes/asistencia/alumno", "/reportes/asistencia/alumno/**").hasAnyRole("ADMIN", "PROFESOR", "ALUMNO")
                .requestMatchers("/reportes/asistencia/clase", "/reportes/asistencia/clase/**").hasAnyRole("ADMIN", "PROFESOR")
                
                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )
            // Configuración del formulario de login y logout
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true) // Redirige a la raíz después de un login exitoso
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .permitAll()
            );

        return http.build();
    }
    
    // Este bean es crucial para encriptar las contraseñas de los usuarios
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}