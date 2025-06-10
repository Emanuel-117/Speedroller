package com.rollerspeed.rollerspeed.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode; // Importar esto si usas @EqualsAndHashCode.Exclude
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Data // Genera getters, setters, toString, equals y hashCode
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double monto;

    @Column(nullable = false)
    private LocalDateTime fechaPago;

    @Column(nullable = false, length = 50)
    private String metodoPago; // Ejemplo: "Tarjeta", "Efectivo", "Transferencia"

    @Column(nullable = false, length = 20)
    private String estado; // Ejemplo: "Pendiente", "Pagado", "Vencido"

    @ManyToOne
    @JoinColumn(name = "alumno_id", nullable = false) // Columna en la tabla 'pagos' que referencia al ID del alumno
    @EqualsAndHashCode.Exclude // Excluir de equals/hashCode para evitar StackOverflowError
    private Usuario alumno; // El alumno que realiza el pago

    @ManyToOne
    @JoinColumn(name = "clase_id") // Opcional: El pago puede estar asociado a una clase específica
    @EqualsAndHashCode.Exclude // Excluir de equals/hashCode
    private Clase clase;
}
