// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.push.mode;

import de.mossgrabers.controller.push.PushConfiguration;
import de.mossgrabers.controller.push.controller.Push1Display;
import de.mossgrabers.controller.push.controller.PushControlSurface;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.graphics.display.DisplayModel;
import de.mossgrabers.framework.mode.AbstractMode;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.ViewManager;
import de.mossgrabers.framework.view.Views;


/**
 * Mode to select a view.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class SessionViewSelectMode extends BaseMode
{
    /** The views to choose from. */
    private static final Views []  VIEWS      =
    {
        Views.SESSION,
        Views.SESSION,
        Views.SCENE_PLAY,
        null,
        null
    };

    /** The views to choose from. */
    private static final String [] VIEW_NAMES =
    {
        "Session",
        "Flipped",
        "Scenes",
        "",
        ""
    };


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model The model
     */
    public SessionViewSelectMode (final PushControlSurface surface, final IModel model)
    {
        super ("Session View", surface, model);
    }


    /** {@inheritDoc} */
    @Override
    public void onFirstRow (final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.UP)
            return;

        final PushConfiguration configuration = this.surface.getConfiguration ();

        switch (index)
        {
            case 0:
            case 1:
                configuration.setFlipSession (index == 1);
                this.activateView (VIEWS[index]);
                break;

            case 2:
                configuration.setSceneView ();
                this.surface.getModeManager ().restoreMode ();
                break;

            case 6:
                this.surface.getModeManager ().setActiveMode (Modes.MARKERS);
                break;

            case 7:
                this.surface.getModeManager ().restoreMode ();
                configuration.toggleScenesClipMode ();
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
        final ViewManager viewManager = this.surface.getViewManager ();
        display.setBlock (1, 0, "Session view:");
        for (int i = 0; i < VIEWS.length; i++)
        {
            if (VIEWS[i] != null)
                display.setCell (3, i, (this.isSelected (viewManager, i) ? Push1Display.SELECT_ARROW : "") + VIEW_NAMES[i]);
        }
        display.setBlock (1, 3, "Session mode:");
        final boolean isOn = this.surface.getModeManager ().isActiveMode (Modes.SESSION);
        display.setCell (3, 6, "Markers");
        display.setCell (3, 7, (isOn ? Push1Display.SELECT_ARROW : "") + " Clips");
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay2 (final DisplayModel message)
    {
        final ViewManager viewManager = this.surface.getViewManager ();
        for (int i = 0; i < VIEWS.length; i++)
        {
            final boolean isMenuBottomSelected = VIEWS[i] != null && this.isSelected (viewManager, i);
            message.addOptionElement ("", "", false, i == 0 ? "Session view" : "", VIEW_NAMES[i], isMenuBottomSelected, false);
        }
        final boolean isOn = this.surface.getModeManager ().isActiveMode (Modes.SESSION);
        message.addOptionElement ("", "", false, "", "", false, false);
        message.addOptionElement ("", "", false, "Session mode", "Markers", false, false);
        message.addOptionElement ("", "", false, "", "Clips", isOn, false);
    }


    /** {@inheritDoc} */
    @Override
    public void updateFirstRow ()
    {
        final ColorManager colorManager = this.model.getColorManager ();
        final ViewManager viewManager = this.surface.getViewManager ();
        for (int i = 0; i < VIEWS.length; i++)
            this.surface.updateTrigger (20 + i, colorManager.getColor (VIEWS[i] == null ? AbstractMode.BUTTON_COLOR_OFF : this.isSelected (viewManager, i) ? AbstractMode.BUTTON_COLOR_HI : AbstractMode.BUTTON_COLOR_ON));

        this.surface.updateTrigger (25, AbstractMode.BUTTON_COLOR_OFF);
        this.surface.updateTrigger (26, AbstractMode.BUTTON_COLOR_ON);
        final boolean isOn = this.surface.getModeManager ().isActiveMode (Modes.SESSION);
        this.surface.updateTrigger (27, isOn ? AbstractMode.BUTTON_COLOR_HI : AbstractMode.BUTTON_COLOR_ON);
    }


    private void activateView (final Views viewID)
    {
        if (viewID == null)
            return;
        this.surface.getViewManager ().setActiveView (viewID);
        this.surface.getModeManager ().restoreMode ();
    }


    private boolean isSelected (final ViewManager viewManager, final int index)
    {
        final boolean activeView = viewManager.isActiveView (VIEWS[index]);
        switch (index)
        {
            case 0:
                return activeView && !this.surface.getConfiguration ().isFlipSession ();

            case 1:
                return activeView && this.surface.getConfiguration ().isFlipSession ();

            default:
                return activeView;
        }
    }
}