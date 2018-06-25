package com.almundo.alzate.test.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.almundo.alzate.test.messages.Messages;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class DispatcherActorTest {

    private ActorSystem system = ActorSystem.create("test");

    @Test
    public void shouldResponseAfterSimulateCallSuccessfull() {
        TestKit probe = new TestKit(system);
        ActorRef operatorDispatcherActor = system.actorOf(DispatcherActor.props(1, "Operator", Optional.empty()));
        operatorDispatcherActor.tell(new Messages.CallReceived(0), probe.getRef());
        Messages.CallFinished response = probe.expectMsgClass(new FiniteDuration(10, TimeUnit.SECONDS), Messages.CallFinished.class);
        assertEquals("Operator 0", response.getAttendedBy());
    }

    @Test
    public void shouldResponse10MessagesFor10MEssagesSentConcurrently() {
        int numberOfMessages = 10;
        List<Messages.CallReceived> messages = new ArrayList<>();
        for (int i = 0; i < numberOfMessages; i++) {
            messages.add(new Messages.CallReceived(i));
        }
        TestKit probe = new TestKit(system);
        ActorRef directorDispatcherActor = system.actorOf(DispatcherActor.props(5, "Director", Optional.empty()));
        ActorRef supervisorDispatcherActor = system.actorOf(DispatcherActor.props(5, "Supervisor", Optional.of(directorDispatcherActor)));
        ActorRef operatorDispatcherActor = system.actorOf(DispatcherActor.props(5, "Operator", Optional.of(supervisorDispatcherActor)));
        messages.parallelStream().forEach(message -> operatorDispatcherActor.tell(message, probe.getRef()));
        List<Object> responses = probe.receiveN(numberOfMessages, new FiniteDuration(10, TimeUnit.SECONDS));
        assertEquals(responses.size(), numberOfMessages);
    }

    @Test
    public void shouldResponseFor25MEssagesSentConcurrently() {
        int numberOfMessages = 25;
        List<Messages.CallReceived> messages = new ArrayList<>();
        for (int i = 0; i < numberOfMessages; i++) {
            messages.add(new Messages.CallReceived(i));
        }
        TestKit probe = new TestKit(system);
        ActorRef directorDispatcherActor = system.actorOf(DispatcherActor.props(5, "Director", Optional.empty()));
        ActorRef supervisorDispatcherActor = system.actorOf(DispatcherActor.props(5, "Supervisor", Optional.of(directorDispatcherActor)));
        ActorRef operatorDispatcherActor = system.actorOf(DispatcherActor.props(5, "Operator", Optional.of(supervisorDispatcherActor)));
        directorDispatcherActor.tell(new Messages.ChangeReference(operatorDispatcherActor), probe.getRef());
        messages.parallelStream().forEach(message -> operatorDispatcherActor.tell(message, probe.getRef()));
        List<Object> responses = probe.receiveN(numberOfMessages, new FiniteDuration(20, TimeUnit.SECONDS));
        assertEquals(responses.size(), numberOfMessages);
    }

    @Test
    public void shouldResponseFor1000MEssagesSentConcurrently() {
        int numberOfMessages = 1000;
        int workers = 100;
        List<Messages.CallReceived> messages = new ArrayList<>();
        for (int i = 0; i < numberOfMessages; i++) {
            messages.add(new Messages.CallReceived(i));
        }
        ActorRef directorDispatcherActor = system.actorOf(DispatcherActor.props(workers, "Director", Optional.empty()));
        TestKit probe = new TestKit(system);
        ActorRef supervisorDispatcherActor = system.actorOf(DispatcherActor.props(workers, "Supervisor", Optional.of(directorDispatcherActor)));
        ActorRef operatorDispatcherActor = system.actorOf(DispatcherActor.props(workers, "Operator", Optional.of(supervisorDispatcherActor)));
        directorDispatcherActor.tell(new Messages.ChangeReference(operatorDispatcherActor), probe.getRef());
        messages.parallelStream().forEach(message -> operatorDispatcherActor.tell(message, probe.getRef()));
        List<Object> responses = probe.receiveN(numberOfMessages, new FiniteDuration(40, TimeUnit.SECONDS));
        assertEquals(responses.size(), numberOfMessages);
    }
}