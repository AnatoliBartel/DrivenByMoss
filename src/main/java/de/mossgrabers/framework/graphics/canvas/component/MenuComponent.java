// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.graphics.canvas.component;

import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.graphics.IGraphicsInfo;
import de.mossgrabers.framework.graphics.canvas.component.LabelComponent.LabelLayout;


/**
 * A component which contains a menu and a channels' icon, name and color.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class MenuComponent implements IComponent
{
    protected final LabelComponent header;
    protected final LabelComponent footer;


    /**
     * Constructor.
     *
     * @param menuName The text for the menu
     * @param isMenuSelected True if the menu is selected
     * @param name The of the grid element (track name, parameter name, etc.)
     * @param icon The icon to use
     * @param color The color to use for the header, may be null
     * @param isSelected True if the grid element is selected
     * @param isActive True if channel is activated
     */
    public MenuComponent (final String menuName, final boolean isMenuSelected, final String name, final String icon, final ColorEx color, final boolean isSelected, final boolean isActive)
    {
        this (menuName, isMenuSelected, name, icon, color, isSelected, isActive, LabelLayout.SEPARATE_COLOR);
    }


    /**
     * Constructor.
     *
     * @param menuName The text for the menu
     * @param isMenuSelected True if the menu is selected
     * @param name The of the grid element (track name, parameter name, etc.)
     * @param icon The icon to use
     * @param color The color to use for the header, may be null
     * @param isSelected True if the grid element is selected
     * @param isActive True if channel is activated
     * @param lowerLayout THe layout for the lower label
     */
    public MenuComponent (final String menuName, final boolean isMenuSelected, final String name, final String icon, final ColorEx color, final boolean isSelected, final boolean isActive, final LabelLayout lowerLayout)
    {
        this.header = new LabelComponent (menuName, null, null, isMenuSelected, true, LabelLayout.SMALL_HEADER);
        this.footer = new LabelComponent (name, icon, color, isSelected, isActive, lowerLayout);
    }


    /** {@inheritDoc} */
    @Override
    public void draw (final IGraphicsInfo info)
    {
        this.header.draw (info);

        final String name = this.footer.getText ();
        // Element is off if the name is empty
        if (name == null || name.length () == 0)
            return;

        final double menuHeight = 2 * info.getDimensions ().getMenuHeight ();
        this.footer.draw (info.withBounds (info.getBounds ().getHeight () - menuHeight, menuHeight));
    }
}
