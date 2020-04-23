package client;

import javafx.stage.Stage;
import model.*;
import network.NetworkHelper;
import screencapture.ScreenCaptureHelper;
import utils.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Client {

    public interface View {
        void inflateView(Stage primaryStage, ArrayList<String> arrRunningApps);

        void setSystemTray();

        void startClientInitProcess();

        void onClientInitializedSuccessfully() throws Exception;

        void onConnectEstablishedSuccessfully() throws Exception;

        void onMetaDataSentSuccessfully() throws Exception;

        void onImageSentSuccessfully() throws Exception;
    }


    public interface Presenter {
        void inflateView(Stage primaryStage);

        void setSystemTray();

        void setScreenCaptureRunningStatus(boolean isAppRunning);

        boolean isScreenCaptureRunning();

        void initClient() throws Exception;

        void startScreenCapturing();

        void waitingForServerAck() throws Exception;

        void sendConnectionAckToServer(int noOfPartitions, String projectName, String projectPassword);

        void sendMetadataToServer();

        void sendImageFileToServer() throws Exception;
    }

    public static class ClientPresenterImpl implements Presenter {
        private static final int MAX_IMAGE_DATA_ARRAY_SIZE = 65000;
        private static Object lastSentObj = null;
        private NetworkHelper networkHelper = null;
        private ScreenCaptureHelper screenCaptureHelper = null;
        private NetworkData networkData = null;
        private View view;
        private boolean isAppRunning = false;
        private ImageChunksMetaData[] arrImageChunkData;

        private int noOfPartitions;
        private String projectName;
        private String projectPassword;

        public ClientPresenterImpl(View view) {
            this.view = view;
        }

        @Override
        public void inflateView(Stage primaryStage) {

            ArrayList<String> arrRunningApps = new ArrayList<>();
            try {
                String line;
                HashMap<String, String> map = new HashMap<>();
                StringBuilder pidInfo = new StringBuilder();
//                Process p = Runtime.getRuntime().exec("tasklist /v /fo csv /nh /fi \"username eq cray \" /fi \"status eq running\"");
                Process p = Runtime.getRuntime().exec("tasklist /v /fo csv /nh /fi \"username eq " + System.getProperty("user.name").toLowerCase() + " \" /fi \"status eq running\"");
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String[] arrTemp;
                while ((line = input.readLine()) != null) {
                    arrTemp = line.trim().replace("\"", "").split(",");
                    String appName = arrTemp[0].replace(".exe", "");
                    if (!map.containsKey(appName)) {
                        map.put(appName, arrTemp[1]);
                        arrRunningApps.add(appName);
                        pidInfo.append(appName).append("\n");
                    }
                }
                input.close();
//                System.out.println(pidInfo.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

           /* List<ProcessInfo> processesList = JProcesses.getProcessList();
            for (final ProcessInfo processInfo : processesList) {
                arrRunningApps.add(processInfo.getName().trim());
            }*/

            view.inflateView(primaryStage, arrRunningApps);
        }

        @Override
        public void setSystemTray() {
            view.setSystemTray();
        }

        @Override
        public void initClient() {
            try {
                screenCaptureHelper = new ScreenCaptureHelper();
                networkData = setNetworkData();
                networkHelper = new NetworkHelper(networkData);
                networkHelper.initConnection();
                System.out.println("Client init successful");
                view.onClientInitializedSuccessfully();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void startScreenCapturing() {
            if (isAppRunning) {
                try {
                    if (lastSentObj == null) {
                        //first time
                        view.startClientInitProcess();
                    } else {
                        //Every other time we'll send the Connection est. obj.
                        //As the username & password might change. But as we are
                        //identifying the client with the mac address so change in uname & pass
                        //won't affect much on server side.
                        view.onClientInitializedSuccessfully();
                    }
                } catch (Exception e) {

                }
            }
        }

        @Override
        public void sendConnectionAckToServer(int noOfPartitions, String projectName, String projectPassword) {
            this.noOfPartitions = noOfPartitions;
            EstablishConnection establishConnection = new EstablishConnection();
            establishConnection.setClientId(1);
            establishConnection.setProjectName(projectName);
            establishConnection.setProjectPassword(projectPassword);
            establishConnection.setRetransmissionTimeout(10000); //dummy value will use it later on
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
                int l = 0;
                imageFile = new File(arrImageChunkData[index].getImageName());
                fileSize = imageFile.length();
                fi = new FileInputStream(imageFile);

                for (int i = 0; i < fileSize; ) {
                    arrImageData = new byte[MAX_IMAGE_DATA_ARRAY_SIZE];
                    DataTransfer dataTransfer = new DataTransfer();
                    dataTransfer.setCurrentImageSeqNo(index + 1);
                    if (l == 0) {
                        dataTransfer.setIsFirstPacketOfImageBlock(1);
                    }
                    l = fi.read(arrImageData);
                    if (l < MAX_IMAGE_DATA_ARRAY_SIZE)
                        dataTransfer.setIsLastPacketOfImageBlock(1);
                    if (index == arrSize - 1) {
                        dataTransfer.setIsLastPacket(1);
                    }
                    dataTransfer.setArrImage(arrImageData);
                    dataTransfer.setSeqNo(seqNo);
                    byte[] arrImageDataObj = Utils.convertObjToByteArray(dataTransfer);
                    lastSentObj = dataTransfer;
                    networkHelper.sendAckToServer(arrImageDataObj);
                    Thread.sleep(80);
                    try {
                        String s = new String(networkHelper.receiveTempAckFromServer());
                        if (!s.contains("ACK"))
                            throw new Exception();
                    } catch (Exception ex) {
                    }
                    i += l;
                    seqNo++;
//                    System.out.println("Progress : " + i * 100 / (int) fileSize);
                }
                fi.close();
            }
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
                System.out.println("Connection with Client established successfully");
                view.onConnectEstablishedSuccessfully();
//                System.out.println("EstablishConnection ack received = " + receivedObj);
            } else if (lastSentObj instanceof ImageMetaData) {
                //Move with the next step with Image Transfer
                System.out.println("Metadata sent successfully, ready to send image");
                view.onMetaDataSentSuccessfully();
//                System.out.println("ImageMetaData ack received = " + receivedObj);
            } else if (lastSentObj instanceof DataTransfer) {
                //Continue till the last ack is received.
                if (((DataTransfer) lastSentObj).getIsLastPacket() == 1) {
                    System.out.println("Image sent successfully");
                    view.onImageSentSuccessfully();
                }
//                System.out.println("DataTransfer ack received = " + receivedObj);
            } else {
                throw new Exception("The data is null or not in the proper format");
            }
        }

        @Override
        public boolean isScreenCaptureRunning() {
            return isAppRunning;
        }

        @Override
        public void setScreenCaptureRunningStatus(boolean isAppRunning) {
            this.isAppRunning = isAppRunning;
        }

        private static NetworkData setNetworkData() {
            NetworkData networkData = new NetworkData();
            networkData.setHostName("localhost");
            networkData.setPortNumber(5555);
            return networkData;
        }
    }

}
