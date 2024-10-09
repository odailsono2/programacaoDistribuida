import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HttpClient {

    public static void main(String[] args) {
        try {
            // Conecta ao servidor na porta 8080
            Socket socket = new Socket("localhost", 8080);

            // Envia uma requisição HTTP GET ao servidor
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("GET /criar;3 HTTP/1.1");
            writer.println("Host: localhost");
            writer.println("Connection: close");
            writer.println(""); // Linha em branco para indicar o fim dos cabeçalhos

            // Lê e exibe a resposta do servidor
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                System.out.println(responseLine); // Exibe a resposta no console
            }

            // Fecha as streams e o socket
            writer.close();
            in.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
