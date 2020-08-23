package de.jojomodding.np.ccs.parse;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.stream.Stream;

public class Lexer {

    private String base;
    private int position;
    private Stack<Token> pushedBack;

    protected Lexer(String base) {
        this.base = base;
        this.position = 0;
        this.pushedBack = new Stack<>();
    }

    private int nextChar() {
        if (position >= base.length())
            return -1;
        return base.codePointAt(position++);
    }

    private String nextIdent(int start) {
        StringBuilder sb = new StringBuilder();
        sb.appendCodePoint(start);
        int cc;
        while (Character.isJavaIdentifierPart(cc = nextChar()))
            sb.appendCodePoint(cc);
        position--;
        return sb.toString();
    }

    public Token nextToken() {
        if (!pushedBack.isEmpty()) {
            return pushedBack.pop();
        }
        while (true) {
            int c = nextChar();
            switch (c) {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    continue;
                case -1:
                    return new Token(Token.Type.EOF);
                case '(':
                    return new Token(Token.Type.LPAREN);
                case ')':
                    return new Token(Token.Type.RPAREN);
                case '{':
                    return new Token(Token.Type.LBRACE);
                case '}':
                    return new Token(Token.Type.RBRACE);
                case ',':
                    return new Token(Token.Type.COMMA);
                case '?':
                    return new Token(Token.Type.QUESTIONMARK);
                case '!':
                    return new Token(Token.Type.BANG);
                case '+':
                    return new Token(Token.Type.PLUS);
                case '|':
                    return new Token(Token.Type.PIPE);
                case '\\':
                    return new Token(Token.Type.BACKSLASH);
                case '.':
                    return new Token(Token.Type.DOT);
                case '*':
                    return new Token(Token.Type.STAR);
                case '0':
                    return new Token(Token.Type.STOP);
                case ':': {
                    int cc = nextChar();
                    if (cc == '=')
                        return new Token(Token.Type.COLONEQ);
                }
                default:
                    if (Character.isJavaIdentifierStart(c)) {
                        String s = nextIdent(c);
                        if (s.equals("i") || s.equals("τ"))
                            return new Token(Token.Type.TAU);
                        return new Token(s);
                    } else
                        throw new IllegalArgumentException(new StringBuilder("Invalid character ").appendCodePoint(c).append("!").toString());
            }
        }
    }

    public void pushBack(Token t) {
        pushedBack.push(t);
    }

}
