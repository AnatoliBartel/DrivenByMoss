// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.midi;

import de.mossgrabers.bitwig.framework.hardware.HwAbsoluteKnobImpl;
import de.mossgrabers.bitwig.framework.hardware.HwButtonImpl;
import de.mossgrabers.bitwig.framework.hardware.HwFaderImpl;
import de.mossgrabers.bitwig.framework.hardware.HwRelativeKnobImpl;
import de.mossgrabers.framework.controller.hardware.BindException;
import de.mossgrabers.framework.controller.hardware.BindType;
import de.mossgrabers.framework.controller.hardware.IHwAbsoluteKnob;
import de.mossgrabers.framework.controller.hardware.IHwButton;
import de.mossgrabers.framework.controller.hardware.IHwFader;
import de.mossgrabers.framework.controller.hardware.IHwRelativeKnob;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.daw.midi.INoteInput;
import de.mossgrabers.framework.daw.midi.MidiShortCallback;
import de.mossgrabers.framework.daw.midi.MidiSysExCallback;

import com.bitwig.extension.controller.api.AbsoluteHardwareControl;
import com.bitwig.extension.controller.api.AbsoluteHardwareValueMatcher;
import com.bitwig.extension.controller.api.ContinuousHardwareControl;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.HardwareActionMatcher;
import com.bitwig.extension.controller.api.HardwareButton;
import com.bitwig.extension.controller.api.HardwareSlider;
import com.bitwig.extension.controller.api.MidiIn;
import com.bitwig.extension.controller.api.RelativeHardwareKnob;


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


    /** {@inheritDoc} */
    @Override
    public void bind (final IHwButton button, final BindType type, final int channel, final int value)
    {
        final HardwareButton hardwareButton = ((HwButtonImpl) button).getHardwareButton ();

        if (button.getCommand () == null)
        {
            // Dynamic mapping
            final AbsoluteHardwareValueMatcher pressedMatcher;
            final HardwareActionMatcher releasedMatcher;
            switch (type)
            {
                case CC:
                    pressedMatcher = this.port.createAbsoluteCCValueMatcher (channel, value);
                    releasedMatcher = this.port.createCCActionMatcher (channel, value, 0);
                    break;

                case NOTE:
                    pressedMatcher = this.port.createNoteOnValueMatcher (channel, value);
                    releasedMatcher = this.port.createNoteOffActionMatcher (channel, value);
                    break;

                default:
                    throw new BindException (type);
            }

            hardwareButton.pressedAction ().setPressureActionMatcher (pressedMatcher);
            hardwareButton.releasedAction ().setActionMatcher (releasedMatcher);
            return;
        }

        // Static mapping

        final HardwareActionMatcher pressedMatcher;
        final HardwareActionMatcher releasedMatcher;
        switch (type)
        {
            case CC:
                pressedMatcher = this.port.createCCActionMatcher (channel, value, 127);
                releasedMatcher = this.port.createCCActionMatcher (channel, value, 0);
                break;

            case NOTE:
                pressedMatcher = this.port.createNoteOnActionMatcher (channel, value);
                releasedMatcher = this.port.createNoteOffActionMatcher (channel, value);
                break;

            default:
                throw new BindException (type);
        }

        hardwareButton.pressedAction ().setActionMatcher (pressedMatcher);
        hardwareButton.releasedAction ().setActionMatcher (releasedMatcher);
    }


    /** {@inheritDoc} */
    @Override
    public void bind (final IHwRelativeKnob knob, final BindType type, final int channel, final int value)
    {
        RelativeHardwareKnob hardwareKnob = ((HwRelativeKnobImpl) knob).getHardwareKnob ();

        // TODO Support different relative mappings, understand what these names really mean...
        switch (type)
        {
            case CC:
                hardwareKnob.setAdjustValueMatcher (this.port.createRelative2sComplementCCValueMatcher (channel, value));
                break;
            case PITCHBEND:
                break;
            default:
                throw new BindException (type);
        }
    }


    /** {@inheritDoc} */
    @Override
    public void bind (final IHwFader fader, final BindType type, final int channel, final int value)
    {
        this.bind (type, channel, value, ((HwFaderImpl) fader).getHardwareFader ());
    }


    /** {@inheritDoc} */
    @Override
    public void bind (final IHwAbsoluteKnob knob, final BindType type, final int channel, final int value)
    {
        this.bind (type, channel, value, ((HwAbsoluteKnobImpl) knob).getHardwareKnob ());
    }


    private void bind (final BindType type, final int channel, final int value, final AbsoluteHardwareControl hardwareControl)
    {
        switch (type)
        {
            case CC:
                hardwareControl.setAdjustValueMatcher (this.port.createAbsoluteCCValueMatcher (channel, value));
                break;
            case PITCHBEND:
                hardwareControl.setAdjustValueMatcher (this.port.createAbsolutePitchBendValueMatcher (channel));
                break;
            default:
                throw new BindException (type);
        }
    }


    /** {@inheritDoc} */
    @Override
    public void bindTouch (final IHwRelativeKnob relativeKnob, final BindType type, final int channel, final int control)
    {
        final RelativeHardwareKnob hardwareControl = ((HwRelativeKnobImpl) relativeKnob).getHardwareKnob ();
        bindTouch (hardwareControl, type, channel, control);
    }


    /** {@inheritDoc} */
    @Override
    public void bindTouch (final IHwFader fader, final BindType type, final int channel, final int control)
    {
        final HardwareSlider hardwareControl = ((HwFaderImpl) fader).getHardwareFader ();
        bindTouch (hardwareControl, type, channel, control);
    }


    private void bindTouch (final ContinuousHardwareControl<?> hardwareControl, final BindType type, final int channel, final int control)
    {
        final HardwareActionMatcher pressedMatcher;
        final HardwareActionMatcher releasedMatcher;
        switch (type)
        {
            case CC:
                pressedMatcher = this.port.createCCActionMatcher (channel, control, 127);
                releasedMatcher = this.port.createCCActionMatcher (channel, control, 0);
                break;
            case NOTE:
                pressedMatcher = this.port.createNoteOnActionMatcher (channel, control);
                releasedMatcher = this.port.createNoteOffActionMatcher (channel, control);
                break;
            default:
                throw new BindException (type);
        }
        hardwareControl.beginTouchAction ().setActionMatcher (pressedMatcher);
        hardwareControl.endTouchAction ().setActionMatcher (releasedMatcher);
    }
}
