// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.hardware;

import de.mossgrabers.framework.controller.hardware.AbstractHwControl;
import de.mossgrabers.framework.controller.hardware.IHwLight;

import com.bitwig.extension.controller.api.MultiStateHardwareLight;


/**
 * Implementation of a proxy to a light / LED on a hardware controller.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class HwLightImpl extends AbstractHwControl implements IHwLight
{
    final MultiStateHardwareLight hardwareLight;


    /**
     * Constructor.
     *
     * @param hardwareLight The Bitwig hardware light
     */
    public HwLightImpl (final MultiStateHardwareLight hardwareLight)
    {
        super (null, null);

        this.hardwareLight = hardwareLight;
    }


    /** {@inheritDoc} */
    @Override
    public void turnOff ()
    {
        this.hardwareLight.state ().setValue (-1);
    }


    /** {@inheritDoc} */
    @Override
    public void setBounds (final double x, final double y, final double width, final double height)
    {
        this.hardwareLight.setBounds (x, y, width, height);
    }
}
