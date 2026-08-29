package com.gradeengine.domain;

public sealed interface Token {

    record Number(double value) implements Token {}

    record Ident(String name) implements Token {}

    record Plus() implements Token {}
    record Minus() implements Token {}
    record Star() implements Token {}
    record Slash() implements Token {}
    record LeftParen() implements Token {}
    record RightParen() implements Token {}
    record Comma() implements Token {}
    record Greater() implements Token {}
    record Less() implements Token {}
    record GreaterEq() implements Token {}
    record LessEq() implements Token {}
    record Equal() implements Token {}
    record NotEqual() implements Token {}
    record And() implements Token {}
    record Or() implements Token {}

    Token PLUS = new Plus();
    Token MINUS = new Minus();
    Token STAR = new Star();
    Token SLASH = new Slash();
    Token LEFT_PAREN = new LeftParen();
    Token RIGHT_PAREN = new RightParen();
    Token COMMA = new Comma();
    Token GREATER = new Greater();
    Token LESS = new Less();
    Token GREATER_EQ = new GreaterEq();
    Token LESS_EQ = new LessEq();
    Token EQUAL = new Equal();
    Token NOT_EQUAL = new NotEqual();
    Token AND = new And();
    Token OR = new Or();

    static String display(Token token) {
        return switch (token) {
            case Number n -> String.valueOf(n.value());
            case Ident i -> i.name();
            case Plus p -> "+";
            case Minus m -> "-";
            case Star s -> "*";
            case Slash s -> "/";
            case LeftParen p -> "(";
            case RightParen p -> ")";
            case Comma c -> ",";
            case Greater g -> ">";
            case Less l -> "<";
            case GreaterEq g -> ">=";
            case LessEq l -> "<=";
            case Equal e -> "=";
            case NotEqual n -> "!";
            case And a -> "&";
            case Or o -> "|";
        };
    }
}
