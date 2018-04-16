package routers;

import akka.routing.ConsistentHashingRouter;

public class HashableMessage<T> implements ConsistentHashingRouter.ConsistentHashable {
    private final T message;

    public HashableMessage(T message) {
        this.message = message;
    }

    public T getMessage() {
        return message;
    }

    @Override
    public Object consistentHashKey() {
        return message.hashCode();
    }
}
