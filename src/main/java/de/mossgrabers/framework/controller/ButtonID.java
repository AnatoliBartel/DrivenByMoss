// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller;

/**
 * IDs for common buttons.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public enum ButtonID
{
    /** Shift button. */
    SHIFT,
    /** Select button. */
    SELECT,

    /** Delete button. */
    DELETE,
    /** Duplicate button. */
    DUPLICATE,

    /** Solo button. */
    SOLO,
    /** Mute button. */
    MUTE,

    /** Left button. */
    LEFT,
    /** Right button. */
    RIGHT,
    /** Up button. */
    UP,
    /** Down button. */
    DOWN,

    /** Session button. */
    SESSION,
    /** Device button. */
    DEVICE,
    /** User button. */
    USER,
    /** Note button. */
    NOTE,

    /** Transport play button. */
    PLAY,
    /** Transport record button. */
    RECORD,

    /** New clip button. */
    NEW,
    /** Fixed length button. */
    FIXED_LENGTH,
    /** Automation button. */
    AUTOMATION,
    /** Quantize button. */
    QUANTIZE,
    /** Double button. */
    DOUBLE,
    /** Undo button. */
    UNDO,
    /** Browse button. */
    BROWSE,
    /** Track button. */
    TRACK,
    /** Volume button. */
    VOLUME,
    /** Pan/Send button. */
    PAN_SEND,
    /** Clip button. */
    CLIP,

    /** The command to execute scene 1. */
    SCENE1,
    /** The command to execute scene 2. */
    SCENE2,
    /** The command to execute scene 3. */
    SCENE3,
    /** The command to execute scene 4. */
    SCENE4,
    /** The command to execute scene 5. */
    SCENE5,
    /** The command to execute scene 6. */
    SCENE6,
    /** The command to execute scene 7. */
    SCENE7,
    /** The command to execute scene 8. */
    SCENE8,

    /** Button 1 of row 1 command. */
    ROW1_1,
    /** Button 2 of row 1 command. */
    ROW1_2,
    /** Button 3 of row 1 command. */
    ROW1_3,
    /** Button 4 of row 1 command. */
    ROW1_4,
    /** Button 5 of row 1 command. */
    ROW1_5,
    /** Button 6 of row 1 command. */
    ROW1_6,
    /** Button 7 of row 1 command. */
    ROW1_7,
    /** Button 8 of row 1 command. */
    ROW1_8,

    /** Button 1 of row 2 command. */
    ROW2_1,
    /** Button 2 of row 2 command. */
    ROW2_2,
    /** Button 3 of row 2 command. */
    ROW2_3,
    /** Button 4 of row 2 command. */
    ROW2_4,
    /** Button 5 of row 2 command. */
    ROW2_5,
    /** Button 6 of row 2 command. */
    ROW2_6,
    /** Button 7 of row 2 command. */
    ROW2_7,
    /** Button 8 of row 2 command. */
    ROW2_8,

    /** Button 1 of row 3 command. */
    ROW3_1,
    /** Button 2 of row 3 command. */
    ROW3_2,
    /** Button 3 of row 3 command. */
    ROW3_3,
    /** Button 4 of row 3 command. */
    ROW3_4,
    /** Button 5 of row 3 command. */
    ROW3_5,
    /** Button 6 of row 3 command. */
    ROW3_6,
    /** Button 7 of row 3 command. */
    ROW3_7,
    /** Button 8 of row 3 command. */
    ROW3_8,

    /** Button 1 of row 4 command. */
    ROW4_1,
    /** Button 2 of row 4 command. */
    ROW4_2,
    /** Button 3 of row 4 command. */
    ROW4_3,
    /** Button 4 of row 4 command. */
    ROW4_4,
    /** Button 5 of row 4 command. */
    ROW4_5,
    /** Button 6 of row 4 command. */
    ROW4_6,
    /** Button 7 of row 4 command. */
    ROW4_7,
    /** Button 8 of row 4 command. */
    ROW4_8,

    /** Button 1 of row 5 command. */
    ROW5_1,
    /** Button 2 of row 5 command. */
    ROW5_2,
    /** Button 3 of row 5 command. */
    ROW5_3,
    /** Button 4 of row 5 command. */
    ROW5_4,
    /** Button 5 of row 5 command. */
    ROW5_5,
    /** Button 6 of row 5 command. */
    ROW5_6,
    /** Button 7 of row 5 command. */
    ROW5_7,
    /** Button 8 of row 5 command. */
    ROW5_8,

    /** Button 1 of row 6 command. */
    ROW6_1,
    /** Button 2 of row 6 command. */
    ROW6_2,
    /** Button 3 of row 6 command. */
    ROW6_3,
    /** Button 4 of row 6 command. */
    ROW6_4,
    /** Button 5 of row 6 command. */
    ROW6_5,
    /** Button 6 of row 6 command. */
    ROW6_6,
    /** Button 7 of row 6 command. */
    ROW6_7,
    /** Button 8 of row 6 command. */
    ROW6_8,

    /** Button select row 1 command. */
    ROW_SELECT_1,
    /** Button select row 2 command. */
    ROW_SELECT_2,
    /** Button select row 3 command. */
    ROW_SELECT_3,
    /** Button select row 4 command. */
    ROW_SELECT_4,
    /** Button select row 5 command. */
    ROW_SELECT_5,
    /** Button select row 6 command. */
    ROW_SELECT_6,
    /** Button select row 7 command. */
    ROW_SELECT_7,
    /** Button select row 8 command. */
    ROW_SELECT_8;


    /**
     * Get an offset button ID, e.g. to get F4 set F1 and 3 as parameters.
     * 
     * @param bid The base button ID
     * @param offset The offset
     * @return The offset command
     */
    public static ButtonID get (final ButtonID bid, final int offset)
    {
        return ButtonID.values ()[bid.ordinal () + offset];
    }
}
