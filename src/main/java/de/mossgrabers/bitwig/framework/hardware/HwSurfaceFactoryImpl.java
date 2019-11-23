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
import com.bitwig.extension.controller.api.HardwareButton;
import com.bitwig.extension.controller.api.HardwareLightVisualState;
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

    private int                   controlCounter = 0;


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
        final String id = this.createID (buttonID.name ());
        final HardwareButton hwButton = this.hardwareSurface.createHardwareButton (id);
        return new HwButtonImpl (this.host, hwButton, label);
    }


    /** {@inheritDoc} */
    @Override
    public IHwLight createLight (final IntSupplier supplier, final IntConsumer sendValueConsumer, final IntFunction<ColorEx> stateToColorFunction, final IHwButton button)
    {
        final String id = this.createID ("LIGHT");

        final MultiStateHardwareLight hardwareLight = this.hardwareSurface.createMultiStateHardwareLight (id, encodedColorState -> {

            final int colorIndex = encodedColorState & 0xFF;
            final int blinkColorIndex = (encodedColorState >> 8) & 0xFF;
            final boolean blinkFast = ((encodedColorState >> 16) & 1) > 0;

            final ColorEx colorEx = stateToColorFunction.apply (colorIndex);
            final Color color = Color.fromRGB (colorEx.getRed (), colorEx.getGreen (), colorEx.getBlue ());

            // TODO needs a Bitwig fix - setLabelColor can only be called during init
            // if (button != null)
            // {
            // final ColorEx contrastColorEx = ColorEx.calcContrastColor (colorEx);
            // final Color contrastColor = Color.fromRGB (contrastColorEx.getRed (),
            // contrastColorEx.getGreen (), contrastColorEx.getBlue ());
            // ((HwButtonImpl) button).getHardwareButton ().setLabelColor (contrastColor);
            // }

            if (blinkColorIndex <= 0 || blinkColorIndex >= 128)
                return HardwareLightVisualState.createForColor (color);

            final ColorEx blinkColorEx = stateToColorFunction.apply (blinkColorIndex);
            final Color blinkColor = Color.fromRGB (blinkColorEx.getRed (), blinkColorEx.getGreen (), blinkColorEx.getBlue ());
            final double blinkTimeInSec = blinkFast ? 0.5 : 1;
            return HardwareLightVisualState.createBlinking (blinkColor, color, blinkTimeInSec, blinkTimeInSec);

        });
        hardwareLight.state ().setValueSupplier (supplier);
        hardwareLight.state ().onUpdateHardware (sendValueConsumer);
        final HwLightImpl lightImpl = new HwLightImpl (hardwareLight);

        if (button != null)
            button.addLight (lightImpl);

        return lightImpl;
    }


    /** {@inheritDoc} */
    @Override
    public IHwFader createFader (final ContinuousID faderID, final String label)
    {
        final String id = this.createID (faderID.name ());
        return new HwFaderImpl (this.host, this.hardwareSurface.createHardwareSlider (id), label);
    }


    /** {@inheritDoc} */
    @Override
    public IHwAbsoluteKnob createAbsoluteKnob (final ContinuousID knobID, final String label)
    {
        final String id = this.createID (knobID.name ());
        return new HwAbsoluteKnobImpl (this.host, this.hardwareSurface.createAbsoluteHardwareKnob (id), label);
    }


    /** {@inheritDoc} */
    @Override
    public IHwRelativeKnob createRelativeKnob (final ContinuousID knobID, final String label)
    {
        final String id = this.createID (knobID.name ());
        return new HwRelativeKnobImpl (this.host, this.hardwareSurface.createRelativeHardwareKnob (id), label);
    }


    /** {@inheritDoc} */
    @Override
    public IHwTextDisplay createTextDisplay (final OutputID outputID, final int numLines)
    {
        final String id = this.createID (outputID.name ());
        return new HwTextDisplayImpl (this.hardwareSurface.createHardwareTextDisplay (id, numLines));
    }


    /** {@inheritDoc} */
    @Override
    public IHwGraphicsDisplay createGraphicsDisplay (final OutputID outputID, final IBitmap bitmap)
    {
        final String id = this.createID (outputID.name ());
        return new HwGraphicsDisplayImpl (this.hardwareSurface.createHardwarePixelDisplay (id, ((BitmapImpl) bitmap).getBitmap ()));
    }


    /** {@inheritDoc} */
    @Override
    public void flush ()
    {
        this.hardwareSurface.updateHardware ();
    }


    private String createID (final String name)
    {
        this.controlCounter++;
        return this.controlCounter + "_" + name;
    }
}
