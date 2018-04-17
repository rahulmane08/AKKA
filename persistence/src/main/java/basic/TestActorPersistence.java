package basic;

import akka.actor.Props;
import org.junit.Test;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class TestActorPersistence extends BaseTest {

    @Test
    public void testActorPersistence() {
        FiniteDuration interval = Duration.apply(2, TimeUnit.SECONDS);
        executeTest(Duration.apply(10, TimeUnit.SECONDS), () -> {
            system.actorOf(Props.create(PersistentActor.class, () -> new PersistentActor()), "persistence-actor");
            hold(interval);


            return true;
        });
    }
}
