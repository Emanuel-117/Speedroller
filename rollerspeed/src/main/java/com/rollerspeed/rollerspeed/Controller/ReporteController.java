package com.rollerspeed.rollerspeed.Controller;

import com.rollerspeed.rollerspeed.Service.AsistenciaService;
import com.rollerspeed.rollerspeed.Service.UsuarioService;
import com.rollerspeed.rollerspeed.Service.ClaseService;
import com.rollerspeed.rollerspeed.model.Asistencia;
import com.rollerspeed.rollerspeed.model.Clase;
import com.rollerspeed.rollerspeed.model.Usuario;
import com.rollerspeed.rollerspeed.model.RolUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR', 'ALUMNO')")
    public String mostrarFormularioReporteAlumno(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        RolUsuario rolActual = RolUsuario.valueOf(auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
        String userIdStr = auth.getName();
        Long userId = usuarioService.getUsuarioByEmail(userIdStr).map(Usuario::getId).orElse(null);
        
        List<Usuario> alumnos = new ArrayList<>();
        if (rolActual.equals(RolUsuario.ADMIN) || rolActual.equals(RolUsuario.PROFESOR)) {
            alumnos = usuarioService.getUsuarios().stream()
                .filter(u -> u.getRol().equals(RolUsuario.ALUMNO))
                .collect(Collectors.toList());
        } else if (rolActual.equals(RolUsuario.ALUMNO)) {
            usuarioService.getUsuarioById(userId).ifPresent(alumnos::add);
        }
        model.addAttribute("alumnos", alumnos);
        model.addAttribute("rolActual", rolActual);
        return "reportes/reporteAsistenciaAlumnoForm";
    }

    @GetMapping("/asistencia/alumno/{alumnoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR', 'ALUMNO')")
    public String generarReporteAsistenciaAlumno(@PathVariable Long alumnoId,
                                                 @RequestParam(value = "fechaInicio", required = false) String fechaInicioStr,
                                                 @RequestParam(value = "fechaFin", required = false) String fechaFinStr,
                                                 Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        RolUsuario rolEnSesion = RolUsuario.valueOf(auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
        Long userIdInSession = usuarioService.getUsuarioByEmail(auth.getName()).map(Usuario::getId).orElse(null);

        if (rolEnSesion.equals(RolUsuario.ALUMNO) && !userIdInSession.equals(alumnoId)) {
             redirectAttributes.addFlashAttribute("error", "Acceso denegado. No tienes permiso para ver este reporte.");
             return "redirect:/dashboard/alumno";
        }
        Optional<Usuario> alumnoOptional = usuarioService.getUsuarioById(alumnoId);
        if (alumnoOptional.isEmpty() || !alumnoOptional.get().getRol().equals(RolUsuario.ALUMNO)) {
            redirectAttributes.addFlashAttribute("error", "Alumno no encontrado o rol incorrecto.");
            return "redirect:/reportes/asistencia/alumno";
        }
        
        LocalDate fechaInicio = null;
        if (fechaInicioStr != null && !fechaInicioStr.isEmpty()) {
            try {
                fechaInicio = LocalDate.parse(fechaInicioStr);
            } catch (DateTimeParseException e) {
                redirectAttributes.addFlashAttribute("error", "Formato de fecha de inicio inválido.");
                return "redirect:/reportes/asistencia/alumno/" + alumnoId;
            }
        }
        LocalDate fechaFin = null;
        if (fechaFinStr != null && !fechaFinStr.isEmpty()) {
            try {
                fechaFin = LocalDate.parse(fechaFinStr);
            } catch (DateTimeParseException e) {
                redirectAttributes.addFlashAttribute("error", "Formato de fecha de fin inválido.");
                return "redirect:/reportes/asistencia/alumno/" + alumnoId;
            }
        }
        
        List<Asistencia> asistencias = asistenciaService.getAsistenciasByAlumnoAndFechas(alumnoId, fechaInicio, fechaFin);
        
        long totalClases = claseService.getTotalClasesInscritas(alumnoId);
        long asistenciasContadas = asistencias.stream().filter(Asistencia::isAsistio).count();
        double porcentajeAsistencia = totalClases > 0 ? (asistenciasContadas * 100.0) / totalClases : 0.0;
        
        model.addAttribute("asistencias", asistencias);
        model.addAttribute("alumno", alumnoOptional.get());
        model.addAttribute("porcentajeAsistencia", String.format("%.2f", porcentajeAsistencia));
        model.addAttribute("rolActual", rolEnSesion);
        return "reportes/reporteAsistenciaAlumno";
    }

    @GetMapping("/asistencia/clase")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public String mostrarFormularioReporteClase(Model model) {
        List<Clase> clases = claseService.getAllClases();
        model.addAttribute("clases", clases);
        return "reportes/reporteAsistenciaClaseForm";
    }

    @GetMapping("/asistencia/clase/{claseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public String generarReporteAsistenciaClase(@PathVariable Long claseId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Clase> claseOptional = claseService.getClaseById(claseId);
        if (claseOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Clase no encontrada.");
            return "redirect:/reportes/asistencia/clase";
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        RolUsuario rolEnSesion = RolUsuario.valueOf(auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
        Long userIdInSession = usuarioService.getUsuarioByEmail(auth.getName()).map(Usuario::getId).orElse(null);

        if (rolEnSesion.equals(RolUsuario.PROFESOR) && !claseOptional.get().getProfesor().getId().equals(userIdInSession)) {
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