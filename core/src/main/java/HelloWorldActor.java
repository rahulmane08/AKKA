import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.IOException;
import java.util.Arrays;

class Greeter extends AbstractActor {

    private final ActorRef printerActor;

    private Greeter(ActorRef printerActor) {
        this.printerActor = printerActor;
    }

    static public Props newInstance(ActorRef printerActor) {
        return Props.create(Greeter.class, () -> new Greeter(printerActor));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    printerActor.tell(message, getSelf());
                })
                .build();
    }
}

class Printer extends AbstractActor {

    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    static public Props newInstance() {
        return Props.create(Printer.class, () -> new Printer());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("greet", s -> log.info("Special greeting by Greeter")) //matches for specific message
                .matchEquals("bye", s -> getContext().stop(getSelf())) //greeter stops itself
                .match(String.class, message -> {
                    log.info(String.format("Message from %s : %s", getSender(), message));
                })
                .build();
    }
}

public class HelloWorldActor {
    public static void main(String[] args) throws IOException {
        final ActorSystem system = ActorSystem.create("helloakka");
        final ActorRef printer = system.actorOf(Printer.newInstance(), "printer");
        final ActorRef greeter = system.actorOf(Greeter.newInstance(printer), "greeter");
        //greet lands in deadLetter
        Arrays.asList("hello", "hi", "bye", "greet").forEach(msg -> greeter.tell(msg, ActorRef.noSender()));
        try {
            System.in.read();
        } finally {
            system.terminate();
        }
    }
}
