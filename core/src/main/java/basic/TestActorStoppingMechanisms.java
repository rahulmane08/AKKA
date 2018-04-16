package basic;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Kill;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class TestActorStoppingMechanisms extends BaseTest {

    @Test
    public void testActorStopping() throws InterruptedException {
        ActorRef actor1 = system.actorOf(Props.create(TestActor.class, () -> new TestActor()), "actor1");
        ActorRef actor2 = system.actorOf(Props.create(TestActor.class, () -> new TestActor()), "actor2");
        ActorRef actor3 = system.actorOf(Props.create(TestActor.class, () -> new TestActor()), "actor3");
        ActorRef actor4 = system.actorOf(Props.create(TestActor.class, () -> new TestActor()), "actor4");
        ActorRef deatchWatcher = system.actorOf(
                Props.create(DeathWatcherActor.class, () -> new DeathWatcherActor(actor1, actor2, actor3, actor4)),
                "death-watcher");
        Thread.sleep(5 * 1000);
        actor1.tell("hi", ActorRef.noSender());
        Thread.sleep(5 * 1000);
        actor1.tell("kill", ActorRef.noSender());
        Thread.sleep(5 * 1000);
        actor2.tell(PoisonPill.getInstance(), ActorRef.noSender());
        Thread.sleep(5 * 1000);
        actor3.tell(Kill.getInstance(), ActorRef.noSender());
        Thread.sleep(5 * 1000);
        Patterns.gracefulStop(actor4, Duration.apply(5, TimeUnit.SECONDS)).onComplete(val -> {
            system.log().info("actor4 stopped: " + val.get());
            return val.get();
        }, system.dispatcher());


        Thread.sleep(6 * 1000);
    }

    class TestActor extends AbstractActor {
        private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals("kill", msg -> {
                        log.info(getSelf().path().name() + " killing itself");
                        getContext().stop(getSelf());
                    })
                    .match(String.class, msg -> log.info(getSelf().path().name() + " got the message " + msg))
                    .build();
        }
    }

}
