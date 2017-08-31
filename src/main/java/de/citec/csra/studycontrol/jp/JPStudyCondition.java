package de.citec.csra.studycontrol.jp;

import java.util.ArrayList;
import java.util.List;
import org.openbase.jps.preset.AbstractJPListString;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JPStudyCondition extends AbstractJPListString {

    public static final String[] COMMANDIDENTIFIER = {"--conditions"};

    public JPStudyCondition() {
        super(COMMANDIDENTIFIER);
    }

    @Override
    protected List<String> getPropertyDefaultValue() {
        final List conditionList = new ArrayList<>();
        conditionList.add("1");
        conditionList.add("2");
        return conditionList;
    }

    @Override
    public String getDescription() {
        return "Property can be used to specify the conditions of this study.";
    }
}
