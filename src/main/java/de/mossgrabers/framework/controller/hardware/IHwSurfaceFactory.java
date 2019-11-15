// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller.hardware;

import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.ContinuousID;
import de.mossgrabers.framework.controller.OutputID;
import de.mossgrabers.framework.controller.color.ColorEx;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;


/**
 * Interface for a factory to create hardware elements proxies of a hardware controller device.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public interface IHwSurfaceFactory
{
    /**
     * Create a proxy to a hardware button.
     *
     * @param buttonID The button ID to use
     * @param label The label of the button
     * @return The created button
     */
    IHwButton createButton (ButtonID buttonID, String label);


    /**
     * Create a proxy to a hardware light.
     *
     * @param supplier Callback for getting the state of the light
     * @param sendValueConsumer Callback for sending the state to the controller device
     * @param stateToColorFunction Convert the state of the light to a color, which can be displayed
     *            in the simulated GUI
     * @return The created light
     */
    IHwLight createLight (IntSupplier supplier, IntConsumer sendValueConsumer, IntFunction<ColorEx> stateToColorFunction);


    /**
     * Create a proxy to a hardware fader.
     *
     * @param faderID The fader ID to use
     * @param label The label of the button
     * @return The created fader
     */
    IHwFader createFader (ContinuousID faderID, String label);


    /**
     * Create a proxy to a hardware absolute knob.
     * 
     * @param knobID The knob ID to use
     * @param label The label of the knob
     * @return The created knob
     */
    IHwAbsoluteKnob createAbsoluteKnob (ContinuousID knobID, String label);


    /**
     * Create a proxy to a hardware relative knob.
     *
     * @param knobID The knob ID to use
     * @param label The label of the knob
     * @return The created knob
     */
    IHwRelativeKnob createRelativeKnob (ContinuousID knobID, String label);


    /**
     * Create a proxy to a hardware Text display.
     * 
     * @param outputID The ID of the display
     * @param numLines The number of lines of the display
     * @return The created display
     */
    IHwTextDisplay createTextDisplay (OutputID outputID, int numLines);


    /**
     * Flush the state to the hardware device.
     */
    void flush ();
}
