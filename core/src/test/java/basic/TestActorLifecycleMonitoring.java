package basic;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class TestActorLifecycleMonitoring extends BaseTest {
    public static void main(String[] args) throws InterruptedException {
        final ActorSystem system = ActorSystem.create("helloakka");
        TestActorLifecycleMonitoring test = new TestActorLifecycleMonitoring();
        ActorRef watchee = system.actorOf(Props.create(WatchedActor.class, () -> test.new WatchedActor()), "watchee");
        system.actorOf(Props.create(Watcher.class, () -> test.new Watcher(watchee)), "watcher");
        watchee.tell("hello", ActorRef.noSender());
        Thread.sleep(10000);
        watchee.tell("bye", ActorRef.noSender());
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            system.terminate();
        }
    }

    @Test
    public void testMonitorActorsLifecycle() {
        executeTest(scala.concurrent.duration.Duration.apply(10, TimeUnit.SECONDS), () -> {
            FiniteDuration interval = scala.concurrent.duration.Duration.apply(2, TimeUnit.SECONDS);
            ActorRef watchee = system.actorOf(Props.create(WatchedActor.class, () -> new WatchedActor()), "watchee");
            ActorRef deathWatcherActor = system.actorOf(
                    Props.create(DeathWatcherActor.class, () -> new DeathWatcherActor(watchee)), "death-watcher");
            deathWatcherActor.tell(watchee, probingActor);
            hold(interval);
            watchee.tell("hello", ActorRef.noSender());
            hold(interval);
            watchee.tell("bye", ActorRef.noSender());
            hold(interval);
            return true;
        });
    }

    private class WatchedActor extends AbstractActor {
        private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

        @Override
        public void preStart() throws Exception {
            super.preStart();
            log.info("WatchedActor starting");
        }

        @Override
        public void postStop() throws Exception {
            super.postStop();
            log.info("WatchedActor stopping");
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals("bye", msg -> getContext().stop(getSelf()))
                    .match(String.class, msg -> log.info("received msg " + msg))
                    .build();
        }
    }

    private class Watcher extends AbstractActor {
        private final ActorRef watchee;
        private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

        public Watcher(ActorRef watchee) {
            this.watchee = watchee;
        }

        @Override
        public void preStart() throws Exception {
            super.preStart();
            getContext().watch(watchee);
        }

        @Override
        public void postStop() throws Exception {
            super.postStop();
            getContext().unwatch(watchee);
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(Object.class, msg -> log.info(String.format("%s got death notification : %s", getSelf(), String.valueOf(msg))))
                    .build();
        }
    }
}
