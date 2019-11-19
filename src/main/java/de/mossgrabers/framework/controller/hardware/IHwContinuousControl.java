// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller.hardware;

import de.mossgrabers.framework.command.core.ContinuousCommand;
import de.mossgrabers.framework.command.core.PitchbendCommand;
import de.mossgrabers.framework.command.core.TriggerCommand;
import de.mossgrabers.framework.daw.midi.IMidiInput;


/**
 * A control on a controller surface which sends continuous values.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public interface IHwContinuousControl extends IHwInputControl
{
    /**
     * Assign a command to the control.
     *
     * @param command The command to assign
     */
    void bind (ContinuousCommand command);


    /**
     * Assign a pitchbend command to the control.
     *
     * @param command The command to assign
     */
    void bind (PitchbendCommand command);


    /**
     * Bind a command which is executed when the control (knob, fader) is touched.
     *
     * @param command The command to bind to touch
     * @param input The MIDI input
     * @param type How to bind
     * @param control The MIDI CC or note to bind
     */
    void bindTouch (TriggerCommand command, IMidiInput input, BindType type, int control);


    /**
     * Get the touch trigger command, if any.
     *
     * @return The command or null if not bound
     */
    TriggerCommand getTouchCommand ();


    /**
     * Get the continuous command.
     *
     * @return The command or null if not bound
     */
    ContinuousCommand getCommand ();


    /**
     * Get the pitchbend command.
     *
     * @return The command or null if not bound
     */
    PitchbendCommand getPitchbendCommand ();


    /**
     * Handle a value update. Only for internal updates.
     *
     * @param value The new value
     */
    void handleValue (double value);
}
