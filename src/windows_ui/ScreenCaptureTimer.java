package windows_ui;

import client.Client;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.ImageChunksMetaData;

import java.util.ArrayList;

public class ScreenCaptureTimer extends Application implements Client.View {

    private static Client.ClientPresenterImpl clientPresenterImpl = null;
    ObservableList<String> arrRunningAppsList = FXCollections.observableList(new ArrayList<>());
    private TextField txtProjectName = null, txtProjectPassword = null,
            txtImagePartition = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        clientPresenterImpl = new Client.ClientPresenterImpl(this);
        clientPresenterImpl.inflateView(primaryStage);
        clientPresenterImpl.getRunningTaskList();
    }

    @Override
    public void inflateView(Stage primaryStage) {
        Stage window = primaryStage;
        window.setTitle("Phantom Eye");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(10);
        grid.setHgap(10);

        //project name
        Label Projlabel = new Label("Project Name:");
        GridPane.setConstraints(Projlabel, 0, 0);
        txtProjectName = new TextField();
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
        TextField timetext = new TextField("10");
        GridPane.setConstraints(timetext, 1, 0);

        //screen selection
        Label screen_parts = new Label("Screen Partition:");
        screen_parts.setStyle("-fx-text-fill: #ff9a16;");
        GridPane.setConstraints(screen_parts, 0, 1);
        txtImagePartition = new TextField("4");
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
        choice.getItems().addAll(arrRunningAppsList);
        GridPane.setConstraints(choice, 1, 2);
        choice.getSelectionModel().selectedItemProperty().addListener((v, oldvalue, newvalue) -> System.out.println(newvalue));

        //mainscreen button on second screen
        Button mainScreen = new Button("Main Screen");
        GridPane.setConstraints(mainScreen, 1, 5);
        GridPane grid2 = new GridPane();
        grid2.setPadding(new Insets(10, 10, 10, 10));
        grid2.setVgap(10);
        grid2.setHgap(10);
        grid2.getChildren().addAll(mainScreen, time, timetext, screen_parts, txtImagePartition, select, choice);
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
                clientPresenterImpl.setAppRunningStatus(false);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Please insert the username and password!");
//                alert.setContentText("");
                alert.showAndWait();
            } else if (start.getText().equals("Start")) {
                start.setText("Stop");
                clientPresenterImpl.setAppRunningStatus(true);
                clientPresenterImpl.startApp();
            } else {
                start.setText("Start");
                clientPresenterImpl.setAppRunningStatus(false);
//                task.cancel();
//                service.shutdown();
//                task = null;
            }
        });

        GridPane.setConstraints(start, 1, 5);
        grid.getChildren().addAll(Projlabel, txtProjectName, password, txtProjectPassword, start, changescreen);
        Scene scene = new Scene(grid, 370, 200);
        mainScreen.setOnAction(e -> window.setScene(scene));
        scene.getStylesheets().add("css/phantom.css");
        advanceScene.getStylesheets().add("css/phantom.css");
        window.setResizable(false);
        window.setScene(scene);
        window.show();
    }

    @Override
    public void onTaskListFetched(ArrayList<String> arrTaskList) {
        arrRunningAppsList.clear();
        arrRunningAppsList = FXCollections.observableList(arrTaskList);
    }

    @Override
    public void startClientInitProcess() {
        clientPresenterImpl.initClient();
    }

    @Override
    public void onClientInitializedSuccessfully() {
        int noOfPartitions = Integer.valueOf(txtImagePartition.getText().trim());
        String projectName = txtProjectName.getText().trim();
        String projectPassword = txtProjectPassword.getText().trim();
        clientPresenterImpl.sendConnectionAckToServer(noOfPartitions, projectName, projectPassword);
        clientPresenterImpl.waitingForServerAck();
    }

    @Override
    public void onConnectEstablishedSuccessfully() {
        clientPresenterImpl.startScreenCapture();
    }

    @Override
    public void onScreenCapturedSuccessfully(ImageChunksMetaData[] arrImageChunksmetaData) {
        clientPresenterImpl.sendMetadataToServer(arrImageChunksmetaData);
        clientPresenterImpl.waitingForServerAck();
    }

    @Override
    public void onMetaDataSentSuccessfully(ImageChunksMetaData[] arrImageChunks) {
        clientPresenterImpl.sendImageFileToServer(arrImageChunks);
        clientPresenterImpl.waitingForServerAck();
    }

    @Override
    public void onImageSentSuccessfully() {
        //Check if the screen capture is enabled/disabled & then init the process again
        if (clientPresenterImpl.isScreenCaptureRunning()) {
            clientPresenterImpl.startScreenCapture();
        } else {
            System.out.println("Screen capture app is disabled");
        }
    }
}