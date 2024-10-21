package Patterns.RWL;

import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnnectionsPool {

    private BlockingQueue<Socket> pool;
    private String host;
    private int port;
    private int tamanhoMaximo;

    ConnnectionsPool(String host, int port, int tamanhoMaximo) {
        this.host = host;
        this.port = port;
        this.tamanhoMaximo = tamanhoMaximo;
        pool = new LinkedBlockingQueue<>(tamanhoMaximo);
    }

    // Inicializa o pool com o número máximo de conexões
    public void inicializar() throws Exception {
        for (int i = 0; i < tamanhoMaximo; i++) {
            pool.add(criarNovaConexao());
        }
    }

    // Obtém uma conexão do pool
    public Socket obterConexao() throws InterruptedException {
        return pool.take(); // Bloqueia até ter uma conexão disponível
    }

    // Devolve a conexão ao pool
    public void devolverConexao(Socket socket) {
        if (socket != null) {
            pool.offer(socket);
        }
    }

    // Cria uma nova conexão
    private Socket criarNovaConexao() throws Exception {
        return new Socket(host, port);
    }

    // Fechar todas as conexões
    public void fecharConexoes() {
        for (Socket socket : pool) {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        ConnnectionsPool pool = new ConnnectionsPool("localhost", 8080, 5);
        try {
            // Inicializar o pool de conexões

            System.out.println("Pool de conexões inicializado.");

            // Obter uma conexão
            Socket conexao = pool.obterConexao();
            System.out.println("Conexão obtida: " + conexao);

            // Devolver a conexão ao pool
            pool.devolverConexao(conexao);
            System.out.println("Conexão devolvida ao pool.");

            // Fechar todas as conexões
            pool.fecharConexoes();
            System.out.println("Todas as conexões fechadas.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
