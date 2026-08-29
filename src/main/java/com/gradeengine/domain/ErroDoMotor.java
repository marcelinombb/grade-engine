package com.gradeengine.domain;

/**
 * Une os erros das 3 camadas do pipeline (lexer -> parser -> avaliador)
 * em um único tipo, do mesmo jeito que o enum ErroDoMotor faz no Rust --
 * lá isso permite usar `?` sem match aninhado por camada; aqui, como toda
 * exception já se propaga sozinha, o papel do wrapper é só preservar de
 * qual camada o erro veio, via getCause().
 */
public final class ErroDoMotor extends Exception {

    private ErroDoMotor(Exception causa) {
        super(causa.getMessage(), causa);
    }

    public static ErroDoMotor lexico(ErroLexico erro) {
        return new ErroDoMotor(erro);
    }

    public static ErroDoMotor parse(ErroDeParse erro) {
        return new ErroDoMotor(erro);
    }

    public static ErroDoMotor calculo(ErroDeCalculo erro) {
        return new ErroDoMotor(erro);
    }
}
