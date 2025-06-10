package com.rollerspeed.rollerspeed.Controller;

import com.rollerspeed.rollerspeed.Repository.userRepository;
import com.rollerspeed.rollerspeed.Service.UsuarioService;
import com.rollerspeed.rollerspeed.model.Usuario;
import com.rollerspeed.rollerspeed.model.RolUsuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService userService;
    
    @Autowired
    private userRepository usuarioRepository;

    @GetMapping("/")
    public String index(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Si hay un usuario logueado, redirigimos a su dashboard específico
        if (session.getAttribute("userId") != null) {
            RolUsuario rolActual = (RolUsuario) session.getAttribute("userRol"); // Recupera como RolUsuario
            if (rolActual != null) { // Asegúrate de que el rol no sea nulo antes de usarlo
                switch (rolActual) {
                    case ADMIN:
                        return "redirect:/dashboard/admin";
                    case PROFESOR:
                        return "redirect:/dashboard/profesor";
                    case ALUMNO:
                        return "redirect:/dashboard/alumno";
                    default:
                        session.invalidate();
                        redirectAttributes.addFlashAttribute("error", "Rol de usuario no reconocido. Por favor, inicia sesión de nuevo.");
                        return "redirect:/login";
                }
            }
        }
        // Si no hay usuario logueado o el rol no es válido, muestra la página pública de inicio
        return "publicIndex";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email, @RequestParam String password,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.getPassword().equals(password)) { // ¡ATENCIÓN! Esto NO es seguro para producción. Usa BCryptPasswordEncoder.
                session.setAttribute("userId", usuario.getId());
                session.setAttribute("userRol", usuario.getRol()); // Guarda el enum directamente
                session.setAttribute("userName", usuario.getNombre());
                redirectAttributes.addFlashAttribute("mensaje", "¡Bienvenido, " + usuario.getNombre() + "!");

                switch (usuario.getRol()) {
                    case ADMIN:
                        return "redirect:/dashboard/admin";
                    case PROFESOR:
                        return "redirect:/dashboard/profesor";
                    case ALUMNO:
                        return "redirect:/dashboard/alumno";
                    default:
                        session.invalidate();
                        redirectAttributes.addFlashAttribute("error", "Rol de usuario no válido. Por favor, contacta al soporte.");
                        return "redirect:/login";
                }
            }
        }
        redirectAttributes.addFlashAttribute("error", "Credenciales inválidas. Inténtalo de nuevo.");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("mensaje", "Has cerrado sesión exitosamente.");
        return "redirect:/login";
    }

    // --- Métodos de Dashboards Específicos ---

    @GetMapping("/dashboard/admin")
    public String showAdminDashboard(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol"); // Recupera como RolUsuario
        if (rolEnSesion == null || rolEnSesion != RolUsuario.ADMIN) { // Compara directamente con el enum
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. No tienes permisos de administrador.");
            return "redirect:/login";
        }
        model.addAttribute("totalUsuarios", userService.getUsuarios().size());
        return "admin/adminDashboard";
    }

    @GetMapping("/dashboard/profesor")
    public String showProfesorDashboard(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol"); // Recupera como RolUsuario
        if (rolEnSesion == null || rolEnSesion != RolUsuario.PROFESOR) { // Compara directamente con el enum
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. No tienes permisos de profesor.");
            return "redirect:/login";
        }
        return "profesor/profesorDashboard";
    }

    @GetMapping("/dashboard/alumno")
    public String showAlumnoDashboard(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol"); // Recupera como RolUsuario
        if (rolEnSesion == null || rolEnSesion != RolUsuario.ALUMNO) { // Compara directamente con el enum
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. No tienes permisos de alumno.");
            return "redirect:/login";
        }
        return "alumno/alumnoDashboard";
    }
}