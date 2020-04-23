package windows_ui;

import client.Client;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utils.PhantomMouseListener;
import utils.Utils;

import java.awt.MenuItem;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class ScreenCaptureTimer extends Application implements Client.View,
        ChangeListener<Boolean> {
    private static Client.ClientPresenterImpl clientPresenterImpl = null;
    private TrayIcon trayIcon = null;
    private Stage window = null;
    private SystemTray tray = null;
    private TextField txtProjectName = null, txtProjectPassword = null,
            txtImagePartition = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        clientPresenterImpl = new Client.ClientPresenterImpl(this);
        clientPresenterImpl.inflateView(primaryStage);
//        clientPresenterImpl.setSystemTray();
    }

    @Override
    public void inflateView(Stage primaryStage, ArrayList<String> arrRunningApps) {
        window = primaryStage;
        window.setTitle("Phantom Eye");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(10);
        grid.setHgap(10);

        //project name
        Label Projlabel = new Label("Project Name:");
        GridPane.setConstraints(Projlabel, 0, 0);
        txtProjectName = new TextField();
        Platform.runLater( () -> window.requestFocus() );
        txtProjectName.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) {
                if (!txtProjectName.getText().matches("[A-Za-z\\s]+")) {
                    txtProjectName.setText("");
                    Utils.showAlert(Alert.AlertType.ERROR, "Error" , "Please insert characters only!");
                }
            }
        });
        txtProjectName.setPrefWidth(20);
        GridPane.setConstraints(txtProjectName, 1, 0);
        Projlabel.setStyle("-fx-text-fill: #ff9a16;");

        //password
        Label password = new Label("Password:");
        GridPane.setConstraints(password, 0, 2);
        password.setStyle("-fx-text-fill: #ff9a16;");
        txtProjectPassword = new PasswordField();
        GridPane.setConstraints(txtProjectPassword, 1, 2);

        //time
        Label time = new Label("Frame rate :");
        GridPane.setConstraints(time, 0, 0);
        time.setStyle("-fx-text-fill: #ff9a16;");
        TextField frames = new TextField("10");
        frames.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) {
                if (!frames.getText().matches("[0-9]+")) {
                    frames.setText("");
                    Utils.showAlert(Alert.AlertType.ERROR, "Error" , "Please insert numbers only!");
                }
            }
        });
        GridPane.setConstraints(frames, 1, 0);

        //screen selection
        Label screen_parts = new Label("Screen Partition:");
        screen_parts.setStyle("-fx-text-fill: #ff9a16;");
        GridPane.setConstraints(screen_parts, 0, 1);
        txtImagePartition = new TextField("4");
        txtImagePartition.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) {
                if (!txtImagePartition.getText().matches("[0-9]+")) {
                    txtImagePartition.setText("");
                    Utils.showAlert(Alert.AlertType.ERROR, "Error" , "Please insert numbers only!");
                }
            }
        });
        GridPane.setConstraints(txtImagePartition, 1, 1);
        //radio for cursor
       /* RadioButton rb1 = new RadioButton("Yes");
        GridPane.setConstraints(rb1, 1, 1);
        rb1.setStyle("-fx-text-fill: #ff9a16;");
        RadioButton rb2 = new RadioButton("No");
        rb2.setStyle("-fx-text-fill: #ff9a16;");
        GridPane.setConstraints(rb2, 1, 2);
        Label mouse = new Label("Cursor Control:");
        mouse.setStyle("-fx-text-fill: #ff9a16;");
        GridPane.setConstraints(mouse, 0, 1);

        ToggleGroup radio = new ToggleGroup();
        rb1.setToggleGroup(radio);
        rb2.setToggleGroup(radio);*/

        // drop down list
        Label select = new Label("Select Application:");
        select.setStyle("-fx-text-fill: #ff9a16;");
        GridPane.setConstraints(select, 0, 2);
        ChoiceBox<String> choice = new ChoiceBox<>();
        choice.setPrefSize(200.0, 10.0);
        choice.getItems().addAll(arrRunningApps);
        GridPane.setConstraints(choice, 1, 2);
        choice.getSelectionModel().selectedItemProperty().addListener((v, oldvalue, newvalue) -> System.out.println(newvalue));

        //mainscreen button on second screen
        Button mainScreen = new Button("Main Screen");
        GridPane.setConstraints(mainScreen, 1, 5);
        GridPane grid2 = new GridPane();
        grid2.setPadding(new Insets(10, 10, 10, 10));
        grid2.setVgap(10);
        grid2.setHgap(10);
        grid2.getChildren().addAll(mainScreen, time, frames, screen_parts, txtImagePartition, select, choice);
        Scene advanceScene = new Scene(grid2, 370, 200);
        Button changescreen = new Button("Advanced");
        GridPane.setConstraints(changescreen, 0, 5);
        changescreen.setOnAction(e -> window.setScene(advanceScene));

        //start button
        Button start = new Button("Start");
        start.setStyle("-fx-text-fill: green;");

        start.setOnAction(e -> {
            //TODO : The user should not be able to change the text-fields while the app is running.
            if (start.getText().equals("Start") && (txtProjectName.getText().trim().isEmpty() || txtProjectPassword.getText().trim().isEmpty())) {
                clientPresenterImpl.setScreenCaptureRunningStatus(false);
                Utils.showAlert(Alert.AlertType.ERROR, "Error" , "Please insert the username and password!");
            } else if (start.getText().equals("Start")) {
                start.setText("Stop");
                clientPresenterImpl.setScreenCaptureRunningStatus(true);
                clientPresenterImpl.startScreenCapturing();
            } else {
                start.setText("Start");
                clientPresenterImpl.setScreenCaptureRunningStatus(false);
            }
        });

        GridPane.setConstraints(start, 1, 5);
        grid.getChildren().addAll(Projlabel, txtProjectName, password, txtProjectPassword, start, changescreen);
        Scene scene = new Scene(grid, 370, 200);
        mainScreen.setOnAction(e -> window.setScene(scene));
        scene.getStylesheets().add("css/phantom.css");
        advanceScene.getStylesheets().add("css/phantom.css");
        window.setResizable(false);
        window.initStyle(StageStyle.UTILITY);
        window.setScene(scene);
        window.show();

        Platform.setImplicitExit(false);
        window.showingProperty().addListener(this);
    }


    @Override
    public void setSystemTray() {

        if (!SystemTray.isSupported()) return;

        tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage("C:/Capstone/WindowsApp/src/client/os.jpg");


        PhantomMouseListener mouseListener = new PhantomMouseListener() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    openWindow();
                }
            }
        };

        PopupMenu popup = new PopupMenu();
        java.awt.MenuItem openItem = new MenuItem("Open");
        java.awt.MenuItem defaultItem = new MenuItem("Exit");

        java.awt.Font defaultFont = java.awt.Font.decode(null);
        java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
        openItem.setFont(boldFont);
        defaultItem.setFont(boldFont);

        openItem.addActionListener(e -> openWindow());
        defaultItem.addActionListener(e -> System.exit(0));
        popup.add(openItem);
        popup.add(defaultItem);

        trayIcon = new TrayIcon(image, "Phantom", popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(e -> openWindow());
        trayIcon.addMouseListener(mouseListener);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("TrayIcon could not be added.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openWindow() {
        Platform.runLater(() -> {
            if (window != null) {
                window.show();
                window.toFront();

                if (tray != null && trayIcon != null)
                    tray.remove(trayIcon);
            }
        });
    }


    @Override
    public void startClientInitProcess() {
        clientPresenterImpl.initClient();
    }

    @Override
    public void onClientInitializedSuccessfully() throws Exception {
        int noOfPartitions = (int) Math.sqrt(Integer.valueOf(txtImagePartition.getText().trim()));
        String projectName = txtProjectName.getText().trim();
        String projectPassword = txtProjectPassword.getText().trim();
        clientPresenterImpl.sendConnectionAckToServer(noOfPartitions, projectName, projectPassword);
        clientPresenterImpl.waitingForServerAck();
    }

    @Override
    public void onConnectEstablishedSuccessfully() throws Exception {
        clientPresenterImpl.sendMetadataToServer();
        clientPresenterImpl.waitingForServerAck();
    }

    @Override
    public void onMetaDataSentSuccessfully() throws Exception {
        clientPresenterImpl.sendImageFileToServer();
        clientPresenterImpl.waitingForServerAck();
    }

    @Override
    public void onImageSentSuccessfully() throws Exception {
        //Check if the screen capture is enabled/disabled & then init the process again
        Thread.sleep(5000);
        if (clientPresenterImpl.isScreenCaptureRunning()) {
            clientPresenterImpl.sendMetadataToServer();
            clientPresenterImpl.waitingForServerAck();
        } else {
            System.out.println("Screen capture app is disabled");
        }
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
        if (!t1.booleanValue()) {
            clientPresenterImpl.setSystemTray();
        } else {
            if (tray != null && trayIcon != null)
                tray.remove(trayIcon);
        }
    }
}