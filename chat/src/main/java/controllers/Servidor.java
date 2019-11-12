package controllers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import model.Mensaje;

public class Servidor {
	// a unique ID for each connection
	private static int uniqueId;
	// an ArrayList to keep the list of the Client
	private ArrayList<ClientThread> clientsConnected;
	// if I am in a GUI
	private ServidorController servidorController;
	// to display time
	private SimpleDateFormat sdf;
	// the port number to listen for connection
	private int port;
	// the boolean that will be turned of to stop the servidor
	private boolean keepGoing;

	/*
	 * servidor constructor that receive the port to listen to for connection as
	 * parameter
	 */
	public Servidor(int port) {
		this(port, null);
	}

	public Servidor(int port, ServidorController servidorController) {
		// GUI or not
		this.servidorController = servidorController;
		// the port
		this.port = port;
		// to display hh:mm:ss
		sdf = new SimpleDateFormat("HH:mm:ss");
		// ArrayList for the Client list
		clientsConnected = new ArrayList<ClientThread>();
	}

	public void start() {
		keepGoing = true;
		/* create socket servidor and wait for connection requests */
		try {
			// the socket used by the servidor
			ServerSocket serverSocket = new ServerSocket(port);

			// infinite loop to wait for connections
			while (keepGoing) {
				// format message saying we are waiting
				display("Servidor waiting for Clients on port " + port + ".");

				Socket socket = serverSocket.accept(); // accept connection
				// if I was asked to stop
				if (!keepGoing)
					break;
				ClientThread t = new ClientThread(socket); // make a thread of it
				clientsConnected.add(t); // save it in the ArrayList
				t.start();
			}
			// I was asked to stop
			try {
				serverSocket.close();
				for (int i = 0; i < clientsConnected.size(); ++i) {
					ClientThread tc = clientsConnected.get(i);
					try {
						tc.sInput.close();
						tc.sOutput.close();
						tc.socket.close();
					} catch (IOException ioE) {
						// not much I can do
					}
				}
			} catch (Exception e) {
				display("Exception closing the servidor and clients: " + e);
			}
		}
		// something went bad
		catch (IOException e) {
			String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}

	/*
	 * For the GUI to stop the servidor
	 */
	public void stop() {
		keepGoing = false;
		// connect to myself as Client to exit statement
		// Socket socket = serverSocket.accept();
		try {
			new Socket("localhost", port);
		} catch (Exception e) {
			// nothing I can really do
		}
	}

	/*
	 * Display an event (not a message) to the GUI
	 */
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		servidorController.appendEvent(time + "\n");
	}

	/*
	 * to broadcast a message to all Clients
	 */
	private synchronized void broadcast(String message) {
		// add HH:mm:ss and \n to the message
		String time = sdf.format(new Date());
		String messageLf;
		if (message.contains("WHOISIN") || message.contains("REMOVE")) {
			messageLf = message;
		} else {
			messageLf = time + " " + message + "\n";
			servidorController.appendRoom(messageLf); // append in the room window
		}

		// we loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for (int i = clientsConnected.size(); --i >= 0;) {
			ClientThread ct = clientsConnected.get(i);
			// try to write to the Client if it fails remove it from the list
			if (!ct.writeMsg(messageLf)) {
				clientsConnected.remove(i);
				servidorController.remove(ct.username);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}

	// for a client who logoff using the LOGOUT message
	synchronized void remove(int id) {
		// scan the array list until we found the Id
		for (int i = 0; i < clientsConnected.size(); ++i) {
			ClientThread ct = clientsConnected.get(i);
			// found it
			if (ct.id == id) {
				servidorController.remove(ct.username);
				ct.writeMsg(ct.username + ":REMOVE");
				clientsConnected.remove(i);
				return;
			}
		}
	}

	/** One instance of this thread will run for each client */
	class ClientThread extends Thread {
		// the socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// my unique id (easier for deconnection)
		int id;
		// the Username of the Client
		String username;
		// the only type of message a will receive
		Mensaje cm;
		// the date I connect
		String date;

		// Constructore
		ClientThread(Socket socket) {
			// a unique id
			id = ++uniqueId;
			this.socket = socket;
			/* Creating both Data Stream */
			System.out.println("Thread trying to create Object Input/Output Streams");
			try {
				// create output first
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
				servidorController.addUser(username);
				System.out.println(username);
				broadcast(username + ":WHOISIN"); // Broadcast user who logged in
				writeMsg(username + ":WHOISIN");
				for (ClientThread client : clientsConnected) {
					writeMsg(client.username + ":WHOISIN");
				}
				display(username + " just connected.");
			} catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			// have to catch ClassNotFoundException
			// but I read a String, I am sure it will work
			catch (ClassNotFoundException e) {
			}
			date = new Date().toString() + "\n";
		}

		// what will run forever
		public void run() {
			// to loop until LOGOUT
			boolean keepGoing = true;
			while (keepGoing) {
				// read a String (which is an object)
				try {
					cm = (Mensaje) sInput.readObject();
				} catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;
				} catch (ClassNotFoundException e2) {
					break;
				}
				// the messaage part of the ChatMessage
				String message = cm.getMessage();

				// Switch on the type of message receive
				switch (cm.getType()) {

				case Mensaje.MESSAGE:
					broadcast(username + ": " + message);
					break;
				case Mensaje.FILE:
					FileInputStream fis = null;
					BufferedInputStream bis = null;
					OutputStream os = null;

					System.out.println("File");

					display(cm.getFile().getName());

					try {
						File archivo = cm.getFile();
						byte[] ab = new byte[(int) archivo.length()]; // se crea un array de bytes
						fis = new FileInputStream(archivo);
						bis = new BufferedInputStream(fis);
						bis.read(ab, 0, ab.length);
						os = socket.getOutputStream();
						os.write(ab, 0, ab.length);
						os.flush();
						broadcast(username + ":FILE" + ":" + archivo.getName() + ":" + archivo.length());
					} catch (IOException e) {
						display(username + " Exception reading Streams: " + e);
					}
					break;
				case Mensaje.LOGOUT:
					display(username + " disconnected with a LOGOUT message.");
					broadcast(username + ":REMOVE");
					keepGoing = false;
					break;
				}
			}
			// remove myself from the arrayList containing the list of the
			// connected Clients
			remove(id);
			close();
		}

		// try to close everything
		private void close() {
			// try to close the connection
			try {
				if (sOutput != null)
					sOutput.close();
			} catch (Exception e) {
			}
			try {
				if (sInput != null)
					sInput.close();
			} catch (Exception e) {
			}
			;
			try {
				if (socket != null)
					socket.close();
			} catch (Exception e) {
			}
		}

		/*
		 * Write a String to the Client output stream
		 */
		private boolean writeMsg(String msg) {
			// if Client is still connected send the message to it
			if (!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch (IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}
}