// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller.hardware;

import de.mossgrabers.framework.command.core.TriggerCommand;
import de.mossgrabers.framework.daw.midi.IMidiInput;


/**
 * Interface for a proxy to a button on a hardware controller.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public interface IButton
{
    /**
     * Assign a command to a button, which is triggered by the button.
     *
     * @param command The command to assign
     * @return The button for fluent interface
     */
    IButton trigger (TriggerCommand command);


    /**
     * Bind a midi CC coming from an MIDI input to the button.
     * 
     * @param input The MIDI input
     * @param cc The MIDI CC
     */
    void bind (IMidiInput input, int cc);


    /**
     * Bind a midi CC coming from an MIDI input to the button.
     * 
     * @param input The MIDI input
     * @param channel The MIDI channel
     * @param cc The MIDI CC
     */
    void bind (IMidiInput input, int channel, int cc);


    /**
     * Add a light / LED to the button.
     *
     * @param light The light to assign
     */
    void addLight (ILight light);


    /**
     * Get the light, if any.
     *
     * @return The light or null
     */
    ILight getLight ();


    /**
     * Test if the button is in pressed state.
     *
     * @return True if pressed
     */
    boolean isPressed ();


    /**
     * Set the consumed state, which means the UP event is not fired on button release.
     */
    void setConsumed ();
}
