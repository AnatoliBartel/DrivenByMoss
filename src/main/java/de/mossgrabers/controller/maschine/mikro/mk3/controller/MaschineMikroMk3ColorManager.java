// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.maschine.mikro.mk3.controller;

import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.daw.DAWColor;
import de.mossgrabers.framework.mode.AbstractMode;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.view.AbstractDrumView;
import de.mossgrabers.framework.view.AbstractPlayView;


/**
 * Color states to use for the Maschine Mikro Mk3 buttons.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
@SuppressWarnings("javadoc")
public class MaschineMikroMk3ColorManager extends ColorManager
{
    public static final int COLOR_BLACK        = 0;
    public static final int COLOR_GREY         = 76;
    public static final int COLOR_WHITE        = 78;
    public static final int COLOR_ROSE         = 7;
    public static final int COLOR_RED          = 6;
    public static final int COLOR_RED_LO       = 5;
    public static final int COLOR_AMBER        = 14;
    public static final int COLOR_AMBER_LO     = 13;
    public static final int COLOR_LIME         = 34;
    public static final int COLOR_LIME_LO      = 33;
    public static final int COLOR_GREEN        = 30;
    public static final int COLOR_GREEN_LO     = 29;
    public static final int COLOR_SPRING       = 26;
    public static final int COLOR_SPRING_LO    = 25;
    public static final int COLOR_TURQUOISE_LO = 27;
    public static final int COLOR_TURQUOISE    = 31;
    public static final int COLOR_SKY          = 38;
    public static final int COLOR_SKY_LO       = 37;
    public static final int COLOR_BLUE         = 42;
    public static final int COLOR_BLUE_LO      = 41;
    public static final int COLOR_MAGENTA      = 58;
    public static final int COLOR_MAGENTA_LO   = 57;
    public static final int COLOR_PINK         = 62;
    public static final int COLOR_PINK_LO      = 61;
    public static final int COLOR_ORANGE       = 10;
    public static final int COLOR_ORANGE_LO    = 9;
    public static final int COLOR_PURPLE       = 50;
    public static final int COLOR_PURPLE_LO    = 49;
    public static final int COLOR_SKIN         = 11;
    public static final int COLOR_YELLOW       = 22;


    /**
     * Private due to utility class.
     */
    public MaschineMikroMk3ColorManager ()
    {
        this.registerColorIndex (ColorManager.BUTTON_STATE_OFF, 0);
        this.registerColorIndex (ColorManager.BUTTON_STATE_ON, 0);
        this.registerColorIndex (ColorManager.BUTTON_STATE_HI, 127);

        this.registerColorIndex (Scales.SCALE_COLOR_OFF, COLOR_BLACK);
        this.registerColorIndex (Scales.SCALE_COLOR_OCTAVE, COLOR_BLUE);
        this.registerColorIndex (Scales.SCALE_COLOR_NOTE, COLOR_WHITE);
        this.registerColorIndex (Scales.SCALE_COLOR_OUT_OF_SCALE, COLOR_BLACK);

        this.registerColorIndex (AbstractMode.BUTTON_COLOR_OFF, 0);
        this.registerColorIndex (AbstractMode.BUTTON_COLOR_ON, 0);
        this.registerColorIndex (AbstractMode.BUTTON_COLOR_HI, 127);

        this.registerColorIndex (AbstractPlayView.COLOR_PLAY, COLOR_GREEN);
        this.registerColorIndex (AbstractPlayView.COLOR_RECORD, COLOR_RED);
        this.registerColorIndex (AbstractPlayView.COLOR_OFF, COLOR_BLACK);

        this.registerColorIndex (AbstractDrumView.COLOR_PAD_OFF, COLOR_BLACK);
        this.registerColorIndex (AbstractDrumView.COLOR_PAD_RECORD, COLOR_RED);
        this.registerColorIndex (AbstractDrumView.COLOR_PAD_PLAY, COLOR_GREEN);
        this.registerColorIndex (AbstractDrumView.COLOR_PAD_SELECTED, COLOR_BLUE);
        this.registerColorIndex (AbstractDrumView.COLOR_PAD_MUTED, COLOR_AMBER);
        this.registerColorIndex (AbstractDrumView.COLOR_PAD_HAS_CONTENT, COLOR_GREY);
        this.registerColorIndex (AbstractDrumView.COLOR_PAD_NO_CONTENT, COLOR_BLACK);

        this.registerColorIndex (DAWColor.COLOR_OFF, COLOR_BLACK);
        this.registerColorIndex (DAWColor.DAW_COLOR_GRAY_HALF, COLOR_GREY);
        this.registerColorIndex (DAWColor.DAW_COLOR_DARK_GRAY, COLOR_GREY);
        this.registerColorIndex (DAWColor.DAW_COLOR_GRAY, COLOR_GREY);
        this.registerColorIndex (DAWColor.DAW_COLOR_LIGHT_GRAY, COLOR_GREY);
        this.registerColorIndex (DAWColor.DAW_COLOR_SILVER, COLOR_GREY);
        this.registerColorIndex (DAWColor.DAW_COLOR_DARK_BROWN, COLOR_AMBER_LO);
        this.registerColorIndex (DAWColor.DAW_COLOR_BROWN, COLOR_AMBER_LO);
        this.registerColorIndex (DAWColor.DAW_COLOR_DARK_BLUE, COLOR_BLUE_LO);
        this.registerColorIndex (DAWColor.DAW_COLOR_PURPLE_BLUE, COLOR_PURPLE_LO);
        this.registerColorIndex (DAWColor.DAW_COLOR_PURPLE, COLOR_PURPLE);
        this.registerColorIndex (DAWColor.DAW_COLOR_PINK, COLOR_PINK);
        this.registerColorIndex (DAWColor.DAW_COLOR_RED, COLOR_RED);
        this.registerColorIndex (DAWColor.DAW_COLOR_ORANGE, COLOR_ORANGE);
        this.registerColorIndex (DAWColor.DAW_COLOR_LIGHT_ORANGE, COLOR_YELLOW);
        this.registerColorIndex (DAWColor.DAW_COLOR_MOSS_GREEN, COLOR_LIME_LO);
        this.registerColorIndex (DAWColor.DAW_COLOR_GREEN, COLOR_SPRING);
        this.registerColorIndex (DAWColor.DAW_COLOR_COLD_GREEN, COLOR_TURQUOISE);
        this.registerColorIndex (DAWColor.DAW_COLOR_BLUE, COLOR_BLUE);
        this.registerColorIndex (DAWColor.DAW_COLOR_LIGHT_PURPLE, COLOR_MAGENTA);
        this.registerColorIndex (DAWColor.DAW_COLOR_LIGHT_PINK, COLOR_PINK);
        this.registerColorIndex (DAWColor.DAW_COLOR_SKIN, COLOR_SKIN);
        this.registerColorIndex (DAWColor.DAW_COLOR_REDDISH_BROWN, COLOR_AMBER);
        this.registerColorIndex (DAWColor.DAW_COLOR_LIGHT_BROWN, COLOR_AMBER_LO);
        this.registerColorIndex (DAWColor.DAW_COLOR_LIGHT_GREEN, COLOR_SPRING);
        this.registerColorIndex (DAWColor.DAW_COLOR_BLUISH_GREEN, COLOR_LIME);
        this.registerColorIndex (DAWColor.DAW_COLOR_GREEN_BLUE, COLOR_TURQUOISE_LO);
        this.registerColorIndex (DAWColor.DAW_COLOR_LIGHT_BLUE, COLOR_SKY);
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