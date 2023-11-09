package org.vanillamodifier.events.client;

import com.cubk.event.impl.Event;

public class TickEvent implements Event {
    private final boolean pre;

    public TickEvent(boolean isPre){
        this.pre = isPre;
    }

    public boolean isPre() {
        return pre;
    }

    public boolean isPost(){
        return !pre;
    }
}
