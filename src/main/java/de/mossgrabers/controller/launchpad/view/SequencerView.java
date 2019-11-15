// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.launchpad.view;

import de.mossgrabers.controller.launchpad.LaunchpadConfiguration;
import de.mossgrabers.controller.launchpad.controller.LaunchpadColors;
import de.mossgrabers.controller.launchpad.controller.LaunchpadControlSurface;
import de.mossgrabers.controller.launchpad.definition.LaunchpadProControllerDefinition;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.view.AbstractNoteSequencerView;


/**
 * The sequencer view.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class SequencerView extends AbstractNoteSequencerView<LaunchpadControlSurface, LaunchpadConfiguration>
{
    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public SequencerView (final LaunchpadControlSurface surface, final IModel model)
    {
        super ("Sequencer", surface, model, true);
    }


    /** {@inheritDoc} */
    @Override
    public void onActivate ()
    {
        super.onActivate ();
        this.surface.setLaunchpadToPrgMode ();
        this.surface.scheduleTask (this::delayedUpdateArrowButtons, 150);
    }


    private void delayedUpdateArrowButtons ()
    {
        // TODO
        // this.surface.setTrigger (this.surface.getTriggerId (ButtonID.SESSION),
        // LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO);
        // this.surface.setTrigger (this.surface.getTriggerId (ButtonID.NOTE),
        // LaunchpadColors.LAUNCHPAD_COLOR_BLUE);
        // this.surface.setTrigger (this.surface.getTriggerId (ButtonID.DEVICE),
        // LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO);
        if (this.surface.isPro ())
            this.surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_USER, this.model.getHost ().hasUserParameters () ? LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO : LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
    }

    // TODO
    // /** {@inheritDoc} */
    // @Override
    // public void updateSceneButton (final int scene)
    // {
    // final boolean isActive = this.isActive ();
    // final int color = scene == 7 - this.selectedResolutionIndex ?
    // LaunchpadColors.LAUNCHPAD_COLOR_YELLOW : LaunchpadColors.LAUNCHPAD_COLOR_GREEN;
    // this.surface.setTrigger (this.surface.getSceneTrigger (scene), isActive ? color :
    // LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
    // }
}
