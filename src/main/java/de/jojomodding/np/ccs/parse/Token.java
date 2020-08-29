package de.jojomodding.np.ccs.parse;

public class Token {

    private final Type type;
    private final String text;
    public Token(Type type) {
        this.type = type;
        this.text = null;
    }

    public Token(String text) {
        this.type = Type.IDENT;
        this.text = text;
    }

    public Type getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        if (type == Type.IDENT)
            return "IDENT(" + text + ")";
        return type.toString();
    }

    public static enum Type {
        LPAREN, RPAREN,
        LBRACE, RBRACE, COMMA,
        IDENT, TAU, QUESTIONMARK, BANG,
        PLUS, PIPE, BACKSLASH, DOT, STAR, STOP,
        COLONEQ,

        EOF
    }
}
