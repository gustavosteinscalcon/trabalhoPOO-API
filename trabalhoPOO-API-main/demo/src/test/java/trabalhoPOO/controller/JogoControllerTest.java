package trabalhoPOO.controller;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import trabalhoPOO.model.LocalStarWars;
import trabalhoPOO.model.Usuario;
import trabalhoPOO.repository.LocalRepository;
import trabalhoPOO.repository.PartidaRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JogoControllerTest {

    @Test
    void usaPlanetasDistintosEConservaPerguntaAoAtualizar() {
        LocalRepository localRepository = mock(LocalRepository.class);
        PartidaRepository partidaRepository = mock(PartidaRepository.class);
        List<LocalStarWars> planetas = IntStream.range(0, 12)
                .mapToObj(indice -> new LocalStarWars(
                        "planet-" + indice,
                        "Planeta " + indice,
                        "Descrição " + indice,
                        null,
                        "Planeta"))
                .toList();
        Map<String, LocalStarWars> planetasPorId = new HashMap<>();
        planetas.forEach(planeta -> planetasPorId.put(planeta.getId(), planeta));

        when(localRepository.findAll()).thenReturn(planetas);
        when(localRepository.findById(anyString())).thenAnswer(invocation ->
                Optional.ofNullable(planetasPorId.get(invocation.getArgument(0))));

        JogoController controller = new JogoController(localRepository, partidaRepository);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", new Usuario());
        Set<String> planetasUsados = new HashSet<>();

        for (int rodada = 0; rodada < 10; rodada++) {
            ExtendedModelMap primeiroModel = new ExtendedModelMap();
            controller.jogo(session, primeiroModel);
            LocalStarWars primeiroPlaneta = (LocalStarWars) primeiroModel.get("local");
            assertNotNull(primeiroPlaneta);

            ExtendedModelMap refreshModel = new ExtendedModelMap();
            controller.jogo(session, refreshModel);
            LocalStarWars planetaAposRefresh = (LocalStarWars) refreshModel.get("local");
            assertEquals(primeiroPlaneta.getId(), planetaAposRefresh.getId());
            assertTrue(planetasUsados.add(primeiroPlaneta.getId()));

            controller.responder("", session, new ExtendedModelMap());
        }

        assertEquals(10, planetasUsados.size());
    }
}