package com.gradeengine;

record Token(TipoToken tipo, String texto, double valorNumero) {

    static final Token MAIS = new Token(TipoToken.MAIS, "+", 0.0);
    static final Token MENOS = new Token(TipoToken.MENOS, "-", 0.0);
    static final Token ASTERISCO = new Token(TipoToken.ASTERISCO, "*", 0.0);
    static final Token BARRA = new Token(TipoToken.BARRA, "/", 0.0);
    static final Token PARENTESE_ESQ = new Token(TipoToken.PARENTESE_ESQ, "(", 0.0);
    static final Token PARENTESE_DIR = new Token(TipoToken.PARENTESE_DIR, ")", 0.0);
    static final Token VIRGULA = new Token(TipoToken.VIRGULA, ",", 0.0);
    static final Token MAIOR = new Token(TipoToken.MAIOR, ">", 0.0);
    static final Token MENOR = new Token(TipoToken.MENOR, "<", 0.0);
    static final Token MAIOR_IGUAL = new Token(TipoToken.MAIOR_IGUAL, ">=", 0.0);
    static final Token MENOR_IGUAL = new Token(TipoToken.MENOR_IGUAL, "<=", 0.0);
    static final Token IGUAL = new Token(TipoToken.IGUAL, "=", 0.0);
    static final Token DIFERENTE = new Token(TipoToken.DIFERENTE, "!", 0.0);
    static final Token E = new Token(TipoToken.E, "&", 0.0);
    static final Token OU = new Token(TipoToken.OU, "|", 0.0);

    // Aliases para compatibilidade / ergonomia
    static final Token PLUS = MAIS;
    static final Token MINUS = MENOS;
    static final Token STAR = ASTERISCO;
    static final Token SLASH = BARRA;
    static final Token LEFT_PAREN = PARENTESE_ESQ;
    static final Token RIGHT_PAREN = PARENTESE_DIR;
    static final Token COMMA = VIRGULA;
    static final Token GREATER = MAIOR;
    static final Token LESS = MENOR;
    static final Token GREATER_EQ = MAIOR_IGUAL;
    static final Token LESS_EQ = MENOR_IGUAL;
    static final Token EQUAL = IGUAL;
    static final Token NOT_EQUAL = DIFERENTE;
    static final Token AND = E;
    static final Token OR = OU;

    Token(TipoToken tipo, String texto) {
        this(tipo, texto, 0.0);
    }

    static Token numero(double valor) {
        return new Token(TipoToken.NUMERO, String.valueOf(valor), valor);
    }

    static Token number(double valor) {
        return numero(valor);
    }

    static Token identificador(String nome) {
        return new Token(TipoToken.IDENTIFICADOR, nome, 0.0);
    }

    static Token ident(String nome) {
        return identificador(nome);
    }

    boolean is(TipoToken tipoDesejado) {
        return this.tipo == tipoDesejado;
    }

    static String display(Token token) {
        return token != null ? token.texto() : "";
    }
}
