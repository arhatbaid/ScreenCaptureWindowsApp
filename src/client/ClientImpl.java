package client;

import model.*;
import network.NetworkCalls;

import java.io.*;

public class ClientImpl {
    private NetworkCalls networkCalls = null;
    private NetworkData networkData = null;
    private static Object lastSentObj = null;
    private Listener listener = null;


    public ClientImpl(Listener listener) {
        this.listener = listener;
    }

    protected void initClient() throws Exception {
        networkData = setNetworkData();
        networkCalls = new NetworkCalls(networkData);
        networkCalls.initConnection();
        listener.onClientInitializedSuccessfully();
    }

    private static NetworkData setNetworkData() {
        NetworkData networkData = new NetworkData();
        networkData.setHostName("localhost");
        networkData.setPortNumber(5555);
        return networkData;
    }

    protected void waitingForServerAck() throws Exception {
        Object receivedObj = networkCalls.receiveAckFromServer();
        //TODO Ask for/Do retransmission
        if (receivedObj == null || !(receivedObj instanceof PacketAck)) {
            throw new Exception("The Ack is null or not in proper format");
        }

        if (lastSentObj instanceof EstablishConnection) {
            //Move with the next step with Image MetaData
            listener.onConnectEstablishedSuccessfully();
            System.out.println("EstablishConnection ack received = " + receivedObj);
        } else if (lastSentObj instanceof ImageMetaData) {
            //Move with the next step with Image Transfer
            listener.onMetaDataSentSuccessfully();
            System.out.println("ImageMetaData ack received = " + receivedObj);
        } else if (lastSentObj instanceof DataTransfer) {
            //Continue till the last ack is received.
            System.out.println("DataTransfer ack received = " + receivedObj);
        } else {
            //TODO object corrupt or not identified.
//            throw new Exception("The data is null or not in the proper format");
        }
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
        lastSentObj = establishConnection;
        networkCalls.sendAckToServer(outputStream.toByteArray());
    }

    protected void sendMetadataToServer() {
        File f = new File("abc.jpeg"); //TODO remove it later on
        ImageMetaData imageMetaData = new ImageMetaData();
        imageMetaData.setClient_id(1);
        imageMetaData.setFile_name("Output.jpeg");
        imageMetaData.setFile_length(f.length());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(outputStream);
            os.writeObject(imageMetaData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        lastSentObj = imageMetaData;
        networkCalls.sendAckToServer(outputStream.toByteArray());
    }

    protected void sendImageFileToServer() throws Exception {
        boolean failed;
        byte[] b;

        File f = new File("abc.jpeg");//TODO change
        FileInputStream fi = new FileInputStream(f);
        long filesize = f.length(); //TODO change
        for (int i = 0; i < filesize; i++) {



            networkCalls.tempImage(fi);
            /*if (sendCount < 5) {
                i += l;
                jpb.setValue(i * 100 / (int) filesize);
                jpb.setString(jpb.getValue() + " %");
            } else {
                JOptionPane.showMessageDialog(null, "Client is not receiving");
                System.exit(0);
            }*/
        }
        fi.close();
    }


    interface Listener {
        void onClientInitializedSuccessfully() throws Exception;

        void onConnectEstablishedSuccessfully() throws Exception;

        void onMetaDataSentSuccessfully() throws Exception;

        void onImageSentSuccessfully();
    }

}
