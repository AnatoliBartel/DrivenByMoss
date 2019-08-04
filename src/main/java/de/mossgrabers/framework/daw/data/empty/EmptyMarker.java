// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.daw.data.empty;

import de.mossgrabers.framework.daw.data.IMarker;


/**
 * Default data for an empty scene.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class EmptyMarker extends EmptyItem implements IMarker
{
    /** The singleton. */
    public static final IMarker INSTANCE = new EmptyMarker ();


    /**
     * Constructor.
     */
    private EmptyMarker ()
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public double [] getColor ()
    {
        return COLOR_OFF;
    }


    /** {@inheritDoc} */
    @Override
    public void launch (final boolean quantized)
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public void removeMarker ()
    {
        // Intentionally empty
    }
}
