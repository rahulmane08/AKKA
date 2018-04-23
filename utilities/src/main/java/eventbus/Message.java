package eventbus;

public class Message {
    public final String topic;
    public final Object payload;

    public Message(String topic, Object payload) {
        this.topic = topic;
        this.payload = payload;
    }
}
