package io.spring.platform.bus.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.amqp.Amqp;
import org.springframework.integration.dsl.support.GenericHandler;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.platform.config.client.RefreshEndpoint;
import org.springframework.platform.context.restart.RestartEndpoint;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Spencer Gibb
 */
@Configuration
public class AmqpBusAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(AmqpBusAutoConfiguration.class);

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired(required = false)
    private RefreshEndpoint refreshEndpoint;

    @Autowired(required = false)
    private RestartEndpoint restartEndpoint;

    @Bean
    protected FanoutExchange platformBusExchange() {
        //TODO: change to TopicExchange?
        FanoutExchange exchange = new FanoutExchange("spring.platform.bus");
        amqpAdmin.declareExchange(exchange);
        return exchange;
    }

    @Bean
    protected Queue localPlatformBusQueue() {
        Queue queue = amqpAdmin.declareQueue();
        amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(platformBusExchange()));
        return queue;
    }

    @Bean
    public IntegrationFlow platformBusFlow() {
        return IntegrationFlows.from(Amqp.inboundGateway(connectionFactory, localPlatformBusQueue().getName()))
            .transform(Transformers.fromJson(String.class))
            .handle(String.class, new GenericHandler<String>() {
                @Override
                public Object handle(String p, Map<String, Object> headers) {
                    System.out.println(p);
                    if (refreshEndpoint != null && "refresh".equals(p)) {
                        String[] refreshed = refreshEndpoint.refresh();
                        logger.info("The following configuration keys were refreshed {}", Arrays.asList(refreshed));
                    } else if (restartEndpoint != null && "restart".equals(p)) {
                        //TODO: restart doesn't let this message get ack'ed so it keeps reading the message, run in Thread?
                        //ConfigurableApplicationContext context = restartEndpoint.restart();
                        logger.info("The application was restarted.  Context startupDate");//context.getStartupDate());
                    }
                    return null;
                }
            })
            .get();
    }
}
