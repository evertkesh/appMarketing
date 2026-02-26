package appMarketing.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgument(HttpServletRequest request, IllegalArgumentException ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", "Argumento inválido: " + ex.getMessage());
        mav.addObject("timestamp", LocalDateTime.now());
        mav.addObject("status", HttpStatus.BAD_REQUEST.value());
        mav.setViewName("error/error-page");
        return mav;
    }

    @ExceptionHandler(NullPointerException.class)
    public ModelAndView handleNullPointer(HttpServletRequest request, NullPointerException ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", "Recurso no encontrado o error de datos nulos");
        mav.addObject("timestamp", LocalDateTime.now());
        mav.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        mav.setViewName("error/error-page");
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(HttpServletRequest request, Exception ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", "Error interno del servidor: " + ex.getMessage());
        mav.addObject("timestamp", LocalDateTime.now());
        mav.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        mav.setViewName("error/error-page");
        return mav;
    }
}
