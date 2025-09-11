package com.rollerspeed.rollerspeed.Repository;

import com.rollerspeed.rollerspeed.model.RolUsuario;
import com.rollerspeed.rollerspeed.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface userRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByRol(RolUsuario rol);
}