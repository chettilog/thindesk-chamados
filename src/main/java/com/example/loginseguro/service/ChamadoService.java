package com.example.loginseguro.service;

import com.example.loginseguro.dto.ChamadoRequest;
import com.example.loginseguro.dto.ResolverRequest;
import com.example.loginseguro.model.Chamado;
import com.example.loginseguro.model.Prioridade;
import com.example.loginseguro.model.StatusChamado;
import com.example.loginseguro.model.User;
import com.example.loginseguro.repository.ChamadoRepository;
import com.example.loginseguro.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Regras de negócio dos chamados técnicos.
 * Usado tanto pela API REST quanto pelas telas web.
 *
 * Permissões:
 * - USUARIO: abre chamados e vê só os próprios
 * - GESTOR: vê todos os chamados (acompanhamento)
 * - TECNICO e ADMIN: veem todos, atendem e resolvem
 */
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
        chamado.setPrioridade(req.prioridade() != null ? req.prioridade() : Prioridade.MEDIA);
        chamado.setSolicitanteId(usuario.getId());
        chamado.setDataAbertura(LocalDateTime.now());
        chamado.setStatus(StatusChamado.ABERTO);

        return chamadoRepository.save(chamado);
    }

    // Usuário comum só lista os próprios chamados; gestor/técnico/admin lista de qualquer um
    public List<Chamado> listarPorUsuario(Authentication auth, String userId) {
        User logado = usuarioLogado(auth);
        if (!podeVerTodos(auth) && !logado.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode ver os seus próprios chamados");
        }
        return chamadoRepository.findBySolicitanteIdOrderByDataAberturaDesc(userId);
    }

    // Lista usada nas telas e no dashboard: todos os chamados ou só os do usuário logado
    public List<Chamado> listarVisiveis(Authentication auth) {
        if (podeVerTodos(auth)) {
            return chamadoRepository.findAllByOrderByDataAberturaDesc();
        }
        User logado = usuarioLogado(auth);
        return chamadoRepository.findBySolicitanteIdOrderByDataAberturaDesc(logado.getId());
    }

    public Chamado consultar(Authentication auth, String id) {
        Chamado chamado = buscar(id);
        User logado = usuarioLogado(auth);
        if (!podeVerTodos(auth) && !logado.getId().equals(chamado.getSolicitanteId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode ver os seus próprios chamados");
        }
        return chamado;
    }

    // Técnico assume o chamado: ABERTO -> EM_ANDAMENTO
    public Chamado atender(Authentication auth, String id) {
        Chamado chamado = buscar(id);

        if (chamado.getStatus() != StatusChamado.ABERTO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Só é possível atender chamados que estão abertos");
        }

        User tecnico = usuarioLogado(auth);
        chamado.setStatus(StatusChamado.EM_ANDAMENTO);
        chamado.setTecnicoId(tecnico.getId());

        return chamadoRepository.save(chamado);
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

    // Mapa id -> nome, para mostrar o nome do solicitante/técnico nas telas
    public Map<String, String> nomesDosUsuarios() {
        Map<String, String> nomes = new HashMap<>();
        for (User u : userRepository.findAll()) {
            nomes.put(u.getId(), u.getNome());
        }
        return nomes;
    }

    public boolean podeVerTodos(Authentication auth) {
        return temAlgumPerfil(auth, "ROLE_GESTOR", "ROLE_TECNICO", "ROLE_ADMIN");
    }

    public boolean podeAtender(Authentication auth) {
        return temAlgumPerfil(auth, "ROLE_TECNICO", "ROLE_ADMIN");
    }

    private Chamado buscar(String id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chamado não encontrado"));
    }

    private User usuarioLogado(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado"));
    }

    private boolean temAlgumPerfil(Authentication auth, String... perfis) {
        return auth.getAuthorities().stream().anyMatch(a -> List.of(perfis).contains(a.getAuthority()));
    }
}
