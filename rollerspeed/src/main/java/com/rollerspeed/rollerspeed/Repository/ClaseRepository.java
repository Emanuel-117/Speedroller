package com.rollerspeed.rollerspeed.Repository;

import com.rollerspeed.rollerspeed.model.Clase;
import com.rollerspeed.rollerspeed.model.Usuario; // ¡IMPORTANTE: Añade esta importación!

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, Long> {
    List<Clase> findByProfesor(Usuario profesor); // Este método es clave para la línea que te aparece en rojo
    List<Clase> findByFechaInicioBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);
    List<Clase> findByAlumnos(Usuario alumno); // Necesario para findClasesByAlumno en el servicio
}