package client;

import model.*;
import network.NetworkCalls;

import javax.swing.*;
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
        imageMetaData.setFile_name("abc.jpeg");
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
        File imageFile = new File("abc.jpeg");
        int l = 1, sendCount;
        long filesize = imageFile.length();
        FileInputStream fi = new FileInputStream(imageFile);
        byte[] arrImage = new byte[65000];
        for (int i = 0; i < filesize; ) {
            DataTransfer dataTransfer = new DataTransfer();
            failed = false;
            l = fi.read(arrImage);
            dataTransfer.setArrImage(arrImage);
            sendCount = 0;
            do {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectOutputStream os = null;
                try {
                    os = new ObjectOutputStream(outputStream);
                    os.writeObject(dataTransfer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                lastSentObj = dataTransfer;
                networkCalls.sendAckToServer(outputStream.toByteArray());
                sendCount++;
                Thread.sleep(80);
                try {
                    String s = networkCalls.receiveTempAckFromServer();
                    if (s.contains("ACK") == false)
                        throw new Exception();
                } catch (Exception ex) {
                    failed = true;
                }
            } while (failed && sendCount < 5);
            if (sendCount < 5) {
                i += l;
                System.out.println("Progress : " + i * 100 / (int) filesize);
            } else {
                JOptionPane.showMessageDialog(null, "Client is not receiving");
                System.exit(0);
            }
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
