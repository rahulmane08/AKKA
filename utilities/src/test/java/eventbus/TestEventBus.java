package eventbus;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import basic.BaseTest;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class TestEventBus extends BaseTest {

    @Test
    public void testEventBus() {
        executeTest(Duration.apply(10, TimeUnit.SECONDS), () -> {
            SampleEventBus eventBus = new SampleEventBus();
            ActorRef subscriber1 = system.actorOf(Props.create(Subscriber.class,
                    () -> new Subscriber("topic1", eventBus)), "subscriber1");
            ActorRef subscriber2 = system.actorOf(Props.create(Subscriber.class,
                    () -> new Subscriber("topic2", eventBus)), "subscriber2");
            hold(Duration.apply(2, TimeUnit.SECONDS));
            eventBus.publish(new Message("topic1", "Message to subscriber1"));
            eventBus.publish(new Message("topic2", "Message to subscriber2"));
            eventBus.publish(new Message("topic3", "Message to no subscriber"));

            return true;
        });
    }

    class Subscriber extends AbstractActor {
        private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

        public Subscriber(String topic, SampleEventBus eventBus) {
            //eventBus.subscribe(this, topic);
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(Message.class, msg -> log.info(String.format("%s received the message [%s] from bus")
                            , getSelf().path().name(), msg.payload))
                    .build();
        }
    }
}
