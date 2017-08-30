package de.citec.csra.studycontrol;

import com.jfoenix.controls.JFXSpinner;
import de.citec.csra.studycontrol.jp.JPStartRecordScript;
import de.citec.csra.studycontrol.jp.JPStopRecordScript;
import de.citec.csra.studycontrol.jp.JPStudyCondition;
import de.citec.csra.studycontrol.jp.JPStudyConditionScriptDirectory;
import de.citec.csra.studycontrol.jp.JPStudyDataPefix;
import de.citec.csra.studycontrol.jp.JPStudyName;
import de.citec.csra.studycontrol.jp.JPStudyParticipantId;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Future;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPFile;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.VerificationFailedException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.VariablePrinter;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.visual.javafx.iface.DynamicPane;

/**
 * FXML Controller class
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class StudyControlPaneController implements Initializable, DynamicPane {

    private final BooleanProperty recordingProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty busyProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty recordingValidProperty = new SimpleBooleanProperty(false);
    private final VariablePrinter printer = new VariablePrinter();
    private String recordPath;
    private ExecuteStreamHandler scriptStreamHandler = new ExecuteStreamHandler() {
        @Override
        public void setProcessInputStream(OutputStream os) throws IOException {
            // no script input supported
        }

        @Override
        public void setProcessErrorStream(final InputStream is) throws IOException {
            final BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = in.readLine()) != null) {
                print("ERROR: " + line);
            }
        }

        @Override
        public void setProcessOutputStream(final InputStream is) throws IOException {
            final BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = in.readLine()) != null) {
                print(line);
            }
        }

        @Override
        public void start() throws IOException {
        }

        @Override
        public void stop() throws IOException {
        }
    };

    @FXML
    private ComboBox<String> conditionComboBox;

    @FXML
    private Button recordStartButton;

    @FXML
    private Button recordStopButton;

    @FXML
    private TextField participantIdTextField;

    @FXML
    private Pane participantConfigPane;

    @FXML
    private Pane recordPane;

    @FXML
    private Label recordingStateLabel;

    @FXML
    private Pane recordingStatePane;

    @FXML
    private JFXSpinner recordSpinner;

    @FXML
    private TextField savePath;

    @FXML
    private Pane settingsPane;

    @FXML
    private Pane configPane;

    @FXML
    private TextField studyName;

    @FXML
    private TextArea logArea;

    @FXML
    private CheckBox enableConditionScriptCheckBox;

    @FXML
    private CheckBox enableStartScriptCheckBox;

    @FXML
    private CheckBox enableStopScriptCheckBox;

    @FXML
    private CheckBox enableRSBagRecordCheckBox;

    @FXML
    private CheckBox enableVideoRecordCheckBox;

    private Future currentAction;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
        initContent();
    }

    @Override
    public void initContent() {

        // component setup
        recordStartButton.setDisable(true);

        try {
            studyName.setText(JPService.getProperty(JPStudyName.class).getValue());
            conditionComboBox.getItems().addAll(JPService.getProperty(JPStudyCondition.class).getValue());
            participantIdTextField.setText(JPService.getProperty(JPStudyParticipantId.class).getValue());
            savePath.setText(JPService.getProperty(JPStudyDataPefix.class).getValue());
        } catch (JPNotAvailableException ex) {
            ExceptionPrinter.printHistory(ex, System.err);
            print(ex);
        }

        recordingProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean recording) -> {
            try {
                if (recording) {
                    recordingStatePane.setStyle("-fx-background-color: red");
                    recordingStateLabel.setText("Recording started");
                } else {
                    recordingStatePane.setStyle("-fx-background-color: blue");
                    recordingStateLabel.setText("Recording Stopped");
                }
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(ex, System.err);
                print(ex);
            }
        });

        recordSpinner.visibleProperty().bind(recordingProperty.or(busyProperty));
        recordStartButton.disableProperty().bind(recordingProperty.or(busyProperty));
        recordStopButton.disableProperty().bind(recordingProperty.not().and(recordingValidProperty).or(busyProperty));
        settingsPane.disableProperty().bind(recordingProperty.or(busyProperty));
        configPane.disableProperty().bind(recordingProperty.or(busyProperty));
        participantConfigPane.disableProperty().bind(recordingProperty.or(busyProperty));
        recordPane.disableProperty().bind(recordingValidProperty.not().or(busyProperty));

        recordStartButton.setOnAction((event) -> {
            event.consume();
            try {
                busyProperty.set(true);
                recordingStatePane.setStyle("-fx-background-color: green");
                recordingStateLabel.setText("Setup Recording");
                currentAction = startRecording();
            } catch (Exception ex) {
                print(ex);
            }
        });

        recordStopButton.setOnAction((event) -> {
            event.consume();
            try {
                busyProperty.set(true);
                recordingStatePane.setStyle("-fx-background-color: green");
                recordingStateLabel.setText("Finish Recording");
                currentAction = stopRecording();
            } catch (Exception ex) {
                print(ex);
            }
        });

        studyName.textProperty().addListener((observable, oldValue, newValue) -> {
            updateDynamicContent();
        });

        savePath.textProperty().addListener((observable, oldValue, newValue) -> {
            updateDynamicContent();
        });

        participantIdTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateDynamicContent();
        });

        conditionComboBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            updateDynamicContent();
        });

        logArea.textProperty().addListener((observable, oldValue, newValue) -> {
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    @Override
    public void updateDynamicContent() {
        isValid();
    }

    public void print(final String message) {
        if (Platform.isFxApplicationThread()) {
            internalPrint(message);
        } else {
            Platform.runLater(() -> {
                internalPrint(message);
            });
        }
    }

    public void print(final Exception ex) {
        if (Platform.isFxApplicationThread()) {
            internalPrint(ex);
        } else {
            Platform.runLater(() -> {
                internalPrint(ex);
            });
        }
    }
    
    public void internalPrint(final String message) {
        printer.print(message);
        logArea.setText(printer.getMessages());
        logArea.appendText("");
    }

    public void internalPrint(final Exception ex) {
        ExceptionPrinter.printHistory(ex, printer);
        recordingStatePane.setStyle("-fx-background-color: orange");
        recordingStateLabel.setText("Warning Occured");
        logArea.setText(printer.getMessages());
        logArea.appendText("");
    }

    public boolean isValid() {
        try {
            verifyRecording();
        } catch (VerificationFailedException ex) {
//            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public void verifyRecording() throws VerificationFailedException {
        try {
            if (studyName.getText().isEmpty()) {
                throw new NotAvailableException("StudyName");
            }

            if (savePath.getText().isEmpty()) {
                throw new NotAvailableException("SavePath");
            }

            if (participantIdTextField.getText().isEmpty()) {
                throw new NotAvailableException("Participant Id");
            }

            if (conditionComboBox.getSelectionModel().getSelectedItem() == null) {
                throw new NotAvailableException("Condition");
            }
        } catch (final CouldNotPerformException ex) {
            recordingValidProperty.set(false);
            throw new VerificationFailedException("Recording currently not possible!", ex);
        }
        recordingValidProperty.set(true);
    }

    public Future<Void> startRecording() throws CouldNotPerformException {

        if (currentAction != null) {
            throw new InvalidStateException("There is still a action ongoing!");
        }

        return GlobalCachedExecutorService.submit(() -> {
            try {
                verifyRecording();

                recordPath = savePath.getText() + "/" + studyName.getText() + "/" + conditionComboBox.getSelectionModel().getSelectedItem() + "/" + participantIdTextField.getText();
                print("setup record path to " + recordPath);

                // execute condition script
                if (enableConditionScriptCheckBox.isSelected()) {
                    try {
                        executeScript(loadConditionScript(JPStudyConditionScriptDirectory.class));
                    } catch (final NotAvailableException ex) {
                        print("condition script execution skiped because its not available.");
                    }
                }

                // execute start script
                if (enableStartScriptCheckBox.isSelected()) {
                    try {
                        executeScript(loadScript(JPStartRecordScript.class));
                    } catch (final NotAvailableException ex) {
                        print("start script execution skiped because its not available.");
                    }
                }

                // start rsbag recording
                if (enableRSBagRecordCheckBox.isSelected()) {
                    startRSBagRecording();
                }

                // start video recording
                if (enableVideoRecordCheckBox.isSelected()) {
                    startVideoRecording();
                }
            } catch (CouldNotPerformException ex) {
                Exception exx = new CouldNotPerformException("Could not start recording!", ex);
                print(exx);
                throw exx;
            }

            currentAction = null;

            Platform.runLater(() -> {
                busyProperty.set(false);
                recordingProperty.set(true);
            });
            return null;
        });
    }

    public Future stopRecording() throws CouldNotPerformException, InterruptedException {

        if (currentAction != null) {
            throw new InvalidStateException("There is still a action ongoing!");
        }

        return GlobalCachedExecutorService.submit(() -> {
            try {
                if (enableStopScriptCheckBox.isSelected()) {
                    // execute stop script
                    try {
                        executeScript(loadScript(JPStopRecordScript.class));
                    } catch (final NotAvailableException ex) {
                        print("stop script execution skiped because its not available.");
                    }
                }

                // stop rsbag recording
                if (enableRSBagRecordCheckBox.isSelected()) {
                    stopRSBagRecording();
                }

                // stop video recording
                if (enableVideoRecordCheckBox.isSelected()) {
                    stopVideoRecording();
                }

                print("stopped recording on " + recordPath);
            } catch (CouldNotPerformException ex) {
                Exception exx = new CouldNotPerformException("Could not stop recording!", ex);
                print(exx);
                throw exx;
            }

            currentAction = null;

            Platform.runLater(() -> {
                busyProperty.set(false);
                recordingProperty.set(false);
            });
            return null;
        });
    }

    public File loadScript(final Class<? extends AbstractJPFile> scriptJP) throws CouldNotPerformException {
        try {
            File script = JPService.getProperty(scriptJP).getValue();
            if (!script.exists()) {
                throw new NotAvailableException(script.getAbsoluteFile());
            }
            return script;
        } catch (JPNotAvailableException ex) {
            throw new CouldNotPerformException(ex);
        }
    }

    public File loadConditionScript(final Class<? extends AbstractJPFile> scriptJP) throws CouldNotPerformException {
        try {
            File scriptFolder = JPService.getProperty(scriptJP).getValue();
            if (!scriptFolder.exists()) {
                throw new NotAvailableException("condition script directory");
            }
            return new File(scriptFolder, conditionComboBox.getSelectionModel().getSelectedItem() + ".sh");
        } catch (Exception ex) { 
           throw new CouldNotPerformException(ex);
        }
    }

    public void executeScript(final File script) throws CouldNotPerformException {
        print("execute: " + script.getAbsolutePath());
        try {
            final CommandLine command = CommandLine.parse(script.getAbsolutePath());
            final DefaultExecutor executor = new DefaultExecutor();

            final Map<String, String> env = new HashMap<>();

            env.put("STUDY_NAME", studyName.getText());
            env.put("STUDY_PARTICIPANT_ID", participantIdTextField.getText());
            env.put("STUDY_CONDITION", conditionComboBox.getSelectionModel().getSelectedItem());
            env.put("STUDY_RUN_PREFIX", recordPath);

            executor.setStreamHandler(scriptStreamHandler);
            executor.setExitValue(0);
            executor.execute(command, env);
        } catch (IOException ex) {
            throw new CouldNotPerformException("Could not execute " + script.getAbsolutePath(), ex);
        }
    }

    public static final String SCOPE_VIDEO_RECORD = "/videorecorder";
    public static final String SCOPE_RSBAG_RECORD = "/logger/rsbag/all";

    public void startVideoRecording() throws CouldNotPerformException, InterruptedException {
        print("start video recording... ");
        startRecordServer(SCOPE_VIDEO_RECORD);
    }

    public void stopVideoRecording() throws CouldNotPerformException, InterruptedException {
        print("stop video recording... ");
        stopRecordServer(SCOPE_VIDEO_RECORD);
    }

    public void startRSBagRecording() throws CouldNotPerformException, InterruptedException {
        print("start rsbag recording... ");
        startRecordServer(SCOPE_RSBAG_RECORD);
    }

    public void stopRSBagRecording() throws CouldNotPerformException, InterruptedException {
        print("stop rsbag recording... ");
        stopRecordServer(SCOPE_RSBAG_RECORD);
    }

    public void startRecordServer(final String scope) throws CouldNotPerformException, InterruptedException {
        Thread.sleep(10000);
//        RSBRemoteServer recordServer = RSBFactoryImpl.getInstance().createSynchronizedRemoteServer(scope);
//        recordServer.activate();
//
//        if ((Boolean) recordServer.call("isstarted").getData()) {
//            recordServer.call("stop");
//        }
//
//        if ((Boolean) recordServer.call("isopen").getData()) {
//            recordServer.call("close");
//        }
//
//        recordServer.call("open", savePath);
//        recordServer.call("start");
    }

    public void stopRecordServer(final String scope) throws CouldNotPerformException, InterruptedException {
        Thread.sleep(10000);
//        RSBRemoteServer recordServer = RSBFactoryImpl.getInstance().createSynchronizedRemoteServer(scope);
//        recordServer.activate();
//
//        if ((Boolean) recordServer.call("isstarted").getData()) {
//            recordServer.call("stop");
//        }
//
//        if ((Boolean) recordServer.call("isopen").getData()) {
//            recordServer.call("close");
//        }
    }
}
