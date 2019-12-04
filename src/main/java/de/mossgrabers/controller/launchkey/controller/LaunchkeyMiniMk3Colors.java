// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.launchkey.controller;

import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.grid.PadGrid;
import de.mossgrabers.framework.daw.DAWColor;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.view.AbstractDrumView;
import de.mossgrabers.framework.view.AbstractPlayView;
import de.mossgrabers.framework.view.AbstractSequencerView;
import de.mossgrabers.framework.view.AbstractSessionView;


/**
 * Different colors to use for the pads and buttons of Launchkey Mini Mk3.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
@SuppressWarnings("javadoc")
public class LaunchkeyMiniMk3Colors
{
    public static final int    LAUNCHKEY_COLOR_BLACK            = 0;
    public static final int    LAUNCHKEY_COLOR_GREY_LO          = 1;
    public static final int    LAUNCHKEY_COLOR_GREY_MD          = 2;
    public static final int    LAUNCHKEY_COLOR_WHITE            = 3;
    public static final int    LAUNCHKEY_COLOR_ROSE             = 4;
    public static final int    LAUNCHKEY_COLOR_RED_HI           = 5;
    public static final int    LAUNCHKEY_COLOR_RED              = 6;
    public static final int    LAUNCHKEY_COLOR_RED_LO           = 7;
    public static final int    LAUNCHKEY_COLOR_RED_AMBER        = 8;
    public static final int    LAUNCHKEY_COLOR_AMBER_HI         = 9;
    public static final int    LAUNCHKEY_COLOR_AMBER            = 10;
    public static final int    LAUNCHKEY_COLOR_AMBER_LO         = 11;
    public static final int    LAUNCHKEY_COLOR_AMBER_YELLOW     = 12;
    public static final int    LAUNCHKEY_COLOR_YELLOW_HI        = 13;
    public static final int    LAUNCHKEY_COLOR_YELLOW           = 14;
    public static final int    LAUNCHKEY_COLOR_YELLOW_LO        = 15;
    public static final int    LAUNCHKEY_COLOR_YELLOW_LIME      = 16;
    public static final int    LAUNCHKEY_COLOR_LIME_HI          = 17;
    public static final int    LAUNCHKEY_COLOR_LIME             = 18;
    public static final int    LAUNCHKEY_COLOR_LIME_LO          = 19;
    public static final int    LAUNCHKEY_COLOR_LIME_GREEN       = 20;
    public static final int    LAUNCHKEY_COLOR_GREEN_HI         = 21;
    public static final int    LAUNCHKEY_COLOR_GREEN            = 22;
    public static final int    LAUNCHKEY_COLOR_GREEN_LO         = 23;
    public static final int    LAUNCHKEY_COLOR_GREEN_SPRING     = 24;
    public static final int    LAUNCHKEY_COLOR_SPRING_HI        = 25;
    public static final int    LAUNCHKEY_COLOR_SPRING           = 26;
    public static final int    LAUNCHKEY_COLOR_SPRING_LO        = 27;
    public static final int    LAUNCHKEY_COLOR_SPRING_TURQUOISE = 28;
    public static final int    LAUNCHKEY_COLOR_TURQUOISE_LO     = 29;
    public static final int    LAUNCHKEY_COLOR_TURQUOISE        = 30;
    public static final int    LAUNCHKEY_COLOR_TURQUOISE_HI     = 31;
    public static final int    LAUNCHKEY_COLOR_TURQUOISE_CYAN   = 32;
    public static final int    LAUNCHKEY_COLOR_CYAN_HI          = 33;
    public static final int    LAUNCHKEY_COLOR_CYAN             = 34;
    public static final int    LAUNCHKEY_COLOR_CYAN_LO          = 35;
    public static final int    LAUNCHKEY_COLOR_CYAN_SKY         = 36;
    public static final int    LAUNCHKEY_COLOR_SKY_HI           = 37;
    public static final int    LAUNCHKEY_COLOR_SKY              = 38;
    public static final int    LAUNCHKEY_COLOR_SKY_LO           = 39;
    public static final int    LAUNCHKEY_COLOR_SKY_OCEAN        = 40;
    public static final int    LAUNCHKEY_COLOR_OCEAN_HI         = 41;
    public static final int    LAUNCHKEY_COLOR_OCEAN            = 42;
    public static final int    LAUNCHKEY_COLOR_OCEAN_LO         = 43;
    public static final int    LAUNCHKEY_COLOR_OCEAN_BLUE       = 44;
    public static final int    LAUNCHKEY_COLOR_BLUE_HI          = 45;
    public static final int    LAUNCHKEY_COLOR_BLUE             = 46;
    public static final int    LAUNCHKEY_COLOR_BLUE_LO          = 47;
    public static final int    LAUNCHKEY_COLOR_BLUE_ORCHID      = 48;
    public static final int    LAUNCHKEY_COLOR_ORCHID_HI        = 49;
    public static final int    LAUNCHKEY_COLOR_ORCHID           = 50;
    public static final int    LAUNCHKEY_COLOR_ORCHID_LO        = 51;
    public static final int    LAUNCHKEY_COLOR_ORCHID_MAGENTA   = 52;
    public static final int    LAUNCHKEY_COLOR_MAGENTA_HI       = 53;
    public static final int    LAUNCHKEY_COLOR_MAGENTA          = 54;
    public static final int    LAUNCHKEY_COLOR_MAGENTA_LO       = 55;
    public static final int    LAUNCHKEY_COLOR_MAGENTA_PINK     = 56;
    public static final int    LAUNCHKEY_COLOR_PINK_HI          = 57;
    public static final int    LAUNCHKEY_COLOR_PINK             = 58;
    public static final int    LAUNCHKEY_COLOR_PINK_LO          = 59;
    public static final int    LAUNCHKEY_COLOR_ORANGE           = 60;

    public static final int [] DAW_INDICATOR_COLORS             =
    {
        LAUNCHKEY_COLOR_RED,
        LAUNCHKEY_COLOR_AMBER,
        LAUNCHKEY_COLOR_YELLOW,
        LAUNCHKEY_COLOR_SPRING,
        LAUNCHKEY_COLOR_CYAN,
        LAUNCHKEY_COLOR_OCEAN,
        LAUNCHKEY_COLOR_MAGENTA,
        LAUNCHKEY_COLOR_PINK
    };


    /**
     * Private due to utility class.
     */
    private LaunchkeyMiniMk3Colors ()
    {
        // Intentionally empty
    }


    /**
     * Configures all colors for Launchpad controllers.
     *
     * @param colorManager The color manager
     */
    public static void addColors (final ColorManager colorManager)
    {
        colorManager.registerColorIndex (Scales.SCALE_COLOR_OFF, LAUNCHKEY_COLOR_BLACK);
        colorManager.registerColorIndex (Scales.SCALE_COLOR_OCTAVE, LAUNCHKEY_COLOR_OCEAN_HI);
        colorManager.registerColorIndex (Scales.SCALE_COLOR_NOTE, LAUNCHKEY_COLOR_WHITE);
        colorManager.registerColorIndex (Scales.SCALE_COLOR_OUT_OF_SCALE, LAUNCHKEY_COLOR_BLACK);

        colorManager.registerColorIndex (AbstractSequencerView.COLOR_STEP_HILITE_NO_CONTENT, LAUNCHKEY_COLOR_GREEN_LO);
        colorManager.registerColorIndex (AbstractSequencerView.COLOR_STEP_HILITE_CONTENT, LAUNCHKEY_COLOR_GREEN_HI);
        colorManager.registerColorIndex (AbstractSequencerView.COLOR_NO_CONTENT, LAUNCHKEY_COLOR_BLACK);
        colorManager.registerColorIndex (AbstractSequencerView.COLOR_CONTENT, LAUNCHKEY_COLOR_BLUE_HI);
        colorManager.registerColorIndex (AbstractSequencerView.COLOR_CONTENT_CONT, LAUNCHKEY_COLOR_BLUE_ORCHID);
        colorManager.registerColorIndex (AbstractSequencerView.COLOR_PAGE, LAUNCHKEY_COLOR_WHITE);
        colorManager.registerColorIndex (AbstractSequencerView.COLOR_ACTIVE_PAGE, LAUNCHKEY_COLOR_GREEN);
        colorManager.registerColorIndex (AbstractSequencerView.COLOR_SELECTED_PAGE, LAUNCHKEY_COLOR_BLUE_ORCHID);

        colorManager.registerColorIndex (AbstractDrumView.COLOR_PAD_OFF, LAUNCHKEY_COLOR_BLACK);
        colorManager.registerColorIndex (AbstractDrumView.COLOR_PAD_RECORD, LAUNCHKEY_COLOR_RED_HI);
        colorManager.registerColorIndex (AbstractDrumView.COLOR_PAD_PLAY, LAUNCHKEY_COLOR_GREEN_HI);
        colorManager.registerColorIndex (AbstractDrumView.COLOR_PAD_SELECTED, LAUNCHKEY_COLOR_BLUE_HI);
        colorManager.registerColorIndex (AbstractDrumView.COLOR_PAD_MUTED, LAUNCHKEY_COLOR_AMBER_LO);
        colorManager.registerColorIndex (AbstractDrumView.COLOR_PAD_HAS_CONTENT, LAUNCHKEY_COLOR_YELLOW_HI);
        colorManager.registerColorIndex (AbstractDrumView.COLOR_PAD_NO_CONTENT, LAUNCHKEY_COLOR_YELLOW_LO);

        colorManager.registerColorIndex (AbstractPlayView.COLOR_PLAY, LAUNCHKEY_COLOR_GREEN_HI);
        colorManager.registerColorIndex (AbstractPlayView.COLOR_RECORD, LAUNCHKEY_COLOR_RED_HI);
        colorManager.registerColorIndex (AbstractPlayView.COLOR_OFF, LAUNCHKEY_COLOR_BLACK);

        colorManager.registerColorIndex (AbstractSessionView.COLOR_SCENE, LAUNCHKEY_COLOR_GREEN);
        colorManager.registerColorIndex (AbstractSessionView.COLOR_SELECTED_SCENE, LAUNCHKEY_COLOR_GREEN_HI);
        colorManager.registerColorIndex (AbstractSessionView.COLOR_SCENE_OFF, LAUNCHKEY_COLOR_BLACK);

        colorManager.registerColorIndex (PadGrid.GRID_OFF, LAUNCHKEY_COLOR_BLACK);

        colorManager.registerColorIndex (DAWColor.COLOR_OFF, LAUNCHKEY_COLOR_BLACK);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_GRAY_HALF, LAUNCHKEY_COLOR_GREY_MD);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_DARK_GRAY, LAUNCHKEY_COLOR_GREY_LO);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_GRAY, LAUNCHKEY_COLOR_GREY_MD);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_LIGHT_GRAY, LAUNCHKEY_COLOR_GREY_LO);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_SILVER, LAUNCHKEY_COLOR_SKY_OCEAN);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_DARK_BROWN, LAUNCHKEY_COLOR_AMBER_LO);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_BROWN, LAUNCHKEY_COLOR_AMBER_YELLOW);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_DARK_BLUE, LAUNCHKEY_COLOR_OCEAN);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_PURPLE_BLUE, LAUNCHKEY_COLOR_OCEAN_BLUE);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_PURPLE, LAUNCHKEY_COLOR_ORCHID_HI);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_PINK, LAUNCHKEY_COLOR_PINK_HI);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_RED, LAUNCHKEY_COLOR_RED);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_ORANGE, LAUNCHKEY_COLOR_ORANGE);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_LIGHT_ORANGE, LAUNCHKEY_COLOR_AMBER_HI);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_MOSS_GREEN, LAUNCHKEY_COLOR_LIME_LO);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_GREEN, LAUNCHKEY_COLOR_SPRING);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_COLD_GREEN, LAUNCHKEY_COLOR_TURQUOISE);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_BLUE, LAUNCHKEY_COLOR_SKY_HI);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_LIGHT_PURPLE, LAUNCHKEY_COLOR_BLUE_ORCHID);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_LIGHT_PINK, LAUNCHKEY_COLOR_MAGENTA_PINK);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_SKIN, LAUNCHKEY_COLOR_ROSE);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_REDDISH_BROWN, LAUNCHKEY_COLOR_AMBER);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_LIGHT_BROWN, LAUNCHKEY_COLOR_AMBER_YELLOW);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_LIGHT_GREEN, LAUNCHKEY_COLOR_LIME);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_BLUISH_GREEN, LAUNCHKEY_COLOR_SPRING_HI);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_GREEN_BLUE, LAUNCHKEY_COLOR_TURQUOISE_CYAN);
        colorManager.registerColorIndex (DAWColor.DAW_COLOR_LIGHT_BLUE, LAUNCHKEY_COLOR_OCEAN_HI);

        colorManager.registerColorIndex (ColorManager.BUTTON_STATE_OFF, 0);
        colorManager.registerColorIndex (ColorManager.BUTTON_STATE_ON, 1);
        colorManager.registerColorIndex (ColorManager.BUTTON_STATE_HI, 127);
    }
}