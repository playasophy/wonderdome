import java.io.IOException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.codeminders.hidapi.*;

import org.playasophy.wonderdome.Wonderdome;
import org.playasophy.wonderdome.input.ButtonEvent;


/**
 * This class produces input events from a USB NES Controller.
 *
 * @author Kevin Litwack (kevin.litwack@gmail.com)
 */
public class NESControllerInput {

    ///// CONSTANTS /////

    private static final int BUFFER_SIZE = 2048;
    private static final int NES_CONTROLLER_VENDOR_ID = 4797;
    private static final int NES_CONTROLLER_PRODUCT_ID = 53269;

    public static final Set<ButtonEvent.Id> BUTTON_IDS = new HashSet<ButtonEvent.Id>(Arrays.asList(
        ButtonEvent.Id.LEFT,
        ButtonEvent.Id.RIGHT,
        ButtonEvent.Id.UP,
        ButtonEvent.Id.DOWN,
        ButtonEvent.Id.SELECT,
        ButtonEvent.Id.START,
        ButtonEvent.Id.A,
        ButtonEvent.Id.B
    ));



    ///// PROPERTIES /////

    private Wonderdome wonderdome;
    private final HIDDevice device;
    private final byte[] buffer;
    private NESControllerState currentState;



    ///// STATIC INITIALIZATION /////

    private static boolean libraryInitialized;
    static {
        try {
            System.loadLibrary("hidapi-jni");
            libraryInitialized = true;
        } catch ( UnsatisfiedLinkError e ) {
            System.err.println("Failed to initialize USB HID library; NES Controller will not work.");
            libraryInitialized = false;
        }
    }



    ///// INITIALIZATION /////

    public static NESControllerInput getController(final Wonderdome wonderdome) {

        // Don't try to get a controller if the library wasn't initialized properly.
        if ( !libraryInitialized ) {
            return null;
        }

        NESControllerInput controller = null;
        try {
            HIDDevice device = getHIDDevice(NES_CONTROLLER_VENDOR_ID, NES_CONTROLLER_PRODUCT_ID);
            device.disableBlocking();
            if ( device != null ) {
                controller = new NESControllerInput(wonderdome, device);
            }
        } catch ( Exception e ) {
            System.err.println("Failed to instantiate USB NES controller input:");
            e.printStackTrace();
        }

        return controller;

    }

    private NESControllerInput(final Wonderdome wonderdome, final HIDDevice device) {
        this.wonderdome = wonderdome;
        this.device = device;
        this.buffer = new byte[BUFFER_SIZE];
        this.currentState = new NESControllerState();
    }

    // FIXME: Make sure stuff gets cleaned up properly.
    /*
     * This is code pasted from the usb hid library example; it's probably OK
     * to not do these things, but leaving the code here for reference.
     *
    } finally{
        dev.close();
        hid_mgr.release();
        System.gc();
    }
    */



    ///// PUBLIC METHODS /////

    public void updateState() {
        try {
            int bytesRead = device.read(buffer);
            // FIXME: Don't instantiate a new state every time.
            NESControllerState newState = new NESControllerState(buffer, bytesRead);
            generateEvents(currentState, newState);
            currentState = newState;
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return currentState.toString();
    }



    ///// PRIVATE METHODS /////

    private void generateEvents(final NESControllerState currentState, final NESControllerState newState) {

        for ( ButtonEvent.Id id : BUTTON_IDS ) {

            if ( currentState.getState(id) != newState.getState(id) ) {

                // The event type is determined by the new state of the button.
                ButtonEvent.Type type;
                if ( newState.getState(id) ) {
                    type = ButtonEvent.Type.PRESSED;
                } else {
                    type = ButtonEvent.Type.RELEASED;
                }

                ButtonEvent event = new ButtonEvent(0, id, type);
                wonderdome.handleEvent(event);

            }

        }

    }

    private static HIDDevice getHIDDevice(final String path) throws IOException {
        HIDManager manager = HIDManager.getInstance();
        return manager.openByPath(path);
    }

    private static HIDDevice getHIDDevice(final int vendorId, final int productId) throws IOException {
        HIDManager manager = HIDManager.getInstance();
        return manager.openById(vendorId, productId, null);
    }



    ///// PRIVATE CLASSES /////

    private class NESControllerState {

        private Map<ButtonEvent.Id, Boolean> buttons;

        public NESControllerState() {
            buttons = new HashMap<ButtonEvent.Id, Boolean>();
            for ( ButtonEvent.Id id : BUTTON_IDS ) {
                buttons.put(id, false);
            }
        }

        public NESControllerState(final byte[] buffer, final int bytesRead) {
            this();
            setState(buffer, bytesRead);
        }

        public boolean getState(ButtonEvent.Id id) {
            Boolean b = buttons.get(id);
            return ( b == null? false : b.booleanValue() );
        }

        public void setState(final byte[] buffer, final int bytesRead) {

            // TODO: Verify that bytesRead % 8 == 0.
            // TODO: Handle case where bytesRead > 8, i.e multiple messages.
            switch ( buffer[0] ) {
                case 0:
                    buttons.put(ButtonEvent.Id.LEFT, true);
                    buttons.put(ButtonEvent.Id.RIGHT, false);
                    break;
                case (byte) 0xff:
                    buttons.put(ButtonEvent.Id.LEFT, false);
                    buttons.put(ButtonEvent.Id.RIGHT, true);
                    break;
                default:
                    buttons.put(ButtonEvent.Id.LEFT, false);
                    buttons.put(ButtonEvent.Id.RIGHT, false);
                    break;
            }

            switch ( buffer[1] ) {
                case 0:
                    buttons.put(ButtonEvent.Id.UP, true);
                    buttons.put(ButtonEvent.Id.DOWN, false);
                    break;
                case (byte) 0xff:
                    buttons.put(ButtonEvent.Id.UP, false);
                    buttons.put(ButtonEvent.Id.DOWN, true);
                    break;
                default:
                    buttons.put(ButtonEvent.Id.UP, false);
                    buttons.put(ButtonEvent.Id.DOWN, false);
                    break;
            }

            buttons.put(ButtonEvent.Id.A, (buffer[3] & 0x1) != 0);
            buttons.put(ButtonEvent.Id.B, (buffer[3] & 0x2) != 0);
            buttons.put(ButtonEvent.Id.SELECT, (buffer[4] & 0x1) != 0);
            buttons.put(ButtonEvent.Id.START, (buffer[4] & 0x2) != 0);

        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(getState(ButtonEvent.Id.LEFT) ? "L" : "-");
            sb.append(getState(ButtonEvent.Id.RIGHT) ? "R" : "-");
            sb.append(getState(ButtonEvent.Id.UP) ? "U" : "-");
            sb.append(getState(ButtonEvent.Id.DOWN) ? "D" : "-");
            sb.append(getState(ButtonEvent.Id.SELECT) ? "Se" : "--");
            sb.append(getState(ButtonEvent.Id.START) ? "St" : "--");
            sb.append(getState(ButtonEvent.Id.A) ? "A" : "-");
            sb.append(getState(ButtonEvent.Id.B) ? "B" : "-");
            return sb.toString();
        }

    }

}
