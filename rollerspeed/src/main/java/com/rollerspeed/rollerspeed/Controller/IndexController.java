package com.rollerspeed.rollerspeed.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequestMapping("/")
public class IndexController {
    @GetMapping("index")
    public String index() {
        return "index";
    }
    @GetMapping("servicios") 
    public String Servicios() {
        return "servicios"; 
    }

   
    @GetMapping("eventos") 
    public String Eventos() {
        return "eventos"; 
    }

    
    @GetMapping("mision") 
    public String Mision() {
        return "mision";
    }
   
    @GetMapping("vision")
    public String Vision() {
        return "vision";
    }
   
    @GetMapping("valores") 
    public String Valores() {
        return "valores";
    }

    
}
