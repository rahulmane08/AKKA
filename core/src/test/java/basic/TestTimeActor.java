package basic;

import akka.actor.Props;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class TestTimeActor extends BaseTest {

    @Test
    public void testTimerActor() {
        executeTest(Duration.apply(10, TimeUnit.SECONDS), () -> {
            system.actorOf(Props.create(TimeActor.class, () -> new TimeActor(Duration.apply(4, TimeUnit.SECONDS))),
                    "timer-actor")
                    .tell("start", probingActor);
            return true;
        });
    }
}
