import controllers.ServidorController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class ChatServidor extends Application {
	
	private Stage primaryStage;
	private VBox serverLayout;

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Servidor Chat");

		initServerLayout();
	}

	private void initServerLayout() {
		try {
			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("views/servidorPrincipal.fxml"));
			final ServidorController servidorController = new ServidorController();
			loader.setController(servidorController);
			serverLayout = loader.load();

			// Show the scene containing the root layout.
			Scene scene = new Scene(serverLayout);
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					// We need to eliminate the Servidor Threads
					// If the User decides to close it.
					if (servidorController.servidor != null) {
						servidorController.servidor.stop();
						servidorController.servidor = null;
					}
				}
			});        
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
