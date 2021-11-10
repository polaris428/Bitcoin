import java.security.PublicKey;

public class TransactionOutputs {
    public String id;
    public PublicKey reciepient; //이 코인의 새로운 소유자라고도 합니다
    public float value; //그들이 소유한 동전의 양
    public String parentTransactionId; //이 출력이 생성된 트랜잭션의 ID

    //Constructor
    public TransactionOutputs(PublicKey reciepient, float value, String parentTransactionId) {
        this.reciepient = reciepient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient)+Float.toString(value)+parentTransactionId);
    }

    //코인이 본인 소유인지 확인
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == reciepient);
    }

}