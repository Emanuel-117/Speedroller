package com.rollerspeed.rollerspeed.Controller;

import com.rollerspeed.rollerspeed.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService userService;

    @GetMapping("/")
    public String index(Model model) {
        // Spring Security maneja el usuario autenticado automáticamente
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/dashboard/redirect";
        }
        return "index";
    }

    @GetMapping("/dashboard/redirect")
    public String redirectByRole(Authentication authentication) {
        try {
            // Verificar autenticación
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return "redirect:/login";
            }
            
            // Verificar roles
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/dashboard/admin";
            } else if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PROFESOR"))) {
                return "redirect:/dashboard/profesor";
            } else if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ALUMNO"))) {
                return "redirect:/dashboard/alumno";
            }
            
            // Si el usuario tiene un rol no reconocido
            return "redirect:/login?error=unauthorized";
        } catch (Exception e) {
            // Log del error y redirección segura en caso de cualquier error
            return "redirect:/login?error=session_error";
        }
    }
    
    @GetMapping("/dashboard/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAdminDashboard(Model model) {
        model.addAttribute("totalUsuarios", userService.getUsuarios().size());
        return "admin/adminDashboard";
    }

    @GetMapping("/dashboard/profesor")
    @PreAuthorize("hasRole('PROFESOR')")
    public String showProfesorDashboard(Model model) {
        return "profesor/profesorDashboard";
    }

    @GetMapping("/dashboard/alumno")
    @PreAuthorize("hasRole('ALUMNO')")
    public String showAlumnoDashboard(Model model) {
        return "alumno/alumnoDashboard";
    }
}