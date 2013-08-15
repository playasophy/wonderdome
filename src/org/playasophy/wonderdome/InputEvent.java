package org.playasophy.wonderdome;

public abstract class InputEvent {

    // TODO: Possibly use a richer type than string? For now just require string to be a unique ID.
    private String source;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

}
