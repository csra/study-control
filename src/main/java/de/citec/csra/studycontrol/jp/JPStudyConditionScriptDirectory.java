
package de.citec.csra.studycontrol.jp;

import java.io.File;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPDirectory;
import org.openbase.jps.tools.FileHandler;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JPStudyConditionScriptDirectory extends AbstractJPDirectory {

     public static final String[] COMMANDIDENTIFIER = {"--condition-script-directory"};

    public JPStudyConditionScriptDirectory() {
        super(COMMANDIDENTIFIER, FileHandler.ExistenceHandling.CanExist, FileHandler.AutoMode.Off);
    }
    
    @Override
    protected File getPropertyDefaultValue() throws JPNotAvailableException {
        return new File("/tmp/");
    }

    @Override
    public String getDescription() {
        return "Property can be used to specify the location of study condition scripts which will be executed before the recording starts. The provided file name should be like ${CONDITION_NAME}.sh";
    }
}