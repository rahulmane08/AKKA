package sample;

import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.dispatch.PriorityGenerator;
import akka.dispatch.UnboundedStablePriorityMailbox;

public class MyPrioMailbox extends UnboundedStablePriorityMailbox {
    // needed for reflective instantiation
    public MyPrioMailbox(ActorSystem.Settings settings, com.typesafe.config.Config config) {
        // Create a new PriorityGenerator, lower prio means more important
        super(new PriorityGenerator() {
            @Override
            public int gen(Object msg) {
                if (msg.equals(PoisonPill.getInstance())) {
                    return 3; // PoisonPill when no other left
                }
                else {
                    String message = (String)msg;
                    if (message.startsWith("highpriority"))
                        return 0; // 'highpriority messages should be treated first if possible
                    else if (message.startsWith("lowpriority"))
                        return 2; // 'lowpriority messages should be treated last if possible
                    else
                        return 1; // By default they go between high and low prio
                }
            }
        });
    }
}