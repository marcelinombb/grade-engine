package com.gradeengine.io;

import com.gradeengine.ErroDoMotor;

/**
 * Resultado do cálculo de nota de um aluno específico. Um erro aqui
 * (fórmula referenciando uma avaliação que esse aluno não tem, por
 * exemplo) não deve interromper o processamento dos demais alunos do
 * mesmo lote -- por isso o erro é carregado como valor, não relançado.
 */
public sealed interface ResultadoAluno {

    String aluno();

    record Ok(String aluno, double nota) implements ResultadoAluno {}

    record Erro(String aluno, ErroDoMotor erro) implements ResultadoAluno {}
}
