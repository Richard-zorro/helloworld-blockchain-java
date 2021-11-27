package com.xingkaichun.helloworldblockchain.netcore.dto;


import java.io.Serializable;

/**
 * 交易输出
 * 属性含义参考
 * @see com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionOutput
 *
 * @author 邢开春 409060350@qq.com
 */
public class TransactionOutputDto implements Serializable {

    //[输出脚本]
    private OutputScriptDto outputScript;

    //交易输出的金额
    private long value;

    //部署的合约
    private String contract;

    //执行合约
    private String executeContract;


    //region get set

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public OutputScriptDto getOutputScript() {
        return outputScript;
    }

    public void setOutputScript(OutputScriptDto outputScript) {
        this.outputScript = outputScript;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public String getExecuteContract() {
        return executeContract;
    }

    public void setExecuteContract(String executeContract) {
        this.executeContract = executeContract;
    }
//endregion
}
