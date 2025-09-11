package com.rollerspeed.rollerspeed.Service;

import com.rollerspeed.rollerspeed.Repository.AsistenciaRepository;
import com.rollerspeed.rollerspeed.model.Asistencia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AsistenciaService {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    public List<Asistencia> getAllAsistencias() {
        return asistenciaRepository.findAll();
    }

    public Optional<Asistencia> getAsistenciaById(Long id) {
        return asistenciaRepository.findById(id);
    }
    
    public List<Asistencia> getAsistenciasByClase(Long claseId) {
        return asistenciaRepository.findByClaseId(claseId);
    }
    
    public List<Asistencia> getAsistenciasByAlumnoAndFechas(Long alumnoId, LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio != null && fechaFin != null) {
            return asistenciaRepository.findByAlumnoIdAndFechaBetween(alumnoId, fechaInicio, fechaFin);
        } else if (fechaInicio != null) {
            return asistenciaRepository.findByAlumnoIdAndFechaAfter(alumnoId, fechaInicio);
        } else if (fechaFin != null) {
            return asistenciaRepository.findByAlumnoIdAndFechaBefore(alumnoId, fechaFin);
        }
        return asistenciaRepository.findByAlumnoId(alumnoId);
    }
}