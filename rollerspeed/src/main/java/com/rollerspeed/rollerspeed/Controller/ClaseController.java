package com.rollerspeed.rollerspeed.Controller;

import com.rollerspeed.rollerspeed.Service.ClaseService;
import com.rollerspeed.rollerspeed.Service.UsuarioService;
import com.rollerspeed.rollerspeed.model.Clase;
import com.rollerspeed.rollerspeed.model.Usuario;
import com.rollerspeed.rollerspeed.model.RolUsuario;

import org.springframework.validation.BindingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // <-- Podrías quitar esta importación si ya no la usas en este controlador
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/clases")
public class ClaseController {

    @Autowired
    private ClaseService claseService;

    @Autowired
    private UsuarioService usuarioService;

    // Método para mostrar todas las clases (accesible por ADMIN y PROFESOR)
    @GetMapping("/listarClases")
    public String listarClases(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        
        // --- INICIO: Lógica de verificación de userId en sesión ---
        Object userIdObj = session.getAttribute("userId");
        Long userIdEnSesion; // Declaración sin inicialización

        if (userIdObj instanceof Long) {
            userIdEnSesion = (Long) userIdObj; // Asignación segura
        } else {
            // Si userId no es un Long o es nulo, la sesión es inválida.
            System.err.println("Advertencia: 'userId' en la sesión no es de tipo Long o es nulo. Tipo actual: " + (userIdObj != null ? userIdObj.getClass().getName() : "null"));
            redirectAttributes.addFlashAttribute("error", "Sesión inválida o expirada. Por favor, vuelve a iniciar sesión.");
            return "redirect:/login"; 
        }
        // --- FIN: Lógica de verificación ---

        if (rolEnSesion == null || (rolEnSesion != RolUsuario.ADMIN && rolEnSesion != RolUsuario.PROFESOR)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Inicia sesión como Administrador o Profesor.");
            return "redirect:/login";
        }

        List<Clase> clases;
        if (rolEnSesion == RolUsuario.ADMIN) {
            clases = claseService.getAllClases();
        } else { // RolUsuario.PROFESOR
            Optional<Usuario> profesorOptional = usuarioService.getUsuarioById(userIdEnSesion); // userIdEnSesion es seguro aquí
            if (profesorOptional.isPresent()) {
                clases = claseService.findClasesByProfesor(profesorOptional.get());
            } else {
                clases = List.of(); 
            }
        }
        model.addAttribute("clases", clases);
        model.addAttribute("rolActual", rolEnSesion);
        return "clases/listarClases";
    }

    // Método para mostrar el formulario de creación/edición de clase
    @GetMapping("/nueva")
    public String mostrarFormularioClase(Model model, @RequestParam(required = false) Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        
        // --- INICIO: Lógica de verificación de userId en sesión ---
        Object userIdObj = session.getAttribute("userId");
        Long userIdEnSesion; 

        if (userIdObj instanceof Long) {
            userIdEnSesion = (Long) userIdObj;
        } else {
            System.err.println("Advertencia: 'userId' en la sesión no es de tipo Long o es nulo. Tipo actual: " + (userIdObj != null ? userIdObj.getClass().getName() : "null"));
            redirectAttributes.addFlashAttribute("error", "Sesión inválida o expirada. Por favor, vuelve a iniciar sesión.");
            return "redirect:/login"; 
        }
        // --- FIN: Lógica de verificación ---

        if (rolEnSesion == null || !rolEnSesion.equals(RolUsuario.ADMIN)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Solo administradores pueden crear/editar clases.");
            return "redirect:/login";
        }

        Clase clase = new Clase();
        if (id != null) {
            Optional<Clase> optionalClase = claseService.getClaseById(id);
            if (optionalClase.isPresent()) {
                clase = optionalClase.get();
            } else {
                redirectAttributes.addFlashAttribute("error", "Clase no encontrada.");
                return "redirect:/clases";
            }
        }

        model.addAttribute("clase", clase);
        model.addAttribute("profesores", usuarioService.getUsuariosByRol(RolUsuario.PROFESOR));
        model.addAttribute("editMode", id != null);
        return "clases/claseForm";
    }

