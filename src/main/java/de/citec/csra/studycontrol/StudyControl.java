package de.citec.csra.studycontrol;

import de.citec.csra.studycontrol.jp.JPStartRecordScript;
import de.citec.csra.studycontrol.jp.JPStopRecordScript;
import de.citec.csra.studycontrol.jp.JPStudyCondition;
import de.citec.csra.studycontrol.jp.JPStudyConditionScriptDirectory;
import de.citec.csra.studycontrol.jp.JPStudyDataPefix;
import de.citec.csra.studycontrol.jp.JPStudyName;
import de.citec.csra.studycontrol.jp.JPStudyParticipantId;
import java.io.File;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.openbase.jps.core.JPService;

public class StudyControl extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        
        JPService.setApplicationName(StudyControl.class);
        JPService.registerProperty(JPStudyName.class);
        JPService.registerProperty(JPStudyDataPefix.class);
        JPService.registerProperty(JPStudyParticipantId.class);
        JPService.registerProperty(JPStudyCondition.class);
        JPService.registerProperty(JPStudyConditionScriptDirectory.class, new File("/home/divine/tmp/condition"));
        JPService.registerProperty(JPStartRecordScript.class, new File("/home/divine/tmp/start.sh"));
        JPService.registerProperty(JPStopRecordScript.class, new File("/home/divine/tmp/stop.sh"));
        JPService.parseAndExitOnError(getParameters().getRaw());
        
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/StudyControlPane.fxml"));
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/main-style.css");
        stage.setTitle("CSRA Study Control");
        stage.setScene(scene);
        stage.show();
    }
    
    @Override
    public void stop() throws Exception {
        super.stop();
        System.exit(0);
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
