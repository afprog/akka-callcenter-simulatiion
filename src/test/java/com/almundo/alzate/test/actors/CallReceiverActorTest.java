package com.almundo.alzate.test.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.almundo.alzate.test.messages.Messages;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class CallReceiverActorTest {

    private ActorSystem system = ActorSystem.create("test");


    @Test
    public void shouldResponseCallFinishedMessageAfterSimulateCallSuccessfull() {
        TestKit probe = new TestKit(system);
        ActorRef operatorActor = system.actorOf(CallReceiverActor.props("Operator", 1));
        operatorActor.tell(new Messages.CallReceived(0), probe.getRef());
        Messages.CallFinished response = probe.expectMsgClass(new FiniteDuration(10,TimeUnit.SECONDS),Messages.CallFinished.class);
        assertEquals("Operator", response.getAttendedBy());
    }

}