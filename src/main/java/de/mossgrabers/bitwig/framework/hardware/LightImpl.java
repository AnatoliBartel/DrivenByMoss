// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.hardware;

import de.mossgrabers.framework.controller.hardware.ControlImpl;
import de.mossgrabers.framework.controller.hardware.ILight;

import com.bitwig.extension.controller.api.MultiStateHardwareLight;


/**
 * Implementation of a proxy to a light / LED on a hardware controller.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class LightImpl extends ControlImpl implements ILight
{
    final MultiStateHardwareLight hardwareLight;


    /**
     * Constructor.
     *
     * @param hardwareLight The Bitwig hardware light
     */
    public LightImpl (final MultiStateHardwareLight hardwareLight)
    {
        this.hardwareLight = hardwareLight;
    }


    /** {@inheritDoc} */
    @Override
    public void turnOff ()
    {
        this.hardwareLight.state ().setValue (-1);
    }
}
