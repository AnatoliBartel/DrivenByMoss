// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.launchpad.view;

import de.mossgrabers.controller.launchpad.LaunchpadConfiguration;
import de.mossgrabers.controller.launchpad.controller.LaunchpadColors;
import de.mossgrabers.controller.launchpad.controller.LaunchpadControlSurface;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.configuration.AbstractConfiguration;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.grid.PadGrid;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.constants.Resolution;
import de.mossgrabers.framework.daw.midi.INoteRepeat;
import de.mossgrabers.framework.mode.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.AbstractView;
import de.mossgrabers.framework.view.SceneView;


/**
 * Simulates the missing buttons (in contrast to Launchpad Pro) on the grid.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class ShiftView extends AbstractView<LaunchpadControlSurface, LaunchpadConfiguration> implements SceneView
{
    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public ShiftView (final LaunchpadControlSurface surface, final IModel model)
    {
        super ("Shift", surface, model);
    }


    /** {@inheritDoc} */
    @Override
    public void onActivate ()
    {
        this.surface.setLaunchpadToPrgMode ();
        super.onActivate ();
    }


    /** {@inheritDoc} */
    @Override
    public void drawGrid ()
    {
        final PadGrid padGrid = this.surface.getPadGrid ();

        padGrid.light (97, LaunchpadColors.LAUNCHPAD_COLOR_GREEN);
        padGrid.light (98, LaunchpadColors.LAUNCHPAD_COLOR_GREEN_SPRING);
        padGrid.light (99, LaunchpadColors.LAUNCHPAD_COLOR_TURQUOISE_CYAN);

        final int clipLengthIndex = this.surface.getConfiguration ().getNewClipLength ();
        for (int i = 0; i < 8; i++)
            padGrid.light (36 + i, i == clipLengthIndex ? LaunchpadColors.LAUNCHPAD_COLOR_WHITE : LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO);

        // Note Repeat
        final INoteRepeat noteRepeat = this.surface.getInput ().getDefaultNoteInput ().getNoteRepeat ();
        padGrid.light (87, noteRepeat.isActive () ? LaunchpadColors.LAUNCHPAD_COLOR_ORCHID_HI : LaunchpadColors.LAUNCHPAD_COLOR_ORCHID_LO);

        // Note Repeat period
        final int periodIndex = Resolution.getMatch (noteRepeat.getPeriod ());
        padGrid.light (79, periodIndex == 0 ? LaunchpadColors.LAUNCHPAD_COLOR_SKY_HI : LaunchpadColors.LAUNCHPAD_COLOR_SKY_LO);
        padGrid.light (71, periodIndex == 2 ? LaunchpadColors.LAUNCHPAD_COLOR_SKY_HI : LaunchpadColors.LAUNCHPAD_COLOR_SKY_LO);
        padGrid.light (63, periodIndex == 4 ? LaunchpadColors.LAUNCHPAD_COLOR_SKY_HI : LaunchpadColors.LAUNCHPAD_COLOR_SKY_LO);
        padGrid.light (55, periodIndex == 6 ? LaunchpadColors.LAUNCHPAD_COLOR_SKY_HI : LaunchpadColors.LAUNCHPAD_COLOR_SKY_LO);

        padGrid.light (80, periodIndex == 1 ? LaunchpadColors.LAUNCHPAD_COLOR_PINK_HI : LaunchpadColors.LAUNCHPAD_COLOR_PINK_LO);
        padGrid.light (72, periodIndex == 3 ? LaunchpadColors.LAUNCHPAD_COLOR_PINK_HI : LaunchpadColors.LAUNCHPAD_COLOR_PINK_LO);
        padGrid.light (64, periodIndex == 5 ? LaunchpadColors.LAUNCHPAD_COLOR_PINK_HI : LaunchpadColors.LAUNCHPAD_COLOR_PINK_LO);
        padGrid.light (56, periodIndex == 7 ? LaunchpadColors.LAUNCHPAD_COLOR_PINK_HI : LaunchpadColors.LAUNCHPAD_COLOR_PINK_LO);

        // Note Repeat length
        final int lengthIndex = Resolution.getMatch (noteRepeat.getNoteLength ());
        padGrid.light (81, lengthIndex == 0 ? LaunchpadColors.LAUNCHPAD_COLOR_SKY_HI : LaunchpadColors.LAUNCHPAD_COLOR_SKY_LO);
        padGrid.light (73, lengthIndex == 2 ? LaunchpadColors.LAUNCHPAD_COLOR_SKY_HI : LaunchpadColors.LAUNCHPAD_COLOR_SKY_LO);
        padGrid.light (65, lengthIndex == 4 ? LaunchpadColors.LAUNCHPAD_COLOR_SKY_HI : LaunchpadColors.LAUNCHPAD_COLOR_SKY_LO);
        padGrid.light (57, lengthIndex == 6 ? LaunchpadColors.LAUNCHPAD_COLOR_SKY_HI : LaunchpadColors.LAUNCHPAD_COLOR_SKY_LO);

        padGrid.light (82, lengthIndex == 1 ? LaunchpadColors.LAUNCHPAD_COLOR_PINK_HI : LaunchpadColors.LAUNCHPAD_COLOR_PINK_LO);
        padGrid.light (74, lengthIndex == 3 ? LaunchpadColors.LAUNCHPAD_COLOR_PINK_HI : LaunchpadColors.LAUNCHPAD_COLOR_PINK_LO);
        padGrid.light (66, lengthIndex == 5 ? LaunchpadColors.LAUNCHPAD_COLOR_PINK_HI : LaunchpadColors.LAUNCHPAD_COLOR_PINK_LO);
        padGrid.light (58, lengthIndex == 7 ? LaunchpadColors.LAUNCHPAD_COLOR_PINK_HI : LaunchpadColors.LAUNCHPAD_COLOR_PINK_LO);

        if (this.surface.isPro ())
        {
            for (int i = 44; i < 55; i++)
                padGrid.light (i, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
            for (int i = 59; i < 63; i++)
                padGrid.light (i, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
            for (int i = 67; i < 71; i++)
                padGrid.light (i, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
            for (int i = 75; i < 79; i++)
                padGrid.light (i, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
            for (int i = 83; i < 87; i++)
                padGrid.light (i, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
            for (int i = 88; i < 97; i++)
                padGrid.light (i, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
            return;
        }

        padGrid.light (44, LaunchpadColors.LAUNCHPAD_COLOR_RED);
        padGrid.light (45, LaunchpadColors.LAUNCHPAD_COLOR_ROSE);

        for (int i = 46; i < 51; i++)
            padGrid.light (i, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);

        padGrid.light (51, LaunchpadColors.LAUNCHPAD_COLOR_RED);
        padGrid.light (52, LaunchpadColors.LAUNCHPAD_COLOR_GREEN);
        padGrid.light (53, LaunchpadColors.LAUNCHPAD_COLOR_GREEN_SPRING);

        padGrid.light (54, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
        padGrid.light (59, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);

        padGrid.light (60, LaunchpadColors.LAUNCHPAD_COLOR_GREEN);
        padGrid.light (61, LaunchpadColors.LAUNCHPAD_COLOR_GREEN_SPRING);

        padGrid.light (62, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
        padGrid.light (67, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);

        padGrid.light (68, LaunchpadColors.LAUNCHPAD_COLOR_GREEN);
        padGrid.light (69, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);

        padGrid.light (70, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
        padGrid.light (75, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);

        padGrid.light (76, LaunchpadColors.LAUNCHPAD_COLOR_GREEN);

        padGrid.light (78, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
        padGrid.light (83, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);

        padGrid.light (84, LaunchpadColors.LAUNCHPAD_COLOR_GREEN);
        padGrid.light (85, LaunchpadColors.LAUNCHPAD_COLOR_GREEN_SPRING);

        padGrid.light (86, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);

        for (int i = 88; i < 92; i++)
            padGrid.light (i, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);

        padGrid.light (92, LaunchpadColors.LAUNCHPAD_COLOR_GREEN);
        padGrid.light (93, LaunchpadColors.LAUNCHPAD_COLOR_GREEN_SPRING);

        for (int i = 94; i < 97; i++)
            padGrid.light (i, LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
    }


    /** {@inheritDoc} */
    @Override
    public void onGridNote (final int note, final int velocity)
    {
        if (velocity == 0)
            return;

        final INoteRepeat noteRepeat = this.surface.getInput ().getDefaultNoteInput ().getNoteRepeat ();

        switch (note)
        {
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
                final int newClipLength = note - 36;
                this.surface.getConfiguration ().setNewClipLength (newClipLength);
                this.surface.getDisplay ().notify ("New clip length: " + AbstractConfiguration.getNewClipLengthValue (newClipLength));
                break;

            case 87:
                noteRepeat.toggleActive ();
                break;

            case 79:
                noteRepeat.setPeriod (Resolution.getValueAt (0));
                break;
            case 80:
                noteRepeat.setPeriod (Resolution.getValueAt (1));
                break;
            case 71:
                noteRepeat.setPeriod (Resolution.getValueAt (2));
                break;
            case 72:
                noteRepeat.setPeriod (Resolution.getValueAt (3));
                break;
            case 63:
                noteRepeat.setPeriod (Resolution.getValueAt (4));
                break;
            case 64:
                noteRepeat.setPeriod (Resolution.getValueAt (5));
                break;
            case 55:
                noteRepeat.setPeriod (Resolution.getValueAt (6));
                break;
            case 56:
                noteRepeat.setPeriod (Resolution.getValueAt (7));
                break;

            case 81:
                noteRepeat.setNoteLength (Resolution.getValueAt (0));
                break;
            case 82:
                noteRepeat.setNoteLength (Resolution.getValueAt (1));
                break;
            case 73:
                noteRepeat.setNoteLength (Resolution.getValueAt (2));
                break;
            case 74:
                noteRepeat.setNoteLength (Resolution.getValueAt (3));
                break;
            case 65:
                noteRepeat.setNoteLength (Resolution.getValueAt (4));
                break;
            case 66:
                noteRepeat.setNoteLength (Resolution.getValueAt (5));
                break;
            case 57:
                noteRepeat.setNoteLength (Resolution.getValueAt (6));
                break;
            case 58:
                noteRepeat.setNoteLength (Resolution.getValueAt (7));
                break;

            case 97:
                this.model.getApplication ().addInstrumentTrack ();
                return;
            case 98:
                this.model.getApplication ().addAudioTrack ();
                return;
            case 99:
                this.model.getApplication ().addEffectTrack ();
                return;
            default:
                // Not used
                break;
        }

        if (this.surface.isPro ())
            return;

        switch (note)
        {
            case 92:
                this.executeNormal (ButtonID.METRONOME, ButtonEvent.DOWN);
                break;
            case 93:
                this.executeShifted (ButtonID.METRONOME, ButtonEvent.DOWN);
                break;
            case 84:
                this.executeNormal (ButtonID.UNDO, ButtonEvent.DOWN);
                break;
            case 85:
                this.executeShifted (ButtonID.UNDO, ButtonEvent.DOWN);
                break;
            case 76:
                this.executeNormal (ButtonID.DELETE, ButtonEvent.UP);
                break;
            case 68:
                this.executeNormal (ButtonID.QUANTIZE, ButtonEvent.DOWN);
                break;
            case 60:
                this.executeNormal (ButtonID.DUPLICATE, ButtonEvent.DOWN);
                break;
            case 61:
                this.executeShifted (ButtonID.DUPLICATE, ButtonEvent.DOWN);
                break;
            case 52:
                this.executeNormal (ButtonID.PLAY, ButtonEvent.DOWN);
                break;
            case 53:
                this.executeNormal (ButtonID.NEW, ButtonEvent.DOWN);
                break;
            case 44:
                this.executeNormal (ButtonID.RECORD, ButtonEvent.UP);
                break;
            case 45:
                this.executeShifted (ButtonID.RECORD, ButtonEvent.UP);
                break;
            case 51:
                this.model.getCurrentTrackBank ().stop ();
                break;
            default:
                // Not used
                break;
        }
    }


    /** {@inheritDoc} */
    @Override
    public int getButtonColor (final ButtonID buttonID)
    {
        final boolean isPro = this.surface.isPro ();
        switch (buttonID)
        {
            case SCENE1:
                return isPro ? LaunchpadColors.LAUNCHPAD_COLOR_BLACK : LaunchpadColors.LAUNCHPAD_COLOR_CYAN;
            case SCENE2:
                return isPro ? LaunchpadColors.LAUNCHPAD_COLOR_BLACK : LaunchpadColors.LAUNCHPAD_COLOR_SKY;
            case SCENE3:
                return isPro ? LaunchpadColors.LAUNCHPAD_COLOR_BLACK : LaunchpadColors.LAUNCHPAD_COLOR_ORCHID;
            case SCENE4:
                return isPro ? LaunchpadColors.LAUNCHPAD_COLOR_BLACK : LaunchpadColors.LAUNCHPAD_COLOR_GREEN;
            case SCENE5:
                return isPro ? LaunchpadColors.LAUNCHPAD_COLOR_BLACK : LaunchpadColors.LAUNCHPAD_COLOR_ROSE;
            case SCENE6:
                return isPro ? LaunchpadColors.LAUNCHPAD_COLOR_BLACK : LaunchpadColors.LAUNCHPAD_COLOR_YELLOW;
            case SCENE7:
                return isPro ? LaunchpadColors.LAUNCHPAD_COLOR_BLACK : LaunchpadColors.LAUNCHPAD_COLOR_BLUE;
            case SCENE8:
                return isPro ? LaunchpadColors.LAUNCHPAD_COLOR_BLACK : LaunchpadColors.LAUNCHPAD_COLOR_RED;
            default:
                return LaunchpadColors.LAUNCHPAD_COLOR_BLACK;
        }
    }


    private boolean handleControlModes (final ButtonID commandID)
    {
        this.surface.getButton (commandID).getCommand ().execute (ButtonEvent.DOWN);
        final ModeManager modeManager = this.surface.getModeManager ();
        final Modes activeOrTempModeId = modeManager.getActiveOrTempModeId ();
        if (activeOrTempModeId != null && activeOrTempModeId.equals (modeManager.getPreviousModeId ()))
            modeManager.setActiveMode (null);
        return true;
    }


    /** {@inheritDoc} */
    @Override
    public void onScene (final int scene, final ButtonEvent event)
    {
        if (this.surface.isPro () || event != ButtonEvent.DOWN)
            return;

        switch (scene)
        {
            case 0:
                this.handleControlModes (ButtonID.VOLUME);
                break;
            case 1:
                this.handleControlModes (ButtonID.PAN_SEND);
                break;
            case 2:
                this.handleControlModes (ButtonID.SENDS);
                break;
            case 3:
                this.handleControlModes (ButtonID.TRACK);
                break;
            case 4:
                this.handleControlModes (ButtonID.STOP_CLIP);
                break;
            case 5:
                this.handleControlModes (ButtonID.MUTE);
                break;
            case 6:
                this.handleControlModes (ButtonID.SOLO);
                break;
            case 7:
                this.handleControlModes (ButtonID.REC_ARM);
                break;
            default:
                // Not used
                break;
        }
    }


    @SuppressWarnings("rawtypes")
    private void executeNormal (final ButtonID buttonID, final ButtonEvent event)
    {
        ((AbstractTriggerCommand) this.surface.getButton (buttonID)).executeNormal (event);
    }


    @SuppressWarnings("rawtypes")
    private void executeShifted (final ButtonID buttonID, final ButtonEvent event)
    {
        ((AbstractTriggerCommand) this.surface.getButton (buttonID)).executeShifted (event);
    }
}