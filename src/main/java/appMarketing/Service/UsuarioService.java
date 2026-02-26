package appMarketing.Service;

import java.util.Optional;

import appMarketing.entity.Usuario;

public interface UsuarioService {
    Optional<Usuario> findById(Long id);
    Usuario guardar(Usuario usuario);
    Optional<Usuario> findByUsername(String username);
    
    
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
  
    boolean validarCredenciales(String username, String password);
}