    // Método para guardar una clase (crear o actualizar)
    @PostMapping("/guardar")
    public String guardarClase(@Valid @ModelAttribute("clase") Clase clase, // <-- @ModelAttribute("clase") es buena práctica, pero no estrictamente necesario si el nombre de la variable coincide
                               BindingResult result,
                               Model model,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol"); 
        if (rolEnSesion == null || !rolEnSesion.equals(RolUsuario.ADMIN)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Solo administradores pueden guardar clases.");
            return "redirect:/login";
        }

        // Si hay errores de @Valid en el objeto 'clase' (incluyendo el profesor @NotNull)
        if (result.hasErrors()) {
            model.addAttribute("profesores", usuarioService.getUsuariosByRol(RolUsuario.PROFESOR));
            model.addAttribute("editMode", clase.getId() != null);
            return "clases/claseForm";
        }

        try {
            // AHORA Spring debería haber mapeado el profesor directamente en el objeto 'clase'
            // Ya no necesitas el @RequestParam("profesorId") ni buscar el profesor por ID
            // si el campo 'name' en tu HTML es "profesor" (como en <select name="profesor">)
            // Y el campo en tu Clase.java es 'private Usuario profesor;'
            
            // Sin embargo, si quieres UNA VALIDACIÓN ADICIONAL de que el profesor sea de rol PROFESOR,
            // puedes hacerlo usando clase.getProfesor().getId()
            if (clase.getProfesor() == null || clase.getProfesor().getId() == null) {
                 // Esto no debería ocurrir si @NotNull está en Clase.java y el HTML es correcto
                 // pero como fallback, podrías añadir un error aquí si el binding falla inesperadamente
                result.rejectValue("profesor", "clase.profesor.missing", "Se debe asignar un profesor a la clase.");
                model.addAttribute("profesores", usuarioService.getUsuariosByRol(RolUsuario.PROFESOR));
                model.addAttribute("editMode", clase.getId() != null);
                return "clases/claseForm";
            }

            Optional<Usuario> profesorOptional = usuarioService.getUsuarioById(clase.getProfesor().getId());
            if (profesorOptional.isEmpty() || profesorOptional.get().getRol() != RolUsuario.PROFESOR) {
                result.rejectValue("profesor", "clase.profesor.invalid", "El profesor seleccionado no es válido o no tiene el rol de profesor.");
                model.addAttribute("profesores", usuarioService.getUsuariosByRol(RolUsuario.PROFESOR));
                model.addAttribute("editMode", clase.getId() != null);
                return "clases/claseForm";
            }
            // Asigna el profesor cargado de la BD para asegurar que el objeto esté en estado persistente y completo
            clase.setProfesor(profesorOptional.get()); 

            claseService.saveClase(clase);
            redirectAttributes.addFlashAttribute("mensaje", "Clase guardada exitosamente!");
        } catch (IllegalArgumentException e) {
            // Este catch es para errores de lógica de negocio, como las fechas
            result.rejectValue("fechaFin", "clase.fechaFin.invalid", e.getMessage());
            model.addAttribute("profesores", usuarioService.getUsuariosByRol(RolUsuario.PROFESOR));
            model.addAttribute("editMode", clase.getId() != null);
            return "clases/claseForm";
        } catch (Exception e) {
            // Captura cualquier otra excepción inesperada
            redirectAttributes.addFlashAttribute("error", "Error al guardar la clase: " + e.getMessage());
        }
        return "redirect:/clases";
    }

    // Método para eliminar una clase
    @PostMapping("/eliminar/{id}")
    public String eliminarClase(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        if (rolEnSesion == null || !rolEnSesion.equals(RolUsuario.ADMIN)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Solo administradores pueden eliminar clases.");
            return "redirect:/login";
        }
        try {
            claseService.deleteClase(id);
            redirectAttributes.addFlashAttribute("mensaje", "Clase eliminada con éxito!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la clase: " + e.getMessage());
        }
        return "redirect:/clases";
    }

    // ----------------------------------------------------
    // Métodos para inscripción/desinscripción de alumnos por el ADMIN
    // ----------------------------------------------------

    // Muestra el formulario para inscribir/desinscribir usuarios
    @GetMapping("/admin/inscribir")
    public String mostrarFormularioAdminInscripcion(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        // No se necesita userIdEnSesion aquí ya que solo verifica el rol ADMIN
        if (rolEnSesion == null || !rolEnSesion.equals(RolUsuario.ADMIN)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Solo administradores pueden gestionar inscripciones.");
            return "redirect:/login";
        }

        List<Usuario> alumnos = usuarioService.getUsuariosByRol(RolUsuario.ALUMNO);
        List<Clase> clases = claseService.getAllClases();

        model.addAttribute("alumnos", alumnos);
        model.addAttribute("clases", clases);
        model.addAttribute("rolActual", rolEnSesion);
        return "clases/adminInscripcionForm"; 
    }

    // NUEVO MÉTODO: Procesar la inscripción de un usuario a una clase por el ADMIN
    @PostMapping("/admin/inscribir")
    public String inscribirUsuarioAdmin(
            @RequestParam("alumnoId") Long alumnoId,
            @RequestParam("claseId") Long claseId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        // No se necesita userIdEnSesion aquí ya que solo verifica el rol ADMIN
        if (rolEnSesion == null || !rolEnSesion.equals(RolUsuario.ADMIN)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Solo administradores pueden inscribir usuarios.");
            return "redirect:/login";
        }

        boolean exito = usuarioService.inscribirUsuarioAClase(alumnoId, claseId);

        if (exito) {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario inscrito exitosamente a la clase.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo realizar la inscripción. El alumno o la clase no existen, el usuario no es un alumno, o ya está inscrito.");
        }
        return "redirect:/clases/admin/inscribir"; 
    }

