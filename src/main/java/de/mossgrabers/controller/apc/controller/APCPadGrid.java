// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.apc.controller;

import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.grid.PadGridImpl;
import de.mossgrabers.framework.daw.midi.IMidiOutput;


/**
 * Implementation of the APC grid of pads.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
/**
 *
 * @author mos (Fraunhofer IOSB)
 */
public class APCPadGrid extends PadGridImpl
{
    private final boolean isMkII;


    /**
     * Constructor.
     *
     * @param colorManager The color manager for accessing specific colors to use
     * @param output The midi output which can address the pad states
     * @param isMkII True if it is the MkII
     */
    public APCPadGrid (final ColorManager colorManager, final IMidiOutput output, final boolean isMkII)
    {
        super (colorManager, output, 5, 8, 36);
        this.isMkII = isMkII;
    }


    /** {@inheritDoc} */
    @Override
    protected void sendBlinkState (final int channel, final int note, final int blinkColor, final boolean fast)
    {
        if (this.isMkII)
            this.output.sendNoteEx (fast ? 12 : 10, note, blinkColor);
        else
            this.output.sendNoteEx (channel, note, blinkColor);
    }


    /** {@inheritDoc} */
    @Override
    public int translateToGrid (final int note)
    {
        return note + 36;
    }


    /** {@inheritDoc} */
    @Override
    public int [] translateToController (final int note)
    {
        int n = note - 36;

        if (this.isMkII)
            return new int []
            {
                0,
                n
            };

        return new int []
        {
            n % 8,
            0x39 - n / 8
        };
    }
}