package basic;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.RecoveryCompleted;
import akka.persistence.SnapshotOffer;

import java.io.Serializable;
import java.util.ArrayList;

class Cmd implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String data;

    public Cmd(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}

class Evt implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String data;

    public Evt(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}

class State implements Serializable {
    private static final long serialVersionUID = 1L;
    private final ArrayList<String> events;

    public State() {
        this(new ArrayList<>());
    }

    public State(ArrayList<String> events) {
        this.events = events;
    }

    public State copy() {
        return new State(new ArrayList<>(events));
    }

    public void update(Evt evt) {
        events.add(evt.getData());
    }

    public int size() {
        return events.size();
    }

    @Override
    public String toString() {
        return events.toString();
    }
}

public class PersistentActor extends AbstractPersistentActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private State state = new State();
    private int snapshotInterval = 1000;

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(Evt.class, state::update) // sequence of events are replayed on the state.
                .match(SnapshotOffer.class, ss -> state = (State) ss.snapshot()) // state is reinitialized after actor recovery
                .match(RecoveryCompleted.class, r -> log.info("RECOVERY COMPLETED"))
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Cmd.class, c -> {
                    final String data = c.getData(); // get the data from command
                    final Evt evt = new Evt(data + "-" + getNumEvents()); // create the event
                    persist(evt, (Evt e) -> {
                        state.update(e);
                        getContext().getSystem().eventStream().publish(e);
                        if (lastSequenceNr() % snapshotInterval == 0 && lastSequenceNr() != 0)
                            // IMPORTANT: create a copy of snapshot because ExampleState is mutable
                            saveSnapshot(state.copy());
                    });
                })
                .matchEquals("print", s -> log.info(String.valueOf(state)))
                .build();
    }

    @Override
    public String persistenceId() {
        // A persistent actor must have an identifier that doesn’t change across different actor incarnations.
        // The identifier must be defined with the persistenceId method.
        return "sample-id-1";
    }

    public int getNumEvents() {
        return state.size();
    }
}
