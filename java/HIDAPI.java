import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDManager;

import java.io.IOException;


/**
 * Utility class to work around an issue with loading JNI libraries from Clojure
 * code. This occurs due to Clojure using its own ClassLoader.
 *
 * @see http://blog.darevay.com/2011/09/on-clojure-swig-and-the-unsatisfiedlinkerror/
 */
public class HIDAPI {

    private static final String library = "hidapi-jni-64";
    private static boolean loaded;


    static {
        try {
            System.loadLibrary(library);
            loaded = true;
        } catch ( UnsatisfiedLinkError e ) {
            System.err.println("Failed to initialize USB HID library: " + e);
            loaded = false;
        }
    }


    /**
     * Loads a USB input device by the vendor and product identifiers. These
     * can be found by inspecting the output of `lsusb` or watching the system
     * log while plugging the device in.
     *
     * @param vendorId   numeric vendor identifier
     * @param productId  numeric product identifier
     * @return the loaded device object
     */
    public static HIDDevice getDevice(
            final int vendorId,
            final int productId)
            throws IOException {

        if ( loaded ) {
            HIDManager manager = HIDManager.getInstance();
            return manager.openById(vendorId, productId, null);
        } else {
            return null;
        }

    }

}
