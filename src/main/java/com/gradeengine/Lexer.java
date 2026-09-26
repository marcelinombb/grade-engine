package com.gradeengine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class Lexer {

    private final String input;
    private int cursor = 0;

    Lexer(String input) {
        this.input = input;
    }

    public Optional<Token> nextToken() throws ErroLexico {
        return Optional.ofNullable(proximoToken());
    }

    Token proximoToken() throws ErroLexico {
        while (cursor < input.length()) {
            char chr = input.charAt(cursor++);

            switch (chr) {
                case ' ', '\n', '\t', '\r' -> {
                    continue;
                }
                case '+' -> { return Token.MAIS; }
                case '-' -> { return Token.MENOS; }
                case '*' -> { return Token.ASTERISCO; }
                case '/' -> { return Token.BARRA; }
                case '(' -> { return Token.PARENTESE_ESQ; }
                case ')' -> { return Token.PARENTESE_DIR; }
                case ',' -> { return Token.VIRGULA; }
                case '|' -> { return Token.OU; }
                case '&' -> { return Token.E; }
                case '<' -> {
                    if (cursor < input.length() && input.charAt(cursor) == '=') {
                        cursor++;
                        return Token.MENOR_IGUAL;
                    }
                    return Token.MENOR;
                }
                case '>' -> {
                    if (cursor < input.length() && input.charAt(cursor) == '=') {
                        cursor++;
                        return Token.MAIOR_IGUAL;
                    }
                    return Token.MAIOR;
                }
                case '=' -> {
                    return Token.IGUAL;
                }
                case '!' -> {
                    return Token.DIFERENTE;
                }
                default -> {
                    if (Character.isDigit(chr)) {
                        return consumeNumber(chr);
                    } else if (Character.isLetter(chr) || chr == '_') {
                        return consumeIdentifier(chr);
                    } else {
                        throw new ErroLexico.CaractereInvalido(String.valueOf(chr));
                    }
                }
            }
        }
        return null;
    }

    public List<Token> tokens() throws ErroLexico {
        List<Token> tokens = new ArrayList<>();
        Token token;
        while ((token = proximoToken()) != null) {
            tokens.add(token);
        }
        return tokens;
    }

    private Token consumeNumber(char primeiro) throws ErroLexico {
        StringBuilder numero = new StringBuilder().append(primeiro);
        boolean hasDot = false;

        while (cursor < input.length()) {
            char peeked = input.charAt(cursor);

            if (Character.isDigit(peeked) || peeked == '.') {
                if (peeked == '.' && hasDot) {
                    throw new ErroLexico.MultiplosPontos("Numero inválido " + numero + peeked);
                } else if (peeked == '.') {
                    hasDot = true;
                }
                numero.append(peeked);
                cursor++;
            } else {
                break;
            }
        }

        try {
            double valor = Double.parseDouble(numero.toString());
            return Token.numero(valor);
        } catch (NumberFormatException e) {
            throw new ErroLexico.NumeroInvalido(e.getMessage());
        }
    }

    private Token consumeIdentifier(char primeiro) {
        StringBuilder identifier = new StringBuilder().append(primeiro);

        while (cursor < input.length()) {
            char peeked = input.charAt(cursor);
            if (Character.isLetterOrDigit(peeked) || peeked == '_') {
                identifier.append(peeked);
                cursor++;
            } else {
                break;
            }
        }

        return Token.identificador(identifier.toString());
    }
}
