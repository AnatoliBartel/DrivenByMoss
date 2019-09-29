// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.push.mode;

import de.mossgrabers.controller.push.controller.Push1Display;
import de.mossgrabers.controller.push.controller.PushControlSurface;
<<<<<<< HEAD
import de.mossgrabers.framework.controller.IValueChanger;
import de.mossgrabers.framework.controller.display.Format;
import de.mossgrabers.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.constants.EditCapability;
import de.mossgrabers.framework.daw.constants.Resolution;
import de.mossgrabers.framework.daw.data.IParameter;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.midi.INoteInput;
import de.mossgrabers.framework.daw.midi.INoteRepeat;
=======
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.constants.Resolution;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.midi.INoteInput;
>>>>>>> remotes/origin/master
import de.mossgrabers.framework.mode.AbstractMode;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.utils.Pair;


/**
 * Editing the length of note repeat notes.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class NoteRepeatMode extends BaseMode
{
    private final IHost       host;
    private final INoteRepeat noteRepeat;


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model The model
     */
    public NoteRepeatMode (final PushControlSurface surface, final IModel model)
    {
        super ("Note Repeat", surface, model);

        this.isTemporary = true;
        this.host = this.model.getHost ();

        final INoteInput defaultNoteInput = surface.getInput ().getDefaultNoteInput ();
        this.noteRepeat = defaultNoteInput == null ? null : defaultNoteInput.getNoteRepeat ();
    }


    /** {@inheritDoc} */
    @Override
    public void onKnobValue (final int index, final int value)
    {
        final ITrack selectedTrack = this.model.getCurrentTrackBank ().getSelectedItem ();

        switch (index)
        {
            case 0:
            case 1:
                final int sel = Resolution.change (Resolution.getMatch (this.noteRepeat.getPeriod (selectedTrack)), this.model.getValueChanger ().calcKnobSpeed (value) > 0);
                this.noteRepeat.setPeriod (selectedTrack, Resolution.getValueAt (sel));
                break;

            case 2:
            case 3:
                if (this.host.canEdit (EditCapability.NOTE_REPEAT_LENGTH))
                {
                    final int sel2 = Resolution.change (Resolution.getMatch (this.noteRepeat.getNoteLength (selectedTrack)), this.model.getValueChanger ().calcKnobSpeed (value) > 0);
                    this.noteRepeat.setNoteLength (selectedTrack, Resolution.getValueAt (sel2));
                }
                break;

            case 6:
                if (this.host.canEdit (EditCapability.NOTE_REPEAT_SWING))
                    this.model.getGroove ().getParameters ()[1].changeValue (value);
                break;

            case 7:
                if (this.host.canEdit (EditCapability.NOTE_REPEAT_VELOCITY_RAMP))
                    this.changeVelocityRamp (selectedTrack, value);
                break;

            default:
                // Not used
                break;
        }
    }


    /** {@inheritDoc} */
    @Override
    public void onKnobTouch (final int index, final boolean isTouched)
    {
        this.isKnobTouched[index] = isTouched;

        if (isTouched && this.surface.isDeletePressed ())
        {
            this.surface.setTriggerConsumed (this.surface.getDeleteTriggerId ());

            if (index == 6 && this.host.canEdit (EditCapability.NOTE_REPEAT_SWING))
                this.model.getGroove ().getParameters ()[1].resetValue ();
            else if (index == 7 && this.host.canEdit (EditCapability.NOTE_REPEAT_VELOCITY_RAMP))
                this.noteRepeat.setVelocityRamp (this.model.getCurrentTrackBank ().getSelectedItem (), 0);
        }
    }


    /** {@inheritDoc} */
    @Override
    public void onFirstRow (final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.UP || this.noteRepeat == null)
            return;

        final ITrack selectedTrack = this.model.getCurrentTrackBank ().getSelectedItem ();
        switch (index)
        {
            case 0:
            case 1:
                final int sel = Resolution.change (Resolution.getMatch (this.noteRepeat.getPeriod (selectedTrack)), index == 1);
                this.noteRepeat.setPeriod (selectedTrack, Resolution.getValueAt (sel));
                break;

            case 2:
            case 3:
                if (this.host.canEdit (EditCapability.NOTE_REPEAT_LENGTH))
                {
                    final int sel2 = Resolution.change (Resolution.getMatch (this.noteRepeat.getNoteLength (selectedTrack)), index == 3);
                    this.noteRepeat.setNoteLength (selectedTrack, Resolution.getValueAt (sel2));
                }
                break;

            case 6:
                if (this.host.canEdit (EditCapability.NOTE_REPEAT_SWING))
                    this.noteRepeat.toggleShuffle (selectedTrack);
                break;

            case 7:
                if (this.host.canEdit (EditCapability.NOTE_REPEAT_VELOCITY_RAMP))
                    this.noteRepeat.toggleUsePressure (selectedTrack);
                break;

            default:
                // Unused
                break;
        }
    }


    /** {@inheritDoc} */
    @Override
    public void updateFirstRow ()
    {
        this.surface.updateTrigger (20, AbstractMode.BUTTON_COLOR_ON);
        this.surface.updateTrigger (21, AbstractMode.BUTTON_COLOR_ON);
        this.surface.updateTrigger (22, this.host.canEdit (EditCapability.NOTE_REPEAT_LENGTH) ? AbstractMode.BUTTON_COLOR_ON : AbstractMode.BUTTON_COLOR_OFF);
        this.surface.updateTrigger (23, this.host.canEdit (EditCapability.NOTE_REPEAT_LENGTH) ? AbstractMode.BUTTON_COLOR_ON : AbstractMode.BUTTON_COLOR_OFF);

        this.surface.updateTrigger (24, AbstractMode.BUTTON_COLOR_OFF);
        this.surface.updateTrigger (25, AbstractMode.BUTTON_COLOR_OFF);
        final ITrack selectedTrack = this.model.getCurrentTrackBank ().getSelectedItem ();
        if (this.host.canEdit (EditCapability.NOTE_REPEAT_SWING))
            this.surface.updateTrigger (26, this.noteRepeat.isShuffle (selectedTrack) ? AbstractMode.BUTTON_COLOR_HI : AbstractMode.BUTTON_COLOR_ON);
        else
            this.surface.updateTrigger (26, AbstractMode.BUTTON_COLOR_OFF);
        if (this.host.canEdit (EditCapability.NOTE_REPEAT_VELOCITY_RAMP))
            this.surface.updateTrigger (27, this.noteRepeat.usePressure (selectedTrack) ? AbstractMode.BUTTON_COLOR_HI : AbstractMode.BUTTON_COLOR_ON);
        else
            this.surface.updateTrigger (27, AbstractMode.BUTTON_COLOR_OFF);
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay1 (final ITextDisplay display)
    {
        final ITrack selectedTrack = this.model.getCurrentTrackBank ().getSelectedItem ();

        display.setCell (0, 0, "Period:");
        final int selPeriodIndex = this.getSelectedPeriodIndex (selectedTrack);
        int pos = 0;
        for (final Pair<String, Boolean> p: Push1Display.createMenuList (4, Resolution.getNames (), selPeriodIndex))
        {
            display.setCell (pos, 1, (p.getValue ().booleanValue () ? Push1Display.SELECT_ARROW : " ") + p.getKey ());
            pos++;
        }

        if (this.host.canEdit (EditCapability.NOTE_REPEAT_LENGTH))
        {
            display.setCell (0, 2, "Length:");
            final int selLengthIndex = this.getSelectedNoteLengthIndex (selectedTrack);
            pos = 0;
            for (final Pair<String, Boolean> p: Push1Display.createMenuList (4, Resolution.getNames (), selLengthIndex))
            {
                display.setCell (pos, 3, (p.getValue ().booleanValue () ? Push1Display.SELECT_ARROW : " ") + p.getKey ());
                pos++;
            }
        }

        if (this.host.canEdit (EditCapability.NOTE_REPEAT_SWING))
        {
            final IParameter shuffleParam = this.model.getGroove ().getParameters ()[1];
            display.setCell (0, 6, shuffleParam.getName (10));
            display.setCell (1, 6, shuffleParam.getDisplayedValue (8));
            display.setCell (2, 6, shuffleParam.getValue (), Format.FORMAT_VALUE);
            display.setCell (3, 6, "Shuffle");
        }

        if (this.host.canEdit (EditCapability.NOTE_REPEAT_VELOCITY_RAMP))
        {
            display.setCell (0, 7, "Vel.Ramp");
            display.setCell (1, 7, this.noteRepeat.getVelocityRampStr (selectedTrack));
            display.setCell (2, 7, this.getRampDisplayValue (selectedTrack), Format.FORMAT_VALUE);
            display.setCell (3, 7, "Pressure");
        }
    }


    /** {@inheritDoc} */
    @Override
<<<<<<< HEAD
    public void updateDisplay2 (final IGraphicDisplay display)
    {
        if (this.noteRepeat == null)
            return;

        final ITrack selectedTrack = this.model.getCurrentTrackBank ().getSelectedItem ();

        display.addOptionElement ("Period", "", false, "", "", false, false);
        final int selPeriodIndex = this.getSelectedPeriodIndex (selectedTrack);
        display.addListElement (6, Resolution.getNames (), selPeriodIndex);

        if (this.host.canEdit (EditCapability.NOTE_REPEAT_LENGTH))
        {
            display.addOptionElement ("  Length", "", false, "", "", false, false);
            final int selLengthIndex = this.getSelectedNoteLengthIndex (selectedTrack);
            display.addListElement (6, Resolution.getNames (), selLengthIndex);
        }
        else
        {
            display.addEmptyElement ();
            display.addEmptyElement ();
        }

        display.addEmptyElement ();
        display.addEmptyElement ();

        if (this.host.canEdit (EditCapability.NOTE_REPEAT_SWING))
        {
            final IParameter shuffleParam = this.model.getGroove ().getParameters ()[1];
            display.addParameterElementWithPlainMenu ("", false, "Shuffle", null, this.noteRepeat.isShuffle (selectedTrack), shuffleParam.getName (10), shuffleParam.getValue (), shuffleParam.getDisplayedValue (8), this.isKnobTouched[6], -1);
        }
        else
            display.addEmptyElement ();

        if (this.host.canEdit (EditCapability.NOTE_REPEAT_VELOCITY_RAMP))
            display.addParameterElementWithPlainMenu ("", false, "Pressure", null, this.noteRepeat.usePressure (selectedTrack), "Vel. Ramp", this.getRampDisplayValue (selectedTrack), this.noteRepeat.getVelocityRampStr (selectedTrack), this.isKnobTouched[7], -1);
        else
            display.addEmptyElement ();
    }


    private int getRampDisplayValue (final ITrack selectedTrack)
    {
        final double ramp = this.noteRepeat.getVelocityRamp (selectedTrack);
        return this.model.getValueChanger ().fromNormalizedValue ((ramp + 1.0) / 2.0);
    }


    /**
     * Get the index of the selected period.
     *
     * @param selectedTrack The currently selected track
     * @return The selected period index
     */
    private int getSelectedPeriodIndex (final ITrack selectedTrack)
    {
        return this.noteRepeat == null ? -1 : Resolution.getMatch (this.noteRepeat.getPeriod (selectedTrack));
    }


    /**
     * Get the index of the selected length.
     *
     * @param selectedTrack The currently selected track
     * @return The selected lenth index
     */
    private int getSelectedNoteLengthIndex (final ITrack selectedTrack)
    {
        return this.noteRepeat == null ? -1 : Resolution.getMatch (this.noteRepeat.getNoteLength (selectedTrack));
    }


    private void changeVelocityRamp (final ITrack selectedTrack, final int control)
    {
        final IValueChanger valueChanger = this.model.getValueChanger ();
        final double inc = valueChanger.toNormalizedValue ((int) valueChanger.calcKnobSpeed (control));
        final double value = Math.max (-1.0, Math.min (1.0, this.noteRepeat.getVelocityRamp (selectedTrack) + inc));
        this.noteRepeat.setVelocityRamp (selectedTrack, value);
=======
    public void updateDisplay1 (final ITextDisplay display)
    {
        final ITextDisplay d = this.surface.getTextDisplay ().clear ();
        d.setBlock (2, 0, "Repeat Length:");
        final ITrack selectedTrack = this.model.getCurrentTrackBank ().getSelectedItem ();
        for (int i = 0; i < 8; i++)
            d.setCell (3, i, (this.isPeriodSelected (selectedTrack, i) ? Push1Display.SELECT_ARROW : "") + Resolution.getNameAt (i));
        d.allDone ();
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay2 (final IGraphicDisplay display)
    {
        final ITrack selectedTrack = this.model.getCurrentTrackBank ().getSelectedItem ();
        for (int i = 0; i < 8; i++)
            display.addOptionElement ("", "", false, i == 0 ? "Repeat Length" : "", Resolution.getNameAt (i), this.isPeriodSelected (selectedTrack, i), false);
>>>>>>> remotes/origin/master
    }
}
