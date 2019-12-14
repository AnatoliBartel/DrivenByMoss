// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller;

import de.mossgrabers.framework.configuration.Configuration;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.display.IDisplay;
import de.mossgrabers.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.controller.grid.PadGrid;
import de.mossgrabers.framework.controller.hardware.IHwAbsoluteKnob;
import de.mossgrabers.framework.controller.hardware.IHwButton;
import de.mossgrabers.framework.controller.hardware.IHwContinuousControl;
import de.mossgrabers.framework.controller.hardware.IHwFader;
import de.mossgrabers.framework.controller.hardware.IHwLight;
import de.mossgrabers.framework.controller.hardware.IHwPianoKeyboard;
import de.mossgrabers.framework.controller.hardware.IHwRelativeKnob;
import de.mossgrabers.framework.controller.valuechanger.RelativeEncoding;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.daw.midi.IMidiOutput;
import de.mossgrabers.framework.mode.ModeManager;
import de.mossgrabers.framework.view.ViewManager;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;


/**
 * Interface of a hardware control surface.
 *
 * @param <C> The type of the configuration
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public interface IControlSurface<C extends Configuration>
{
    /**
     * Get the surface ID.
     *
     * @return The surface ID
     */
    int getSurfaceID ();


    /**
     * Get the view manager.
     *
     * @return The view manager
     */
    ViewManager getViewManager ();


    /**
     * Get the mode manager.
     *
     * @return The mode manager
     */
    ModeManager getModeManager ();


    /**
     * Get the configuration settings.
     *
     * @return The configuration object
     */
    C getConfiguration ();


    /**
     * Get the default text display of the controller.
     *
     * @return The display interface
     */
    ITextDisplay getTextDisplay ();


    /**
     * Get the default text display.
     *
     * @return The default text display
     */
    IDisplay getDisplay ();


    /**
     * Get the interface to one of the text displays of the controller.
     *
     * @param index The index of the interface
     * @return The display interface
     */
    ITextDisplay getTextDisplay (int index);


    /**
     * Add a text display to the controller.
     *
     * @param display The display interface
     */
    void addTextDisplay (final ITextDisplay display);


    /**
     * Get the default graphics display of the controller.
     *
     * @return The graphics interface
     */
    IGraphicDisplay getGraphicsDisplay ();


    /**
     * Get the interface to one of the graphics displays of the controller.
     *
     * @param index The index of the interface
     * @return The display interface
     */
    IGraphicDisplay getGraphicsDisplay (int index);


    /**
     * Add a graphics display to the controller.
     *
     * @param display The display interface
     */
    void addGraphicsDisplay (final IGraphicDisplay display);


    /**
     * Get the interface to the grid of some pads if the controller does have pads.
     *
     * @return The interface to pads
     */
    PadGrid getPadGrid ();


    /**
     * Get the midi output.
     *
     * @return The output
     */
    IMidiOutput getMidiOutput ();


    /**
     * Get the midi input.
     *
     * @return The input
     */
    IMidiInput getMidiInput ();


    /**
     * Set the mapping of midi notes to the midi notes sent to the DAW.
     *
     * @param table The table has 128 items. The index is the incoming note, the value at the index
     *            the outgoing note.
     */
    void setKeyTranslationTable (int [] table);


    /**
     * Get the mapping of midi notes to the midi notes sent to the DAW.
     *
     * @return The table has 128 items. The index is the incoming note, the value at the index the
     *         outgoing note.
     */
    int [] getKeyTranslationTable ();


    /**
     * Sets the mapping of midi note velocities to the midi note velocities sent to the DAW
     *
     * @param table The table has 128 items. The index is the incoming velocity, the value at the
     *            index the outgoing velocity. E.g. if you set all values to 127 you set the
     *            velocity always to maximum
     */
    void setVelocityTranslationTable (int [] table);


    /**
     * Is the select trigger pressed (if the controller has one).
     *
     * @return The state of the select trigger
     */
    boolean isSelectPressed ();


    /**
     * Is the shift trigger pressed (if the controller has one).
     *
     * @return The state of the shift trigger
     */
    boolean isShiftPressed ();


    /**
     * Is the delete trigger pressed (if the controller has one).
     *
     * @return The state of the delete trigger
     */
    boolean isDeletePressed ();


    /**
     * Is the solo trigger pressed (if the controller has one).
     *
     * @return The state of the solo trigger
     */
    boolean isSoloPressed ();


    /**
     * Is the mute trigger pressed (if the controller has one).
     *
     * @return The state of the mute trigger
     */
    boolean isMutePressed ();


    /**
     * Test if the trigger with the given button ID is pressed.
     *
     * @param buttonID The ID of the button to test
     * @return True if pressed
     */
    boolean isPressed (ButtonID buttonID);


    /**
     * Test if the trigger with the given button ID is long pressed.
     *
     * @param buttonID The ID of the button to test
     * @return True if long pressed
     */
    boolean isLongPressed (ButtonID buttonID);


    /**
     * Sets a trigger as consumed which prevents LONG and UP events following a DOWN event for a
     * trigger.
     *
     * @param buttonID The trigger to set as consumed
     */
    void setTriggerConsumed (ButtonID buttonID);


    /**
     * Test if the consumed flag is set for a trigger.
     *
     * @param buttonID The trigger to set as consumed
     * @return The consumed flag
     */
    boolean isTriggerConsumed (ButtonID buttonID);


    /**
     * Update the lighting of a trigger (if the trigger has light), sending on the default midi
     * channel.
     *
     * @param cc The trigger
     * @param value The color / brightness depending on the controller
     */
    void setTrigger (int cc, int value);


    /**
     * Update the lighting of a trigger (if the trigger has light), sending on the default midi
     * channel.
     *
     * @param cc The trigger
     * @param colorID A registered color ID of the color / brightness depending on the controller
     */
    void setTrigger (int cc, String colorID);


    /**
     * Update the lighting of a trigger (if the trigger has light).
     *
     * @param channel The midi channel to use
     * @param cc The trigger
     * @param value The color / brightness depending on the controller
     */
    void setTrigger (int channel, int cc, int value);


    /**
     * Update the lighting of a trigger (if the trigger has light).
     *
     * @param channel The midi channel to use
     * @param cc The trigger
     * @param colorID A registered color ID of the color / brightness depending on the controller
     */
    void setTrigger (int channel, int cc, String colorID);


    /**
     * Add a piano keyboard.
     *
     * @param numKeys The number of the keys, e.g. 25 or 88
     * @param input The midi input to bind to
     */
    void addPianoKeyboard (int numKeys, IMidiInput input);


    /**
     * Get the piano keyboard, if added.
     *
     * @return The piano keyboard or null if not added
     */
    IHwPianoKeyboard getPianoKeyboard ();


    /**
     * Creates a button for the surface.
     *
     * @param buttonID The ID of the button for looking it up
     * @param label The label of the button
     * @return The created button
     */
    IHwButton createButton (ButtonID buttonID, String label);


    /**
     * Get a button the was created with the given ID.
     *
     * @param buttonID The button ID
     * @return The button or null if not created
     */
    IHwButton getButton (ButtonID buttonID);


    /**
     * Get a light the was created with the given ID.
     *
     * @param outputID The output ID
     * @return The light or null if not created
     */
    IHwLight getLight (OutputID outputID);


    /**
     * Create a proxy to a hardware light.
     *
     * @param outputID The ID of the light, may be null
     * @param supplier Callback for getting the color of the light
     * @param sendValueConsumer Callback for sending the state to the controller device
     * @return The created light
     */
    IHwLight createLight (OutputID outputID, Supplier<ColorEx> supplier, Consumer<ColorEx> sendValueConsumer);


    /**
     * Creates a light (e.g. LED) for the surface.
     *
     * @param outputID The outputID, can be null
     * @param supplier Callback for retrieving the state of the light
     * @param sendConsumer Callback for sending the update command to the controller surface
     * @param stateToColorFunction Convert the state of the light to a color, which can be displayed
     *            in the simulated GUI
     * @param button Binds the light to this button, can be null
     * @return The created light
     */
    IHwLight createLight (OutputID outputID, IntSupplier supplier, IntConsumer sendConsumer, IntFunction<ColorEx> stateToColorFunction, IHwButton button);


    /**
     * Create a fader for the surface.
     *
     * @param faderID The fader ID
     * @param label The label of the fader
     * @param isVertical True if the fader is vertical, otherwise horizontal
     * @return The created fader
     */
    IHwFader createFader (ContinuousID faderID, String label, boolean isVertical);


    /**
     * Create an absolute knob for the surface.
     *
     * @param knobID The knob ID
     * @param label The label of the knob
     * @return The created knob
     */
    IHwAbsoluteKnob createAbsoluteKnob (ContinuousID knobID, String label);


    /**
     * Create a relative knob for the surface.
     *
     * @param knobID The knob ID
     * @param label The label of the knob
     * @return The created knob
     */
    IHwRelativeKnob createRelativeKnob (ContinuousID knobID, String label);


    /**
     * Create a relative knob for the surface.
     *
     * @param knobID The knob ID
     * @param label The label of the knob
     * @param encoding The encoding of the relative value
     * @return The created knob
     */
    IHwRelativeKnob createRelativeKnob (ContinuousID knobID, String label, RelativeEncoding encoding);


    /**
     * Get a continuous control (fader or knob) that was created with the given ID.
     *
     * @param continuousID The continuous ID
     * @return The button or null if not created
     */
    IHwContinuousControl getContinuous (ContinuousID continuousID);


    /**
     * Turn off all triggers.
     */
    void turnOffTriggers ();


    /**
     * Schedule a task.
     *
     * @param callback The code to delay
     * @param delay The time in ms how long to delay the execution of the task
     */
    void scheduleTask (Runnable callback, long delay);


    /**
     * Send a midi message to the DAW (not to the midi output).
     *
     * @param status The midi status byte
     * @param data1 The midi data byte 1
     * @param data2 The midi data byte 2
     */
    void sendMidiEvent (int status, int data1, int data2);


    /**
     * Flush all displays and grids.
     */
    void flush ();


    /**
     * Overwrite for shutdown cleanups.
     */
    void shutdown ();


    /**
     * Print a message to the console.
     *
     * @param message The message to print
     */
    void println (String message);


    /**
     * Print an error message to the console.
     *
     * @param message The message to print
     */
    void errorln (String message);
}
