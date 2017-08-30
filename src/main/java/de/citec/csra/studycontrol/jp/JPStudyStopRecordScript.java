
package de.citec.csra.studycontrol.jp;

import java.io.File;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPFile;
import org.openbase.jps.tools.FileHandler;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JPStudyStopRecordScript extends AbstractJPFile {

     public static final String[] COMMANDIDENTIFIER = {"--stop-record-script"};

    public JPStudyStopRecordScript() {
        super(COMMANDIDENTIFIER, FileHandler.ExistenceHandling.Must, FileHandler.AutoMode.Off);
    }
    
    @Override
    protected File getPropertyDefaultValue() throws JPNotAvailableException {
        return new File("/tmp/onStop.sh");
    }

    @Override
    public String getDescription() {
        return "Property can be used to specify a script which will be executed after the recording has been stopped.";
    }

}