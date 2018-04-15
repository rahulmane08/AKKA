import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.testkit.TestActorRef;
import akka.testkit.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class BaseTest {
    protected static ActorSystem system;
    protected final TestKit probe;
    protected final ActorRef probingActor;

    public BaseTest() {
        probe = new TestKit(system);
        probingActor = probe.testActor();
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

    protected <T extends AbstractActor, M, E> E ask(Props props, M message, long askTimeoutMillis)
            throws Exception {
        TestActorRef<T> testActorRef = TestActorRef.create(system, props, "test-actor");
        Future<Object> askResult = Patterns.ask(testActorRef, message, askTimeoutMillis);
        assertTrue(askResult.isCompleted());
        Object result = Await.result(askResult, Duration.Zero());
        return (E) result;
    }
}
