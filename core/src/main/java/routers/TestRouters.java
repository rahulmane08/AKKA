package routers;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.BalancingPool;
import akka.routing.BroadcastPool;
import akka.routing.ConsistentHashingPool;
import akka.routing.ConsistentHashingRouter;
import akka.routing.RandomPool;
import akka.routing.RoundRobinPool;
import akka.routing.SmallestMailboxPool;
import basic.BaseTest;
import org.junit.Test;

public class TestRouters extends BaseTest {

    @Test
    public void testRoundRobinRouter() throws InterruptedException {
        ActorRef roundRobinRouter = BaseTest.system.actorOf(new RoundRobinPool(5)
                .props(Props.create(Routee.class, () -> new Routee())), "round-robin-router");
        sendMessage(roundRobinRouter);
    }

    @Test
    public void testBroadCastRouter() throws InterruptedException {
        ActorRef broadcastRouter = BaseTest.system.actorOf(new BroadcastPool(5)
                .props(Props.create(Routee.class, () -> new Routee())), "broadcast-router");
        sendMessage(broadcastRouter);
    }

    @Test
    public void testConsistentHashingRouter() throws InterruptedException {
        ActorRef consistentHashRouter = BaseTest.system.actorOf(new ConsistentHashingPool(5)
                .withHashMapper(msg -> msg.hashCode())
                .props(Props.create(Routee.class, () -> new Routee())), "consistent-hash-router");

        for (int i=0; i<5; i++) {
            for (int j=0; j<5; j++){
                String message = "message"+j;
                consistentHashRouter.tell(message, probingActor);
                Thread.sleep(2*1000);
            }
        }
    }

    @Test
    public void testSmallestMailboxPoolRouter() throws InterruptedException {
        ActorRef smallestMailboxPoolRouter = BaseTest.system.actorOf(new SmallestMailboxPool(5)
                .props(Props.create(Routee.class, () -> new Routee())), "smallest-mb-pool-router");
        // load routee $a with 1000 messages
        for (int i=0; i<10000; i++) {
            system.actorSelection(smallestMailboxPoolRouter.path().child("$a")).tell("burstmessage", probingActor);
        }
        // this router will not choose $a and send the actual messages only to $b as its the next actor with smallest mb
        sendMessage(smallestMailboxPoolRouter);
    }

    @Test
    public void testRandomRouter() throws InterruptedException {
        ActorRef randomRouter = BaseTest.system.actorOf(new RandomPool(5)
                .props(Props.create(Routee.class, () -> new Routee())), "random-router");
        sendMessage(randomRouter);
    }

    private void sendMessage(ActorRef roundRobinRouter) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            roundRobinRouter.tell("message" + i, probingActor);
            Thread.sleep(2 * 1000);
        }
    }

    private class Routee extends AbstractActor {
        private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(String.class,
                            msg -> {
                                Thread.sleep(1000);
                                log.info(String.format("%s received message %s", getSelf().path(), msg));
                            })
                    .build();
        }
    }
}
