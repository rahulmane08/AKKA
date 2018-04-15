package sample;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfigurationActorSystem {
    public static ActorSystem newInstance() {
        Config config = ConfigFactory.load().getConfig("mysystem");
        ActorSystem system = ActorSystem.create("my-system", config);
        return system;
    }
}
