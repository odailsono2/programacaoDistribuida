package Patterns.WAL;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class WriteAheadLog {
    private String logFilePath;

    public WriteAheadLog(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    // Grava uma entrada no log
    public void append(LogEntry entry) {
        try {
            // Verifica se o arquivo já existe
            boolean fileExists = new File(logFilePath).exists();

            // Se o arquivo não existir, cria um novo ObjectOutputStream
            try (FileOutputStream fos = new FileOutputStream(logFilePath, true);
                 ObjectOutputStream oos = fileExists ? new AppendableObjectOutputStream(fos) : new ObjectOutputStream(fos)) {
                oos.writeObject(entry);
                System.out.println("Objeto adicionado ao log: " + entry);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    // Lê todas as entradas do log
    // Lê todos os objetos do arquivo de log
    public List<LogEntry> readEntries() {
        List<LogEntry> entries = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(logFilePath))) {
            while (true) {
                LogEntry person = (LogEntry) ois.readObject();
                entries.add(person);
            }
        } catch (EOFException e) {
            // Fim do arquivo
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return entries;
    }
}

// Classe para adicionar o suporte de append em ObjectOutputStream
class AppendableObjectOutputStream extends ObjectOutputStream {
    public AppendableObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    protected void writeStreamHeader() throws IOException {
        // Não escrever o cabeçalho para permitir anexar ao arquivo existente
        reset();
    }
}

