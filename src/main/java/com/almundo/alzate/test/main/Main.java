package com.almundo.alzate.test.main;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.almundo.alzate.test.actors.DispatcherActor;
import com.almundo.alzate.test.messages.Messages;
import akka.http.javadsl.marshallers.jackson.Jackson;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static akka.pattern.PatternsCS.ask;

public class Main  extends AllDirectives {
    private final ActorRef operatorDispatcherActor;
    private final int workers = 100;

    public Main(final ActorSystem system) {
        ActorRef directorDispatcherActor = system.actorOf(DispatcherActor.props(workers, "Director", Optional.empty()));
        ActorRef supervisorDispatcherActor = system.actorOf(DispatcherActor.props(workers, "Supervisor", Optional.of(directorDispatcherActor)));
        operatorDispatcherActor = system.actorOf(DispatcherActor.props(workers, "Operator", Optional.of(supervisorDispatcherActor)));
        directorDispatcherActor.tell(new Messages.ChangeReference(operatorDispatcherActor),directorDispatcherActor);
    }

    public static void main(String ... args) throws IOException {

        ActorSystem system = ActorSystem.create("routes");

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        Main app = new Main(system);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost("localhost", 8080), materializer);

        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read();

        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());
    }

    private Route createRoute() {
        return route(
                path("call", () ->
                        post(() ->{
                                    CompletionStage<Messages.CallFinished> bids = ask(operatorDispatcherActor, new Messages.CallReceived(0), 10000)
                                            .thenApply((Messages.CallFinished.class::cast));
                                    return completeOKWithFuture(bids,Jackson.marshaller());
                                })));
    }
}
