// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller.hardware;

import de.mossgrabers.framework.daw.midi.IMidiInput;


/**
 * A input control on a controller surface.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public interface IHwInputControl extends IHwControl
{
    /**
     * Bind a midi command coming from a MIDI input to the button.
     *
     * @param input The MIDI input
     * @param type How to bind
     * @param value The MIDI CC or note to bind
     */
    void bind (IMidiInput input, BindType type, int value);


    /**
     * Bind a midi command coming from a MIDI input to the button.
     *
     * @param input The MIDI input
     * @param channel The MIDI channel
     * @param type How to bind
     * @param value The MIDI CC or note to bind
     */
    void bind (IMidiInput input, BindType type, int channel, int value);
}
