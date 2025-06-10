package com.rollerspeed.rollerspeed.Service;

import com.rollerspeed.rollerspeed.model.Pago;
import com.rollerspeed.rollerspeed.model.Usuario; // Necesario para buscar el alumno
import com.rollerspeed.rollerspeed.model.RolUsuario; // Necesario para filtrar usuarios por rol
import com.rollerspeed.rollerspeed.Repository.PagoRepository;
import com.rollerspeed.rollerspeed.Repository.userRepository; // Repositorio de Usuario (asumiendo este nombre)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importar para gestión de transacciones

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service // Marca esta clase como un componente de servicio de Spring
public class PagoService {

    @Autowired // Inyecta una instancia de PagoRepository
    private PagoRepository pagoRepository;

    @Autowired // Inyecta una instancia de UserRepository (asumiendo este nombre)
    private userRepository userRepository; // Necesario para buscar el alumno

    /**
     * Registra un nuevo pago en el sistema.
     * La fecha de pago se establece automáticamente si no se proporciona.
     * Se valida que el alumno exista antes de guardar el pago.
     *
     * @param pago El objeto Pago a registrar.
     * @return El objeto Pago guardado.
     * @throws IllegalArgumentException Si el alumno asociado al pago no existe.
     */
    @Transactional // Asegura que esta operación se ejecute dentro de una transacción
    public Pago registrarPago(Pago pago) {
        // Validación: Asegurarse de que el monto sea positivo
        if (pago.getMonto() <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor que cero.");
        }

        // Validación: Asegurarse de que el alumno asociado al pago existe
        if (pago.getAlumno() == null || pago.getAlumno().getId() == null) {
            throw new IllegalArgumentException("Se requiere un alumno para registrar un pago.");
        }
        Optional<Usuario> alumnoOptional = userRepository.findById(pago.getAlumno().getId());
        if (alumnoOptional.isEmpty()) {
            throw new IllegalArgumentException("El alumno con ID " + pago.getAlumno().getId() + " no fue encontrado.");
        }
        pago.setAlumno(alumnoOptional.get()); // Asigna el objeto Usuario completo al Pago

        // Si se asocia una clase, valida que exista (opcional, si ClaseService está disponible)
        // if (pago.getClase() != null && pago.getClase().getId() != null) {
        //     claseService.getClaseById(pago.getClase().getId()).ifPresent(pago::setClase);
        // }

        // Asegurarse de que la fecha de pago se establece si no viene del formulario
        if (pago.getFechaPago() == null) {
            pago.setFechaPago(LocalDateTime.now());
        }

        // Establecer el estado inicial si no se especifica (ej. "Pendiente" o "Pagado" si el método es instantáneo)
        if (pago.getEstado() == null || pago.getEstado().isEmpty()) {
            pago.setEstado("Pendiente"); // O "Pagado" si asumes que todos los pagos son instantáneos al registrar
        }

        return pagoRepository.save(pago);
    }

    /**
     * Obtiene un pago por su ID.
     *
     * @param id El ID del pago.
     * @return Un Optional que contiene el Pago si existe, o vacío si no.
     */
    public Optional<Pago> getPagoById(Long id) {
        return pagoRepository.findById(id);
    }

    /**
     * Obtiene una lista de todos los pagos registrados.
     *
     * @return Una lista de objetos Pago.
     */
    public List<Pago> getAllPagos() {
        return pagoRepository.findAll();
    }

    /**
     * Obtiene una lista de pagos de un alumno específico por su ID.
     *
     * @param alumnoId El ID del alumno.
     * @return Una lista de objetos Pago asociados a ese alumno.
     */
    public List<Pago> getPagosByAlumnoId(Long alumnoId) {
        return pagoRepository.findByAlumnoId(alumnoId);
    }

    /**
     * Elimina un pago por su ID.
     *
     * @param id El ID del pago a eliminar.
     * @throws IllegalArgumentException Si el pago no existe.
     */
    @Transactional
    public void eliminarPago(Long id) {
        if (!pagoRepository.existsById(id)) {
            throw new IllegalArgumentException("El pago con ID " + id + " no existe y no puede ser eliminado.");
        }
        pagoRepository.deleteById(id);
    }

    /**
     * Marca un pago como "Pagado".
     *
     * @param id El ID del pago a marcar como pagado.
     * @return El pago actualizado.
     * @throws IllegalArgumentException Si el pago no existe o ya está pagado.
     */
    @Transactional
    public Pago marcarPagoComoPagado(Long id) {
        Optional<Pago> pagoOptional = pagoRepository.findById(id);
        if (pagoOptional.isEmpty()) {
            throw new IllegalArgumentException("Pago con ID " + id + " no encontrado.");
        }
        Pago pago = pagoOptional.get();
        if ("Pagado".equalsIgnoreCase(pago.getEstado())) {
            throw new IllegalArgumentException("El pago con ID " + id + " ya está marcado como Pagado.");
        }
        pago.setEstado("Pagado");
        return pagoRepository.save(pago);
    }

    /**
     * Obtiene una lista de usuarios que tienen el rol de "ALUMNO".
     * Necesario para poblar el selector de alumnos en el formulario de pago.
     *
     * @return Una lista de objetos Usuario con rol ALUMNO.
     */
    public List<Usuario> getAlumnos() {
        // Asegúrate de que RolUsuario.ALUMNO sea el valor correcto para el enum
        return userRepository.findByRol(RolUsuario.ALUMNO);
    }

    // --- Métodos adicionales sugeridos para reportes o notificaciones ---

    /**
     * Obtiene todos los pagos con un estado específico (ej. "Pendiente", "Pagado").
     * @param estado El estado del pago a buscar.
     * @return Lista de pagos con el estado especificado.
     */
    public List<Pago> getPagosByEstado(String estado) {
        return pagoRepository.findByEstado(estado);
    }

    /**
     * Calcula el total de ingresos por pagos en un rango de fechas.
     * @param fechaInicio La fecha de inicio del rango.
     * @param fechaFin La fecha de fin del rango.
     * @return El monto total de pagos en el rango.
     */
    public double getTotalIngresosPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // Esto requeriría un método en PagoRepository como:
        // @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.fechaPago BETWEEN :fechaInicio AND :fechaFin AND p.estado = 'Pagado'")
        // Double sumMontoByFechaPagoBetweenAndEstado(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin, String estado);
        // Si no tienes el método @Query, puedes filtrar en Java, pero es menos eficiente para grandes volúmenes.
        return pagoRepository.findAll().stream()
                .filter(pago -> pago.getFechaPago().isAfter(fechaInicio) && pago.getFechaPago().isBefore(fechaFin) && "Pagado".equalsIgnoreCase(pago.getEstado()))
                .mapToDouble(Pago::getMonto)
                .sum();
    }

    // Nota sobre notificaciones:
    // Las notificaciones automáticas (ej. por pagos pendientes) suelen requerir:
    // 1. Una tarea programada (ej. con @Scheduled de Spring) que se ejecute periódicamente.
    // 2. Un servicio de envío de correos electrónicos (Spring Mail) o SMS.
    // Esto es más avanzado y podría ser una fase posterior del proyecto.
}