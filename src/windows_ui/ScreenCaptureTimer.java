package windows_ui;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class ScreenCaptureTimer extends Application {

    private static final String APPLICATION_NAME = "kchrome.exe";

    @Override
    public void start(Stage primaryStage) throws Exception {
        openDialog(primaryStage);
        startCapturingScreen();
    }

    private WinDef.RECT activeWindowInfo() {
        char[] buffer = new char[1024 * 2];
        WinDef.HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        User32.INSTANCE.GetWindowText(hwnd, buffer, 1024);
        System.out.println("Active window title: " + Native.toString(buffer));
        WinDef.RECT rect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(hwnd, rect);
        System.out.println("rect = " + rect);
        System.out.println("\n===========================================");
        return rect;
    }

    private void takeScreenShot(WinDef.RECT rect) {
        try {
            Robot robot = new Robot();
            Rectangle captureRect = null;
            BufferedImage screenFullImage = null;
            ByteArrayOutputStream baos = null;
            byte[] imageInByte = null;
            String format = "jpeg";
            String fileName = null;
            File screenCapture = null;

            int noOfPartition = 4;
            int leftPos = rect.left;
            int topPos = rect.top;
            int width = (rect.right - rect.left);
            int height = (rect.bottom - rect.top);
            int heightCell = height / noOfPartition;
            int widthCell = width / noOfPartition;

            for (int indexX = 0; indexX < noOfPartition; indexX++) {
                for (int indexY = 0; indexY < noOfPartition; indexY++) {
                    fileName = new StringBuffer("screen_").append((indexX + 1) * (indexY + 1)).append(format).toString();
                    screenCapture = new File(fileName);
                    captureRect = new Rectangle(leftPos + (widthCell * indexX), topPos + (heightCell * indexY), widthCell, heightCell);
                    screenFullImage = robot.createScreenCapture(captureRect);
                    baos = new ByteArrayOutputStream();
                    ImageIO.write(screenFullImage, format, baos);
                    baos.flush();
                    imageInByte = baos.toByteArray();
                    baos.close();
                    ImageIO.write(screenFullImage, format, screenCapture);
                    System.out.println("A fileName " + fileName + " saved!");
                    System.out.println(" ==> Size" + screenCapture.length());
                }
            }
        } catch (AWTException | IOException ex) {
            System.err.println(ex);
        }
    }

    private void openDialog(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Phantom Eye");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }
   /* private boolean isDesiredApplicationIsRunning() throws IOException {
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
*/

    private void startCapturingScreen() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                        takeScreenShot(activeWindowInfo());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Timer timer = new Timer();
        long delay = 1000L;
        timer.scheduleAtFixedRate(task, delay, delay);
    }

    /*private byte[] takeScreenShot(WinDef.RECT rect) {
        byte[] empty = new byte[0];
        try {
            Robot robot = new Robot();
            String format = "jpeg";
            String fileName = "FullScreenshot." + format;
            File screenCapture = new File(fileName);

            Rectangle captureRect = new Rectangle(rect.toRectangle());
            BufferedImage screenFullImage = robot.createScreenCapture(captureRect);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(screenFullImage, format, baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            ImageIO.write(screenFullImage, format, screenCapture);
            System.out.println("A fileName screenshot saved!");
            System.out.println(" ==> Size" + screenCapture.length());
            return imageInByte;
        } catch (AWTException | IOException ex) {
            System.err.println(ex);
        }
        return empty;
    }*/


    public static void main(String[] args) {
        launch(args);
    }
}
