// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.apc.view;

import de.mossgrabers.controller.apc.APCConfiguration;
import de.mossgrabers.controller.apc.controller.APCControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.daw.DAWColor;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.IChannel;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.AbstractDrumView;


/**
 * The drum sequencer.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class DrumView extends AbstractDrumView<APCControlSurface, APCConfiguration>
{
    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public DrumView (final APCControlSurface surface, final IModel model)
    {
        super ("Drum", surface, model, 2, 3);
    }


    /** {@inheritDoc} */
    @Override
    protected String getPadContentColor (final IChannel drumPad)
    {
        return this.surface.isMkII () ? DAWColor.getColorIndex (drumPad.getColor ()) : AbstractDrumView.COLOR_PAD_HAS_CONTENT;
    }


    /** {@inheritDoc} */
    @Override
    public void playNote (final int note, final int velocity)
    {
        this.surface.sendMidiEvent (0x90, note, velocity);
    }


    /** {@inheritDoc} */
    @Override
    public void onScene (final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.DOWN)
            return;

        if (!this.isActive ())
            return;

        switch (index)
        {
            case 0:
                this.changeOctave (event, true, 4);
                break;
            case 1:
                this.changeOctave (event, false, 4);
                break;
            case 3:
                this.onOctaveUp (event);
                break;
            case 4:
                this.onOctaveDown (event);
                break;
            default:
                // Intentionally empty
                break;
        }
    }


    /** {@inheritDoc} */
    @Override
    public String getButtonColorID (final ButtonID buttonID)
    {
        if (buttonID == ButtonID.SCENE3)
            return ColorManager.BUTTON_STATE_OFF;
        return this.isActive () ? ColorManager.BUTTON_STATE_ON : ColorManager.BUTTON_STATE_OFF;
    }
}