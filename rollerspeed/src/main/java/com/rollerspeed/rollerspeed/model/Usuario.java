package com.rollerspeed.rollerspeed.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
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

   
    @ManyToMany
    @JoinTable(
        name = "usuario_clase", 
        joinColumns = @JoinColumn(name = "usuario_id"), 
        inverseJoinColumns = @JoinColumn(name = "clase_id")
    )
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
