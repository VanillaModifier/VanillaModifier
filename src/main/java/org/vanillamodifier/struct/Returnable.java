package org.vanillamodifier.struct;

public class Returnable {
    private boolean cancel = false;
    private Object returnValue;

    public void cancel() {
        cancel = true;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    @SuppressWarnings("unchecked")
    public <T> T getReturnValue() {
        return (T) returnValue;
    }
}
