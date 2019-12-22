// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.maschine.mikro.mk3.controller;

import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.mode.AbstractMode;


/**
 * Color states to use for the Maschine Mikro Mk3 buttons.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class MaschineMikroMk3ColorManager extends ColorManager
{
    /**
     * Private due to utility class.
     */
    public MaschineMikroMk3ColorManager ()
    {
        this.registerColorIndex (AbstractMode.BUTTON_COLOR_OFF, 0);
        this.registerColorIndex (AbstractMode.BUTTON_COLOR_ON, 0);
        this.registerColorIndex (AbstractMode.BUTTON_COLOR_HI, 127);

        this.registerColorIndex (ColorManager.BUTTON_STATE_OFF, 0);
        this.registerColorIndex (ColorManager.BUTTON_STATE_ON, 0);
        this.registerColorIndex (ColorManager.BUTTON_STATE_HI, 127);
    }


    /** {@inheritDoc} */
    @Override
    public ColorEx getColor (final int colorIndex, final ButtonID buttonID)
    {
        if (colorIndex < 0)
            return ColorEx.BLACK;

        if (buttonID == null)
            return ColorEx.GRAY;

        switch (buttonID)
        {
            case PLAY:
                return colorIndex > 0 ? ColorEx.GREEN : ColorEx.DARK_GREEN;
            case RECORD:
                return colorIndex > 0 ? ColorEx.RED : ColorEx.DARK_RED;
            default:
                return colorIndex > 0 ? ColorEx.WHITE : ColorEx.GRAY;
        }
    }
}