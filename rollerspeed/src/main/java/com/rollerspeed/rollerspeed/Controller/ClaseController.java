package com.rollerspeed.rollerspeed.Controller;

import com.rollerspeed.rollerspeed.Service.ClaseService;
import com.rollerspeed.rollerspeed.Service.UsuarioService;
import com.rollerspeed.rollerspeed.model.Clase;
import com.rollerspeed.rollerspeed.model.Usuario;
import com.rollerspeed.rollerspeed.model.RolUsuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/clases")
public class ClaseController {

    @Autowired
    private ClaseService claseService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/listar")
    public String listarClases(Model model) {
        List<Clase> clases = claseService.getAllClases();
        model.addAttribute("clases", clases);
        return "clases/listar";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNuevaClase(Model model) {
        Clase clase = new Clase();
        model.addAttribute("clase", clase);

        List<Usuario> profesores = usuarioService.getUsuarios().stream()
                                                .filter(u -> u.getRol() == RolUsuario.PROFESOR)
                                                .collect(Collectors.toList());
        model.addAttribute("profesores", profesores);

        return "clases/claseForm";
    }

    @PostMapping("/guardar")
    public String guardarClase(@ModelAttribute("clase") Clase clase,
                               @RequestParam("profesorId") Long profesorId,
                               RedirectAttributes redirectAttributes) {
        try {

            Optional<Usuario> optionalProfesor = usuarioService.buscarById(profesorId);
            if (optionalProfesor.isPresent()) {
                clase.setProfesor(optionalProfesor.get());
            } else {
                throw new RuntimeException("Profesor no encontrado con ID: " + profesorId);
            }


            claseService.saveClase(clase);
            redirectAttributes.addFlashAttribute("mensaje", "Clase guardada exitosamente!");

            return "redirect:/clases/calendarioSimple";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/clases/nueva";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar la clase: " + e.getMessage());
            return "redirect:/clases/nueva";
        }
    }

    @GetMapping("/{usuarioId}/mis-clases")
    public String verMisClases(@PathVariable Long usuarioId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Usuario> optionalUsuario = usuarioService.buscarById(usuarioId);
        if (optionalUsuario.isPresent()) {
            Usuario usuario = optionalUsuario.get();
            if (usuario.getRol() == RolUsuario.ALUMNO) {
                model.addAttribute("usuario", usuario);
                model.addAttribute("clasesInscritas", usuario.getClasesInscritas());
                return "clases/misClases";
            } else {
                redirectAttributes.addFlashAttribute("error", "Acceso denegado: Solo los alumnos pueden ver sus clases inscritas.");
                return "redirect:/index";
            }
        }
        redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
        return "redirect:/index";
    }

    @PostMapping("/{usuarioId}/inscribir/{claseId}")
    public String inscribirUsuarioAClase(
            @PathVariable Long usuarioId,
            @PathVariable Long claseId,
            RedirectAttributes redirectAttributes) {

        boolean exito = usuarioService.inscribirUsuarioAClase(usuarioId, claseId);

        if (exito) {
            redirectAttributes.addFlashAttribute("mensaje", "¡Inscripción exitosa!");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo realizar la inscripción. Puede que el usuario o la clase no existan, o el usuario no sea un alumno o ya esté inscrito.");
        }
        return "redirect:/clases/" + usuarioId + "/mis-clases";
    }

    @PostMapping("/{usuarioId}/desinscribir/{claseId}")
    public String desinscribirUsuarioDeClase(
            @PathVariable Long usuarioId,
            @PathVariable Long claseId,
            RedirectAttributes redirectAttributes) {
        boolean exito = usuarioService.desinscribirUsuarioDeClase(usuarioId, claseId);
        if (exito) {
            redirectAttributes.addFlashAttribute("mensaje", "¡Desinscripción exitosa!");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo realizar la desinscripción.");
        }
        return "redirect:/clases/" + usuarioId + "/mis-clases";
    }

    @GetMapping("/calendarioSimple")
    public String verCalendarioSimple(@RequestParam(name = "fecha", required = false) String fechaStr, Model model) {
        LocalDate fechaSeleccionada;

        if (fechaStr != null && !fechaStr.isEmpty()) {
            try {
                fechaSeleccionada = LocalDate.parse(fechaStr);
            } catch (Exception e) {
                fechaSeleccionada = LocalDate.now();
                model.addAttribute("errorFecha", "Formato de fecha inválido. Mostrando clases para hoy.");
            }
        } else {
            fechaSeleccionada = LocalDate.now();
        }

        List<Clase> clasesDelDia = claseService.getClasesForDay(fechaSeleccionada.atStartOfDay());

        model.addAttribute("fechaSeleccionada", fechaSeleccionada);
        model.addAttribute("clases", clasesDelDia);

        return "clases/calendarioSimple";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarClase(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            claseService.deleteClase(id);
            redirectAttributes.addFlashAttribute("mensaje", "Clase eliminada exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la clase: " + e.getMessage());
        }

        return "redirect:/clases/listar";
    }

    @GetMapping("/detalles/{id}")
    public String verDetallesClase(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Clase> optionalClase = claseService.getClaseById(id);
        if (optionalClase.isPresent()) {
            Clase clase = optionalClase.get();
            model.addAttribute("clase", clase);
            return "clases/detallesClase";
        } else {
            redirectAttributes.addFlashAttribute("error", "Clase no encontrada con ID: " + id);
            return "redirect:/clases/calendarioSimple";
        }
    }
}