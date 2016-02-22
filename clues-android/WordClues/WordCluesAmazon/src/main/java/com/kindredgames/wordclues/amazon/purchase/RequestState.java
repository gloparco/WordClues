package com.kindredgames.wordclues.amazon.purchase;

/**
 * Represents the state of a request which goes from SENT, RECEIVED to
 * FULFILLED.
 */
public enum RequestState {

    NOT_AVAILABLE(0), //
    SENT(1), //
    RECEIVED(2), //
    FULFILLED(3);

    private int state;

    private RequestState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    /**
     * Gets RequestState enum by int state.
     */
    public static RequestState valueOf(int state) {
        for (RequestState requestState : values()) {
            if (requestState.getState() == state) {
                return requestState;
            }
        }
        return null;
    }
}
