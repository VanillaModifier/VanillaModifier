package org.vanillamodifier.events.client;

import com.cubk.event.impl.Event;

public class KeyInputEvent implements Event {
    private final int keyCode;

    public KeyInputEvent(int keyCode){
        this.keyCode = keyCode;
    }

    public int getKeyCode() {
        return keyCode;
    }
}
