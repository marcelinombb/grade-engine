package com.gradeengine.io;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvContextoReaderTest {

    @Test
    void leDuasLinhasComTresNotasCada() throws ErroDeLeituraDeDados {
        String csv = """
            aluno,P1,P2,P3
            Joao,8.0,6.5,9.0
            Maria,7.0,8.0,10.0
            """;

        List<ContextoDeAluno> contextos = CsvContextoReader.ler(new StringReader(csv));

        assertEquals(2, contextos.size());
        assertEquals("Joao", contextos.get(0).aluno());
        assertEquals(8.0, contextos.get(0).contexto().buscar("P1").orElseThrow());
        assertEquals(6.5, contextos.get(0).contexto().buscar("P2").orElseThrow());
        assertEquals(9.0, contextos.get(0).contexto().buscar("P3").orElseThrow());

        assertEquals("Maria", contextos.get(1).aluno());
        assertEquals(10.0, contextos.get(1).contexto().buscar("P3").orElseThrow());
    }

    @Test
    void semColunaAlunoRetornaErro() {
        String csv = """
            P1,P2
            8.0,6.5
            """;

        assertThrows(ErroDeLeituraDeDados.class, () -> CsvContextoReader.ler(new StringReader(csv)));
    }

    @Test
    void valorNaoNumericoRetornaErro() {
        String csv = """
            aluno,P1
            Joao,oito
            """;

        assertThrows(ErroDeLeituraDeDados.class, () -> CsvContextoReader.ler(new StringReader(csv)));
    }
}
