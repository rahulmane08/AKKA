package routers;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.BalancingPool;
import akka.routing.BroadcastPool;
import akka.routing.ConsistentHashingPool;
import akka.routing.RandomPool;
import akka.routing.RoundRobinPool;
import akka.routing.SmallestMailboxPool;
import basic.BaseTest;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class TestRouters extends BaseTest {

    @Test
    public void testRoundRobinRouter() throws InterruptedException {
        executeTest(Duration.apply(20, TimeUnit.SECONDS), () -> {
            ActorRef roundRobinRouter = BaseTest.system.actorOf(new RoundRobinPool(5)
                    .props(Props.create(Routee.class, () -> new Routee())), "round-robin-router");
            sendMessage(roundRobinRouter);
            return true;
        });
    }

    @Test
    public void testBroadCastRouter() throws InterruptedException {
        executeTest(Duration.apply(20, TimeUnit.SECONDS), () -> {
            ActorRef broadcastRouter = BaseTest.system.actorOf(new BroadcastPool(5)
                    .props(Props.create(Routee.class, () -> new Routee())), "broadcast-router");
            sendMessage(broadcastRouter);
            return true;
        });
    }

    @Test
    public void testConsistentHashingRouter() throws InterruptedException {
        executeTest(Duration.apply(20, TimeUnit.SECONDS), () -> {
            ActorRef consistentHashRouter = BaseTest.system.actorOf(new ConsistentHashingPool(5)
                    .withHashMapper(msg -> msg.hashCode())
                    .props(Props.create(Routee.class, () -> new Routee())), "consistent-hash-router");

            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    String message = "message" + j;
                    consistentHashRouter.tell(message, probingActor);
                    try {
                        Thread.sleep(2 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return true;
        });
    }

    @Test
    public void testSmallestMailboxPoolRouter() throws InterruptedException {
        executeTest(Duration.apply(20, TimeUnit.SECONDS), () -> {
            ActorRef smallestMailboxPoolRouter = BaseTest.system.actorOf(new SmallestMailboxPool(5)
                    .props(Props.create(Routee.class, () -> new Routee())), "smallest-mb-pool-router");
            // load routee $a with 1000 messages
            for (int i = 0; i < 10000; i++) {
                system.actorSelection(smallestMailboxPoolRouter.path().child("$a")).tell("burstmessage", probingActor);
            }
            // this router will not choose $a and send the actual messages only to $b as its the next actor with smallest mb
            sendMessage(smallestMailboxPoolRouter);
            return true;
        });
    }

    @Test
    public void testBalancingPoolRouter() throws InterruptedException {
        executeTest(Duration.apply(20, TimeUnit.SECONDS), () -> {
            ActorRef balancingPool = BaseTest.system.actorOf(new BalancingPool(5)
                    .props(Props.create(Routee.class, () -> new Routee())), "balancing-pool-router");
            // load routee $a with 1000 messages
            for (int i = 0; i < 10000; i++) {
                system.actorSelection(balancingPool.path().child("$a")).tell("burstmessage", probingActor);
            }
            // this router will not choose $a and send the actual messages only to $b as its the next actor with smallest mb
            sendMessage(balancingPool);
            return true;
        });
    }

    @Test
    public void testRandomRouter() throws InterruptedException {
        executeTest(Duration.apply(20, TimeUnit.SECONDS), () -> {
            ActorRef randomRouter = BaseTest.system.actorOf(new RandomPool(5)
                    .props(Props.create(Routee.class, () -> new Routee())), "random-router");
            sendMessage(randomRouter);
            return true;
        });
    }

    private void sendMessage(ActorRef router) {
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(2 * 1000);
                router.tell("message" + i, probingActor);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
