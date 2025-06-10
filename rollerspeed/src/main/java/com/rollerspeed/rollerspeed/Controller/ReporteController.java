package com.rollerspeed.rollerspeed.Controller;

import com.rollerspeed.rollerspeed.Service.AsistenciaService;
import com.rollerspeed.rollerspeed.Service.UsuarioService;
import com.rollerspeed.rollerspeed.Service.ClaseService;
import com.rollerspeed.rollerspeed.model.Asistencia;
import com.rollerspeed.rollerspeed.model.Clase;
import com.rollerspeed.rollerspeed.model.Usuario;
import com.rollerspeed.rollerspeed.model.RolUsuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private AsistenciaService asistenciaService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private ClaseService claseService;

    @GetMapping("/asistencia/alumno")
    public String mostrarFormularioReporteAlumno(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        if (rolEnSesion == null || (!rolEnSesion.equals(RolUsuario.ADMIN) && !rolEnSesion.equals(RolUsuario.PROFESOR) && !rolEnSesion.equals(RolUsuario.ALUMNO))) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. No tienes permiso para ver reportes de asistencia.");
            return "redirect:/login";
        }
        
        List<Usuario> alumnos = new ArrayList<>();
        if (rolEnSesion.equals(RolUsuario.ADMIN) || rolEnSesion.equals(RolUsuario.PROFESOR)) {
            alumnos = usuarioService.getUsuarios().stream()
                .filter(u -> u.getRol().equals(RolUsuario.ALUMNO))
                .collect(Collectors.toList());
        } else if (rolEnSesion.equals(RolUsuario.ALUMNO)) {
            Long userId = (Long) session.getAttribute("userId");
            usuarioService.getUsuarioById(userId).ifPresent(alumnos::add);
        }

        model.addAttribute("alumnos", alumnos);
        model.addAttribute("rolActual", rolEnSesion);
        return "reportes/reporteAsistenciaAlumnoForm";
    }

    @GetMapping("/asistencia/alumno/{alumnoId}")
    public String generarReporteAsistenciaAlumno(@PathVariable Long alumnoId,
                                                 @RequestParam(value = "fechaInicio", required = false) String fechaInicioStr,
                                                 @RequestParam(value = "fechaFin", required = false) String fechaFinStr,
                                                 Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        Long userIdInSession = (Long) session.getAttribute("userId");

        if (rolEnSesion == null ||
            (!rolEnSesion.equals(RolUsuario.ADMIN) && !rolEnSesion.equals(RolUsuario.PROFESOR) &&
             (rolEnSesion.equals(RolUsuario.ALUMNO) && !userIdInSession.equals(alumnoId)))) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. No tienes permiso para ver este reporte.");
            return "redirect:/login";
        }

        Optional<Usuario> alumnoOptional = usuarioService.getUsuarioById(alumnoId);
        if (alumnoOptional.isEmpty() || !alumnoOptional.get().getRol().equals(RolUsuario.ALUMNO)) {
            redirectAttributes.addFlashAttribute("error", "Alumno no encontrado o rol incorrecto.");
            return "redirect:/reportes/asistencia/alumno";
        }

        List<Asistencia> asistencias;
        LocalDate fechaInicio = null;
        LocalDate fechaFin = null;

        try {
            if (fechaInicioStr != null && !fechaInicioStr.isEmpty()) {
                fechaInicio = LocalDate.parse(fechaInicioStr);
            }
            if (fechaFinStr != null && !fechaFinStr.isEmpty()) {
                fechaFin = LocalDate.parse(fechaFinStr);
            }
        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("error", "Formato de fecha inválido. Por favor, use YYYY-MM-DD.");
            return "redirect:/reportes/asistencia/alumno/" + alumnoId;
        }

        if (fechaInicio != null && fechaFin != null) {
            asistencias = asistenciaService.getAsistenciasByAlumnoAndDateRange(alumnoId, fechaInicio, fechaFin);
        } else {
            asistencias = asistenciaService.getAsistenciasByAlumno(alumnoId);
        }

        model.addAttribute("alumno", alumnoOptional.get());
        model.addAttribute("asistencias", asistencias);
        model.addAttribute("rolActual", rolEnSesion);
        model.addAttribute("fechaInicio", fechaInicioStr);
        model.addAttribute("fechaFin", fechaFinStr);
        return "reportes/reporteAsistenciaAlumno";
    }

    @GetMapping("/asistencia/clase")
    public String mostrarFormularioReporteClase(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        if (rolEnSesion == null || (!rolEnSesion.equals(RolUsuario.ADMIN) && !rolEnSesion.equals(RolUsuario.PROFESOR))) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. Solo administradores y profesores pueden ver reportes por clase.");
            return "redirect:/login";
        }
        List<Clase> clases = claseService.getAllClases();
        model.addAttribute("clases", clases);
        model.addAttribute("rolActual", rolEnSesion);
        return "reportes/reporteAsistenciaClaseForm";
    }

    @GetMapping("/asistencia/clase/{claseId}")
    public String generarReporteAsistenciaClase(@PathVariable Long claseId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        RolUsuario rolEnSesion = (RolUsuario) session.getAttribute("userRol");
        if (rolEnSesion == null || (!rolEnSesion.equals(RolUsuario.ADMIN) && !rolEnSesion.equals(RolUsuario.PROFESOR))) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. No tienes permiso para ver este reporte.");
            return "redirect:/login";
        }

        Optional<Clase> claseOptional = claseService.getClaseById(claseId);
        if (claseOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Clase no encontrada.");
            return "redirect:/reportes/asistencia/clase";
        }
        
        if (rolEnSesion.equals(RolUsuario.PROFESOR) && !claseOptional.get().getProfesor().getId().equals(session.getAttribute("userId"))) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. No eres el profesor de esta clase.");
            return "redirect:/dashboard/profesor";
        }

        List<Asistencia> asistencias = asistenciaService.getAsistenciasByClase(claseId);

        model.addAttribute("clase", claseOptional.get());
        model.addAttribute("asistencias", asistencias);
        model.addAttribute("rolActual", rolEnSesion);
        return "reportes/reporteAsistenciaClase";
    }
}