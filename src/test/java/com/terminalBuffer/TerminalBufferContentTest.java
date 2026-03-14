package com.terminalBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalBufferContentTest {
    private TerminalBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new TerminalBuffer(10, 4, 5);
    }

    @Test
    void getCharAtReturnsCorrectCharacter() {
        buffer.writeText("hello");
        // scrollback is empty so screen starts at row 0
        assertEquals('h', buffer.getCharAt(0, 0));
        assertEquals('e', buffer.getCharAt(0, 1));
    }

    @Test
    void getCharAtReturnsSpaceForEmptyCell() {
        assertEquals(' ', buffer.getCharAt(0, 0));
    }

    @Test
    void getCharAtThrowsOnInvalidColumn() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            buffer.getCharAt(0, 99);
        });
    }

    @Test
    void getCharAtThrowsOnInvalidRow() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            buffer.getCharAt(999, 0);
        });
    }

    @Test
    void getAttributesAtReturnsWrittenAttributes() {
        buffer.setForegroundColor(TerminalColor.BLUE);
        buffer.setBold(true);
        buffer.writeText("x");

        CellAttributes attrs = buffer.getAttributesAt(0, 0);
        assertEquals(TerminalColor.BLUE, attrs.getForeground());
        assertTrue(attrs.isBold());
    }

    @Test
    void getLineAsStringFromScreen() {
        buffer.writeText("hey");
        String line = buffer.getLineAsString(0);
        // "hey" plus 7 spaces to fill the width
        assertEquals("hey       ", line);
    }

    @Test
    void getLineAsStringFromScrollback() {
        // Write a full line on each row, then keep pushing lines to force scrollback
        buffer.setCursorPosition(0, 0);
        buffer.writeText("SCROLLME");

        // Push that line off the screen by inserting empty lines at bottom
        for (int i = 0; i < 4; i++) {
            buffer.insertEmptyLineAtBottom();
        }

        // "SCROLLME" should now be in scrollback at row 0
        String line = buffer.getLineAsString(0);
        assertTrue(line.startsWith("SCROLLME"));
    }

    @Test
    void getLineAsStringThrowsOnBadRow() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            buffer.getLineAsString(-1);
        });
    }

    @Test
    void getScreenAsStringShowsAllRows() {
        buffer.setCursorPosition(0, 0);
        buffer.writeText("aaa");
        buffer.setCursorPosition(1, 0);
        buffer.writeText("bbb");

        String screen = buffer.getScreenAsString();
        String[] lines = screen.split("\n", -1);
        assertEquals(4, lines.length);
        assertTrue(lines[0].startsWith("aaa"));
        assertTrue(lines[1].startsWith("bbb"));
    }

    @Test
    void getScreenAsStringOnEmptyBuffer() {
        String screen = buffer.getScreenAsString();
        // Should be 4 lines of 10 spaces each
        String[] lines = screen.split("\n", -1);
        assertEquals(4, lines.length);
        for (String line : lines) {
            assertEquals(10, line.length());
            assertEquals("          ", line);
        }
    }

    @Test
    void getFullContentIncludesScrollback() {
        // Push some lines into scrollback by overflowing the screen
        for (int i = 0; i < 7; i++) {
            buffer.setCursorPosition(buffer.getHeight() - 1, 0);
            buffer.writeText("L" + i);
            if (i < 6) buffer.insertEmptyLineAtBottom();
        }

        String full = buffer.getFullContentAsString();
        // Should contain both scrollback and screen content
        assertTrue(full.contains("L"));
        // More lines than just the screen
        String[] lines = full.split("\n", -1);
        assertTrue(lines.length > buffer.getHeight());
    }

    @Test
    void clearScreenResetsEverything() {
        buffer.writeText("stuff");
        buffer.setCursorPosition(2, 5);
        buffer.clearScreen();

        assertEquals(0, buffer.getCursorRow());
        assertEquals(0, buffer.getCursorColumn());
        assertEquals(' ', buffer.getScreenCell(0, 0).getCharacter());
        assertEquals(' ', buffer.getScreenCell(2, 5).getCharacter());
    }

    @Test
    void clearScreenDoesNotTouchScrollback() {
        // Push a line into scrollback
        for (int i = 0; i < 5; i++) {
            buffer.insertEmptyLineAtBottom();
        }
        buffer.setCursorPosition(0, 0);
        buffer.writeText("visible");

        buffer.clearScreen();

        String full = buffer.getFullContentAsString();
        // Full content should be longer than just the screen
        // because scrollback is still there
        String[] lines = full.split("\n", -1);
        assertTrue(lines.length > buffer.getHeight());
    }

    @Test
    void clearScreenAndScrollbackClearsBoth() {
        for (int i = 0; i < 5; i++) {
            buffer.insertEmptyLineAtBottom();
        }
        buffer.setCursorPosition(0, 0);
        buffer.writeText("text");

        buffer.clearScreenAndScrollback();

        String full = buffer.getFullContentAsString();
        String[] lines = full.split("\n", -1);
        assertEquals(buffer.getHeight(), lines.length);
        for (String line : lines) {
            assertEquals("          ", line);
        }
    }


    @Test
    void scrollbackRespectsMaxSize() {
        // maxScrollbackSize is 5, push 8 lines through
        for (int i = 0; i < 8; i++) {
            buffer.setCursorPosition(buffer.getHeight() - 1, 0);
            buffer.writeText("s" + i);
            buffer.insertEmptyLineAtBottom();
        }

        String full = buffer.getFullContentAsString();
        String[] lines = full.split("\n", -1);
        // 5 scrollback + 4 screen = 9 max
        assertTrue(lines.length <= 5 + buffer.getHeight());
    }
}
