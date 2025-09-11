package com.rollerspeed.rollerspeed.Controller;

import com.rollerspeed.rollerspeed.Service.UsuarioService;
import com.rollerspeed.rollerspeed.Service.ClaseService;
import com.rollerspeed.rollerspeed.model.Clase;
import com.rollerspeed.rollerspeed.model.GeneroUsuario;
import com.rollerspeed.rollerspeed.model.MedioPago;
import com.rollerspeed.rollerspeed.model.RolUsuario;
import com.rollerspeed.rollerspeed.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class userController {

    @Autowired
    private UsuarioService userService;

    @Autowired
    private ClaseService claseService;

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String mostrarFormularioNuevoUsuarioAdmin(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", RolUsuario.values());
        model.addAttribute("generos", GeneroUsuario.values());
        model.addAttribute("mediosPago", MedioPago.values());
        return "users/usuarioForm";
    }

    @GetMapping("/registrar-alumno")
    public String mostrarFormularioRegistroAlumno(Model model) {
        Usuario nuevoAlumno = new Usuario();
        nuevoAlumno.setRol(RolUsuario.ALUMNO);
        model.addAttribute("usuario", nuevoAlumno);
        model.addAttribute("generos", GeneroUsuario.values());
        model.addAttribute("mediosPago", MedioPago.values());
        return "users/usuarioForm";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN') or #usuario.id == null")
    public String guardarUsuario(@ModelAttribute("usuario") Usuario usuario,
                                 RedirectAttributes redirectAttributes) {
        boolean isPublicRegistration = (usuario.getId() == null);
        if (isPublicRegistration) {
            usuario.setRol(RolUsuario.ALUMNO);
            userService.guardaUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "¡Registro exitoso! Ya puedes iniciar sesión con tu email y contraseña.");
            return "redirect:/login";
        } else {
            userService.guardaUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario guardado exitosamente!");
            return "redirect:/dashboard/redirect";
        }
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN') and #id != principal.id")
    public String eliminarUsuario(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUsuario(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado exitosamente.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el usuario: " + e.getMessage());
        }
        return "redirect:/dashboard/admin";
    }

    @GetMapping(value = "/editar/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public String mostrarFormularioEditarUsuario(@PathVariable Long id,
                                                 Model model, RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuarioOptional = userService.getUsuarioById(id);
        if (usuarioOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Usuario con ID " + id + " no encontrado.");
            return "redirect:/";
        }
        
        Usuario usuarioAEditar = usuarioOptional.get();
        model.addAttribute("usuario", usuarioAEditar);
        model.addAttribute("generos", GeneroUsuario.values());
        model.addAttribute("mediosPago", MedioPago.values());
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            model.addAttribute("roles", RolUsuario.values());
        }
        return "users/usuarioForm";
    }

    @GetMapping(value = "/listar")
    @PreAuthorize("hasRole('ADMIN')")
    public String listarUsuarios(Model model) {
        List<Usuario> usuariosAListar = userService.getUsuarios();
        model.addAttribute("usuarios", usuariosAListar);
        model.addAttribute("tituloListado", "Todos los Usuarios (Administrador)");
        return "users/usuarios";
    }

    @GetMapping(value = "/listar/alumnos")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public String listarSoloAlumnos(Model model) {
        List<Usuario> alumnos = userService.getUsuariosByRol(RolUsuario.ALUMNO);
        model.addAttribute("usuarios", alumnos);
        model.addAttribute("tituloListado", "Alumnos del Sistema");
        return "users/usuarios";
    }
    
    @GetMapping("/mi-perfil")
    @PreAuthorize("isAuthenticated()")
    public String verMiPerfil(Authentication authentication) {
        Long userId = userService.getUsuarioByEmail(authentication.getName()).map(Usuario::getId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return "redirect:/users/editar/" + userId;
    }
    
    @GetMapping("/alumno/mis-clases")
    @PreAuthorize("hasRole('ALUMNO')")
    public String mostrarMisClases(Model model, Authentication authentication) {
        Long userId = userService.getUsuarioByEmail(authentication.getName()).map(Usuario::getId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Optional<Usuario> alumnoOptional = userService.getUsuarioById(userId);
        
        Usuario alumno = alumnoOptional.get();
        List<Clase> clasesInscritas = claseService.findClasesByAlumno(alumno);
        
        model.addAttribute("clasesInscritas", clasesInscritas);
        return "alumno/mis-clases";
    }
}