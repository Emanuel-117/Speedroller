package com.rollerspeed.rollerspeed.Repository;

import com.rollerspeed.rollerspeed.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    // Puedes añadir métodos personalizados aquí si los necesitas, por ejemplo:
    List<Pago> findByAlumnoId(Long alumnoId);
    List<Pago> findByClaseId(Long claseId);
    List<Pago> findByEstado(String estado);
}