package com.rollerspeed.rollerspeed.Service;

import com.rollerspeed.rollerspeed.Repository.ClaseRepository;
import com.rollerspeed.rollerspeed.model.Clase;
import com.rollerspeed.rollerspeed.model.Usuario; // ¡IMPORTANTE: Asegúrate de que esta importación esté presente!
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Collections; // ¡IMPORTANTE: Asegúrate de que esta importación esté presente para Collections.emptyList()!

@Service
public class ClaseService {

    @Autowired
    private ClaseRepository claseRepository;

    public List<Clase> getAllClases() {
        return claseRepository.findAll();
    }

    public Optional<Clase> getClaseById(Long id) {
        return claseRepository.findById(id);
    }

    @Transactional
    public Clase saveClase(Clase clase) {
        if (clase.getFechaFin() != null && clase.getFechaInicio() != null &&
            clase.getFechaFin().isBefore(clase.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha y hora de fin no puede ser anterior a la fecha y hora de inicio.");
        }
        return claseRepository.save(clase);
    }

    @Transactional
    public void deleteClase(Long id) {
        claseRepository.deleteById(id);
    }

    public List<Clase> getClasesForDay(LocalDateTime fecha) {
        LocalDateTime startOfDay = fecha.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = fecha.toLocalDate().atTime(23, 59, 59);
        return claseRepository.findByFechaInicioBetween(startOfDay, endOfDay);
    }

    /**
     * Busca y devuelve una lista de clases impartidas por un profesor específico.
     *
     * @param profesor El objeto Usuario que representa al profesor.
     * @return Una lista de clases asociadas a ese profesor, o una lista vacía si el profesor es nulo o no tiene clases.
     */
    public List<Clase> findClasesByProfesor(Usuario profesor) {
        if (profesor == null) {
            return Collections.emptyList(); // Devuelve una lista vacía si el profesor es nulo para evitar NullPointerException
        }
        return claseRepository.findByProfesor(profesor);
    }

    /**
     * Busca y devuelve una lista de clases a las que un alumno está inscrito.
     *
     * @param alumno El objeto Usuario que representa al alumno.
     * @return Una lista de clases en las que el alumno está inscrito, o una lista vacía si el alumno es nulo o no está inscrito en clases.
     */
    public List<Clase> findClasesByAlumno(Usuario alumno) {
        if (alumno == null) {
            return Collections.emptyList(); // Devuelve una lista vacía si el alumno es nulo
        }
        return claseRepository.findByAlumnos(alumno);
    }
}