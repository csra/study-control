package de.citec.csra.studycontrol;

public class StudyControlLauncher {

    public static void main(String[] args) {
        // This is the javafx workaround to probably link the javafx dependencies.
        // Without it, the application can only be run via the javafx plugin
        // if the main class extends the javafx application class.
        StudyControl.main(args);
    }
}
