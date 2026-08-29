package com.gradeengine.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Lexer {

    private final String input;
    private int cursor = 0;

    public Lexer(String input) {
        this.input = input;
    }

    public Optional<Token> nextToken() throws ErroLexico {
        while (true) {
            Optional<Character> maybeChr = advance();
            if (maybeChr.isEmpty()) return Optional.empty();
            char chr = maybeChr.get();

            switch (chr) {
                case ' ', '\n', '\t', '\r' -> {
                    continue;
                }
                case '+' -> {
                    return Optional.of(Token.PLUS);
                }
                case '-' -> {
                    return Optional.of(Token.MINUS);
                }
                case '*' -> {
                    return Optional.of(Token.STAR);
                }
                case '/' -> {
                    return Optional.of(Token.SLASH);
                }
                case '(' -> {
                    return Optional.of(Token.LEFT_PAREN);
                }
                case ')' -> {
                    return Optional.of(Token.RIGHT_PAREN);
                }
                case ',' -> {
                    return Optional.of(Token.COMMA);
                }
                case '|' -> {
                    return Optional.of(Token.OR);
                }
                case '&' -> {
                    return Optional.of(Token.AND);
                }
                default -> {
                    if (isComparacao(chr)) {
                        return Optional.of(consumeComparationChars(chr));
                    } else if (Character.isDigit(chr)) {
                        return Optional.of(consumeNumberChars(chr));
                    } else if (Character.isLetterOrDigit(chr)) {
                        return Optional.of(consumeIdentifierChars(chr));
                    } else {
                        throw new ErroLexico.CaractereInvalido(String.valueOf(chr));
                    }
                }
            }
        }
    }

    public List<Token> tokens() throws ErroLexico {
        List<Token> tokens = new ArrayList<>();

        while (true) {
            Optional<Token> token = nextToken();
            if (token.isEmpty()) break;
            tokens.add(token.get());
        }

        return tokens;
    }

    private static boolean isComparacao(char ch) {
        return ch == '<' || ch == '>' || ch == '=' || ch == '!';
    }

    private Token consumeNumberChars(char ch) throws ErroLexico {
        StringBuilder numero = new StringBuilder().append(ch);
        boolean hasDot = false;

        while (true) {
            Optional<Character> maybeChr = peek();
            if (maybeChr.isEmpty()) break;
            char peeked = maybeChr.get();

            if (Character.isDigit(peeked) || peeked == '.') {
                if (peeked == '.' && hasDot) {
                    throw new ErroLexico.MultiplosPontos("Numero inválido " + numero + peeked);
                } else if (peeked == '.') {
                    hasDot = true;
                }
                numero.append(advance().get());
            } else {
                break;
            }
        }

        try {
            return new Token.Number(Double.parseDouble(numero.toString()));
        } catch (NumberFormatException e) {
            throw new ErroLexico.NumeroInvalido(e.getMessage());
        }
    }

    private Token consumeIdentifierChars(char ch) {
        StringBuilder identifier = new StringBuilder().append(ch);

        while (true) {
            Optional<Character> maybeChr = peek();
            if (maybeChr.isEmpty()) break;
            char peeked = maybeChr.get();

            if (Character.isLetterOrDigit(peeked)) {
                identifier.append(advance().get());
            } else {
                break;
            }
        }

        return new Token.Ident(identifier.toString());
    }

    private Token consumeComparationChars(char ch) {
        return switch (ch) {
            case '=' -> {
                advance();
                yield Token.EQUAL;
            }
            case '<' -> {
                if (peek().equals(Optional.of('='))) {
                    advance();
                    yield Token.LESS_EQ;
                } else {
                    yield Token.LESS;
                }
            }
            case '>' -> {
                if (peek().equals(Optional.of('='))) {
                    advance();
                    yield Token.GREATER_EQ;
                } else {
                    yield Token.GREATER;
                }
            }
            case '!' -> {
                advance();
                yield Token.NOT_EQUAL;
            }
            default -> throw new IllegalStateException("caractere inválido: " + ch);
        };
    }

    private Optional<Character> peek() {
        if (cursor >= input.length()) {
            return Optional.empty();
        }
        return Optional.of(input.charAt(cursor));
    }

    private Optional<Character> advance() {
        Optional<Character> character = peek();
        cursor += 1;
        return character;
    }
}
