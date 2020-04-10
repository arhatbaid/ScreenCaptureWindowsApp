package windows_ui;

import client.Client;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;

public class ScreenCaptureTimer extends Application implements Client.View {

    private static Client.ClientPresenterImpl clientPresenterImpl = null;
    //TODO params will be linked with UI
    private int noOfPartitions = 3;
    private String projectName = "phantom";
    private String projectPassword = "1234";
//    private TimerTask task = null;
//    private ScheduledExecutorService service = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        clientPresenterImpl = new Client.ClientPresenterImpl(this, noOfPartitions, projectName, projectPassword);
        clientPresenterImpl.inflateView(primaryStage);
    }

    @Override
    public void inflateView(Stage primaryStage, ArrayList<String> arrRunningApps) {
        Stage window = primaryStage;
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
                start.setText("Stop");
                clientPresenterImpl.setScreenCaptureRunningStatus(true);
            } else if (start.getText().equals("Start") && (projText.getText().equals("") || passtext.getText().equals(""))) {
                clientPresenterImpl.setScreenCaptureRunningStatus(false);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error Dialog");
                alert.setHeaderText("Look, an error dialog");
                alert.setContentText("Please insert the username and password!");
                alert.showAndWait();
            } else {
                start.setText("Start");
                clientPresenterImpl.setScreenCaptureRunningStatus(false);
//                task.cancel();
//                service.shutdown();
//                task = null;
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

    @Override
    public void startClientInitProcess() {
        clientPresenterImpl.initClient();
    }

    @Override
    public void onClientInitializedSuccessfully() throws Exception {
        clientPresenterImpl.sendConnectionAckToServer();
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
}