// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller.hardware;

import de.mossgrabers.framework.command.core.ContinuousCommand;
import de.mossgrabers.framework.daw.IHost;


/**
 * A control on a hardware controller.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public abstract class AbstractHwContinuousControl extends AbstractHwInputControl implements IHwContinuousControl
{
    protected ContinuousCommand command;


    /**
     * Constructor.
     *
     * @param host The host
     * @param label The label of the control
     */
    public AbstractHwContinuousControl (final IHost host, final String label)
    {
        super (host, label);
    }


    /** {@inheritDoc} */
    @Override
    public void bind (final ContinuousCommand command)
    {
        this.command = command;
    }


    /** {@inheritDoc} */
    @Override
    public ContinuousCommand getCommand ()
    {
        return this.command;
    }
}
