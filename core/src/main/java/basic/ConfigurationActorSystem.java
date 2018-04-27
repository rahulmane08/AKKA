package basic;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfigurationActorSystem {
    public static ActorSystem newInstance(String actorSystemNameInConfig) {
        Config config = ConfigFactory.load().getConfig(actorSystemNameInConfig);
        ActorSystem system = ActorSystem.create(actorSystemNameInConfig, config);
        return system;
    }
}
