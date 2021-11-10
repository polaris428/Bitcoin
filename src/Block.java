import java.util.ArrayList;
import java.util.Date;


public class Block {

    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); //우리의 데이터는 간단한 메시지가 될 것입니다.
    public long timeStamp; //1970년 1월 1일 이후의 밀리초 수로 표시됩니다.
    public int nonce;

    //블록 생성자.
    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();

        this.hash = calculateHash(); //다른 값을 설정한 후에 이 작업을 수행해야 합니다.
    }

    //블록 내용을 기반으로 새 해시 계산
    public String calculateHash() {
        String calculatedhash = StringUtil.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) +
                        merkleRoot
        );
        return calculatedhash;
    }

    //해시 대상에 도달할 때까지 nonce 값을 늘립니다.
    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDificultyString(difficulty); //Create a string with difficulty * "0"
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("블록 채굴!!! : " + hash);
    }

    //이 블록에 트랜잭션 추가
    public boolean addTransaction(Transaction transaction) {
        //process transaction and check if valid, unless block is genesis block then ignore.
        if (transaction == null) return false;
        if ((previousHash != "0")) {
            if ((transaction.processTransaction() != true)) {
                System.out.println("거래를 처리하지 못했습니다. 폐기됨.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("블록에 트랜잭션이 성공적으로 추가되었습니다.");
        return true;
    }


}
