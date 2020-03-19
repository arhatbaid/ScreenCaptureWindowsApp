package network;

import model.NetworkData;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;

public class NetworkCalls {

    private NetworkData networkData = null;
    private static DatagramSocket socket = null;
    private static InetAddress address = null;
    private static DatagramPacket dataPacket = null;
    private static final int MAX_BUFFER_SIZE= 65507;

    public NetworkCalls(NetworkData networkData) {
        this.networkData = networkData;
    }

    public void initConnection() {
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(networkData.getHostName());
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void sendDataToServer(byte[] data) {
        try {
            dataPacket = new DatagramPacket(data, data.length, InetAddress.getByName(networkData.getHostName()), networkData.getPortNumber());
            socket.send(dataPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object receiveAckFromServer() {
        Object object = null;
        byte[] arrData = new byte[MAX_BUFFER_SIZE];
        dataPacket = new DatagramPacket(arrData, MAX_BUFFER_SIZE);
        try {
            System.out.println("===>Port " + dataPacket.getPort());
            System.out.println("===>Address " + dataPacket.getAddress());
            socket.receive(dataPacket);
            arrData = dataPacket.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(arrData);
            ObjectInputStream is = new ObjectInputStream(in);
            object = is.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }

    public void sendAckToServer(byte[] data) {
        try {
            if (dataPacket != null) {
                dataPacket = new DatagramPacket(data, data.length, dataPacket.getAddress(), dataPacket.getPort());
            } else {
                dataPacket = new DatagramPacket(data, data.length, InetAddress.getByName(networkData.getHostName()), networkData.getPortNumber());
            }
            System.out.println("===>Port " + dataPacket.getPort());
            System.out.println("===>Address " + dataPacket.getAddress());
            socket.send(dataPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendConnectionRequestToServer(byte[] data) {
        try {
            if (dataPacket != null) {
                dataPacket = new DatagramPacket(data, data.length, dataPacket.getAddress(), dataPacket.getPort());
            } else {
                dataPacket = new DatagramPacket(data, data.length, InetAddress.getByName(networkData.getHostName()), networkData.getPortNumber());
            }
            socket.send(dataPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tempImage(FileInputStream fi) throws Exception {
        boolean failed = false;
        byte[] b = new byte[65507];
        int sendCount = 0, l = fi.read(b);
        do {
            System.out.println("===>Port " + dataPacket.getPort());
            System.out.println("===>Address " + dataPacket.getAddress());
            dataPacket = new DatagramPacket(b, l, dataPacket.getAddress(), dataPacket.getPort());
            socket.send(dataPacket);
            sendCount++;
            Thread.sleep(80);
            try {
                socket.receive(dataPacket);
                String s = new String(b);
                if (s.contains("ACK") == false)
                    throw new Exception();
            } catch (Exception ex) {
                failed = true;
            }
        } while (failed && sendCount < 5);
    }


}
