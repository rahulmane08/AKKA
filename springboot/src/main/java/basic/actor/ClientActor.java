package basic.actor;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ConsistentHashingRouter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("clientActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ClientActor extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static class Message<T> implements ConsistentHashingRouter.ConsistentHashable{
        private final String tenantId;
        private final T payload;

        public Message(String tenantId, T payload) {
            this.tenantId = tenantId;
            this.payload = payload;
        }

        @Override
        public Object consistentHashKey() {
            return tenantId;
        }

        public String getTenantId() {
            return tenantId;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "tenantId='" + tenantId + '\'' +
                    ", payload=" + payload +
                    '}';
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Message.class, msg -> {
                    String printMessage =
                            String.format("%s processing the message %s", getSelf().path(), String.valueOf(msg));
                    log.info(printMessage);
                })
                .build();
    }
}
