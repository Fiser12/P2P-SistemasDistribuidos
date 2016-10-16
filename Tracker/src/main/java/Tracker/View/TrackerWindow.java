package Tracker.View;

/**
 * Created by Fiser on 16/10/16.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class TrackerWindow extends Application {

    @Override
    public void start(Stage primaryStage) {
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource("Tracker.fxml"));
            primaryStage.setTitle("Tracker");
            primaryStage.setScene(new Scene(root, 700, 450));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
