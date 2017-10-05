package de.citec.csra.studycontrol;

import java.util.concurrent.Future;
import org.openbase.bco.dal.remote.unit.ColorableLightRemote;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.schedule.FutureProcessor;
import org.slf4j.LoggerFactory;
import rst.domotic.action.ActionFutureType.ActionFuture;
import rst.domotic.state.PowerStateType;
import rst.vision.HSBColorType;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class RecordLight {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RecordLight.class);

    public static final HSBColorType.HSBColor HSV_COLOR_RED = HSBColorType.HSBColor.newBuilder().setHue(0).setSaturation(100).setBrightness(100).build();
    public static final HSBColorType.HSBColor HSV_COLOR_PURPLE = HSBColorType.HSBColor.newBuilder().setHue(280).setSaturation(100).setBrightness(100).build();
    public static final HSBColorType.HSBColor HSV_COLOR_BLUE = HSBColorType.HSBColor.newBuilder().setHue(260).setSaturation(100).setBrightness(100).build();
    public static final HSBColorType.HSBColor HSV_COLOR_ORANGE = HSBColorType.HSBColor.newBuilder().setHue(60).setSaturation(100).setBrightness(100).build();
    public static final HSBColorType.HSBColor HSV_COLOR_YELLOW = HSBColorType.HSBColor.newBuilder().setHue(100).setSaturation(100).setBrightness(100).build();

    public static ColorableLightRemote recordLight;

    public static void init() {
        try {
            recordLight = Units.getUnit("f1397800-9741-401d-a46f-8bf139c12e92", false, Units.COLORABLE_LIGHT);
        } catch (Exception ex) {
            ExceptionPrinter.printHistory("Could not init record light of the control room.", ex, LOGGER);
        }
    }

    public static Future<ActionFuture> setColor(final HSBColorType.HSBColor color) {
        try {
            verify();
            return recordLight.setColor(color);
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not control record light!", ex, LOGGER);
            return FutureProcessor.canceledFuture(ex);
        }
    }

    public static Future<ActionFuture> setPowerState(final PowerStateType.PowerState.State powerState) {
        try {
            verify();
            return recordLight.setPowerState(powerState);
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not control record light!", ex, LOGGER);
            return FutureProcessor.canceledFuture(ex);
        }
    }

    public static Future<ActionFuture> setNeutralWhite() {
        try {
            verify();
            return recordLight.setNeutralWhite();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not control record light!", ex, LOGGER);
            return FutureProcessor.canceledFuture(ex);
        }
    }

    private static void verify() throws CouldNotPerformException {
        if (recordLight == null) {
            throw new NotAvailableException("RecordLight");
        }

        if (!recordLight.isActive() || !recordLight.isDataAvailable()) {
            throw new InvalidStateException("Record light not ready yet!");
        }

        if (!recordLight.isConnected()) {
            throw new InvalidStateException("Can not connect to the record light.!");
        }
    }
}
