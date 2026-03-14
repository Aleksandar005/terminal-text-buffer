package com.terminalBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalBufferWideCharTest {
    private TerminalBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new TerminalBuffer(10, 4);
    }

    @Test
    void wideCharTakesTwoCells() {
        buffer.writeText("\u4F60"); // 你
        assertEquals('\u4F60', buffer.getScreenCell(0, 0).getCharacter());
        assertTrue(buffer.getScreenCell(0, 0).isWideChar());
        assertTrue(buffer.getScreenCell(0, 1).isContinuationOfWideChar());
    }

    @Test
    void cursorMovesBy2AfterWideChar() {
        buffer.writeText("\u4F60");
        assertEquals(0, buffer.getCursorRow());
        assertEquals(2, buffer.getCursorColumn());
    }

    @Test
    void wideCharFollowedByNarrow() {
        buffer.writeText("\u4F60x");
        assertEquals('\u4F60', buffer.getScreenCell(0, 0).getCharacter());
        assertTrue(buffer.getScreenCell(0, 1).isContinuationOfWideChar());
        assertEquals('x', buffer.getScreenCell(0, 2).getCharacter());
    }

    @Test
    void wideCharWrapsWhenOnlyOneCellLeft() {
        buffer.setCursorPosition(0, 9);
        buffer.writeText("\u4F60");

        // last cell on first row should be a space, char goes to next row
        assertEquals(' ', buffer.getScreenCell(0, 9).getCharacter());
        assertEquals('\u4F60', buffer.getScreenCell(1, 0).getCharacter());
        assertTrue(buffer.getScreenCell(1, 1).isContinuationOfWideChar());
    }

    @Test
    void multipleWideChars() {
        buffer.writeText("\u4F60\u597D"); // 你好
        assertEquals('\u4F60', buffer.getScreenCell(0, 0).getCharacter());
        assertTrue(buffer.getScreenCell(0, 1).isContinuationOfWideChar());
        assertEquals('\u597D', buffer.getScreenCell(0, 2).getCharacter());
        assertTrue(buffer.getScreenCell(0, 3).isContinuationOfWideChar());
    }

    @Test
    void wideCharAppliesAttributes() {
        buffer.setForegroundColor(TerminalColor.RED);
        buffer.writeText("\u4F60");

        assertEquals(TerminalColor.RED,
                buffer.getScreenCell(0, 0).getAttributes().getForeground());
    }

    @Test
    void getLineAsStringSkipsContinuationCells() {
        buffer.writeText("\u4F60x");
        String line = buffer.getLineAsString(0);
        // should be: 你x followed by spaces, NOT 你 x
        assertTrue(line.startsWith("\u4F60x"));
        assertFalse(line.startsWith("\u4F60 x"));
    }
}
