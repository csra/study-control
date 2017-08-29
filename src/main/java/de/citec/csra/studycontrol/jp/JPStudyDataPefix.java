
package de.citec.csra.studycontrol.jp;

import org.openbase.jps.preset.AbstractJPString;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JPStudyDataPefix extends AbstractJPString {

    public static final String[] COMMANDIDENTIFIER = { "--data-prefix"};

    public JPStudyDataPefix() {
        super(COMMANDIDENTIFIER);
    }

    @Override
    protected String getPropertyDefaultValue() {
        return "/vol/csra/data/persistent/study";
    }

    @Override
    public String getDescription() {
        return "Property can be used to specify the data path where all the recordings should be stored. Be aware that this path is just a prefix and the study name  + condition + participant id are automatically added as sub folders.";
    }
}
