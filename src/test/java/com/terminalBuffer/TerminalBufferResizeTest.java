package com.terminalBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalBufferResizeTest {

    private TerminalBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new TerminalBuffer(10, 4, 5);
    }

    @Test
    void resizeWiderKeepsContent() {
        buffer.writeText("hello");
        buffer.resize(20, 4);

        assertEquals(20, buffer.getWidth());
        assertEquals('h', buffer.getScreenCell(0, 0).getCharacter());
        assertEquals('o', buffer.getScreenCell(0, 4).getCharacter());
    }

    @Test
    void resizeNarrowerTruncatesLines() {
        buffer.writeText("0123456789");
        buffer.resize(5, 4);

        assertEquals(5, buffer.getWidth());
        assertEquals('0', buffer.getScreenCell(0, 0).getCharacter());
        assertEquals('4', buffer.getScreenCell(0, 4).getCharacter());
    }

    @Test
    void resizeTallerAddsEmptyRows() {
        buffer.writeText("abc");
        buffer.resize(10, 8);

        assertEquals(8, buffer.getHeight());
        assertEquals('a', buffer.getScreenCell(0, 0).getCharacter());
        assertEquals(' ', buffer.getScreenCell(5, 0).getCharacter());
    }

    @Test
    void resizeShorterPushesLinesToScrollback() {
        buffer.setCursorPosition(0, 0);
        buffer.writeText("first");
        buffer.setCursorPosition(1, 0);
        buffer.writeText("second");
        buffer.setCursorPosition(2, 0);
        buffer.writeText("third");
        buffer.setCursorPosition(3, 0);
        buffer.writeText("fourth");

        buffer.resize(10, 2);

        assertEquals(2, buffer.getHeight());
        // "first" and "second" should be in scrollback now
        String full = buffer.getFullContentAsString();
        assertTrue(full.contains("first"));
        assertTrue(full.contains("fourth"));
    }

    @Test
    void resizeClampsCursor() {
        buffer.setCursorPosition(3, 9);
        buffer.resize(5, 2);

        assertEquals(1, buffer.getCursorRow());
        assertEquals(4, buffer.getCursorColumn());
    }

    @Test
    void resizeIgnoresInvalidDimensions() {
        buffer.resize(0, 5);
        assertEquals(10, buffer.getWidth());

        buffer.resize(5, -1);
        assertEquals(4, buffer.getHeight());
    }

    @Test
    void resizeScrollbackLinesMatchNewWidth() {
        // push something into scrollback first
        buffer.setCursorPosition(0, 0);
        buffer.writeText("scroll");
        for (int i = 0; i < 4; i++) {
            buffer.insertEmptyLineAtBottom();
        }

        buffer.resize(5, 4);

        // scrollback line should now be 5 chars wide
        String line = buffer.getLineAsString(0);
        assertEquals(5, line.length());
        assertTrue(line.startsWith("scrol"));
    }

    @Test
    void writeWorksAfterResize() {
        buffer.resize(5, 3);
        buffer.setCursorPosition(0, 0);
        buffer.writeText("ab");

        assertEquals('a', buffer.getScreenCell(0, 0).getCharacter());
        assertEquals('b', buffer.getScreenCell(0, 1).getCharacter());
        assertEquals(2, buffer.getCursorColumn());
    }
}