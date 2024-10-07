package Patterns.WAL;

import java.io.Serializable;

public class LogEntry implements Serializable {
    private static final long serialVersionUID = 1L; // Defina uma versão
    private String key;
    private String data;

    public LogEntry(String key, String data) {
        this.key = key;
        this.data = data;
    }

    public String getOperation() {
        return key;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return key + ": " + data;
    }
}

