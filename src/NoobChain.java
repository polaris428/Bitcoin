
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

public class NoobChain extends isChainValid{

    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static HashMap<String, TransactionOutputs> UTXOs = new HashMap<String, TransactionOutputs>();

    public static int difficulty = 3;
    public static float minimumTransaction = 0.1f;
    public static Wallet walletA;
    public static Wallet walletB;
    public static Transaction genesisTransaction;

    public static void main(String[] args) {
        //블록체인 ArrayList에 블록을 추가합니다.
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //보안 공급자로 바운시 캐슬 설정

        //지갑 생성:
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        //100 NoobCoin을 walletA로 보내는 제네시스 트랜잭션 생성:
        genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
        genesisTransaction.generateSignature(coinbase.privateKey);     //manually sign the genesis transaction
        genesisTransaction.transactionId = "0"; //manually set the transaction id
        genesisTransaction.outputs.add(new TransactionOutputs(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); //manually add the Transactions Output
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.

        System.out.println("생성 및 마이닝 Genesis 블록...");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        //테스트
        Block block1 = new Block(genesis.hash);
        System.out.println("\nWalletA의 잔액은 다음과 같습니다.: " + walletA.getBalance());
        System.out.println("\nWalletA가 WalletB로 자금(40)을 송금하려고 합니다...\n");
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
        addBlock(block1);
        System.out.println("\nWalletA의 잔액은 다음과 같습니다.: " + walletA.getBalance());
        System.out.println("\nWalletB의 잔액은: " + walletB.getBalance());

        Block block2 = new Block(block1.hash);
        System.out.println("\nWalletA 현재 보유하고 있는 금액보다 많은 금액(1000)을 송금하려고 시도 중입니다...");
        block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
        addBlock(block2);
        System.out.println("\nWalletA의 잔액은 다음과 같습니다. " + walletA.getBalance());
        System.out.println("\nWalletB의 잔액은 다음과 같습니다. " + walletB.getBalance());

        Block block3 = new Block(block2.hash);
        System.out.println("\nWalletB가 WalletA로 자금(20)을 송금하려고 합니다...");
        block3.addTransaction(walletB.sendFunds(walletA.publicKey, 20));
        System.out.println("\nWalletA의 잔액은 다음과 같습니다. " + walletA.getBalance());
        System.out.println("WalletB의 잔액은 다음과 같습니다. " + walletB.getBalance());
        isChainValid(blockchain);


    }




    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}