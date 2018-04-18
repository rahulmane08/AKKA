package basic;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import org.junit.Test;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class TestBecomeUnbecome extends BaseTest {
    @Test
    public void testHotSwapOfBehaviour() {
        FiniteDuration probeWait = Duration.apply(5, TimeUnit.SECONDS);
        executeTest(probeWait, () -> {
            ActorRef hotswapActor = system.actorOf(
                    Props.create(HotSwapActor.class, HotSwapActor::new), "hot-swap-actor");
            hold(Duration.apply(2, TimeUnit.SECONDS));
            hotswapActor.tell("foo", probingActor); // actor becomes angry
            hotswapActor.tell("foo", probingActor); // actor remains angry
            system.log().info(testKitProbe.expectMsgClass(probeWait, String.class));
            hotswapActor.tell("bar", probingActor); // actor becomes angry
            hotswapActor.tell("bar", probingActor); // actor remains angry
            system.log().info(testKitProbe.expectMsgClass(probeWait, String.class));
            return true;
        });
    }

    class HotSwapActor extends AbstractActor {
        private AbstractActor.Receive angry;
        private AbstractActor.Receive happy;

        public HotSwapActor() {
            angry =
                    receiveBuilder()
                            .matchEquals("foo", s -> {
                                getSender().tell("I am already angry?", getSelf());
                            })
                            .matchEquals("bar", s -> {
                                getContext().become(happy);
                            })
                            .build();

            happy = receiveBuilder()
                    .matchEquals("bar", s -> {
                        getSender().tell("I am already happy :-)", getSelf());
                    })
                    .matchEquals("foo", s -> {
                        getContext().become(angry);
                    })
                    .build();
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals("foo", s ->
                            getContext().become(angry)
                    )
                    .matchEquals("bar", s ->
                            getContext().become(happy)
                    )
                    .build();
        }
    }
}
