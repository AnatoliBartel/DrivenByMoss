// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.kontrol.mkii.controller;

import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.mode.AbstractMode;


/**
 * Different colors to use for the buttons of Komplete Kontrol MkII.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class KontrolProtocolColorManager extends ColorManager
{
    /**
     * Constructor.
     */
    public KontrolProtocolColorManager ()
    {
        this.registerColorIndex (AbstractMode.BUTTON_COLOR_OFF, 0);
        this.registerColorIndex (AbstractMode.BUTTON_COLOR_ON, 0);
        this.registerColorIndex (AbstractMode.BUTTON_COLOR_HI, 1);

        this.registerColorIndex (ColorManager.BUTTON_STATE_OFF, 0);
        this.registerColorIndex (ColorManager.BUTTON_STATE_ON, 0);
        this.registerColorIndex (ColorManager.BUTTON_STATE_HI, 1);
    }


    /** {@inheritDoc} */
    @Override
    public ColorEx getColor (final int colorIndex, final ButtonID buttonID)
    {
        if (buttonID == null)
            return ColorEx.BLACK;

        switch (buttonID)
        {
            case PLAY:
                return colorIndex > 0 ? ColorEx.GREEN : ColorEx.DARK_GREEN;
            case RECORD:
            case REC_ARM:
                return colorIndex > 0 ? ColorEx.RED : ColorEx.DARK_RED;
            case SOLO:
                return colorIndex > 0 ? ColorEx.BLUE : ColorEx.DARK_BLUE;
            case MUTE:
                return colorIndex > 0 ? ColorEx.ORANGE : ColorEx.DARK_ORANGE;
            default:
                return colorIndex > 0 ? ColorEx.WHITE : ColorEx.DARK_GRAY;
        }
    }
}