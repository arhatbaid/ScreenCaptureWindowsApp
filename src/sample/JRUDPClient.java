package sample;

import fr.slaynash.communication.handlers.OrderedPacketHandler;
import fr.slaynash.communication.rudp.RUDPClient;
import fr.slaynash.communication.utils.NetUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class JRUDPClient {
    public static final InetAddress SERVER_HOST = NetUtils.getInternetAdress("localhost");
    public static final int SERVER_PORT = 56448;
    public static RUDPClient client;

    public static void initServer() {
        try {
            client = new RUDPClient(SERVER_HOST, SERVER_PORT);
            client.setPacketHandler(OrderedPacketHandler.class);
            client.connect();
        } catch (SocketException e) {
            System.out.println("Cannot allow port for the client. Client can't be launched.");
            System.exit(-1);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + SERVER_HOST);
            System.exit(-1);
        } catch (SocketTimeoutException e) {
            System.out.println("Connection to " + SERVER_HOST + ":" + SERVER_PORT + " timed out.");
        } catch (InstantiationException e) {
        } //Given handler class can't be instantiated.
        catch (IllegalAccessException e) {
        } //Given handler class can't be accessed.
        catch (IOException e) {
        }

//        client.sendPacket(new byte[]{0x00}); //Send packet to the server
//        client.sendReliablePacket(new byte[]{0x00}); //Send packet to the server
    }

    public static void disconnectConnection() {
        if (client != null && client.isConnected())
            client.disconnect(); //Disconnect from server
    }
}