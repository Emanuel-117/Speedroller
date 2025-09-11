package com.rollerspeed.rollerspeed.Controller;

import com.rollerspeed.rollerspeed.model.Pago;
import com.rollerspeed.rollerspeed.model.RolUsuario;
import com.rollerspeed.rollerspeed.Service.PagoService;
import com.rollerspeed.rollerspeed.Service.UsuarioService;
import com.rollerspeed.rollerspeed.Service.ClaseService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pagos")
@PreAuthorize("hasRole('ADMIN')")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ClaseService claseService;

    @GetMapping("/nuevo")
    public String mostrarFormularioPago(Model model) {
        model.addAttribute("pago", new Pago());
        model.addAttribute("alumnos", usuarioService.getUsuariosByRol(RolUsuario.ALUMNO));
        model.addAttribute("clases", claseService.getAllClases());
        return "pagos/pagoForm";
    }

    @PostMapping("/guardar")
    public String guardarPago(@ModelAttribute Pago pago, RedirectAttributes redirectAttributes) {
        try {
            pagoService.guardarPago(pago);
            redirectAttributes.addFlashAttribute("mensaje", "¡Pago registrado con éxito!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar el pago: " + e.getMessage());
        }
        return "redirect:/pagos/listar";
    }

    @GetMapping("/listar")
    public String listarPagos(Model model) {
        model.addAttribute("pagos", pagoService.getAllPagos());
        return "pagos/listarPagos";
    }

    @GetMapping("/detalles/{id}")
    public String detallesPago(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Pago> pagoOptional = pagoService.getPagoById(id);
        if (pagoOptional.isPresent()) {
            model.addAttribute("pago", pagoOptional.get());
            return "pagos/detallesPago";
        } else {
            redirectAttributes.addFlashAttribute("error", "Pago no encontrado.");
            return "redirect:/pagos/listar";
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarPago(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pagoService.eliminarPago(id);
            redirectAttributes.addFlashAttribute("mensaje", "Pago eliminado con éxito!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el pago: " + e.getMessage());
        }
        return "redirect:/pagos/listar";
    }
}