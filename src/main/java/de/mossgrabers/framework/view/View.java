// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.view;

import de.mossgrabers.framework.command.ContinuousCommandID;
import de.mossgrabers.framework.command.core.AftertouchCommand;
import de.mossgrabers.framework.command.core.ContinuousCommand;
import de.mossgrabers.framework.command.core.PitchbendCommand;


/**
 * Interface to a view. A view contains a grid of pads and a number of buttons to which commands can
 * be assigned.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public interface View
{
    /**
     * Get the name of the view.
     *
     * @return The name of the view
     */
    String getName ();


    /**
     * Called when the view should be activated.
     */
    void onActivate ();


    /**
     * Called when the view should be deactivated.
     */
    void onDeactivate ();


    /**
     * Registers a continuous command for this view.
     *
     * @param commandID The ID of the command
     * @param command The command
     */
    void registerContinuousCommand (ContinuousCommandID commandID, ContinuousCommand command);


    /**
     * Get a registered continuous command.
     *
     * @param commandID The ID of the command.
     * @return The command or null
     */
    ContinuousCommand getContinuousCommand (ContinuousCommandID commandID);


    /**
     * Execute a continuous command which has been registered before.
     *
     * @param commandID The ID of the command
     * @param value The updated value
     */
    void executeContinuousCommand (ContinuousCommandID commandID, int value);


    /**
     * Registers the aftertouch command.
     *
     * @param command The command
     */
    void registerAftertouchCommand (AftertouchCommand command);


    /**
     * Execute the aftertouch command which has been registered before.
     *
     * @param note The note on which aftertouch is applied. Set to -1 for channel aftertouch
     * @param value The updated value
     */
    void executeAftertouchCommand (int note, int value);


    /**
     * Registers the pitchbend command.
     *
     * @param command The command
     */
    void registerPitchbendCommand (PitchbendCommand command);


    /**
     * Execute the pitchbend command which has been registered before.
     *
     * @param channel The midi channel
     * @param data1 The first pitchbend byte
     * @param data2 The second pitchbend byte
     */
    void executePitchbendCommand (int channel, int data1, int data2);


    /**
     * Get the registered pitchbend command.
     *
     * @return The command or null if not registered
     */
    PitchbendCommand getPitchbendCommand ();


    /**
     * Draw the pad grid.
     */
    void drawGrid ();


    /**
     * A pad has been pressed or released.
     *
     * @param note The note of the pad
     * @param velocity The velocity of the press
     */
    void onGridNote (int note, int velocity);


    /**
     * Hook to update all button LEDs, displays, etc.
     */
    void updateControlSurface ();


    /**
     * Long press actions on grid pads
     *
     * @param note The long pressed note
     */
    void onGridNoteLongPress (int note);


    /**
     * Update the note mapping of the grid pads.
     */
    void updateNoteMapping ();


    /**
     * Selects a track in the current page of the current track bank and makes the track visible in
     * the DAW.
     *
     * @param index The index of the track in the page
     */
    void selectTrack (int index);
}
