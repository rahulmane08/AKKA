package basic;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class TestPriorityMailbox extends BaseTestWithConfiguration {

    @Test
    public void testPrioMailbox() {
        executeTest(Duration.apply(1, TimeUnit.MINUTES), () -> {
            ActorRef testActor = system.actorOf(Props.create(AbstractActor.class, () -> new AbstractActor() {
                @Override
                public Receive createReceive() {
                    return receiveBuilder()
                            .match(String.class, msg -> {
                                Thread.sleep(2 * 1000);
                                system.log().info(String.format("processing msg: %s", msg));
                            })
                            .build();
                }
            }).withMailbox("prio-mailbox"), "test-actor");
            ActorRef deathWatcher = system.actorOf(
                    Props.create(DeathWatcherActor.class, () -> new DeathWatcherActor(testActor)), "death-watcher");

            for (int i = 0; i < 5; i++) {
                testActor.tell("lowpriority" + i, ActorRef.noSender());
            }

            testActor.tell(PoisonPill.getInstance(), ActorRef.noSender());

            for (int i = 0; i < 5; i++) {
                testActor.tell("highpriority" + i, ActorRef.noSender());
            }
            return true;
        });

    }
}
