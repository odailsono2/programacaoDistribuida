
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class serverTestes3RWL{

	public static void main(String[] args) {

		ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

		// Criar threads virtuais para TCP e UDP

		serverTestes1RWL server = new serverTestes1RWL(8083);

		var udpFuture1 = virtualThreadExecutor.submit(() -> {
			try {
				server.inicializarUDP();
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		});

		var tcpFuture1 = virtualThreadExecutor.submit(() -> server.inicializarTCP());

		// Verificar se a tarefa foi concluída e capturar exceções
		try {
			udpFuture1.get(); // Bloqueia até que a tarefa seja concluída
			tcpFuture1.get();
		} catch (InterruptedException | ExecutionException e) {
			System.out.println(e.getMessage()); // Captura e imprime qualquer erro
		}

	}

}
