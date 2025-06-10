package com.rollerspeed.rollerspeed.Repository;

import com.rollerspeed.rollerspeed.model.Asistencia;
import com.rollerspeed.rollerspeed.model.Clase;
import com.rollerspeed.rollerspeed.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    // Buscar asistencias por alumno
    List<Asistencia> findByAlumno(Usuario alumno);

    // Buscar asistencias por clase
    List<Asistencia> findByClase(Clase clase);

    // Buscar asistencias por alumno y clase
    Optional<Asistencia> findByAlumnoAndClaseAndFechaHoraRegistroBetween(Usuario alumno, Clase clase, LocalDateTime startOfDay, LocalDateTime endOfDay);

    // Reporte de asistencia por alumno (todas las asistencias de un alumno)
    List<Asistencia> findByAlumnoOrderByFechaHoraRegistroDesc(Usuario alumno);

    // Reporte de asistencia por clase (todas las asistencias para una clase específica)
    List<Asistencia> findByClaseOrderByFechaHoraRegistroDesc(Clase clase);

    // Reporte de asistencia por alumno y un rango de fechas
    List<Asistencia> findByAlumnoAndFechaHoraRegistroBetweenOrderByFechaHoraRegistroDesc(Usuario alumno, LocalDateTime startDate, LocalDateTime endDate);
}