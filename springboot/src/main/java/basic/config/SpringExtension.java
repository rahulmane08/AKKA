package basic.config;

import akka.actor.AbstractExtensionId;
import akka.actor.ExtendedActorSystem;
import akka.actor.Extension;
import akka.actor.Props;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

public class SpringExtension extends AbstractExtensionId<SpringExtension.SpringExt> {
    private static final SpringExtension SPRING_EXTENSION_PROVIDER
            = new SpringExtension();

    public static SpringExtension getInstance() {
        return SPRING_EXTENSION_PROVIDER;
    }

    @Override
    public SpringExt createExtension(ExtendedActorSystem system) {
        return new SpringExt();
    }

    public static class SpringExt implements Extension {
        private volatile ApplicationContext applicationContext;

        public void initialize(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        public Props props(String actorBeanName) {
            return Props.create(
                    SpringActorProducer.class, applicationContext, actorBeanName).withDispatcher("my-dispatcher");
        }
    }
}
