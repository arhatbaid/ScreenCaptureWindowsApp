package client;

import model.Connection;
import network.ConnectionClass;
import utils.Utils;

public class ClientSimplified {

    private static Connection connection = null;
    private static ConnectionClass connectionClass = null;

    public static void main(String[] args){
        setServerInfo();
        connectionClass = new ConnectionClass(connection);
        connectionClass.initConnection();
        connectionClass.sendDataToServer(Utils.getFileBytes());
        connectionClass.receiveAckFromServer();
    }


    private static void setServerInfo() {
        connection = new Connection();
        connection.setHostName("localhost");
        connection.setPortNumber(5555);
    }
}
