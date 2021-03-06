// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.push.mode;

import de.mossgrabers.controller.push.controller.Push1Display;
import de.mossgrabers.controller.push.controller.PushControlSurface;
import de.mossgrabers.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.daw.IMarkerBank;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.IMarker;
import de.mossgrabers.framework.mode.AbstractMode;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.utils.StringUtils;


/**
 * Editing of accent parameters.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class MarkersMode extends BaseMode
{
    private static final String [] EDIT_MENU        =
    {
        "Add Marker",
        "",
        "",
        "",
        "",
        "",
        "Select",
        "Launch",
    };

    private boolean                actionModeLaunch = true;


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model The model
     */
    public MarkersMode (final PushControlSurface surface, final IModel model)
    {
        super ("Marker", surface, model);
    }


    /** {@inheritDoc} */
    @Override
    public void onFirstRow (final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.UP)
            return;

        final IMarker marker = this.model.getMarkerBank ().getItem (index);
        if (!marker.doesExist ())
            return;

        if (this.actionModeLaunch)
            marker.launch (true);
        else
            marker.select ();
    }


    /** {@inheritDoc} */
    @Override
    public void onSecondRow (final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.UP)
            return;

        final boolean canEditMarkers = this.model.getHost ().canEditMarkers ();

        switch (index)
        {
            case 0:
                this.model.getMarkerBank ().addMarker ();
                break;
            case 6:
                if (canEditMarkers)
                    this.actionModeLaunch = false;
                break;
            case 7:
                if (canEditMarkers)
                    this.actionModeLaunch = true;
                break;
            default:
                // Not used
                break;
        }
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay1 (final ITextDisplay display)
    {
        final boolean canEditMarkers = this.model.getHost ().canEditMarkers ();
        final IMarkerBank markerBank = this.model.getMarkerBank ();

        display.setCell (2, 0, "Markers:");

        for (int i = 0; i < 8; i++)
        {
            if (canEditMarkers)
            {
                final boolean isMenuTopSelected = i == 6 && !this.actionModeLaunch || i == 7 && this.actionModeLaunch;
                display.setCell (0, i, (isMenuTopSelected ? Push1Display.SELECT_ARROW : "") + EDIT_MENU[i]);
            }

            final IMarker marker = markerBank.getItem (i);
            if (marker.doesExist ())
                display.setCell (3, i, StringUtils.shortenAndFixASCII (marker.getName (), 8));
        }

        if (canEditMarkers)
            display.setCell (0, 5, "Action:");
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay2 (final IGraphicDisplay display)
    {
        final boolean canEditMarkers = this.model.getHost ().canEditMarkers ();
        final IMarkerBank markerBank = this.model.getMarkerBank ();
        for (int i = 0; i < 8; i++)
        {
            final IMarker marker = markerBank.getItem (i);
            final String menuTopName = canEditMarkers ? EDIT_MENU[i] : "";
            final String headerBottomName = i == 0 ? "Markers" : "";
            final String headerTopName = canEditMarkers && i == 6 ? "Action" : "";
            final boolean isMenuTopSelected = i == 6 && !this.actionModeLaunch || i == 7 && this.actionModeLaunch;
            display.addOptionElement (headerTopName, menuTopName, isMenuTopSelected, null, headerBottomName, marker.doesExist () ? marker.getName (12) : "", false, marker.getColor (), false);
        }
    }


    /** {@inheritDoc} */
    @Override
    public void updateFirstRow ()
    {
        final IMarkerBank markerBank = this.model.getMarkerBank ();
        for (int i = 0; i < 8; i++)
            this.surface.updateTrigger (20 + i, markerBank.getItem (i).doesExist () ? AbstractMode.BUTTON_COLOR_ON : AbstractMode.BUTTON_COLOR_OFF);
    }


    /** {@inheritDoc} */
    @Override
    public void updateSecondRow ()
    {
        final boolean canEditMarkers = this.model.getHost ().canEditMarkers ();
        for (int i = 0; i < 8; i++)
            this.surface.updateTrigger (102 + i, canEditMarkers && !EDIT_MENU[i].isEmpty () ? AbstractMode.BUTTON_COLOR2_ON : AbstractMode.BUTTON_COLOR_OFF);
    }


    /** {@inheritDoc} */
    @Override
    protected IMarkerBank getBank ()
    {
        return this.model.getMarkerBank ();
    }
}
