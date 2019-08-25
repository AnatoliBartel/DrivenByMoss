// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.osc.module;

import de.mossgrabers.controller.osc.exception.IllegalParameterException;
import de.mossgrabers.controller.osc.exception.MissingCommandException;
import de.mossgrabers.framework.daw.IClip;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.IParameter;
import de.mossgrabers.framework.daw.data.ISend;
import de.mossgrabers.framework.osc.IOpenSoundControlWriter;

import java.util.LinkedList;


/**
 * Abstract implementation of an OSC module.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public abstract class AbstractModule implements IModule
{
    protected final IHost             host;
    protected final IModel            model;
    protected IOpenSoundControlWriter writer;


    /**
     * Constructor.
     *
     * @param host The host
     * @param model The model
     * @param writer The writer
     */
    public AbstractModule (final IHost host, final IModel model, final IOpenSoundControlWriter writer)
    {
        this.host = host;
        this.model = model;
        this.writer = writer;
    }


    /** {@inheritDoc} */
    @Override
    public void flush (final boolean dump)
    {
        // Intentionally empty
    }


    /**
     * Get the clip to use.
     *
     * @return The clip
     */
    protected IClip getClip ()
    {
        return this.model.getNoteClip (8, 128);
    }


    /**
     * Test for a trigger value.
     *
     * @param value The value to test
     * @return Returns true if the value is null or a number with a positive value greater 0
     */
    protected static boolean isTrigger (final Object value)
    {
        return value == null || value instanceof Number && ((Number) value).doubleValue () > 0;
    }


    /**
     * Converts the given value to an integer.
     *
     * @param value The value
     * @return The value is converted to an integer
     * @throws IllegalParameterException If the value is null or not a number
     */
    protected static int toInteger (final Object value) throws IllegalParameterException
    {
        return (int) toNumber (value);
    }


    /**
     * Converts the given value to a number.
     *
     * @param value The value
     * @param defaultValue The default value to return if value is null
     * @return If the value is null the default value is returned, otherwise the value is converted
     *         to a double
     * @throws IllegalParameterException If the value is not null and not a number
     */
    protected static double toNumber (final Object value, final double defaultValue) throws IllegalParameterException
    {
        if (value == null)
            return defaultValue;
        if (value instanceof Number)
            return ((Number) value).doubleValue ();
        throw new IllegalParameterException ("Parameter is not a Number");
    }


    /**
     * Converts the given value to a number.
     *
     * @param value The value
     * @return The value is converted to a double
     * @throws IllegalParameterException If the value is null or not a number
     */
    protected static double toNumber (final Object value) throws IllegalParameterException
    {
        if (value == null)
            throw new IllegalParameterException ("Number parameter missing");
        if (value instanceof Number)
            return ((Number) value).doubleValue ();
        throw new IllegalParameterException ("Parameter is not a Number");
    }


    /**
     * Converts the given value to a string.
     *
     * @param value The value
     * @return The value is converted to a string
     * @throws IllegalParameterException If the value is null
     */
    protected static String toString (final Object value) throws IllegalParameterException
    {
        if (value == null)
            throw new IllegalParameterException ("String parameter missing");
        return value.toString ();
    }


    /**
     * Get the next sub-command from the path and removes it from the path.
     *
     * @param path The path
     * @return The sub-command
     * @throws MissingCommandException If the path is empty
     */
    protected static String getSubCommand (final LinkedList<String> path) throws MissingCommandException
    {
        if (path.isEmpty ())
            throw new MissingCommandException ();
        return path.removeFirst ();
    }


    /**
     * Flush all data of a parameter.
     *
     * @param writer Where to send the messages to
     * @param fxAddress The start address for the effect
     * @param fxParam The parameter
     * @param dump Forces a flush if true otherwise only changed values are flushed
     */
    protected void flushParameterData (final IOpenSoundControlWriter writer, final String fxAddress, final IParameter fxParam, final boolean dump)
    {
        final boolean isSend = fxParam instanceof ISend;

        writer.sendOSC (fxAddress + "name", fxParam.getName (), dump);
        writer.sendOSC (fxAddress + (isSend ? "volumeStr" : "valueStr"), fxParam.getDisplayedValue (), dump);
        writer.sendOSC (fxAddress + (isSend ? "volume" : "value"), fxParam.getValue (), dump);
        writer.sendOSC (fxAddress + "modulatedValue", fxParam.getModulatedValue (), dump);
    }
}
