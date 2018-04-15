package sample;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;

public class SampleActor extends AbstractActor {
    private final String name;
    private final SupervisorStrategy supervisorStrategy;
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ReceiveBuilder receiveBuilder;
    private int children = 0;

    private SampleActor(String name, int children) {
        this(name, null, children);
    }

    private SampleActor(String name, SupervisorStrategy supervisorStrategy, int children) {
        this.name = name;
        this.supervisorStrategy = supervisorStrategy;
        this.children = children;
    }

    static public Props newInstance(String name, int children) {
        return Props.create(SampleActor.class, () -> new SampleActor(name, children));
    }

    static public Props newInstance(String name, SupervisorStrategy supervisorStrategy, int children) {
        return Props.create(SampleActor.class, () -> new SampleActor(name, supervisorStrategy, children));
    }

    @Override
    public void preStart() throws Exception {
        log.info(String.format("%s actor starting", getSelf().path().name()));
        if (children > 0)
            for (int i = 0; i < children; i++)
                getContext().actorOf(newInstance("child" + i, supervisorStrategy, 0));
    }

    @Override
    public void postStop() throws Exception {
        log.info(String.format("%s actor stopped", getSelf().path().name()));
    }

    public ReceiveBuilder getReceiveBuilder() {
        return receiveBuilder;
    }

    public SampleActor setReceiveBuilder(ReceiveBuilder receiveBuilder) {
        this.receiveBuilder = receiveBuilder;
        return this;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder != null ? null : receiveBuilder.build();
    }
}
