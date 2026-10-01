package trabalhoPOO.controller;

import trabalhoPOO.model.LocalStarWars;
import trabalhoPOO.model.Partida;
import trabalhoPOO.model.Usuario;
import trabalhoPOO.repository.LocalRepository;
import trabalhoPOO.repository.PartidaRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class JogoController {

    private static final int MAX_RODADAS = 10;
    private static final int PONTOS_POR_ACERTO = 10;
    private static final String LOCAIS_DA_PARTIDA = "locaisDaPartida";

    private final LocalRepository localRepository;
    private final PartidaRepository partidaRepository;

    public JogoController(LocalRepository localRepository, PartidaRepository partidaRepository) {
        this.localRepository = localRepository;
        this.partidaRepository = partidaRepository;
    }

    @GetMapping("/jogo")
    public String jogo(
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        // Não está logado
        if (usuario == null) {
            return "redirect:/login";
        }

        Integer rodada = (Integer) session.getAttribute("rodada");
        Integer pontuacao = (Integer) session.getAttribute("pontuacao");

        // Inicializa estado e ordem aleatória apenas uma vez por partida.
        if (rodada == null) {
            rodada = 1;
            pontuacao = 0;

            session.setAttribute("rodada", rodada);
            session.setAttribute("pontuacao", pontuacao);
            session.setAttribute("dataInicio", LocalDateTime.now());
        }
        if (pontuacao == null) {
            pontuacao = 0;
            session.setAttribute("pontuacao", pontuacao);
        }

        @SuppressWarnings("unchecked")
        List<String> locaisDaPartida = (List<String>) session.getAttribute(LOCAIS_DA_PARTIDA);
        if (locaisDaPartida == null) {
            locaisDaPartida = criarOrdemDaPartida();
            session.setAttribute(LOCAIS_DA_PARTIDA, locaisDaPartida);
            session.setAttribute("totalRodadas", Math.min(MAX_RODADAS, locaisDaPartida.size()));
        }
        Integer totalRodadas = (Integer) session.getAttribute("totalRodadas");

        int indiceLocal = rodada - 1;
        if (indiceLocal >= 0 && indiceLocal < locaisDaPartida.size()) {
            localRepository.findById(locaisDaPartida.get(indiceLocal)).ifPresent(local -> {
                session.setAttribute("respostaCorreta", local.getNome());
                model.addAttribute("local", local);
            });
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("rodada", rodada);
        model.addAttribute("totalRodadas", totalRodadas);
        model.addAttribute("pontuacao", pontuacao);
        model.addAttribute("pagina", "jogo");

        return "index";
    }

    @PostMapping("/jogo/responder")
    public String responder(
            @RequestParam(required = false, defaultValue = "") String resposta,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        Integer rodada = (Integer) session.getAttribute("rodada");
        Integer pontuacao = (Integer) session.getAttribute("pontuacao");
        Integer totalRodadas = (Integer) session.getAttribute("totalRodadas");

        if (rodada == null) rodada = 1;
        if (pontuacao == null) pontuacao = 0;
        if (totalRodadas == null) totalRodadas = MAX_RODADAS;

        String respostaCorreta = (String) session.getAttribute("respostaCorreta");

        boolean acertou = respostaCorreta != null &&
                resposta != null &&
                resposta.trim().equalsIgnoreCase(respostaCorreta.trim());

        if (acertou) {
            pontuacao += PONTOS_POR_ACERTO;
        }

        rodada++;

        session.setAttribute("rodada", rodada);
        session.setAttribute("pontuacao", pontuacao);

        // Fim da partida
        if (rodada > totalRodadas) {
            salvarPartida(session, usuario, pontuacao, totalRodadas);
            return "redirect:/jogo/resultado";
        }

        model.addAttribute("acertou", acertou);
        model.addAttribute("pontuacao", pontuacao);
        model.addAttribute("respostaCorreta", respostaCorreta);
        model.addAttribute("pagina", "resultadoRodada");

        return "index";
    }

    @GetMapping("/jogo/resultado")
    public String resultado(
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        Integer pontuacao = (Integer) session.getAttribute("pontuacao");

        model.addAttribute("usuario", usuario);
        model.addAttribute("pontuacao", pontuacao != null ? pontuacao : 0);
        model.addAttribute("pagina", "resultado");

        return "index";
    }

    @GetMapping("/jogo/novo")
    public String novoJogo(HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        session.setAttribute("rodada", 1);
        session.setAttribute("pontuacao", 0);
        session.setAttribute("dataInicio", LocalDateTime.now());
        session.removeAttribute(LOCAIS_DA_PARTIDA);
        session.removeAttribute("totalRodadas");
        session.removeAttribute("respostaCorreta");

        return "redirect:/jogo";
    }

    private List<String> criarOrdemDaPartida() {
        List<String> ids = localRepository.findAll().stream()
            .filter(local -> !"Não identificado".equals(local.getTipo()))
                .map(LocalStarWars::getId)
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(ids);
        return ids;
    }

    private void salvarPartida(HttpSession session, Usuario usuario, int pontuacao, int totalRodadas) {
        LocalDateTime dataInicio = (LocalDateTime) session.getAttribute("dataInicio");
        if (dataInicio == null) {
            dataInicio = LocalDateTime.now();
        }

        Partida partida = new Partida();
        partida.setUsuario(usuario);
        partida.setPontuacao(pontuacao);
        partida.setRodadas(totalRodadas);
        partida.setDataInicio(dataInicio);
        partida.setDataFim(LocalDateTime.now());

        partidaRepository.save(partida);
    }
} 