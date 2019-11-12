// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.push.view;

import de.mossgrabers.controller.push.PushConfiguration;
import de.mossgrabers.controller.push.command.trigger.SelectSessionViewCommand;
import de.mossgrabers.controller.push.controller.PushColors;
import de.mossgrabers.controller.push.controller.PushControlSurface;
import de.mossgrabers.framework.command.core.TriggerCommand;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.ISceneBank;
import de.mossgrabers.framework.daw.ITrackBank;
import de.mossgrabers.framework.daw.data.IScene;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.mode.BrowserActivator;
import de.mossgrabers.framework.mode.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.AbstractSessionView;
import de.mossgrabers.framework.view.SessionColor;
import de.mossgrabers.framework.view.TransposeView;


/**
 * The Session view.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class SessionView extends AbstractSessionView<PushControlSurface, PushConfiguration> implements TransposeView
{
    private final BrowserActivator<PushControlSurface, PushConfiguration> browserModeActivator;


    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public SessionView (final PushControlSurface surface, final IModel model)
    {
        super ("Session", surface, model, 8, 8, true);

        this.browserModeActivator = new BrowserActivator<> (Modes.BROWSER, model, surface);

        final boolean isPush2 = this.surface.getConfiguration ().isPush2 ();
        final int redLo = isPush2 ? PushColors.PUSH2_COLOR2_RED_LO : PushColors.PUSH1_COLOR2_RED_LO;
        final int redHi = isPush2 ? PushColors.PUSH2_COLOR2_RED_HI : PushColors.PUSH1_COLOR2_RED_HI;
        final int black = isPush2 ? PushColors.PUSH2_COLOR2_BLACK : PushColors.PUSH1_COLOR2_BLACK;
        final int green = isPush2 ? PushColors.PUSH2_COLOR2_GREEN : PushColors.PUSH1_COLOR2_GREEN;
        final int amber = isPush2 ? PushColors.PUSH2_COLOR2_AMBER : PushColors.PUSH1_COLOR2_AMBER;
        final SessionColor isRecording = new SessionColor (redHi, redHi, false);
        final SessionColor isRecordingQueued = new SessionColor (redHi, black, true);
        final SessionColor isPlaying = new SessionColor (green, green, false);
        final SessionColor isPlayingQueued = new SessionColor (green, green, true);
        final SessionColor hasContent = new SessionColor (amber, -1, false);
        final SessionColor noContent = new SessionColor (black, -1, false);
        final SessionColor recArmed = new SessionColor (redLo, -1, false);
        this.setColors (isRecording, isRecordingQueued, isPlaying, isPlayingQueued, hasContent, noContent, recArmed);

        this.birdColorHasContent = hasContent;
        this.birdColorSelected = isPlaying;
    }


    /** {@inheritDoc} */
    @Override
    public void onGridNote (final int note, final int velocity)
    {
        if (velocity == 0)
        {
            final TriggerCommand triggerCommand = this.surface.getButton (ButtonID.SESSION).getCommand ();
            ((SelectSessionViewCommand) triggerCommand).setTemporary ();
            return;
        }

        final int index = note - 36;
        final int x = index % this.columns;
        final int y = this.rows - 1 - index / this.columns;

        final boolean flipSession = this.surface.getConfiguration ().isFlipSession ();

        final int t = flipSession ? y : x;
        final int s = flipSession ? x : y;

        // Birds-eye-view navigation
        if (this.surface.isShiftPressed ())
        {
            this.onGridNoteBirdsEyeView (x, y, 0);
            return;
        }

        // Duplicate a clip
        final ITrackBank tb = this.model.getCurrentTrackBank ();
        final ITrack track = tb.getItem (t);
        if (this.surface.isPressed (PushControlSurface.PUSH_BUTTON_DUPLICATE))
        {
            this.surface.setTriggerConsumed (PushControlSurface.PUSH_BUTTON_DUPLICATE);
            if (track.doesExist ())
                track.getSlotBank ().getItem (s).duplicate ();
            return;
        }

        // Stop clip
        if (this.surface.isPressed (PushControlSurface.PUSH_BUTTON_STOP_CLIP))
        {
            this.surface.setTriggerConsumed (PushControlSurface.PUSH_BUTTON_STOP_CLIP);
            track.stop ();
            return;
        }

        // Browse for clips
        if (this.surface.isPressed (PushControlSurface.PUSH_BUTTON_BROWSE))
        {
            this.surface.setTriggerConsumed (PushControlSurface.PUSH_BUTTON_BROWSE);
            if (!track.doesExist ())
                return;
            this.model.getBrowser ().replace (track.getSlotBank ().getItem (s));
            final ModeManager modeManager = this.surface.getModeManager ();
            if (!modeManager.isActiveOrTempMode (Modes.BROWSER))
                this.browserModeActivator.activate ();
            return;
        }

        super.onGridNote (note, velocity);
    }


    /** {@inheritDoc} */
    @Override
    public void updateSceneButton (final int scene)
    {
        // TODO REmove
    }


    /** {@inheritDoc} */
    @Override
    public String getSceneButtonColor (final int scene)
    {
        final ISceneBank sceneBank = this.model.getSceneBank ();
        final IScene s = sceneBank.getItem (7 - scene);
        if (s.doesExist ())
            return s.isSelected () ? AbstractSessionView.COLOR_SELECTED_SCENE : AbstractSessionView.COLOR_SCENE;
        return AbstractSessionView.COLOR_SCENE_OFF;
    }


    /** {@inheritDoc} */
    @Override
    public void onOctaveDown (final ButtonEvent event)
    {
        if (event == ButtonEvent.DOWN)
            this.model.getCurrentTrackBank ().getSceneBank ().selectNextPage ();
    }


    /** {@inheritDoc} */
    @Override
    public void onOctaveUp (final ButtonEvent event)
    {
        if (event == ButtonEvent.DOWN)
            this.model.getCurrentTrackBank ().getSceneBank ().selectPreviousPage ();
    }


    /** {@inheritDoc} */
    @Override
    public boolean isOctaveUpButtonOn ()
    {
        return this.model.getCurrentTrackBank ().getSceneBank ().canScrollPageForwards ();
    }


    /** {@inheritDoc} */
    @Override
    public boolean isOctaveDownButtonOn ()
    {
        return this.model.getCurrentTrackBank ().getSceneBank ().canScrollPageBackwards ();
    }
}