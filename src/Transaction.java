import java.security.*;
import java.util.ArrayList;

public class Transaction {

    public String transactionId; // 이것은 트랜잭션의 해시이기도 합니다.
    public PublicKey sender; // 발신자 주소/공개 키
    public PublicKey reciepient; // 수신자 주소/공개 키
    public float value;
    public byte[] signature; // 이것은 다른 사람이 우리 지갑에서 자금을 지출하는 것을 방지하기 위한 것입니다.

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutputs> outputs = new ArrayList<TransactionOutputs>();

    private static int sequence = 0; // 얼마나 많은 트랜잭션이 생성되었는지에 대한 대략적인 계산

    // Constructor:
    public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.reciepient = to;
        this.value = value;
        this.inputs = inputs;
    }

    // 이것은 트랜잭션 해시(ID로 사용됨)를 계산합니다
    private String calulateHash() {
        sequence++; //동일한 해시를 갖는 2개의 동일한 트랜잭션을 피하기 위해 시퀀스를 늘립니다.
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                        StringUtil.getStringFromKey(reciepient) +
                        Float.toString(value) + sequence
        );
    }
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value)	;
        signature = StringUtil.applyECDSASig(privateKey,data);
    }
    //서명한 데이터가 변조되지 않았는지 확인합니다.
    public boolean verifiySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value)	;
        return StringUtil.verifyECDSASig(sender, data, signature);
    }
    public boolean processTransaction() {

        if(verifiySignature() == false) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        //트랜잭션 입력 수집(사용되지 않았는지 확인):
        for(TransactionInput i : inputs) {
            i.UTXO = NoobChain.UTXOs.get(i.transactionOutputId);
        }

        //거래가 유효한지 확인:
        if(getInputsValue() < NoobChain.minimumTransaction) {
            System.out.println("#작은 거래 입력: " + getInputsValue());
            return false;
        }

        //트랜잭션 출력 생성:
        float leftOver = getInputsValue() - value; //입력 값을 얻은 다음 남은 변경 사항:
        transactionId = calulateHash();
        outputs.add(new TransactionOutputs( this.reciepient, value,transactionId)); //send value to recipient
        outputs.add(new TransactionOutputs( this.sender, leftOver,transactionId)); //send the left over 'change' back to sender

        //미사용 목록에 출력 추가
        for(TransactionOutputs o : outputs) {
            NoobChain.UTXOs.put(o.id , o);
        }

        //지출된 UTXO 목록에서 트랜잭션 입력을 제거합니다.:
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; //if Transaction can't be found skip it
            NoobChain.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    //입력(UTXO) 값의 합계를 반환합니다.
    public float getInputsValue() {
        float total = 0;
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; //if Transaction can't be found skip it
            total += i.UTXO.value;
        }
        return total;
    }

    //출력 합계를 반환합니다.:
    public float getOutputsValue() {
        float total = 0;
        for(TransactionOutputs o : outputs) {
            total += o.value;
        }
        return total;
    }
}