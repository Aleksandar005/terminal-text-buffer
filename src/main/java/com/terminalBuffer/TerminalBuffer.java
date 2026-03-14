package com.terminalBuffer;

import java.util.ArrayList;
import java.util.List;

public class TerminalBuffer {
    private final int width;
    private int height;
    private final int maxScrollbackSize;

    private final List<TerminalCell[]> screen;
    private final List<TerminalCell[]> scrollback;

    private int cursorRow;
    private int cursorColumn;

    private final CellAttributes currentAttributes;

    public TerminalBuffer(int width, int height, int maxScrollbackSize) {
        this.width = width;
        this.height = height;
        this.maxScrollbackSize = maxScrollbackSize;

        this.screen = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            screen.add(createEmptyLine());
        }

        this.scrollback = new ArrayList<>();

        this.cursorRow = 0;
        this.cursorColumn = 0;

        this.currentAttributes = new CellAttributes();
    }

    public TerminalBuffer(int width, int height) {
        this(width, height, 1000);
    }

    private TerminalCell[] createEmptyLine() {
        TerminalCell[] line = new TerminalCell[width];
        for (int i = 0; i < width; i++) {
            line[i] = new TerminalCell();
        }
        return line;
    }

    public void setCursorPosition(int row, int column) {
        // Clamp to screen bounds so cursor never ends up outside the grid
        this.cursorRow = Math.max(0, Math.min(row, height - 1));
        this.cursorColumn = Math.max(0, Math.min(column, width - 1));
    }

    public void moveCursorUp(int n) {
        cursorRow = Math.max(0, cursorRow - n);
    }

    public void moveCursorDown(int n) {
        cursorRow = Math.min(height - 1, cursorRow + n);
    }

    public void moveCursorLeft(int n) {
        cursorColumn = Math.max(0, cursorColumn - n);
    }

    public void moveCursorRight(int n) {
        cursorColumn = Math.min(width - 1, cursorColumn + n);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getCursorRow() {
        return cursorRow;
    }

    public int getCursorColumn() {
        return cursorColumn;
    }

    public CellAttributes getCurrentAttributes() {
        return currentAttributes;
    }

    public void setForegroundColor(TerminalColor color) {
        currentAttributes.setForeground(color);
    }

    public void setBackgroundColor(TerminalColor color) {
        currentAttributes.setBackground(color);
    }

    public void setBold(boolean bold) {
        currentAttributes.setBold(bold);
    }

    public void setItalic(boolean italic) {
        currentAttributes.setItalic(italic);
    }

    public void setUnderline(boolean underline) {
        currentAttributes.setUnderline(underline);
    }

    public void resetAttributes() {
        currentAttributes.setForeground(TerminalColor.DEFAULT);
        currentAttributes.setBackground(TerminalColor.DEFAULT);
        currentAttributes.setBold(false);
        currentAttributes.setItalic(false);
        currentAttributes.setUnderline(false);
    }

    public void writeText(String text){
        for(char c : text.toCharArray()){
            if(cursorColumn >= width){
                // Go to the next line if we hit the edge
                cursorColumn = 0;
                cursorRow++;
            }
            if(cursorRow >= height){
                scrollUp();
                cursorRow = height - 1;
            }

            screen.get(cursorRow)[cursorColumn] = new TerminalCell(c, currentAttributes);
            cursorColumn++;
        }
    }

    private void scrollUp(){
        TerminalCell[] topLine = screen.remove(0);
        scrollback.add(topLine);

        // Don't let scrollback grow forever
        if (scrollback.size() > maxScrollbackSize) {
            scrollback.remove(0);
        }

        screen.add(createEmptyLine());
    }

    public TerminalCell getScreenCell(int row, int column) {
        if (row < 0 || row >= height || column < 0 || column >= width) {
            throw new IndexOutOfBoundsException(
                    "Position (" + row + ", " + column + ") is out of screen bounds"
            );
        }
        return screen.get(row)[column];
    }

    public void insertText(String text){
        TerminalCell[] line = screen.get(cursorRow);

        for (char c : text.toCharArray()) {
            if (cursorColumn >= width) {
                cursorColumn = 0;
                cursorRow++;

                if (cursorRow >= height) {
                    scrollUp();
                    cursorRow = height - 1;
                }
                line = screen.get(cursorRow);
            }

            // Shift existing cells to the right to make room
            for (int i = width - 1; i > cursorColumn; i--) {
                line[i] = line[i - 1];
            }

            line[cursorColumn] = new TerminalCell(c, currentAttributes);
            cursorColumn++;
        }
    }

    public void fillLine(int row, char c) {
        if (row < 0 || row >= height) return;

        TerminalCell[] line = screen.get(row);
        for (int i = 0; i < width; i++) {
            line[i] = new TerminalCell(c, currentAttributes);
        }
    }

    public void fillLineEmpty(int row) {
        if (row < 0 || row >= height) return;
        screen.set(row, createEmptyLine());
    }

    public void insertEmptyLineAtBottom() {
        scrollUp();
    }
}
