package basic;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import basic.BaseTest;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TestAkkaScheduler extends BaseTest {

    class Ticker extends AbstractActor {
        private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals("Tick", m -> {
                        log.info("Ticker received tick at "+new Date());
                    })
                    .build();
        }
    }

    @Test
    public void testScheduler() {
        executeTest(Duration.apply(1, TimeUnit.MINUTES), () -> {
            ActorRef ticker = system.actorOf(Props.create(Ticker.class, Ticker::new), "ticker");
            Cancellable cancellable = system.scheduler().schedule(Duration.apply(5, TimeUnit.SECONDS),
                    Duration.apply(10, TimeUnit.SECONDS), ticker, "Tick",
                    system.dispatcher(), ActorRef.noSender());

            hold(Duration.apply(45, TimeUnit.SECONDS));
            cancellable.cancel();
            system.log().info("cancelled the scheduled ticks");
            return true;
        });
    }
}
