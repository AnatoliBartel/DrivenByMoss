// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.launchpad.view;

import de.mossgrabers.controller.launchpad.LaunchpadConfiguration;
import de.mossgrabers.controller.launchpad.controller.LaunchpadColors;
import de.mossgrabers.controller.launchpad.controller.LaunchpadControlSurface;
import de.mossgrabers.controller.launchpad.definition.LaunchpadProControllerDefinition;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.grid.PadGrid;
import de.mossgrabers.framework.daw.DAWColors;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.ISceneBank;
import de.mossgrabers.framework.daw.ITrackBank;
import de.mossgrabers.framework.daw.data.IScene;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.mode.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.AbstractSessionView;
import de.mossgrabers.framework.view.SessionColor;
import de.mossgrabers.framework.view.ViewManager;
import de.mossgrabers.framework.view.Views;


/**
 * Session view.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class SessionView extends AbstractSessionView<LaunchpadControlSurface, LaunchpadConfiguration>
{
    protected boolean isTemporary;
    private boolean   isBirdsEyeViewActive = false;


    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public SessionView (final LaunchpadControlSurface surface, final IModel model)
    {
        super ("Session", surface, model, 8, 8, true);

        final SessionColor isRecording = new SessionColor (LaunchpadColors.LAUNCHPAD_COLOR_RED_HI, LaunchpadColors.LAUNCHPAD_COLOR_RED_HI, false);
        final SessionColor isRecordingQueued = new SessionColor (LaunchpadColors.LAUNCHPAD_COLOR_RED_HI, LaunchpadColors.LAUNCHPAD_COLOR_BLACK, true);
        final SessionColor isPlaying = new SessionColor (LaunchpadColors.LAUNCHPAD_COLOR_GREEN, LaunchpadColors.LAUNCHPAD_COLOR_GREEN, false);
        final SessionColor isPlayingQueued = new SessionColor (LaunchpadColors.LAUNCHPAD_COLOR_GREEN, LaunchpadColors.LAUNCHPAD_COLOR_GREEN, true);
        final SessionColor hasContent = new SessionColor (LaunchpadColors.LAUNCHPAD_COLOR_AMBER, -1, false);
        final SessionColor noContent = new SessionColor (LaunchpadColors.LAUNCHPAD_COLOR_BLACK, -1, false);
        final SessionColor recArmed = new SessionColor (LaunchpadColors.LAUNCHPAD_COLOR_RED_LO, -1, false);
        this.setColors (isRecording, isRecordingQueued, isPlaying, isPlayingQueued, hasContent, noContent, recArmed);

        this.birdColorHasContent = hasContent;
        this.birdColorSelected = new SessionColor (LaunchpadColors.LAUNCHPAD_COLOR_GREEN, -1, false);
    }


    /** {@inheritDoc} */
    @Override
    public void onActivate ()
    {
        this.switchLaunchpadMode ();

        super.onActivate ();

        this.surface.scheduleTask (this::delayedUpdateArrowButtons, 150);
    }


    protected void delayedUpdateArrowButtons ()
    {
        this.surface.setTrigger (this.surface.getTriggerId (ButtonID.SESSION), LaunchpadColors.LAUNCHPAD_COLOR_LIME);
        this.surface.setTrigger (this.surface.getTriggerId (ButtonID.NOTE), LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO);
        this.surface.setTrigger (this.surface.getTriggerId (ButtonID.DEVICE), LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO);
        if (this.surface.isPro ())
            this.surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_USER, this.model.getHost ().hasUserParameters () ? LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO : LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
    }


    /** {@inheritDoc} */
    @Override
    public void onGridNote (final int note, final int velocity)
    {
        final ModeManager modeManager = this.surface.getModeManager ();
        final Modes activeModeId = modeManager.getActiveOrTempModeId ();
        // Block 1st row if mode is active
        final boolean isNotRow1 = note >= 44;
        if (activeModeId == null || isNotRow1)
        {
            if (this.isBirdsEyeActive ())
            {
                this.onGridNoteBankSelection (note, velocity, isNotRow1);
                return;
            }

            final int n = note - (activeModeId != null ? 8 : 0);
            final int index = n - 36;
            final int t = index % this.columns;

            // Duplicate a clip
            final int duplicateTriggerId = this.surface.getTriggerId (ButtonID.DUPLICATE);
            if (this.surface.isPressed (duplicateTriggerId))
            {
                this.surface.setTriggerConsumed (duplicateTriggerId);
                final ITrackBank tb = this.model.getCurrentTrackBank ();
                final ITrack track = tb.getItem (t);
                if (track.doesExist ())
                {
                    final int s = this.rows - 1 - index / this.columns;
                    track.getSlotBank ().getItem (s).duplicate ();
                }
                return;
            }

            super.onGridNote (n, velocity);
            return;
        }

        if (velocity != 0)
            this.handleFirstRowModes (note, modeManager);
    }


    /** {@inheritDoc} */
    @Override
    public void drawGrid ()
    {
        final Modes controlMode = this.surface.getModeManager ().getActiveOrTempModeId ();
        final boolean controlModeIsOff = controlMode == null;
        this.rows = controlModeIsOff ? 8 : 7;
        this.columns = 8;

        super.drawGrid ();

        if (controlModeIsOff)
            return;

        final ITrackBank tb = this.model.getCurrentTrackBank ();
        final PadGrid pads = this.surface.getPadGrid ();
        final ModeManager modeManager = this.surface.getModeManager ();
        for (int x = 0; x < this.columns; x++)
        {
            final ITrack track = tb.getItem (x);
            final boolean exists = track.doesExist ();
            if (modeManager.isActiveOrTempMode (Modes.REC_ARM))
                pads.lightEx (x, 7, exists ? track.isRecArm () ? LaunchpadColors.LAUNCHPAD_COLOR_RED_HI : LaunchpadColors.LAUNCHPAD_COLOR_RED_LO : LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
            else if (modeManager.isActiveOrTempMode (Modes.TRACK_SELECT))
                pads.lightEx (x, 7, exists ? track.isSelected () ? LaunchpadColors.LAUNCHPAD_COLOR_GREEN_HI : LaunchpadColors.LAUNCHPAD_COLOR_GREEN_LO : LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
            else if (modeManager.isActiveOrTempMode (Modes.MUTE))
                pads.lightEx (x, 7, exists ? track.isMute () ? LaunchpadColors.LAUNCHPAD_COLOR_YELLOW_HI : LaunchpadColors.LAUNCHPAD_COLOR_YELLOW_LO : LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
            else if (modeManager.isActiveOrTempMode (Modes.SOLO))
                pads.lightEx (x, 7, exists ? track.isSolo () ? LaunchpadColors.LAUNCHPAD_COLOR_BLUE_HI : LaunchpadColors.LAUNCHPAD_COLOR_BLUE_LO : LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
            else if (modeManager.isActiveOrTempMode (Modes.STOP_CLIP))
                pads.lightEx (x, 7, exists ? LaunchpadColors.LAUNCHPAD_COLOR_ROSE : LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
        }
    }


    /**
     * Switch to the appropriate launchpad mode, which supports different features with the pad
     * grid, e.g. the simulation of faders.
     */
    public void switchLaunchpadMode ()
    {
        this.surface.setLaunchpadToPrgMode ();
    }


    /**
     * Set the birds eye view in-/active.
     *
     * @param isBirdsEyeActive True to activate
     */
    public void setBirdsEyeActive (final boolean isBirdsEyeActive)
    {
        this.isBirdsEyeViewActive = isBirdsEyeActive;
    }


    /** {@inheritDoc} */
    @Override
    public void updateSceneButton (final int scene)
    {
        final ITrackBank tb = this.model.getCurrentTrackBank ();
        final ISceneBank sceneBank = tb.getSceneBank ();
        final IScene s = sceneBank.getItem (scene);

        if (s.doesExist ())
            this.surface.setTrigger (this.surface.getSceneTrigger (scene), DAWColors.getColorIndex (s.getColor ()));
        else
            this.surface.setTrigger (this.surface.getSceneTrigger (scene), LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
    }


    @Override
    public String getSceneButtonColor (final int scene)
    {
        // TODO Auto-generated method stub
        return ColorManager.BUTTON_STATE_OFF;
    }


    /**
     * The session button was pressed.
     *
     * @param event The button event
     */
    public void onSession (final ButtonEvent event)
    {
        switch (event)
        {
            case LONG:
                this.isTemporary = true;
                break;

            case UP:
                if (!this.isTemporary)
                    return;
                this.isTemporary = false;

                final ViewManager viewManager = this.surface.getViewManager ();
                final ITrack selectedTrack = this.model.getSelectedTrack ();
                if (selectedTrack == null)
                    return;
                final Views viewId = viewManager.getPreferredView (selectedTrack.getPosition ());
                viewManager.setActiveView (viewId == null ? Views.PLAY : viewId);
                break;

            default:
                // Intentionally empty
                return;
        }
    }


    /** {@inheritDoc} */
    @Override
    public boolean isBirdsEyeActive ()
    {
        return this.isBirdsEyeViewActive;
    }


    /**
     * Handle pad presses in the birds eye view.
     *
     * @param note The note of the pad
     * @param velocity The velocity of the press
     * @param isNotOffset Apply row 1 note offset if false
     */
    protected void onGridNoteBankSelection (final int note, final int velocity, final boolean isNotOffset)
    {
        if (velocity == 0)
            return;
        final int n = isNotOffset ? note : note - 8;
        final int index = n - 36;
        this.onGridNoteBirdsEyeView (index % this.columns, this.rows - 1 - index / this.columns, isNotOffset ? 0 : 1);
    }


    /**
     * Execute the functions of row 1 if active.
     *
     * @param note The pressed note on the first row
     * @param modeManager The mode manager
     */
    private void handleFirstRowModes (final int note, final ModeManager modeManager)
    {
        // First row mode handling
        final int index = note - 36;
        final ITrack track = this.model.getCurrentTrackBank ().getItem (index);

        final int duplicateTriggerId = this.surface.getTriggerId (ButtonID.DUPLICATE);
        if (this.surface.isPressed (duplicateTriggerId))
        {
            this.surface.setTriggerConsumed (duplicateTriggerId);
            track.duplicate ();
            return;
        }

        if (modeManager.isActiveOrTempMode (Modes.REC_ARM))
            track.toggleRecArm ();
        else if (modeManager.isActiveOrTempMode (Modes.TRACK_SELECT))
            this.selectTrack (index);
        else if (modeManager.isActiveOrTempMode (Modes.MUTE))
            track.toggleMute ();
        else if (modeManager.isActiveOrTempMode (Modes.SOLO))
            track.toggleSolo ();
        else if (modeManager.isActiveOrTempMode (Modes.STOP_CLIP))
            track.stop ();
    }
}
