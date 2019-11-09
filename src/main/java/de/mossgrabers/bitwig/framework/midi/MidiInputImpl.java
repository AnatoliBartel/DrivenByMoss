// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.midi;

import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.daw.midi.INoteInput;
import de.mossgrabers.framework.daw.midi.MidiShortCallback;
import de.mossgrabers.framework.daw.midi.MidiSysExCallback;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.MidiIn;


/**
 * A midi input.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class MidiInputImpl implements IMidiInput
{
    private MidiIn        port;
    private NoteInputImpl defaultNoteInput;


    /**
     * Constructor. Creates a default note input.
     *
     * @param portNumber The number of the midi input port
     * @param host The Bitwig host
     * @param name the name of the note input as it appears in the track input choosers in Bitwig
     *            Studio
     * @param filters a filter string formatted as hexadecimal value with `?` as wildcard. For
     *            example `80????` would match note-off on channel 1 (0). When this parameter is
     *            {@null}, a standard filter will be used to forward note-related messages on
     *            channel 1 (0).
     */
    public MidiInputImpl (final int portNumber, final ControllerHost host, final String name, final String [] filters)
    {
        this.port = host.getMidiInPort (portNumber);

        if (name != null)
            this.defaultNoteInput = new NoteInputImpl (this.port.createNoteInput (name, filters));
    }


    /** {@inheritDoc} */
    @Override
    public INoteInput createNoteInput (final String name, final String... filters)
    {
        return new NoteInputImpl (this.port.createNoteInput (name, filters));
    }


    /** {@inheritDoc} */
    @Override
    public void setMidiCallback (final MidiShortCallback callback)
    {
        this.port.setMidiCallback (callback::handleMidi);
    }


    /** {@inheritDoc} */
    @Override
    public void setSysexCallback (final MidiSysExCallback callback)
    {
        this.port.setSysexCallback (callback::handleMidi);
    }


    /** {@inheritDoc} */
    @Override
    public void sendRawMidiEvent (final int status, final int data1, final int data2)
    {
        if (this.defaultNoteInput != null)
            this.defaultNoteInput.sendRawMidiEvent (status, data1, data2);
    }


    /** {@inheritDoc} */
    @Override
    public INoteInput getDefaultNoteInput ()
    {
        return this.defaultNoteInput;
    }


    /**
     * Get the Bitwig input port.
     *
     * @return The input port
     */
    public MidiIn getPort ()
    {
        return this.port;
    }
}
