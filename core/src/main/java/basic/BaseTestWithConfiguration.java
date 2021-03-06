package basic;

import akka.testkit.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class BaseTestWithConfiguration extends BaseTest {
    @BeforeClass
    public static void start() {
        system = ConfigurationActorSystem.newInstance("my-system");
    }

    @AfterClass
    public static void cleanup() {
        TestKit.shutdownActorSystem(system, Duration.apply(10, TimeUnit.SECONDS), true);
        system = null;
    }
}
