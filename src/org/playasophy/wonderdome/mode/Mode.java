package org.playasophy.wonderdome.mode;

import org.playasophy.wonderdome.input.InputEvent;

public interface Mode {

    void update(int[][] pixels, long dtMillis);

    void handleEvent(InputEvent event);

}
