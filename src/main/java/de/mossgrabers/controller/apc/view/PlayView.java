// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.apc.view;

import de.mossgrabers.controller.apc.APCConfiguration;
import de.mossgrabers.controller.apc.controller.APCControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.AbstractPlayView;
import de.mossgrabers.framework.view.SceneView;


/**
 * The play view.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class PlayView extends AbstractPlayView<APCControlSurface, APCConfiguration> implements SceneView
{
    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public PlayView (final APCControlSurface surface, final IModel model)
    {
        super ("Play", surface, model, surface.isMkII ());
    }


    /** {@inheritDoc} */
    @Override
    public void onGridNote (final int note, final int velocity)
    {
        if (!this.model.canSelectedTrackHoldNotes ())
            return;

        super.onGridNote (note, velocity);

        final int mapped = this.keyManager.map (note);
        if (mapped != -1)
            this.surface.sendMidiEvent (0x90, mapped, velocity);
    }


    /** {@inheritDoc} */
    @Override
    public String getButtonColorID (final ButtonID buttonID)
    {
        final int index = buttonID.ordinal () - ButtonID.SCENE1.ordinal ();
        if (index >= 0 && index < 8)
            return index == 2 ? ColorManager.BUTTON_STATE_OFF : ColorManager.BUTTON_STATE_ON;

        return ColorManager.BUTTON_STATE_OFF;
    }


    /** {@inheritDoc} */
    @Override
    public void onScene (final int scene, final ButtonEvent event)
    {
        if (event != ButtonEvent.DOWN)
            return;

        switch (scene)
        {
            case 0:
                this.scales.nextScale ();
                this.updateScale ();
                break;

            case 1:
                this.scales.prevScale ();
                this.updateScale ();
                break;

            case 2:
                this.scales.toggleChromatic ();
                final boolean isChromatic = this.scales.isChromatic ();
                this.surface.getConfiguration ().setScaleInKey (!isChromatic);
                this.surface.getDisplay ().notify (isChromatic ? "Chromatic" : "In Key");
                break;

            case 3:
                this.onOctaveUp (event);
                break;

            case 4:
                this.onOctaveDown (event);
                break;

            default:
                // Not used
                break;
        }
        this.updateNoteMapping ();
    }


    private void updateScale ()
    {
        final String name = this.scales.getScale ().getName ();
        this.surface.getConfiguration ().setScale (name);
        this.surface.getDisplay ().notify (name);
    }
}