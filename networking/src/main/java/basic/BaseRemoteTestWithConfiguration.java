package basic;

import akka.testkit.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class BaseRemoteTestWithConfiguration extends BaseTest {
    @BeforeClass
    public static void start() {
        system = ConfigurationActorSystem.newInstance("remote1-system");
    }

    @AfterClass
    public static void cleanup() {
        TestKit.shutdownActorSystem(system, Duration.apply(10, TimeUnit.SECONDS), true);
        system = null;
    }
}
