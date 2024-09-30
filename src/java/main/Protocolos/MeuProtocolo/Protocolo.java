package Protocolos.MeuProtocolo;
public class Protocolo {

    private static Protocolo protocolo;

    Protocolo(){}
    
    public static Protocolo getProtocolo(){
        if (protocolo == null){
            protocolo = new Protocolo();
        }

        return protocolo;
    }

    public String[] processarMensagem(String mensagem ) {
        
        String[] partes = mensagem.split(";");
        for (int i = 0; i < partes.length; i++) {
            partes[i] = partes[i].trim();
        }
        return partes;
    }
        
    //     switch (operacao) {
    //         case "criar":
    //             return criarConta(partes[1]);

    //         case "depositar":
    //             String[] dadosDeposito = partes[1].split("-");
    //             return depositar(dadosDeposito[0], Double.parseDouble(dadosDeposito[1]));

    //         case "transferir":
    //             String[] dadosTransferencia = partes[1].split("-");
    //             return transferir(dadosTransferencia[0], dadosTransferencia[1], Double.parseDouble(dadosTransferencia[2]));

    //         default:
    //             return "Operação inválida!";
    //     }
    // }
    
}
