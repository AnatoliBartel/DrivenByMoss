// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.hardware;

import de.mossgrabers.bitwig.framework.daw.HostImpl;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.hardware.IButton;
import de.mossgrabers.framework.controller.hardware.IFader;
import de.mossgrabers.framework.controller.hardware.ILight;
import de.mossgrabers.framework.controller.hardware.ISurfaceFactory;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.api.HardwareSlider;
import com.bitwig.extension.controller.api.HardwareSurface;
import com.bitwig.extension.controller.api.MultiStateHardwareLight;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;


/**
 * Factory for creating hardware elements proxies of a hardware controller device.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class SurfaceFactoryImpl implements ISurfaceFactory
{
    private final HostImpl        host;
    private final HardwareSurface hardwareSurface;


    /**
     * Constructor.
     *
     * @param host The host
     */
    public SurfaceFactoryImpl (final HostImpl host)
    {
        this.host = host;
        this.hardwareSurface = host.getControllerHost ().createHardwareSurface ();
    }


    /** {@inheritDoc} */
    @Override
    public IButton createButton (final ButtonID buttonID, final String label)
    {
        return new ButtonImpl (this.host, this.hardwareSurface.createHardwareButton (buttonID.name ()), label);
    }


    /** {@inheritDoc} */
    @Override
    public ILight createLight (final IntSupplier supplier, final IntConsumer sendValueConsumer)
    {
        // TODO set correct color transformator
        final MultiStateHardwareLight hardwareLight = this.hardwareSurface.createMultiStateHardwareLight (state -> Color.blackColor ());
        hardwareLight.state ().setValueSupplier (supplier);
        hardwareLight.state ().onUpdateHardware (sendValueConsumer);
        return new LightImpl (hardwareLight);
    }


    /** {@inheritDoc} */
    @Override
    public IFader createFader (final String label)
    {
        // TODO Replace the label with an ID
        final HardwareSlider fader = this.hardwareSurface.createHardwareSlider (label);
        return new FaderImpl (this.host, fader, label);
    }


    /** {@inheritDoc} */
    @Override
    public void flush ()
    {
        this.hardwareSurface.updateHardware ();
    }
}
