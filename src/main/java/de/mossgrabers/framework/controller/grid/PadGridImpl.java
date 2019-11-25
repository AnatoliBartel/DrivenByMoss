// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller.grid;

import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.daw.midi.IMidiOutput;


/**
 * Implementation of a grid of pads.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class PadGridImpl implements PadGrid
{
    protected static final int   NUM_NOTES = 128;

    protected final IMidiOutput  output;
    protected final ColorManager colorManager;

    protected PadInfo []         padStates;

    protected final int          rows;
    protected final int          cols;
    protected final int          startNote;
    protected final int          endNote;


    /**
     * Constructor.
     *
     * @param colorManager The color manager for accessing specific colors to use
     * @param output The midi output which can address the pad states
     */
    public PadGridImpl (final ColorManager colorManager, final IMidiOutput output)
    {
        this (colorManager, output, 8, 8, 36);
    }


    /**
     * Constructor.
     *
     * @param colorManager The color manager for accessing specific colors to use
     * @param output The midi output which can address the pad states
     * @param rows The number of rows of the grid
     * @param cols The number of columns of the grid
     * @param startNote The start note of the grid
     */
    public PadGridImpl (final ColorManager colorManager, final IMidiOutput output, final int rows, final int cols, final int startNote)
    {
        this.colorManager = colorManager;
        this.output = output;
        this.rows = rows;
        this.cols = cols;
        this.startNote = startNote;
        this.endNote = this.startNote + this.rows * this.cols - 1;

        // Note: The grid contains only 64 pads but is more efficient to use
        // the 128 note values the pads understand
        this.padStates = new PadInfo [NUM_NOTES];
        for (int i = 0; i < NUM_NOTES; i++)
            this.padStates[i] = new PadInfo ();
    }


    /** {@inheritDoc} */
    @Override
    public void light (final int note, final int color)
    {
        this.setLight (note, color, -1, false);
    }


    /** {@inheritDoc} */
    @Override
    public void light (final int note, final int color, final int blinkColor, final boolean fast)
    {
        this.setLight (note, color, blinkColor, fast);
    }


    /** {@inheritDoc} */
    @Override
    public void lightEx (final int x, final int y, final int color)
    {
        this.lightEx (x, y, color, -1, false);
    }


    /** {@inheritDoc} */
    @Override
    public void lightEx (final int x, final int y, final int color, final int blinkColor, final boolean fast)
    {
        final int off = (this.rows - 1) * this.cols + this.startNote;
        this.setLight (off + x - this.cols * y, color, blinkColor, fast);
    }


    /** {@inheritDoc} */
    @Override
    public void light (final int note, final String colorID)
    {
        this.light (note, colorID, null, false);
    }


    /** {@inheritDoc} */
    @Override
    public void lightEx (final int x, final int y, final String colorID)
    {
        this.lightEx (x, y, colorID, null, false);
    }


    /** {@inheritDoc} */
    @Override
    public void light (final int note, final String colorID, final String blinkColorID, final boolean fast)
    {
        this.light (note, this.colorManager.getColorIndex (colorID), blinkColorID == null ? -1 : this.colorManager.getColorIndex (blinkColorID), fast);
    }


    /** {@inheritDoc} */
    @Override
    public void lightEx (final int x, final int y, final String colorID, final String blinkColorID, final boolean fast)
    {
        this.lightEx (x, y, this.colorManager.getColorIndex (colorID), blinkColorID == null ? -1 : this.colorManager.getColorIndex (blinkColorID), fast);
    }


    /**
     * Set the lighting state of a pad.
     *
     * @param note The note in the array (0-127)
     * @param color The color or brightness to set
     * @param blinkColor The state to make a pad blink
     * @param fast Blinking is fast if true
     */
    protected void setLight (final int note, final int color, final int blinkColor, final boolean fast)
    {
        this.padStates[note].setColors (color, blinkColor >= 0 ? blinkColor : this.colorManager.getColorIndex (GRID_OFF), fast);
    }


    /** {@inheritDoc} */
    @Override
    public void forceFlush (final int note)
    {
        this.padStates[note].setColors (-1, -1, false);
    }


    /** {@inheritDoc} */
    @Override
    public void forceFlush ()
    {
        for (int i = this.startNote; i <= this.endNote; i++)
            this.padStates[i].setColors (-1, -1, false);
    }


    /** {@inheritDoc} */
    @Override
    public PadInfo getPadInfo (final int note)
    {
        return this.padStates[note];
    }


    /** {@inheritDoc} */
    @Override
    public void sendPadState (final int note)
    {
        final PadInfo state = this.padStates[note];
        final int translated = this.translateToController (note);
        final int color = state.getColor ();
        this.sendNoteState (translated, color < 0 ? 0 : color);
        final int blinkColor = state.getBlinkColor ();
        if (blinkColor > 0 && blinkColor < 128)
            this.sendBlinkState (translated, blinkColor, state.isFast ());
    }


    /**
     * Send the note/pad update to the controller.
     *
     * @param note The note
     * @param color The color
     */
    protected void sendNoteState (final int note, final int color)
    {
        this.output.sendNote (note, color);
    }


    /**
     * Set the given pad/note to blink.
     *
     * @param note The note
     * @param blinkColor The color to use for blinking
     * @param fast Blink fast or slow
     */
    protected void sendBlinkState (final int note, final int blinkColor, final boolean fast)
    {
        this.output.sendNoteEx (fast ? 14 : 10, note, blinkColor);
    }


    /** {@inheritDoc} */
    @Override
    public void turnOff ()
    {
        final int color = this.colorManager.getColorIndex (GRID_OFF);
        for (int i = this.startNote; i <= this.endNote; i++)
        {
            this.light (i, color, -1, false);
            this.sendPadState (i);
        }
    }


    /** {@inheritDoc} */
    @Override
    public int translateToGrid (final int note)
    {
        return note;
    }


    /** {@inheritDoc} */
    @Override
    public int translateToController (final int note)
    {
        return note;
    }


    /** {@inheritDoc} */
    @Override
    public int getRows ()
    {
        return this.rows;
    }


    /** {@inheritDoc} */
    @Override
    public int getCols ()
    {
        return this.cols;
    }


    /** {@inheritDoc} */
    @Override
    public int getStartNote ()
    {
        return this.startNote;
    }


    /** {@inheritDoc} */
    @Override
    public boolean isGridNote (final int note)
    {
        final int gridNote = this.translateToGrid (note);
        return gridNote >= this.startNote && gridNote <= this.endNote;
    }
}