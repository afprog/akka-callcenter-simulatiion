package com.almundo.alzate.test.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.almundo.alzate.test.messages.Messages;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class CallReceiverActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final String employeeType;
    private int id;

    public CallReceiverActor(String employeeType, int id) {
        this.employeeType = employeeType;
        this.id = id;
    }

    public static Props props(String employeeType, int id) {
        return Props.create(CallReceiverActor.class, employeeType, id);
    }

    private void simulateCall(ActorRef sender){
        ActorSystem system = getContext().getSystem();
        long delay = ThreadLocalRandom.current().nextLong(2000, 10000);
        system.scheduler().scheduleOnce(Duration.ofMillis(delay),
                getSelf(), new Messages.CallFinished(employeeType,delay), system.dispatcher(), sender);
    }



    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Messages.CallReceived.class, r ->  simulateCall(getSender()))
                .match(Messages.CallFinished.class, r ->  {
                    System.out.println("CallFinished after " + r.getDelay());
                    getSender().tell(r, getSelf());
                })
                .build();
    }
}
