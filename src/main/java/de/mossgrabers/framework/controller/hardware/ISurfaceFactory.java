// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller.hardware;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;


/**
 * Interface for a factory to create hardware elements proxies of a hardware controller device.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public interface ISurfaceFactory
{
    /**
     * Create a proxy to a hardware button.
     *
     * @param label The label of the button
     * @return The created button
     */
    IButton createButton (String label);


    /**
     * Create a proxy to a hardware light.
     *
     * @param supplier Callback for getting the state of the light
     * @param sendValueConsumer Callback for sending the state to the controller device
     * @return The created light
     */
    ILight createLight (IntSupplier supplier, IntConsumer sendValueConsumer);


    /**
     * Flush the state to the hardware device.
     */
    void flush ();
}
