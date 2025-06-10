package com.rollerspeed.rollerspeed.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol;

    @Column(nullable = true)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 10)
    private GeneroUsuario genero;

    @Column(nullable = true, length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 50)
    private MedioPago medioPago;

    @ManyToMany
    @JoinTable(
        name = "usuario_clase",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "clase_id")
    )
    @EqualsAndHashCode.Exclude
    private Set<Clase> clasesInscritas = new HashSet<>();

    public void addClase(Clase clase) {
        if (this.clasesInscritas.add(clase)) {
            clase.getAlumnos().add(this);
        }
    }

    public void removeClase(Clase clase) {
        if (this.clasesInscritas.remove(clase)) {
            clase.getAlumnos().remove(this);
        }
    }
}