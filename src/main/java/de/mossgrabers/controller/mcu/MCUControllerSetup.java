// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.mcu;

import de.mossgrabers.controller.mcu.command.continuous.PlayPositionTempoCommand;
import de.mossgrabers.controller.mcu.command.pitchbend.PitchbendVolumeCommand;
import de.mossgrabers.controller.mcu.command.trigger.AssignableCommand;
import de.mossgrabers.controller.mcu.command.trigger.DevicesCommand;
import de.mossgrabers.controller.mcu.command.trigger.GrooveCommand;
import de.mossgrabers.controller.mcu.command.trigger.KeyCommand;
import de.mossgrabers.controller.mcu.command.trigger.KeyCommand.Key;
import de.mossgrabers.controller.mcu.command.trigger.MCUCursorCommand;
import de.mossgrabers.controller.mcu.command.trigger.MCURecordCommand;
import de.mossgrabers.controller.mcu.command.trigger.OverdubCommand;
import de.mossgrabers.controller.mcu.command.trigger.ScrubCommand;
import de.mossgrabers.controller.mcu.command.trigger.SelectCommand;
import de.mossgrabers.controller.mcu.command.trigger.SendSelectCommand;
import de.mossgrabers.controller.mcu.command.trigger.TempoTicksCommand;
import de.mossgrabers.controller.mcu.command.trigger.ToggleDisplayCommand;
import de.mossgrabers.controller.mcu.command.trigger.TracksCommand;
import de.mossgrabers.controller.mcu.command.trigger.ZoomCommand;
import de.mossgrabers.controller.mcu.controller.MCUControlSurface;
import de.mossgrabers.controller.mcu.controller.MCUDisplay;
import de.mossgrabers.controller.mcu.controller.MCUSegmentDisplay;
import de.mossgrabers.controller.mcu.mode.MarkerMode;
import de.mossgrabers.controller.mcu.mode.device.DeviceBrowserMode;
import de.mossgrabers.controller.mcu.mode.device.DeviceParamsMode;
import de.mossgrabers.controller.mcu.mode.track.MasterMode;
import de.mossgrabers.controller.mcu.mode.track.PanMode;
import de.mossgrabers.controller.mcu.mode.track.SendMode;
import de.mossgrabers.controller.mcu.mode.track.TrackMode;
import de.mossgrabers.controller.mcu.mode.track.VolumeMode;
import de.mossgrabers.framework.command.ContinuousCommandID;
import de.mossgrabers.framework.command.continuous.KnobRowModeCommand;
import de.mossgrabers.framework.command.core.NopCommand;
import de.mossgrabers.framework.command.trigger.AutomationCommand;
import de.mossgrabers.framework.command.trigger.BrowserCommand;
import de.mossgrabers.framework.command.trigger.MarkerCommand;
import de.mossgrabers.framework.command.trigger.ShiftCommand;
import de.mossgrabers.framework.command.trigger.application.DuplicateCommand;
import de.mossgrabers.framework.command.trigger.application.LayoutCommand;
import de.mossgrabers.framework.command.trigger.application.PaneCommand;
import de.mossgrabers.framework.command.trigger.application.SaveCommand;
import de.mossgrabers.framework.command.trigger.application.UndoCommand;
import de.mossgrabers.framework.command.trigger.device.DeviceOnOffCommand;
import de.mossgrabers.framework.command.trigger.mode.ModeCursorCommand.Direction;
import de.mossgrabers.framework.command.trigger.mode.ModeSelectCommand;
import de.mossgrabers.framework.command.trigger.track.MoveTrackBankCommand;
import de.mossgrabers.framework.command.trigger.track.ToggleTrackBanksCommand;
import de.mossgrabers.framework.command.trigger.track.ToggleVUCommand;
import de.mossgrabers.framework.command.trigger.transport.MetronomeCommand;
import de.mossgrabers.framework.command.trigger.transport.PlayCommand;
import de.mossgrabers.framework.command.trigger.transport.PunchInCommand;
import de.mossgrabers.framework.command.trigger.transport.PunchOutCommand;
import de.mossgrabers.framework.command.trigger.transport.StopCommand;
import de.mossgrabers.framework.command.trigger.transport.TapTempoCommand;
import de.mossgrabers.framework.command.trigger.transport.ToggleLoopCommand;
import de.mossgrabers.framework.command.trigger.transport.WindCommand;
import de.mossgrabers.framework.configuration.AbstractConfiguration;
import de.mossgrabers.framework.configuration.ISettingsUI;
import de.mossgrabers.framework.controller.AbstractControllerSetup;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.ISetupFactory;
import de.mossgrabers.framework.controller.Relative2ValueChanger;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.daw.IApplication;
import de.mossgrabers.framework.daw.ICursorDevice;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.IParameterBank;
import de.mossgrabers.framework.daw.ISendBank;
import de.mossgrabers.framework.daw.ITrackBank;
import de.mossgrabers.framework.daw.ITransport;
import de.mossgrabers.framework.daw.ModelSetup;
import de.mossgrabers.framework.daw.data.IMasterTrack;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.midi.IMidiAccess;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.daw.midi.IMidiOutput;
import de.mossgrabers.framework.mode.Mode;
import de.mossgrabers.framework.mode.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.view.ControlOnlyView;
import de.mossgrabers.framework.view.ViewManager;
import de.mossgrabers.framework.view.Views;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;


