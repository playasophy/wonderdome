import java.util.Arrays;
import java.util.List;

import hypermedia.net.UDP;

import org.playasophy.wonderdome.Wonderdome;
import org.playasophy.wonderdome.input.ButtonEvent;
import org.playasophy.wonderdome.input.InputEvent;


/**
 * This class produces input events from incoming UDP packets.
 *
 * @author Kevin Litwack (kevin.litwack@gmail.com)
 */
public class UDPInput {

    ///// CONSTANTS /////

    // TODO: Don't hardcode these; take them as parameters to constructor.
    private final int PORT_RX = 50000;
    private final String HOST_IP = "localhost";



    ///// PROPERTIES /////

    private UDP udp;
    private Wonderdome wonderdome;



    ///// INITIALIZATION /////

    public UDPInput(final Wonderdome wonderdome) {

        this.wonderdome = wonderdome;

        // Create a UDP object and start listening.
        udp = new UDP(this, PORT_RX, HOST_IP);
        //udp.log(true);
        udp.listen(true);

    }

    private List<String> getParts(String message) {
        return Arrays.asList(message.split("\\|"));
    }

    public void receive(byte[] data, String HOST_IP, int PORT_RX) {

        // Split the incoming message into parts.
        List<String> parts = getParts(new String(data));
        String handler = parts.get(0);
        List<String> arguments = parts.subList(1, parts.size());

        // Send the message to an appropriate handler based on the first part.
        if ( handler.equals("admin") ) {
            handleAdminMessage(arguments);
        } else if ( handler.equals("control") ) {
            handleControlMessage(arguments);
        }

    }

    private void handleAdminMessage(List<String> arguments) {
        // TODO: Validate that there is at least one argument.
        String command = arguments.get(0);
        if ( command.equals("pause") ) {
            wonderdome.pause();
        } else if ( command.equals("resume") ) {
            wonderdome.resume();
        }
    }

    private void handleControlMessage(List<String> arguments) {

        // TODO: Validate that there is at least one argument.
        String button = arguments.get(0);
        ButtonEvent.Id id;
        if ( button.equals("up") ) {
            id = ButtonEvent.Id.UP;
        } else if ( button.equals("down") ) {
            id = ButtonEvent.Id.DOWN;
        } else if ( button.equals("left") ) {
            id = ButtonEvent.Id.LEFT;
        } else if ( button.equals("right") ) {
            id = ButtonEvent.Id.RIGHT;
        } else if ( button.equals("a") ) {
            id = ButtonEvent.Id.A;
        } else if ( button.equals("b") ) {
            id = ButtonEvent.Id.B;
        } else if ( button.equals("select") ) {
            id = ButtonEvent.Id.SELECT;
        } else if ( button.equals("start") ) {
            id = ButtonEvent.Id.START;
        } else {
            System.err.println("Invalid button ID '" + button + "'");
            return;
        }

        ButtonEvent pressed = new ButtonEvent(0, id, ButtonEvent.Type.PRESSED);
        wonderdome.handleEvent(pressed);
        ButtonEvent released = new ButtonEvent(0, id, ButtonEvent.Type.RELEASED);
        wonderdome.handleEvent(released);

    }

}
