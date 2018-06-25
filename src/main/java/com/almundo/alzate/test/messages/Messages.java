package com.almundo.alzate.test.messages;

import akka.actor.ActorRef;

public class Messages {
    public static final class CallReceived {
        private int callId;

        public CallReceived(int callId) {
            this.callId = callId;
        }

        public int getCallId() {
            return callId;
        }
    }

    public static final class CallFinished {
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

    public static final class ChangeReference {
        private ActorRef reference;

        public ChangeReference(ActorRef reference) {
            this.reference = reference;
        }

        public ActorRef getReference() {
            return reference;
        }
    }
}
