// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller;

/**
 * IDs for common continuous controls.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public enum ContinuousID
{
    /** The Mastertrack fader. */
    FADER_MASTER,

    /** The fader 1. */
    FADER1,
    /** The fader 2. */
    FADER2,
    /** The fader 3. */
    FADER3,
    /** The fader 4. */
    FADER4,
    /** The fader 5. */
    FADER5,
    /** The fader 6. */
    FADER6,
    /** The fader 7. */
    FADER7,
    /** The fader 8. */
    FADER8,

    /** Knob 1. */
    KNOB1,
    /** Knob 2. */
    KNOB2,
    /** Knob 3. */
    KNOB3,
    /** Knob 4. */
    KNOB4,
    /** Knob 5. */
    KNOB5,
    /** Knob 6. */
    KNOB6,
    /** Knob 7. */
    KNOB7,
    /** Knob 8. */
    KNOB8;


    /**
     * Get an offset control ID, e.g. to get FADER4 set FADER1 and 3 as parameters.
     *
     * @param cid The base control ID
     * @param offset The offset
     * @return The offset control ID
     */
    public static ContinuousID get (final ContinuousID cid, final int offset)
    {
        return ContinuousID.values ()[cid.ordinal () + offset];
    }
}
