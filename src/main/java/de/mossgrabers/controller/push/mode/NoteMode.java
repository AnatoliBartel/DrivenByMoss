// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.push.mode;

import de.mossgrabers.controller.push.controller.PushControlSurface;
import de.mossgrabers.framework.controller.IValueChanger;
<<<<<<< HEAD
import de.mossgrabers.framework.controller.display.Format;
import de.mossgrabers.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.INoteClip;
import de.mossgrabers.framework.daw.IStepInfo;
import de.mossgrabers.framework.daw.constants.EditCapability;
=======
import de.mossgrabers.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.INoteClip;
>>>>>>> remotes/origin/master
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.utils.StringUtils;


/**
 * Editing of note parameters.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class NoteMode extends BaseMode
{
    private final IHost host;

    private INoteClip   clip = null;
    private int         step = 0;
    private int         note = 60;


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model The model
     */
    public NoteMode (final PushControlSurface surface, final IModel model)
    {
        super ("Note", surface, model);

        this.host = this.model.getHost ();
    }


    /**
     * Set the values.
     *
     * @param clip The clip to edit
     * @param step The step to edit
     * @param note The note to edit
     */
    public void setValues (final INoteClip clip, final int step, final int note)
    {
        this.clip = clip;
        this.step = step;
        this.note = note;
    }


    /** {@inheritDoc} */
    @Override
    public void onKnobTouch (final int index, final boolean isTouched)
    {
        if (isTouched && this.surface.isDeletePressed ())
        {
            this.surface.setTriggerConsumed (this.surface.getDeleteTriggerId ());
            switch (index)
            {
                case 1:
                    this.clip.updateStepDuration (this.step, this.note, 1.0);
                    break;

                case 2:
                    this.clip.updateStepVelocity (this.step, this.note, 1.0);
                    break;

                case 3:
                    if (this.host.canEdit (EditCapability.NOTE_EDIT_RELEASE_VELOCITY))
                        this.clip.updateStepReleaseVelocity (this.step, this.note, 1.0);
                    break;

                case 4:
                    if (this.host.canEdit (EditCapability.NOTE_EDIT_PRESSURE))
                        this.clip.updateStepPressure (this.step, this.note, 0);
                    break;

                case 5:
                    if (this.host.canEdit (EditCapability.NOTE_EDIT_TIMBRE))
                        this.clip.updateStepTimbre (this.step, this.note, 0);
                    break;

                case 6:
                    if (this.host.canEdit (EditCapability.NOTE_EDIT_PANORAMA))
                        this.clip.updateStepPan (this.step, this.note, 0);
                    break;

                case 7:
                    if (this.host.canEdit (EditCapability.NOTE_EDIT_TRANSPOSE))
                        this.clip.updateStepTranspose (this.step, this.note, 0);
                    break;

                default:
                    return;
            }
            return;
        }

        this.clip.edit (this.step, this.note, isTouched);
    }


    /** {@inheritDoc} */
    @Override
    public void onKnobValue (final int index, final int value)
    {
        switch (index)
        {
            case 1:
                this.clip.changeStepDuration (this.step, this.note, value);
                break;

            case 2:
                this.clip.changeStepVelocity (this.step, this.note, value);
                break;

            case 3:
                if (this.host.canEdit (EditCapability.NOTE_EDIT_RELEASE_VELOCITY))
                    this.clip.changeStepReleaseVelocity (this.step, this.note, value);
                break;

            case 4:
                if (this.host.canEdit (EditCapability.NOTE_EDIT_PRESSURE))
                    this.clip.changeStepPressure (this.step, this.note, value);
                break;

            case 5:
                if (this.host.canEdit (EditCapability.NOTE_EDIT_TIMBRE))
                    this.clip.changeStepTimbre (this.step, this.note, value);
                break;

            case 6:
                if (this.host.canEdit (EditCapability.NOTE_EDIT_PANORAMA))
                    this.clip.changeStepPan (this.step, this.note, value);
                break;

            case 7:
                if (this.host.canEdit (EditCapability.NOTE_EDIT_TRANSPOSE))
                    this.clip.changeStepTranspose (this.step, this.note, value);
                break;

            default:
                return;
        }
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay1 (final ITextDisplay display)
    {
        final IStepInfo stepInfo = this.clip.getStep (this.step, this.note);
        final IValueChanger valueChanger = this.model.getValueChanger ();

        display.setCell (0, 1, "Length").setCell (1, 1, this.formatLength (stepInfo.getDuration ()));

        final double noteVelocity = stepInfo.getVelocity ();
        final int parameterValue = valueChanger.fromNormalizedValue (noteVelocity);
        display.setCell (0, 2, "Velocity").setCell (1, 2, formatPercentage (noteVelocity)).setCell (2, 2, parameterValue, Format.FORMAT_VALUE);

        if (this.host.canEdit (EditCapability.NOTE_EDIT_RELEASE_VELOCITY))
        {
            final double noteReleaseVelocity = stepInfo.getReleaseVelocity ();
            final int parameterReleaseValue = valueChanger.fromNormalizedValue (noteReleaseVelocity);
            display.setCell (0, 3, "R-Velocity").setCell (1, 3, formatPercentage (noteReleaseVelocity)).setCell (2, 3, parameterReleaseValue, Format.FORMAT_VALUE);
        }
        if (this.host.canEdit (EditCapability.NOTE_EDIT_PRESSURE))
        {
            final double notePressure = stepInfo.getPressure ();
            final int parameterPressureValue = valueChanger.fromNormalizedValue (notePressure);
            display.setCell (0, 4, "Pressure").setCell (1, 4, formatPercentage (notePressure)).setCell (2, 4, parameterPressureValue, Format.FORMAT_VALUE);
        }
        if (this.host.canEdit (EditCapability.NOTE_EDIT_TIMBRE))
        {
            final double noteTimbre = stepInfo.getTimbre ();
            final int parameterTimbreValue = valueChanger.fromNormalizedValue ((noteTimbre + 1.0) / 2.0);
            display.setCell (0, 5, "Timbre").setCell (1, 5, formatPercentage (noteTimbre)).setCell (2, 5, parameterTimbreValue, Format.FORMAT_VALUE);
        }
        if (this.host.canEdit (EditCapability.NOTE_EDIT_PANORAMA))
        {
            final double notePan = stepInfo.getPan ();
            final int parameterPanValue = valueChanger.fromNormalizedValue ((notePan + 1.0) / 2.0);
            display.setCell (0, 6, "Pan").setCell (1, 6, formatPercentage (notePan)).setCell (2, 6, parameterPanValue, Format.FORMAT_PAN);
        }
        if (this.host.canEdit (EditCapability.NOTE_EDIT_TRANSPOSE))
        {
            final double noteTranspose = stepInfo.getTranspose ();
            final int parameterTransposeValue = valueChanger.fromNormalizedValue ((noteTranspose + 24.0) / 48.0);
            display.setCell (0, 7, "Pitch").setCell (1, 7, String.format ("%.1f", Double.valueOf (noteTranspose))).setCell (2, 7, parameterTransposeValue, Format.FORMAT_PAN);
        }

        display.setCell (3, 0, "Step: " + (this.step + 1));
        display.setCell (3, 1, "Note: " + Scales.formatNoteAndOctave (this.note, -3));
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay2 (final IGraphicDisplay display)
    {
        final IStepInfo stepInfo = this.clip.getStep (this.step, this.note);
        final double noteVelocity = stepInfo.getVelocity ();

        final IValueChanger valueChanger = this.model.getValueChanger ();

        display.addOptionElement ("Step: " + (this.step + 1), "", false, "Note: " + Scales.formatNoteAndOctave (this.note, -3), "", false, false);

        display.addParameterElement ("Length", -1, this.formatLength (stepInfo.getDuration ()), this.isKnobTouched[1], -1);

        final int parameterValue = valueChanger.fromNormalizedValue (noteVelocity);
        display.addParameterElement ("Velocity", parameterValue, formatPercentage (noteVelocity), this.isKnobTouched[2], parameterValue);

        if (this.host.canEdit (EditCapability.NOTE_EDIT_RELEASE_VELOCITY))
        {
            final double noteReleaseVelocity = stepInfo.getReleaseVelocity ();
            final int parameterReleaseValue = valueChanger.fromNormalizedValue (noteReleaseVelocity);
            display.addParameterElement ("R-Velocity", parameterReleaseValue, formatPercentage (noteReleaseVelocity), this.isKnobTouched[3], parameterReleaseValue);
        }
        else
            display.addEmptyElement ();

        if (this.host.canEdit (EditCapability.NOTE_EDIT_PRESSURE))
        {
            final double notePressure = stepInfo.getPressure ();
            final int parameterPressureValue = valueChanger.fromNormalizedValue (notePressure);
            display.addParameterElement ("Pressure", parameterPressureValue, formatPercentage (notePressure), this.isKnobTouched[4], parameterPressureValue);
        }
        else
            display.addEmptyElement ();

        if (this.host.canEdit (EditCapability.NOTE_EDIT_TIMBRE))
        {
            final double noteTimbre = stepInfo.getTimbre ();
            final int parameterTimbreValue = valueChanger.fromNormalizedValue ((noteTimbre + 1.0) / 2.0);
            display.addParameterElement ("Timbre", parameterTimbreValue, formatPercentage (noteTimbre), this.isKnobTouched[5], parameterTimbreValue);
        }
        else
            display.addEmptyElement ();

        if (this.host.canEdit (EditCapability.NOTE_EDIT_PANORAMA))
        {
            final double notePan = stepInfo.getPan ();
            final int parameterPanValue = valueChanger.fromNormalizedValue ((notePan + 1.0) / 2.0);
            display.addParameterElement ("Pan", parameterPanValue, formatPercentage (notePan), this.isKnobTouched[6], parameterPanValue);
        }
        else
            display.addEmptyElement ();

        if (this.host.canEdit (EditCapability.NOTE_EDIT_TRANSPOSE))
        {
            final double noteTranspose = stepInfo.getTranspose ();
            final int parameterTransposeValue = valueChanger.fromNormalizedValue ((noteTranspose + 24.0) / 48.0);
            display.addParameterElement ("Pitch", parameterTransposeValue, String.format ("%.1f", Double.valueOf (noteTranspose)), this.isKnobTouched[7], parameterTransposeValue);
        }
        else
            display.addEmptyElement ();
    }


<<<<<<< HEAD
    /**
     * Format a velocity percentage.
     *
     * @param noteVelocity The velocity in the range of 0..1.
     * @return The formatted velocity
     */
    private static String formatPercentage (final double noteVelocity)
    {
        return String.format ("%.01f%%", Double.valueOf (noteVelocity * 100.0));
    }


    /**
     * Format the duration of the current note.
     *
     * @param duration The note duration
     * @return The formatted value
     */
    private String formatLength (final double duration)
    {
        return StringUtils.formatMeasuresLong (this.model.getTransport ().getQuartersPerMeasure (), duration, 0);
=======
    /** {@inheritDoc} */
    @Override
    public void updateDisplay1 (final ITextDisplay display)
    {
        final int quarters = (int) Math.floor (this.noteLength);
        final int fine = (int) Math.floor (this.noteLength * 100) % 100;
        display.setCell (0, 0, "Quarters").setCell (1, 0, Integer.toString (quarters));
        display.setCell (0, 1, "Fine").setCell (1, 1, Integer.toString (fine));
        display.setCell (0, 2, "Velocity").setCell (1, 2, Integer.toString (this.noteVelocity * 100 / 127) + "%");
        display.setBlock (3, 0, "Step: " + (this.step + 1));
        display.setBlock (3, 1, "Selec. Note: " + Scales.formatNoteAndOctave (this.note, -3));

    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay2 (final IGraphicDisplay display)
    {
        final int quarters = (int) Math.floor (this.noteLength);
        final int fine = (int) Math.floor (this.noteLength * 100) % 100;

        display.addParameterElement ("Quarters", quarters, Integer.toString (quarters), this.isKnobTouched[0], -1);
        display.addParameterElement ("Fine", fine, Integer.toString (fine), this.isKnobTouched[1], -1);
        final int parameterValue = this.noteVelocity * 1023 / 127;
        display.addParameterElement ("Velocity", parameterValue, Integer.toString (this.noteVelocity * 100 / 127) + "%", this.isKnobTouched[2], parameterValue);
        display.addOptionElement ("    Step: " + (this.step + 1), "", false, "    Selected note: " + Scales.formatNoteAndOctave (this.note, -3), "", false, false);
        for (int i = 4; i < 8; i++)
            display.addOptionElement ("", "", false, "", "", false, false);

>>>>>>> remotes/origin/master
    }
}