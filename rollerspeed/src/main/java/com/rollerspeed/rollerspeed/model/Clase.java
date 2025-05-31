package com.rollerspeed.rollerspeed.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode; // Importar esto
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

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @ManyToOne
    @JoinColumn(name = "profesor_id", nullable = false)
    @EqualsAndHashCode.Exclude 
    private Usuario profesor;

    @ManyToMany(mappedBy = "clasesInscritas")
    @EqualsAndHashCode.Exclude 
    private Set<Usuario> alumnos = new HashSet<>();

    public void addAlumno(Usuario alumno) {
        this.alumnos.add(alumno);
        alumno.getClasesInscritas().add(this);
    }

    public void removeAlumno(Usuario alumno) {
        this.alumnos.remove(alumno);
        alumno.getClasesInscritas().remove(this);
    }
}