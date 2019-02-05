package de.citec.csra.studycontrol;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openbase.bco.dal.lib.layer.unit.ColorableLight;
import org.openbase.bco.dal.remote.layer.unit.Units;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.schedule.FutureProcessor;
import org.slf4j.LoggerFactory;
import org.openbase.type.domotic.state.PowerStateType;
import org.openbase.type.domotic.state.PowerStateType.PowerState;
import org.openbase.type.vision.HSBColorType;

/**
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class RecordLight {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RecordLight.class);

    public static final HSBColorType.HSBColor HSV_COLOR_RED = HSBColorType.HSBColor.newBuilder().setHue(0).setSaturation(100).setBrightness(100).build();
    public static final HSBColorType.HSBColor HSV_COLOR_PURPLE = HSBColorType.HSBColor.newBuilder().setHue(280).setSaturation(100).setBrightness(100).build();
    public static final HSBColorType.HSBColor HSV_COLOR_BLUE = HSBColorType.HSBColor.newBuilder().setHue(260).setSaturation(100).setBrightness(100).build();
    public static final HSBColorType.HSBColor HSV_COLOR_ORANGE = HSBColorType.HSBColor.newBuilder().setHue(60).setSaturation(100).setBrightness(100).build();
    public static final HSBColorType.HSBColor HSV_COLOR_YELLOW = HSBColorType.HSBColor.newBuilder().setHue(100).setSaturation(100).setBrightness(100).build();

    public static final String RECORD_LIGHT_ID = "f1397800-9741-401d-a46f-8bf139c12e92";

    public static final long RECORD_LIGHT_INIT_TIMEOUT = 3000;
    public static final long RECORD_LIGHT_TIMEOUT = 100;

    public static boolean init() {
        try {
            Units.getFutureUnit(RECORD_LIGHT_ID, true, Units.COLORABLE_LIGHT).get(RECORD_LIGHT_INIT_TIMEOUT, TimeUnit.MILLISECONDS).setColor(HSV_COLOR_BLUE);
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
            getLight().setColor(color).get(RECORD_LIGHT_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            ExceptionPrinter.printHistory("Could not control record light!", ex, LOGGER);
        }
    }

    public static void setPowerState(final PowerStateType.PowerState.State powerState) {
        try {
            getLight().setPowerState(powerState).get(RECORD_LIGHT_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            ExceptionPrinter.printHistory("Could not control record light!", ex, LOGGER);
        }
    }

    public static void setNeutralWhite() {
        try {
            getLight().setNeutralWhite().get(RECORD_LIGHT_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            ExceptionPrinter.printHistory("Could not control record light!", ex, LOGGER);
        }
    }
}
