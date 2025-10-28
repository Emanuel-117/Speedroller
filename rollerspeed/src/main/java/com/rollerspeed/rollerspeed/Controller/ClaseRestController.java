package com.rollerspeed.rollerspeed.Controller;

import com.rollerspeed.rollerspeed.Service.ClaseService;
import com.rollerspeed.rollerspeed.model.Clase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/clases")
@Tag(name = "Clases", description = "API para la gestión de clases")
public class ClaseRestController {

    @Autowired
    private ClaseService claseService;

    @Operation(
        summary = "Obtener todas las clases",
        description = "Obtiene una lista de todas las clases disponibles"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de clases obtenida correctamente",
                    content = @Content(schema = @Schema(implementation = Clase.class))),
        @ApiResponse(responseCode = "403", description = "No autorizado para ver las clases")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<List<Clase>> getAllClases() {
        return ResponseEntity.ok(claseService.getAllClases());
    }

    @Operation(
        summary = "Obtener una clase por ID",
        description = "Obtiene los detalles de una clase específica por su ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Clase encontrada"),
        @ApiResponse(responseCode = "404", description = "Clase no encontrada")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<Clase> getClaseById(
            @Parameter(description = "ID de la clase a buscar") @PathVariable Long id) {
        Optional<Clase> clase = claseService.getClaseById(id);
        return clase.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Crear una nueva clase",
        description = "Crea una nueva clase con los datos proporcionados"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Clase creada correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de clase inválidos")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Clase> createClase(
            @Parameter(description = "Datos de la clase a crear") 
            @Valid @RequestBody Clase clase) {
        Clase nuevaClase = claseService.guardarClase(clase);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaClase);
    }

    @Operation(
        summary = "Actualizar una clase existente",
        description = "Actualiza los datos de una clase existente"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Clase actualizada correctamente"),
        @ApiResponse(responseCode = "404", description = "Clase no encontrada"),
        @ApiResponse(responseCode = "400", description = "Datos de clase inválidos")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Clase> updateClase(
            @Parameter(description = "ID de la clase a actualizar") @PathVariable Long id,
            @Parameter(description = "Nuevos datos de la clase") 
            @Valid @RequestBody Clase clase) {
        if (!claseService.getClaseById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        clase.setId(id);
        return ResponseEntity.ok(claseService.guardarClase(clase));
    }

    @Operation(
        summary = "Eliminar una clase",
        description = "Elimina una clase existente por su ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Clase eliminada correctamente"),
        @ApiResponse(responseCode = "404", description = "Clase no encontrada")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClase(
            @Parameter(description = "ID de la clase a eliminar") @PathVariable Long id) {
        if (!claseService.getClaseById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        claseService.deleteClase(id);
        return ResponseEntity.noContent().build();
    }
}