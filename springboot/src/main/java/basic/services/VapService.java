package basic.services;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import basic.actor.ClientActor;
import basic.actor.MasterActor;
import basic.config.SpringExtension;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

@RestController("vapService")
@RequestMapping("/akka/master")
public class VapService implements InitializingBean {

    private ActorRef masterActor;

    @Autowired
    private ActorSystem system;

    @GetMapping("/hi")
    public String sayHi() throws Exception {
        Timeout timeout = new Timeout(Duration.apply(1, TimeUnit.SECONDS));
        Future<Object> reply = Patterns.ask(masterActor, "hi", timeout);
        return (String) Await.result(reply, timeout.duration());
    }

    @GetMapping("/tenant")
    public String processTenant(@RequestParam(value = "id",required = true) String tenantId) {
        ClientActor.Message<String> message = new ClientActor.Message<>(tenantId, "Test Data");
        masterActor.tell(message, ActorRef.noSender());
        return "processed "+message;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.masterActor = system.actorOf(SpringExtension.getInstance().get(system)
                .props("masterActor"), "masterActor");
    }
}
