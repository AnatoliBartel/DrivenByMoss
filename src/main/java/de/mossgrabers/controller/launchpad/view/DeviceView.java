// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.launchpad.view;

import de.mossgrabers.controller.launchpad.controller.LaunchpadColorManager;
import de.mossgrabers.controller.launchpad.controller.LaunchpadControlSurface;
import de.mossgrabers.controller.launchpad.definition.LaunchpadProControllerDefinition;
import de.mossgrabers.framework.daw.ICursorDevice;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.IParameterBank;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Edit remote parameters.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class DeviceView extends AbstractFaderView
{
    private ICursorDevice cursorDevice;


    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public DeviceView (final LaunchpadControlSurface surface, final IModel model)
    {
        super (surface, model);
        this.cursorDevice = this.model.getCursorDevice ();
    }


    /** {@inheritDoc} */
    @Override
    protected void delayedUpdateArrowButtons ()
    {
        // TODO
        // this.surface.setTrigger (this.surface.getTriggerId (ButtonID.SESSION),
        // LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO);
        // this.surface.setTrigger (this.surface.getTriggerId (ButtonID.NOTE),
        // LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO);
        // this.surface.setTrigger (this.surface.getTriggerId (ButtonID.DEVICE),
        // LaunchpadColors.LAUNCHPAD_COLOR_AMBER);
        if (this.surface.isPro ())
            this.surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_USER, this.model.getHost ().hasUserParameters () ? LaunchpadColorManager.LAUNCHPAD_COLOR_GREY_LO : LaunchpadColorManager.LAUNCHPAD_COLOR_BLACK);
    }


    /** {@inheritDoc} */
    @Override
    public void setupFader (final int index)
    {
        this.surface.setupFader (index, LaunchpadColorManager.DAW_INDICATOR_COLORS[index], false);
    }


    /** {@inheritDoc} */
    @Override
    public void onValueKnob (final int index, final int value)
    {
        this.cursorDevice.getParameterBank ().getItem (index).setValue (value);
    }


    /** {@inheritDoc} */
    @Override
    protected int getFaderValue (final int index)
    {
        return this.cursorDevice.getParameterBank ().getItem (index).getValue ();
    }


    /** {@inheritDoc} */
    @Override
    public void drawGrid ()
    {
        final IParameterBank parameterBank = this.cursorDevice.getParameterBank ();
        for (int i = 0; i < 8; i++)
            this.surface.setFaderValue (i, parameterBank.getItem (i).getValue ());
    }


    /** {@inheritDoc} */
    @Override
    public void onScene (final int scene, final ButtonEvent event)
    {
        if (event == ButtonEvent.DOWN && scene == 0)
            this.model.getCursorDevice ().toggleWindowOpen ();
    }

    // TODO
    // /** {@inheritDoc} */
    // @Override
    // public void updateSceneButton (final int scene)
    // {
    // this.surface.setTrigger (this.surface.getSceneTrigger (scene), scene == 0 ?
    // LaunchpadColors.LAUNCHPAD_COLOR_AMBER : LaunchpadColors.LAUNCHPAD_COLOR_BLACK);
    // }
}