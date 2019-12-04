// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.launchpad.view;

import de.mossgrabers.controller.launchpad.controller.LaunchpadControlSurface;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.daw.DAWColor;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.ITrackBank;
import de.mossgrabers.framework.daw.data.IMasterTrack;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * 8 volume faders.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class VolumeView extends AbstractFaderView
{
    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public VolumeView (final LaunchpadControlSurface surface, final IModel model)
    {
        super (surface, model);
    }


    /** {@inheritDoc} */
    @Override
    public void onValueKnob (final int index, final int value)
    {
        this.model.getCurrentTrackBank ().getItem (index).setVolume (value);
    }


    /** {@inheritDoc} */
    @Override
    protected int getFaderValue (final int index)
    {
        return this.model.getCurrentTrackBank ().getItem (index).getVolume ();
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
            final int color = cm.getColorIndex (DAWColor.getColorIndex (track.getColor ()));
            if (this.trackColors[i] != color)
            {
                this.trackColors[i] = color;
                this.setupFader (i);
            }
            this.surface.setFaderValue (i, track.getVolume ());
        }
    }

    // TODO
    // /** {@inheritDoc} */
    // @Override
    // public void updateSceneButton (final int scene)
    // {
    // final ColorManager cm = this.model.getColorManager ();
    // final IMasterTrack track = this.model.getMasterTrack ();
    // final int sceneMax = 9 * track.getVolume () / this.model.getValueChanger ().getUpperBound ();
    // final int color = cm.getColor (DAWColors.getColorIndex (track.getColor ()));
    // this.surface.setTrigger (this.surface.getSceneTrigger (scene), scene < sceneMax ? color :
    // LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
    // }


    /** {@inheritDoc} */
    @Override
    public void onScene (final int scene, final ButtonEvent event)
    {
        if (event != ButtonEvent.DOWN)
            return;
        final IMasterTrack track = this.model.getMasterTrack ();
        track.setVolume (Math.min (127, (7 - scene) * this.model.getValueChanger ().getUpperBound () / 7));
    }
}