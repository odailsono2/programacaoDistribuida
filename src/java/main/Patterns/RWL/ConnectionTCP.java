package Patterns.RWL;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionTCP implements AutoCloseable {

	private Socket socket;

	private ConnectionTCP() {
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocketFrom(Socket socket) throws Exception {

		if (socket == null) {

			throw new Exception("Null socket!");

		}

		this.socket = socket;
		// setAddress(socket.getInetAddress());
		// setPort(socket.getPort());
	}

	public void makeSocket() throws Exception {

		if (socket == null) {
			setSocketFrom(new Socket(socket.getInetAddress(), socket.getPort()));
			return;
		}

	}

	public byte[] receiveData() throws Exception {

		if (socket == null) {
			throw new Exception("Connection.receiveData: socket is null");
		}

		InputStream input = new BufferedInputStream(socket.getInputStream());

		byte[] buffer = new byte[1024];

		var bytesRead = input.read(buffer);

		var arraBytes = new ByteArrayInputStream(buffer, 0, bytesRead);

		var mensagemRecebidaBytes = arraBytes.readAllBytes();

		return mensagemRecebidaBytes;

	}

	public <Tobject> Tobject receiveObject(Class<Tobject> classe) throws Exception {

		var obj = receiveData();

		ByteArrayInputStream bais = new ByteArrayInputStream(obj);

		ObjectInputStream ois = new ObjectInputStream(bais);

		var objeto = classe.cast(ois.readObject());

		return objeto;

	}

	private void sendViaTCP(byte[] data) throws IOException {

		// System.out.println(new String(reply));

		ByteArrayOutputStream replyByteArrayOutputStream = new ByteArrayOutputStream();

		replyByteArrayOutputStream.write(data);

		BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

		replyByteArrayOutputStream.writeTo(out);

		// out.write(replyByteArrayOutputStream.toByteArray());

		out.flush();

		// out.close();

	}

	public <Tobject extends Serializable> void sendObject(Tobject obj) throws Exception {

		// ---serializar
		ByteArrayOutputStream objSerializado = new ByteArrayOutputStream();
		ObjectOutputStream output = new ObjectOutputStream(objSerializado);
		output.writeObject(obj);

		send(objSerializado.toByteArray());

	}

	public void send(byte[] reply) throws Exception {

		sendViaTCP(reply);

	}

	public static class Builder {

		ConnectionTCP novaConnection;
		TypeConnection typeConnection;
		InetAddress address;
		int port;
		Socket socketBuid;

		public Builder() {
			novaConnection = new ConnectionTCP();
		}

		public Builder setAddress(InetAddress address) {
			this.address = address;
			// novaConnection.setAddress(address);
			return this;
		}

		public Builder setPort(int port) {
			this.port = port;
			// novaConnection.setPort(port);
			return this;
		}

		public Builder setSocketFromOtherSocket(Socket socket) throws Exception {

			if (socket == null) {

				throw new Exception("Builder.setSocketFromOtherSocket: socket passado no parametro é nulo");

			}

			if (novaConnection.socket == null) {

				novaConnection.socket = socket;

				return this;
			}
			throw new Exception("socket já existente");
		}

		public ConnectionTCP build() throws Exception {

			if (novaConnection.socket == null) {

				var socketTCP = new Socket(address, port);
				novaConnection.setSocketFrom(socketTCP);

			}
			return novaConnection;

		}

	}

	@Override
	public void close() throws Exception {
		socket.close();
	}

	public static void main(String[] args) {

		int porta = 8080;

		String address = "localhost";

		new Thread(() -> {

			System.out.println("TCP - Abrindo Conexão TCP no servidor");
			try (ServerSocket socket = new ServerSocket(porta)) {

				Socket cliente = socket.accept();

				var conexaoCliente = new ConnectionTCP.Builder().setSocketFromOtherSocket(cliente)
						.build();

				// -----Exemplo recebimento e envio de objeto pelo servidor

				PessoaTCP obj = conexaoCliente.receiveObject(PessoaTCP.class);

				System.out.println("TCP: Objeto recebido: " + obj);

				conexaoCliente.sendObject(new PessoaTCP(obj.getNome(), obj.getIdade(), "ServidorTCP"));

				// ----------------------------------------------------

				// -----Exemplo de recebimento e envio de String pelo servidor

				// var mensagem = new String(conexaoCliente.receiveData());

				// var saidaString = "mensagem recebida do cliente: " + new String(mensagem);

				// conexaoCliente.send(saidaString.getBytes());

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();

		new Thread(() -> {

			System.out.println("TCP (String)- Abrindo Conexão TCP no servidor");
			try (ServerSocket socket = new ServerSocket(8081)) {

				Socket cliente = socket.accept();

				var conexaoCliente = new ConnectionTCP.Builder().setSocketFromOtherSocket(cliente)
						.build();

				// -----Exemplo recebimento e envio de objeto pelo servidor

				var mensagemRecebida = new String(conexaoCliente.receiveData());

				System.out.println("TCP servidor - mensagem recebida: : " + mensagemRecebida);

				conexaoCliente.send("Mensagem recebida do cliente".getBytes());

				// ----------------------------------------------------

				// -----Exemplo de recebimento e envio de String pelo servidor

				// var mensagem = new String(conexaoCliente.receiveData());

				// var saidaString = "mensagem recebida do cliente: " + new String(mensagem);

				// conexaoCliente.send(saidaString.getBytes());

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();

		new Thread(() -> {

			var pessoa = new PessoaTCP("Odailson", 39, "Cliente");

			System.out.println("TCP : Abrindo conexão cliente");

			try (ConnectionTCP cliente = new ConnectionTCP.Builder()
					.setAddress(InetAddress.getByName(address))
					.setPort(porta).build()) {

				cliente.makeSocket();

				// --- teste de envio de objeto

				System.out.println("TCP: Envio do objeto" + pessoa);

				cliente.send(pessoa.toByteArray());

				cliente.sendObject(pessoa);

				var objrecebido = cliente.receiveObject(PessoaTCP.class);

				System.out.println("TCP- cliente - Objeto recebido: " + objrecebido);

				// ---------Teste de envio de mensagem

				// var mensagem = "Ola Vc";

				// cliente.send(mensagem.getBytes());

				// System.out.println("Cliente TCP : mensagem enviada para servidor: " +
				// mensagem);

				// var mensagemRecebidaDoServidor = cliente.receiveData();

				// System.out.println(
				// "Cliente TCP: mensagem recebida do servidor " + new
				// String(mensagemRecebidaDoServidor));

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
		new Thread(() -> {

			var pessoa = new PessoaTCP("Odailson", 39, "Cliente");

			System.out.println("TCP (String): Abrindo conexão cliente");

			try (ConnectionTCP cliente = new ConnectionTCP.Builder()
					.setAddress(InetAddress.getByName(address))
					.setPort(8081).build()) {

				cliente.makeSocket();

				// --- teste de envio de objeto

				cliente.send("testando envio string".getBytes());

				var respostaServidor = new String(cliente.receiveData());

				System.out.println("TCP- cliente - reposta recebida: " + respostaServidor);

				// ---------Teste de envio de mensagem

				// var mensagem = "Ola Vc";

				// cliente.send(mensagem.getBytes());

				// System.out.println("Cliente TCP : mensagem enviada para servidor: " +
				// mensagem);

				// var mensagemRecebidaDoServidor = cliente.receiveData();

				// System.out.println(
				// "Cliente TCP: mensagem recebida do servidor " + new
				// String(mensagemRecebidaDoServidor));

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();

	}

}

class PessoaTCP implements Serializable {
	private String nome;
	private int idade;
	private String remetente;

	public PessoaTCP(String nome, int idade, String remetente) {
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
		return "PessoaTCP [nome=" + nome + ", idade=" + idade + ", remetente=" + remetente + "]";
	}

}