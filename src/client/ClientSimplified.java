package client;

import model.Connection;
import model.ConnectionEstablish;
import network.ConnectionClass;
import utils.Utils;

public class ClientSimplified {

    private static Connection connection = null;
    private static ConnectionClass connectionClass = null;

    public static void main(String[] args){
        setServerInfo();

        connectionClass = new ConnectionClass(connection);
        connectionClass.initConnection();
        sendConnectionAckToServer();
        waitingForServerAck();
    }

    private static void waitingForServerAck() {
        connectionClass.receiveAckFromServer();
    }


    private static void sendConnectionAckToServer() {
        ConnectionEstablish connectionEstablish = new ConnectionEstablish();
        connectionEstablish.setClient_id(1);
        connectionEstablish.setRetransmission_timeout(10000);
        connectionClass.sendAckToServer(connectionEstablish.toByte());
    }

    private static void setServerInfo() {
        connection = new Connection();
        connection.setHostName("localhost");
        connection.setPortNumber(5555);
    }
}
