
package de.citec.csra.studycontrol.jp;

import java.io.File;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPFile;
import org.openbase.jps.tools.FileHandler;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JPStudyStartRecordScript extends AbstractJPFile {

     public static final String[] COMMANDIDENTIFIER = {"--start-record-script"};

    public JPStudyStartRecordScript() {
        super(COMMANDIDENTIFIER, FileHandler.ExistenceHandling.Must, FileHandler.AutoMode.Off);
    }
    
    @Override
    protected File getPropertyDefaultValue() throws JPNotAvailableException {
        return new File("/tmp/onStart.sh");
    }

    @Override
    public String getDescription() {
        return "Property can be used to specify a script which will be executed before the recording starts.";
    }
}