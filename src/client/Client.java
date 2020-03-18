package client;

public class Client implements ClientImpl.Listener {
    private static ClientImpl clientImpl = null;
    private ClientImpl.Listener listener = this;


    public static void main(String[] args) throws Exception {
        Client client = new Client();
        clientImpl = new ClientImpl(client.listener);
        clientImpl.initClient();
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
    public void onMetaDataSentSuccessfully() {
        System.out.println("Metadata sent successfully");
    }
}
