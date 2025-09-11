package com.rollerspeed.rollerspeed.Service;

import com.rollerspeed.rollerspeed.Repository.ClaseRepository;
import com.rollerspeed.rollerspeed.Repository.userRepository;
import com.rollerspeed.rollerspeed.model.Clase;
import com.rollerspeed.rollerspeed.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClaseService {

    @Autowired
    private ClaseRepository claseRepository;
    
    @Autowired
    private userRepository usuarioRepository;

    public List<Clase> getAllClases() {
        return claseRepository.findAll();
    }

    public Optional<Clase> getClaseById(Long id) {
        return claseRepository.findById(id);
    }

    public Clase guardarClase(Clase clase) {
        return claseRepository.save(clase);
    }

    public void deleteClase(Long id) {
        claseRepository.deleteById(id);
    }
    
    public List<Clase> findClasesByProfesor(Usuario profesor) {
        return claseRepository.findByProfesor(profesor);
    }
    
    public List<Clase> findClasesByAlumno(Usuario alumno) {
        return claseRepository.findByAlumnosContaining(alumno);
    }
    
    public void inscribirAlumnoAClase(Long alumnoId, Long claseId) {
        Usuario alumno = usuarioRepository.findById(alumnoId).orElseThrow(() -> new IllegalArgumentException("Alumno no encontrado"));
        Clase clase = claseRepository.findById(claseId).orElseThrow(() -> new IllegalArgumentException("Clase no encontrada"));
        
        if (clase.getAlumnos().contains(alumno)) {
            throw new IllegalStateException("El alumno ya está inscrito en esta clase.");
        }
        
        clase.getAlumnos().add(alumno);
        claseRepository.save(clase);
    }
    
    public void desinscribirAlumnoDeClase(Long alumnoId, Long claseId) {
        Usuario alumno = usuarioRepository.findById(alumnoId).orElseThrow(() -> new IllegalArgumentException("Alumno no encontrado"));
        Clase clase = claseRepository.findById(claseId).orElseThrow(() -> new IllegalArgumentException("Clase no encontrada"));

        if (!clase.getAlumnos().contains(alumno)) {
            throw new IllegalStateException("El alumno no está inscrito en esta clase.");
        }

        clase.getAlumnos().remove(alumno);
        claseRepository.save(clase);
    }
    
    public long getTotalClasesInscritas(Long alumnoId) {
        Optional<Usuario> alumnoOptional = usuarioRepository.findById(alumnoId);
        if (alumnoOptional.isPresent()) {
            return claseRepository.countByAlumnosContaining(alumnoOptional.get());
        }
        return 0;
    }
}