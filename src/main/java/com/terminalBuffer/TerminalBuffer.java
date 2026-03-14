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
}
