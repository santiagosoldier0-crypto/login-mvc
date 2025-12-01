package com.exposicion.loginmvc.controller;

import com.exposicion.loginmvc.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador que maneja las peticiones HTTP para el login. Delega la lógica de
 * autenticación al AuthService.
 */
/**Hola mundo**/
@Controller
public class LoginController {

    // Inyectamos el Servicio (Lógica de Negocio).
    @Autowired
    private AuthService authService;

    // --- Petición GET: Muestra el formulario ---
    @GetMapping({"/", "/login"})
    public String showLoginForm(Model model) {
        return "login"; // Vista login.html
    }

    // --- Petición POST: Procesa el formulario ---
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        // Usamos el Servicio para autenticar
        if (authService.authenticate(username, password)) {
            // Éxito: Redirecciona a la página de bienvenida
            return "redirect:/home";
        } else {
            // Fallo: Vuelve al login con mensaje de error
            model.addAttribute("error", "Credenciales incorrectas. Verifique usuario y contraseña.");
            return "login";
        }
    }

    // --- Petición GET: Página de Bienvenida (Home) ---
    @GetMapping("/home")
    public String showHomePage() {
        return "home";
    }
}
