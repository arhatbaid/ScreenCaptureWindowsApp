package windows_ui;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
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
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScreenCaptureTimer extends Application {

    private static List<String> arrProcesses = new ArrayList<>();
    Stage window;
    private TimerTask task = null;
    private ScheduledExecutorService service = null;

    public static void main(String[] args) {
        getTask();
        launch(args);
    }

    private static void getTask() {
        List<ProcessInfo> processesList = JProcesses.getProcessList();
        for (final ProcessInfo processInfo : processesList) {
            arrProcesses.add(processInfo.getName().trim());
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle("Phantom Eye");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(10);
        grid.setHgap(10);

        //project name
        Label Projlabel = new Label("Project Name:");
        GridPane.setConstraints(Projlabel, 0, 0);
        TextField projText = new TextField();
        projText.setPrefWidth(20);
        GridPane.setConstraints(projText, 1, 0);
        Projlabel.setStyle("-fx-text-fill: #ff9a16;");

        //password
        Label password = new Label("Password:");
        GridPane.setConstraints(password, 0, 2);
        password.setStyle("-fx-text-fill: #ff9a16;");
        PasswordField passtext = new PasswordField();
        GridPane.setConstraints(passtext, 1, 2);

        //time
        Label time = new Label("Frame rate :");
        GridPane.setConstraints(time, 0, 0);
        time.setStyle("-fx-text-fill: #ff9a16;");
        TextField timetext = new TextField("10");
        GridPane.setConstraints(timetext, 1, 0);

        //screen selection
        Label screen_parts = new Label("Screen Partition:");
        screen_parts.setStyle("-fx-text-fill: #ff9a16;");
        GridPane.setConstraints(screen_parts, 0, 1);
        TextField parts = new TextField("4");
        GridPane.setConstraints(parts, 1, 1);
        //radio for cursor
//        RadioButton rb1 = new RadioButton("Yes");
//        GridPane.setConstraints(rb1, 1, 1);
//        rb1.setStyle("-fx-text-fill: #ff9a16;");
//        RadioButton rb2 = new RadioButton("No");
//        rb2.setStyle("-fx-text-fill: #ff9a16;");
//        GridPane.setConstraints(rb2, 1, 2);
//        Label mouse = new Label("Cursor Control:");
//        mouse.setStyle("-fx-text-fill: #ff9a16;");
//        GridPane.setConstraints(mouse, 0, 1);

//        ToggleGroup radio = new ToggleGroup();
//        rb1.setToggleGroup(radio);
//        rb2.setToggleGroup(radio);

        // drop down list
        Label select = new Label("Select Application:");
        select.setStyle("-fx-text-fill: #ff9a16;");
        GridPane.setConstraints(select, 0, 2);
        ChoiceBox<String> choice = new ChoiceBox<>();
        choice.setPrefSize(200.0,10.0);
        choice.getItems().addAll(arrProcesses);
        GridPane.setConstraints(choice, 1, 2);
        choice.getSelectionModel().selectedItemProperty().addListener((v, oldvalue, newvalue) -> System.out.println(newvalue));

        //mainscreen button on second screen
        Button mainScreen = new Button("Main Screen");
        GridPane.setConstraints(mainScreen, 1, 5);
        GridPane grid2 = new GridPane();
        grid2.setPadding(new Insets(10, 10, 10, 10));
        grid2.setVgap(10);
        grid2.setHgap(10);
        grid2.getChildren().addAll(mainScreen, time, timetext, screen_parts, parts, select, choice);
        Scene advanceScene = new Scene(grid2, 370, 200);
        Button changescreen = new Button("Advanced");
        GridPane.setConstraints(changescreen, 0, 5);
        changescreen.setOnAction(e -> window.setScene(advanceScene));

        //start button
        Button start = new Button("Start");
        start.setStyle("-fx-text-fill: green;");

        start.setOnAction(e -> {
            if (start.getText().equals("Start") && projText.getText().equals("phantom") && passtext.getText().equals("1234")) {
                startCapturingScreen();
                start.setText("Stop");
            } else if (start.getText().equals("Start") && (projText.getText().equals("") || passtext.getText().equals(""))) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error Dialog");
                alert.setHeaderText("Look, an Error Dialog");
                alert.setContentText("Please insert the username and password!");
                alert.showAndWait();
            } else {
                start.setText("Start");
                task.cancel();
                service.shutdown();
                task = null;
            }
        });

        GridPane.setConstraints(start, 1, 5);
        grid.getChildren().addAll(Projlabel, projText, password, passtext, start, changescreen);
        Scene scene = new Scene(grid, 370, 200);
        mainScreen.setOnAction(e -> window.setScene(scene));
        scene.getStylesheets().add("css/phantom.css");
        advanceScene.getStylesheets().add("css/phantom.css");
        window.setResizable(false);
        window.setScene(scene);
        window.show();
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

            int noOfPartition = 2;
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
        task = null;
        service = null;
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
        service.scheduleAtFixedRate(task, delay, period, TimeUnit.MILLISECONDS);
    }
}