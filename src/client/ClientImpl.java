package client;

import model.*;
import network.NetworkHelper;
import screencapture.ScreenCaptureHelper;
import utils.Utils;

import javax.swing.*;
import java.io.*;

public class ClientImpl {
    private NetworkHelper networkHelper = null;
    private ScreenCaptureHelper screenCaptureHelper = null;
    private NetworkData networkData = null;
    private static Object lastSentObj = null;
    private Listener listener = null;

    private int noOfPartitions = 0;
    private String projectName = "";
    private String projectPassword = "";

    public ClientImpl(Listener listener, int noOfPartitions, String projectName, String projectPassword) {
        this.listener = listener;
        this.noOfPartitions = noOfPartitions;
        this.projectName = projectName;
        this.projectPassword = projectPassword;
    }

    public void initClient() throws Exception {
        screenCaptureHelper = new ScreenCaptureHelper();
        networkData = setNetworkData();
        networkHelper = new NetworkHelper(networkData);
        networkHelper.initConnection();
        listener.onClientInitializedSuccessfully();
    }

    private static NetworkData setNetworkData() {
        NetworkData networkData = new NetworkData();
        networkData.setHostName("localhost");
        networkData.setPortNumber(5555);
        return networkData;
    }

    public void waitingForServerAck() throws Exception {
        Object receivedObj = networkHelper.receiveAckFromServer();
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

    public void sendConnectionAckToServer() {
        EstablishConnection establishConnection = new EstablishConnection();
        establishConnection.setClientId(1);
        establishConnection.setProjectName(projectName);
        establishConnection.setProjectPassword(projectPassword);
        establishConnection.setRetransmissionTimeout(10000);
        byte[] objArray = Utils.convertObjToByteArray(establishConnection);
        lastSentObj = establishConnection;
        networkHelper.sendAckToServer(objArray);
    }

    public void sendMetadataToServer() {
        ImageChunksMetaData[] arrImageData = screenCaptureHelper.startCapturingScreen(noOfPartitions);
        ImageMetaData imageMetaData = new ImageMetaData();
        imageMetaData.setClientId(1);
        imageMetaData.setArrImageChunks(arrImageData);
        byte[] objArray = Utils.convertObjToByteArray(imageMetaData);
        lastSentObj = imageMetaData;
        networkHelper.sendAckToServer(objArray);
    }

    public void sendImageFileToServer() throws Exception {
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
                networkHelper.sendAckToServer(outputStream.toByteArray());
                sendCount++;
                Thread.sleep(80);
                try {
                    String s = networkHelper.receiveTempAckFromServer();
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


    public interface Listener {
        void onClientInitializedSuccessfully() throws Exception;

        void onConnectEstablishedSuccessfully() throws Exception;

        void onMetaDataSentSuccessfully() throws Exception;

        void onImageSentSuccessfully();
    }

}
