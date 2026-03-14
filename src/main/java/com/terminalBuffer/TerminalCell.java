package com.terminalBuffer;

public class TerminalCell {
    private char character;
    private CellAttributes attributes;

    private boolean wideChar;
    private boolean continuationOfWideChar;

    public TerminalCell() {
        this.character = ' ';
        this.attributes = new CellAttributes();
        this.wideChar = false;
        this.continuationOfWideChar = false;
    }

    public TerminalCell(char character, CellAttributes attributes) {
        this.character = character;
        this.attributes = attributes.copy();
        this.wideChar = false;
        this.continuationOfWideChar = false;
    }

    public void reset(){
        this.character = ' ';
        this.attributes = new CellAttributes();
        this.wideChar = false;
        this.continuationOfWideChar = false;
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

    public boolean isWideChar() {
        return wideChar;
    }

    public void setWideChar(boolean wideChar) {
        this.wideChar = wideChar;
    }

    public boolean isContinuationOfWideChar() {
        return continuationOfWideChar;
    }

    public void setContinuationOfWideChar(boolean continuationOfWideChar) {
        this.continuationOfWideChar = continuationOfWideChar;
    }
}
