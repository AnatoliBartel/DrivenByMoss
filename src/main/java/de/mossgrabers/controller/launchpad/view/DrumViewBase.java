// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.launchpad.view;

import de.mossgrabers.controller.launchpad.LaunchpadConfiguration;
import de.mossgrabers.controller.launchpad.controller.LaunchpadControlSurface;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.ITrackBank;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.AbstractDrumView;


/**
 * The base class for drum views.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public abstract class DrumViewBase extends AbstractDrumView<LaunchpadControlSurface, LaunchpadConfiguration>
{
    protected int soundOffset;


    /**
     * Constructor.
     *
     * @param name The name of the view
     * @param surface The surface
     * @param model The model
     * @param numSequencerLines The number of rows to use for the sequencer
     * @param numPlayLines The number of rows to use for playing
     */
    public DrumViewBase (final String name, final LaunchpadControlSurface surface, final IModel model, final int numSequencerLines, final int numPlayLines)
    {
        super (name, surface, model, numSequencerLines, numPlayLines);
    }


    /** {@inheritDoc} */
    @Override
    public void onScene (final int index, final ButtonEvent event)
    {
        if (this.surface.isShiftPressed ())
        {
            if (event != ButtonEvent.DOWN || !this.isActive ())
                return;

            final ITrackBank tb = this.model.getCurrentTrackBank ();
            final ITrack selectedTrack = tb.getSelectedItem ();
            if (selectedTrack != null)
                this.onLowerScene (index);
            return;
        }

        super.onScene (index, event);
    }


    /**
     * Handle the functionality in sub-classes.
     *
     * @param index The scene index
     */
    protected void onLowerScene (final int index)
    {
        // Intentionally empty
    }

    // /** {@inheritDoc} */
    // @Override
    // public void updateSceneButton (final int scene)
    // {
    // TODO
    //
    // if (this.surface.isShiftPressed ())
    // {
    // for (int i = 0; i < 4; i++)
    // this.surface.setTrigger (this.surface.getSceneTrigger (i),
    // LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
    // this.updateLowerSceneButtons ();
    // return;
    // }
    //
    // final boolean isActive = this.isActive ();
    // for (int i = 0; i < 8; i++)
    // {
    // final int sceneButton = this.surface.getSceneTrigger (i);
    // final int color = i == 7 - this.selectedResolutionIndex ?
    // LaunchpadColors.LAUNCHPAD_COLOR_YELLOW : LaunchpadColors.LAUNCHPAD_COLOR_GREEN;
    // this.surface.setTrigger (sceneButton, isActive ? color :
    // LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
    // }
    // }


    /**
     * Update the lower scene button LEDs.
     */
    protected void updateLowerSceneButtons ()
    {
        // TODO
        // for (int i = 4; i < 8; i++)
        // this.surface.setTrigger (this.surface.getSceneTrigger (i),
        // LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
    }
}
