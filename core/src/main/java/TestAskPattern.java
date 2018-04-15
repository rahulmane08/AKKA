import akka.actor.AbstractActor;
import akka.actor.Props;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestAskPattern extends BaseTest {

    @Test
    public void testAskPattern() throws Exception {
        String result = ask(Props.create(DelayedActor.class, () -> new DelayedActor()),
                "hi", 20 * 1000);
        assertTrue(result.contains("hello"));
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
