package basic;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.testkit.TestActorRef;
import akka.testkit.TestKit;
import akka.util.Timeout;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import scala.Function0;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class BaseTest {
    protected static ActorSystem system;
    protected final TestKit testKitProbe;
    protected final ActorRef probingActor;

    public BaseTest() {
        testKitProbe = new TestKit(system);
        probingActor = testKitProbe.testActor();
    }

    @BeforeClass
    public static void start() {
        system = ActorSystem.create("test-actor-system");
    }

    @AfterClass
    public static void cleanup() {
        TestKit.shutdownActorSystem(system, Duration.apply(10, TimeUnit.SECONDS), true);
        system = null;
    }

    protected void hold(FiniteDuration duration) {
        testKitProbe.expectNoMessage(duration);
    }

    protected void hold() {
        testKitProbe.expectNoMessage();
    }

    protected <T> void executeTest(FiniteDuration deadline, Function0<T> test) {
        testKitProbe.within(deadline, () -> {
            T retVal = test.apply();
            hold();
            return retVal;
        });
    }

    protected <T extends AbstractActor, M, E> E ask(Props props, M message, int timeoutInSeconds)
            throws Exception {
        Timeout timeout = new Timeout(Duration.create(5, "seconds"));
        TestActorRef<T> testActorRef = TestActorRef.create(system, props, "test-actor");
        Future<Object> askResult = Patterns.ask(testActorRef, message, timeout);
        assertTrue(askResult.isCompleted());
        Object result = Await.result(askResult, timeout.duration());
        return (E) result;
    }
}
