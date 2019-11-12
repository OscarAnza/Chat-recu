import java.io.IOException;

import controllers.InicioSesionController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ChatCliente extends Application {

	@Override
	public void start(Stage primaryStage) throws IOException {
		primaryStage.setTitle("Inicio de Sesión"); // Titulo pantalla inicial
		FXMLLoader inicioFXML = new FXMLLoader(getClass().getResource("/views/inicioSesion.fxml")); // Obtener datos de
																									// pantalla

		Pane paneSesion = inicioFXML.load(); // Obtenemos los datos

		InicioSesionController controllerInicio = (InicioSesionController) inicioFXML.getController();

		controllerInicio.setPrevStage(primaryStage);

		Scene escenaSesion = new Scene(paneSesion);
		primaryStage.setResizable(false); // Mover el tamaño
		primaryStage.setScene(escenaSesion); // Subimos la escena
		primaryStage.show(); // Mostramos la pantalla

	}

	public static void main(String[] args) {
		launch();
	}
}
