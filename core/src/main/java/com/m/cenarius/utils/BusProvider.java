package com.m.cenarius.utils;

import android.os.Bundle;

import de.greenrobot.event.EventBus;

public final class BusProvider {

    public static EventBus getInstance() {
        return EventBus.getDefault();
    }

    private BusProvider() {

    }

    public static class BusEvent {
        public int eventId;
        public Bundle data;

        public BusEvent(int eventId, Bundle data) {
            this.eventId = eventId;
            this.data = data;
        }
    }
}
