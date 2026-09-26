package com.gradeengine.io;

import com.gradeengine.ContextoDeCalculo;

/**
 * Uma linha de entrada (CSV ou JSON) já traduzida para o contexto de
 * cálculo do motor, mantendo o identificador do aluno para reassociar o
 * resultado depois.
 */
public record ContextoDeAluno(String aluno, ContextoDeCalculo contexto) {
}
