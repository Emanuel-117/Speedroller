package com.rollerspeed.rollerspeed.Controller;

import com.rollerspeed.rollerspeed.Service.UsuarioService;
import com.rollerspeed.rollerspeed.Service.ClaseService;
import com.rollerspeed.rollerspeed.model.Clase;
import com.rollerspeed.rollerspeed.model.GeneroUsuario;
import com.rollerspeed.rollerspeed.model.MedioPago;
import com.rollerspeed.rollerspeed.model.RolUsuario;
import com.rollerspeed.rollerspeed.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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

    // --- Métodos de Gestión de Usuarios ---

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoUsuarioAdmin(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        if (rolEnSesion == null || !rolEnSesion.equals(RolUsuario.ADMIN)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Solo administradores pueden registrar usuarios con roles específicos.");
            return "redirect:/login";
        }
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", RolUsuario.values()); // Aunque en el HTML se usa T().values(), esta línea no daña.
        model.addAttribute("generos", GeneroUsuario.values());
        model.addAttribute("mediosPago", MedioPago.values());
        model.addAttribute("rolActual", rolEnSesion);
        return "users/usuarioForm";
    }

    @GetMapping("/registrar-alumno")
    public String mostrarFormularioRegistroAlumno(Model model) {
        Usuario nuevoAlumno = new Usuario();
        nuevoAlumno.setRol(RolUsuario.ALUMNO);
        model.addAttribute("usuario", nuevoAlumno);
        model.addAttribute("generos", GeneroUsuario.values());
        model.addAttribute("mediosPago", MedioPago.values());
        // No pasar rolActual aquí intencionalmente para que no se muestre el select de rol
        return "users/usuarioForm";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute("usuario") Usuario usuario,
                                 HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        Long userIdInSession = (Long) session.getAttribute("userId");

        boolean isSelfEdit = (usuario.getId() != null && userIdInSession != null && userIdInSession.equals(usuario.getId()));
        boolean isAdminCreatingOrEditing = (rolEnSesion != null && rolEnSesion.equals(RolUsuario.ADMIN));
        boolean isPublicRegistration = (usuario.getId() == null && rolEnSesion == null); // Un registro donde no hay sesión

        if (!isSelfEdit && !isAdminCreatingOrEditing && !isPublicRegistration) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para guardar este usuario.");
            return "redirect:/login";
        }

        // Manejo de la contraseña al editar un usuario existente
        if (usuario.getId() != null) { // Si es una edición
            Optional<Usuario> existingUserOpt = userService.getUsuarioById(usuario.getId());
            if (existingUserOpt.isPresent()) {
                Usuario existingUser = existingUserOpt.get();
                // Si la contraseña en el formulario está vacía, mantener la existente
                if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                    usuario.setPassword(existingUser.getPassword());
                }
                // Si no es un ADMIN editando a otro, el rol no puede ser cambiado
                if (!isAdminCreatingOrEditing) {
                    usuario.setRol(existingUser.getRol());
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Usuario a editar no encontrado.");
                // Redirigir a algún lugar sensato, por ejemplo, la lista de usuarios o el dashboard
                return "redirect:/dashboard/admin";
            }
        } else { // Si es un nuevo registro
             if (isPublicRegistration) {
                 usuario.setRol(RolUsuario.ALUMNO); // Siempre es ALUMNO para el registro público
             }
             // Si no es registro público y no es admin, esto no debería pasar si la lógica de acceso es correcta.
        }

        userService.guardaUsuario(usuario);
        redirectAttributes.addFlashAttribute("mensaje", "Usuario guardado exitosamente!");

        if (isSelfEdit) {
            if (rolEnSesion != null) {
                if (rolEnSesion.equals(RolUsuario.ADMIN)) return "redirect:/dashboard/admin";
                if (rolEnSesion.equals(RolUsuario.PROFESOR)) return "redirect:/dashboard/profesor";
                if (rolEnSesion.equals(RolUsuario.ALUMNO)) return "redirect:/dashboard/alumno";
            }
        } else if (isAdminCreatingOrEditing) {
            return "redirect:/dashboard/admin"; // Un admin que guarda un usuario vuelve al dashboard admin
        } else if (isPublicRegistration) {
            redirectAttributes.addFlashAttribute("mensaje", "¡Registro exitoso! Ya puedes iniciar sesión con tu email y contraseña.");
            return "redirect:/login";
        }
        return "redirect:/login"; // Fallback si no se cumple ninguna condición
    }

    // El método de eliminación está bien en el controlador, pero necesita que el HTML use un formulario POST.
    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id,
                                   HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        Long userIdInSession = (Long) session.getAttribute("userId"); // Añadido para prevenir que un admin se elimine a sí mismo

        // Prohibir que un admin se elimine a sí mismo
        if (userIdInSession != null && userIdInSession.equals(id)) {
            redirectAttributes.addFlashAttribute("error", "No puedes eliminar tu propia cuenta de administrador.");
            return "redirect:/dashboard/admin";
        }

        if (rolEnSesion == null || !rolEnSesion.equals(RolUsuario.ADMIN)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Solo administradores pueden eliminar usuarios.");
            return "redirect:/login";
        }
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
    public String mostrarFormularioEditarUsuario(@PathVariable Long id,
                                                 Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        Long userIdInSession = (Long) session.getAttribute("userId");

        if (rolEnSesion == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para acceder a esta función.");
            return "redirect:/login";
        }

        Optional<Usuario> usuarioOptional = userService.getUsuarioById(id);
        if (!usuarioOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Usuario con ID " + id + " no encontrado.");
            // Redirige al dashboard apropiado si el usuario a editar no existe
            if (rolEnSesion.equals(RolUsuario.ADMIN)) return "redirect:/dashboard/admin";
            if (rolEnSesion.equals(RolUsuario.PROFESOR)) return "redirect:/dashboard/profesor";
            if (rolEnSesion.equals(RolUsuario.ALUMNO)) return "redirect:/dashboard/alumno";
            return "redirect:/login";
        }

        Usuario usuarioAEditar = usuarioOptional.get();

        // Lógica de autorización para editar
        if (rolEnSesion.equals(RolUsuario.ADMIN)) {
            // ADMIN puede editar a cualquiera
            model.addAttribute("usuario", usuarioAEditar);
            model.addAttribute("roles", RolUsuario.values()); // ADMIN puede ver y seleccionar todos los roles
            model.addAttribute("generos", GeneroUsuario.values());
            model.addAttribute("mediosPago", MedioPago.values());
            model.addAttribute("rolActual", rolEnSesion);
            return "users/usuarioForm";
        } else if (userIdInSession != null && userIdInSession.equals(id)) {
            // Otros roles (PROFE/ALUMNO) solo pueden editar su propio perfil
            model.addAttribute("usuario", usuarioAEditar);
            // Para edición de perfil propio, el select de rol no debe mostrarse, por lo que no pasamos "roles"
            model.addAttribute("generos", GeneroUsuario.values());
            model.addAttribute("mediosPago", MedioPago.values());
            model.addAttribute("rolActual", rolEnSesion); // Pasa rolActual para el th:if en HTML
            return "users/usuarioForm";
        } else {
            // Intento de editar otro perfil sin ser ADMIN
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para editar este perfil.");
            if (rolEnSesion.equals(RolUsuario.PROFESOR)) return "redirect:/dashboard/profesor";
            if (rolEnSesion.equals(RolUsuario.ALUMNO)) return "redirect:/dashboard/alumno";
            return "redirect:/login";
        }
    }

    @GetMapping(value = "/listar")
    public String listarUsuarios(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        if (rolEnSesion == null || !rolEnSesion.equals(RolUsuario.ADMIN)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Solo administradores pueden ver la lista completa de usuarios.");
            return "redirect:/login";
        }
        List<Usuario> usuariosAListar = userService.getUsuarios();
        model.addAttribute("usuarios", usuariosAListar);
        model.addAttribute("tituloListado", "Todos los Usuarios (Administrador)");
        model.addAttribute("rolActual", rolEnSesion);
        return "users/usuarios";
    }

    @GetMapping(value = "/listar/alumnos")
    public String listarSoloAlumnos(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        if (rolEnSesion == null || (!rolEnSesion.equals(RolUsuario.PROFESOR) && !rolEnSesion.equals(RolUsuario.ADMIN))) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Solo profesores o administradores pueden ver la lista de alumnos.");
            return "redirect:/login";
        }
        List<Usuario> alumnos = userService.getUsuariosByRol(RolUsuario.ALUMNO);
        model.addAttribute("usuarios", alumnos);
        model.addAttribute("tituloListado", "Alumnos del Sistema");
        model.addAttribute("rolActual", rolEnSesion);
        return "users/usuarios";
    }

    @GetMapping("/mi-perfil")
    public String verMiPerfil(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para ver tu perfil.");
            return "redirect:/login";
        }
        return mostrarFormularioEditarUsuario(userId, model, session, redirectAttributes);
    }

    // --- MÉTODO PARA "MIS CLASES INSCRITAS" PARA ALUMNOS ---
    @GetMapping("/alumno/mis-clases")
    public String mostrarMisClases(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");

        if (userId == null || rolEnSesion == null || !rolEnSesion.equals(RolUsuario.ALUMNO)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Debes ser un alumno logueado para ver tus clases.");
            return "redirect:/login";
        }

        Optional<Usuario> alumnoOptional = userService.getUsuarioById(userId);
        if (!alumnoOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Tu perfil de alumno no fue encontrado.");
            return "redirect:/login";
        }

        Usuario alumno = alumnoOptional.get();
        List<Clase> clasesInscritas = claseService.findClasesByAlumno(alumno);

        model.addAttribute("clasesInscritas", clasesInscritas);
        model.addAttribute("rolActual", rolEnSesion);

        return "alumno/mis-clases";
    }
}