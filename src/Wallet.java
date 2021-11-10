import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wallet {

    public PrivateKey privateKey;
    public PublicKey publicKey;

    public Wallet(){
        generateKeyPair();
    }
    public HashMap<String,TransactionOutputs> UTXOs = new HashMap<String,TransactionOutputs>(); //only UTXOs owned by this wallet.

    public void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            //키 생성기를 초기화하고 KeyPair를 생성합니다.
            keyGen.initialize(ecSpec, random);   //허용 가능한 보안 수준을 제공하는 256바이트
            KeyPair keyPair = keyGen.generateKeyPair();
            // keyPair에서 공개 및 개인 키 설정
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransactionOutputs> item: NoobChain.UTXOs.entrySet()){
            TransactionOutputs UTXO = item.getValue();
            if(UTXO.isMine(publicKey)) { //출력이 나에게 속한 경우 (코인이 나에게 속한 경우)
                UTXOs.put(UTXO.id,UTXO); //미사용 거래 목록에 추가하십시오.
                total += UTXO.value ;
            }
        }
        return total;
    }
    //이 지갑에서 새 트랜잭션을 생성하고 반환합니다.
    public Transaction sendFunds(PublicKey _recipient,float value ) {
        if(getBalance() < value) { //gather balance and check funds.
            System.out.println("#거래를 보낼 자금이 충분하지 않습니다. 거래가 삭제되었습니다.");
            return null;
        }
        //입력 배열 목록 생성
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

        float total = 0;
        for (Map.Entry<String, TransactionOutputs> item: UTXOs.entrySet()){
            TransactionOutputs UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if(total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);
        newTransaction.generateSignature(privateKey);

        for(TransactionInput input: inputs){
            UTXOs.remove(input.transactionOutputId);
        }
        return newTransaction;
    }
}