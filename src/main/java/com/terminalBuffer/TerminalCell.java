package com.terminalBuffer;

public class TerminalCell {
    private char character;
    private CellAttributes attributes;

    public TerminalCell() {
        this.character = ' ';
        this.attributes = new CellAttributes();
    }

    public TerminalCell(char character, CellAttributes attributes) {
        this.character = character;
        this.attributes = attributes;
    }

    public char getCharacter() {
        return character;
    }

    public void setCharacter(char character) {
        this.character = character;
    }

    public CellAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(CellAttributes attributes) {
        this.attributes = attributes.copy();
    }

    public void reset(){
        this.character = ' ';
        this.attributes = new CellAttributes();
    }
}
