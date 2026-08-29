package com.gradeengine.io;

import com.gradeengine.GradeEngine;
import com.gradeengine.domain.ErroDoMotor;

import java.util.ArrayList;
import java.util.List;

/**
 * Avalia a mesma fórmula para uma lista de alunos (tipicamente lida via
 * CsvContextoReader ou JsonContextoReader). A AST é parseada uma vez só
 * (GradeEngine já cacheia por texto de fórmula); erro no cálculo de um
 * aluno não interrompe os demais.
 */
public final class AvaliacaoEmLote {

    private AvaliacaoEmLote() {
    }

    public static List<ResultadoAluno> avaliar(
        GradeEngine engine, String formula, List<ContextoDeAluno> alunos
    ) {
        List<ResultadoAluno> resultados = new ArrayList<>(alunos.size());

        for (ContextoDeAluno aluno : alunos) {
            try {
                double nota = engine.avaliar(formula, aluno.contexto());
                resultados.add(new ResultadoAluno.Ok(aluno.aluno(), nota));
            } catch (ErroDoMotor erro) {
                resultados.add(new ResultadoAluno.Erro(aluno.aluno(), erro));
            }
        }

        return resultados;
    }
}
