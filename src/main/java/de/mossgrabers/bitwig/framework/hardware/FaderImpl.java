// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.hardware;

import de.mossgrabers.bitwig.framework.daw.HostImpl;
import de.mossgrabers.framework.command.core.ContinuousCommand;
import de.mossgrabers.framework.controller.hardware.BindType;
import de.mossgrabers.framework.controller.hardware.ControlImpl;
import de.mossgrabers.framework.controller.hardware.IFader;
import de.mossgrabers.framework.daw.midi.IMidiInput;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.HardwareSlider;


/**
 * Implementation of a proxy to a fader on a hardware controller.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class FaderImpl extends ControlImpl implements IFader
{
    private final HardwareSlider hardwareFader;
    private final ControllerHost host;
    private ContinuousCommand    command;


    /**
     * Constructor.
     *
     * @param host The controller host
     * @param hardwareFader The Bitwig hardware fader
     * @param label The label of the fader
     */
    public FaderImpl (final HostImpl host, final HardwareSlider hardwareFader, final String label)
    {
        this.host = host.getControllerHost ();
        this.hardwareFader = hardwareFader;
        this.hardwareFader.setLabel (label);
    }


    /** {@inheritDoc} */
    @Override
    public IFader bind (final ContinuousCommand command)
    {
        this.command = command;

        this.hardwareFader.addBinding (this.host.createAbsoluteHardwareControlAdjustmentTarget (this::handleValue));
        return this;
    }


    /** {@inheritDoc} */
    @Override
    public void bind (final IMidiInput input, final BindType type, final int value)
    {
        this.bind (input, type, 0, value);
    }


    /** {@inheritDoc} */
    @Override
    public void bind (final IMidiInput input, final BindType type, final int channel, final int value)
    {
        input.bind (this, type, channel, value);
    }


    /** {@inheritDoc} */
    @Override
    public void handleValue (final double value)
    {
        // TODO Support pitchbend
        this.command.execute ((int) Math.round (value * 127.0));
    }


    /** {@inheritDoc} */
    @Override
    public ContinuousCommand getCommand ()
    {
        return this.command;
    }


    /**
     * Get the Bitwig hardware fader proxy.
     *
     * @return The fader proxy
     */
    public HardwareSlider getHardwareFader ()
    {
        return this.hardwareFader;
    }
}
