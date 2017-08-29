
package de.citec.csra.studycontrol.jp;

import org.openbase.jps.preset.AbstractJPString;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JPStudyName extends AbstractJPString {

    public static final String[] COMMANDIDENTIFIER = { "--name"};

    public JPStudyName() {
        super(COMMANDIDENTIFIER);
    }

    @Override
    protected String getPropertyDefaultValue() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Property can be used to specify the name of the study.";
    }
}
