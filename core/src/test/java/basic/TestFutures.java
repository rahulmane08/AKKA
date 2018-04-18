package basic;

import akka.dispatch.Futures;
import akka.dispatch.OnComplete;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.event.LoggingAdapter;
import org.junit.Test;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;
import scala.concurrent.Promise;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static akka.dispatch.Futures.failed;
import static akka.dispatch.Futures.future;
import static akka.dispatch.Futures.sequence;
import static akka.dispatch.Futures.successful;
import static akka.dispatch.Futures.traverse;

public class TestFutures extends BaseTest {

    private <T> OnSuccess<T> logOnSuccessHandler(LoggingAdapter log) {
        return new OnSuccess<T>() {
            @Override
            public void onSuccess(T result) throws Throwable {
                log.info(String.valueOf(result));
            }
        };
    }

    private OnFailure logFailureHandler(LoggingAdapter log) {
        return new OnFailure() {
            @Override
            public void onFailure(Throwable failure) throws Throwable {
                log.error(failure.getMessage());
            }
        };
    }

    private <T> OnComplete<T> logOnCompleteHandler(LoggingAdapter log) {
        return new OnComplete<T>() {
            @Override
            public void onComplete(Throwable failure, T result) throws Throwable {
                if (failure != null)
                    log.error(failure.getMessage());
                else
                    log.info(String.valueOf(result));
            }
        };
    }

    @Test
    public void testFutures() {
        executeTest(Duration.apply(20, TimeUnit.SECONDS), () -> {
            ExecutionContextExecutor executor = system.dispatcher();
            LoggingAdapter logger = system.log();
            FiniteDuration interval = Duration.apply(3, TimeUnit.SECONDS);

            Future<String> f1 = future(() -> {
                Thread.sleep(10000);
                return "hello";
            }, executor);
            f1.onSuccess(logOnSuccessHandler(system.log()), executor);

            successful(" successful future").onSuccess(logOnSuccessHandler(logger), executor);
            failed(new IllegalArgumentException("test exception")).onFailure(logFailureHandler(logger), executor);

            // promise
            Promise<String> promise = Futures.promise();
            promise.future().onSuccess(logOnSuccessHandler(logger), executor);
            hold(interval);
            promise.success("Promised Hello"); // on fulfilling the promise the on success proceeds

            // finishes when f1 finishes and prints length of Hello
            f1.map(String::length, executor).onSuccess(logOnSuccessHandler(logger), executor);

            // combining futures into a single
            List<Future<Integer>> listOfFutures = new ArrayList<>();
            for (int i = 0; i < 10; i++)
                listOfFutures.add(successful(i));
            Future<Iterable<Integer>> futureListOfInts = sequence(listOfFutures, executor); // single future over 10 futures
            Future<Long> sumFuture = futureListOfInts.map(list -> { // get the sum of 10 numbers
                long sum = 0;
                for (Integer i : list)
                    sum += i;
                return sum;
            }, executor);
            sumFuture.onSuccess(logOnSuccessHandler(logger), executor);

            // creating a single Future of 10 future ints using traverse
            List<Integer> intList = new ArrayList<>();
            for (int i = 0; i < 10; i++)
                intList.add(i);
            Future<Iterable<Integer>> traverse = traverse(intList, i -> successful(i), executor);
            sumFuture = traverse.map(list -> { // get the sum of 10 numbers
                long sum = 0;
                for (Integer i : list)
                    sum += i;
                return sum;
            }, executor);
            sumFuture.onSuccess(logOnSuccessHandler(logger), executor);

            // fallback
            Future<String> future1 = Futures.failed(new IllegalStateException("OHNOES1"));
            Future<String> future2 = Futures.failed(new IllegalStateException("OHNOES2"));
            Future<String> future3 = Futures.successful("bar");
            // Will have "bar" in this case
            Future<String> future4 = future1.fallbackTo(future2).fallbackTo(future3);
            future4.onComplete(logOnCompleteHandler(logger), executor);

            return true;
        });
    }
}
