package collector.event.publisher;

import collector.configuration.EthConfiguration;
import collector.event.EthTxEvent;
import collector.util.CollectorThreadFactory;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import java.util.Objects;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author zacconding
 * @Date 2018-12-20
 * @GitHub : https://github.com/zacscoding
 */
@Slf4j(topic = "publisher")
@Component
@ConditionalOnBean(value = EthConfiguration.class)
public class EthTransactionPublisher {

    private EventBus asyncEventBus;

    @PostConstruct
    private void setUp() {
        CollectorThreadFactory factory = new CollectorThreadFactory("tx-publisher", true);
        this.asyncEventBus = new AsyncEventBus("tx-event-bus", Executors.newCachedThreadPool(factory));
    }

    public void publish(EthTxEvent txEvent) {
        asyncEventBus.post(txEvent);
    }

    public void register(Object listener) {
        Objects.requireNonNull(listener, "listener must be not null");
        asyncEventBus.register(listener);
        logger.debug("## Success to register listener : {} at Eth Tx Publisher", listener.getClass().getSimpleName());
    }
}
