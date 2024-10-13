import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InicializaServidores {

    private final AtomicInteger numInicializacoes = new AtomicInteger(0);  // Variável atômica

    List<serverTestes1RWL> servidores = new ArrayList<>();

    InicializaServidores() {
        int numServidores = 3;

        // Executor com threads virtuais
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        List<Future<?>> servers = new ArrayList<>();

        // Criar instâncias de servidores com portas diferentes
        for (int i = 0; i < numServidores; i++) {
            servidores.add(new serverTestes1RWL(8081 + i));
            System.out.println("Servidor criado na porta: " + servidores.get(i).porta);
        }

        // Iniciar os servidores em threads separadas
        for (int i = 0; i < numServidores; i++) {
            int index = i;  // Capturar a variável 'i' para a lambda
            Future<?> server = executor.submit(() -> {
                int initNumber = numInicializacoes.incrementAndGet();  // Incrementar atômicamente
                System.out.println("Inicializando servidor " + initNumber + " na porta " + servidores.get(index).porta);
                try {
                    servidores.get(index).inicializarTCP();  // Inicializar o servidor
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            servers.add(server);
        }

        // Aguardar a conclusão de todas as threads
        for (Future<?> serv : servers) {
            try {
                serv.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Fechar o executor para liberar recursos
        executor.shutdown();
    }

    public static void main(String[] args) {
        new InicializaServidores();
    }
}
