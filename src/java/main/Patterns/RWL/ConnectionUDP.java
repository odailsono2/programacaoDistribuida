package Patterns.RWL;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ConnectionUDP implements AutoCloseable {

	private DatagramSocket datagramSocket;
	private DatagramPacket receivDatagramPacket;

	private ConnectionUDP() {
	}

	public DatagramSocket getDatagramSocket() {
		return datagramSocket;
	}

	public DatagramPacket getReceivDatagramPacket() {
		return receivDatagramPacket;
	}

	public void setDatagramSocket(DatagramSocket datagramSocket) throws Exception {
		if (datagramSocket != null) {
			this.datagramSocket = datagramSocket;
		} else {
			throw new Exception("datagramsScket is null");
		}
	}

	public void setSocketFrom(DatagramSocket datagramSocket) throws Exception {

		if (datagramSocket == null) {

			throw new Exception("ConnectionUDP.setSocketFrom: Null socket!");

		}

		this.datagramSocket = datagramSocket;
		// setAddress(socket.getInetAddress());
		// setPort(socket.getPort());
	}

	public void makeSocket() throws Exception {
		if (datagramSocket == null) {
			setDatagramSocket(new DatagramSocket());
			return;
		}
		throw new Exception("DatagramSocket is null");
	}

	public byte[] receiveData() throws Exception {

		if (datagramSocket == null) {
			throw new Exception("Connection.receiveData: socket is null");
		}

		byte[] buffer = new byte[1024];

		receivDatagramPacket = new DatagramPacket(buffer, buffer.length);

		datagramSocket.receive(receivDatagramPacket);

		var arraBytes = new ByteArrayInputStream(receivDatagramPacket.getData());

		return arraBytes.readAllBytes();

	}

	public <Tobject> Tobject receiveObject(Class<Tobject> classe) throws Exception {

		var obj = receiveData();

		ByteArrayInputStream bais = new ByteArrayInputStream(obj);

		ObjectInputStream ois = new ObjectInputStream(bais);

		var objeto = classe.cast(ois.readObject());

		return objeto;

	}

	public void sendViaUDP(byte[] data, DatagramPacket datagramPacket) throws Exception {

		if (datagramSocket != null) {

			datagramSocket.send(datagramPacket);
		} else {
			throw new Exception("Connection.UDP::sendViaUDP(): datagram socket null");
		}
	}

	public <Tobject extends Serializable> void sendObject(Tobject obj, InetAddress address, int porta)
			throws Exception {

		// ---serializar
		ByteArrayOutputStream objSerializado = new ByteArrayOutputStream();
		ObjectOutputStream output = new ObjectOutputStream(objSerializado);
		output.writeObject(obj);

		byte[] buffer = new byte[1024];
		buffer = objSerializado.toByteArray();
		DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, address, porta);

		sendViaUDP(objSerializado.toByteArray(), datagramPacket);

	}

	public static class Builder {

		ConnectionUDP novaConnection;

		public Builder() {
			novaConnection = new ConnectionUDP();
		}

		public Builder setDatagramSocketFromOther(DatagramSocket datagramSocket) throws Exception {

			if (datagramSocket != null) {
				this.novaConnection.setDatagramSocket(datagramSocket);
				return this;
			}

			throw new Exception("DatagramSocket is null");
		}

		public Builder setSocketFromOtherSocket(DatagramSocket datagramSocket) throws Exception {

			if (novaConnection.datagramSocket == null) {

				novaConnection.datagramSocket = datagramSocket;

				return this;
			}
			throw new Exception("socket já existente");
		}

		public ConnectionUDP build() throws Exception {

			if (novaConnection.datagramSocket == null) {

				var socketUDP = new DatagramSocket();
				novaConnection.setSocketFrom(socketUDP);

			}
			return novaConnection;

		}

	}

	@Override
	public void close() throws Exception {
		datagramSocket.close();
	}

	public static void main(String[] args) {

		int porta = 8080;

		String address = "localhost";

		new Thread(() -> {

			System.out.println("Abrindo conexão com servidor UDP");

			try (DatagramSocket socketUDP = new DatagramSocket(porta)) {

				// byte[] receiveMessage = new byte[1024];

				// DatagramPacket receivePacket = new DatagramPacket(receiveMessage,
				// receiveMessage.length);

				// socketUDP.receive(receivePacket);

				// ByteArrayInputStream objByteArray = new
				// ByteArrayInputStream(receivePacket.getData(), 0,
				// receivePacket.getLength());
				// ObjectInputStream objRecebido = new ObjectInputStream(new
				// BufferedInputStream(objByteArray));

				// var obj = (Pessoa) objRecebido.readObject();

				ConnectionUDP datagramSocket = new ConnectionUDP.Builder().setDatagramSocketFromOther(socketUDP)
						.build();

						PessoaExemplo obj = datagramSocket.receiveObject(PessoaExemplo.class);

				System.out.println("----Servidor UDP recebido:" + obj);

				obj = new PessoaExemplo(obj.getNome(), obj.getIdade(), "ServidorUDP");

				System.out.println("----Servidor UDP enviado:" + obj);

				datagramSocket.sendObject(obj, datagramSocket.getReceivDatagramPacket().getAddress(),
						datagramSocket.getReceivDatagramPacket().getPort());

				// ---enviando mensagem
				// var mensagemRecebida = receivePacket.getData();

				// System.out.println(new String(mensagemRecebida));

			} catch (Exception e) {
				// TODO: handle exception
			}

		}).start();

		new Thread(() -> {

			System.out.println("Iniciando cliente UDP");

			try (ConnectionUDP clienteUDP = new ConnectionUDP.Builder().setDatagramSocketFromOther(new DatagramSocket())
					.build()) {

				var pessoa = new PessoaExemplo("OdailsonUDP", 39, "clienteUDP");

				clienteUDP.sendObject(pessoa, InetAddress.getByName("localhost"), 8080);

				var obj = clienteUDP.receiveObject(PessoaExemplo.class);

				System.out.println("---cliente UDP recebeu :" + obj);

			} catch (Exception e) {

				e.printStackTrace();

			}

			// try (DatagramSocket datagramSocket = new DatagramSocket()) {

			// // var mensagemEnviada = "---- cliente: \r\n ola vc via UDP";

			// // byte[] buffer = new byte[1024];
			// // buffer = mensagemEnviada.getBytes();

			// Pessoa pessoa = new Pessoa("OdailsonUDP", 39, "Cliente");

			// ByteArrayOutputStream objByteArray = new ByteArrayOutputStream();
			// ObjectOutputStream obj = new ObjectOutputStream(new
			// BufferedOutputStream(objByteArray));
			// obj.writeObject(pessoa);

			// byte[] buffer = objByteArray.toByteArray();

			// try {

			// DatagramPacket datagramPacketEnivado = new DatagramPacket(buffer,
			// buffer.length,
			// InetAddress.getByName(address), porta);

			// // obj.writeObject(pessoa);
			// // obj.flush();

			// System.out.println("----UDP Enviando obj: " + pessoa);

			// datagramSocket.send(datagramPacketEnivado);

			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			// } catch (SocketException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (IOException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }

		}).start();

	}

}

class PessoaExemplo implements Serializable {
	private String nome;
	private int idade;
	private String remetente;

	public PessoaExemplo(String nome, int idade, String remetente) {
		this.nome = nome;
		this.idade = idade;
		this.remetente = remetente;
	}

	public String getRemetente() {
		return remetente;
	}

	public String getNome() {
		return nome;
	}

	public int getIdade() {
		return idade;
	}

	public byte[] toByteArray() throws IOException {
		// Serializar o objeto para bytes com ByteArrayOutputStream
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(this);
		oos.flush();

		return baos.toByteArray();
	}

	@Override
	public String toString() {
		return "Pessoa [nome=" + nome + ", idade=" + idade + ", remetente=" + remetente + "]";
	}

}