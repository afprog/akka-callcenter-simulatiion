package com.almundo.alzate.test.messages;

public class Messages {
    public static final class CallReceived{

    }

    public static final class CallFinished{
        private String attendedBy;
        private long delay;

        public CallFinished(String attendetBy, long delay) {
            this.attendedBy = attendetBy;
            this.delay = delay;
        }

        public String getAttendedBy() {
            return attendedBy;
        }

        public long getDelay() {
            return delay;
        }
    }
}
