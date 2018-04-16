package basic;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.ArrayList;
import java.util.List;

public class DeathWatcherActor extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private List<ActorRef> watchees = new ArrayList<>();

    public DeathWatcherActor(ActorRef... list) {
        for (ActorRef watchee : list) {
            watchees.add(watchee);
            getContext().watch(watchee);
        }

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActorRef.class, watchee -> log.info(String.format("%s added %s on its death watch list",
                        getSelf().path().name(),
                        watchee.path().name())))
                .match(Terminated.class,
                        terminated -> log.info(String.format("death notification %s", terminated)))
                .build();
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        for (ActorRef watchee : watchees)
            getContext().unwatch(watchee);
    }
}
