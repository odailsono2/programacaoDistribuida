package services.banco;
public class Contabuilder {
    private Conta conta;

    Contabuilder(){
        this.conta = new Conta();
    }

    public Contabuilder setSaldo(float saldo){
        conta.setSaldo(saldo);
        return this;
    }

    public Contabuilder setId(String idConta){
        conta.setId(idConta);
        return this;
    }

    public Conta criarConta(){
        return conta;
    }


}
