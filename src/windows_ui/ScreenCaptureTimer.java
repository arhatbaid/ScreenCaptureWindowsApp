package windows_ui;

import client.ClientImpl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;


public class ScreenCaptureTimer extends Application implements ClientImpl.Listener {


    private static ClientImpl clientImpl = null;
    private Stage window = null;
    private int noOfPartitions = 4;
    private String projectName = "Phantom4";
    private String projectPassword = "Phantom4";

    @Override
    public void start(Stage primaryStage) throws Exception {
        openDialog(primaryStage);

        clientImpl = new ClientImpl(this, noOfPartitions, projectName, projectPassword);
        clientImpl.initClient();
    }

    private void openDialog(Stage primaryStage) throws IOException {
        window = primaryStage;
        window.setTitle("Phantom Eye");
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void onClientInitializedSuccessfully() throws Exception {
        System.out.println("Client init successful");
        clientImpl.sendConnectionAckToServer();
        clientImpl.waitingForServerAck();
    }

    @Override
    public void onConnectEstablishedSuccessfully() throws Exception {
        System.out.println("Connection with Client established successfully");
        clientImpl.sendMetadataToServer();
        clientImpl.waitingForServerAck();
    }

    @Override
    public void onMetaDataSentSuccessfully() throws Exception {
        System.out.println("Metadata sent successfully, ready to send image");
//        clientImpl.sendImageFileToServer();
    }

    @Override
    public void onImageSentSuccessfully() {

    }
}