
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

public class NoobChain {

    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static HashMap<String, TransactionOutputs> UTXOs = new HashMap<String, TransactionOutputs>();

    public static int difficulty = 3;
    public static float minimumTransaction = 0.1f;
    public static Wallet walletA;
    public static Wallet walletB;
    public static Transaction genesisTransaction;

    public static void main(String[] args) {
        //add our blocks to the blockchain ArrayList:
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider

        //Create wallets:
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        //create genesis transaction, which sends 100 NoobCoin to walletA:
        genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
        genesisTransaction.generateSignature(coinbase.privateKey);     //manually sign the genesis transaction
        genesisTransaction.transactionId = "0"; //manually set the transaction id
        genesisTransaction.outputs.add(new TransactionOutputs(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); //manually add the Transactions Output
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.

        System.out.println("생성 및 마이닝 Genesis 블록...");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        //testing
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

        isChainValid();

    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String, TransactionOutputs> tempUTXOs = new HashMap<String, TransactionOutputs>(); //a temporary working list of unspent transactions at a given block state.
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        //loop through blockchain to check hashes:
        for (int i = 1; i < blockchain.size(); i++) {

            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);
            //compare registered hash and calculated hash:
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("#현재 해쉬가 같지않음");
                return false;
            }
            //compare previous hash and registered previous hash
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                System.out.println("이전 해시가 같지 않음");
                return false;
            }
            //check if hash is solved
            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("#이 블록은 채굴되지 않았습니다");
                return false;
            }

            //loop thru blockchains transactions:
            TransactionOutputs tempOutput;
            for (int t = 0; t < currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if (!currentTransaction.verifiySignature()) {
                    System.out.println("#거래 서명(" + t + ") 유효하지 않다");
                    return false;
                }
                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("#\n입력은 트랜잭션의 출력과 동일합니다(" + t + ")");
                    return false;
                }

                for (TransactionInput input : currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if (tempOutput == null) {
                        System.out.println("#트랜잭션에 대한 참조 입력\n(" + t + ") 누락");
                        return false;
                    }

                    if (input.UTXO.value != tempOutput.value) {
                        System.out.println("#참조된 입력 트랜잭션(" + t + ") 값이 잘못되었습니다");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for (TransactionOutputs output : currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if (currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
                    System.out.println("#거래(" + t + ") 출력 수신자는 원래 있어야 하는 사람이 아닙니다.");
                    return false;
                }
                if (currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
                    System.out.println("#거래(" + t + ") 출력 '변경'은 발신자가 아닙니다.");
                    return false;
                }

            }

        }
        System.out.println("블록체인은 유효합니다");
        return true;
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}