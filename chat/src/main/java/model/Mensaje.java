package model;

import java.io.Serializable;

public class Mensaje implements Serializable {
	
	protected static final long serialVersionUID = 1112122200L;

	// The different types of message sent by the Client
	// WHOISIN to receive the list of the users connected
	// MESSAGE an ordinary message
	// LOGOUT to disconnect from the Servidor
	public static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2, FILE = 3, REMOVE = 4;
	private int type;
	private String message;
	
	// constructor
	public Mensaje(int type, String message) {
		this.type = type;
		this.message = message;
	}
	
	// getters
	public int getType() {
		return type;
	}
	public String getMessage() {
		return message;
	}

}
