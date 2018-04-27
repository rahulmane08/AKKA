package basic.services;

import akka.pattern.Patterns;
import akka.util.Timeout;
import basic.actor.MasterActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/akka/master")
public class MasterActorService {

    @Autowired
    private MasterActor masterActor;

    @GetMapping("/hi")
    public String sayHi() throws Exception {
        Timeout timeout = new Timeout(Duration.apply(1, TimeUnit.SECONDS));
        Future<Object> reply = Patterns.ask(masterActor.getActorRef(), "hi", timeout);
        return (String) Await.result(reply, timeout.duration());
    }
}
