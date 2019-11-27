// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.hardware;

import de.mossgrabers.bitwig.framework.daw.HostImpl;
import de.mossgrabers.framework.controller.hardware.AbstractHwControl;
import de.mossgrabers.framework.controller.hardware.BindType;
import de.mossgrabers.framework.controller.hardware.IHwPianoKeyboard;
import de.mossgrabers.framework.daw.midi.IMidiInput;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.PianoKeyboard;


/**
 * Implementation of a proxy to a fader on a hardware controller.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class HwPianoKeyboardImpl extends AbstractHwControl implements IHwPianoKeyboard
{
    private final PianoKeyboard  hardwarePianoKeyboard;
    private final ControllerHost controllerHost;


    /**
     * Constructor.
     *
     * @param host The controller host
     * @param hardwarePianoKeyboard The Bitwig hardware piano keyboard
     */
    public HwPianoKeyboardImpl (final HostImpl host, final PianoKeyboard hardwarePianoKeyboard)
    {
        super (host, "");

        this.controllerHost = host.getControllerHost ();
        this.hardwarePianoKeyboard = hardwarePianoKeyboard;
    }


    /** {@inheritDoc} */
    @Override
    public void setBounds (final double x, final double y, final double width, final double height)
    {
        this.hardwarePianoKeyboard.setBounds (x, y, width, height);
    }


    @Override
    public void bind (IMidiInput input, BindType type, int control)
    {
        // TODO Auto-generated method stub

    }


    @Override
    public void bind (IMidiInput input, BindType type, int channel, int control)
    {
        // TODO Auto-generated method stub

    }
}
