// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.daw.data;

import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.daw.data.AbstractItemImpl;
import de.mossgrabers.framework.daw.data.IMarker;
import de.mossgrabers.framework.observer.IValueObserver;

import com.bitwig.extension.controller.api.ColorValue;
import com.bitwig.extension.controller.api.CueMarker;


/**
 * Encapsulates the data of a marker.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class MarkerImpl extends AbstractItemImpl implements IMarker
{
    private final CueMarker marker;


    /**
     * Constructor.
     *
     * @param marker The marker
     * @param index The index of the marker
     */
    public MarkerImpl (final CueMarker marker, final int index)
    {
        super (index);

        this.marker = marker;

        marker.exists ().markInterested ();
        marker.getName ().markInterested ();
        marker.getColor ().markInterested ();
    }


    /** {@inheritDoc} */
    @Override
    public void enableObservers (final boolean enable)
    {
        Util.setIsSubscribed (this.marker.exists (), enable);
        Util.setIsSubscribed (this.marker.getName (), enable);
        Util.setIsSubscribed (this.marker.getColor (), enable);
    }


    /** {@inheritDoc} */
    @Override
    public boolean doesExist ()
    {
        return this.marker.exists ().get ();
    }


    /** {@inheritDoc} */
    @Override
    public String getName ()
    {
        return this.marker.getName ().get ();
    }


    /** {@inheritDoc} */
    @Override
    public String getName (final int limit)
    {
        return this.marker.getName ().getLimited (limit);
    }


    /** {@inheritDoc} */
    @Override
    public void addNameObserver (final IValueObserver<String> observer)
    {
        this.marker.getName ().addValueObserver (observer::update);
    }


    /** {@inheritDoc} */
    @Override
    public ColorEx getColor ()
    {
        final ColorValue color = this.marker.getColor ();
        return new ColorEx (color.red (), color.green (), color.blue ());
    }


    /** {@inheritDoc} */
    @Override
    public void launch (final boolean quantized)
    {
        this.marker.launch (quantized);
    }


    /** {@inheritDoc} */
    @Override
    public void removeMarker ()
    {
        // TODO API extension required - https://github.com/teotigraphix/Framework4Bitwig/issues/215
    }


    /** {@inheritDoc} */
    @Override
    public void select ()
    {
        // Markers cannot be selected but should also not crash
    }
}
