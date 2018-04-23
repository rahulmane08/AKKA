package eventbus;

import akka.actor.ActorRef;
import akka.event.japi.LookupEventBus;

public class SampleEventBus extends LookupEventBus<Message, ActorRef, String> {

    /**
     * determines the initial size of the index data structure
     * used internally (i.e. the expected number of different classifiers)
     * @return
     */
    @Override
    public int mapSize() {
        return 128;
    }

    /**
     * must define a full order over the subscribers, expressed as expected from
     * `java.lang.Comparable.compare`
     * @param a
     * @param b
     * @return
     */
    @Override
    public int compareSubscribers(ActorRef a, ActorRef b) {
        return a.compareTo(b);
    }

    /**
     * is used for extracting the classifier from the incoming events
     */
    @Override
    public String classify(Message event) {
        return event.topic;
    }

    /**
     * will be invoked for each event for all subscribers which registered themselves
     * for the event’s classifier
     * @param event
     * @param subscriber
     */
    @Override
    public void publish(Message event, ActorRef subscriber) {
        subscriber.tell(event.payload, ActorRef.noSender());
    }
}
