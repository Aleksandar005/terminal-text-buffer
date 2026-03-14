package com.terminalBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalBufferWriteText {
    private TerminalBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new TerminalBuffer(20, 5);
    }

    @Test
    void writeSimpleText() {
        buffer.writeText("hello");
        assertEquals('h', buffer.getScreenCell(0, 0).getCharacter());
        assertEquals('o', buffer.getScreenCell(0, 4).getCharacter());
    }

    @Test
    void writeMovesCursor() {
        buffer.writeText("abc");
        assertEquals(0, buffer.getCursorRow());
        assertEquals(3, buffer.getCursorColumn());
    }

    @Test
    void writeWrapsAtEdge() {
        buffer.setCursorPosition(0, 18);
        buffer.writeText("abcd");
        // 'a' and 'b' fit on first line and 'c' and 'd' go to second
        assertEquals('a', buffer.getScreenCell(0, 18).getCharacter());
        assertEquals('b', buffer.getScreenCell(0, 19).getCharacter());
        assertEquals('c', buffer.getScreenCell(1, 0).getCharacter());
        assertEquals('d', buffer.getScreenCell(1, 1).getCharacter());
    }

    @Test
    void writeAppliesCurrentAttributes() {
        buffer.setForegroundColor(TerminalColor.RED);
        buffer.setBold(true);
        buffer.writeText("x");

        TerminalCell cell = buffer.getScreenCell(0, 0);
        assertEquals(TerminalColor.RED, cell.getAttributes().getForeground());
        assertTrue(cell.getAttributes().isBold());
    }

    @Test
    void writeScrollsWhenScreenIsFull() {
        // Write enough text to fill the screen line by line
        for (int i = 0; i < 5; i++) {
            buffer.setCursorPosition(i, 0);
            buffer.writeText("line" + i);
        }
        // Now cursor is on the last row. Move to end of it and keep writing.
        buffer.setCursorPosition(4, 19);
        buffer.writeText("X");
        // 'X' fills the last cell. Next write should trigger scroll.
        buffer.writeText("overflow");

        // line0 should have scrolled off, line1 is now at the top
        assertEquals('l', buffer.getScreenCell(0, 0).getCharacter());
    }

    @Test
    void resetAttributesClearsEverything() {
        buffer.setForegroundColor(TerminalColor.GREEN);
        buffer.setBold(true);
        buffer.setItalic(true);
        buffer.setUnderline(true);
        buffer.resetAttributes();

        buffer.writeText("a");
        TerminalCell cell = buffer.getScreenCell(0, 0);
        assertEquals(TerminalColor.DEFAULT, cell.getAttributes().getForeground());
        assertFalse(cell.getAttributes().isBold());
        assertFalse(cell.getAttributes().isItalic());
        assertFalse(cell.getAttributes().isUnderline());
    }
}