// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.hardware;

import de.mossgrabers.bitwig.framework.daw.HostImpl;
import de.mossgrabers.framework.command.core.ContinuousCommand;
import de.mossgrabers.framework.command.core.TriggerCommand;
import de.mossgrabers.framework.controller.hardware.AbstractHwContinuousControl;
import de.mossgrabers.framework.controller.hardware.BindType;
import de.mossgrabers.framework.controller.hardware.IHwAbsoluteKnob;
import de.mossgrabers.framework.daw.midi.IMidiInput;

import com.bitwig.extension.controller.api.AbsoluteHardwareKnob;
import com.bitwig.extension.controller.api.ControllerHost;


/**
 * Implementation of a proxy to an absolute knob on a hardware controller.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class HwAbsoluteKnobImpl extends AbstractHwContinuousControl implements IHwAbsoluteKnob
{
    private final AbsoluteHardwareKnob hardwareKnob;
    private final ControllerHost       controllerHost;


    /**
     * Constructor.
     *
     * @param host The controller host
     * @param hardwareKnob The Bitwig hardware knob
     * @param label The label of the knob
     */
    public HwAbsoluteKnobImpl (final HostImpl host, final AbsoluteHardwareKnob hardwareKnob, final String label)
    {
        super (host, label);

        this.controllerHost = host.getControllerHost ();
        this.hardwareKnob = hardwareKnob;
        this.hardwareKnob.setLabel (label);
    }


    /** {@inheritDoc} */
    @Override
    public void bind (final ContinuousCommand command)
    {
        super.bind (command);
        this.hardwareKnob.addBinding (this.controllerHost.createAbsoluteHardwareControlAdjustmentTarget (this::handleValue));
    }


    /** {@inheritDoc} */
    @Override
    public void bind (final IMidiInput input, final BindType type, final int channel, final int value)
    {
        input.bind (this, type, channel, value);
    }


    /** {@inheritDoc} */
    @Override
    public void bindTouch (final TriggerCommand command, final IMidiInput input, final BindType type, final int control)
    {
        // No touch on absolute knob
    }


    /** {@inheritDoc} */
    @Override
    public void handleValue (final double value)
    {
        // TODO Support pitchbend
        this.command.execute ((int) Math.round (value * 127.0));
    }


    /**
     * Get the Bitwig hardware knob proxy.
     *
     * @return The knob proxy
     */
    public AbsoluteHardwareKnob getHardwareKnob ()
    {
        return this.hardwareKnob;
    }


    /** {@inheritDoc} */
    @Override
    public void setBounds (double x, double y, double width, double height)
    {
        this.hardwareKnob.setBounds (x, y, width, height);
    }
}
