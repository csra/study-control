
package de.citec.csra.studycontrol.jp;

import org.openbase.jps.preset.AbstractJPString;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JPStudyParticipantId extends AbstractJPString {

    public static final String[] COMMANDIDENTIFIER = { "--participant-id"};

    public JPStudyParticipantId() {
        super(COMMANDIDENTIFIER);
    }

    @Override
    protected String getPropertyDefaultValue() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Property can be used to specify initial participant id.";
    }
}
