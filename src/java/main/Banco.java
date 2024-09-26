import java.util.HashMap;

public class Banco {

    private HashMap<String,Conta> contas = new HashMap<>();

    public void criar(String id){

        Contabuilder criadorContas = new Contabuilder();

        Conta novaConta = criadorContas
                            .setId(id)
                            .setSaldo(0)
                            .criarConta();

        contas.put(id, novaConta);
        
    }

    public void transferir(String idContaOrigem, String idContaDestino, double valor) throws Exception{
        
            try {
                testaValorPositivo(valor);
                contaValida(idContaOrigem);
                contaValida(idContaDestino);
                testaSaldoSuficiente(idContaOrigem, valor);
                contas.get(idContaOrigem).sacar(valor);
                contas.get(idContaDestino).depositar(valor);
               
            } catch (Exception e) {
                throw new Exception("Transferencia Erro: "+e.getMessage());
            }
        
    }

    public void depositar(String id, float valor) throws Exception{
        try{
            testaValorPositivo(valor);
            contaValida(id);
            contas.get(id).depositar(valor);
        }
        catch(Exception e){
            throw new Exception("Deposito Erro:" + e.getMessage());
        }
    }

    public void contaValida(String id) throws Exception{
        if (contas.get(id) != null){
            return;
        }
        else{
            throw new Exception("Conta Inexistente");
        }
    }
    public void testaValorPositivo(double valor) throws Exception{
        if (valor > 0){
            return;
        }
        else{
            throw new Exception("Valor deve ser maior que zero!");
        }
    }

    public void testaSaldoSuficiente(String id, double valor) throws Exception{
        if (contas.get(id).getSaldo() > valor){
            return;
        }
        else{
            throw new Exception("Saldo Insuficiente!");
        }
    }


    public static void main(String[] args) {
        Banco banco = new Banco();

        banco.criar("1");
        banco.criar("2");
        banco.criar("3");

        try {
            banco.depositar("2", 400);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            banco.transferir("2","1", 125.55);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            banco.transferir("1","3", 25);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        banco.contas.values().stream().forEach(System.out::println);
    }

}
