package basic;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.AllForOneStrategy;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import org.junit.Test;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class TestSupervisionStrategy extends BaseTest {
    @Test
    public void testSupervisionStrategy() {
        executeTest(Duration.apply(30, TimeUnit.SECONDS), () -> {
            FiniteDuration interval = Duration.apply(2, TimeUnit.SECONDS);
            ActorRef supervisor = system.actorOf(Props.create(Supervisor.class, Supervisor::new));
            hold(interval);
            ActorSelection child1 = system.actorSelection(supervisor.path().child("child1"));
            ActorSelection child2 = system.actorSelection(supervisor.path().child("child2"));
            ActorSelection child3 = system.actorSelection(supervisor.path().child("child3"));
            supervisor.tell("child1", probingActor);
            hold(interval);
            child1.tell("hello", probingActor);
            hold(interval);
            supervisor.tell("child2", probingActor);
            hold(Duration.apply(5, TimeUnit.SECONDS));
            child2.tell("hello", probingActor);
            hold(interval);
            supervisor.tell("child3", probingActor);
            hold(interval);
            child3.tell("hello", probingActor);
            return true;
        });
    }

    class Child1 extends AbstractActor {
        private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

        @Override
        public void preStart() throws Exception {
            log.info("starting child1");
            super.preStart();
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals("hello", msg -> log.info(String.format("%s says hello", getSelf().path())))
                    .matchEquals("start", msg -> {
                        throw new ArithmeticException();
                    })
                    .build();
        }


    }

    class Child2 extends AbstractActor {
        private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

        @Override
        public void preStart() throws Exception {
            log.info("starting child2");
            super.preStart();
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals("hello", msg -> log.info(String.format("%s says hello", getSelf().path())))
                    .matchEquals("start", msg -> {
                        throw new IllegalArgumentException();
                    })
                    .build();
        }

    }

    class Child3 extends AbstractActor {
        private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

        @Override
        public void preStart() throws Exception {
            log.info("starting child3");
            super.preStart();
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals("hello", msg -> log.info(String.format("%s says hello", getSelf().path())))
                    .matchEquals("start", msg -> {
                        throw new NullPointerException();
                    })
                    .build();
        }

    }

    class Supervisor extends AbstractActor {
        private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
        private ActorRef child1, child2, child3;
        private SupervisorStrategy oneForOneStrategy = new OneForOneStrategy(10,
                Duration.create("1 minute"), DeciderBuilder
                .match(ArithmeticException.class, e -> SupervisorStrategy.resume())
                .match(NullPointerException.class, e -> SupervisorStrategy.restart())
                .match(IllegalArgumentException.class, e -> SupervisorStrategy.stop())
                .matchAny(o -> SupervisorStrategy.escalate())
                .build());

        private SupervisorStrategy allForOneStrategy = new AllForOneStrategy(10,
                Duration.create("1 minute"), DeciderBuilder
                .match(ArithmeticException.class, e -> SupervisorStrategy.resume())
                .match(NullPointerException.class, e -> SupervisorStrategy.restart())
                .match(IllegalArgumentException.class, e -> SupervisorStrategy.stop())
                .matchAny(o -> SupervisorStrategy.escalate())
                .build());

        @Override
        public SupervisorStrategy supervisorStrategy() {
            return oneForOneStrategy;
        }

        @Override
        public void preStart() throws Exception {
            log.info("starting supervisor");
            child1 = getContext().actorOf(Props.create(Child1.class, Child1::new), "child1");
            child2 = getContext().actorOf(Props.create(Child2.class, Child2::new), "child2");
            child3 = getContext().actorOf(Props.create(Child3.class, Child3::new), "child3");
            super.preStart();
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals("child1", msg -> child1.tell("start", getSelf()))
                    .matchEquals("child2", msg -> child2.tell("start", getSelf()))
                    .matchEquals("child3", msg -> child3.tell("start", getSelf()))
                    .build();
        }
    }

}
