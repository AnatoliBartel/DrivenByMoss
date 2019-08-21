// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.generic.flexihandler;

import de.mossgrabers.controller.generic.GenericFlexiConfiguration;
import de.mossgrabers.controller.generic.controller.FlexiCommand;
import de.mossgrabers.controller.generic.controller.GenericFlexiControlSurface;
import de.mossgrabers.framework.command.trigger.clip.NewCommand;
import de.mossgrabers.framework.controller.IValueChanger;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.ISceneBank;
import de.mossgrabers.framework.daw.ISlotBank;
import de.mossgrabers.framework.daw.ITrackBank;
import de.mossgrabers.framework.daw.data.ISlot;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * The handler for clip commands.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class ClipHandler extends AbstractHandler
{
    private final NewCommand<GenericFlexiControlSurface, GenericFlexiConfiguration> newCommand;


    /**
     * Constructor.
     *
     * @param model The model
     * @param surface The surface
     * @param configuration The configuration
     * @param relative2ValueChanger The relative value changer variant 2
     * @param relative3ValueChanger The relative value changer variant 3
     */
    public ClipHandler (final IModel model, final GenericFlexiControlSurface surface, final GenericFlexiConfiguration configuration, final IValueChanger relative2ValueChanger, final IValueChanger relative3ValueChanger)
    {
        super (model, surface, configuration, relative2ValueChanger, relative3ValueChanger);

        this.newCommand = new NewCommand<> (this.model, this.surface);
    }


    /** {@inheritDoc} */
    @Override
    public FlexiCommand [] getSupportedCommands ()
    {
        return new FlexiCommand []
        {
            FlexiCommand.CLIP_PREVIOUS,
            FlexiCommand.CLIP_NEXT,
            FlexiCommand.CLIP_SCROLL,
            FlexiCommand.CLIP_PLAY,
            FlexiCommand.CLIP_STOP,
            FlexiCommand.CLIP_RECORD,
            FlexiCommand.CLIP_NEW
        };
    }


    /** {@inheritDoc} */
    @Override
    public int getCommandValue (final FlexiCommand command)
    {
        final ISlot selectedSlot = this.model.getSelectedSlot ();
        switch (command)
        {
            case CLIP_PLAY:
                return selectedSlot != null && selectedSlot.isPlaying () ? 127 : 0;

            case CLIP_STOP:
                return selectedSlot != null && selectedSlot.isPlaying () ? 0 : 127;

            case CLIP_RECORD:
                return selectedSlot != null && selectedSlot.isRecording () ? 127 : 0;

            default:
                return -1;
        }
    }


    /** {@inheritDoc} */
    @Override
    public void handle (final FlexiCommand command, final int knobMode, final int value)
    {
        final boolean isButtonPressed = this.isButtonPressed (knobMode, value);

        switch (command)
        {
            case CLIP_PREVIOUS:
                if (isButtonPressed)
                    this.scrollClipLeft (false);
                break;

            case CLIP_NEXT:
                if (isButtonPressed)
                    this.scrollClipRight (false);
                break;

            case CLIP_SCROLL:
                this.scrollClips (knobMode, value);
                break;

            case CLIP_PLAY:
                if (isButtonPressed)
                {
                    final ISlot selectedSlot = this.model.getSelectedSlot ();
                    if (selectedSlot != null)
                        selectedSlot.launch ();
                }
                break;

            case CLIP_STOP:
                if (isButtonPressed)
                {
                    final ITrack track = this.model.getSelectedTrack ();
                    if (track != null)
                        track.stop ();
                }
                break;

            case CLIP_RECORD:
                if (isButtonPressed)
                {
                    final ISlot selectedSlot = this.model.getSelectedSlot ();
                    if (selectedSlot != null)
                        selectedSlot.record ();
                }
                break;

            case CLIP_NEW:
                if (isButtonPressed)
                    this.newCommand.executeNormal (ButtonEvent.DOWN);
                break;

            default:
                throw new FlexiHandlerException (command);
        }
    }


    private void scrollClips (final int knobMode, final int value)
    {
        if (isAbsolute (knobMode))
            return;

        if (!this.increaseKnobMovement ())
            return;

        if (this.getRelativeSpeed (knobMode, value) > 0)
            this.scrollClipRight (false);
        else
            this.scrollClipLeft (false);
    }


    private void scrollClipLeft (final boolean switchBank)
    {
        final ITrackBank tb = this.model.getCurrentTrackBank ();
        final ITrack track = tb.getSelectedItem ();
        if (track == null)
            return;
        final ISlotBank slotBank = track.getSlotBank ();
        final ISlot sel = slotBank.getSelectedItem ();
        final int index = sel == null ? 0 : sel.getIndex () - 1;
        if (index == -1 || switchBank)
        {
            tb.getSceneBank ().selectPreviousPage ();
            return;
        }
        slotBank.getItem (index).select ();
    }


    private void scrollClipRight (final boolean switchBank)
    {
        final ITrackBank tb = this.model.getCurrentTrackBank ();
        final ITrack track = tb.getSelectedItem ();
        if (track == null)
            return;
        final ISlotBank slotBank = track.getSlotBank ();
        final ISlot sel = slotBank.getSelectedItem ();
        final int index = sel == null ? 0 : sel.getIndex () + 1;
        final ISceneBank sceneBank = tb.getSceneBank ();
        if (index == sceneBank.getPageSize () || switchBank)
        {
            sceneBank.selectNextPage ();
            return;
        }
        slotBank.getItem (index).select ();
    }
}
