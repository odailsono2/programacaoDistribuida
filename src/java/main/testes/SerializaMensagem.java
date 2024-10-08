package testes;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializaMensagem<T>{
    ByteArrayOutputStream byteArrayOutputStream;

    SerializaMensagem(){
        byteArrayOutputStream = new ByteArrayOutputStream();
    }


    public ByteArrayOutputStream serializar(T message){
    
        ByteArrayOutputStream byteArrayOutputStream = null;

        try{     
            // Serializar o objeto para um array de bytes
            byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(byteArrayOutputStream));
            objectOutputStream.writeObject(message);  // Serializa o objeto
            objectOutputStream.flush();  // Garante que todos os dados sejam gravados
            
            objectOutputStream.close();

        }catch(Exception e){
            e.printStackTrace();
        }

        return byteArrayOutputStream;

    }

    public void close(){
        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public Object deserializar(byte[] objSerializado){
    
        Object req = null;

        try{     
            ByteArrayInputStream byteArrayIntputStream = new ByteArrayInputStream(objSerializado);
            ObjectInputStream objStream = new ObjectInputStream(new BufferedInputStream(byteArrayIntputStream));
            req = objStream.readObject();
            objStream.close();

        }catch(Exception e){
            e.printStackTrace();
        }

        return req;

    }



}