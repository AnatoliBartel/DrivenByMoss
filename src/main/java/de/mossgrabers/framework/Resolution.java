// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework;

/**
 * Resolutions for grid, beat repeat, etc..
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public enum Resolution
{
    /** 1/4 */
    RES_1_4("1/4", 1),
    /** 1/4t */
    RES_1_4T("1/4t", 2.0 / 3.0),
    /** 1/8 */
    RES_1_8("1/8", 1.0 / 2.0),
    /** 1/8t */
    RES_1_8T("1/8t", 1.0 / 3.0),
    /** 1/16 */
    RES_1_16("1/16", 1.0 / 4.0),
    /** 1/16t */
    RES_1_16T("1/16t", 1.0 / 6.0),
    /** 1/32 */
    RES_1_32("1/32", 1.0 / 8.0),
    /** 1/32t */
    RES_1_32T("1/32t", 1.0 / 12.0);

    private static final String [] RESOLUTION_NAMES;
    static
    {
        final Resolution [] values = values ();
        RESOLUTION_NAMES = new String [values.length];
        for (int i = 0; i < values.length; i++)
            RESOLUTION_NAMES[i] = values[i].name;
    }

    private final String name;
    private final double value;


    /**
     * Constructor.
     *
     * @param name The name of the resolution
     * @param value The value
     */
    Resolution (final String name, final double value)
    {
        this.name = name;
        this.value = value;
    }


    /**
     * Get the name of the resolution.
     *
     * @return The name
     */
    public String getName ()
    {
        return this.name;
    }


    /**
     * Get the value of the resolution.
     *
     * @return The value
     */
    public double getValue ()
    {
        return this.value;
    }


    /**
     * Get the value of the resolution.
     * 
     * @param index The index of the resolution enum
     * @return The value
     */
    public static double getValueAt (final int index)
    {
        return values ()[index].getValue ();
    }


    /**
     * Get the name of the resolution.
     * 
     * @param index The index of the resolution enum
     * @return The name
     */
    public static String getNameAt (final int index)
    {
        return values ()[index].getName ();
    }


    /**
     * Test if the given value matches the value at the given index.
     *
     * @param index The index
     * @param value A resolution value
     * @return True if the difference is less than 0.001
     */
    public static boolean matches (final int index, final double value)
    {
        return Math.abs (value - getValueAt (index)) < 0.001;
    }


    /**
     * Get the resolution which matches the given value (the difference is less than 0.001).
     *
     * @param value A resolution value
     * @return The index
     */
    public static int getMatch (final double value)
    {
        final Resolution [] resolutions = values ();
        for (int i = 0; i < resolutions.length; i++)
        {
            if (Math.abs (value - getValueAt (i)) < 0.001)
                return i;
        }
        return 0;
    }


    /**
     * Increase or descrease the index of a resolution and keeps it in the bounds of the existing
     * resolution range.
     *
     * @param index An index of a resolution
     * @param inc Increase if true otherwise descrease
     * @return The index of the previous or next resolution
     */
    public static int change (final int index, final boolean inc)
    {
        return Math.max (0, Math.min (values ().length - 1, index + (inc ? 1 : -1)));
    }


    /**
     * Get the names of all resolutions.
     *
     * @return The names of all resolutions
     */
    public static String [] getNames ()
    {
        return RESOLUTION_NAMES;
    }
}
