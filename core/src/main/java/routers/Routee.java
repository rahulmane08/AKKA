package routers;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ConsistentHashingRouter;

class Routee extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class,
                        msg -> {
                            Thread.sleep(1000);
                            log.info(String.format("%s received message %s", getSelf().path(), msg));
                        })
                .match(HashableMessage.class,
                        msg -> {
                            Thread.sleep(1000);
                            log.info(String.format("%s received message %s", getSelf().path(), msg.getMessage()));
                        })
                .build();
    }
}
