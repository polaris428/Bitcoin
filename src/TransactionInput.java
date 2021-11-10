public class TransactionInput {
    public String transactionOutputId; //TransactionOutputs -> transactionId에 대한 참조
    public TransactionOutputs UTXO; //미사용 트랜잭션 출력을 포함합니다.

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}