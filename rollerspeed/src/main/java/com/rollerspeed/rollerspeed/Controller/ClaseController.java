package com.rollerspeed.rollerspeed.Controller;

import com.rollerspeed.rollerspeed.Service.ClaseService;
import com.rollerspeed.rollerspeed.Service.UsuarioService;
import com.rollerspeed.rollerspeed.model.Clase;
import com.rollerspeed.rollerspeed.model.Usuario;
import com.rollerspeed.rollerspeed.model.RolUsuario;
import org.springframework.validation.BindingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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

    @GetMapping("/listarClases")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public String listarClases(Model model, Authentication authentication) {
        List<Clase> clases;
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            clases = claseService.getAllClases();
        } else {
            Usuario profesor = usuarioService.getUsuarioByEmail(authentication.getName()).get();
            clases = claseService.findClasesByProfesor(profesor);
        }
        model.addAttribute("clases", clases);
        return "clases/listarClases";
    }

    @GetMapping("/nueva")
    @PreAuthorize("hasRole('ADMIN')")
    public String mostrarFormularioClase(Model model, @RequestParam(required = false) Long id, RedirectAttributes redirectAttributes) {
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

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardarClase(@Valid @ModelAttribute("clase") Clase clase, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("profesores", usuarioService.getUsuariosByRol(RolUsuario.PROFESOR));
            model.addAttribute("editMode", clase.getId() != null);
            return "clases/claseForm";
        }
        claseService.guardarClase(clase);
        redirectAttributes.addFlashAttribute("mensaje", "Clase guardada exitosamente!");
        return "redirect:/clases/listarClases";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminarClase(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        claseService.deleteClase(id);
        redirectAttributes.addFlashAttribute("mensaje", "Clase eliminada exitosamente.");
        return "redirect:/clases/listarClases";
    }

    @GetMapping("/admin/inscribir")
    @PreAuthorize("hasRole('ADMIN')")
    public String mostrarFormularioAdminInscripcion(Model model) {
        List<Usuario> alumnos = usuarioService.getUsuariosByRol(RolUsuario.ALUMNO);
        List<Clase> clases = claseService.getAllClases();
        model.addAttribute("alumnos", alumnos);
        model.addAttribute("clases", clases);
        return "clases/adminInscripcionForm";
    }

    @PostMapping("/admin/inscribir")
    @PreAuthorize("hasRole('ADMIN')")
    public String inscribirUsuarioAdmin(@RequestParam("alumnoId") Long alumnoId, @RequestParam("claseId") Long claseId, RedirectAttributes redirectAttributes) {
        try {
            claseService.inscribirAlumnoAClase(alumnoId, claseId);
            redirectAttributes.addFlashAttribute("mensaje", "Alumno inscrito exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al inscribir al alumno: " + e.getMessage());
        }
        return "redirect:/clases/admin/inscribir";
    }

    @PostMapping("/admin/desinscribir")
    @PreAuthorize("hasRole('ADMIN')")
    public String desinscribirUsuarioAdmin(@RequestParam("alumnoId") Long alumnoId, @RequestParam("claseId") Long claseId, RedirectAttributes redirectAttributes) {
        try {
            claseService.desinscribirAlumnoDeClase(alumnoId, claseId);
            redirectAttributes.addFlashAttribute("mensaje", "Alumno desinscrito exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al desinscribir al alumno: " + e.getMessage());
        }
        return "redirect:/clases/admin/inscribir";
    }

    @GetMapping("/alumno/{alumnoId}/mis-clases")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ALUMNO') and #alumnoId == principal.id)")
    public String mostrarClasesInscritasAlumno(@PathVariable Long alumnoId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Usuario> alumnoOptional = usuarioService.getUsuarioById(alumnoId);
        if (alumnoOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Alumno no encontrado.");
            return "redirect:/";
        }
        Usuario alumno = alumnoOptional.get();
        List<Clase> clasesInscritas = claseService.findClasesByAlumno(alumno);
        model.addAttribute("clasesInscritas", clasesInscritas);
        return "clases/misClases";
    }

    @GetMapping("/profesor/{profesorId}/mis-clases")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROFESOR') and #profesorId == principal.id)")
    public String mostrarClasesImpartidasProfesor(@PathVariable Long profesorId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Usuario> profesorOptional = usuarioService.getUsuarioById(profesorId);
        if (profesorOptional.isEmpty() || profesorOptional.get().getRol() != RolUsuario.PROFESOR) {
            redirectAttributes.addFlashAttribute("error", "Profesor no encontrado o rol incorrecto.");
            return "redirect:/";
        }
        Usuario profesor = profesorOptional.get();
        List<Clase> clasesImpartidas = claseService.findClasesByProfesor(profesor);
        model.addAttribute("clasesImpartidas", clasesImpartidas);
        return "clases/profesorClases";
    }
}