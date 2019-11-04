package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

public class ServidorController {
	@FXML
	private TextArea txtAreaChatMsg;
	@FXML
	private TextArea txtAreaEventLog;
	@FXML
	private ListView<String> listUsersConnected;
	@FXML
	private Button btnStartServer;
	@FXML
	private Button btnStopServer;

	public Servidor servidor;

	private ObservableList<String> users;

	public void startServer() {
		// create a new Servidor
		servidor = new Servidor(1500, this);
		users = FXCollections.observableArrayList();
		listUsersConnected.setItems(users);
		new ServerRunning().start();
		btnStartServer.setDisable(true);
		btnStopServer.setDisable(false);
	}

	public void stopServer() {
		if(servidor != null) {
			servidor.stop();
			btnStopServer.setDisable(true);
			btnStartServer.setDisable(false);
			listUsersConnected.setItems(null);
			servidor = null;
			return;
		}
	}

	/*
	 * A thread to run the Servidor
	 */
	class ServerRunning extends Thread {
		public void run() {
			servidor.start();         // should execute until if fails
			// the servidor failed
			appendEvent("Servidor Stopped\n");
			servidor = null;
			users = null;
		}
	}

	public void addUser(final String user) {
		Platform.runLater(new Runnable() {
			public void run() {
				users.add(user);
			}
		});
	}
	public void appendEvent(String string) {
		txtAreaEventLog.appendText(string);
	}

	public void appendRoom(String messageLf) {
		txtAreaChatMsg.appendText(messageLf);
	}

	public void remove(final String username) {
		Platform.runLater(new Runnable() {
			public void run() {
				users.remove(username);
			}
		});
	}
}
