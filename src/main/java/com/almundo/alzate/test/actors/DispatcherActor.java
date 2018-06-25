package com.almundo.alzate.test.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.almundo.alzate.test.messages.Messages;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DispatcherActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private int workers;
    private String workersType;
    private Optional<ActorRef> nextDispatcher;
    private ActorRef responseActor;

    private Queue<ActorRef>  workersQueue = new ConcurrentLinkedQueue<>();

    public DispatcherActor(int workers, String workersType, Optional<ActorRef> nextDispatcher) {
        this.workers = workers;
        this.workersType = workersType;
        this.nextDispatcher = nextDispatcher;
        for(int i=0; i< workers ; i++){
            ActorRef worker = getContext().getSystem().actorOf(CallReceiverActor.props(workersType, i));
            workersQueue.add(worker);
        }
        log.info("Workers size = {}", workersQueue.size());
    }

    public static Props props(int workers, String workersType, Optional<ActorRef> nextDispatcher) {
        return Props.create(DispatcherActor.class, workers, workersType, nextDispatcher);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Messages.CallReceived.class, r ->  {
                    responseActor = getSender();
                    if(workersQueue.size() > 0){
                        workersQueue.poll().tell(r,getSelf());
                    }else{
                        if(nextDispatcher.isPresent()){
                            nextDispatcher.get().forward(r,getContext());
                        }else{
                            throw new Exception("All Operators are busy");
                        }
                    }
                })
                .match(Messages.CallFinished.class, r ->  {
                    log.info("CallFinished after {} by {}",r.getDelay(),r.getAttendedBy());
                    workersQueue.add(getSender());
                    responseActor.tell(r,getSelf());
                })
                .match(Messages.ChangeReference.class, r ->  {
                    log.info("Change reference ");
                    nextDispatcher = Optional.of(r.getReference());
                })
                .build();
    }
}
