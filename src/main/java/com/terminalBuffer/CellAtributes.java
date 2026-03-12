package com.terminalBuffer;

public class CellAtributes {
    private TerminalColor foreground;
    private TerminalColor background;
    private boolean bold;
    private boolean italic;
    private boolean underline;

    public CellAtributes() {
        this.foreground = TerminalColor.DEFAULT;
        this.background = TerminalColor.DEFAULT;
        this.bold = false;
        this.italic = false;
        this.underline = false;
    }

    public CellAtributes(TerminalColor foreground, TerminalColor background, boolean bold, boolean italic, boolean underline) {
        this.foreground = foreground;
        this.background = background;
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
    }

    public CellAtributes copy(){
        return new CellAtributes(foreground, background, bold, italic, underline);
    }

    public TerminalColor getForeground() {
        return foreground;
    }

    public void setForeground(TerminalColor foreground) {
        this.foreground = foreground;
    }

    public TerminalColor getBackground() {
        return background;
    }

    public void setBackground(TerminalColor background) {
        this.background = background;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public boolean isUnderline() {
        return underline;
    }

    public void setUnderline(boolean underline) {
        this.underline = underline;
    }
}
