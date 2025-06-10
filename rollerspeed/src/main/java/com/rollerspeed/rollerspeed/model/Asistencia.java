package com.rollerspeed.rollerspeed.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "asistencias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Asistencia pertenece a un alumno
    @JoinColumn(name = "alumno_id", nullable = false)
    private Usuario alumno; // El alumno que asiste

    @ManyToOne(fetch = FetchType.LAZY) // Asistencia pertenece a una clase
    @JoinColumn(name = "clase_id", nullable = false)
    private Clase clase; // La clase a la que asiste

    @Column(nullable = false)
    private LocalDateTime fechaHoraRegistro; // Cuándo se registró la asistencia

    @Column(nullable = false)
    private boolean presente; // true si está presente, false si falta

    // Constructor sin id (para cuando se crea una nueva asistencia)
    public Asistencia(Usuario alumno, Clase clase, LocalDateTime fechaHoraRegistro, boolean presente) {
        this.alumno = alumno;
        this.clase = clase;
        this.fechaHoraRegistro = fechaHoraRegistro;
        this.presente = presente;
    }
}