/**
 * Support for the Mackie MCU protocol.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class MCUControllerSetup extends AbstractControllerSetup<MCUControlSurface, MCUConfiguration>
{
    /** State for button LED on. */
    public static final int                 MCU_BUTTON_STATE_ON  = 127;
    /** State for button LED off. */
    public static final int                 MCU_BUTTON_STATE_OFF = 0;

    private static final Map<Modes, String> MODE_ACRONYMS        = new EnumMap<> (Modes.class);

    static
    {
        MODE_ACRONYMS.put (Modes.TRACK, "TR");
        MODE_ACRONYMS.put (Modes.VOLUME, "VL");
        MODE_ACRONYMS.put (Modes.PAN, "PN");
        MODE_ACRONYMS.put (Modes.SEND1, "S1");
        MODE_ACRONYMS.put (Modes.SEND2, "S2");
        MODE_ACRONYMS.put (Modes.SEND3, "S3");
        MODE_ACRONYMS.put (Modes.SEND4, "S4");
        MODE_ACRONYMS.put (Modes.SEND5, "S5");
        MODE_ACRONYMS.put (Modes.SEND6, "S6");
        MODE_ACRONYMS.put (Modes.SEND7, "S7");
        MODE_ACRONYMS.put (Modes.SEND8, "S8");
        MODE_ACRONYMS.put (Modes.MASTER, "MT");
        MODE_ACRONYMS.put (Modes.DEVICE_PARAMS, "DC");
        MODE_ACRONYMS.put (Modes.BROWSER, "BR");
        MODE_ACRONYMS.put (Modes.MARKERS, "MK");

    }

    private final int [] masterVuValues   = new int [2];
    private int          masterFaderValue = -1;
    private final int [] vuValues         = new int [36];
    private final int [] faderValues      = new int [36];
    private final int    numMCUDevices;


    /**
     * Constructor.
     *
     * @param host The DAW host
     * @param factory The factory
     * @param globalSettings The global settings
     * @param documentSettings The document (project) specific settings
     * @param numMCUDevices The number of MCU devices (main device + extenders) to support
     */
    public MCUControllerSetup (final IHost host, final ISetupFactory factory, final ISettingsUI globalSettings, final ISettingsUI documentSettings, final int numMCUDevices)
    {
        super (factory, host, globalSettings, documentSettings);

        this.numMCUDevices = numMCUDevices;

        Arrays.fill (this.vuValues, -1);
        Arrays.fill (this.faderValues, -1);
        Arrays.fill (this.masterVuValues, -1);

        this.colorManager = new ColorManager ();
        this.valueChanger = new Relative2ValueChanger (16241 + 1, 100, 10);
        this.configuration = new MCUConfiguration (host, this.valueChanger);
    }


    /** {@inheritDoc} */
    @Override
    public void flush ()
    {
        super.flush ();

        this.updateMode (this.getSurface ().getModeManager ().getActiveOrTempModeId ());
    }


    /** {@inheritDoc} */
    @Override
    protected void createModel ()
    {
        final int adjustedNum = 8 * this.numMCUDevices;

        final ModelSetup ms = new ModelSetup ();
        ms.setNumTracks (adjustedNum);
        ms.setNumScenes (0);
        ms.setNumFilterColumnEntries (8);
        ms.setNumResults (8);
        ms.setNumParams (adjustedNum);
        ms.setNumDeviceLayers (0);
        ms.setNumDrumPadLayers (0);
        ms.setNumMarkers (adjustedNum);
        this.model = this.factory.createModel (this.colorManager, this.valueChanger, this.scales, ms);

        final ITrackBank trackBank = this.model.getTrackBank ();
        trackBank.setIndication (true);
        trackBank.addSelectionObserver ( (index, isSelected) -> this.handleTrackChange (isSelected));

        this.model.getMasterTrack ().addSelectionObserver ( (index, isSelected) -> {
            final ModeManager modeManager = this.getSurface ().getModeManager ();
            if (isSelected)
                modeManager.setActiveMode (Modes.MASTER);
            else
                modeManager.restoreMode ();
        });
    }


    /** {@inheritDoc} */
    @Override
    protected void createSurface ()
    {
        final IMidiAccess midiAccess = this.factory.createMidiAccess ();

        for (int i = 0; i < this.numMCUDevices; i++)
        {
            final IMidiOutput output = midiAccess.createOutput (i);
            final IMidiInput input = midiAccess.createInput (i, null);
            final MCUControlSurface surface = new MCUControlSurface (this.surfaces, this.host, this.colorManager, this.configuration, output, input, 8 * (this.numMCUDevices - i - 1), i == 0);
            this.surfaces.add (surface);
            surface.addTextDisplay (new MCUDisplay (this.host, output, true, false));
            surface.addTextDisplay (new MCUDisplay (this.host, output, false, i == 0));
            surface.addTextDisplay (new MCUSegmentDisplay (output));
            surface.getModeManager ().setDefaultMode (Modes.VOLUME);
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void createModes ()
    {
        for (int index = 0; index < this.numMCUDevices; index++)
        {
            final MCUControlSurface surface = this.getSurface (index);
            final ModeManager modeManager = surface.getModeManager ();

            modeManager.registerMode (Modes.TRACK, new TrackMode (surface, this.model));
            modeManager.registerMode (Modes.VOLUME, new VolumeMode (surface, this.model));
            modeManager.registerMode (Modes.PAN, new PanMode (surface, this.model));
            final SendMode modeSend = new SendMode (surface, this.model);
            for (int i = 0; i < 8; i++)
                modeManager.registerMode (Modes.get (Modes.SEND1, i), modeSend);
            modeManager.registerMode (Modes.MASTER, new MasterMode (surface, this.model, false));

            modeManager.registerMode (Modes.DEVICE_PARAMS, new DeviceParamsMode (surface, this.model));
            modeManager.registerMode (Modes.BROWSER, new DeviceBrowserMode (surface, this.model));
            modeManager.registerMode (Modes.MARKERS, new MarkerMode (surface, this.model));
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void createObservers ()
    {
        for (int index = 0; index < this.numMCUDevices; index++)
        {
            final MCUControlSurface surface = this.getSurface (index);
            surface.getModeManager ().addModeListener ( (oldMode, newMode) -> {

                for (int d = 0; d < this.numMCUDevices; d++)
                {
                    final MCUControlSurface s = this.getSurface (d);
                    if (!s.equals (surface))
                        s.getModeManager ().setActiveMode (newMode);
                }

                this.updateMode (null);
                this.updateMode (newMode);
            });
        }

        this.configuration.addSettingObserver (AbstractConfiguration.ENABLE_VU_METERS, () -> {
            for (int index = 0; index < this.numMCUDevices; index++)
            {
                final MCUControlSurface surface = this.getSurface (index);
                surface.switchVuMode (this.configuration.isEnableVUMeters () ? MCUControlSurface.VUMODE_LED_AND_LCD : MCUControlSurface.VUMODE_OFF);
                final Mode activeMode = surface.getModeManager ().getActiveOrTempMode ();
                if (activeMode != null)
                    activeMode.updateDisplay ();
                ((MCUDisplay) surface.getDisplay ()).forceFlush ();
            }
        });
    }


    /** {@inheritDoc} */
    @Override
    protected void createViews ()
    {
        for (int index = 0; index < this.numMCUDevices; index++)
        {
            final MCUControlSurface surface = this.getSurface (index);
            surface.getViewManager ().registerView (Views.CONTROL, new ControlOnlyView<> (surface, this.model));
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void registerTriggerCommands ()
    {
        // Assignments to the main device
        final MCUControlSurface surface = this.getSurface ();

        // Footswitches
        this.addButton (ButtonID.FOOTSWITCH1, "Footswitch 1", new AssignableCommand (0, this.model, surface), MCUControlSurface.MCU_USER_A);
        this.addButton (ButtonID.FOOTSWITCH2, "Footswitch 2", new AssignableCommand (1, this.model, surface), MCUControlSurface.MCU_USER_B);

        // Navigation
        this.addButton (ButtonID.REWIND, "<<", new WindCommand<> (this.model, surface, false), MCUControlSurface.MCU_REWIND);
        this.addButton (ButtonID.FORWARD, ">>", new WindCommand<> (this.model, surface, true), MCUControlSurface.MCU_FORWARD);
        this.addButton (ButtonID.LOOP, "Loop", new ToggleLoopCommand<> (this.model, surface), MCUControlSurface.MCU_REPEAT);
        this.addButton (ButtonID.STOP, "Stop", new StopCommand<> (this.model, surface), MCUControlSurface.MCU_STOP);
        this.addButton (ButtonID.PLAY, "Play", new PlayCommand<> (this.model, surface), MCUControlSurface.MCU_PLAY);
        this.addButton (ButtonID.RECORD, "Record", new MCURecordCommand (this.model, surface), MCUControlSurface.MCU_RECORD);

        this.addButton (ButtonID.SCRUB, "Scrub", new ScrubCommand (this.model, surface), MCUControlSurface.MCU_SCRUB);
        this.addButton (ButtonID.ARROW_LEFT, "Left", new MCUCursorCommand (Direction.LEFT, this.model, surface), MCUControlSurface.MCU_ARROW_LEFT);
        this.addButton (ButtonID.ARROW_RIGHT, "Right", new MCUCursorCommand (Direction.RIGHT, this.model, surface), MCUControlSurface.MCU_ARROW_RIGHT);
        this.addButton (ButtonID.ARROW_UP, "Up", new MCUCursorCommand (Direction.UP, this.model, surface), MCUControlSurface.MCU_ARROW_UP);
        this.addButton (ButtonID.ARROW_DOWN, "Down", new MCUCursorCommand (Direction.DOWN, this.model, surface), MCUControlSurface.MCU_ARROW_DOWN);
        this.addButton (ButtonID.ZOOM, "Zoom", new ZoomCommand (this.model, surface), MCUControlSurface.MCU_ZOOM);

        // Display Mode
        this.addButton (ButtonID.TOGGLE_DISPLAY, "Toggle Display", new ToggleDisplayCommand (this.model, surface), MCUControlSurface.MCU_NAME_VALUE);
        this.addButton (ButtonID.TEMPO_TICKS, "Tempo Ticks", new TempoTicksCommand (this.model, surface), MCUControlSurface.MCU_SMPTE_BEATS);

        // Functions
        this.addButton (ButtonID.SHIFT, "Shift", new ShiftCommand<> (this.model, surface), MCUControlSurface.MCU_SHIFT);
        this.addButton (ButtonID.SELECT, "Option", NopCommand.INSTANCE, MCUControlSurface.MCU_OPTION);
        this.addButton (ButtonID.PUNCH_IN, "Punch In", new PunchInCommand<> (this.model, surface), MCUControlSurface.MCU_F6);
        this.addButton (ButtonID.PUNCH_OUT, "Punch Out", new PunchOutCommand<> (this.model, surface), MCUControlSurface.MCU_F7);
        this.addButton (ButtonID.F1, "F1", new AssignableCommand (2, this.model, surface), MCUControlSurface.MCU_F1);
        this.addButton (ButtonID.F2, "F2", new AssignableCommand (3, this.model, surface), MCUControlSurface.MCU_F2);
        this.addButton (ButtonID.F3, "F3", new AssignableCommand (4, this.model, surface), MCUControlSurface.MCU_F3);
        this.addButton (ButtonID.F4, "F4", new AssignableCommand (5, this.model, surface), MCUControlSurface.MCU_F4);
        this.addButton (ButtonID.F5, "F5", new AssignableCommand (6, this.model, surface), MCUControlSurface.MCU_F5);

        // Assignment
        this.addButton (ButtonID.TRACK, "IO", new TracksCommand (this.model, surface), MCUControlSurface.MCU_MODE_IO);
        this.addButton (ButtonID.PAN_SEND, "Panorama", new ModeSelectCommand<> (this.model, surface, Modes.PAN), MCUControlSurface.MCU_MODE_PAN);
        this.addButton (ButtonID.SENDS, "Sends", new SendSelectCommand (this.model, surface), MCUControlSurface.MCU_MODE_SENDS);
        this.addButton (ButtonID.DEVICE, "Device", new DevicesCommand (this.model, surface), MCUControlSurface.MCU_MODE_PLUGIN);
        this.addButton (ButtonID.MOVE_TRACK_LEFT, "Eq", new MoveTrackBankCommand<> (this.model, surface, Modes.DEVICE_PARAMS, true, true), MCUControlSurface.MCU_MODE_EQ);
        this.addButton (ButtonID.MOVE_TRACK_RIGHT, "Dyn", new MoveTrackBankCommand<> (this.model, surface, Modes.DEVICE_PARAMS, true, false), MCUControlSurface.MCU_MODE_DYN);

        // Automation
        this.addButton (ButtonID.AUTOMATION_READ, "Read", new AutomationCommand<> (0, this.model, surface), MCUControlSurface.MCU_READ);
        final AutomationCommand<MCUControlSurface, MCUConfiguration> writeCommand = new AutomationCommand<> (1, this.model, surface);
        // TODO does not work like this...
        this.addButton (ButtonID.AUTOMATION_WRITE, "Write", writeCommand, MCUControlSurface.MCU_WRITE);
        this.addButton (ButtonID.AUTOMATION_WRITE, "Group", writeCommand, MCUControlSurface.MCU_GROUP);
        this.addButton (ButtonID.AUTOMATION_TRIM, "Trim", new AutomationCommand<> (2, this.model, surface), MCUControlSurface.MCU_TRIM);
        this.addButton (ButtonID.AUTOMATION_TOUCH, "Touch", new AutomationCommand<> (3, this.model, surface), MCUControlSurface.MCU_TOUCH);
        this.addButton (ButtonID.AUTOMATION_LATCH, "Latch", new AutomationCommand<> (4, this.model, surface), MCUControlSurface.MCU_LATCH);
        this.addButton (ButtonID.UNDO, "Undo", new UndoCommand<> (this.model, surface), MCUControlSurface.MCU_UNDO);

        // Panes
        this.addButton (ButtonID.NOTE_EDITOR, "Note Editor", new PaneCommand<> (PaneCommand.Panels.NOTE, this.model, surface), MCUControlSurface.MCU_MIDI_TRACKS);
        this.addButton (ButtonID.AUTOMATION_EDITOR, "Automation Editor", new PaneCommand<> (PaneCommand.Panels.AUTOMATION, this.model, surface), MCUControlSurface.MCU_INPUTS);
        this.addButton (ButtonID.TOGGLE_DEVICE, "Toggle Device", new PaneCommand<> (PaneCommand.Panels.DEVICE, this.model, surface), MCUControlSurface.MCU_AUDIO_TRACKS);
        this.addButton (ButtonID.MIXER, "Mixer", new PaneCommand<> (PaneCommand.Panels.MIXER, this.model, surface), MCUControlSurface.MCU_AUDIO_INSTR);

        // Layouts
        this.addButton (ButtonID.LAYOUT_ARRANGE, "Arrange", new LayoutCommand<> (IApplication.PANEL_LAYOUT_ARRANGE, this.model, surface), MCUControlSurface.MCU_AUX);
        this.addButton (ButtonID.LAYOUT_MIX, "Mix", new LayoutCommand<> (IApplication.PANEL_LAYOUT_MIX, this.model, surface), MCUControlSurface.MCU_BUSSES);
        this.addButton (ButtonID.LAYOUT_EDIT, "Edit", new LayoutCommand<> (IApplication.PANEL_LAYOUT_EDIT, this.model, surface), MCUControlSurface.MCU_OUTPUTS);

        // Utilities
        this.addButton (ButtonID.BROWSE, "Browse", new BrowserCommand<> (Modes.BROWSER, this.model, surface), MCUControlSurface.MCU_USER);
        this.addButton (ButtonID.METRONOME, "Metronome", new MetronomeCommand<> (this.model, surface), MCUControlSurface.MCU_CLICK);
        this.addButton (ButtonID.GROOVE, "Groove", new GrooveCommand (this.model, surface), MCUControlSurface.MCU_SOLO);
        this.addButton (ButtonID.OVERDUB, "Overdub", new OverdubCommand (this.model, surface), MCUControlSurface.MCU_REPLACE);
        this.addButton (ButtonID.TAP_TEMPO, "Tap Tempo", new TapTempoCommand<> (this.model, surface), MCUControlSurface.MCU_NUDGE);
        this.addButton (ButtonID.DUPLICATE, "Duplicate", new DuplicateCommand<> (this.model, surface), MCUControlSurface.MCU_DROP);

        this.addButton (ButtonID.DEVICE_ON_OFF, "Device On/Off", new DeviceOnOffCommand<> (this.model, surface), MCUControlSurface.MCU_F8);

        // Currently not used but prevent error in console
        this.addButton (ButtonID.CONTROL, "Control", NopCommand.INSTANCE, MCUControlSurface.MCU_CONTROL);
        this.addButton (ButtonID.ALT, "Alt", NopCommand.INSTANCE, MCUControlSurface.MCU_ALT);

        // Fader Controls
        this.addButton (ButtonID.FLIP, "Flip", new ToggleTrackBanksCommand<> (this.model, surface), MCUControlSurface.MCU_FLIP);
        this.addButton (ButtonID.CANCEL, "Cancel", new KeyCommand (Key.ESCAPE, this.model, surface), MCUControlSurface.MCU_CANCEL);
        this.addButton (ButtonID.ENTER, "Enter", new KeyCommand (Key.ENTER, this.model, surface), MCUControlSurface.MCU_ENTER);

        this.addButton (ButtonID.MOVE_BANK_LEFT, "Bank Left", new MoveTrackBankCommand<> (this.model, surface, Modes.DEVICE_PARAMS, false, true), MCUControlSurface.MCU_BANK_LEFT);
        this.addButton (ButtonID.MOVE_BANK_RIGHT, "Bank Right", new MoveTrackBankCommand<> (this.model, surface, Modes.DEVICE_PARAMS, false, false), MCUControlSurface.MCU_BANK_RIGHT);
        this.addButton (ButtonID.MOVE_TRACK_LEFT, "Left", new MoveTrackBankCommand<> (this.model, surface, Modes.DEVICE_PARAMS, true, true), MCUControlSurface.MCU_TRACK_LEFT);
        this.addButton (ButtonID.MOVE_TRACK_RIGHT, "Right", new MoveTrackBankCommand<> (this.model, surface, Modes.DEVICE_PARAMS, true, false), MCUControlSurface.MCU_TRACK_RIGHT);

        // Additional commands for footcontrollers
        final ViewManager viewManager = surface.getViewManager ();
        // TODO
        // viewManager.registerTriggerCommand (TriggerCommandID.NEW, new NewCommand<> (this.model,
        // surface));
        // viewManager.registerTriggerCommand (TriggerCommandID.TAP_TEMPO, new TapTempoCommand<>
        // (this.model, surface));

        // Only MCU
        this.addButton (ButtonID.SAVE, "Save", new SaveCommand<> (this.model, surface), MCUControlSurface.MCU_SAVE);
        this.addButton (ButtonID.MARKER, "Marker", new MarkerCommand<> (this.model, surface), MCUControlSurface.MCU_MARKER);
        this.addButton (ButtonID.TOGGLE_VU, "Toggle VU", new ToggleVUCommand<> (this.model, surface), MCUControlSurface.MCU_EDIT);

        this.addButton (ButtonID.MASTERTRACK, "Master", new SelectCommand (8, this.model, surface), MCUControlSurface.MCU_FADER_MASTER);

        // TODO This is only a light, no button
        // this.setupButton (ButtonID.LED_1, "SMPTE LED", NopCommand.INSTANCE,
        // MCUControlSurface.MCU_SMPTE_LED);
        // this.setupButton (ButtonID.LED_2, "Beats LED", NopCommand.INSTANCE,
        // MCUControlSurface.MCU_BEATS_LED);

        this.registerTriggerCommandsToAllDevices ();
    }


    /**
     * Common track editing - Assignment to all devices
     */
    protected void registerTriggerCommandsToAllDevices ()
    {
        for (int index = 0; index < this.numMCUDevices; index++)
        {
            final MCUControlSurface surface = this.getSurface (index);
            final ViewManager viewManager = surface.getViewManager ();
            for (int i = 0; i < 8; i++)
            {
                // TODO
                // TriggerCommandID commandID = TriggerCommandID.get (TriggerCommandID.ROW_SELECT_1,
                // i);
                // viewManager.registerTriggerCommand (commandID, new SelectCommand (i, this.model,
                // surface));
                // surface.assignTriggerCommand (MCUControlSurface.MCU_SELECT1 + i, commandID);
                //
                // commandID = TriggerCommandID.get (TriggerCommandID.FADER_TOUCH_1, i);
                // viewManager.registerTriggerCommand (commandID, new FaderTouchCommand (i,
                // this.model, surface));
                // surface.assignTriggerCommand (MCUControlSurface.MCU_FADER_TOUCH1 + i, commandID);
                //
                // this.setupButton (ButtonID.get (TriggerCommandID.ROW1_1, i),
                // MCUControlSurface.MCU_VSELECT1 + i, new ButtonRowModeCommand<> (0, i, this.model,
                // surface), index);
                // this.setupButton (ButtonID.get (TriggerCommandID.ROW2_1, i),
                // MCUControlSurface.MCU_ARM1 + i, new ButtonRowModeCommand<> (1, i, this.model,
                // surface), index);
                // this.setupButton (ButtonID.get (TriggerCommandID.ROW3_1, i),
                // MCUControlSurface.MCU_SOLO1 + i, new ButtonRowModeCommand<> (2, i, this.model,
                // surface), index);
                // this.setupButton (ButtonID.get (TriggerCommandID.ROW4_1, i),
                // MCUControlSurface.MCU_MUTE1 + i, new ButtonRowModeCommand<> (3, i, this.model,
                // surface), index);
            }

            viewManager.registerPitchbendCommand (new PitchbendVolumeCommand (this.model, surface));
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void registerContinuousCommands ()
    {
        MCUControlSurface surface = this.getSurface ();
        ViewManager viewManager = surface.getViewManager ();
        viewManager.registerContinuousCommand (ContinuousCommandID.PLAY_POSITION, new PlayPositionTempoCommand (this.model, surface));
        surface.assignContinuousCommand (1, MCUControlSurface.MCU_CC_JOG, ContinuousCommandID.PLAY_POSITION);

        for (int index = 0; index < this.numMCUDevices; index++)
        {
            surface = this.getSurface (index);
            viewManager = surface.getViewManager ();
            for (int i = 0; i < 8; i++)
            {
                final ContinuousCommandID commandID = ContinuousCommandID.get (ContinuousCommandID.KNOB1, i);
                viewManager.registerContinuousCommand (commandID, new KnobRowModeCommand<> (i, this.model, surface));
                surface.assignContinuousCommand (1, MCUControlSurface.MCU_CC_VPOT1 + i, commandID);
            }
        }
    }


    /** {@inheritDoc} */
    @Override
    public void startup ()
    {
        for (int index = 0; index < this.numMCUDevices; index++)
        {
            final MCUControlSurface surface = this.getSurface (index);
            surface.switchVuMode (MCUControlSurface.VUMODE_LED);

            surface.getViewManager ().setActiveView (Views.CONTROL);
            surface.getModeManager ().setActiveMode (Modes.PAN);
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void updateButtons ()
    {
        final MCUControlSurface surface = this.getSurface ();
        final Modes mode = surface.getModeManager ().getActiveOrTempModeId ();
        if (mode == null)
            return;

        final boolean isShift = surface.isShiftPressed ();

        this.updateVUandFaders (isShift);
        this.updateSegmentDisplay ();

        // Set button states
        final ITransport t = this.model.getTransport ();
        final boolean isFlipRecord = this.configuration.isFlipRecord ();
        final boolean isRecordShifted = isShift && !isFlipRecord || !isShift && isFlipRecord;

        final boolean isTrackOn = Modes.TRACK.equals (mode) || Modes.VOLUME.equals (mode);
        final boolean isPanOn = Modes.PAN.equals (mode);
        final boolean isSendOn = mode.ordinal () >= Modes.SEND1.ordinal () && mode.ordinal () <= Modes.SEND8.ordinal ();
        final boolean isDeviceOn = Modes.DEVICE_PARAMS.equals (mode);

        // final boolean isLEDOn = surface.isPressed (MCUControlSurface.MCU_OPTION) ?
        // this.model.isCursorTrackPinned () : isTrackOn;
        // surface.updateTrigger (MCUControlSurface.MCU_MODE_IO, isLEDOn ? MCU_BUTTON_STATE_ON :
        // MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_MODE_PAN, isPanOn ? MCU_BUTTON_STATE_ON :
        // MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_MODE_SENDS, isSendOn ? MCU_BUTTON_STATE_ON :
        // MCU_BUTTON_STATE_OFF);
        //
        // final ICursorDevice cursorDevice = this.model.getCursorDevice ();
        // final boolean isOn = surface.isPressed (MCUControlSurface.MCU_OPTION) ?
        // cursorDevice.isPinned () : isDeviceOn;
        //
        // surface.updateTrigger (MCUControlSurface.MCU_MODE_PLUGIN, isOn ? MCU_BUTTON_STATE_ON :
        // MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_USER, Modes.BROWSER.equals (mode) ?
        // MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF);
        //
        // final String automationWriteMode = t.getAutomationWriteMode ();
        // final boolean writingArrangerAutomation = t.isWritingArrangerAutomation ();
        //
        // surface.updateTrigger (MCUControlSurface.MCU_F6, t.isPunchInEnabled () ?
        // MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_F7, t.isPunchOutEnabled () ?
        // MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF);
        //
        // surface.updateTrigger (MCUControlSurface.MCU_READ, !writingArrangerAutomation ?
        // MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF);
        // final int writeState = writingArrangerAutomation &&
        // TransportConstants.AUTOMATION_MODES_VALUES[2].equals (automationWriteMode) ?
        // MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF;
        // surface.updateTrigger (MCUControlSurface.MCU_WRITE, writeState);
        // surface.updateTrigger (MCUControlSurface.MCU_GROUP, writeState);
        // surface.updateTrigger (MCUControlSurface.MCU_TRIM, t.isWritingClipLauncherAutomation () ?
        // MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_TOUCH, writingArrangerAutomation &&
        // TransportConstants.AUTOMATION_MODES_VALUES[1].equals (automationWriteMode) ?
        // MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_LATCH, writingArrangerAutomation &&
        // TransportConstants.AUTOMATION_MODES_VALUES[0].equals (automationWriteMode) ?
        // MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF);
        //
        // surface.updateTrigger (MCUControlSurface.MCU_REWIND, ((WindCommand<?, ?>)
        // surface.getButton (ButtonID.REWIND)).isRewinding () ? MCU_BUTTON_STATE_ON :
        // MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_FORWARD, ((WindCommand<?, ?>)
        // surface.getButton (ButtonID.FORWARD)).isForwarding () ? MCU_BUTTON_STATE_ON :
        // MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_REPEAT, t.isLoop () ? MCU_BUTTON_STATE_ON :
        // MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_STOP, !t.isPlaying () ? MCU_BUTTON_STATE_ON
        // : MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_PLAY, t.isPlaying () ? MCU_BUTTON_STATE_ON :
        // MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_RECORD, isRecordShifted ?
        // t.isLauncherOverdub () ? MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF : t.isRecording () ?
        // MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF);
        //
        // surface.updateTrigger (MCUControlSurface.MCU_NAME_VALUE, surface.getConfiguration
        // ().isDisplayTrackNames () ? MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_ZOOM, surface.getConfiguration
        // ().isZoomState () ? MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_SCRUB, surface.getModeManager
        // ().isActiveOrTempMode (Modes.DEVICE_PARAMS) ? MCU_BUTTON_STATE_ON :
        // MCU_BUTTON_STATE_OFF);
        //
        // surface.updateTrigger (MCUControlSurface.MCU_MIDI_TRACKS, MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_INPUTS, MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_AUDIO_TRACKS, surface.isShiftPressed () &&
        // cursorDevice.isWindowOpen () ? MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_AUDIO_INSTR, MCU_BUTTON_STATE_OFF);
        //
        // surface.updateTrigger (MCUControlSurface.MCU_CLICK, (isShift ? t.isMetronomeTicksOn () :
        // t.isMetronomeOn ()) ? MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_SOLO, this.model.getGroove ().getParameters
        // ()[0].getValue () > 0 ? MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_REPLACE, (isShift ? t.isLauncherOverdub () :
        // t.isArrangerOverdub ()) ? MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_FLIP, this.model.isEffectTrackBankActive ()
        // ? MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF);
        //
        // final boolean displayTicks = this.configuration.isDisplayTicks ();
        // surface.updateTrigger (MCUControlSurface.MCU_SMPTE_BEATS, displayTicks ?
        // MCU_BUTTON_STATE_OFF : MCU_BUTTON_STATE_ON);
        // surface.updateTrigger (MCUControlSurface.MCU_SMPTE_LED, displayTicks ?
        // MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF);
        // surface.updateTrigger (MCUControlSurface.MCU_BEATS_LED, displayTicks ?
        // MCU_BUTTON_STATE_OFF : MCU_BUTTON_STATE_ON);
        //
        // surface.updateTrigger (MCUControlSurface.MCU_MARKER, this.model.getArranger
        // ().areCueMarkersVisible () ? MCU_BUTTON_STATE_ON : MCU_BUTTON_STATE_OFF);
    }


    private void updateSegmentDisplay ()
    {
        if (!this.configuration.hasSegmentDisplay ())
            return;

        final ITransport t = this.model.getTransport ();
        String positionText = t.getPositionText ();
        if (this.configuration.isDisplayTicks ())
            positionText += " ";
        else
        {
            String tempoStr = t.formatTempoNoFraction (t.getTempo ());
            final int pos = positionText.lastIndexOf (':');
            if (tempoStr.length () < 3)
                tempoStr = "0" + tempoStr;
            positionText = positionText.substring (0, pos + 1) + tempoStr;
        }

        this.getSurface ().getSegmentDisplay ().setTransportPositionDisplay (positionText);
    }


    private void updateVUandFaders (final boolean isShiftPressed)
    {
        final double upperBound = this.valueChanger.getUpperBound ();
        final boolean enableVUMeters = this.configuration.isEnableVUMeters ();
        final boolean hasMotorFaders = this.configuration.hasMotorFaders ();

        final ITrackBank tb = this.model.getCurrentTrackBank ();
        IMidiOutput output;
        for (int index = 0; index < this.numMCUDevices; index++)
        {
            final MCUControlSurface surface = this.getSurface (index);
            output = surface.getOutput ();
            final int extenderOffset = surface.getExtenderOffset ();
            for (int i = 0; i < 8; i++)
            {
                final int channel = extenderOffset + i;
                final ITrack track = tb.getItem (channel);

                // Update VU LEDs of channel
                if (enableVUMeters)
                {
                    final int vu = track.getVu ();
                    if (vu != this.vuValues[channel])
                    {
                        this.vuValues[channel] = vu;
                        final int scaledValue = (int) Math.round (vu * 12 / upperBound);
                        output.sendChannelAftertouch (0x10 * i + scaledValue, 0);
                    }
                }

                // Update motor fader of channel
                if (hasMotorFaders)
                    this.updateFaders (output, i, channel, track);
            }
        }

        final IMasterTrack masterTrack = this.model.getMasterTrack ();

        final MCUControlSurface surface = this.getSurface ();
        output = surface.getOutput ();

        // Stereo VU of master channel
        if (enableVUMeters)
        {
            int vu = masterTrack.getVuLeft ();
            if (vu != this.masterVuValues[0])
            {
                this.masterVuValues[0] = vu;
                final int scaledValue = (int) Math.round (vu * 12 / upperBound);
                output.sendChannelAftertouch (1, scaledValue, 0);
            }

            vu = masterTrack.getVuRight ();
            if (vu != this.masterVuValues[1])
            {
                this.masterVuValues[1] = vu;
                final int scaledValue = (int) Math.round (vu * 12 / upperBound);
                output.sendChannelAftertouch (1, 0x10 + scaledValue, 0);
            }
        }

        // Update motor fader of master channel
        if (hasMotorFaders)
        {
            final int volume = isShiftPressed ? this.model.getTransport ().getMetronomeVolume () : masterTrack.getVolume ();
            if (volume != this.masterFaderValue)
            {
                this.masterFaderValue = volume;
                output.sendPitchbend (8, volume % 127, volume / 127);
            }
        }
    }


    private void updateFaders (final IMidiOutput output, final int index, final int channel, final ITrack track)
    {
        int value = track.getVolume ();

        if (this.configuration.useFadersAsKnobs ())
        {
            final ModeManager modeManager = this.getSurface ().getModeManager ();
            if (modeManager.isActiveOrTempMode (Modes.VOLUME))
                value = track.getVolume ();
            else if (modeManager.isActiveOrTempMode (Modes.PAN))
                value = track.getPan ();
            else if (modeManager.isActiveOrTempMode (Modes.TRACK))
            {
                final ITrack selectedTrack = this.model.getSelectedTrack ();
                if (selectedTrack == null)
                    value = 0;
                else
                {
                    switch (index)
                    {
                        case 0:
                            value = selectedTrack.getVolume ();
                            break;
                        case 1:
                            value = selectedTrack.getPan ();
                            break;
                        default:
                            final boolean effectTrackBankActive = this.model.isEffectTrackBankActive ();
                            if (index == 2)
                            {
                                if (this.configuration.isDisplayCrossfader ())
                                {
                                    final int crossfadeMode = selectedTrack.getCrossfadeModeAsNumber ();
                                    value = crossfadeMode == 2 ? this.valueChanger.getUpperBound () : crossfadeMode == 1 ? this.valueChanger.getUpperBound () / 2 : 0;
                                }
                                else if (!effectTrackBankActive)
                                    value = selectedTrack.getSendBank ().getItem (0).getValue ();
                            }
                            else if (!effectTrackBankActive)
                                value = selectedTrack.getSendBank ().getItem (index - (this.configuration.isDisplayCrossfader () ? 3 : 2)).getValue ();
                            break;
                    }
                }
            }
            else if (modeManager.isActiveOrTempMode (Modes.SEND1))
                value = track.getSendBank ().getItem (0).getValue ();
            else if (modeManager.isActiveOrTempMode (Modes.SEND2))
                value = track.getSendBank ().getItem (1).getValue ();
            else if (modeManager.isActiveOrTempMode (Modes.SEND3))
                value = track.getSendBank ().getItem (2).getValue ();
            else if (modeManager.isActiveOrTempMode (Modes.SEND4))
                value = track.getSendBank ().getItem (3).getValue ();
            else if (modeManager.isActiveOrTempMode (Modes.SEND5))
                value = track.getSendBank ().getItem (4).getValue ();
            else if (modeManager.isActiveOrTempMode (Modes.SEND6))
                value = track.getSendBank ().getItem (5).getValue ();
            else if (modeManager.isActiveOrTempMode (Modes.SEND7))
                value = track.getSendBank ().getItem (6).getValue ();
            else if (modeManager.isActiveOrTempMode (Modes.SEND8))
                value = track.getSendBank ().getItem (7).getValue ();
            else if (modeManager.isActiveOrTempMode (Modes.DEVICE_PARAMS))
                value = this.model.getCursorDevice ().getParameterBank ().getItem (channel).getValue ();
        }

        if (value != this.faderValues[channel])
        {
            this.faderValues[channel] = value;
            output.sendPitchbend (index, value % 127, value / 127);
        }
    }


    private void updateMode (final Modes mode)
    {
        if (mode == null)
            return;

        this.updateIndication (mode);
        if (this.configuration.hasAssignmentDisplay ())
            this.getSurface ().getSegmentDisplay ().setAssignmentDisplay (MODE_ACRONYMS.get (mode));
    }


    /** {@inheritDoc} */
    @Override
    protected void updateIndication (final Modes mode)
    {
        if (this.currentMode != null && this.currentMode.equals (mode))
            return;
        this.currentMode = mode;

        final ITrackBank tb = this.model.getTrackBank ();
        final ITrackBank tbe = this.model.getEffectTrackBank ();
        final boolean isEffect = this.model.isEffectTrackBankActive ();
        final boolean isPan = Modes.PAN.equals (mode);
        final boolean isTrack = Modes.TRACK.equals (mode);
        final boolean isVolume = Modes.VOLUME.equals (mode);
        final boolean isDevice = Modes.DEVICE_PARAMS.equals (mode);

        tb.setIndication (!isEffect);
        if (tbe != null)
            tbe.setIndication (isEffect);

        final ICursorDevice cursorDevice = this.model.getCursorDevice ();
        final ITrack selectedTrack = tb.getSelectedItem ();
        for (int i = 0; i < tb.getPageSize (); i++)
        {
            final boolean hasTrackSel = selectedTrack != null && selectedTrack.getIndex () == i && isTrack;
            final ITrack track = tb.getItem (i);
            track.setVolumeIndication (!isEffect && (isVolume || hasTrackSel));
            track.setPanIndication (!isEffect && (isPan || hasTrackSel));

            final ISendBank sendBank = track.getSendBank ();
            for (int j = 0; j < sendBank.getPageSize (); j++)
                sendBank.getItem (j).setIndication (!isEffect && (mode.ordinal () - Modes.SEND1.ordinal () == j || hasTrackSel));

            if (tbe != null)
            {
                final ITrack fxTrack = tbe.getItem (i);
                fxTrack.setVolumeIndication (isEffect);
                fxTrack.setPanIndication (isEffect && isPan);
            }
        }

        final IParameterBank parameterBank = cursorDevice.getParameterBank ();
        for (int i = 0; i < parameterBank.getPageSize (); i++)
            parameterBank.getItem (i).setIndication (isDevice);
    }


    /**
     * Handle a track selection change.
     *
     * @param isSelected Has the track been selected?
     */
    private void handleTrackChange (final boolean isSelected)
    {
        if (!isSelected)
            return;

        final ModeManager modeManager = this.getSurface ().getModeManager ();
        if (modeManager.isActiveOrTempMode (Modes.MASTER))
            modeManager.setActiveMode (Modes.TRACK);
    }
}
