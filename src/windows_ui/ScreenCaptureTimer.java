package windows_ui;

import client.Client;
import javafx.application.Application;
import javafx.stage.Stage;


public class ScreenCaptureTimer extends Application implements Client.View {


    private static Client.ClientPresenterImpl clientPresenterImpl = null;

    //TODO params will be linked with UI
    private int noOfPartitions = 4;
    private String projectName = "Phantom4";
    private String projectPassword = "Phantom4";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        clientPresenterImpl = new Client.ClientPresenterImpl(this, noOfPartitions, projectName, projectPassword);
        clientPresenterImpl.inflateView(primaryStage);
        clientPresenterImpl.initClient();
    }

    @Override
    public void inflateView(Stage primaryStage) {
        Stage window = primaryStage;
        window.setTitle("Phantom Eye");
    }

    @Override
    public void onClientInitializedSuccessfully() throws Exception {
        System.out.println("Client init successful");
        clientPresenterImpl.sendConnectionAckToServer();
        clientPresenterImpl.waitingForServerAck();
    }

    @Override
    public void onConnectEstablishedSuccessfully() throws Exception {
        System.out.println("Connection with Client established successfully");
        clientPresenterImpl.sendMetadataToServer();
        clientPresenterImpl.waitingForServerAck();
    }

    @Override
    public void onMetaDataSentSuccessfully() throws Exception {
        System.out.println("Metadata sent successfully, ready to send image");
//        clientPresenter.sendImageFileToServer();
    }

    @Override
    public void onImageSentSuccessfully() {

    }
}