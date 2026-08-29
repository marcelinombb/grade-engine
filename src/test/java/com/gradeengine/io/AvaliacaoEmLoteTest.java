package com.gradeengine.io;

import com.gradeengine.GradeEngine;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AvaliacaoEmLoteTest {

    @Test
    void avaliaMediaParaTodosOsAlunosDoCsv() throws ErroDeLeituraDeDados {
        String csv = """
            aluno,P1,P2
            Joao,6.0,10.0
            Maria,8.0,8.0
            """;

        List<ContextoDeAluno> alunos = CsvContextoReader.ler(new StringReader(csv));
        List<ResultadoAluno> resultados = AvaliacaoEmLote.avaliar(new GradeEngine(), "media(P1, P2)", alunos);

        assertEquals(2, resultados.size());

        assertInstanceOf(ResultadoAluno.Ok.class, resultados.get(0));
        assertEquals(8.0, ((ResultadoAluno.Ok) resultados.get(0)).nota());

        assertInstanceOf(ResultadoAluno.Ok.class, resultados.get(1));
        assertEquals(8.0, ((ResultadoAluno.Ok) resultados.get(1)).nota());
    }

    @Test
    void erroDeUmAlunoNaoInterrompeOsDemais() throws ErroDeLeituraDeDados {
        // Maria não tem P2 -- a fórmula referencia uma avaliação inexistente
        // só para ela; Joao e Pedro devem ser calculados normalmente.
        String csv = """
            aluno,P1,P2
            Joao,6.0,10.0
            Maria,8.0,8.0
            Pedro,5.0,7.0
            """;

        List<ContextoDeAluno> alunos = CsvContextoReader.ler(new StringReader(csv));

        // Remove P2 do contexto da Maria manualmente para simular dado
        // faltante (o CSV em si tem a coluna, então criamos o cenário
        // direto no ContextoDeCalculo do meio da lista).
        List<ContextoDeAluno> comFalha = List.of(
            alunos.get(0),
            new ContextoDeAluno("Maria", new com.gradeengine.domain.ContextoDeCalculo()),
            alunos.get(2)
        );

        List<ResultadoAluno> resultados = AvaliacaoEmLote.avaliar(new GradeEngine(), "media(P1, P2)", comFalha);

        assertEquals(3, resultados.size());
        assertInstanceOf(ResultadoAluno.Ok.class, resultados.get(0));
        assertEquals("Joao", resultados.get(0).aluno());

        assertInstanceOf(ResultadoAluno.Erro.class, resultados.get(1));
        assertEquals("Maria", resultados.get(1).aluno());

        assertInstanceOf(ResultadoAluno.Ok.class, resultados.get(2));
        assertEquals("Pedro", resultados.get(2).aluno());
    }
}
