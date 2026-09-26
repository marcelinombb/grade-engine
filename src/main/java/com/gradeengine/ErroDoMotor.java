package com.gradeengine;

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
