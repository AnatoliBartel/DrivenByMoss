// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2018
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.maschine.mikro.mk3.view;

import de.mossgrabers.controller.maschine.mikro.mk3.controller.MaschineMikroMk3ControlSurface;
import de.mossgrabers.framework.daw.IModel;


/**
 * The Mute view.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class MuteView extends BaseView
{
    /**
     * Constructor.
     *
     * @param surface The controller
     * @param model The model
     */
    public MuteView (final MaschineMikroMk3ControlSurface surface, final IModel model)
    {
        super ("Mute", surface, model);
    }


    /** {@inheritDoc} */
    @Override
    protected void executeFunction (final int padIndex)
    {
        this.model.getCurrentTrackBank ().getItem (padIndex).toggleMute ();
    }
}