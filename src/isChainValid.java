import java.util.ArrayList;
import java.util.HashMap;

public class isChainValid {
    public static Boolean isChainValid(ArrayList blockchain) {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[NoobChain.difficulty]).replace('\0', '0');
        HashMap<String, TransactionOutputs> tempUTXOs = new HashMap<String, TransactionOutputs>(); //a temporary working list of unspent transactions at a given block state.
        tempUTXOs.put(NoobChain.genesisTransaction.outputs.get(0).id, NoobChain.genesisTransaction.outputs.get(0));

        //loop through blockchain to check hashes:
        for (int i = 1; i < blockchain.size(); i++) {

            currentBlock = (Block) blockchain.get(i);
            previousBlock = (Block) blockchain.get(i - 1);
            //등록된 해시와 계산된 해시 비교:
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("#현재 해쉬가 같지않음");
                return false;
            }
            //이전 해시와 등록된 이전 해시 비교
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                System.out.println("이전 해시가 같지 않음");
                return false;
            }
            //해시가 해결되었는지 확인
            if (!currentBlock.hash.substring(0, NoobChain.difficulty).equals(hashTarget)) {
                System.out.println("#이 블록은 채굴되지 않았습니다");
                return false;
            }

            //루프 스루 블록체인 트랜잭션:
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
}