    // NUEVO MÉTODO: Procesar la desinscripción de un usuario de una clase por el ADMIN
    @PostMapping("/admin/desinscribir")
    public String desinscribirUsuarioAdmin(
            @RequestParam("alumnoId") Long alumnoId,
            @RequestParam("claseId") Long claseId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        // No se necesita userIdEnSesion aquí ya que solo verifica el rol ADMIN
        if (rolEnSesion == null || !rolEnSesion.equals(RolUsuario.ADMIN)) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Solo administradores pueden desinscribir usuarios.");
            return "redirect:/login";
        }

        boolean exito = usuarioService.desinscribirUsuarioDeClase(alumnoId, claseId);

        if (exito) {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario desinscrito exitosamente de la clase.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo realizar la desinscripción. El alumno o la clase no existen, o el usuario no está inscrito en esta clase.");
        }
        return "redirect:/clases/admin/inscribir";
    }

    // Mostrar las clases inscritas de un alumno específico (para el perfil del alumno o admin/profesor consultando)
    @GetMapping("/alumno/{alumnoId}/mis-clases")
    public String mostrarClasesInscritasAlumno(@PathVariable Long alumnoId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        
        // --- INICIO: Lógica de verificación de userId en sesión ---
        Object userIdObj = session.getAttribute("userId");
        Long userIdEnSesion; 

        if (userIdObj instanceof Long) {
            userIdEnSesion = (Long) userIdObj;
        } else {
            System.err.println("Advertencia: 'userId' en la sesión no es de tipo Long o es nulo. Tipo actual: " + (userIdObj != null ? userIdObj.getClass().getName() : "null"));
            redirectAttributes.addFlashAttribute("error", "Sesión inválida o expirada. Por favor, vuelve a iniciar sesión.");
            return "redirect:/login"; 
        }
        // --- FIN: Lógica de verificación ---

        // Permite a ADMIN o al propio ALUMNO ver sus clases
        if (rolEnSesion == null || (!rolEnSesion.equals(RolUsuario.ADMIN) && !(rolEnSesion.equals(RolUsuario.ALUMNO) && userIdEnSesion.equals(alumnoId)))) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. No tienes permiso para ver estas clases.");
            return "redirect:/login";
        }

        Optional<Usuario> alumnoOptional = usuarioService.getUsuarioById(alumnoId);
        if (alumnoOptional.isEmpty() || alumnoOptional.get().getRol() != RolUsuario.ALUMNO) {
            redirectAttributes.addFlashAttribute("error", "Alumno no encontrado o rol incorrecto.");
            return "redirect:/"; 
        }

        Usuario alumno = alumnoOptional.get();
        List<Clase> clasesInscritas = claseService.findClasesByAlumno(alumno);

        model.addAttribute("alumno", alumno);
        model.addAttribute("clasesInscritas", clasesInscritas);
        model.addAttribute("rolActual", rolEnSesion);
        return "clases/misClases"; 
    }

    // Método para mostrar las clases que imparte un profesor específico
    @GetMapping("/profesor/{profesorId}/mis-clases")
    public String mostrarClasesImpartidasProfesor(@PathVariable Long profesorId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        
        // --- INICIO: Lógica de verificación de userId en sesión ---
        Object userIdObj = session.getAttribute("userId");
        Long userIdEnSesion; 

        if (userIdObj instanceof Long) {
            userIdEnSesion = (Long) userIdObj;
        } else {
            System.err.println("Advertencia: 'userId' en la sesión no es de tipo Long o es nulo. Tipo actual: " + (userIdObj != null ? userIdObj.getClass().getName() : "null"));
            redirectAttributes.addFlashAttribute("error", "Sesión inválida o expirada. Por favor, vuelve a iniciar sesión.");
            return "redirect:/login"; 
        }
        // --- FIN: Lógica de verificación ---

        // Permite a ADMIN o al propio PROFESOR ver sus clases
        if (rolEnSesion == null || (!rolEnSesion.equals(RolUsuario.ADMIN) && !(rolEnSesion.equals(RolUsuario.PROFESOR) && userIdEnSesion.equals(profesorId)))) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. No tienes permiso para ver estas clases.");
            return "redirect:/login";
        }

        Optional<Usuario> profesorOptional = usuarioService.getUsuarioById(profesorId);
        if (profesorOptional.isEmpty() || profesorOptional.get().getRol() != RolUsuario.PROFESOR) {
            redirectAttributes.addFlashAttribute("error", "Profesor no encontrado o rol incorrecto.");
            return "redirect:/"; 
        }

        Usuario profesor = profesorOptional.get();
        List<Clase> clasesImpartidas = claseService.findClasesByProfesor(profesor);

        model.addAttribute("profesor", profesor);
        model.addAttribute("clasesImpartidas", clasesImpartidas);
        model.addAttribute("rolActual", rolEnSesion);
        return "clases/profesorClases"; 
    }
}