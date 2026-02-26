package appMarketing.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import appMarketing.entity.Usuario;
import appMarketing.repository.UsuarioRepository;

@Service
public class CustomUserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario obtenerUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username).orElse(null);
    }

    public boolean validarCredenciales(String username, String password) {
        Usuario usuario = obtenerUsuarioPorUsername(username);
        if (usuario == null || !usuario.isActivo()) {
            return false;
        }
        return usuario.getPassword().equals(password);
    }
}

