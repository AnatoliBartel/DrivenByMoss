// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.push.command.trigger;

import de.mossgrabers.controller.push.PushConfiguration;
import de.mossgrabers.controller.push.controller.PushControlSurface;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.AbstractSequencerView;
import de.mossgrabers.framework.view.View;
import de.mossgrabers.framework.view.ViewManager;
import de.mossgrabers.framework.view.Views;


/**
 * Command to dive out the layer / drum pads.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class PageRightCommand extends AbstractTriggerCommand<PushControlSurface, PushConfiguration>
{
    /**
     * Constructor.
     *
     * @param model The model
     * @param surface The surface
     */
    public PageRightCommand (final IModel model, final PushControlSurface surface)
    {
        super (model, surface);
    }


    /** {@inheritDoc} */
    @SuppressWarnings("rawtypes")
    @Override
    public void execute (final ButtonEvent event)
    {
        final ViewManager viewManager = this.surface.getViewManager ();
        if (viewManager.isActiveView (Views.SESSION))
        {
            if (event == ButtonEvent.DOWN)
                this.model.getCurrentTrackBank ().selectNextPage ();
            return;
        }

        final View activeView = viewManager.getActiveView ();
        if (activeView instanceof AbstractSequencerView)
            ((AbstractSequencerView) activeView).onRight (event);
    }
}
