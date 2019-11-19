// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.hardware;

import de.mossgrabers.bitwig.framework.daw.HostImpl;
import de.mossgrabers.bitwig.framework.graphics.BitmapImpl;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.ContinuousID;
import de.mossgrabers.framework.controller.OutputID;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.hardware.IHwAbsoluteKnob;
import de.mossgrabers.framework.controller.hardware.IHwButton;
import de.mossgrabers.framework.controller.hardware.IHwFader;
import de.mossgrabers.framework.controller.hardware.IHwGraphicsDisplay;
import de.mossgrabers.framework.controller.hardware.IHwLight;
import de.mossgrabers.framework.controller.hardware.IHwRelativeKnob;
import de.mossgrabers.framework.controller.hardware.IHwSurfaceFactory;
import de.mossgrabers.framework.controller.hardware.IHwTextDisplay;
import de.mossgrabers.framework.graphics.IBitmap;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.api.HardwareSurface;
import com.bitwig.extension.controller.api.MultiStateHardwareLight;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;


/**
 * Factory for creating hardware elements proxies of a hardware controller device.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class HwSurfaceFactoryImpl implements IHwSurfaceFactory
{
    private final HostImpl        host;
    private final HardwareSurface hardwareSurface;

    private int                   lightCounter = 0;


    /**
     * Constructor.
     *
     * @param host The host
     * @param width The width of the controller device
     * @param height The height of the controller device
     */
    public HwSurfaceFactoryImpl (final HostImpl host, final double width, final double height)
    {
        this.host = host;
        this.hardwareSurface = host.getControllerHost ().createHardwareSurface ();
        this.hardwareSurface.setPhysicalSize (width, height);
    }


    /** {@inheritDoc} */
    @Override
    public IHwButton createButton (final ButtonID buttonID, final String label)
    {
        return new HwButtonImpl (this.host, this.hardwareSurface.createHardwareButton (buttonID.name ()), label);
    }


    /** {@inheritDoc} */
    @Override
    public IHwLight createLight (final IntSupplier supplier, final IntConsumer sendValueConsumer, IntFunction<ColorEx> stateToColorFunction)
    {
        this.lightCounter++;
        final MultiStateHardwareLight hardwareLight = this.hardwareSurface.createMultiStateHardwareLight ("LIGHT" + this.lightCounter, state -> {
            final ColorEx colorEx = stateToColorFunction.apply (state);
            return Color.fromRGB (colorEx.getRed (), colorEx.getGreen (), colorEx.getBlue ());
        });
        hardwareLight.state ().setValueSupplier (supplier);
        hardwareLight.state ().onUpdateHardware (sendValueConsumer);
        return new HwLightImpl (hardwareLight);
    }


    /** {@inheritDoc} */
    @Override
    public IHwFader createFader (final ContinuousID faderID, final String label)
    {
        return new HwFaderImpl (this.host, this.hardwareSurface.createHardwareSlider (faderID.name ()), label);
    }


    /** {@inheritDoc} */
    @Override
    public IHwAbsoluteKnob createAbsoluteKnob (final ContinuousID knobID, final String label)
    {
        return new HwAbsoluteKnobImpl (this.host, this.hardwareSurface.createAbsoluteHardwareKnob (knobID.name ()), label);
    }


    /** {@inheritDoc} */
    @Override
    public IHwRelativeKnob createRelativeKnob (final ContinuousID knobID, final String label)
    {
        return new HwRelativeKnobImpl (this.host, this.hardwareSurface.createRelativeHardwareKnob (knobID.name ()), label);
    }


    /** {@inheritDoc} */
    @Override
    public IHwTextDisplay createTextDisplay (final OutputID outputID, final int numLines)
    {
        return new HwTextDisplayImpl (this.hardwareSurface.createHardwareTextDisplay (outputID.name (), numLines));
    }


    /** {@inheritDoc} */
    @Override
    public IHwGraphicsDisplay createGraphicsDisplay (final OutputID outputID, final IBitmap bitmap)
    {
        return new HwGraphicsDisplayImpl (this.hardwareSurface.createHardwarePixelDisplay (outputID.name (), ((BitmapImpl) bitmap).getBitmap ()));
    }


    /** {@inheritDoc} */
    @Override
    public void flush ()
    {
        this.hardwareSurface.updateHardware ();
    }
}
