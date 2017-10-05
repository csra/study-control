package de.citec.csra.studycontrol;

import com.jfoenix.controls.JFXSpinner;
import de.citec.csra.studycontrol.jp.JPStudyCondition;
import de.citec.csra.studycontrol.jp.JPStudyConditionScriptDirectory;
import de.citec.csra.studycontrol.jp.JPStudyDataPefix;
import de.citec.csra.studycontrol.jp.JPStudyEnableRSBagRecording;
import de.citec.csra.studycontrol.jp.JPStudyEnableVideoRecording;
import de.citec.csra.studycontrol.jp.JPStudyName;
import de.citec.csra.studycontrol.jp.JPStudyParticipantId;
import de.citec.csra.studycontrol.jp.JPStudyStartRecordScript;
import de.citec.csra.studycontrol.jp.JPStudyStopRecordScript;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.exception.printer.VariablePrinter;
import org.openbase.jul.extension.rsb.com.RSBFactoryImpl;
import org.openbase.jul.extension.rsb.iface.RSBRemoteServer;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.visual.javafx.iface.DynamicPane;
import org.slf4j.LoggerFactory;

/**
 * FXML Controller class
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class StudyControlPaneController implements Initializable, DynamicPane {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StudyControlPaneController.class);

    private final BooleanProperty recordingProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty busyProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty recordingValidProperty = new SimpleBooleanProperty(false);
    private final VariablePrinter printer = new VariablePrinter();

    private String recordPath;
    private String recordFile;

    private ExecuteStreamHandler scriptStreamHandler = new ExecuteStreamHandler() {
        @Override
        public void setProcessInputStream(OutputStream os) throws IOException {
            // no script input supported yet.
            // use env variables to pass properties to the scripts.
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
    private Button cancelButton;

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

        // initial component setup
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

        try {
            enableRSBagRecordCheckBox.setSelected(JPService.getProperty(JPStudyEnableRSBagRecording.class).getValue());
        } catch (JPNotAvailableException ex) {
            enableRSBagRecordCheckBox.setSelected(false);
        }

        try {
            enableVideoRecordCheckBox.setSelected(JPService.getProperty(JPStudyEnableVideoRecording.class).getValue());
        } catch (JPNotAvailableException ex) {
            enableVideoRecordCheckBox.setSelected(false);
        }

        try {
            enableStartScriptCheckBox.setSelected(JPService.getProperty(JPStudyStartRecordScript.class).isParsed());
        } catch (JPNotAvailableException ex) {
            enableStartScriptCheckBox.setSelected(false);
        }

        try {
            enableStopScriptCheckBox.setSelected(JPService.getProperty(JPStudyStopRecordScript.class).isParsed());
        } catch (JPNotAvailableException ex) {
            enableStopScriptCheckBox.setSelected(false);
        }

        try {
            enableConditionScriptCheckBox.setSelected(JPService.getProperty(JPStudyConditionScriptDirectory.class).isParsed());
        } catch (JPNotAvailableException ex) {
            enableConditionScriptCheckBox.setSelected(false);
        }

        recordingProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean recording) -> {
            try {
                if (recording) {
                    recordingStatePane.setStyle("-fx-background-color: red");
                    recordingStateLabel.setText("Recording started");
                    RecordLight.setColor(RecordLight.HSV_COLOR_RED);
                } else {
                    recordingStatePane.setStyle("-fx-background-color: blue");
                    recordingStateLabel.setText("Recording Stopped");
                    RecordLight.setColor(RecordLight.HSV_COLOR_BLUE);
                }
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(ex, System.err);
                print(ex);
            }
        });

        recordSpinner.visibleProperty().bind(recordingProperty.or(busyProperty));
        recordStartButton.disableProperty().bind(recordingProperty.or(busyProperty));
        recordStopButton.disableProperty().bind(recordingProperty.not().and(recordingValidProperty).or(busyProperty));
        cancelButton.disableProperty().bind(busyProperty.not());
        settingsPane.disableProperty().bind(recordingProperty.or(busyProperty));
        configPane.disableProperty().bind(recordingProperty.or(busyProperty));
        participantConfigPane.disableProperty().bind(recordingProperty.or(busyProperty));
        recordPane.disableProperty().bind(recordingValidProperty.not());

        recordStartButton.setOnAction((event) -> {
            event.consume();
            try {
                busyProperty.set(true);
                recordingStatePane.setStyle("-fx-background-color: green");
                recordingStateLabel.setText("Setup Recording");
                RecordLight.setColor(RecordLight.HSV_COLOR_PURPLE);
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
                RecordLight.setColor(RecordLight.HSV_COLOR_PURPLE);
                currentAction = stopRecording();
            } catch (Exception ex) {
                print(ex);
            }
        });

        cancelButton.setOnAction((event) -> {
            event.consume();
            try {
                recordingStatePane.setStyle("-fx-background-color: yellow");
                recordingStateLabel.setText("Cancel Action");
                RecordLight.setColor(RecordLight.HSV_COLOR_ORANGE);
                Future action = currentAction;
                if (action != null && !action.isDone()) {
                    action.cancel(true);
                    synchronized (action) {
                        action.wait();
                    }
                }
                busyProperty.set(false);
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

        RecordLight.setNeutralWhite();
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
        RecordLight.setColor(RecordLight.HSV_COLOR_ORANGE);
        recordingStateLabel.setText("Warning Occured");
        logArea.setText(printer.getMessages());
        logArea.appendText("");
    }

    public boolean isValid() {
        try {
            verifyRecording();
        } catch (VerificationFailedException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.DEBUG);
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

                recordPath = savePath.getText() + "/" + studyName.getText() + "/participant-" + participantIdTextField.getText();
                recordFile = recordPath + "/" + "condition-" + conditionComboBox.getSelectionModel().getSelectedItem();
                print("setup record path to " + recordPath);
                print("setup record file to " + recordFile);

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
                        executeScript(loadScript(JPStudyStartRecordScript.class));
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

                // mark recording as started
                Platform.runLater(() -> {
                    recordingProperty.set(true);
                });
            } catch (Exception ex) {
                Exception exx = new CouldNotPerformException("Could not start recording!", ex);
                print(exx);
                throw exx;
            } finally {
                clearAction();
            }
            return null;
        });
    }

    private void clearAction() {
        currentAction = null;

        Platform.runLater(() -> {
            busyProperty.set(false);
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
                        executeScript(loadScript(JPStudyStopRecordScript.class));
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

                print("stopped recording on " + recordFile);

                // mark recording as stopped
                Platform.runLater(() -> {
                    recordingProperty.set(false);
                });
            } catch (Exception ex) {
                Exception exx = new CouldNotPerformException("Could not stop recording!", ex);
                print(exx);
                throw exx;
            } finally {
                clearAction();
            }
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

            final File conditionScript = new File(scriptFolder, conditionComboBox.getSelectionModel().getSelectedItem() + ".sh");
            if (!conditionScript.exists()) {
                throw new NotAvailableException("condition script");
            }

            return conditionScript;
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
            env.put("STUDY_RECORD_PATH", recordPath);
            env.put("STUDY_RECORD_FILE", recordFile);

            executor.setStreamHandler(scriptStreamHandler);
            executor.setExitValue(0);
            executor.execute(command, env);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not execute " + script.getAbsolutePath(), ex);
        }
    }

    public static final String SCOPE_VIDEO_RECORD = "/videorecorder";
    public static final String SCOPE_RSBAG_RECORD = "/logger/rsbag/all";

    public void startVideoRecording() throws Exception, InterruptedException {
        print("start video recording... ");
        startRecordServer(SCOPE_VIDEO_RECORD, recordFile);
    }

    public void stopVideoRecording() throws Exception, InterruptedException {
        print("stop video recording... ");
        stopRecordServer(SCOPE_VIDEO_RECORD);
    }

    public void startRSBagRecording() throws Exception, InterruptedException {
        print("start rsbag recording... ");
        startRecordServer(SCOPE_RSBAG_RECORD, recordFile + ".tide");
    }

    public void stopRSBagRecording() throws Exception, InterruptedException {
        print("stop rsbag recording... ");
        stopRecordServer(SCOPE_RSBAG_RECORD);
    }

    public void startRecordServer(final String scope, final String recordFile) throws Exception, InterruptedException {
        RSBRemoteServer recordServer = RSBFactoryImpl.getInstance().createSynchronizedRemoteServer(scope);
        recordServer.activate();

        if ((Boolean) recordServer.call("isstarted").getData()) {
            print("there is still a recording ongoing which will now be finished via record server" + scope);
            stopRecordServer(scope);
        }

        if (recordServer.call("isopen").getData() instanceof String) {
            print("there is still a record file opened which will now be closed via record server" + scope);
            stopRecordServer(scope);
        }

        // open
        print("open new file via record server " + scope);
        Future openFuture = recordServer.callAsync("ensuredirectoryandopen", recordFile);
        try {
            openFuture.get(10, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException ex) {
            print("record server" + scope + " does not respont to \"ensuredirectoryandopen\" . Maybe the started version does not supported this feature. Fallback solution will be applied.");

            // cancel task if execution is not possible
            openFuture.cancel(true);

            // try fallback solution in case the record server does not support the "ensuredirectoryandopen" feature
            recordServer.call("open", recordFile);
        }

        // verify open
        if (!recordServer.call("isopen").getData().equals(recordFile)) {
            throw new VerificationFailedException("could not open record file via record server " + scope);
        }

        // start
        print("try to start recording via record server " + scope);
        recordServer.call("start");

        // verify start
        if (!(Boolean) recordServer.call("isstarted").getData()) {
            throw new VerificationFailedException("could not start recording via record server " + scope);
        }

        print("recording successfully started on record server " + scope);
    }

    public void stopRecordServer(final String scope) throws Exception, InterruptedException {
        RSBRemoteServer recordServer = RSBFactoryImpl.getInstance().createSynchronizedRemoteServer(scope);
        recordServer.activate();

        if ((Boolean) recordServer.call("isstarted").getData()) {
            recordServer.call("stop");
        }

        if (recordServer.call("isopen").getData() instanceof String) {
            recordServer.call("close");
        }

        // verify
        if ((Boolean) recordServer.call("isstarted").getData()) {
            throw new VerificationFailedException("could not stop recording via record server " + scope, new InvalidStateException("record server " + scope + " does not respont!"));
        }

        if (recordServer.call("isopen").getData() instanceof String) {
            throw new VerificationFailedException("could not close record file via record server " + scope);
        }

        print("recording stopped on record server " + scope);
    }
}
