package client;

import model.EstablishConnection;
import model.NetworkData;
import network.NetworkCalls;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ClientImpl {
    private NetworkCalls networkCalls = null;
    private NetworkData networkData = null;


    public ClientImpl() {
        networkData = setNetworkData();
        networkCalls = new NetworkCalls(networkData);
        networkCalls.initConnection();
    }

    private static NetworkData setNetworkData() {
        NetworkData networkData = new NetworkData();
        networkData.setHostName("localhost");
        networkData.setPortNumber(5555);
        return networkData;
    }

    protected void waitingForServerAck() {
        Object receivedObj = networkCalls.receiveAckFromServer();
        if (receivedObj == null) return; //TODO Ask for retransmission

        if (receivedObj instanceof EstablishConnection) {
            //Move with the next step
        } else if (receivedObj instanceof NetworkData) {

        } else if (receivedObj instanceof NetworkData) {

        } else if (receivedObj instanceof NetworkData) {

        }
        System.out.println("ConnectionEstablish object received = " + receivedObj);
    }

    protected void sendConnectionAckToServer() {
        EstablishConnection establishConnection = new EstablishConnection();
        establishConnection.setClient_id(1);
        establishConnection.setRetransmission_timeout(10000);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(outputStream);
            os.writeObject(establishConnection);
        } catch (IOException e) {
            e.printStackTrace();
        }
        networkCalls.sendAckToServer(outputStream.toByteArray());
    }

}
