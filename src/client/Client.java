package client;

public class Client {
    private static ClientImpl clientImpl = null;


    public static void main(String[] args){
        clientImpl = new ClientImpl();
        clientImpl.sendConnectionAckToServer();
        clientImpl.waitingForServerAck();
    }
}
