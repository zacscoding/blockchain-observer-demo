package collector.ethereum.observer;

import collector.configuration.EthereumProperties;
import collector.ethereum.EthereumNetwork;
import collector.ethereum.EthereumNode;
import collector.ethereum.rpc.EthereumRpcServiceManager;
import collector.ethereum.event.EthereumBlockEvent;
import collector.ethereum.event.EthereumPendingTxEvent;
import collector.ethereum.event.publisher.EthereumBlockPublisher;
import collector.ethereum.event.publisher.EthereumPendingTxPublisher;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.Transaction;

/**
 * @author zacconding
 * @Date 2018-12-19
 * @GitHub : https://github.com/zacscoding
 */
@Slf4j(topic = "observer")
@Component
public class EthereumObserver {

    private EthereumProperties ethProperties;
    private EthereumRpcServiceManager ethRpcServiceManager;
    private EthereumBlockPublisher ethBlockPublisher;
    private EthereumPendingTxPublisher ethPendingTxPublisher;

    @Autowired
    public EthereumObserver(EthereumProperties ethProperties, EthereumRpcServiceManager ethRpcServiceManager
        , EthereumBlockPublisher ethBlockPublisher, EthereumPendingTxPublisher ethPendingTxPublisher) {

        this.ethProperties = ethProperties;
        this.ethRpcServiceManager = ethRpcServiceManager;
        this.ethBlockPublisher = ethBlockPublisher;
        this.ethPendingTxPublisher = ethPendingTxPublisher;
    }

    @PostConstruct
    private void setUp() {
        startObserve();
    }

    /**
     * Start block filter + pending tx filter
     */
    private void startObserve() {
        List<EthereumNetwork> networks = ethProperties.getNetworks();
        if (CollectionUtils.isEmpty(networks)) {
            log.info("## Skip ethereum observer because empty ethereum networks");
            return;
        }

        for (EthereumNetwork network : networks) {
            List<EthereumNode> ethNodes = network.getNodes();

            if (CollectionUtils.isEmpty(ethNodes)) {
                log.info("## Skip observe {} eth network. because empty nodes", network.getNetworkName());
                continue;
            }

            log.debug("## Start to observe {} ethereum network. block time : {} / # nodes : {}"
                , network.getNetworkName(), network.getBlockTime(), network.getNodes().size());

            for (EthereumNode ethNode : ethNodes) {
                try {
                    // block filter
                    startBlockFilter(network.getNetworkName(), network.getBlockTime(), ethNode);

                    // pending tx observer
                    startPendingTxFilter(network.getNetworkName(), network.getBlockTime(),
                        network.getPendingTxPollingInterval(), ethNode);
                } catch (Exception e) {
                    log.error("Exception occur while registering filters", e);
                    ethRpcServiceManager.cancelBlockFilter(ethNode);
                    ethRpcServiceManager.cancelPendingTxFilter(ethNode);
                }
            }
        }
    }

    private void startBlockFilter(String networkName, long blockTime, EthereumNode ethNode) {
        // block observer
        final Consumer<Block> onBlock = block -> {
            EthereumBlockEvent event = new EthereumBlockEvent(networkName, blockTime, ethNode, block);
            ethBlockPublisher.publish(event);
        };

        boolean result = ethRpcServiceManager.registerBlockFilter(ethNode, blockTime, onBlock);
        log.debug("## Tried to register block filter : {} > {}", ethNode.getNodeName(), result);
    }

    private void startPendingTxFilter(String networkName, long blockTime, long pollingInterval, EthereumNode ethNode) {
        final Consumer<Transaction> onPendingTx = tx -> {
            EthereumPendingTxEvent event = new EthereumPendingTxEvent(networkName, ethNode.getNodeName(), tx);
            ethPendingTxPublisher.publish(event);
        };

        boolean result = ethRpcServiceManager.registerPendingTxFilter(ethNode, blockTime, pollingInterval, onPendingTx);
        log.debug("## Tried to register pending tx filter : {} > {}", ethNode.getNodeName(), result);
    }
}
