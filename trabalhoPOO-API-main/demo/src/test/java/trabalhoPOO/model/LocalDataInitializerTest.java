package trabalhoPOO.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalDataInitializerTest {

    @Test
    void traduzClimaTerrenoEFormataPopulacao() {
        String description = LocalDataInitializer.criarDescricao(
                "temperate, arid",
                "rock, desert, mountain, barren",
                "100000000000");

        assertTrue(description.contains("clima temperado e árido"));
        assertTrue(description.contains("formações rochosas, desertos, montanhas e áreas áridas"));
        assertTrue(description.contains("100.000.000.000 habitantes"));
        assertFalse(description.contains("temperate"));
        assertFalse(description.contains("100000000000"));
    }

    @Test
    void traduzTerrenosUrbanosOceanicosEPantanosos() {
        String description = LocalDataInitializer.criarDescricao(
                "temperate",
                "urban, oceans, swamps, bogs",
                "7000000000");

        assertTrue(description.contains("clima temperado"));
        assertTrue(description.contains("áreas urbanas, oceanos, pântanos e brejos"));
        assertTrue(description.contains("7.000.000.000 habitantes"));
        assertFalse(description.contains("swamps"));
        assertFalse(description.contains("bogs"));
    }
}