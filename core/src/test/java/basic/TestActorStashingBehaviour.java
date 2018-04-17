package basic;

import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.junit.Test;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class TestActorStashingBehaviour extends BaseTest{
    class StashingActor extends AbstractActorWithStash {
        private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
        private Receive stashMode = receiveBuilder()
                .matchEquals("read", cs -> unstashAll())
                .matchEquals("endStashMode", msg -> getContext().unbecome())
                .matchAny(msg -> log.info("Message in stash mode = "+msg))
                .build();

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals("startStashMode", s -> getContext().become(stashMode))
                    .matchEquals("stashMessage",msg -> stash())
                    .match(String.class, msg -> log.info("Stashing actor working in normal mode for msg = "+msg))
                    .build();
        }
    }

    @Test
    public void testStashMode() {
        FiniteDuration probeWait = Duration.apply(10, TimeUnit.SECONDS);
        FiniteDuration interval = Duration.apply(2, TimeUnit.SECONDS);
        executeTest(probeWait, () -> {
            ActorRef stashingActor = system.actorOf(
                    Props.create(StashingActor.class, () -> new StashingActor()), "stashing-actor");
            testKitProbe.expectNoMessage(interval);
            stashingActor.tell("hello", probingActor); // normal mode
            for (int i=0; i<10; i++)
                stashingActor.tell("stashMessage", probingActor); //messages landing into stash
            stashingActor.tell("startStashMode", probingActor); // enable stash-mode
            stashingActor.tell("read", probingActor); // unstash the messages
            testKitProbe.expectNoMessage(interval);
            stashingActor.tell("endStashMode", probingActor); // end stash mode
            stashingActor.tell("bye bye", probingActor); // normal mode
            testKitProbe.expectNoMessage();
            return true;
        });
    }
}
