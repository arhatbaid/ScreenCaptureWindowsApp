package client;

import javafx.stage.Stage;
import model.*;
import network.NetworkHelper;
import screencapture.ScreenCaptureHelper;
import utils.Utils;

import java.io.*;
import java.util.Arrays;

public class Client {

    public interface View {
        void inflateView(Stage primaryStage);

        void onClientInitializedSuccessfully() throws Exception;

        void onConnectEstablishedSuccessfully() throws Exception;

        void onMetaDataSentSuccessfully() throws Exception;

        void onImageSentSuccessfully();
    }


    public interface Presenter {
        void inflateView(Stage primaryStage);

        void initClient() throws Exception;

        void waitingForServerAck() throws Exception;

        void sendConnectionAckToServer();

        void sendMetadataToServer();

        void sendImageFileToServer() throws Exception;
    }

    public static class ClientPresenterImpl implements Presenter {
        private NetworkHelper networkHelper = null;
        private ScreenCaptureHelper screenCaptureHelper = null;
        private NetworkData networkData = null;
        private static Object lastSentObj = null;
        private View view;
        private ImageChunksMetaData[] arrImageChunkData = new ImageChunksMetaData[1];
        private static final int MAX_IMAGE_DATA_ARRAY_SIZE = 65000;

        private int noOfPartitions;
        private String projectName;
        private String projectPassword;

        public ClientPresenterImpl(View view, int noOfPartitions, String projectName, String projectPassword) {
            this.view = view;
            this.noOfPartitions = noOfPartitions;
            this.projectName = projectName;
            this.projectPassword = projectPassword;
        }

        @Override
        public void inflateView(Stage primaryStage) {
            view.inflateView(primaryStage);
        }

        @Override
        public void initClient() throws Exception {
            screenCaptureHelper = new ScreenCaptureHelper();
            networkData = setNetworkData();
            networkHelper = new NetworkHelper(networkData);
            networkHelper.initConnection();
            view.onClientInitializedSuccessfully();
        }

        @Override
        public void waitingForServerAck() throws Exception {
            Object receivedObj = networkHelper.receiveAckFromServer();
            //TODO Ask for/Do retransmission
            if (!(receivedObj instanceof PacketAck)) {
                throw new Exception("The Ack is not in a proper format");
            }

            if (lastSentObj instanceof EstablishConnection) {
                //Move with the next step with Image MetaData
                view.onConnectEstablishedSuccessfully();
//                System.out.println("EstablishConnection ack received = " + receivedObj);
            } else if (lastSentObj instanceof ImageMetaData) {
                //Move with the next step with Image Transfer
                view.onMetaDataSentSuccessfully();
//                System.out.println("ImageMetaData ack received = " + receivedObj);
            } else if (lastSentObj instanceof DataTransfer) {
                //Continue till the last ack is received.
                if (((DataTransfer) lastSentObj).getIsLastPacket() == 1)
                    view.onImageSentSuccessfully();
//                System.out.println("DataTransfer ack received = " + receivedObj);
            } else {
                throw new Exception("The data is null or not in the proper format");
            }
        }

        @Override
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

        @Override
        public void sendMetadataToServer() {
            arrImageChunkData = screenCaptureHelper.startCapturingScreen(noOfPartitions);
            ImageMetaData imageMetaData = new ImageMetaData();
            imageMetaData.setClientId(1);
            imageMetaData.setNoOfImages(noOfPartitions * noOfPartitions);
            imageMetaData.setArrImageChunks(arrImageChunkData);
            byte[] objArray = Utils.convertObjToByteArray(imageMetaData);
            lastSentObj = imageMetaData;
            networkHelper.sendAckToServer(objArray);
        }

        @Override
        public void sendImageFileToServer() throws Exception {
            int seqNo = 2;
            FileInputStream fi;
            byte[] arrImageData;
            long fileSize;
            File imageFile;

            for (int index = 0, arrSize = arrImageChunkData.length; index < arrSize; index++) {
                int l;
                imageFile = new File(arrImageChunkData[index].getImageName());
                fileSize = imageFile.length();
                fi = new FileInputStream(imageFile);

                for (int i = 0; i < fileSize; ) {
                    arrImageData = new byte[MAX_IMAGE_DATA_ARRAY_SIZE];
                    DataTransfer dataTransfer = new DataTransfer();
                    l = fi.read(arrImageData);
                    if (l < MAX_IMAGE_DATA_ARRAY_SIZE)
                        dataTransfer.setIsLastPacketOfImageBlock(1);
                    if(index == arrSize -1){
                        dataTransfer.setIsLastPacket(1);
                    }
                    dataTransfer.setArrImage(arrImageData);
                    dataTransfer.setSeqNo(seqNo);
                    byte[] arrImageDataObj = Utils.convertObjToByteArray(dataTransfer);
                    lastSentObj = dataTransfer;
                    networkHelper.sendAckToServer(arrImageDataObj);
                    Thread.sleep(80);
                    try {
                        String s = networkHelper.receiveTempAckFromServer();
                        if (s.contains("ACK"))
                            throw new Exception();
                    } catch (Exception ex) {
                    }
                    i += l;
                    seqNo++;
                    System.out.println("Progress : " + i * 100 / (int) fileSize);
                }
                fi.close();
            }
        }

        private static NetworkData setNetworkData() {
            NetworkData networkData = new NetworkData();
            networkData.setHostName("localhost");
            networkData.setPortNumber(5555);
            return networkData;
        }
    }

}
