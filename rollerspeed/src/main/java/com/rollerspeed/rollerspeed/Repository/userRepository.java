package com.rollerspeed.rollerspeed.Repository;

import com.rollerspeed.rollerspeed.model.Usuario;
import com.rollerspeed.rollerspeed.model.RolUsuario; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface userRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByRol(RolUsuario rol); 
}