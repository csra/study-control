
package de.citec.csra.studycontrol.jp;

import org.openbase.jps.preset.AbstractJPBoolean;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JPStudyEnableVideoRecording extends AbstractJPBoolean {

    public static final String[] COMMANDIDENTIFIER = { "--enable-video-recording"};

    public JPStudyEnableVideoRecording() {
        super(COMMANDIDENTIFIER);
    }

    @Override
    protected Boolean getPropertyDefaultValue() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Property can be used to initially enable the video recording.";
    }
}
