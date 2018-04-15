import akka.actor.AbstractActor;
import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class TestActorSelection extends BaseTest {
    @Test
    public void testActorSelection() {
        probe.within(Duration.apply(10, TimeUnit.SECONDS), () -> {
            final ActorRef parent = system.actorOf(
                    Props.create(ParentActor.class, () -> new ParentActor()), "parent");
            ActorPath parentPath = parent.path();
            system.actorSelection(parentPath.child("child1")).tell("hi", ActorRef.noSender());
            system.actorSelection(parentPath.child("child1")).tell("greet_brothers", ActorRef.noSender());
            system.actorSelection(parentPath.child("child1")).tell("hi", ActorRef.noSender());
            probe.expectNoMsg();
            return null;
        });
    }

    class ParentActor extends AbstractActor {

        private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

        @Override
        public void preStart() throws Exception {
            super.preStart();
            for (int i = 0; i < 3; i++) {
                String childName = "child" + i;
                getContext().actorOf(Props.create(ChildActor.class, () -> new ChildActor(childName)), childName);
            }
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals("end", msg -> getContext().stop(getSelf()))
                    .build();
        }
    }

    class ChildActor extends AbstractActor {
        private final String name;
        private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

        public ChildActor(String name) {
            this.name = name;
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals("hi", msg -> log.info(String.format("%s got msg %s from %s", name, msg,
                            getSender().path().name())))
                    .matchEquals("greet_brothers", msg ->
                            getContext().actorSelection("../*").tell("hi", getSelf()))
                    .build();
        }
    }
}
