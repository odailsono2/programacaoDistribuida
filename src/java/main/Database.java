

import java.io.IOException;
import java.util.List;

import Patterns.WAL.LogEntry;
import Patterns.WAL.WriteAheadLog;

public class Database {
    private WriteAheadLog wal;

    public Database(String logFileName) {
        this.wal = new WriteAheadLog(logFileName);
    }

    // Simula uma operação de atualização
    public void update(String data) throws IOException {
        // Cria uma nova entrada de log
        LogEntry entry = new LogEntry("UPDATE", data);
        wal.append(entry);
        // Aqui você aplicaria a alteração no banco de dados real.
        System.out.println("Aplicando: " + data);
    }

    // Lê todas as entradas do log
    public void recover() throws IOException, ClassNotFoundException {
        List<LogEntry> entries = wal.readEntries();
        System.out.println("Recuperando entradas do log:");
        for (LogEntry entry : entries) {
            System.out.println(entry);
            // Aqui você aplicaria as alterações de volta ao banco de dados.
        }
    }

    public static void main(String[] args) {
        try {
            Database db = new Database("src/java/main/WAL/write_ahead_log.dat");

            // Atualiza alguns dados
            db.update("Alterar saldo de cliente 1");
            db.update("Adicionar cliente 2");
            db.update("Adicionar cliente 4");
            // Recupera entradas do log
            db.recover();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

