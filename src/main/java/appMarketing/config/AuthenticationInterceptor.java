package appMarketing.config;

import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        
        // Rutas que no requieren autenticación
        if (requestURI.contains("/login") || requestURI.contains("/logout") || requestURI.contains("/error")) {
            return true;
        }
        
        HttpSession session = request.getSession(false);
        
        // Si no hay sesión o no hay usuario en la sesión, redirigir al login
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        
        return true;
    }
}
