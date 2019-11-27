// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.slmkiii.view;

import de.mossgrabers.controller.slmkiii.SLMkIIIConfiguration;
import de.mossgrabers.controller.slmkiii.controller.SLMkIIIColorManager;
import de.mossgrabers.controller.slmkiii.controller.SLMkIIIControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.ISceneBank;
import de.mossgrabers.framework.daw.ITrackBank;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.view.AbstractSessionView;
import de.mossgrabers.framework.view.SessionColor;


/**
 * The Session view.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class SessionView extends AbstractSessionView<SLMkIIIControlSurface, SLMkIIIConfiguration>
{
    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public SessionView (final SLMkIIIControlSurface surface, final IModel model)
    {
        super ("Session", surface, model, 2, 8, true);

        final SessionColor isRecording = new SessionColor (SLMkIIIColorManager.SLMKIII_RED, SLMkIIIColorManager.SLMKIII_RED, false);
        final SessionColor isRecordingQueued = new SessionColor (SLMkIIIColorManager.SLMKIII_RED_HALF, SLMkIIIColorManager.SLMKIII_RED_HALF, true);
        final SessionColor isPlaying = new SessionColor (SLMkIIIColorManager.SLMKIII_GREEN_GRASS, SLMkIIIColorManager.SLMKIII_GREEN, false);
        final SessionColor isPlayingQueued = new SessionColor (SLMkIIIColorManager.SLMKIII_GREEN_GRASS, SLMkIIIColorManager.SLMKIII_GREEN, true);
        final SessionColor hasContent = new SessionColor (SLMkIIIColorManager.SLMKIII_AMBER, -1, false);
        final SessionColor noContent = new SessionColor (SLMkIIIColorManager.SLMKIII_BLACK, -1, false);
        final SessionColor recArmed = new SessionColor (SLMkIIIColorManager.SLMKIII_RED_HALF, -1, false);
        this.setColors (isRecording, isRecordingQueued, isPlaying, isPlayingQueued, hasContent, noContent, recArmed);
    }


    /** {@inheritDoc} */
    @Override
    public void onGridNote (final int note, final int velocity)
    {
        final int index = note - 36;
        final int t = index % this.columns;
        final int s = this.rows - 1 - index / this.columns;
        final ITrackBank tb = this.model.getCurrentTrackBank ();

        // Birds-eye-view navigation
        if (this.surface.isShiftPressed ())
        {
            final ISceneBank sceneBank = tb.getSceneBank ();

            // Calculate page offsets
            final int numTracks = tb.getPageSize ();
            final int numScenes = sceneBank.getPageSize ();
            final int trackPosition = tb.getItem (0).getPosition () / numTracks;
            final int scenePosition = sceneBank.getScrollPosition () / numScenes;
            final int selX = trackPosition;
            final int selY = scenePosition;
            final int padsX = this.columns;
            final int padsY = this.rows;
            final int offsetX = selX / padsX * padsX;
            final int offsetY = selY / padsY * padsY;
            tb.scrollTo (offsetX * numTracks + t * padsX);
            sceneBank.scrollTo (offsetY * numScenes + s * padsY);
            return;
        }

        // Duplicate a clip
        final ITrack track = tb.getItem (t);
        if (this.surface.isPressed (ButtonID.DUPLICATE))
        {
            this.surface.setTriggerConsumed (ButtonID.DUPLICATE);
            if (track.doesExist ())
                track.getSlotBank ().getItem (s).duplicate ();
            return;
        }

        super.onGridNote (note, velocity);
    }


    /** {@inheritDoc} */
    @Override
    public int getButtonColor (final ButtonID buttonID)
    {
        final ColorManager colorManager = this.model.getColorManager ();
        final int colorScene = colorManager.getColorIndex (AbstractSessionView.COLOR_SCENE);
        final int colorSceneSelected = colorManager.getColorIndex (AbstractSessionView.COLOR_SELECTED_SCENE);
        final int colorSceneOff = colorManager.getColorIndex (AbstractSessionView.COLOR_SCENE_OFF);

        final ISceneBank sceneBank = this.model.getSceneBank ();

        // TODO
        // final IScene s = sceneBank.getItem (scene);
        // final int color = s.doesExist () ? s.isSelected () ? colorSceneSelected : colorScene :
        // colorSceneOff;
        // this.surface.updateTrigger (ButtonID.SCENE_1 + scene, color);

        return 0;
    }
}