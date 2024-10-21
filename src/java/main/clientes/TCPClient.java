package clientes;
import java.io.*;
import java.net.*;


public class TCPClient {
    public static void main(String[] args) {
        int originPorta = 8080;

        String originAddress = "localhost";

        try {   
            Socket socket = new Socket(originAddress, originPorta);

            // Enviar mensagem ao servidor TCP
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // String message = "criar;1";
            // String message = "depositar;1;2000";
            // String message = "criar;2";
            // String message = "depositar;2;3000";
            // String message = "transferir;1;2;530";
            String message = "cliente:criar;3";
            var receivedMessage = message;
            
            if (receivedMessage != null) {

                var receivedMessageIsolada = receivedMessage.split(":");
                receivedMessage = receivedMessageIsolada[1];

            }

            //System.out.println(" mensagem recebida sem cabeçalho " + receivedMessage);

            // StringRequest request = new StringRequest();
            // request.setData(message.getBytes());
            // request.setOrigemAddress("localhost");
            // request.setOrigemPort(8080);

            out.println(message);
            System.out.println("Mensagem enviada para o servidor TCP.");

            // Receber a resposta do servidor
            System.out.println("Resposta do servidor TCP completa: ");

            in.lines().forEach(System.out::println);
            //String receivedMessages = in.readLine();
          //  System.out.println("Resposta do servidor TCP: " + receivedMessages);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
