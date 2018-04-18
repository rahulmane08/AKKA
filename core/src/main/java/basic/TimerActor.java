package basic;

import akka.actor.AbstractActorWithTimers;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.FiniteDuration;

public class TimerActor extends AbstractActorWithTimers {
    private final String TICK_KEY = "TickKey";
    private final FiniteDuration repeatDuration;
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public TimerActor(FiniteDuration repeatDuration) {
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
