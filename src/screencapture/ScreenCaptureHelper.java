package screencapture;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import javafx.application.Platform;
import model.ImageChunksMetaData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ScreenCaptureHelper {

    private Listener listener = null;
    private Thread threadRunningTask = null;
    private long delay = 0l;
    private long period = 0l;
    private Robot robot = null;
//    private TimerTask task = null;
//    private ScheduledExecutorService service = null;

    public ScreenCaptureHelper(long delay, long period) {
        this.delay = delay;
        this.period = period;
    }

    public ScreenCaptureHelper(Listener listener) {
        this.listener = listener;
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

    private void takeScreenShot(WinDef.RECT rect, int noOfPartition) {
        threadRunningTask = new Thread(() -> {
            Rectangle captureRect = null;
            BufferedImage screenFullImage = null;
            ByteArrayOutputStream baos = null;
            ImageChunksMetaData[] imageInByte = new ImageChunksMetaData[noOfPartition * noOfPartition];
            String format = "jpeg";
            String fileName = null;
            File screenCapture = null;

            int heightCell = (rect.bottom - rect.top) / noOfPartition;
            int widthCell = (rect.right - rect.left) / noOfPartition;
            int partno = 1;

            try {
                robot = new Robot();
                for (int indexX = 0; indexX < noOfPartition; indexX++) {
                    for (int indexY = 0; indexY < noOfPartition; indexY++) {
                        ImageChunksMetaData chunksMetaData = new ImageChunksMetaData();
                        fileName = new StringBuffer("screen_").append(partno).append(".").append(format).toString();
                        screenCapture = new File(fileName);
                        captureRect = new Rectangle(rect.left + (widthCell * indexX), rect.top + (heightCell * indexY), widthCell, heightCell);
                        screenFullImage = robot.createScreenCapture(captureRect);
                        baos = new ByteArrayOutputStream();
                        ImageIO.write(screenFullImage, format, baos);
                        baos.flush();
                        baos.toByteArray();
                        baos.close();
                        ImageIO.write(screenFullImage, format, screenCapture);
                        System.out.println("FileName : " + fileName + ", Size : " + screenCapture.length());
                        chunksMetaData.setImageNo(partno - 1);
                        chunksMetaData.setImageName(fileName);
                        chunksMetaData.setImageSize(screenCapture.length());
                        imageInByte[partno - 1] = chunksMetaData;
                        partno++;
                    }
                }
            } catch (AWTException | IOException ex) {
                System.err.println(ex);
                Platform.runLater(() -> {
                    listener.onScreenCaptureFailed(noOfPartition);
                    threadRunningTask.interrupt();
                    threadRunningTask = null;
                });
            } finally {
                robot = null;
                Platform.runLater(() -> {
                    listener.onScreenCaptureSuccessful(imageInByte);
                    threadRunningTask.interrupt();
                    threadRunningTask = null;
                });
            }
        });
        threadRunningTask.start();
    }

    public void startCapturingScreen(int noOfPartition) {
        takeScreenShot(activeWindowInfo(), noOfPartition);
        /*if (task != null) {
            return;
        }
            task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        takeScreenShot(activeWindowInfo());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        service = Executors.newSingleThreadScheduledExecutor();
        long delay = 1000L;
        long period = 1000L;
        service.scheduleAtFixedRate(task, delay, period, TimeUnit.MILLISECONDS);*/
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

    public interface Listener {

        void onScreenCaptureSuccessful(ImageChunksMetaData[] arrImageChunksMetaData);

        void onScreenCaptureFailed(int noOfPartitions);
    }


}
