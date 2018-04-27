package basic.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component("masterActor")
public class MasterActor extends AbstractActor implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("hi", msg -> getSender().tell(getSelf().path().name() + " says hi", getSelf()))
                .build();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ActorRef getActorRef() {
        return getSelf();
    }
}
