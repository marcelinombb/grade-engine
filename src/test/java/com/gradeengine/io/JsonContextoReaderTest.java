package com.gradeengine.io;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonContextoReaderTest {

    @Test
    void leDoisObjetosComTresNotasCada() throws ErroDeLeituraDeDados {
        String json = """
            [
              {"aluno": "Joao", "P1": 8.0, "P2": 6.5, "P3": 9.0},
              {"aluno": "Maria", "P1": 7.0, "P2": 8.0, "P3": 10.0}
            ]
            """;

        List<ContextoDeAluno> contextos = JsonContextoReader.ler(new StringReader(json));

        assertEquals(2, contextos.size());
        assertEquals("Joao", contextos.get(0).aluno());
        assertEquals(8.0, contextos.get(0).contexto().buscar("P1").orElseThrow());

        assertEquals("Maria", contextos.get(1).aluno());
        assertEquals(10.0, contextos.get(1).contexto().buscar("P3").orElseThrow());
    }

    @Test
    void raizQueNaoEhArrayRetornaErro() {
        String json = """
            {"aluno": "Joao", "P1": 8.0}
            """;

        assertThrows(ErroDeLeituraDeDados.class, () -> JsonContextoReader.ler(new StringReader(json)));
    }

    @Test
    void semCampoAlunoRetornaErro() {
        String json = """
            [{"P1": 8.0}]
            """;

        assertThrows(ErroDeLeituraDeDados.class, () -> JsonContextoReader.ler(new StringReader(json)));
    }

    @Test
    void valorNaoNumericoRetornaErro() {
        String json = """
            [{"aluno": "Joao", "P1": "oito"}]
            """;

        assertThrows(ErroDeLeituraDeDados.class, () -> JsonContextoReader.ler(new StringReader(json)));
    }

    @Test
    void jsonMalformadoRetornaErro() {
        String json = "[{\"aluno\": \"Joao\", ";

        assertThrows(ErroDeLeituraDeDados.class, () -> JsonContextoReader.ler(new StringReader(json)));
    }
}
