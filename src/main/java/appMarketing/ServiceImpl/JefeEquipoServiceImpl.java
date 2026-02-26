package appMarketing.ServiceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import appMarketing.Service.JefeEquipoService;
import appMarketing.entity.JefeEquipo;
import appMarketing.repository.JefeEquipoRepository;

@Service
public class JefeEquipoServiceImpl implements JefeEquipoService {

    @Autowired
    private JefeEquipoRepository jefeEquipoRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public List<JefeEquipo> listarTodos() {
        return jefeEquipoRepository.findByActivoTrue();
    }

    @Override
    public Optional<JefeEquipo> findById(Long id) {
        return jefeEquipoRepository.findById(id);
    }

    @Override
    public JefeEquipo guardar(JefeEquipo jefeEquipo) {
        return jefeEquipoRepository.save(jefeEquipo);
    }

    @Override
    public void eliminar(Long id) {
        Optional<JefeEquipo> jefe = jefeEquipoRepository.findById(id);
        jefe.ifPresent(j -> {
            j.setActivo(false);
            jefeEquipoRepository.save(j);
        });
    }

    @Override
    public void enviarCorreo(Long jefeEquipoId, String asunto, String mensaje) {
        Optional<JefeEquipo> jefe = jefeEquipoRepository.findById(jefeEquipoId);
        if (jefe.isPresent()) {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(jefe.get().getUsuario().getEmail());
            email.setSubject(asunto);
            email.setText(mensaje);
            mailSender.send(email);
        }
    }

    @Override
    public void enviarAviso(Long jefeEquipoId, String aviso) {
        enviarCorreo(jefeEquipoId, "Aviso Importante - Agencia Marketing", aviso);
    }
}
