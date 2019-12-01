// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.launchpad.controller;

import de.mossgrabers.framework.controller.color.ColorEx;
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
public class LaunchpadColorManager extends ColorManager
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
     * Constructor.
     */
    public LaunchpadColorManager ()
    {
        this.registerColorIndex (Scales.SCALE_COLOR_OFF, LAUNCHPAD_COLOR_BLACK);
        this.registerColorIndex (Scales.SCALE_COLOR_OCTAVE, LAUNCHPAD_COLOR_OCEAN_HI);
        this.registerColorIndex (Scales.SCALE_COLOR_NOTE, LAUNCHPAD_COLOR_WHITE);
        this.registerColorIndex (Scales.SCALE_COLOR_OUT_OF_SCALE, LAUNCHPAD_COLOR_BLACK);

        this.registerColorIndex (AbstractSequencerView.COLOR_STEP_HILITE_NO_CONTENT, LAUNCHPAD_COLOR_GREEN_LO);
        this.registerColorIndex (AbstractSequencerView.COLOR_STEP_HILITE_CONTENT, LAUNCHPAD_COLOR_GREEN_HI);
        this.registerColorIndex (AbstractSequencerView.COLOR_NO_CONTENT, LAUNCHPAD_COLOR_BLACK);
        this.registerColorIndex (AbstractSequencerView.COLOR_CONTENT, LAUNCHPAD_COLOR_BLUE_HI);
        this.registerColorIndex (AbstractSequencerView.COLOR_CONTENT_CONT, LAUNCHPAD_COLOR_BLUE_ORCHID);
        this.registerColorIndex (AbstractSequencerView.COLOR_PAGE, LAUNCHPAD_COLOR_WHITE);
        this.registerColorIndex (AbstractSequencerView.COLOR_ACTIVE_PAGE, LAUNCHPAD_COLOR_GREEN);
        this.registerColorIndex (AbstractSequencerView.COLOR_SELECTED_PAGE, LAUNCHPAD_COLOR_BLUE_ORCHID);

        this.registerColorIndex (AbstractDrumView.COLOR_PAD_OFF, LAUNCHPAD_COLOR_BLACK);
        this.registerColorIndex (AbstractDrumView.COLOR_PAD_RECORD, LAUNCHPAD_COLOR_RED_HI);
        this.registerColorIndex (AbstractDrumView.COLOR_PAD_PLAY, LAUNCHPAD_COLOR_GREEN_HI);
        this.registerColorIndex (AbstractDrumView.COLOR_PAD_SELECTED, LAUNCHPAD_COLOR_BLUE_HI);
        this.registerColorIndex (AbstractDrumView.COLOR_PAD_MUTED, LAUNCHPAD_COLOR_AMBER_LO);
        this.registerColorIndex (AbstractDrumView.COLOR_PAD_HAS_CONTENT, LAUNCHPAD_COLOR_YELLOW_HI);
        this.registerColorIndex (AbstractDrumView.COLOR_PAD_NO_CONTENT, LAUNCHPAD_COLOR_YELLOW_LO);

        this.registerColorIndex (AbstractPlayView.COLOR_PLAY, LAUNCHPAD_COLOR_GREEN_HI);
        this.registerColorIndex (AbstractPlayView.COLOR_RECORD, LAUNCHPAD_COLOR_RED_HI);
        this.registerColorIndex (AbstractPlayView.COLOR_OFF, LAUNCHPAD_COLOR_BLACK);

        this.registerColorIndex (PadGrid.GRID_OFF, LAUNCHPAD_COLOR_BLACK);

        this.registerColorIndex (DAWColors.COLOR_OFF, LAUNCHPAD_COLOR_BLACK);
        this.registerColorIndex (DAWColors.DAW_COLOR_GRAY_HALF, LAUNCHPAD_COLOR_GREY_MD);
        this.registerColorIndex (DAWColors.DAW_COLOR_DARK_GRAY, LAUNCHPAD_COLOR_GREY_LO);
        this.registerColorIndex (DAWColors.DAW_COLOR_GRAY, LAUNCHPAD_COLOR_GREY_MD);
        this.registerColorIndex (DAWColors.DAW_COLOR_LIGHT_GRAY, LAUNCHPAD_COLOR_GREY_LO);
        this.registerColorIndex (DAWColors.DAW_COLOR_SILVER, LAUNCHPAD_COLOR_SKY_OCEAN);
        this.registerColorIndex (DAWColors.DAW_COLOR_DARK_BROWN, LAUNCHPAD_COLOR_AMBER_LO);
        this.registerColorIndex (DAWColors.DAW_COLOR_BROWN, LAUNCHPAD_COLOR_AMBER_YELLOW);
        this.registerColorIndex (DAWColors.DAW_COLOR_DARK_BLUE, LAUNCHPAD_COLOR_OCEAN);
        this.registerColorIndex (DAWColors.DAW_COLOR_PURPLE_BLUE, LAUNCHPAD_COLOR_OCEAN_BLUE);
        this.registerColorIndex (DAWColors.DAW_COLOR_PURPLE, LAUNCHPAD_COLOR_ORCHID_HI);
        this.registerColorIndex (DAWColors.DAW_COLOR_PINK, LAUNCHPAD_COLOR_PINK_HI);
        this.registerColorIndex (DAWColors.DAW_COLOR_RED, LAUNCHPAD_COLOR_RED);
        this.registerColorIndex (DAWColors.DAW_COLOR_ORANGE, LAUNCHPAD_COLOR_ORANGE);
        this.registerColorIndex (DAWColors.DAW_COLOR_LIGHT_ORANGE, LAUNCHPAD_COLOR_AMBER_HI);
        this.registerColorIndex (DAWColors.DAW_COLOR_MOSS_GREEN, LAUNCHPAD_COLOR_LIME_LO);
        this.registerColorIndex (DAWColors.DAW_COLOR_GREEN, LAUNCHPAD_COLOR_SPRING);
        this.registerColorIndex (DAWColors.DAW_COLOR_COLD_GREEN, LAUNCHPAD_COLOR_TURQUOISE);
        this.registerColorIndex (DAWColors.DAW_COLOR_BLUE, LAUNCHPAD_COLOR_SKY_HI);
        this.registerColorIndex (DAWColors.DAW_COLOR_LIGHT_PURPLE, LAUNCHPAD_COLOR_BLUE_ORCHID);
        this.registerColorIndex (DAWColors.DAW_COLOR_LIGHT_PINK, LAUNCHPAD_COLOR_MAGENTA_PINK);
        this.registerColorIndex (DAWColors.DAW_COLOR_SKIN, LAUNCHPAD_COLOR_ROSE);
        this.registerColorIndex (DAWColors.DAW_COLOR_REDDISH_BROWN, LAUNCHPAD_COLOR_AMBER);
        this.registerColorIndex (DAWColors.DAW_COLOR_LIGHT_BROWN, LAUNCHPAD_COLOR_AMBER_YELLOW);
        this.registerColorIndex (DAWColors.DAW_COLOR_LIGHT_GREEN, LAUNCHPAD_COLOR_LIME);
        this.registerColorIndex (DAWColors.DAW_COLOR_BLUISH_GREEN, LAUNCHPAD_COLOR_SPRING_HI);
        this.registerColorIndex (DAWColors.DAW_COLOR_GREEN_BLUE, LAUNCHPAD_COLOR_TURQUOISE_CYAN);
        this.registerColorIndex (DAWColors.DAW_COLOR_LIGHT_BLUE, LAUNCHPAD_COLOR_OCEAN_HI);

        this.registerColorIndex (ColorManager.BUTTON_STATE_OFF, LAUNCHPAD_COLOR_BLACK);
        this.registerColorIndex (ColorManager.BUTTON_STATE_ON, LAUNCHPAD_COLOR_YELLOW_LO);
        this.registerColorIndex (ColorManager.BUTTON_STATE_HI, LAUNCHPAD_COLOR_YELLOW_HI);

        this.registerColor (LAUNCHPAD_COLOR_BLACK, ColorEx.BLACK);
        this.registerColor (LAUNCHPAD_COLOR_GREY_LO, DAWColors.DAW_COLOR_LIGHT_GRAY.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_GREY_MD, DAWColors.DAW_COLOR_GRAY_HALF.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_WHITE, ColorEx.WHITE);
        this.registerColor (LAUNCHPAD_COLOR_ROSE, DAWColors.DAW_COLOR_SKIN.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_RED_HI, DAWColors.DAW_COLOR_RED.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_RED, DAWColors.DAW_COLOR_REDDISH_BROWN.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_RED_LO, ColorEx.fromRGB (39, 4, 1));
        this.registerColor (LAUNCHPAD_COLOR_RED_AMBER, ColorEx.fromRGB (45, 34, 21));
        this.registerColor (LAUNCHPAD_COLOR_AMBER_HI, DAWColors.DAW_COLOR_LIGHT_ORANGE.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_AMBER, DAWColors.DAW_COLOR_REDDISH_BROWN.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_AMBER_LO, DAWColors.DAW_COLOR_DARK_BROWN.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_AMBER_YELLOW, DAWColors.DAW_COLOR_LIGHT_BROWN.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_YELLOW_HI, ColorEx.fromRGB (253, 250, 1));
        this.registerColor (LAUNCHPAD_COLOR_YELLOW, ColorEx.fromRGB (107, 105, 1));
        this.registerColor (LAUNCHPAD_COLOR_YELLOW_LO, ColorEx.fromRGB (37, 36, 1));
        this.registerColor (LAUNCHPAD_COLOR_YELLOW_LIME, ColorEx.fromRGB (141, 248, 57));
        this.registerColor (LAUNCHPAD_COLOR_LIME_HI, ColorEx.fromRGB (70, 247, 1));
        this.registerColor (LAUNCHPAD_COLOR_LIME, ColorEx.fromRGB (29, 104, 1));
        this.registerColor (LAUNCHPAD_COLOR_LIME_LO, DAWColors.DAW_COLOR_MOSS_GREEN.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_LIME_GREEN, ColorEx.fromRGB (53, 248, 58));
        this.registerColor (LAUNCHPAD_COLOR_GREEN_HI, ColorEx.fromRGB (1, 247, 1));
        this.registerColor (LAUNCHPAD_COLOR_GREEN, ColorEx.fromRGB (1, 104, 1));
        this.registerColor (LAUNCHPAD_COLOR_GREEN_LO, ColorEx.fromRGB (1, 36, 1));
        this.registerColor (LAUNCHPAD_COLOR_GREEN_SPRING, ColorEx.fromRGB (52, 248, 88));
        this.registerColor (LAUNCHPAD_COLOR_SPRING_HI, DAWColors.DAW_COLOR_BLUISH_GREEN.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_SPRING, DAWColors.DAW_COLOR_GREEN.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_SPRING_LO, ColorEx.fromRGB (1, 36, 1));
        this.registerColor (LAUNCHPAD_COLOR_SPRING_TURQUOISE, ColorEx.fromRGB (51, 249, 143));
        this.registerColor (LAUNCHPAD_COLOR_TURQUOISE_LO, ColorEx.fromRGB (1, 248, 75));
        this.registerColor (LAUNCHPAD_COLOR_TURQUOISE, DAWColors.DAW_COLOR_COLD_GREEN.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_TURQUOISE_HI, ColorEx.fromRGB (1, 41, 25));
        this.registerColor (LAUNCHPAD_COLOR_TURQUOISE_CYAN, DAWColors.DAW_COLOR_GREEN_BLUE.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_CYAN_HI, ColorEx.fromRGB (1, 248, 161));
        this.registerColor (LAUNCHPAD_COLOR_CYAN, ColorEx.fromRGB (1, 105, 66));
        this.registerColor (LAUNCHPAD_COLOR_CYAN_LO, ColorEx.fromRGB (1, 36, 25));
        this.registerColor (LAUNCHPAD_COLOR_CYAN_SKY, ColorEx.fromRGB (68, 202, 255));
        this.registerColor (LAUNCHPAD_COLOR_SKY_HI, ColorEx.fromRGB (1, 182, 255));
        this.registerColor (LAUNCHPAD_COLOR_SKY, ColorEx.fromRGB (1, 82, 100));
        this.registerColor (LAUNCHPAD_COLOR_SKY_LO, ColorEx.fromRGB (1, 26, 37));
        this.registerColor (LAUNCHPAD_COLOR_SKY_OCEAN, DAWColors.DAW_COLOR_SILVER.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_OCEAN_HI, DAWColors.DAW_COLOR_LIGHT_BLUE.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_OCEAN, DAWColors.DAW_COLOR_DARK_BLUE.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_OCEAN_LO, ColorEx.fromRGB (1, 15, 38));
        this.registerColor (LAUNCHPAD_COLOR_OCEAN_BLUE, DAWColors.DAW_COLOR_PURPLE_BLUE.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_BLUE_HI, ColorEx.fromRGB (14, 54, 255));
        this.registerColor (LAUNCHPAD_COLOR_BLUE, ColorEx.fromRGB (4, 23, 110));
        this.registerColor (LAUNCHPAD_COLOR_BLUE_LO, ColorEx.fromRGB (1, 8, 38));
        this.registerColor (LAUNCHPAD_COLOR_BLUE_ORCHID, DAWColors.DAW_COLOR_LIGHT_PURPLE.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_ORCHID_HI, DAWColors.DAW_COLOR_PURPLE.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_ORCHID, ColorEx.fromRGB (35, 26, 122));
        this.registerColor (LAUNCHPAD_COLOR_ORCHID_LO, ColorEx.fromRGB (20, 14, 67));
        this.registerColor (LAUNCHPAD_COLOR_ORCHID_MAGENTA, ColorEx.fromRGB (255, 108, 255));
        this.registerColor (LAUNCHPAD_COLOR_MAGENTA_HI, ColorEx.fromRGB (255, 67, 255));
        this.registerColor (LAUNCHPAD_COLOR_MAGENTA, ColorEx.fromRGB (110, 28, 109));
        this.registerColor (LAUNCHPAD_COLOR_MAGENTA_LO, ColorEx.fromRGB (39, 9, 38));
        this.registerColor (LAUNCHPAD_COLOR_MAGENTA_PINK, DAWColors.DAW_COLOR_LIGHT_PINK.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_PINK_HI, DAWColors.DAW_COLOR_PINK.getColor ());
        this.registerColor (LAUNCHPAD_COLOR_PINK, ColorEx.fromRGB (110, 20, 40));
        this.registerColor (LAUNCHPAD_COLOR_PINK_LO, ColorEx.fromRGB (48, 9, 26));
        this.registerColor (LAUNCHPAD_COLOR_ORANGE, DAWColors.DAW_COLOR_ORANGE.getColor ());

        // Not used
        for (int i = 60; i < 256; i++)
            this.registerColor (i, ColorEx.BLACK);
    }
}