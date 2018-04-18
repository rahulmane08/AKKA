package basic;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.junit.Test;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

class Device extends AbstractActor {
    final String groupId;
    final String deviceId;
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    Optional<Double> lastTemperatureReading = Optional.empty();

    public Device(String groupId, String deviceId) {
        this.groupId = groupId;
        this.deviceId = deviceId;
    }

    public static Props props(String groupId, String deviceId) {
        return Props.create(Device.class, groupId, deviceId);
    }

    @Override
    public void preStart() {
        log.info("Device actor {}-{} started", groupId, deviceId);
    }

    @Override
    public void postStop() {
        log.info("Device actor {}-{} stopped", groupId, deviceId);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RecordTemperature.class, r -> {
                    log.info("Recorded temperature reading {} with {}", r.value, r.requestId);
                    lastTemperatureReading = Optional.of(r.value);
                    getSender().tell(new TemperatureRecorded(r.requestId), getSelf());
                })
                .match(ReadTemperature.class, r -> {
                    getSender().tell(new RespondTemperature(r.requestId, lastTemperatureReading), getSelf());
                })
                .build();
    }

    public static final class ReadTemperature {
        long requestId;

        public ReadTemperature(long requestId) {
            this.requestId = requestId;
        }
    }

    public static final class RespondTemperature {
        long requestId;
        Optional<Double> value;

        public RespondTemperature(long requestId, Optional<Double> value) {
            this.requestId = requestId;
            this.value = value;
        }
    }

    public static final class RecordTemperature {
        final long requestId;
        final double value;

        public RecordTemperature(long requestId, double value) {
            this.requestId = requestId;
            this.value = value;
        }
    }

    public static final class TemperatureRecorded {
        final long requestId;

        public TemperatureRecorded(long requestId) {
            this.requestId = requestId;
        }
    }
}

public class TestingActorSystemTest extends BaseTest {

    @Test
    public void testActorSystemUsingTestKit() {
        FiniteDuration probeWait = Duration.apply(5, TimeUnit.SECONDS); // test cases will have a deadline of 5 seconds
        ActorRef deviceActor = system.actorOf(Device.props("group", "device"),
                "device-actor-A");

        executeTest(probeWait, () -> {
            deviceActor.tell(new Device.RecordTemperature(1L, 24.0), probingActor);
            assertEquals(1L, testKitProbe.expectMsgClass(probeWait, Device.TemperatureRecorded.class).requestId);

            deviceActor.tell(new Device.ReadTemperature(2L), probingActor);
            Device.RespondTemperature response1 = testKitProbe.expectMsgClass(probeWait, Device.RespondTemperature.class);
            assertEquals(2L, response1.requestId);
            assertEquals(Optional.of(24.0), response1.value);

            deviceActor.tell(new Device.RecordTemperature(3L, 55.0), probingActor);
            assertEquals(3L, testKitProbe.expectMsgClass(probeWait, Device.TemperatureRecorded.class).requestId);

            deviceActor.tell(new Device.ReadTemperature(4L), probingActor);
            Device.RespondTemperature response2 = testKitProbe.expectMsgClass(probeWait, Device.RespondTemperature.class);
            assertEquals(4L, response2.requestId);
            assertEquals(Optional.of(55.0), response2.value);

            testKitProbe.expectNoMessage();
            return true;
        });
    }

    @Test
    public void testActorSystemSynchronously() throws Exception {
        Device.TemperatureRecorded testResult = ask(Device.props("group", "device"),
                new Device.RecordTemperature(1L, 24.0), 3, "device-actor");
        assertEquals(1L, testResult.requestId);
    }
}