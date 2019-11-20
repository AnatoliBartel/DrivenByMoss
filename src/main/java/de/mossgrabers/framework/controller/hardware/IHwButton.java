// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller.hardware;

import de.mossgrabers.framework.command.core.ContinuousCommand;
import de.mossgrabers.framework.command.core.TriggerCommand;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Interface for a proxy to a button on a hardware controller.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public interface IHwButton extends IHwInputControl
{
    /**
     * Assign a command to a button, which is triggered by the button.
     *
     * @param command The command to assign
     */
    void bind (TriggerCommand command);


    /**
     * Assign a dynamic command to a button, which is triggered by the button.
     *
     * @param command The command to assign
     */
    void bindDynamic (ContinuousCommand command);


    /**
     * Get the trigger command,
     *
     * @return The command or null if not bound
     */
    TriggerCommand getCommand ();


    /**
     * Add a light / LED to the button.
     *
     * @param light The light to assign
     */
    void addLight (IHwLight light);


    /**
     * Get the light, if any.
     *
     * @return The light or null
     */
    IHwLight getLight ();


    /**
     * Test if the button is in pressed state.
     *
     * @return True if pressed (or long pressed)
     */
    boolean isPressed ();


    /**
     * Test if the button is in long pressed state.
     *
     * @return True if long pressed
     */
    boolean isLongPressed ();


    /**
     * Manually triggers a button press and release.
     *
     * @param event The button event
     */
    void trigger (ButtonEvent event);


    /**
     * Set the consumed state, which means the UP event is not fired on button release.
     */
    void setConsumed ();


    /**
     * Test if the consumed state is set.
     *
     * @return True if set
     */
    boolean isConsumed ();
}
