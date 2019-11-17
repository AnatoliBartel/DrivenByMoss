// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.apcmini.view;

import de.mossgrabers.controller.apcmini.APCminiConfiguration;
import de.mossgrabers.controller.apcmini.controller.APCminiColors;
import de.mossgrabers.controller.apcmini.controller.APCminiControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.ISlotBank;
import de.mossgrabers.framework.daw.ITrackBank;
import de.mossgrabers.framework.daw.data.ISlot;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.AbstractSessionView;


/**
 * Session view.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class SessionView extends AbstractSessionView<APCminiControlSurface, APCminiConfiguration> implements APCminiView
{
    private final TrackButtons extensions;


    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     * @param trackButtons The track button control
     */
    public SessionView (final APCminiControlSurface surface, final IModel model, final TrackButtons trackButtons)
    {
        super ("Session", surface, model, 8, 8, false);
        this.extensions = trackButtons;
    }


    /** {@inheritDoc} */
    @Override
    public boolean doSelectClipOnLaunch ()
    {
        return this.surface.getConfiguration ().isSelectClipOnLaunch ();
    }


    /** {@inheritDoc} */
    @Override
    public void drawGrid ()
    {
        final ITrackBank tb = this.model.getCurrentTrackBank ();
        for (int x = 0; x < 8; x++)
        {
            final ITrack t = tb.getItem (x);
            final ISlotBank slotBank = t.getSlotBank ();
            for (int y = 0; y < 8; y++)
                this.drawPad (slotBank.getItem (y), x, y, t.isRecArm ());
        }
    }


    /** {@inheritDoc} */
    @Override
    public void drawPad (final ISlot slot, final int x, final int y, final boolean isArmed)
    {
        int color = APCminiColors.APC_COLOR_BLACK;

        if (slot.isRecording ())
            color = APCminiColors.APC_COLOR_RED;
        else if (slot.isRecordingQueued ())
            color = APCminiColors.APC_COLOR_RED_BLINK;
        else if (slot.isPlaying ())
            color = APCminiColors.APC_COLOR_GREEN;
        else if (slot.isPlayingQueued ())
            color = APCminiColors.APC_COLOR_GREEN_BLINK;
        else if (slot.hasContent ())
            color = APCminiColors.APC_COLOR_YELLOW;

        this.surface.getPadGrid ().light (36 + (7 - y) * 8 + x, color);
    }


    /** {@inheritDoc} */
    @Override
    public String getButtonColorID (final ButtonID buttonID)
    {
        final int index = buttonID.ordinal () - ButtonID.SCENE1.ordinal ();
        return this.surface.getNoteVelocity (index) > 0 ? ColorManager.BUTTON_STATE_ON : ColorManager.BUTTON_STATE_OFF;
    }


    /** {@inheritDoc} */
    @Override
    public void onSelectTrack (final int index, final ButtonEvent event)
    {
        this.extensions.onSelectTrack (index, event);
    }


    /** {@inheritDoc} */
    @Override
    public int getTrackButtonColor (final int index)
    {
        return this.extensions.getTrackButtonColor (index);
    }
}