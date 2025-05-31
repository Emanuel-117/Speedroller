package com.rollerspeed.rollerspeed.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; 

import com.rollerspeed.rollerspeed.Service.UsuarioService;

import com.rollerspeed.rollerspeed.model.Usuario;
import com.rollerspeed.rollerspeed.model.RolUsuario; 

@Controller
@RequestMapping("/users")
public class userController {

    @Autowired
    private UsuarioService userService;

    
    @GetMapping(value = "/listar")
    public String getAllUsers(Model model) {
        List<Usuario> ListaUsuarios = userService.getUsuarios();
        model.addAttribute("usuarios", ListaUsuarios);
        
        return "users/usuarios";
    }

    
    @GetMapping(value = "/nuevo")
    public String mostrarFormularioNuevoUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());        
        model.addAttribute("roles", RolUsuario.values()); 
        return "users/usuarioForm";
    }

   
    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute("usuario") Usuario usuario, RedirectAttributes redirectAttributes) { 
         if (usuario.getRol() == null) {
            usuario.setRol(RolUsuario.ALUMNO);
        }
         userService.guardaUsuario(usuario); 
          redirectAttributes.addFlashAttribute("mensaje", "Usuario guardado exitosamente!");
        return "redirect:/users/listar";
    }
    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUsuario(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado exitosamente.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage()); 
              } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el usuario: " + e.getMessage());
            
        }
        return "redirect:/users/listar"; 
    }

    
}