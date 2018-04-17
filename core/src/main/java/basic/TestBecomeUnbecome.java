package basic;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import org.junit.Test;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class TestBecomeUnbecome extends BaseTest {
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

    @Test
    public void testHotSwapOfBehaviour() throws InterruptedException {
        FiniteDuration probeWait = Duration.apply(5, TimeUnit.SECONDS);
        ActorRef hotswapActor = system.actorOf(
                Props.create(HotSwapActor.class, () -> new HotSwapActor()), "hot-swap-actor");
        Thread.sleep(2000);
        hotswapActor.tell("foo", probingActor); // actor becomes angry
        hotswapActor.tell("foo", probingActor); // actor remains angry
        system.log().info(probe.expectMsgClass(probeWait, String.class));
        hotswapActor.tell("bar", probingActor); // actor becomes angry
        hotswapActor.tell("bar", probingActor); // actor remains angry
        system.log().info(probe.expectMsgClass(probeWait, String.class));
        Thread.sleep(2000);
    }
}
