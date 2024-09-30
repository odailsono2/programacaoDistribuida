package testes;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class ObjectLogManager {
    private String filePath;

    public ObjectLogManager(String filePath) {
        this.filePath = filePath;
    }

    // Adiciona um objeto ao arquivo de log
    public void appendObject(Person person) {
        try {
            // Verifica se o arquivo já existe
            boolean fileExists = new File(filePath).exists();

            // Se o arquivo não existir, cria um novo ObjectOutputStream
            try (FileOutputStream fos = new FileOutputStream(filePath, true);
                 ObjectOutputStream oos = fileExists ? new AppendableObjectOutputStream(fos) : new ObjectOutputStream(fos)) {
                oos.writeObject(person);
                System.out.println("Objeto adicionado ao log: " + person);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Lê todos os objetos do arquivo de log
    public List<Person> readObjects() {
        List<Person> people = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            while (true) {
                Person person = (Person) ois.readObject();
                people.add(person);
            }
        } catch (EOFException e) {
            // Fim do arquivo
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return people;
    }
}
