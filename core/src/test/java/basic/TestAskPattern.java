package basic;

import akka.actor.AbstractActor;
import akka.actor.Props;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class TestAskPattern extends BaseTest {

    @Test
    public void testAskPattern() throws Exception {
        executeTest(Duration.apply(20,TimeUnit.SECONDS), () -> {
            String result = null;
            try {
                result = ask(Props.create(DelayedActor.class, () -> new DelayedActor()),
                        "hi", 2);
                assertTrue(result.contains("hello"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        });
    }

    class DelayedActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals("hi", msg -> {
                        Thread.sleep(10 * 1000);
                        system.log().info(" received the message");
                        getSender().tell("hello " + getSender().path().name(), getSelf());
                    })
                    .build();
        }
    }
}
