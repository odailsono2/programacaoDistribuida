package testes;

import java.util.List;
public class Main {
    public static void main(String[] args) {
        ObjectLogManager logManager = new ObjectLogManager("src/java/main/testes/people.log");

        // Adiciona objetos ao log
        logManager.appendObject(new Person("Alice", 30));
        logManager.appendObject(new Person("Bob", 25));
        logManager.appendObject(new Person("Charlie", 35));

        // Lê objetos do log
        System.out.println("Objetos lidos do log:");
        List<Person> people = logManager.readObjects();
        for (Person person : people) {
            System.out.println(person);
        }
    }
}


