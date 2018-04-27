import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.CircuitBreaker;

import java.time.Duration;

public class ActorWithCircuitBreaker extends AbstractActor {
    private final CircuitBreaker circuitBreaker;
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public ActorWithCircuitBreaker() {
        this.circuitBreaker = CircuitBreaker
                .create(getContext().system().scheduler(),
                        5,
                        Duration.ofSeconds(10),
                        Duration.ofMinutes(1))
                .addOnOpenListener(() -> log.error("circuit breaker is in OPEN state"))
                .addOnCloseListener(() -> log.info("circuit breaker is in CLOSED state"))
                .addOnHalfOpenListener(() -> log.info("circuit breaker is in HALF OPEN state"));
    }

    @Override
    public Receive createReceive() {
        return null;
    }
}
