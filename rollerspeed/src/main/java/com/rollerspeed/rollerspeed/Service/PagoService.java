package com.rollerspeed.rollerspeed.Service;

import com.rollerspeed.rollerspeed.Repository.PagoRepository;
import com.rollerspeed.rollerspeed.model.Pago;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    public List<Pago> getAllPagos() {
        return pagoRepository.findAll();
    }

    public Optional<Pago> getPagoById(Long id) {
        return pagoRepository.findById(id);
    }

    public Pago guardarPago(Pago pago) {
        return pagoRepository.save(pago);
    }
    
    public void eliminarPago(Long id) {
        if (!pagoRepository.existsById(id)) {
            throw new IllegalStateException("El pago con ID " + id + " no existe.");
        }
        pagoRepository.deleteById(id);
    }
}