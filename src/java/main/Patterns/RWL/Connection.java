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
import java.net.*;


public class Connection {


	public static DatagramPacket receiveUDP(DatagramSocket socket) throws IOException {
		byte[] receiveMessage = new byte[1024];

		DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);

		socket.receive(receivePacket);

		return receivePacket;

	}
	public static void sendUDP(byte[] sendData,DatagramSocket socket, String address, int porta) throws IOException{

		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(address),
		porta);
		
		socket.send(sendPacket);

	}

	public static byte[] receiveData(Socket socket) throws Exception {

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

	public static <Tobject extends Serializable> Tobject receiveObject(Class<Tobject> classe, Socket socket) throws Exception {

		var obj = receiveData(socket);

		ByteArrayInputStream bais = new ByteArrayInputStream(obj);

		ObjectInputStream ois = new ObjectInputStream(bais);

		var objeto = classe.cast(ois.readObject());

		return objeto;

	}

	public static void send(byte[] data, Socket socket) throws IOException {

		// System.out.println(new String(reply));

		ByteArrayOutputStream replyByteArrayOutputStream = new ByteArrayOutputStream();

		replyByteArrayOutputStream.write(data);

		BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

		replyByteArrayOutputStream.writeTo(out);

		// out.write(replyByteArrayOutputStream.toByteArray());

		out.flush();

		// out.close();

	}

	public static <Tobject extends Serializable> void sendObject(Tobject obj, Socket socket) throws Exception {

		// ---serializar
		ByteArrayOutputStream objSerializado = new ByteArrayOutputStream();
		ObjectOutputStream output = new ObjectOutputStream(objSerializado);
		output.writeObject(obj);

		send(objSerializado.toByteArray(), socket);

	}

	public static <Tobject extends Serializable> byte[] serializarOBJ(Tobject obj) throws Exception {

		// ---serializar
		ByteArrayOutputStream objSerializado = new ByteArrayOutputStream();
		ObjectOutputStream output = new ObjectOutputStream(objSerializado);
		output.writeObject(obj);

		return objSerializado.toByteArray();

	} 

	public static <Tobject extends Serializable> Tobject deserializarOBJ(Class<Tobject> classe, byte[] obj) throws Exception {

		// var obj = receiveData(socket);

		ByteArrayInputStream bais = new ByteArrayInputStream(obj);

		ObjectInputStream ois = new ObjectInputStream(bais);

		var objeto = classe.cast(ois.readObject());

		return objeto;

	}

	public static void main(String[] args) {

		int porta = 8080;

		String address = "localhost";

		new Thread(() -> {

			System.out.println("UDP - Abrindo Conexão UDP no servidor");
			try (DatagramSocket socket = new DatagramSocket(porta)) {

				// -----Exemplo recebimento e envio de objeto pelo servidor

				DatagramPacket datagramObj= Connection.receiveUDP(socket);

				PessoaConnection obj = Connection.deserializarOBJ(PessoaConnection.class, datagramObj.getData());

				System.out.println("UDP- servidor: Objeto recebido: " + obj);

				byte [] objSerializado = Connection.serializarOBJ(new PessoaConnection(obj.getNome(), obj.getIdade(), "ServidorUDP"));

				Connection.sendUDP(objSerializado, socket, datagramObj.getAddress().getHostAddress(),datagramObj.getPort());

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

			var pessoa = new PessoaConnection("Odailson", 39, "ClienteUDP");

			System.out.println("UDP : Abrindo conexão cliente");

			try (DatagramSocket cliente = new DatagramSocket()) {

				
				// --- teste de envio de objeto

				System.out.println("UDP -cliente: Envio do objeto: " + pessoa);

				var objSerializado =  Connection.serializarOBJ(pessoa);

				Connection.sendUDP(objSerializado, cliente, address, porta);

				// Connection.sendObject(pessoa, cliente);

				var datagramPacket = Connection.receiveUDP(cliente);

				PessoaConnection objrecebido = Connection.deserializarOBJ(PessoaConnection.class, datagramPacket.getData());

				System.out.println("UDP- cliente - Objeto recebido: " + objrecebido);

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

			System.out.println("TCP - Abrindo Conexão TCP no servidor");
			try (ServerSocket socket = new ServerSocket(porta)) {

				Socket cliente = socket.accept();

				// -----Exemplo recebimento e envio de objeto pelo servidor

				PessoaConnection obj = Connection.receiveObject(PessoaConnection.class, cliente);

				System.out.println("TCP: Objeto recebido: " + obj);

				Connection.sendObject(new PessoaConnection(obj.getNome(), obj.getIdade(), "ServidorTCP"), cliente);

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

				var mensagemRecebida = Connection.receiveData(cliente);

				// -----Exemplo recebimento e envio de objeto pelo servidor

				System.out.println("TCP (string) servidor - mensagem recebida: : " + new String(mensagemRecebida));

				Connection.send("Mensagem recebida do cliente".getBytes(), cliente);

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

			var pessoa = new PessoaConnection("Odailson", 39, "Cliente");

			System.out.println("TCP : Abrindo conexão cliente");

			try (Socket cliente = new Socket()) {

				cliente.connect(new InetSocketAddress("localhost", 8080));
				// --- teste de envio de objeto

				System.out.println("TCP cliente: Envio do objeto: " + pessoa);

				Connection.send(pessoa.toByteArray(), cliente);

				// Connection.sendObject(pessoa, cliente);

				var objrecebido = Connection.receiveObject(PessoaConnection.class, cliente);

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

			var pessoa = new PessoaConnection("Odailson", 39, "Cliente");

			System.out.println("TCP (String): Abrindo conexão cliente");

			try (Socket cliente = new Socket()) {

				cliente.connect(new InetSocketAddress("localhost", 8081));

				// --- teste de envio de objeto

				Connection.send("testando envio string".getBytes(), cliente);

				var respostaServidor = new String(Connection.receiveData(cliente));

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

class PessoaConnection implements Serializable {
	private String nome;
	private int idade;
	private String remetente;

	public PessoaConnection(String nome, int idade, String remetente) {
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
		return "PessoaConnection [nome=" + nome + ", idade=" + idade + ", remetente=" + remetente + "]";
	}

}