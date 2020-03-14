package windows_ui;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ScreenCaptureTimer extends Application {

    private static List<String> arrProcesses = new ArrayList<>();
    Stage window;

    public static void main(String[] args) {
        getTask();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle("Phantom Eye");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(10);
        grid.setHgap(10);

        //project name
        javafx.scene.control.Label Projlabel = new javafx.scene.control.Label("Project Name:");
        GridPane.setConstraints(Projlabel, 0, 0);
        javafx.scene.control.TextField projText = new javafx.scene.control.TextField();
        projText.setPrefWidth(20);
        GridPane.setConstraints(projText, 1, 0);
        Projlabel.setStyle("-fx-text-fill: #ff9a16;");

        //password
        javafx.scene.control.Label password = new javafx.scene.control.Label("Password:");
        GridPane.setConstraints(password, 0, 1);
        password.setStyle("-fx-text-fill: #ff9a16;");
        javafx.scene.control.TextField passtext = new javafx.scene.control.TextField();
        GridPane.setConstraints(passtext, 1, 1);

        //time
        javafx.scene.control.Label time = new javafx.scene.control.Label("Frame rate (Time):");
        GridPane.setConstraints(time, 0, 2);
        time.setStyle("-fx-text-fill: #ff9a16;");
        javafx.scene.control.TextField timetext = new javafx.scene.control.TextField();
        GridPane.setConstraints(timetext, 1, 2);

        //radio for cursor
        RadioButton rb1 = new RadioButton("Yes");
        GridPane.setConstraints(rb1, 1, 3);
        rb1.setStyle("-fx-text-fill: #ff9a16;");
        RadioButton rb2 = new RadioButton("No");
        rb2.setStyle("-fx-text-fill: #ff9a16;");
        GridPane.setConstraints(rb2, 1, 4);
        javafx.scene.control.Label mouse = new javafx.scene.control.Label("Cursor Control:");
        mouse.setStyle("-fx-text-fill: #ff9a16;");
        GridPane.setConstraints(mouse, 0, 3);

        // drop down list
        javafx.scene.control.Label select = new javafx.scene.control.Label("Select Application:");
        select.setStyle("-fx-text-fill: #ff9a16;");
        GridPane.setConstraints(select, 0, 5);
        ChoiceBox<String> choice = new ChoiceBox(FXCollections.observableArrayList(arrProcesses));
        //  ChoiceBox<String> choice = new ChoiceBox<>();
        //  ComboBox choice = new ComboBox(FXCollections.observableArrayList(Proces));
//        choice.getItems().add("Ashutosh");
        //  choice.getItems().addAll(Proces);
        GridPane.setConstraints(choice, 1, 5);
        choice.getSelectionModel().selectedItemProperty().addListener((v, oldvalue, newvalue) -> System.out.println(newvalue));

        //start button
        javafx.scene.control.Button button = new Button("Start");
        button.setStyle("-fx-text-fill: green;");

        // button.setOnAction(e -> getChoice(choice));
        button.setOnAction(e -> startCapturingScreen());

        ToggleGroup radio = new ToggleGroup();
        rb1.setToggleGroup(radio);
        rb2.setToggleGroup(radio);

        GridPane.setConstraints(button, 1, 7);
        grid.getChildren().addAll(Projlabel, projText, password, passtext, time, timetext, mouse, rb1, rb2, select, button, choice);
        Scene scene = new Scene(grid, 650, 270);
        scene.getStylesheets().add("css/phantom.css");
        window.setResizable(false);
        window.setScene(scene);
        window.show();

    }





    private static void getTask() {
        List<ProcessInfo> processesList = JProcesses.getProcessList();

        for (final ProcessInfo processInfo : processesList) {
            arrProcesses.add(processInfo.getName().trim());
        }
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
            int partno = 1;
            for (int indexX = 0; indexX < noOfPartition; indexX++) {
                for (int indexY = 0; indexY < noOfPartition; indexY++) {
                    fileName = new StringBuffer("screen_").append(partno).append(".").append(format).toString();
                    screenCapture = new File(fileName);
                    captureRect = new Rectangle(leftPos + (widthCell * indexX), topPos + (heightCell * indexY), widthCell, heightCell);
                    screenFullImage = robot.createScreenCapture(captureRect);
                    baos = new ByteArrayOutputStream();
                    ImageIO.write(screenFullImage, format, baos);
                    baos.flush();
                    baos.toByteArray();
                    baos.close();
                    ImageIO.write(screenFullImage, format, screenCapture);
                    System.out.println("A fileName " + fileName + " saved!");
                    System.out.println(" ==> Size" + screenCapture.length());
                    partno++;
                }
            }
        } catch (AWTException | IOException ex) {
            System.err.println(ex);
        }
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
}