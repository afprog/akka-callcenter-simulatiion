package com.almundo.alzate.test.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.almundo.alzate.test.messages.Messages;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class DispatcherActorTest {

    private ActorSystem system = ActorSystem.create("test");

    @Test
    public void testReplyWithEmptyReadingIfNoTemperatureIsKnown() {
        TestKit probe = new TestKit(system);
        ActorRef operatorDispatcherActor = system.actorOf(DispatcherActor.props(1, "Operator", null));
        operatorDispatcherActor.tell(new Messages.CallReceived(), probe.getRef());
        Messages.CallFinished response = probe.expectMsgClass(new FiniteDuration(10,TimeUnit.SECONDS),Messages.CallFinished.class);
        assertEquals("Operator", response.getAttendedBy());

    }

}