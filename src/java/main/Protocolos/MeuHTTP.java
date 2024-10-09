package Protocolos;
import java.util.*;


public class MeuHTTP {

    public MeuHTTP(){

    }

    public static String metodoHTTP(String linha){

        List<Metodo> metodos = new ArrayList<>(Arrays.asList(Metodo.values()));

        List<Metodo> buscaMetodo = metodos.stream().filter((metodo)-> metodo.name().equals(linha)).toList();

        if (!buscaMetodo.isEmpty()){
            return buscaMetodo.get(0).name();
        }
     

        return "None";



    }

    public String criarRespostaHTML(String requisicao, String respostaServico){

        String responseBody = "<html>" +
                            "<head><title>Banco Metrópole</title></head>" +
                            "<body><h1>Operacao: "+requisicao+"</h1>" +
                            "<p>"+respostaServico+"</p></body>" +
                            "</html>";

        return responseBody;
    }

    public enum Metodo{
        GET,
        POST,
        DELETE,
        PUT,

    }

    public static void main(String[] args) {
        var meuHTTP = new MeuHTTP();

        var r = meuHTTP.metodoHTTP("");

        System.out.println(r);
    }
    
}
