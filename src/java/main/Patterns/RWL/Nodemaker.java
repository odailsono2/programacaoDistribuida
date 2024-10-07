package Patterns.RWL;


public class Nodemaker {

    Node node;

    public Nodemaker(){
        node = new Node();
    }

    public Nodemaker setAddress(String address) {
        node.setAdress(address);

        return this;
    }

    public Nodemaker setPort(int port) {
        node.setPort(port);
        return this;
    }

    public Node buideNode(){
        return node;
    }

}
