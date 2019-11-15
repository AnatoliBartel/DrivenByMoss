// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.launchpad.controller;

import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.grid.PadGrid;
import de.mossgrabers.framework.daw.DAWColors;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.view.AbstractDrumView;
import de.mossgrabers.framework.view.AbstractPlayView;
import de.mossgrabers.framework.view.AbstractSequencerView;


/**
 * Different colors to use for the pads and buttons of Launchpad.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
@SuppressWarnings("javadoc")
public class LaunchpadColors
{
    public static final int    LAUNCHPAD_COLOR_BLACK            = 0;
    public static final int    LAUNCHPAD_COLOR_GREY_LO          = 1;
    public static final int    LAUNCHPAD_COLOR_GREY_MD          = 2;
    public static final int    LAUNCHPAD_COLOR_WHITE            = 3;
    public static final int    LAUNCHPAD_COLOR_ROSE             = 4;
    public static final int    LAUNCHPAD_COLOR_RED_HI           = 5;
    public static final int    LAUNCHPAD_COLOR_RED              = 6;
    public static final int    LAUNCHPAD_COLOR_RED_LO           = 7;
    public static final int    LAUNCHPAD_COLOR_RED_AMBER        = 8;
    public static final int    LAUNCHPAD_COLOR_AMBER_HI         = 9;
    public static final int    LAUNCHPAD_COLOR_AMBER            = 10;
    public static final int    LAUNCHPAD_COLOR_AMBER_LO         = 11;
    public static final int    LAUNCHPAD_COLOR_AMBER_YELLOW     = 12;
    public static final int    LAUNCHPAD_COLOR_YELLOW_HI        = 13;
    public static final int    LAUNCHPAD_COLOR_YELLOW           = 14;
    public static final int    LAUNCHPAD_COLOR_YELLOW_LO        = 15;
    public static final int    LAUNCHPAD_COLOR_YELLOW_LIME      = 16;
    public static final int    LAUNCHPAD_COLOR_LIME_HI          = 17;
    public static final int    LAUNCHPAD_COLOR_LIME             = 18;
    public static final int    LAUNCHPAD_COLOR_LIME_LO          = 19;
    public static final int    LAUNCHPAD_COLOR_LIME_GREEN       = 20;
    public static final int    LAUNCHPAD_COLOR_GREEN_HI         = 21;
    public static final int    LAUNCHPAD_COLOR_GREEN            = 22;
    public static final int    LAUNCHPAD_COLOR_GREEN_LO         = 23;
    public static final int    LAUNCHPAD_COLOR_GREEN_SPRING     = 24;
    public static final int    LAUNCHPAD_COLOR_SPRING_HI        = 25;
    public static final int    LAUNCHPAD_COLOR_SPRING           = 26;
    public static final int    LAUNCHPAD_COLOR_SPRING_LO        = 27;
    public static final int    LAUNCHPAD_COLOR_SPRING_TURQUOISE = 28;
    public static final int    LAUNCHPAD_COLOR_TURQUOISE_LO     = 29;
    public static final int    LAUNCHPAD_COLOR_TURQUOISE        = 30;
    public static final int    LAUNCHPAD_COLOR_TURQUOISE_HI     = 31;
    public static final int    LAUNCHPAD_COLOR_TURQUOISE_CYAN   = 32;
    public static final int    LAUNCHPAD_COLOR_CYAN_HI          = 33;
    public static final int    LAUNCHPAD_COLOR_CYAN             = 34;
    public static final int    LAUNCHPAD_COLOR_CYAN_LO          = 35;
    public static final int    LAUNCHPAD_COLOR_CYAN_SKY         = 36;
    public static final int    LAUNCHPAD_COLOR_SKY_HI           = 37;
    public static final int    LAUNCHPAD_COLOR_SKY              = 38;
    public static final int    LAUNCHPAD_COLOR_SKY_LO           = 39;
    public static final int    LAUNCHPAD_COLOR_SKY_OCEAN        = 40;
    public static final int    LAUNCHPAD_COLOR_OCEAN_HI         = 41;
    public static final int    LAUNCHPAD_COLOR_OCEAN            = 42;
    public static final int    LAUNCHPAD_COLOR_OCEAN_LO         = 43;
    public static final int    LAUNCHPAD_COLOR_OCEAN_BLUE       = 44;
    public static final int    LAUNCHPAD_COLOR_BLUE_HI          = 45;
    public static final int    LAUNCHPAD_COLOR_BLUE             = 46;
    public static final int    LAUNCHPAD_COLOR_BLUE_LO          = 47;
    public static final int    LAUNCHPAD_COLOR_BLUE_ORCHID      = 48;
    public static final int    LAUNCHPAD_COLOR_ORCHID_HI        = 49;
    public static final int    LAUNCHPAD_COLOR_ORCHID           = 50;
    public static final int    LAUNCHPAD_COLOR_ORCHID_LO        = 51;
    public static final int    LAUNCHPAD_COLOR_ORCHID_MAGENTA   = 52;
    public static final int    LAUNCHPAD_COLOR_MAGENTA_HI       = 53;
    public static final int    LAUNCHPAD_COLOR_MAGENTA          = 54;
    public static final int    LAUNCHPAD_COLOR_MAGENTA_LO       = 55;
    public static final int    LAUNCHPAD_COLOR_MAGENTA_PINK     = 56;
    public static final int    LAUNCHPAD_COLOR_PINK_HI          = 57;
    public static final int    LAUNCHPAD_COLOR_PINK             = 58;
    public static final int    LAUNCHPAD_COLOR_PINK_LO          = 59;
    public static final int    LAUNCHPAD_COLOR_ORANGE           = 60;

    public static final int [] DAW_INDICATOR_COLORS             =
    {
        LAUNCHPAD_COLOR_RED,
        LAUNCHPAD_COLOR_AMBER,
        LAUNCHPAD_COLOR_YELLOW,
        LAUNCHPAD_COLOR_SPRING,
        LAUNCHPAD_COLOR_CYAN,
        LAUNCHPAD_COLOR_OCEAN,
        LAUNCHPAD_COLOR_MAGENTA,
        LAUNCHPAD_COLOR_PINK
    };


    /**
     * Private due to utility class.
     */
    private LaunchpadColors ()
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
        colorManager.registerColorIndex (Scales.SCALE_COLOR_OFF, LAUNCHPAD_COLOR_BLACK);
        colorManager.registerColorIndex (Scales.SCALE_COLOR_OCTAVE, LAUNCHPAD_COLOR_OCEAN_HI);
        colorManager.registerColorIndex (Scales.SCALE_COLOR_NOTE, LAUNCHPAD_COLOR_WHITE);
        colorManager.registerColorIndex (Scales.SCALE_COLOR_OUT_OF_SCALE, LAUNCHPAD_COLOR_BLACK);

        colorManager.registerColorIndex (AbstractSequencerView.COLOR_STEP_HILITE_NO_CONTENT, LAUNCHPAD_COLOR_GREEN_LO);
        colorManager.registerColorIndex (AbstractSequencerView.COLOR_STEP_HILITE_CONTENT, LAUNCHPAD_COLOR_GREEN_HI);
        colorManager.registerColorIndex (AbstractSequencerView.COLOR_NO_CONTENT, LAUNCHPAD_COLOR_BLACK);
        colorManager.registerColorIndex (AbstractSequencerView.COLOR_CONTENT, LAUNCHPAD_COLOR_BLUE_HI);
        colorManager.registerColorIndex (AbstractSequencerView.COLOR_CONTENT_CONT, LAUNCHPAD_COLOR_BLUE_ORCHID);
        colorManager.registerColorIndex (AbstractSequencerView.COLOR_PAGE, LAUNCHPAD_COLOR_WHITE);
        colorManager.registerColorIndex (AbstractSequencerView.COLOR_ACTIVE_PAGE, LAUNCHPAD_COLOR_GREEN);
        colorManager.registerColorIndex (AbstractSequencerView.COLOR_SELECTED_PAGE, LAUNCHPAD_COLOR_BLUE_ORCHID);

        colorManager.registerColorIndex (AbstractDrumView.COLOR_PAD_OFF, LAUNCHPAD_COLOR_BLACK);
        colorManager.registerColorIndex (AbstractDrumView.COLOR_PAD_RECORD, LAUNCHPAD_COLOR_RED_HI);
        colorManager.registerColorIndex (AbstractDrumView.COLOR_PAD_PLAY, LAUNCHPAD_COLOR_GREEN_HI);
        colorManager.registerColorIndex (AbstractDrumView.COLOR_PAD_SELECTED, LAUNCHPAD_COLOR_BLUE_HI);
        colorManager.registerColorIndex (AbstractDrumView.COLOR_PAD_MUTED, LAUNCHPAD_COLOR_AMBER_LO);
        colorManager.registerColorIndex (AbstractDrumView.COLOR_PAD_HAS_CONTENT, LAUNCHPAD_COLOR_YELLOW_HI);
        colorManager.registerColorIndex (AbstractDrumView.COLOR_PAD_NO_CONTENT, LAUNCHPAD_COLOR_YELLOW_LO);

        colorManager.registerColorIndex (AbstractPlayView.COLOR_PLAY, LAUNCHPAD_COLOR_GREEN_HI);
        colorManager.registerColorIndex (AbstractPlayView.COLOR_RECORD, LAUNCHPAD_COLOR_RED_HI);
        colorManager.registerColorIndex (AbstractPlayView.COLOR_OFF, LAUNCHPAD_COLOR_BLACK);

        colorManager.registerColorIndex (PadGrid.GRID_OFF, LAUNCHPAD_COLOR_BLACK);

        colorManager.registerColorIndex (DAWColors.COLOR_OFF, LAUNCHPAD_COLOR_BLACK);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_GRAY_HALF, LAUNCHPAD_COLOR_GREY_MD);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_DARK_GRAY, LAUNCHPAD_COLOR_GREY_LO);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_GRAY, LAUNCHPAD_COLOR_GREY_MD);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_LIGHT_GRAY, LAUNCHPAD_COLOR_GREY_LO);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_SILVER, LAUNCHPAD_COLOR_SKY_OCEAN);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_DARK_BROWN, LAUNCHPAD_COLOR_AMBER_LO);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_BROWN, LAUNCHPAD_COLOR_AMBER_YELLOW);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_DARK_BLUE, LAUNCHPAD_COLOR_OCEAN);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_PURPLE_BLUE, LAUNCHPAD_COLOR_OCEAN_BLUE);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_PURPLE, LAUNCHPAD_COLOR_ORCHID_HI);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_PINK, LAUNCHPAD_COLOR_PINK_HI);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_RED, LAUNCHPAD_COLOR_RED);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_ORANGE, LAUNCHPAD_COLOR_ORANGE);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_LIGHT_ORANGE, LAUNCHPAD_COLOR_AMBER_HI);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_MOSS_GREEN, LAUNCHPAD_COLOR_LIME_LO);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_GREEN, LAUNCHPAD_COLOR_SPRING);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_COLD_GREEN, LAUNCHPAD_COLOR_TURQUOISE);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_BLUE, LAUNCHPAD_COLOR_SKY_HI);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_LIGHT_PURPLE, LAUNCHPAD_COLOR_BLUE_ORCHID);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_LIGHT_PINK, LAUNCHPAD_COLOR_MAGENTA_PINK);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_SKIN, LAUNCHPAD_COLOR_ROSE);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_REDDISH_BROWN, LAUNCHPAD_COLOR_AMBER);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_LIGHT_BROWN, LAUNCHPAD_COLOR_AMBER_YELLOW);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_LIGHT_GREEN, LAUNCHPAD_COLOR_LIME);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_BLUISH_GREEN, LAUNCHPAD_COLOR_SPRING_HI);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_GREEN_BLUE, LAUNCHPAD_COLOR_TURQUOISE_CYAN);
        colorManager.registerColorIndex (DAWColors.DAW_COLOR_LIGHT_BLUE, LAUNCHPAD_COLOR_OCEAN_HI);

        colorManager.registerColorIndex (ColorManager.BUTTON_STATE_OFF, LAUNCHPAD_COLOR_BLACK);
        colorManager.registerColorIndex (ColorManager.BUTTON_STATE_ON, LAUNCHPAD_COLOR_YELLOW_LO);
        colorManager.registerColorIndex (ColorManager.BUTTON_STATE_HI, LAUNCHPAD_COLOR_YELLOW_HI);
    }
}