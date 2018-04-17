package basic;

import akka.actor.AbstractActorWithTimers;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.FiniteDuration;

import java.time.Duration;

public class TimeActor extends AbstractActorWithTimers {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final String TICK_KEY = "TickKey";
    private final FiniteDuration repeatDuration;

    public TimeActor(FiniteDuration repeatDuration) {
        this.repeatDuration = repeatDuration;
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("start", message -> {
                    // do something useful here
                    getTimers().startPeriodicTimer(TICK_KEY, "repeat", repeatDuration);
                })
                .matchEquals("repeat", message -> log.info("actor repeating task"))
                .build();
    }
}
