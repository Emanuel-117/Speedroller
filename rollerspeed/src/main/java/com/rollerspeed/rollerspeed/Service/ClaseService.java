package com.rollerspeed.rollerspeed.Service;

import com.rollerspeed.rollerspeed.Repository.ClaseRepository;
import com.rollerspeed.rollerspeed.model.Clase;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
        LocalDateTime startOfDay = fecha.toLocalDate().atStartOfDay(); // 2025-05-29 00:00:00
        LocalDateTime endOfDay = fecha.toLocalDate().atTime(23, 59, 59); // 2025-05-29 23:59:59
        return claseRepository.findByFechaInicioBetween(startOfDay, endOfDay);
    }
}
