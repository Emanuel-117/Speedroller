package com.rollerspeed.rollerspeed.Repository;

import com.rollerspeed.rollerspeed.model.Asistencia;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    
    List<Asistencia> findByClaseId(Long claseId);
    List<Asistencia> findByAlumnoId(Long alumnoId);
    List<Asistencia> findByAlumnoIdAndFechaBetween(Long alumnoId, LocalDate fechaInicio, LocalDate fechaFin);
    List<Asistencia> findByAlumnoIdAndFechaAfter(Long alumnoId, LocalDate fechaInicio);
    List<Asistencia> findByAlumnoIdAndFechaBefore(Long alumnoId, LocalDate fechaFin);
}