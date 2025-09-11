package com.rollerspeed.rollerspeed.Repository;

import com.rollerspeed.rollerspeed.model.Clase;
import com.rollerspeed.rollerspeed.model.Usuario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, Long> {
    
    List<Clase> findByProfesor(Usuario profesor);
    List<Clase> findByAlumnosContaining(Usuario alumno);
    long countByAlumnosContaining(Usuario alumno);
}