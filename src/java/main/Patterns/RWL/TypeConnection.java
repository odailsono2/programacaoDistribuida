package Patterns.RWL;

public enum TypeConnection{
	TCP("TCP"),
	UDP("UDP");

	private String valor;

	TypeConnection(String valor){
		this.valor = valor;
	}

	public String getValor() {
		return valor;
	}
}