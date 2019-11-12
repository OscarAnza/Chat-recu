package controllers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Mensaje;

public class ClienteController implements Initializable {
	private @FXML ListView listUser;
	private @FXML Label txtUser;
	private @FXML TextField txtUserMsg;
	private @FXML Circle btnSend;
	private @FXML Circle btnLogout;
	private @FXML ImageView btnArchivo;
	private Stage stageLocal;
	private @FXML TextArea txtAreaServerMsgs;
	private String user;

	private ObservableList<String> users;

	// Servidor Configuracion
	private boolean connected;
	private String server, username;
	private int port;

	// para I/O
	private ObjectInputStream sInput; // para leer desde el socket
	private ObjectOutputStream sOutput; // para escribir desde el socket
	private Socket socket; // socket para conectarse al server

	/**
	 * Metodo usado para logearse
	 */
	public void login() {
		port = 1500;
		server = "localhost";
		// System.out.println(server);
		username = user;
		// test if we can start the connection to the Servidor
		// if it failed nothing we can do
		if (!start())
			return;
		connected = true;
	}

	/**
	 * Method used by btnLogout from Java FX
	 */
	public void logout() {
		if (connected) {
			Mensaje msg = new Mensaje(Mensaje.LOGOUT, "");
			try {
				sOutput.writeObject(msg);
				txtUserMsg.setText("");
				btnLogout.setDisable(false);
			} catch (IOException e) {
				display("Exception writing to servidor: " + e);
			}
		}
	}

	/**
	 * To send a message to the servidor
	 */
	public void sendMessage() {
		if (connected) {
			Mensaje msg = new Mensaje(Mensaje.MESSAGE, txtUserMsg.getText());
			try {
				sOutput.writeObject(msg);
				txtUserMsg.setText("");
			} catch (IOException e) {
				display("Exception writing to servidor: " + e);
			}
		}
	}

	/**
	 * Sends message to servidor Used by TextArea txtUserMsg to handle Enter key
	 * event
	 */
	public void handleEnterPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			sendMessage();
			event.consume();
		}
	}

	public void cerrarVentana() {
		Mensaje msg = new Mensaje(Mensaje.LOGOUT, "");
		try {
			sOutput.writeObject(msg);
			txtUserMsg.setText("");
		} catch (IOException e) {
			display("Exception writing to servidor: " + e);
		}
		disconnect();
	}

	public void sendFile() {
		System.out.println("Enviar archivo");
		if (connected) {
			FileChooser fc = new FileChooser();

			fc.setTitle("Seleccionar archivo");

			// Window stage = null;
			File f = fc.showOpenDialog(stageLocal);

			Mensaje msg = new Mensaje(Mensaje.FILE, f);
			try {
				sOutput.writeObject(msg);
				txtUserMsg.setText("");
			} catch (IOException e) {
				display("Exception writing to servidor: " + e);
			}
		}
	}

	/**
	 * To start the dialog
	 */
	public boolean start() {
		// try to connect to the servidor
		try {
			socket = new Socket(server, port);
		}
		// if it failed not much I can so
		catch (Exception ec) {
			display("Error connectiong to servidor:" + ec);
			return false;
		}

		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);

		/* Creating both Data Stream */
		try {
			sInput = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// creates the Thread to listen from the servidor
		new ClienteController.ListenFromServer().start();
		// Send our username to the servidor this is the only message that we
		// will send as a String. All other messages will be Mensaje objects
		try {
			sOutput.writeObject(username);
		} catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		// success we inform the caller that it worked
		return true;
	}

	/*
	 * To send a message to the console or the GUI
	 */
	private void display(String msg) {
		txtAreaServerMsgs.appendText(msg + "\n"); // append to the ServerChatArea
	}

	/*
	 * When something goes wrong Close the Input/Output streams and disconnect not
	 * much to do in the catch clause
	 */
	private void disconnect() {
		try {
			if (sInput != null)
				sInput.close();
		} catch (Exception e) {
		} // not much else I can do
		try {
			if (sOutput != null)
				sOutput.close();
		} catch (Exception e) {
		} // not much else I can do
		try {
			if (socket != null)
				socket.close();
		} catch (Exception e) {
		} // not much else I can do

		// inform the GUI
		connectionFailed();

	}

	public void connectionFailed() {
		// don't react to a <CR> after the username
		connected = false;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Stage getStageLocal() {
		return stageLocal;
	}

	public void setStageLocal(Stage stageLocal) {
		this.stageLocal = stageLocal;
	}

	public void mandarMensaje(KeyEvent keyEvent) {
		String auxiliarTecla = keyEvent.getCode().toString();
		if (auxiliarTecla == "ENTER") {
			// accion de boton
			sendMessage();
		}
		auxiliarTecla = "";
	}

	/*
	 * a class that waits for the message from the servidor and append them to the
	 * JTextArea if we have a GUI or simply System.out.println() it in console mode
	 */
	class ListenFromServer extends Thread {

		public void run() {
			users = FXCollections.observableArrayList();
			listUser.setItems(users);
			while (true) {
				try {
					String msg = (String) sInput.readObject();
					System.out.println(msg);
					final String[] split = msg.split(":");
					if (split[1].equals("WHOISIN")) {
						Platform.runLater(new Runnable() {
							public void run() {
								users.add(split[0]);
							}
						});
						;
					} else if (split[1].equals("REMOVE")) {
						Platform.runLater(new Runnable() {
							public void run() {
								users.remove(split[0]);
							}
						});
					} else if (split[1].equals("FILE")) {
						int bytesLeidos;
						int actual = 0;
						FileOutputStream fos = null;
						BufferedOutputStream bos = null;

						byte[] ab = new byte[Integer.parseInt(split[3])];
						InputStream is = socket.getInputStream();
						fos = new FileOutputStream(split[2]);
						bos = new BufferedOutputStream(fos);
						bytesLeidos = is.read(ab, 0, ab.length);
						actual = bytesLeidos;

						do {
							bytesLeidos = is.read(ab, actual, (ab.length - actual));
							if (bytesLeidos >= 0)
								actual += bytesLeidos;
						} while (bytesLeidos > -1);

						bos.write(ab, 0, actual);
						bos.flush();
						display("Archivo " + split[2] + " descargado");
					} else {
						txtAreaServerMsgs.appendText(msg);
					}

				} catch (IOException e) {
					display("Servidor has close the connection");
					connectionFailed();
					Platform.runLater(new Runnable() {
						public void run() {
							listUser.setItems(null);
						}
					});
					break;
				}
				// can't happen with a String object but need the catch anyhow
				catch (ClassNotFoundException e2) {

				}
			}
		}
	}

	public void initialize(URL location, ResourceBundle resources) {
		Platform.runLater(new Runnable() {
			public void run() {
				ClienteController.this.login(); // do stuff
				ClienteController.this.txtUser.setText(ClienteController.this.user);
			}
		});
		this.btnSend.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				ClienteController.this.sendMessage();
			}
		});
		this.btnArchivo.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				ClienteController.this.sendFile();
			}
		});
	}
}
