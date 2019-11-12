// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.hardware;

import de.mossgrabers.bitwig.framework.daw.HostImpl;
import de.mossgrabers.framework.command.core.TriggerCommand;
import de.mossgrabers.framework.controller.hardware.AbstractButton;
import de.mossgrabers.framework.controller.hardware.BindType;
import de.mossgrabers.framework.controller.hardware.IButton;
import de.mossgrabers.framework.controller.hardware.ILight;
import de.mossgrabers.framework.daw.midi.IMidiInput;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.HardwareButton;


/**
 * Implementation of a proxy to a button on a hardware controller.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class ButtonImpl extends AbstractButton
{
    private final HardwareButton hardwareButton;


    /**
     * Constructor.
     *
     * @param host The controller host
     * @param hardwareButton The Bitwig hardware button
     * @param label The label of the button
     */
    public ButtonImpl (final HostImpl host, final HardwareButton hardwareButton, final String label)
    {
        super (host, label);

        this.hardwareButton = hardwareButton;
        this.hardwareButton.setLabel (label);
    }


    /** {@inheritDoc} */
    @Override
    public IButton bind (final TriggerCommand command)
    {
        final ControllerHost controllerHost = ((HostImpl) this.host).getControllerHost ();

        // TODO Add a description text
        this.hardwareButton.pressedAction ().addBinding (controllerHost.createAction (this::handleButtonPressed, () -> "TODO"));
        this.hardwareButton.releasedAction ().addBinding (controllerHost.createAction (this::handleButtonRelease, () -> "TODO"));

        return super.bind (command);
    }


    /** {@inheritDoc} */
    @Override
    public void bind (final IMidiInput input, final BindType type, final int channel, final int value)
    {
        input.bind (this, type, channel, value);
    }


    /** {@inheritDoc} */
    @Override
    public void addLight (final ILight light)
    {
        super.addLight (light);

        this.hardwareButton.setBackgroundLight (((LightImpl) light).hardwareLight);
    }


    /**
     * Get the Bitwig hardware button proxy.
     *
     * @return The button proxy
     */
    public HardwareButton getHardwareButton ()
    {
        return this.hardwareButton;
    }
}
