// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller.grid;

/**
 * Info for pad updates.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class PadInfo
{
    private int     color      = -1;
    private int     blinkColor = -1;
    private boolean fast       = false;

    private int     encoded    = -1;


    /**
     * Get the color of the pad.
     *
     * @return The color
     */
    public int getColor ()
    {
        return this.color;
    }


    /**
     * Set all pad info values at once.
     *
     * @param color The color
     * @param blinkColor The new blink color
     * @param fast True to blink fast
     */
    public void setColors (final int color, final int blinkColor, final boolean fast)
    {
        this.color = color;
        this.blinkColor = blinkColor;
        this.fast = fast;

        this.encode ();
    }


    /**
     * Set the color of the pad.
     *
     * @param color The color
     */
    public void setColor (final int color)
    {
        this.color = color;
        this.encode ();
    }


    /**
     * Get the blink color.
     *
     * @return The blink color
     */
    public int getBlinkColor ()
    {
        return this.blinkColor;
    }


    /**
     * Set the blink color.
     *
     * @param blinkColor The new blink color
     */
    public void setBlinkColor (final int blinkColor)
    {
        this.blinkColor = blinkColor;
        this.encode ();
    }


    /**
     * Blink fast or slow?
     *
     * @return True if fast
     */
    public boolean isFast ()
    {
        return this.fast;
    }


    /**
     * Set to blink fast or slow.
     *
     * @param fast True to blink fast
     */
    public void setFast (final boolean fast)
    {
        this.fast = fast;
        this.encode ();
    }


    /**
     * Get the encoded state.
     *
     * @return The encoded state
     */
    public int getEncoded ()
    {
        return this.encoded;
    }


    /**
     * Encode the color and blink states as one integer and store it in the encode field.
     */
    private void encode ()
    {
        int codeBlinkColor = this.blinkColor << 8;
        int codeFast = this.fast ? 1 << 16 : 0;
        this.encoded = codeFast + codeBlinkColor + this.color;
    }
}