package com.terminalBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalBufferEditTest {
    private TerminalBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new TerminalBuffer(10, 5);
    }

    void insertTextShiftsExistingContent() {
        buffer.writeText("abcd");
        buffer.setCursorPosition(0, 1);
        buffer.insertText("XX");

        assertEquals('a', buffer.getScreenCell(0, 0).getCharacter());
        assertEquals('X', buffer.getScreenCell(0, 1).getCharacter());
        assertEquals('X', buffer.getScreenCell(0, 2).getCharacter());
        assertEquals('b', buffer.getScreenCell(0, 3).getCharacter());
        assertEquals('c', buffer.getScreenCell(0, 4).getCharacter());
        assertEquals('d', buffer.getScreenCell(0, 5).getCharacter());
    }

    @Test
    void insertTextDropsContentAtRightEdge() {
        buffer.writeText("0123456789");
        buffer.setCursorPosition(0, 0);
        buffer.insertText("AB");

        // '8' and '9' should have fallen off the right edge
        assertEquals('A', buffer.getScreenCell(0, 0).getCharacter());
        assertEquals('B', buffer.getScreenCell(0, 1).getCharacter());
        assertEquals('0', buffer.getScreenCell(0, 2).getCharacter());
        assertEquals('7', buffer.getScreenCell(0, 9).getCharacter());
    }

    @Test
    void insertTextWrapsToNextLine() {
        buffer.setCursorPosition(0, 8);
        buffer.insertText("abcde");

        assertEquals('a', buffer.getScreenCell(0, 8).getCharacter());
        assertEquals('b', buffer.getScreenCell(0, 9).getCharacter());
        assertEquals('c', buffer.getScreenCell(1, 0).getCharacter());
    }

    @Test
    void insertTextAppliesAttributes() {
        buffer.setForegroundColor(TerminalColor.CYAN);
        buffer.insertText("z");

        assertEquals(TerminalColor.CYAN,
                buffer.getScreenCell(0, 0).getAttributes().getForeground());
    }

    @Test
    void fillLineWithCharacter() {
        buffer.setForegroundColor(TerminalColor.YELLOW);
        buffer.fillLine(2, '-');

        for (int col = 0; col < buffer.getWidth(); col++) {
            assertEquals('-', buffer.getScreenCell(2, col).getCharacter());
            assertEquals(TerminalColor.YELLOW,
                    buffer.getScreenCell(2, col).getAttributes().getForeground());
        }
    }

    @Test
    void fillLineEmptyResetsToDefaults() {
        buffer.setForegroundColor(TerminalColor.RED);
        buffer.fillLine(0, 'X');
        buffer.fillLineEmpty(0);

        for (int col = 0; col < buffer.getWidth(); col++) {
            assertEquals(' ', buffer.getScreenCell(0, col).getCharacter());
            assertEquals(TerminalColor.DEFAULT,
                    buffer.getScreenCell(0, col).getAttributes().getForeground());
        }
    }

    @Test
    void fillLineIgnoresInvalidRow() {
        // Should not throw, just do nothing
        buffer.fillLine(-1, 'x');
        buffer.fillLine(99, 'x');
    }

    @Test
    void insertEmptyLineScrollsScreenUp() {
        buffer.setCursorPosition(0, 0);
        buffer.writeText("first");
        buffer.setCursorPosition(1, 0);
        buffer.writeText("second");

        buffer.insertEmptyLineAtBottom();

        // "first" should have gone to scrollback, "second" is now row 0
        assertEquals('s', buffer.getScreenCell(0, 0).getCharacter());
    }
}
