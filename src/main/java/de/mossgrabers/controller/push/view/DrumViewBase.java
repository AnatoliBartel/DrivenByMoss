// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.push.view;

import de.mossgrabers.controller.push.PushConfiguration;
import de.mossgrabers.controller.push.controller.PushControlSurface;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.AbstractDrumView;
import de.mossgrabers.framework.view.AbstractSequencerView;


/**
 * The base class for drum views.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public abstract class DrumViewBase extends AbstractDrumView<PushControlSurface, PushConfiguration>
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
    public DrumViewBase (final String name, final PushControlSurface surface, final IModel model, final int numSequencerLines, final int numPlayLines)
    {
        super (name, surface, model, numSequencerLines, numPlayLines);
    }


    /** {@inheritDoc} */
    @Override
    public void onScene (final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.DOWN)
            return;

        if (!this.isActive ())
            return;

        if (this.surface.isShiftPressed ())
        {
            final ITrack selectedTrack = this.model.getSelectedTrack ();
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


    /** {@inheritDoc} */
    @Override
    public String getSceneButtonColor (final int scene)
    {
        if (this.surface.isShiftPressed ())
        {
            if (scene >= 4)
                return AbstractSequencerView.COLOR_RESOLUTION_OFF;
            return this.updateLowerSceneButtons (scene);
        }

        return super.getSceneButtonColor (scene);
    }


    /**
     * Update the lower scene button LEDs.
     *
     * @param scene The lower scene 0-3
     * @return The color ID
     */
    protected String updateLowerSceneButtons (final int scene)
    {
        return AbstractSequencerView.COLOR_RESOLUTION_OFF;
    }
}
