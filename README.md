# Terminal Text Buffer

An implementation of a terminal text buffer in Java. This is the basic data structure that terminal emulators use to store and display text. It consists of a grid of character cells, each with its own color and style, a cursor, and a scrollback history.

## Building and running tests
```
./gradlew build
./gradlew test
```

Or just open the project in IntelliJ IDEA and run tests from there.

## How it works

The buffer is basically a grid of cells (rows x columns). Each cell holds a character, a foreground and background color, and style flags (bold, italic, underline). The grid is split into two parts:

* **Screen:** the visible area, e.g., 80x24. This is where text gets written and edited.
* **Scrollback:** lines that got pushed off the top of the screen. They're kept around so the user can scroll up and see them, but they can't be modified. There's a configurable max size, so it doesn't eat up memory forever.

When text is written, and the cursor goes past the last row, the top line gets moved into scrollback, and a new empty line appears at the bottom. This is how real terminals work, too.

## Design decisions and trade-offs

**Screen stored as `List<TerminalCell[]>`:** I used a list of arrays instead of a 2D array because of the need to add and remove lines when scrolling. With a 2D array, I would have to shift everything manually, but using a list, it's easy to just call remove(0) from the top and add() to the bottom. Each array within the list is still a fixed size, as the width doesn't change except when resizing.

**CellAttributes are copied, not shared:** Whenever attributes are assigned to a cell, a copy is created. This prevents a bug where changing the current attributes will alter characters that are already written. It uses a little more memory, but the alternative would be really confusing bugs.

**Wide character handling:** For CJK characters and other wide glyphs, 2 cells are used. The first cell contains the character, and the wideChar flag is set. The second cell is set as continuationOfWideChar. If a wide character cannot fit at the end of a line (only 1 cell available), the last cell is filled with a space, and the character is moved to the next line. This is similar to what most real terminal emulators do. Detection is done by Unicode range, which covers the most common cases but is not an exhaustive implementation of the Unicode East Asian Width property.

**Resize strategy:** When the screen gets shorter, the extra lines from the top go into scrollback rather than being deleted. This way, the user doesn't lose text they've already seen. When the screen gets taller, extra lines are filled in at the bottom. The screen may get wider or narrower, in which case the text is truncated from the right or padded with extra cells on the right. The scrollback lines are resized as well.

**Scrollback max size:** There's a cap on how many lines scrollback can hold. When it's exceeded, the oldest line gets dropped. Default is 1000, which is reasonable for most use cases.

## What I would improve

* **Unicode East Asian Width:** The current wide character detection mechanism, although covering most of the common CJK blocks, should, in an ideal situation, be based on the complete Unicode East Asian Width table. I kept it simple to avoid pulling in extra data or libraries.
* **Line wrapping metadata:** Right now, there's no way to tell if a line was wrapped by the buffer or if it was a real newline from the shell. Real terminal emulators track this for things like copy-paste and reflow on resize.
* **Resize reflow:** Currently, resize just truncates or pads the lines. A more intelligent method would be to reflow the wrapped lines when the width changes. This would add a lot of complexity, though, and it depends on having the wrapping metadata mentioned above.
* **insertText with wide characters:** The insert method shifts cells to the right, but it doesn't currently handle wide characters that may be split by the shifting operation. This may cause "orphan" continuation cells in some edge cases.
* **Performance:** For very large scrollback sizes, the O(n) nature of the List.remove(0) operation on an ArrayList becomes a problem. A LinkedList or a circular buffer would be more efficient here, but for typical terminal sizes, it's not really noticeable.