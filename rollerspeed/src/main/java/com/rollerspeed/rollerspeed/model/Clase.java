package com.rollerspeed.rollerspeed.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Future; // Importación para @Future
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "clases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la clase es obligatorio.")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres.")
    @Column(length = 500)
    private String descripcion;

    @NotNull(message = "La fecha y hora de inicio es obligatoria.")
    @FutureOrPresent(message = "La fecha y hora de inicio no puede ser en el pasado.")
    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha y hora de fin es obligatoria.")
    @Future(message = "La fecha y hora de fin debe ser en el futuro.") // CAMBIO AQUÍ: Ahora es estrictamente futuro
    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @NotNull(message = "Se debe asignar un profesor a la clase.")
    @ManyToOne
    @JoinColumn(name = "profesor_id", nullable = false)
    @EqualsAndHashCode.Exclude // Excluido para evitar recursión y problemas de rendimiento en hashCode/equals
    private Usuario profesor;

    @ManyToMany(mappedBy = "clasesInscritas")
    @EqualsAndHashCode.Exclude // Excluido para evitar recursión y problemas de rendimiento en hashCode/equals
    private Set<Usuario> alumnos = new HashSet<>();

    // Métodos helper para manejar la bidireccionalidad de la relación con alumnos
    public void addAlumno(Usuario alumno) {
        if (this.alumnos == null) {
            this.alumnos = new HashSet<>();
        }
        this.alumnos.add(alumno);
        if (alumno.getClasesInscritas() == null) {
            alumno.setClasesInscritas(new HashSet<>());
        }
        alumno.getClasesInscritas().add(this);
    }

    public void removeAlumno(Usuario alumno) {
        if (this.alumnos != null) {
            this.alumnos.remove(alumno);
        }
        if (alumno.getClasesInscritas() != null) {
            alumno.getClasesInscritas().remove(this);
        }
    }
}