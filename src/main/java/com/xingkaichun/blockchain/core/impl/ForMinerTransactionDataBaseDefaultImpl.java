package com.xingkaichun.blockchain.core.impl;

import com.xingkaichun.blockchain.core.BlockChainDataBase;
import com.xingkaichun.blockchain.core.ForMinerTransactionDataBase;
import com.xingkaichun.blockchain.core.model.transaction.Transaction;
import com.xingkaichun.blockchain.core.utils.atomic.EncodeDecode;
import com.xingkaichun.blockchain.core.utils.atomic.LevelDBUtil;
import com.xingkaichun.blockchain.core.utils.atomic.TransactionUtil;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ForMinerTransactionDataBaseDefaultImpl implements ForMinerTransactionDataBase {

    //交易池数据库
    private DB transactionPoolDB;

    public ForMinerTransactionDataBaseDefaultImpl(String dbPath) throws Exception {

        this.transactionPoolDB = LevelDBUtil.createDB(new File(dbPath,"ForMinerTransactionDataBase"));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                transactionPoolDB.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    public boolean addTransaction(Transaction transaction) throws Exception {

        //校验签名 防止签名错误的交易加入交易池
        boolean verifySignature = TransactionUtil.verifySignature(transaction);
        if(!verifySignature){
            return false;
        }

        //交易已经持久化进交易池数据库 丢弃交易
        String combineKey = combineKey(transaction);
        if(isTransactionExsitInPool(combineKey)){
            return false;
        }
        synchronized (BlockChainDataBase.class){
            LevelDBUtil.put(transactionPoolDB,combineKey, EncodeDecode.encode(transaction));
        }
        return true;
    }

    public List<Transaction> getTransactionList() throws Exception {
        synchronized (BlockChainDataBase.class){
            List<Transaction> transactionList = new ArrayList<>();
            DBIterator dbIterator = this.transactionPoolDB.iterator();
            while (dbIterator.hasNext()){
                Map.Entry<byte[],byte[]> entry =  dbIterator.next();
                byte[] byteTransaction = entry.getValue();
                Transaction transaction = EncodeDecode.decodeToTransaction(byteTransaction);
                transactionList.add(transaction);
            }
            return transactionList;
        }
    }

    /**
     * 交易是否已经存在于交易池
     * @param transactionUUID 交易ID
     */
    private boolean isTransactionExsitInPool(String transactionUUID) throws Exception {
        byte[] byteTransaction = LevelDBUtil.get(transactionPoolDB,transactionUUID);
        return byteTransaction != null;
    }

    /**
     * 生成一个关键字(公钥+交易UUID)
     * 为什么生成这么一个KEY？
     * 假设有客户端恶意生成和别人相同的交易UUID。
     * 因为KEY是有公钥参与生成，客户端不可能冒充别人(想要冒充别人得有别人的公钥与私钥)
     * 一个客户端多次发送同一笔交易，会计算成同一个KEY，所以在交易池只会保存一笔。
     * 因为KEY是有公钥参与生成，所以在交易池看来，它们是不同的交易，它们都可以持久化进交易池。
     * @param transaction 交易
     */
    private String combineKey(Transaction transaction) {
        return TransactionUtil.getSender(transaction).getValue() + transaction.getTransactionUUID();
    }
}
