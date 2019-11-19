// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller.color;

import de.mossgrabers.framework.controller.ButtonID;

import java.util.HashMap;
import java.util.Map;


/**
 * Manages color indices by IDs.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class ColorManager
{
    /** ID for color when button is turned off. */
    public static final String            BUTTON_STATE_OFF = "BUTTON_STATE_OFF";
    /** ID for color when button is turned on. */
    public static final String            BUTTON_STATE_ON  = "BUTTON_STATE_ON";
    /** ID for color when button is highlighted. */
    public static final String            BUTTON_STATE_HI  = "BUTTON_STATE_HI";

    protected final Map<String, Integer>  colorIndexByKey  = new HashMap<> ();
    protected final Map<Integer, ColorEx> colorByIndex     = new HashMap<> ();


    /**
     * Registers a a color index. An exception is thrown if the color index is already registered.
     *
     * @param key The key under which to register the color index
     * @param colorIndex The color index
     */
    public void registerColorIndex (final String key, final int colorIndex)
    {
        if (this.colorIndexByKey.containsKey (key))
            throw new ColorIndexException ("Color for key " + key + " is already registered!");
        this.colorIndexByKey.put (key, Integer.valueOf (colorIndex));
    }


    /**
     * Get the color index which is registered with the given key.
     *
     * @param key The key
     * @return The color index
     */
    public int getColorIndex (final String key)
    {
        final Integer colorIndex = this.colorIndexByKey.get (key);
        if (colorIndex == null)
            throw new ColorIndexException ("Color for key " + key + " is not registered!");
        return colorIndex.intValue ();
    }


    /**
     * Registers the real RGB color which is represented by the given color index.
     *
     * @param colorIndex The color index
     * @param color The RGB color to map to the given key
     */
    public void registerColor (final int colorIndex, final ColorEx color)
    {
        this.colorByIndex.put (Integer.valueOf (colorIndex), color);
    }


    /**
     * Get the color index which is registered with the given key.
     *
     * @param colorIndex The color index
     * @param buttonID The ID of the button in case button LEDs have a different color range
     * @return The color index
     */
    public ColorEx getColor (final int colorIndex, final ButtonID buttonID)
    {
        if (colorIndex < 0)
            return ColorEx.BLACK;
        final ColorEx color = this.colorByIndex.get (Integer.valueOf (colorIndex));
        if (color == null)
            throw new ColorIndexException ("Color for index " + colorIndex + " is not registered!");
        return color;
    }
}
