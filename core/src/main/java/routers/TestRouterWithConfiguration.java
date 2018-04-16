package routers;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.ConsistentHashingRouter;
import akka.routing.FromConfig;
import basic.BaseTestWithConfiguration;
import org.junit.Test;

public class TestRouterWithConfiguration extends BaseTestWithConfiguration {

    @Test
    public void testRoundRobinRouter() throws InterruptedException {
        ActorRef roundRobinRouter = system.actorOf(FromConfig.getInstance().props(Props.create(Routee.class)),
                "round-robin-router");
        sendMessage(roundRobinRouter);
    }

    @Test
    public void testRandomRouter() throws InterruptedException {
        ActorRef randomRouter = system.actorOf(FromConfig.getInstance().props(Props.create(Routee.class)),
                "random-router");
        sendMessage(randomRouter);
    }

    @Test
    public void testBroadCastRouter() throws InterruptedException {
        ActorRef broadcastRouter = system.actorOf(FromConfig.getInstance().props(Props.create(Routee.class)),
                "broadcast-router");
        sendMessage(broadcastRouter);
    }

    @Test
    public void testSmallestMailboxPoolRouter() throws InterruptedException {
        ActorRef smallestMailboxPoolRouter = system.actorOf(FromConfig.getInstance().props(Props.create(Routee.class)),
                "smallest-mb-router");
        // load routee $a with 1000 messages
        for (int i=0; i<10000; i++) {
            system.actorSelection(smallestMailboxPoolRouter.path().child("$a")).tell("burstmessage", probingActor);
        }
        // this router will not choose $a and send the actual messages only to $b as its the next actor with smallest mb
        sendMessage(smallestMailboxPoolRouter);
    }

    @Test
    public void testBalancingPoolRouter() throws InterruptedException {
        ActorRef balancingPoolRouter = system.actorOf(FromConfig.getInstance().props(Props.create(Routee.class)),
                "balancing-pool-router");
        // load routee $a with 1000 messages
        for (int i=0; i<10000; i++) {
            system.actorSelection(balancingPoolRouter.path().child("$a")).tell("burstmessage", probingActor);
        }
        // this router will not choose $a and send the actual messages only to $b as its the next actor with smallest mb
        sendMessage(balancingPoolRouter);
    }

    @Test
    public void testConsistentHashingRouter() throws InterruptedException {
        ActorRef consistentHashRouter = system.actorOf(FromConfig.getInstance().props(Props.create(Routee.class)),
                "consistent-hash-router");
        for (int i=0; i<5; i++) {
            for (int j=0; j<5; j++){
                String message = "message"+j;
                consistentHashRouter.tell(new HashableMessage<>(message) , probingActor);
                Thread.sleep(2*1000);
            }
        }
    }

    private void sendMessage(ActorRef router) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            Thread.sleep(2 * 1000);
            router.tell("message" + i, probingActor);
        }
    }

}
