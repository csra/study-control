package de.citec.csra.studycontrol;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.openbase.bco.dal.lib.action.Action;
import org.openbase.bco.dal.lib.layer.unit.ColorableLight;
import org.openbase.bco.dal.remote.action.RemoteAction;
import org.openbase.bco.dal.remote.layer.unit.Units;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.pattern.controller.Remote;
import org.openbase.type.domotic.action.ActionParameterType.ActionParameter;
import org.openbase.type.domotic.action.ActionPriorityType.ActionPriority.Priority;
import org.slf4j.LoggerFactory;
import org.openbase.type.domotic.state.PowerStateType;
import org.openbase.type.vision.HSBColorType;

/**
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class RecordLight {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RecordLight.class);

    public static final HSBColorType.HSBColor HSV_COLOR_RED = HSBColorType.HSBColor.newBuilder().setHue(0).setSaturation(1d).setBrightness(1d).build();
    public static final HSBColorType.HSBColor HSV_COLOR_PURPLE = HSBColorType.HSBColor.newBuilder().setHue(280).setSaturation(1d).setBrightness(1d).build();
    public static final HSBColorType.HSBColor HSV_COLOR_BLUE = HSBColorType.HSBColor.newBuilder().setHue(260).setSaturation(1d).setBrightness(1d).build();
    public static final HSBColorType.HSBColor HSV_COLOR_ORANGE = HSBColorType.HSBColor.newBuilder().setHue(30).setSaturation(1d).setBrightness(1d).build();
    public static final HSBColorType.HSBColor HSV_COLOR_YELLOW = HSBColorType.HSBColor.newBuilder().setHue(20).setSaturation(1d).setBrightness(1d).build();

    public static final String RECORD_LIGHT_ID = "f1397800-9741-401d-a46f-8bf139c12e92";

    public static final long RECORD_LIGHT_INIT_TIMEOUT = 5000;
    public static final long RECORD_LIGHT_TIMEOUT = 1000;
    public static final boolean RECORD_LIGHT_AUTO_ACTION_EXTENTION = true;

    private static RemoteAction lastRemoteAction;

    public static final ActionParameter ACTION_PARAMETER = ActionParameter
            .newBuilder()
            .setPriority(Priority.HIGH)
            .setExecutionTimePeriod(Long.MAX_VALUE)
            .build();


    public static boolean init() {
        try {
            lastRemoteAction = new RemoteAction(Units.getFutureUnit(RECORD_LIGHT_ID, true, Units.COLORABLE_LIGHT).get(RECORD_LIGHT_INIT_TIMEOUT, TimeUnit.MILLISECONDS).setColor(HSV_COLOR_BLUE, ACTION_PARAMETER), () -> RECORD_LIGHT_AUTO_ACTION_EXTENTION);
        } catch (Exception ex) {
            ExceptionPrinter.printHistory("Could not init record light!", ex, LOGGER);
            return false;
        }
        return true;
    }

    private static ColorableLight getLight() throws NotAvailableException {
        try {
            return Units.getFutureUnit(RECORD_LIGHT_ID, false, Units.COLORABLE_LIGHT).get(RECORD_LIGHT_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            throw new NotAvailableException("RecordLight", ex);
        }
    }

    public static void setColor(final HSBColorType.HSBColor color) {
        try {
            lastRemoteAction = new RemoteAction(getLight().setColor(color, ACTION_PARAMETER).get(RECORD_LIGHT_TIMEOUT, TimeUnit.MILLISECONDS), () -> RECORD_LIGHT_AUTO_ACTION_EXTENTION);
        } catch (Exception ex) {
            ExceptionPrinter.printHistory("Could not control record light!", ex, LOGGER);
        }
    }

    public static void setPowerState(final PowerStateType.PowerState.State powerState) {
        try {
            lastRemoteAction = new RemoteAction(getLight().setPowerState(powerState, ACTION_PARAMETER).get(RECORD_LIGHT_TIMEOUT, TimeUnit.MILLISECONDS), () -> RECORD_LIGHT_AUTO_ACTION_EXTENTION);
        } catch (Exception ex) {
            ExceptionPrinter.printHistory("Could not control record light!", ex, LOGGER);
        }
    }

    public static void setNeutralWhite() {
        try {
            lastRemoteAction = new RemoteAction(getLight().setNeutralWhite(ACTION_PARAMETER).get(RECORD_LIGHT_TIMEOUT, TimeUnit.MILLISECONDS), () -> RECORD_LIGHT_AUTO_ACTION_EXTENTION);
        } catch (Exception ex) {
            ExceptionPrinter.printHistory("Could not control record light!", ex, LOGGER);
        }
    }

    public static void cancelLastAction() throws ExecutionException, InterruptedException {
        if (lastRemoteAction == null) {
            return;
        }

        lastRemoteAction.cancel().get();
    }
}
