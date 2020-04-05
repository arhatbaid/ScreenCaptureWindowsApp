package client;

import javafx.stage.Stage;

public class ClientDemo implements Client.View {
    private static Client.Presenter clientImpl = null;
    public Client.View view = this;


    public static void main(String[] args) throws Exception {
        ClientDemo client = new ClientDemo();
        clientImpl = new Client.ClientPresenterImpl(client.view, 0, "projectName", "projectPassword");
        clientImpl.initClient();
    }

    @Override
    public void inflateView(Stage primaryStage) {

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
        System.out.println("Metadata sent successfully");
        clientImpl.sendImageFileToServer();
    }

    @Override
    public void onImageSentSuccessfully() {

    }
}
