package com.terminalBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalBufferCursorTest {
    private TerminalBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new TerminalBuffer(80, 24);
    }

    @Test
    void cursorStartsAtOrigin() {
        assertEquals(0, buffer.getCursorRow());
        assertEquals(0, buffer.getCursorColumn());
    }

    @Test
    void setCursorToValidPosition() {
        buffer.setCursorPosition(10, 40);
        assertEquals(10, buffer.getCursorRow());
        assertEquals(40, buffer.getCursorColumn());
    }

    @Test
    void setCursorClampsNegativeValues() {
        buffer.setCursorPosition(-5, -10);
        assertEquals(0, buffer.getCursorRow());
        assertEquals(0, buffer.getCursorColumn());
    }

    @Test
    void setCursorClampsBeyondScreenBounds() {
        buffer.setCursorPosition(100, 200);
        assertEquals(23, buffer.getCursorRow());
        assertEquals(79, buffer.getCursorColumn());
    }

    @Test
    void moveCursorDownBasic() {
        buffer.moveCursorDown(5);
        assertEquals(5, buffer.getCursorRow());
    }

    @Test
    void moveCursorDownStopsAtBottom() {
        buffer.moveCursorDown(999);
        assertEquals(23, buffer.getCursorRow());
    }

    @Test
    void moveCursorUpFromMiddle() {
        buffer.setCursorPosition(10, 0);
        buffer.moveCursorUp(3);
        assertEquals(7, buffer.getCursorRow());
    }

    @Test
    void moveCursorUpStopsAtTop() {
        buffer.setCursorPosition(2, 0);
        buffer.moveCursorUp(50);
        assertEquals(0, buffer.getCursorRow());
    }

    @Test
    void moveCursorRightBasic() {
        buffer.moveCursorRight(10);
        assertEquals(10, buffer.getCursorColumn());
    }

    @Test
    void moveCursorRightStopsAtEdge() {
        buffer.moveCursorRight(9999);
        assertEquals(79, buffer.getCursorColumn());
    }

    @Test
    void moveCursorLeftFromMiddle() {
        buffer.setCursorPosition(0, 30);
        buffer.moveCursorLeft(10);
        assertEquals(20, buffer.getCursorColumn());
    }

    @Test
    void moveCursorLeftStopsAtZero() {
        buffer.moveCursorLeft(5);
        assertEquals(0, buffer.getCursorColumn());
    }

    @Test
    void combinedMovement() {
        buffer.moveCursorDown(10);
        buffer.moveCursorRight(20);
        buffer.moveCursorUp(3);
        buffer.moveCursorLeft(5);
        assertEquals(7, buffer.getCursorRow());
        assertEquals(15, buffer.getCursorColumn());
    }

}
