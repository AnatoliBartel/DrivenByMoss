// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.launchpad.view;

import de.mossgrabers.controller.launchpad.controller.LaunchpadControlSurface;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.daw.DAWColor;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.ITrackBank;
import de.mossgrabers.framework.daw.data.ISend;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * 8 send faders.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class SendsView extends AbstractFaderView
{
    private int selectedSend;


    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public SendsView (final LaunchpadControlSurface surface, final IModel model)
    {
        super (surface, model);
    }


    /** {@inheritDoc} */
    @Override
    public void onValueKnob (final int index, final int value)
    {
        if (!this.model.isEffectTrackBankActive ())
            this.model.getTrackBank ().getItem (index).getSendBank ().getItem (this.selectedSend).setValue (value);
    }


    /** {@inheritDoc} */
    @Override
    protected int getFaderValue (final int index)
    {
        if (this.model.isEffectTrackBankActive ())
            return 0;
        return this.model.getTrackBank ().getItem (index).getSendBank ().getItem (this.selectedSend).getValue ();
    }


    /** {@inheritDoc} */
    @Override
    public void onScene (final int scene, final ButtonEvent event)
    {
        if (event == ButtonEvent.DOWN)
            this.selectedSend = scene;
    }


    /** {@inheritDoc} */
    @Override
    public void drawGrid ()
    {
        final ColorManager cm = this.model.getColorManager ();
        final ITrackBank tb = this.model.getCurrentTrackBank ();
        for (int i = 0; i < 8; i++)
        {
            final ITrack track = tb.getItem (i);
            final ISend send = track.getSendBank ().getItem (this.selectedSend);
            final int color = cm.getColorIndex (DAWColor.getColorIndex (track.getColor ()));
            if (this.trackColors[i] != color)
            {
                this.trackColors[i] = color;
                this.surface.setupFader (i, color, false);
            }
            this.surface.setFaderValue (i, send.getValue ());
        }
    }

    // TODO
    // /** {@inheritDoc} */
    // @Override
    // public void updateSceneButton (final int scene)
    // {
    // this.surface.setTrigger (this.surface.getSceneTrigger (scene), this.selectedSend == scene ?
    // LaunchpadColors.LAUNCHPAD_COLOR_ORCHID : LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
    // }


    /**
     * Get the selected send channel.
     *
     * @return The number of the channel
     */
    public int getSelectedSend ()
    {
        return this.selectedSend;
    }
}