// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller;

import de.mossgrabers.framework.configuration.Configuration;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.display.DummyDisplay;
import de.mossgrabers.framework.controller.display.IDisplay;
import de.mossgrabers.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.controller.grid.PadGrid;
import de.mossgrabers.framework.controller.hardware.IHwAbsoluteKnob;
import de.mossgrabers.framework.controller.hardware.IHwButton;
import de.mossgrabers.framework.controller.hardware.IHwContinuousControl;
import de.mossgrabers.framework.controller.hardware.IHwFader;
import de.mossgrabers.framework.controller.hardware.IHwLight;
import de.mossgrabers.framework.controller.hardware.IHwRelativeKnob;
import de.mossgrabers.framework.controller.hardware.IHwSurfaceFactory;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.daw.midi.IMidiOutput;
import de.mossgrabers.framework.daw.midi.INoteInput;
import de.mossgrabers.framework.mode.ModeManager;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.utils.ContinuousInfo;
import de.mossgrabers.framework.utils.LatestTaskExecutor;
import de.mossgrabers.framework.view.View;
import de.mossgrabers.framework.view.ViewManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;


/**
 * Abstract implementation of a Control Surface.
 *
 * @param <C> The type of the configuration
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public abstract class AbstractControlSurface<C extends Configuration> implements IControlSurface<C>
{
    protected static final int                      BUTTON_STATE_INTERVAL = 400;
    protected static final int                      NUM_NOTES             = 128;
    protected static final int                      NUM_INFOS             = 256;

    protected final IHost                           host;
    protected final C                               configuration;
    protected final ColorManager                    colorManager;
    protected final IMidiOutput                     output;
    protected final IMidiInput                      input;
    protected final IHwSurfaceFactory               surfaceFactory;

    protected final ViewManager                     viewManager           = new ViewManager ();
    protected final ModeManager                     modeManager           = new ModeManager ();

    protected int                                   defaultMidiChannel    = 0;

    private Map<ButtonID, IHwButton>                buttons               = new EnumMap<> (ButtonID.class);
    private Map<ContinuousID, IHwContinuousControl> continuous            = new EnumMap<> (ContinuousID.class);
    private final ContinuousInfo [] []              continuousInfos       = new ContinuousInfo [16] [NUM_INFOS];
    private final int []                            noteVelocities;

    protected List<ITextDisplay>                    textDisplays          = new ArrayList<> (1);
    protected List<IGraphicDisplay>                 graphicsDisplays      = new ArrayList<> (1);

    protected final PadGrid                         pads;

    private final boolean []                        gridNoteConsumed;
    private final ButtonEvent []                    gridNoteStates;
    private final int []                            gridNoteVelocities;
    private int []                                  keyTranslationTable;

    private final LatestTaskExecutor                flushExecutor         = new LatestTaskExecutor ();
    private final DummyDisplay                      dummyDisplay;


    /**
     * Constructor.
     *
     * @param host The host
     * @param configuration The configuration
     * @param colorManager
     * @param output The midi output
     * @param input The midi input
     * @param padGrid The pads if any, may be null
     * @param width The width of the controller device
     * @param height The height of the controller device
     */
    public AbstractControlSurface (final IHost host, final C configuration, final ColorManager colorManager, final IMidiOutput output, final IMidiInput input, final PadGrid padGrid, final double width, final double height)
    {
        this.host = host;
        this.configuration = configuration;
        this.colorManager = colorManager;
        this.pads = padGrid;

        this.surfaceFactory = host.createSurfaceFactory (width, height);

        this.dummyDisplay = new DummyDisplay (host);

        this.output = output;
        this.input = input;
        if (this.input != null)
            this.input.setMidiCallback (this::handleMidi);

        // Notes
        this.noteVelocities = new int [NUM_NOTES];

        // Grid notes
        this.gridNoteConsumed = new boolean [NUM_NOTES];
        Arrays.fill (this.gridNoteConsumed, false);
        final int size = 8 * 8;
        this.gridNoteStates = new ButtonEvent [NUM_NOTES];
        this.gridNoteVelocities = new int [NUM_NOTES];
        for (int i = 0; i < size; i++)
        {
            this.gridNoteStates[i] = ButtonEvent.UP;
            this.gridNoteVelocities[i] = 0;
        }
    }


    /** {@inheritDoc} */
    @Override
    public ViewManager getViewManager ()
    {
        return this.viewManager;
    }


    /** {@inheritDoc} */
    @Override
    public ModeManager getModeManager ()
    {
        return this.modeManager;
    }


    /** {@inheritDoc} */
    @Override
    public C getConfiguration ()
    {
        return this.configuration;
    }


    /** {@inheritDoc} */
    @Override
    public IDisplay getDisplay ()
    {
        if (this.graphicsDisplays.isEmpty ())
            return this.getTextDisplay (0);
        return this.getGraphicsDisplay ();
    }


    /** {@inheritDoc} */
    @Override
    public ITextDisplay getTextDisplay ()
    {
        return this.getTextDisplay (0);
    }


    /** {@inheritDoc} */
    @Override
    public ITextDisplay getTextDisplay (final int index)
    {
        if (index >= this.textDisplays.size ())
            return this.dummyDisplay;
        return this.textDisplays.get (index);
    }


    /** {@inheritDoc} */
    @Override
    public IGraphicDisplay getGraphicsDisplay ()
    {
        return this.getGraphicsDisplay (0);
    }


    /** {@inheritDoc} */
    @Override
    public IGraphicDisplay getGraphicsDisplay (final int index)
    {
        return this.graphicsDisplays.get (index);
    }


    /** {@inheritDoc} */
    @Override
    public void addTextDisplay (final ITextDisplay display)
    {
        display.setHardwareDisplay (this.surfaceFactory.createTextDisplay (OutputID.DISPLAY1, display.getNoOfLines ()));
        this.textDisplays.add (display);
    }


    /** {@inheritDoc} */
    @Override
    public void addGraphicsDisplay (final IGraphicDisplay display)
    {
        this.graphicsDisplays.add (display);
    }


    /** {@inheritDoc} */
    @Override
    public PadGrid getPadGrid ()
    {
        return this.pads;
    }


    /** {@inheritDoc} */
    @Override
    public IMidiOutput getOutput ()
    {
        return this.output;
    }


    /** {@inheritDoc} */
    @Override
    public IMidiInput getInput ()
    {
        return this.input;
    }


    /** {@inheritDoc} */
    @Override
    public boolean isGridNote (final int note)
    {
        return this.pads != null && this.pads.isGridNote (note);
    }


    /** {@inheritDoc} */
    @Override
    public void setKeyTranslationTable (final int [] table)
    {
        this.keyTranslationTable = table;
        if (this.input == null)
            return;
        final Integer [] t = new Integer [table.length];
        for (int i = 0; i < table.length; i++)
            t[i] = Integer.valueOf (table[i]);
        final INoteInput defaultNoteInput = this.input.getDefaultNoteInput ();
        if (defaultNoteInput != null)
            defaultNoteInput.setKeyTranslationTable (t);
    }


    /** {@inheritDoc} */
    @Override
    public int [] getKeyTranslationTable ()
    {
        return this.keyTranslationTable;
    }


    /** {@inheritDoc} */
    @Override
    public void setVelocityTranslationTable (final int [] table)
    {
        if (this.input == null)
            return;
        final Integer [] t = new Integer [table.length];
        for (int i = 0; i < table.length; i++)
            t[i] = Integer.valueOf (table[i]);
        final INoteInput defaultNoteInput = this.input.getDefaultNoteInput ();
        if (defaultNoteInput != null)
            defaultNoteInput.setVelocityTranslationTable (t);
    }


    /** {@inheritDoc} */
    @Override
    public IHwButton getButton (final ButtonID buttonID)
    {
        return this.buttons.get (buttonID);
    }


    /** {@inheritDoc} */
    @Override
    public IHwContinuousControl getContinuous (ContinuousID continuousID)
    {
        return this.continuous.get (continuousID);
    }


    /** {@inheritDoc} */
    @Override
    public boolean isShiftPressed ()
    {
        return this.isPressed (ButtonID.SHIFT);
    }


    /** {@inheritDoc} */
    @Override
    public boolean isSelectPressed ()
    {
        return this.isPressed (ButtonID.SELECT);
    }


    /** {@inheritDoc} */
    @Override
    public boolean isDeletePressed ()
    {
        return this.isPressed (ButtonID.DELETE);
    }


    /** {@inheritDoc} */
    @Override
    public boolean isSoloPressed ()
    {
        return this.isPressed (ButtonID.SOLO);
    }


    /** {@inheritDoc} */
    @Override
    public boolean isMutePressed ()
    {
        return this.isPressed (ButtonID.MUTE);
    }


    /** {@inheritDoc} */
    @Override
    public boolean isPressed (final ButtonID buttonID)
    {
        final IHwButton button = this.buttons.get (buttonID);
        return button != null && button.isPressed ();
    }


    /** {@inheritDoc} */
    @Override
    public boolean isLongPressed (final ButtonID buttonID)
    {
        final IHwButton button = this.buttons.get (buttonID);
        return button != null && button.isLongPressed ();
    }


    /** {@inheritDoc} */
    @Override
    public IHwButton createButton (final ButtonID buttonID, final String label)
    {
        final IHwButton button = this.surfaceFactory.createButton (buttonID, label);
        this.buttons.put (buttonID, button);
        return button;
    }


    /** {@inheritDoc} */
    @Override
    public IHwLight createLight (final IntSupplier supplier, final IntConsumer sendConsumer, IntFunction<ColorEx> stateToColorFunction)
    {
        return this.surfaceFactory.createLight (supplier, sendConsumer, stateToColorFunction);
    }


    /** {@inheritDoc} */
    @Override
    public IHwFader createFader (final ContinuousID faderID, final String label)
    {
        final IHwFader fader = this.surfaceFactory.createFader (faderID, label);
        this.continuous.put (faderID, fader);
        return fader;
    }


    /** {@inheritDoc} */
    @Override
    public IHwAbsoluteKnob createAbsoluteKnob (final ContinuousID knobID, final String label)
    {
        final IHwAbsoluteKnob knob = this.surfaceFactory.createAbsoluteKnob (knobID, label);
        this.continuous.put (knobID, knob);
        return knob;
    }


    /** {@inheritDoc} */
    @Override
    public IHwRelativeKnob createRelativeKnob (final ContinuousID knobID, final String label)
    {
        final IHwRelativeKnob knob = this.surfaceFactory.createRelativeKnob (knobID, label);
        this.continuous.put (knobID, knob);
        return knob;
    }


    /** {@inheritDoc} */
    @Override
    public void setTrigger (final int cc, final int state)
    {
        this.setTrigger (this.defaultMidiChannel, cc, state);
    }


    /** {@inheritDoc} */
    @Override
    public void setTrigger (final int cc, final String colorID)
    {
        this.setTrigger (cc, this.colorManager.getColorIndex (colorID));
    }


    /** {@inheritDoc} */
    @Override
    public void setTrigger (final int channel, final int cc, final String colorID)
    {
        this.setTrigger (channel, cc, this.colorManager.getColorIndex (colorID));
    }


    /** {@inheritDoc} */
    @Override
    public void setTrigger (final int channel, final int cc, final int state)
    {
        // Overwrite to support trigger LEDs
    }


    /** {@inheritDoc} */
    @Override
    public void setContinuous (final int channel, final int cc, final int value)
    {
        // Overwrite to support continuous LEDs/motors
    }


    /** {@inheritDoc} */
    @Override
    public void clearTriggerCache ()
    {
        this.turnOffTriggers ();
    }


    /** {@inheritDoc} */
    @Override
    public void setTriggerConsumed (final ButtonID buttonID)
    {
        final IHwButton button = this.buttons.get (buttonID);
        if (button != null)
            button.setConsumed ();
    }


    /** {@inheritDoc} */
    @Override
    public boolean isTriggerConsumed (final ButtonID buttonID)
    {
        final IHwButton button = this.buttons.get (buttonID);
        return button != null && button.isConsumed ();
    }


    /** {@inheritDoc} */
    @Override
    public void turnOffTriggers ()
    {
        this.buttons.values ().forEach (button -> {
            final IHwLight light = button.getLight ();
            if (light != null)
                light.turnOff ();
        });
        this.surfaceFactory.flush ();
    }


    /** {@inheritDoc} */
    @Override
    public void updateContinuous (final int cc, final int value)
    {
        this.updateContinuous (this.defaultMidiChannel, cc, value);
    }


    /** {@inheritDoc} */
    @Override
    public void updateContinuous (final int channel, final int cc, final int value)
    {
        final ContinuousInfo info = this.getContinuousInfo (channel, cc);
        if (info == null || info.getValue () == value)
            return;
        this.setContinuous (channel, cc, value);
        info.setValue (value);
    }


    /** {@inheritDoc} */
    @Override
    public void setContinuous (final int cc, final int state)
    {
        this.setContinuous (this.defaultMidiChannel, cc, state);
    }


    /** {@inheritDoc} */
    @Override
    public void clearContinuousCache ()
    {
        for (int channel = 0; channel < 16; channel++)
        {
            for (int cc = 0; cc < NUM_INFOS; cc++)
            {
                if (this.continuousInfos[channel][cc] != null)
                    this.continuousInfos[channel][cc].setValue (-1);
            }
        }
    }


    /** {@inheritDoc} */
    @Override
    public void clearContinuousCache (final int cc)
    {
        this.clearContinuousCache (this.defaultMidiChannel, cc);
    }


    /** {@inheritDoc} */
    @Override
    public void clearContinuousCache (final int channel, final int cc)
    {
        final ContinuousInfo info = this.getContinuousInfo (channel, cc);
        if (info != null)
            info.setValue (-1);
    }


    /** {@inheritDoc} */
    @Override
    public void flush ()
    {
        this.flushExecutor.execute ( () -> {
            try
            {
                this.scheduledFlush ();
                this.redrawGrid ();
            }
            catch (final RuntimeException ex)
            {
                this.host.error ("Crash during flush.", ex);
            }
        });
    }


    /** {@inheritDoc} */
    @Override
    public void shutdown ()
    {
        this.flushExecutor.shutdown ();

        this.turnOffTriggers ();

        if (this.pads != null)
            this.pads.turnOff ();

        this.textDisplays.forEach (IDisplay::shutdown);
        this.graphicsDisplays.forEach (IDisplay::shutdown);
    }


    /**
     * Handle received midi data.
     *
     * @param status The midi status byte
     * @param data1 The midi data byte 1
     * @param data2 The midi data byte 2
     */
    protected void handleMidi (final int status, final int data1, final int data2)
    {
        final int code = status & 0xF0;
        final int channel = status & 0xF;

        switch (code)
        {
            // Note off
            case 0x80:
                this.handleNote (data1, 0);
                break;

            // Note on
            case 0x90:
                this.handleNote (data1, data2);
                break;

            // Polyphonic Aftertouch
            case 0xA0:
                this.handlePolyAftertouch (data1, data2);
                break;

            // CC
            case 0xB0:
                // TODO Check if already fully handled by framework
                // this.handleCC (channel, data1, data2);
                break;

            // Program Change
            case 0xC0:
                this.handleProgramChange (channel, data1, data2);
                break;

            // Channel Aftertouch
            case 0xD0:
                this.handleChannelAftertouch (data1);
                break;

            // Pitch Bend
            case 0xE0:
                this.handlePitchBend (channel, data1, data2);
                break;

            default:
                this.host.println ("Unhandled midi status: " + status);
                break;
        }
    }


    /**
     * Handle a note event
     *
     * @param note The note
     * @param velocity The velocity
     */
    protected void handleNote (final int note, final int velocity)
    {
        if (this.isGridNote (note))
            this.handleGridNote (note, velocity);
        else
            this.handleNoteEvent (note, velocity);
    }


    /**
     * Handle pitch bend.
     *
     * @param channel The MIDI channel
     * @param data1 First data byte
     * @param data2 Second data byte
     */
    protected void handlePitchBend (final int channel, final int data1, final int data2)
    {
        final View view = this.viewManager.getActiveView ();
        if (view != null)
            view.executePitchbendCommand (channel, data1, data2);
    }


    /**
     * Handle channel aftertouch.
     *
     * @param data1 First data byte
     */
    protected void handleChannelAftertouch (final int data1)
    {
        final View view = this.viewManager.getActiveView ();
        if (view != null)
            view.executeAftertouchCommand (-1, data1);
    }


    /**
     * Handle poly aftertouch.
     *
     * @param data1 First data byte
     * @param data2 Second data byte
     */
    protected void handlePolyAftertouch (final int data1, final int data2)
    {
        final View view = this.viewManager.getActiveView ();
        if (view != null)
            view.executeAftertouchCommand (data1, data2);
    }


    /**
     * Handle program change.
     *
     * @param channel The MIDI channel
     * @param data1 First data byte
     * @param data2 Second data byte
     */
    protected void handleProgramChange (final int channel, final int data1, final int data2)
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public void scheduleTask (final Runnable callback, final long delay)
    {
        this.host.scheduleTask ( () -> {
            try
            {
                callback.run ();
            }
            catch (final RuntimeException ex)
            {
                this.host.error ("Could not execute scheduled task.", ex);
            }
        }, delay);
    }


    /** {@inheritDoc} */
    @Override
    public void println (final String message)
    {
        this.host.println (message);
    }


    /** {@inheritDoc} */
    @Override
    public void errorln (final String message)
    {
        this.host.error (message);
    }


    /** {@inheritDoc} */
    @Override
    public void sendMidiEvent (final int status, final int data1, final int data2)
    {
        this.input.sendRawMidiEvent (status, data1, data2);
    }


    /**
     * Handle a midi note which belongs to the grid.
     *
     * @param note The midi note
     * @param velocity The velocity of the note
     */
    protected void handleGridNote (final int note, final int velocity)
    {
        final int gridNote = this.pads.translateToGrid (note);

        this.gridNoteStates[gridNote] = velocity > 0 ? ButtonEvent.DOWN : ButtonEvent.UP;
        if (velocity > 0)
            this.gridNoteVelocities[gridNote] = velocity;
        if (this.gridNoteStates[gridNote] == ButtonEvent.DOWN)
            this.scheduleTask ( () -> this.checkGridNoteState (gridNote), AbstractControlSurface.BUTTON_STATE_INTERVAL);

        // If consumed flag is set ignore the UP event
        if (this.gridNoteStates[gridNote] == ButtonEvent.UP && this.gridNoteConsumed[gridNote])
        {
            this.gridNoteConsumed[gridNote] = false;
            return;
        }

        final View view = this.viewManager.getActiveView ();
        if (view != null)
            view.onGridNote (gridNote, velocity);
    }


    private void checkGridNoteState (final int note)
    {
        if (this.gridNoteStates[note] != ButtonEvent.DOWN)
            return;

        this.gridNoteStates[note] = ButtonEvent.LONG;

        final View view = this.viewManager.getActiveView ();
        if (view != null)
            view.onGridNoteLongPress (note);
    }


    /**
     * Set a grid note as consumed.
     *
     * @param note The note to set
     */
    public void setGridNoteConsumed (final int note)
    {
        this.gridNoteConsumed[note] = true;
    }


    /**
     * Get the grid note velocity of a note on the grid.
     *
     * @param note The note
     * @return The velocity
     */
    public int getGridNoteVelocity (final int note)
    {
        return this.gridNoteVelocities[note];
    }


    /**
     * Get the velocity of a pressed note.
     *
     * @param note The note
     * @return The velocity, 0 if currently not pressed
     */
    public int getNoteVelocity (final int note)
    {
        return this.noteVelocities[note];
    }


    /**
     * Handle note events.
     *
     * @param note The midi note
     * @param velocity The velocity
     */
    protected void handleNoteEvent (final int note, final int velocity)
    {
        this.noteVelocities[note] = velocity;
    }


    /**
     * Delayed flush.
     */
    protected void scheduledFlush ()
    {
        final View view = this.viewManager.getActiveView ();
        if (view != null)
            view.updateControlSurface ();
        this.textDisplays.forEach (ITextDisplay::flush);

        this.surfaceFactory.flush ();
    }


    /**
     * Redraws the grid for the active view.
     */
    protected void redrawGrid ()
    {
        final View view = this.viewManager.getActiveView ();
        if (view == null)
            return;
        view.drawGrid ();
        if (this.pads != null)
            this.pads.flush ();
    }


    private ContinuousInfo getContinuousInfo (final int channel, final int cc)
    {
        if (channel < 0 || cc < 0)
            return null;

        if (this.continuousInfos[channel][cc] == null)
        {
            this.errorln ("Unregistered CC continuous: " + cc);
            return null;
        }
        return this.continuousInfos[channel][cc];
    }
}