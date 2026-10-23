package com.example.loginseguro.service;

import com.example.loginseguro.dto.ChamadoRequest;
import com.example.loginseguro.dto.ResolverRequest;
import com.example.loginseguro.model.Chamado;
import com.example.loginseguro.model.StatusChamado;
import com.example.loginseguro.model.User;
import com.example.loginseguro.repository.ChamadoRepository;
import com.example.loginseguro.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final UserRepository userRepository;

    public ChamadoService(ChamadoRepository chamadoRepository, UserRepository userRepository) {
        this.chamadoRepository = chamadoRepository;
        this.userRepository = userRepository;
    }

    // Abre um chamado: o solicitante vem do usuário logado, nunca do corpo da requisição
    public Chamado abrir(Authentication auth, ChamadoRequest req) {
        User usuario = usuarioLogado(auth);

        Chamado chamado = new Chamado();
        chamado.setTitulo(req.titulo().trim());
        chamado.setDescricao(req.descricao().trim());
        chamado.setSolicitanteId(usuario.getId());
        chamado.setDataAbertura(LocalDateTime.now());
        chamado.setStatus(StatusChamado.ABERTO);

        return chamadoRepository.save(chamado);
    }

    // Usuário comum só lista os próprios chamados; técnico/admin lista de qualquer um
    public List<Chamado> listarPorUsuario(Authentication auth, String userId) {
        User logado = usuarioLogado(auth);
        if (!ehTecnico(auth) && !logado.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode ver os seus próprios chamados");
        }
        return chamadoRepository.findBySolicitanteIdOrderByDataAberturaDesc(userId);
    }

    public Chamado consultar(Authentication auth, String id) {
        Chamado chamado = buscar(id);
        User logado = usuarioLogado(auth);
        if (!ehTecnico(auth) && !logado.getId().equals(chamado.getSolicitanteId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode ver os seus próprios chamados");
        }
        return chamado;
    }

    // Resolve o chamado; chamado já resolvido não pode mais ser alterado
    public Chamado resolver(Authentication auth, String id, ResolverRequest req) {
        Chamado chamado = buscar(id);

        if (chamado.getStatus() == StatusChamado.RESOLVIDO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este chamado já foi resolvido e não pode ser alterado");
        }

        User tecnico = usuarioLogado(auth);
        chamado.setSolucao(req.solucao().trim());
        chamado.setStatus(StatusChamado.RESOLVIDO);
        chamado.setDataResolucao(LocalDateTime.now());
        chamado.setTecnicoId(tecnico.getId());

        return chamadoRepository.save(chamado);
    }

    private Chamado buscar(String id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chamado não encontrado"));
    }

    private User usuarioLogado(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado"));
    }

    private boolean ehTecnico(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_TECNICO") || a.getAuthority().equals("ROLE_ADMIN"));
    }
}