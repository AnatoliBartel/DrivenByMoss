// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller.hardware;

import de.mossgrabers.framework.command.core.ContinuousCommand;


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
     * Get the trigger command,
     *
     * @return The command or null if not bound
     */
    ContinuousCommand getCommand ();


    /**
     * Handle a value update. Only for internal updates.
     *
     * @param value The new value
     */
    void handleValue (double value);
}
