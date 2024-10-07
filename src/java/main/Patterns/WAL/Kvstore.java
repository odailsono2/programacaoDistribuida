package Patterns.WAL;

import java.util.HashMap;
import java.util.Map;

public class Kvstore {
    WriteAheadLog wal = new WriteAheadLog("src/java/main/WAL/data/log.dat");

    Map <String,String> kv= new HashMap<>();

    Kvstore(){

    }
    

    public String get(String key) {
        return kv.get(key);
    }

    public void put(String key, String value) {
        appendLog(key, value);
        kv.put(key, value);
    }

    public void appendLog(String key, String value){
        wal.append(new LogEntry(key,value));
    }

    public static void main(String[] args) {
        Kvstore kv = new Kvstore();

        kv.appendLog("1", "set");
    }

}
