package basic;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.Futures;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import org.junit.Test;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class TestFuturePiping extends BaseTest {
    @Test
    public void testPiping() {
        executeTest(Duration.apply(20, TimeUnit.SECONDS), () -> {
            Double x = new Double(2);
            Timeout askTimeout = new Timeout(Duration.apply(15, TimeUnit.SECONDS));
            ActorRef squarer = system.actorOf(Props.create(Squarer.class, Squarer::new), "squarer");
            ActorRef cuber = system.actorOf(Props.create(Cuber.class, Cuber::new), "cuber");
            ActorRef printer = system.actorOf(Props.create(Printer.class, Printer::new), "printer");
            hold(Duration.apply(2, TimeUnit.SECONDS));
            Future<Double> square = Patterns.ask(squarer, x, askTimeout)
                    .map(result -> (Double) result, system.dispatcher());
            Future<Double> cube = Patterns.ask(cuber, x, askTimeout)
                    .map(result -> (Double) result, system.dispatcher());
            Patterns.pipe(square.map(s -> String.format("Square of %1$,.2f = %2$,.2f", x, s), system.dispatcher()),
                    system.dispatcher()).to(printer, probingActor);
            Patterns.pipe(cube.map(c -> String.format("Cube of %1$,.2f = %2$,.2f", x, c), system.dispatcher()),
                    system.dispatcher()).to(printer, probingActor);

            Future<Iterable<Double>> comboFuture = Futures.sequence(Arrays.asList(square, cube), system.dispatcher());
            Future<Double> summed = comboFuture.map(list -> {
                double sum = 0;
                for (Double i : list)
                    sum += i;
                system.log().info("summed future = " + sum);
                return sum;
            }, system.dispatcher());
            Patterns.pipe(summed.map(s -> String.format("Sum of square and cube of %1$,.2f = %2$,.2f", x, s), system.dispatcher()),
                    system.dispatcher()).to(printer, probingActor);
            return true;
        });
    }

    class Squarer extends AbstractActor {
        private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(Double.class, number -> {
                        log.info("Squaring the number = " + number);
                        Thread.sleep(5 * 1000);
                        getSender().tell(Math.pow(number, 2), getSelf());
                    })
                    .build();
        }
    }

    class Cuber extends AbstractActor {
        private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(Double.class, number -> {
                        log.info("Cubing the number = " + number);
                        Thread.sleep(10 * 1000);
                        getSender().tell(Math.pow(number, 3), getSelf());
                    })
                    .build();
        }
    }

    class Printer extends AbstractActor {
        private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchAny(msg -> log.info(String.valueOf(msg)))
                    .build();
        }
    }
}
