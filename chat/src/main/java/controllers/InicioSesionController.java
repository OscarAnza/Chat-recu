package controllers;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class InicioSesionController implements Initializable {
	private Stage stageLocal;
	@FXML
	private TextField txtUsername;
	private String user;

	public void setPrevStage(Stage prevStage) {
		this.stageLocal = prevStage;
	}

	public void initialize(URL location, ResourceBundle resources) {

	}

	public void conectarse() throws IOException {
		Stage stageChat = new Stage(); // Creacion de nuevo Escenario
		stageChat.setTitle("Chat - Universidad Politecnica"); // Poner su titulo
		FXMLLoader getChat = new FXMLLoader(getClass().getResource("/views/chatPrincipal.fxml")); // Obtener la
																									// informacion del
																									// escenario
		Pane paneChat = getChat.load(); // En un pane poner los datos

		/*
		 * Para agregar los datos del usuario
		 */
		user = this.txtUsername.getText();
		System.out.println(user);

		final ClienteController controlChat = getChat.<ClienteController>getController(); // Clase la cual

		controlChat.setUser(user);
		controlChat.setStageLocal(stageLocal); // Asiganmos escenario del otro

		Scene escenaChat = new Scene(paneChat); // Asiganar el panel a escena
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // Obtenecion de tamaño de pantalla

		// Asignacion de tamaño del escenario
		stageChat.setMaximized(true);
		stageChat.setMinHeight(400);
		stageChat.setMinWidth(700);

		stageChat.setScene(escenaChat); // Asignamos escenario

		stageLocal.close(); // Cerramos pantalla de login
		stageChat.show(); // Mostramos pantalla de busqueda

		stageChat.setOnCloseRequest(new EventHandler<WindowEvent>() {

			public void handle(WindowEvent event) {
				System.out.println("Cerrando...");
				controlChat.cerrarVentana();
			}
		});
	}
}
