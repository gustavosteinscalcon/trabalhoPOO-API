package trabalhoPOO.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import trabalhoPOO.repository.LocalRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;
import java.math.BigInteger;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LocalDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(LocalDataInitializer.class);
    private static final String PLANETS_API = "https://swapi.dev/api/planets/";
        private static final Map<String, String> CLIMATE_TRANSLATIONS = Map.ofEntries(
            Map.entry("arid", "árido"),
            Map.entry("temperate", "temperado"),
            Map.entry("tropical", "tropical"),
            Map.entry("frozen", "gelado"),
            Map.entry("hot", "quente"),
            Map.entry("murky", "enevoado"),
            Map.entry("humid", "úmido"),
            Map.entry("frigid", "muito frio"),
            Map.entry("polluted", "poluído"),
            Map.entry("windy", "ventoso"),
            Map.entry("moist", "úmido"),
            Map.entry("artificial temperate", "artificialmente temperado"),
            Map.entry("superheated", "superaquecido"));
        private static final Map<String, String> TERRAIN_TRANSLATIONS = Map.ofEntries(
            Map.entry("desert", "desertos"),
            Map.entry("rock", "formações rochosas"),
            Map.entry("mountain", "montanhas"),
            Map.entry("mountains", "montanhas"),
            Map.entry("grasslands", "planícies"),
            Map.entry("jungle", "selvas"),
            Map.entry("jungles", "selvas"),
            Map.entry("rainforests", "florestas tropicais"),
            Map.entry("tundra", "tundra"),
            Map.entry("ice caves", "cavernas de gelo"),
            Map.entry("mountain ranges", "cadeias montanhosas"),
            Map.entry("swamp", "pântanos"),
            Map.entry("swamps", "pântanos"),
            Map.entry("bogs", "brejos"),
            Map.entry("gas giant", "gigante gasoso"),
            Map.entry("forests", "florestas"),
            Map.entry("lakes", "lagos"),
            Map.entry("cityscape", "paisagem urbana"),
            Map.entry("ocean", "oceanos"),
            Map.entry("oceans", "oceanos"),
            Map.entry("barren", "áreas áridas"),
            Map.entry("urban", "áreas urbanas"),
            Map.entry("scrublands", "vegetação rasteira"),
            Map.entry("savanna", "savanas"),
            Map.entry("canyons", "cânions"),
            Map.entry("sinkholes", "dolinas"),
            Map.entry("volcanoes", "vulcões"),
            Map.entry("lava rivers", "rios de lava"),
            Map.entry("caves", "cavernas"),
            Map.entry("rivers", "rios"),
            Map.entry("airless asteroid", "asteroide sem atmosfera"),
            Map.entry("glaciers", "geleiras"),
            Map.entry("ice canyons", "cânions de gelo"),
            Map.entry("fungus forests", "florestas de fungos"),
            Map.entry("fields", "campos"),
            Map.entry("rock arches", "arcos rochosos"),
            Map.entry("grass", "campos gramados"));

    private final LocalRepository localRepository;
    private final RestClient restClient = RestClient.create();

    public LocalDataInitializer(LocalRepository localRepository) {
        this.localRepository = localRepository;
    }

    @Override
    public void run(String... args) {
        try {
            List<LocalStarWars> planets = fetchPlanets();
            if (planets.size() < 10) {
                throw new IllegalStateException("A API retornou menos de 10 planetas.");
            }

                Set<String> planetIds = planets.stream()
                    .map(LocalStarWars::getId)
                    .collect(Collectors.toSet());
                List<LocalStarWars> outdatedPlanets = localRepository.findAll().stream()
                    .filter(local -> !planetIds.contains(local.getId()))
                    .toList();
            localRepository.saveAll(planets);
                localRepository.deleteAll(outdatedPlanets);
            logger.info("Catálogo sincronizado com {} planetas da SWAPI.", planets.size());
        } catch (RuntimeException exception) {
            logger.warn("Não foi possível sincronizar os planetas da SWAPI; serão mantidos os dados locais.", exception);
            if (localRepository.count() == 0) {
                localRepository.saveAll(fallbackPlanets());
            }
        }
    }

    private List<LocalStarWars> fetchPlanets() {
        List<LocalStarWars> planets = new ArrayList<>();
        String nextPage = PLANETS_API;

        while (nextPage != null) {
            JsonNode page = restClient.get().uri(nextPage).retrieve().body(JsonNode.class);
            if (page == null || !page.has("results")) {
                throw new IllegalStateException("Resposta inválida da API de planetas.");
            }

            for (JsonNode planet : page.path("results")) {
                String apiName = planet.path("name").asText().trim();
                String id = apiId(textOrDefault(planet, "url", ""));
                if (id.isBlank() || apiName.isBlank()) {
                    continue;
                }
                boolean unidentified = apiName.equalsIgnoreCase("unknown");
                String name = unidentified ? "Desconhecido (#" + id + ")" : apiName;

                String climate = textOrDefault(planet, "climate", "");
                String terrain = textOrDefault(planet, "terrain", "");
                String population = textOrDefault(planet, "population", "");
                String description = criarDescricao(climate, terrain, population);

                planets.add(new LocalStarWars(
                    id,
                    name,
                    description,
                    null,
                    unidentified ? "Não identificado" : "Planeta"));
            }

            JsonNode next = page.get("next");
            nextPage = next == null || next.isNull() ? null : next.asText();
        }

        return planets;
    }

    private List<LocalStarWars> fallbackPlanets() {
        return List.of(
                new LocalStarWars("tatooine", "Tatooine", "Planeta desértico de clima extremo, conhecido por suas duas estrelas e vastos mares de areia.", null, "Planeta"),
                new LocalStarWars("hoth", "Hoth", "Mundo gelado e remoto onde a Aliança Rebelde instalou uma base escondida.", null, "Planeta"),
                new LocalStarWars("endor", "Endor", "Lua florestal coberta por árvores gigantes e lar dos Ewoks.", null, "Lua"),
                new LocalStarWars("naboo", "Naboo", "Planeta de lagos e cidades elegantes, lar de humanos e Gungans.", null, "Planeta"),
                new LocalStarWars("mustafar", "Mustafar", "Planeta vulcânico coberto por rios de lava e atividade geotérmica intensa.", null, "Planeta")
        );
    }

    private String textOrDefault(JsonNode node, String field, String defaultValue) {
        String value = node.path(field).asText();
        return value.isBlank() || value.equalsIgnoreCase("unknown") ? defaultValue : value.trim();
    }

    static String criarDescricao(String climate, String terrain, String population) {
        List<String> clues = new ArrayList<>();
        List<String> translatedClimate = traduzirLista(climate, CLIMATE_TRANSLATIONS);
        List<String> translatedTerrain = traduzirLista(terrain, TERRAIN_TRANSLATIONS);

        if (!translatedClimate.isEmpty()) {
            clues.add("clima " + juntarComE(translatedClimate));
        }
        if (!translatedTerrain.isEmpty()) {
            clues.add("paisagens de " + juntarComE(translatedTerrain));
        }

        String populationClue = formatarPopulacao(population);
        if (!populationClue.isBlank()) {
            clues.add("população de aproximadamente " + populationClue + " habitantes");
        }

        if (clues.isEmpty()) {
            return "As características deste planeta ainda não foram catalogadas.";
        }
        return "Este planeta tem " + String.join("; ", clues) + ".";
    }

    private static String juntarComE(List<String> items) {
        if (items.size() < 2) {
            return String.join("", items);
        }
        return String.join(", ", items.subList(0, items.size() - 1))
                + " e " + items.get(items.size() - 1);
    }

    private static List<String> traduzirLista(String value, Map<String, String> translations) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("unknown")) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank() && !item.equalsIgnoreCase("unknown") && !item.equalsIgnoreCase("N/A"))
                .map(item -> translations.getOrDefault(item.toLowerCase(Locale.ROOT), item))
                .distinct()
                .toList();
    }

    private static String formatarPopulacao(String population) {
        if (population == null || population.isBlank() || population.equalsIgnoreCase("unknown")) {
            return "";
        }
        try {
            return NumberFormat.getIntegerInstance(Locale.forLanguageTag("pt-BR"))
                    .format(new BigInteger(population));
        } catch (NumberFormatException exception) {
            return population;
        }
    }

    private String apiId(String url) {
        String normalizedUrl = url.replaceAll("/+$", "");
        int lastSlash = normalizedUrl.lastIndexOf('/');
        return lastSlash >= 0 ? normalizedUrl.substring(lastSlash + 1) : "";
    }
}