package blockchain.rpc;

import blockchain.model.enums.BlockchainType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;

/**
 * @author zacconding
 * @Date 2018-09-26
 * @GitHub : https://github.com/zacscoding
 */
@Getter
@Setter
@ToString
public class RpcServices {

    private BlockchainType blockchainType;

    // tag : ethereum
    private Web3j web3j;
    private Web3jService web3jService;
    // -- tag : ethereum
}
