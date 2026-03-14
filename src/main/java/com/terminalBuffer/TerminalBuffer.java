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

    public void writeText(String text) {
        for (char c : text.toCharArray()) {
            boolean wide = isWideChar(c);

            if (wide && cursorColumn >= width - 1) {
                // Wide char needs 2 cells but we only have 1 left on this line
                // Fill the last cell with a space and wrap
                screen.get(cursorRow)[cursorColumn] = new TerminalCell();
                cursorColumn = 0;
                cursorRow++;
            } else if (cursorColumn >= width) {
                cursorColumn = 0;
                cursorRow++;
            }

            if (cursorRow >= height) {
                scrollUp();
                cursorRow = height - 1;
            }

            TerminalCell cell = new TerminalCell(c, currentAttributes);
            if (wide) {
                cell.setWideChar(true);
            }
            screen.get(cursorRow)[cursorColumn] = cell;
            cursorColumn++;

            if (wide) {
                // Place a continuation cell right after the wide char
                if (cursorColumn >= width) {
                    cursorColumn = 0;
                    cursorRow++;
                    if (cursorRow >= height) {
                        scrollUp();
                        cursorRow = height - 1;
                    }
                }
                TerminalCell cont = new TerminalCell(' ', currentAttributes);
                cont.setContinuationOfWideChar(true);
                screen.get(cursorRow)[cursorColumn] = cont;
                cursorColumn++;
            }
        }
    }

    // Checks if a character takes up 2 cells (CJK, fullwidth forms, etc.)
    private boolean isWideChar(char c) {
        return (c >= 0x1100 && c <= 0x115F)        // Hangul Jamo
                || (c >= 0x2E80 && c <= 0x9FFF)    // CJK radicals, ideographs, etc.
                || (c >= 0xAC00 && c <= 0xD7AF)    // Hangul syllables
                || (c >= 0xF900 && c <= 0xFAFF)    // CJK compatibility ideographs
                || (c >= 0xFE10 && c <= 0xFE6F)    // CJK compatibility forms
                || (c >= 0xFF01 && c <= 0xFF60)    // Fullwidth forms
                || (c >= 0xFFE0 && c <= 0xFFE6);   // Fullwidth signs
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

    public void clearScreen() {
        for (int i = 0; i < height; i++) {
            screen.set(i, createEmptyLine());
        }
        cursorRow = 0;
        cursorColumn = 0;
    }

    public void clearScreenAndScrollback() {
        clearScreen();
        scrollback.clear();
    }

    public char getCharAt(int row, int column) {
        return getCellAt(row, column).getCharacter();
    }


    public CellAttributes getAttributesAt(int row, int column) {
        return getCellAt(row, column).getAttributes();
    }

    // Row index that works across both scrollback and screen.
    // Rows 0..scrollback.size()-1 are scrollback, the rest are screen.
    private TerminalCell getCellAt(int row, int column) {
        if (column < 0 || column >= width) {
            throw new IndexOutOfBoundsException("Column " + column + " out of bounds");
        }

        int scrollbackSize = scrollback.size();

        if (row >= 0 && row < scrollbackSize) {
            return scrollback.get(row)[column];
        }

        int screenRow = row - scrollbackSize;
        if (screenRow >= 0 && screenRow < height) {
            return screen.get(screenRow)[column];
        }

        throw new IndexOutOfBoundsException("Row " + row + " out of bounds");
    }

    public String getLineAsString(int row) {
        int scrollbackSize = scrollback.size();

        TerminalCell[] line;
        if (row >= 0 && row < scrollbackSize) {
            line = scrollback.get(row);
        } else {
            int screenRow = row - scrollbackSize;
            if (screenRow < 0 || screenRow >= height) {
                throw new IndexOutOfBoundsException("Row " + row + " out of bounds");
            }
            line = screen.get(screenRow);
        }

        StringBuilder sb = new StringBuilder();
        for (TerminalCell cell : line) {
            if (!cell.isContinuationOfWideChar()) {
                sb.append(cell.getCharacter());
            }
        }
        return sb.toString();
    }

    public String getScreenAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < height; i++) {
            for (TerminalCell cell : screen.get(i)) {
                sb.append(cell.getCharacter());
            }
            if (i < height - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    public String getFullContentAsString() {
        StringBuilder sb = new StringBuilder();

        for (TerminalCell[] line : scrollback) {
            for (TerminalCell cell : line) {
                sb.append(cell.getCharacter());
            }
            sb.append('\n');
        }

        sb.append(getScreenAsString());
        return sb.toString();
    }
}
