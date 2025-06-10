package com.rollerspeed.rollerspeed.Service;

import com.rollerspeed.rollerspeed.Repository.AsistenciaRepository;
import com.rollerspeed.rollerspeed.model.Asistencia;
import com.rollerspeed.rollerspeed.model.Clase;
import com.rollerspeed.rollerspeed.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AsistenciaService {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private UsuarioService usuarioService; // Necesario para buscar alumnos
    @Autowired
    private ClaseService claseService; // Necesario para buscar clases

    public Asistencia registrarAsistencia(Long alumnoId, Long claseId, boolean presente) {
        Optional<Usuario> alumnoOptional = usuarioService.getUsuarioById(alumnoId);
        Optional<Clase> claseOptional = claseService.getClaseById(claseId);

        if (alumnoOptional.isPresent() && claseOptional.isPresent()) {
            Usuario alumno = alumnoOptional.get();
            Clase clase = claseOptional.get();

            // Opcional: Verificar si el alumno realmente está inscrito en esta clase
            if (!clase.getAlumnos().contains(alumno)) {
                // Manejar el caso donde el alumno no está inscrito en la clase
                // Podrías lanzar una excepción o devolver null
                return null; // O throw new IllegalArgumentException("Alumno no inscrito en esta clase.");
            }

            LocalDateTime now = LocalDateTime.now();
            // Buscar si ya hay un registro de asistencia para este alumno en esta clase para hoy
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);

            Optional<Asistencia> existingAsistencia = asistenciaRepository.findByAlumnoAndClaseAndFechaHoraRegistroBetween(alumno, clase, startOfDay, endOfDay);

            if (existingAsistencia.isPresent()) {
                // Si ya existe un registro hoy, actualizarlo
                Asistencia asistencia = existingAsistencia.get();
                asistencia.setPresente(presente);
                asistencia.setFechaHoraRegistro(now); // Actualizar la hora de registro
                return asistenciaRepository.save(asistencia);
            } else {
                // Si no existe, crear uno nuevo
                Asistencia nuevaAsistencia = new Asistencia(alumno, clase, now, presente);
                return asistenciaRepository.save(nuevaAsistencia);
            }

        }
        return null; // O lanzar una excepción si alumno o clase no existen
    }

    // --- Métodos para Reportes ---

    public List<Asistencia> getAsistenciasByAlumno(Long alumnoId) {
        Optional<Usuario> alumnoOptional = usuarioService.getUsuarioById(alumnoId);
        return alumnoOptional.map(asistenciaRepository::findByAlumnoOrderByFechaHoraRegistroDesc).orElse(null);
    }

    public List<Asistencia> getAsistenciasByClase(Long claseId) {
        Optional<Clase> claseOptional = claseService.getClaseById(claseId);
        return claseOptional.map(asistenciaRepository::findByClaseOrderByFechaHoraRegistroDesc).orElse(null);
    }

    public List<Asistencia> getAsistenciasByAlumnoAndDateRange(Long alumnoId, LocalDate startDate, LocalDate endDate) {
        Optional<Usuario> alumnoOptional = usuarioService.getUsuarioById(alumnoId);
        if (alumnoOptional.isPresent()) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            return asistenciaRepository.findByAlumnoAndFechaHoraRegistroBetweenOrderByFechaHoraRegistroDesc(alumnoOptional.get(), startDateTime, endDateTime);
        }
        return null;
    }
}