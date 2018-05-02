package basic.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.FromConfig;
import basic.config.SpringExtension;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("masterActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MasterActor extends AbstractActor implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    private ActorRef clientHashedRouter;

    @Override
    public void preStart() throws Exception {
        Props routeeProps = SpringExtension.getInstance().get(getContext().system())
                .props("clientActor");
        clientHashedRouter = getContext().actorOf(FromConfig.getInstance().props(routeeProps),
                "consistent-hash-router");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("hi", msg -> getSender().tell(getSelf().path() + " says hi", getSelf()))
                .match(ClientActor.Message.class, message -> clientHashedRouter.tell(message, getSelf()))
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
