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
import de.mossgrabers.framework.view.AbstractNoteSequencerView;


/**
 * The sequencer view.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class SequencerView extends AbstractNoteSequencerView<APCControlSurface, APCConfiguration>
{
    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public SequencerView (final APCControlSurface surface, final IModel model)
    {
        super ("Sequencer", surface, model, surface.isMkII ());

        this.numDisplayRows = 5;
        this.numSequencerRows = 4;
    }


    /** {@inheritDoc} */
    @Override
    public void onScene (final int scene, final ButtonEvent event)
    {
        if (event != ButtonEvent.DOWN)
            return;

        if (!this.isActive ())
            return;

        switch (scene)
        {
            case 0:
                this.scales.nextScale ();
                this.notifyScale ();
                break;

            case 1:
                this.scales.prevScale ();
                this.notifyScale ();
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


    /** {@inheritDoc} */
    @Override
    public String getButtonColorID (final ButtonID buttonID)
    {
        if (buttonID == ButtonID.SCENE3)
            return ColorManager.BUTTON_STATE_OFF;
        return this.isActive () ? ColorManager.BUTTON_STATE_ON : ColorManager.BUTTON_STATE_OFF;
    }


    /** {@inheritDoc} */
    @Override
    public void updateNoteMapping ()
    {
        super.updateNoteMapping ();
        this.updateScale ();
    }


    private void notifyScale ()
    {
        final String name = this.scales.getScale ().getName ();
        this.surface.getConfiguration ().setScale (name);
        this.surface.getDisplay ().notify (name);
    }
}