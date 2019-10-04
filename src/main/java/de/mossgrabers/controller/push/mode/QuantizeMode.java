// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.push.mode;

import de.mossgrabers.controller.push.controller.Push1Display;
import de.mossgrabers.controller.push.controller.PushControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.display.Format;
import de.mossgrabers.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.constants.EditCapability;
import de.mossgrabers.framework.daw.constants.RecordQuantization;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.resource.ChannelType;
import de.mossgrabers.framework.mode.AbstractMode;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Mode for editing the recording options.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class QuantizeMode extends BaseMode
{
    private final static String [] MENU =
    {
        "Quantize",
        "Groove",
        " ",
        " ",
        " ",
        " ",
        " ",
        " "
    };


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model The model
     */
    public QuantizeMode (final PushControlSurface surface, final IModel model)
    {
        super ("Record", surface, model);
    }


    /** {@inheritDoc} */
    @Override
    public void onKnobValue (final int index, final int value)
    {
        if (index == 7)
            this.surface.getConfiguration ().changeQuantizeAmount (value);
    }


    /** {@inheritDoc} */
    @Override
    public void onKnobTouch (final int index, final boolean isTouched)
    {
        if (isTouched && this.surface.isDeletePressed ())
        {
            this.surface.setTriggerConsumed (this.surface.getTriggerId (ButtonID.DELETE));
            if (index == 7)
                this.surface.getConfiguration ().resetQuantizeAmount ();
        }
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay1 (final ITextDisplay display)
    {
        display.setCell (0, 0, Push1Display.SELECT_ARROW + "Quantize");
        display.setCell (0, 1, "Groove");

        final ITrack track = this.model.getSelectedTrack ();
        final RecordQuantization recQuant = track == null ? RecordQuantization.RES_OFF : track.getRecordQuantizationGrid ();
        final RecordQuantization [] values = RecordQuantization.values ();
        display.setBlock (2, 0, "Record Quantize:");
        for (int i = 0; i < values.length; i++)
            display.setCell (3, i, (values[i] == recQuant ? Push1Display.SELECT_ARROW : "") + values[i].getName ());

        if (this.model.getHost ().canEdit (EditCapability.QUANTIZE_INPUT_NOTE_LENGTH))
        {
            display.setBlock (2, 2, "       Quant Note");
            display.setCell (2, 6, "Length:");
            display.setCell (3, 6, track != null && track.isRecordQuantizationNoteLength () ? "On" : "Off");
        }

        final int quantizeAmount = this.surface.getConfiguration ().getQuantizeAmount ();
        display.setCell (0, 7, "Quant Amnt").setCell (1, 7, quantizeAmount + "%").setCell (2, 7, quantizeAmount * 1023 / 100, Format.FORMAT_VALUE);
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay2 (final IGraphicDisplay display)
    {
        final ITrack track = this.model.getSelectedTrack ();
        final RecordQuantization recQuant = track == null ? RecordQuantization.RES_OFF : track.getRecordQuantizationGrid ();
        final RecordQuantization [] values = RecordQuantization.values ();
        for (int i = 0; i < values.length; i++)
            display.addOptionElement ("", MENU[i], i == 0, i == 0 ? "Record Quantization" : "", values[i].getName (), values[i] == recQuant, true);

        if (this.model.getHost ().canEdit (EditCapability.QUANTIZE_INPUT_NOTE_LENGTH))
        {
            display.addOptionElement ("", " ", false, null, "Quantize Note Length", "", false, null, true);
            final boolean isQuantLength = track != null && track.isRecordQuantizationNoteLength ();
            display.addOptionElement ("", " ", false, "", isQuantLength ? "On" : "Off", isQuantLength, true);
        }
        else
        {
            display.addEmptyElement (true);
            display.addEmptyElement (true);
        }

        if (this.model.getHost ().canEdit (EditCapability.QUANTIZE_AMOUNT))
        {
            final int quantizeAmount = this.surface.getConfiguration ().getQuantizeAmount ();
            display.addParameterElement (" ", false, "", (ChannelType) null, null, false, "Qunt Amnt", quantizeAmount * 1023 / 100, quantizeAmount + "%", this.isKnobTouched[0], -1);
        }
        else
            display.addEmptyElement (true);
    }


    /** {@inheritDoc} */
    @Override
    public void onFirstRow (final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.UP)
            return;

        final ITrack track = this.model.getSelectedTrack ();
        if (track == null)
            return;

        switch (index)
        {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                track.setRecordQuantizationGrid (RecordQuantization.values ()[index]);
                break;

            case 6:
                track.toggleRecordQuantizationNoteLength ();
                break;

            default:
                // Not used
                break;
        }
    }


    /** {@inheritDoc} */
    @Override
    public void onSecondRow (final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.UP)
            return;

        if (index == 1)
            this.surface.getModeManager ().setActiveMode (Modes.GROOVE);
    }


    /** {@inheritDoc} */
    @Override
    public void updateFirstRow ()
    {
        final ColorManager colorManager = this.model.getColorManager ();
        final RecordQuantization [] values = RecordQuantization.values ();
        final ITrack track = this.model.getSelectedTrack ();
        final RecordQuantization recQuant = track == null ? RecordQuantization.RES_OFF : track.getRecordQuantizationGrid ();
        for (int i = 0; i < values.length; i++)
            this.surface.updateTrigger (20 + i, colorManager.getColor (values[i] == recQuant ? AbstractMode.BUTTON_COLOR_HI : AbstractMode.BUTTON_COLOR_ON));
        this.surface.updateTrigger (25, colorManager.getColor (AbstractMode.BUTTON_COLOR_OFF));
        this.surface.updateTrigger (26, colorManager.getColor (track != null && track.isRecordQuantizationNoteLength () ? AbstractMode.BUTTON_COLOR_HI : AbstractMode.BUTTON_COLOR_ON));
        this.surface.updateTrigger (27, colorManager.getColor (AbstractMode.BUTTON_COLOR_OFF));
    }


    /** {@inheritDoc} */
    @Override
    public void updateSecondRow ()
    {
        final ColorManager colorManager = this.model.getColorManager ();
        this.surface.updateTrigger (102, colorManager.getColor (AbstractMode.BUTTON_COLOR_HI));
        this.surface.updateTrigger (103, colorManager.getColor (AbstractMode.BUTTON_COLOR_ON));
        for (int i = 0; i < 6; i++)
            this.surface.updateTrigger (104 + i, colorManager.getColor (AbstractMode.BUTTON_COLOR_OFF));
    }
}
