import java.io.*;
import java.net.*;

public class TCPClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 8080);

            // Enviar mensagem ao servidor TCP
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //String message = "criar;1";
            //String message = "depositar;1;2000";
            //String message = "criar;2";
            // String message = "depositar;2;3000";
            // String message = "transferir;1;2;530";
             String message = "criar;1";

            out.println(message);
            System.out.println("Mensagem enviada para o servidor TCP.");

            // Receber a resposta do servidor
            String receivedMessage = in.readLine();
            System.out.println("Resposta do servidor TCP: " + receivedMessage);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
