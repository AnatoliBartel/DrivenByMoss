// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.hardware;

import com.bitwig.extension.controller.api.HardwareLightVisualState;
import com.bitwig.extension.controller.api.InternalHardwareLightState;

import java.util.function.Supplier;


/**
 * Support class to make an interface happy..
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class DefaultInternalHardwareLightState extends InternalHardwareLightState
{
    private Supplier<HardwareLightVisualState> supplier;


    /**
     * Constructor.
     *
     * @param supplier A supplier for the visual LED state
     */
    public DefaultInternalHardwareLightState (final Supplier<HardwareLightVisualState> supplier)
    {
        this.supplier = supplier;
    }


    /** {@inheritDoc}} */
    @Override
    public HardwareLightVisualState getVisualState ()
    {
        return this.supplier.get ();
    }


    /** {@inheritDoc}} */
    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.supplier == null) ? 0 : this.supplier.hashCode ());
        return result;
    }


    /** {@inheritDoc}} */
    @Override
    public boolean equals (Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass () != obj.getClass ())
            return false;
        DefaultInternalHardwareLightState other = (DefaultInternalHardwareLightState) obj;
        if (this.supplier == null)
        {
            if (other.supplier != null)
                return false;
        }
        else if (!this.supplier.equals (other.supplier))
            return false;
        return true;
    }
}
