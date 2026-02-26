package appMarketing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import appMarketing.entity.Usuario;
import appMarketing.entity.Usuario.Rol;
import appMarketing.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador de autenticación
 * Maneja login, registro y obtención de usuario actual
 * Valida usuarios contra BD sin encriptación (para desarrollo)
 */
@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Página de login
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           @RequestParam(value = "registroError", required = false) String registroError,
                           @RequestParam(value = "registroExito", required = false) String registroExito,
                           Model model) {
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Has cerrado sesión exitosamente");
        }
        if (registroError != null) {
            model.addAttribute("registroError", registroError);
        }
        if (registroExito != null) {
            model.addAttribute("registroExito", "Registro exitoso. Por favor inicia sesión.");
        }
        return "login";
    }

    /**
     * Procesar login - Valida credenciales contra la BD
     */
    @PostMapping("/login")
    public String processLogin(@RequestParam(name = "username", required = false) String username,
                               @RequestParam(name = "password", required = false) String password,
                               HttpSession session) {
        try {
            System.out.println("\n\n========== INTENTO LOGIN ==========");
            System.out.println("Username recibido: " + username);
            System.out.println("Password recibido: " + password);
            
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                System.out.println("ERROR: Username o password vacíos");
                return "redirect:/login?error=Usuario o contraseña requeridos";
            }
            
            // Buscar en BD
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
            System.out.println("Usuario existe: " + usuarioOpt.isPresent());
            
            if (!usuarioOpt.isPresent()) {
                System.out.println("ERROR: Usuario no encontrado");
                return "redirect:/login?error=true";
            }
            
            Usuario usuario = usuarioOpt.get();
            System.out.println("Usuario encontrado: " + usuario.getUsername());
            System.out.println("Password en BD: " + usuario.getPassword());
            System.out.println("¿Activo?: " + usuario.isActivo());
            
            if (!usuario.isActivo()) {
                System.out.println("ERROR: Usuario inactivo");
                return "redirect:/login?error=true";
            }
            
            // Comparar contraseñas
            boolean match = usuario.getPassword().equals(password);
            System.out.println("¿Password coincide?: " + match);
            
            if (!match) {
                System.out.println("ERROR: Password incorrecta");
                return "redirect:/login?error=true";
            }
            
            // Login exitoso
            session.setAttribute("usuario", usuario);
            session.setAttribute("username", usuario.getUsername());
            session.setAttribute("rol", usuario.getRol());
            session.setAttribute("userId", usuario.getId());
            
            System.out.println("✓ LOGIN EXITOSO");
            System.out.println("========== FIN LOGIN ==========\n\n");
            
            return "redirect:/admin/dashboard";
            
        } catch (Exception e) {
            System.err.println("EXCEPTION en login: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login?error=true";
        }
    }

    /**
     * Registrar nuevo usuario en la BD
     */
    @PostMapping("/register")
    public String registroUsuario(@RequestParam String username,
                                 @RequestParam String nombreCompleto,
                                 @RequestParam String email,
                                 @RequestParam String password,
                                 @RequestParam String confirmPassword,
                                 Model model) {
        
        System.out.println("=== INICIANDO REGISTRO DE USUARIO ===");
        System.out.println("Username: " + username);
        System.out.println("Email: " + email);
        
        try {
            // Validaciones básicas
            if (!password.equals(confirmPassword)) {
                System.out.println("ERROR: Las contraseñas no coinciden");
                return "redirect:/login?registroError=Las contraseñas no coinciden";
            }
            
            if (username.length() < 3) {
                System.out.println("ERROR: Username muy corto");
                return "redirect:/login?registroError=El usuario debe tener al menos 3 caracteres";
            }
            
            if (password.length() < 6) {
                System.out.println("ERROR: Contraseña muy corta");
                return "redirect:/login?registroError=La contraseña debe tener al menos 6 caracteres";
            }
            
            if (!email.contains("@")) {
                System.out.println("ERROR: Email inválido");
                return "redirect:/login?registroError=El email debe ser válido";
            }
            
            // Verificar si el usuario ya existe
            if (usuarioRepository.findByUsername(username).isPresent()) {
                System.out.println("ERROR: Username ya existe");
                return "redirect:/login?registroError=El usuario ya existe";
            }
            
            // Verificar si el email ya existe
            if (usuarioRepository.findByEmail(email).isPresent()) {
                System.out.println("ERROR: Email ya existe");
                return "redirect:/login?registroError=El email ya está registrado";
            }
            
            // Crear nuevo usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername(username);
            nuevoUsuario.setNombreCompleto(nombreCompleto);
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setPassword(password); // Sin encriptación (desarrollo)
            nuevoUsuario.setRol(Rol.ADMINISTRADOR); // Rol por defecto
            nuevoUsuario.setActivo(true);
            
            // Guardar en la base de datos
            usuarioRepository.save(nuevoUsuario);
            
            System.out.println("✓ Usuario registrado exitosamente: " + username);
            System.out.println("=== REGISTRO COMPLETADO ===");
            
            return "redirect:/login?registroExito=true";
            
        } catch (Exception e) {
            System.err.println("ERROR al registrar usuario: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login?registroError=Error al crear la cuenta";
        }
    }

    /**
     * Cerrar sesión
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }

    /**
     * Endpoint REST para obtener el usuario autenticado actualmente
     * Obtiene el usuario de la sesión HTTP
     */
    @GetMapping("/api/usuario/actual")
    public ResponseEntity<Map<String, Object>> obtenerUsuarioActual(HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            
            if (usuario == null) {
                return ResponseEntity.status(401).body(createErrorResponse("No autenticado"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", usuario.getId());
            response.put("username", usuario.getUsername());
            response.put("nombreCompleto", usuario.getNombreCompleto());
            response.put("email", usuario.getEmail());
            response.put("rol", usuario.getRol().name());
            response.put("activo", usuario.isActivo());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error al obtener usuario: " + e.getMessage());
            return ResponseEntity.status(500).body(createErrorResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Helper para crear respuesta de error
     */
    private Map<String, Object> createErrorResponse(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", mensaje);
        return response;
    }
}

