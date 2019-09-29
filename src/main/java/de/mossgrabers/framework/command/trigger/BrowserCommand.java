// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.command.trigger;

import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.configuration.Configuration;
import de.mossgrabers.framework.controller.IControlSurface;
import de.mossgrabers.framework.daw.IBrowser;
import de.mossgrabers.framework.daw.ICursorDevice;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.mode.BrowserActivator;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Abstract command to open the browser.
 *
 * @param <S> The type of the control surface
 * @param <C> The type of the configuration
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class BrowserCommand<S extends IControlSurface<C>, C extends Configuration> extends AbstractTriggerCommand<S, C>
{
    private final BrowserActivator<S, C> browserModeActivator;


    /**
     * Constructor.
     *
     * @param browserMode The ID of the mode to activate for browsing
     * @param model The model
     * @param surface The surface
     */
    public BrowserCommand (final Modes browserMode, final IModel model, final S surface)
    {
        super (model, surface);

        this.browserModeActivator = new BrowserActivator<> (browserMode, model, surface);
    }


    /** {@inheritDoc} */
    @Override
    public void executeNormal (final ButtonEvent event)
    {
        if (event == ButtonEvent.UP)
            this.startBrowser (this.surface.isSelectPressed (), false);
    }


    /** {@inheritDoc} */
    @Override
    public void executeShifted (final ButtonEvent event)
    {
        if (event == ButtonEvent.UP)
            this.startBrowser (true, true);
    }


    /**
     * Start a browser.
     *
     * @param insertDevice Insert a device if true otherwise select preset
     * @param beforeCurrent Insert the device before the current device if any
     */
    public void startBrowser (final boolean insertDevice, final boolean beforeCurrent)
    {
        final IBrowser browser = this.model.getBrowser ();

        // Patch Browser already active?
        if (browser.isActive ())
        {
            this.discardBrowser (this.getCommit ());
            return;
        }

        final ICursorDevice cursorDevice = this.model.getCursorDevice ();
        if (!insertDevice && cursorDevice.doesExist ())
            browser.replace (cursorDevice);
        else
        {
            if (beforeCurrent)
                browser.insertBefore (cursorDevice);
            else
                browser.insertAfter (cursorDevice);
        }

        this.browserModeActivator.activate ();
    }


    /**
     * Stop browsing and restore the previous mode.
     *
     * @param commit True to commit otherwise cancel
     */
    protected void discardBrowser (final boolean commit)
    {
        this.model.getBrowser ().stopBrowsing (commit);
        this.surface.getModeManager ().restoreMode ();
    }


    /**
     * Commit or cancel browsing? Default implementation cancels if combined with Shift.
     *
     * @return True to commit otherwise cancel
     */
    protected boolean getCommit ()
    {
        return !this.surface.isShiftPressed ();
    }
}
