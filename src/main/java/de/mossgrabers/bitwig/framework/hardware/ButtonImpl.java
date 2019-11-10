// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.hardware;

import de.mossgrabers.bitwig.framework.midi.MidiInputImpl;
import de.mossgrabers.framework.command.core.TriggerCommand;
import de.mossgrabers.framework.controller.hardware.IButton;
import de.mossgrabers.framework.controller.hardware.ILight;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.utils.ButtonEvent;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.HardwareButton;
import com.bitwig.extension.controller.api.MidiIn;


/**
 * Implementation of a proxy to a button on a hardware controller.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class ButtonImpl implements IButton
{
    private static final int     BUTTON_STATE_INTERVAL = 400;

    private final HardwareButton hardwareButton;
    private final ControllerHost host;
    private TriggerCommand       command;
    private ButtonEvent          state;
    private boolean              isConsumed;
    private ILight               light;


    /**
     * Constructor.
     *
     * @param host The controller host
     * @param hardwareButton The Bitwig hardware button
     * @param label The label of the button
     */
    public ButtonImpl (final ControllerHost host, final HardwareButton hardwareButton, final String label)
    {
        this.host = host;
        this.hardwareButton = hardwareButton;
        this.hardwareButton.setLabel (label);
    }


    /** {@inheritDoc} */
    @Override
    public IButton bind (final TriggerCommand command)
    {
        this.command = command;
        // TODO
        this.hardwareButton.pressedAction ().addBinding (this.host.createAction (this::handleButtonDown, () -> "TODO"));
        this.hardwareButton.releasedAction ().addBinding (this.host.createAction (this::handleButtonRelease, () -> "TODO"));
        return this;
    }


    /**
     * Handle a button release.
     */
    private void handleButtonRelease ()
    {
        this.state = ButtonEvent.UP;
        if (!this.isConsumed)
            this.command.execute (ButtonEvent.UP);
    }


    /**
     * Handle a button press.
     */
    private void handleButtonDown ()
    {
        this.state = ButtonEvent.DOWN;
        this.isConsumed = false;
        this.host.scheduleTask (this::checkButtonState, BUTTON_STATE_INTERVAL);
        this.command.execute (ButtonEvent.DOWN);
    }


    /** {@inheritDoc} */
    @Override
    public void bind (final IMidiInput input, final int cc)
    {
        this.bind (input, 0, cc);
    }


    /** {@inheritDoc} */
    @Override
    public void bind (final IMidiInput input, final int channel, final int cc)
    {
        final MidiIn midiInPort = ((MidiInputImpl) input).getPort ();
        this.hardwareButton.pressedAction ().setActionMatcher (midiInPort.createCCActionMatcher (channel, cc, 127));
        this.hardwareButton.releasedAction ().setActionMatcher (midiInPort.createCCActionMatcher (channel, cc, 0));
    }


    /** {@inheritDoc} */
    @Override
    public void addLight (final ILight light)
    {
        this.light = light;
        this.hardwareButton.setBackgroundLight (((LightImpl) light).hardwareLight);
    }


    /** {@inheritDoc} */
    @Override
    public ILight getLight ()
    {
        return this.light;
    }


    /** {@inheritDoc} */
    @Override
    public boolean isPressed ()
    {
        return this.state == ButtonEvent.DOWN || this.state == ButtonEvent.LONG;
    }


    /** {@inheritDoc} */
    @Override
    public void setConsumed ()
    {
        this.isConsumed = true;
    }


    /** {@inheritDoc} */
    @Override
    public void trigger ()
    {
        this.command.execute (ButtonEvent.DOWN);
        this.command.execute (ButtonEvent.UP);
    }


    /** {@inheritDoc} */
    @Override
    public TriggerCommand getTriggerCommand ()
    {
        return this.command;
    }


    /**
     * If the state of the given button is still down, the state is set to long and an event gets
     * fired.
     */
    private void checkButtonState ()
    {
        if (!this.isPressed ())
            return;
        this.state = ButtonEvent.LONG;
        this.command.execute (ButtonEvent.LONG);
    }
}
