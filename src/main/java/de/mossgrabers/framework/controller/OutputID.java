// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller;

/**
 * IDs for output controls (display, etc.).
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public enum OutputID
{
    /** The first display. */
    DISPLAY1,
    /** The second display. */
    DISPLAY2,
    /** The third display. */
    DISPLAY3,
    /** The fourth display. */
    DISPLAY4;


    /**
     * Get an offset output ID, e.g. to get DISPLAY3 set DISPLAY1 and 2 as parameters.
     *
     * @param oid The base output ID
     * @param offset The offset
     * @return The offset button
     */
    public static OutputID get (final OutputID oid, final int offset)
    {
        return OutputID.values ()[oid.ordinal () + offset];
    }
}
