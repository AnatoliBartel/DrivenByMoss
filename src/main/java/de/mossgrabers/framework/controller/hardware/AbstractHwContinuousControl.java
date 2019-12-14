// Written by Jürgen Moßgraber - mossgrabers.de
// protected c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller.hardware;

import de.mossgrabers.framework.command.core.ContinuousCommand;
import de.mossgrabers.framework.command.core.PitchbendCommand;
import de.mossgrabers.framework.command.core.TriggerCommand;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * A control on a hardware controller.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public abstract class AbstractHwContinuousControl extends AbstractHwInputControl implements IHwContinuousControl
{
    protected ContinuousCommand command;
    protected TriggerCommand    touchCommand;
    protected PitchbendCommand  pitchbendCommand;
    protected boolean           isTouched;


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
    public void bind (final PitchbendCommand command)
    {
        this.pitchbendCommand = command;
    }


    /** {@inheritDoc} */
    @Override
    public ContinuousCommand getCommand ()
    {
        return this.command;
    }


    /** {@inheritDoc} */
    @Override
    public TriggerCommand getTouchCommand ()
    {
        return this.touchCommand;
    }


    /** {@inheritDoc} */
    @Override
    public PitchbendCommand getPitchbendCommand ()
    {
        return this.pitchbendCommand;
    }


    /** {@inheritDoc} */
    @Override
    public void triggerTouch (final boolean isDown)
    {
        if (this.touchCommand == null)
            return;
        this.touchCommand.execute (isDown ? ButtonEvent.DOWN : ButtonEvent.UP, isDown ? 127 : 0);
        this.isTouched = isDown;
    }


    /** {@inheritDoc} */
    @Override
    public boolean isTouched ()
    {
        return this.isTouched;
    }
}
