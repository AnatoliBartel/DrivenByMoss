// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.hui;

import de.mossgrabers.controller.hui.command.trigger.AssignableCommand;
import de.mossgrabers.controller.hui.command.trigger.FaderTouchCommand;
import de.mossgrabers.controller.hui.command.trigger.ZoomAndKeysCursorCommand;
import de.mossgrabers.controller.hui.command.trigger.ZoomCommand;
import de.mossgrabers.controller.hui.controller.HUIControlSurface;
import de.mossgrabers.controller.hui.controller.HUIDisplay;
import de.mossgrabers.controller.hui.controller.HUIMainDisplay;
import de.mossgrabers.controller.hui.controller.HUISegmentDisplay;
import de.mossgrabers.controller.hui.mode.track.PanMode;
import de.mossgrabers.controller.hui.mode.track.SendMode;
import de.mossgrabers.controller.hui.mode.track.VolumeMode;
import de.mossgrabers.framework.command.core.NopCommand;
import de.mossgrabers.framework.command.trigger.AutomationCommand;
import de.mossgrabers.framework.command.trigger.application.PaneCommand;
import de.mossgrabers.framework.command.trigger.application.SaveCommand;
import de.mossgrabers.framework.command.trigger.application.UndoCommand;
import de.mossgrabers.framework.command.trigger.mode.ButtonRowModeCommand;
import de.mossgrabers.framework.command.trigger.mode.ModeCursorCommand;
import de.mossgrabers.framework.command.trigger.mode.ModeCursorCommand.Direction;
import de.mossgrabers.framework.command.trigger.mode.ModeSelectCommand;
import de.mossgrabers.framework.command.trigger.track.MuteCommand;
import de.mossgrabers.framework.command.trigger.track.RecArmCommand;
import de.mossgrabers.framework.command.trigger.track.SelectCommand;
import de.mossgrabers.framework.command.trigger.track.SoloCommand;
import de.mossgrabers.framework.command.trigger.transport.MetronomeCommand;
import de.mossgrabers.framework.command.trigger.transport.PlayCommand;
import de.mossgrabers.framework.command.trigger.transport.RecordCommand;
import de.mossgrabers.framework.command.trigger.transport.StopCommand;
import de.mossgrabers.framework.command.trigger.transport.TapTempoCommand;
import de.mossgrabers.framework.command.trigger.transport.ToggleLoopCommand;
import de.mossgrabers.framework.command.trigger.transport.WindCommand;
import de.mossgrabers.framework.configuration.AbstractConfiguration;
import de.mossgrabers.framework.configuration.ISettingsUI;
import de.mossgrabers.framework.controller.AbstractControllerSetup;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.ISetupFactory;
import de.mossgrabers.framework.controller.Relative4ValueChanger;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.daw.ICursorDevice;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.IParameterBank;
import de.mossgrabers.framework.daw.ISendBank;
import de.mossgrabers.framework.daw.ITrackBank;
import de.mossgrabers.framework.daw.ITransport;
import de.mossgrabers.framework.daw.ModelSetup;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.midi.IMidiAccess;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.daw.midi.IMidiOutput;
import de.mossgrabers.framework.mode.Mode;
import de.mossgrabers.framework.mode.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.view.ControlOnlyView;
import de.mossgrabers.framework.view.Views;

import java.util.Arrays;


/**
 * Support for the Mackie HUI protocol.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class HUIControllerSetup extends AbstractControllerSetup<HUIControlSurface, HUIConfiguration>
{
    /** State for button LED on. */
    public static final int HUI_BUTTON_STATE_ON  = 127;
    /** State for button LED off. */
    public static final int HUI_BUTTON_STATE_OFF = 0;

    private final int []    vuValuesL            = new int [8];
    private final int []    vuValuesR            = new int [8];
    private final int []    faderValues          = new int [36];


    /**
     * Constructor.
     *
     * @param host The DAW host
     * @param factory The factory
     * @param globalSettings The global settings
     * @param documentSettings The document (project) specific settings
     */
    public HUIControllerSetup (final IHost host, final ISetupFactory factory, final ISettingsUI globalSettings, final ISettingsUI documentSettings)
    {
        super (factory, host, globalSettings, documentSettings);

        Arrays.fill (this.vuValuesL, -1);
        Arrays.fill (this.vuValuesR, -1);
        Arrays.fill (this.faderValues, -1);

        this.colorManager = new ColorManager ();
        this.valueChanger = new Relative4ValueChanger (16384, 100, 10);
        this.configuration = new HUIConfiguration (host, this.valueChanger);
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
        final ModelSetup ms = new ModelSetup ();
        ms.setHasFullFlatTrackList (true);
        ms.setNumTracks (8);
        ms.setNumSends (5);
        ms.setNumScenes (0);
        ms.setNumFilterColumnEntries (8);
        ms.setNumResults (8);
        ms.setNumParams (8);
        ms.setNumDeviceLayers (0);
        ms.setNumDrumPadLayers (0);
        ms.setNumMarkers (8);
        this.model = this.factory.createModel (this.colorManager, this.valueChanger, this.scales, ms);

        this.model.getTrackBank ().setIndication (true);
    }


    /** {@inheritDoc} */
    @Override
    protected void createSurface ()
    {
        final IMidiAccess midiAccess = this.factory.createMidiAccess ();

        final IMidiOutput output = midiAccess.createOutput ();
        final IMidiInput input = midiAccess.createInput (null);
        final HUIControlSurface surface = new HUIControlSurface (this.host, this.colorManager, this.configuration, output, input, this.model);
        this.surfaces.add (surface);
        surface.addTextDisplay (new HUIDisplay (this.host, output));
        surface.addTextDisplay (new HUIMainDisplay (this.host, output));
        surface.addTextDisplay (new HUISegmentDisplay (output));
        surface.getModeManager ().setDefaultMode (Modes.VOLUME);
    }


    /** {@inheritDoc} */
    @Override
    protected void createModes ()
    {
        final HUIControlSurface surface = this.getSurface ();
        final ModeManager modeManager = surface.getModeManager ();

        modeManager.registerMode (Modes.VOLUME, new VolumeMode (surface, this.model));
        modeManager.registerMode (Modes.PAN, new PanMode (surface, this.model));
        for (int i = 0; i < 5; i++)
            modeManager.registerMode (Modes.get (Modes.SEND1, i), new SendMode (i, surface, this.model));
    }


    /** {@inheritDoc} */
    @Override
    protected void createObservers ()
    {
        final HUIControlSurface surface = this.getSurface ();
        surface.getModeManager ().addModeListener ( (oldMode, newMode) -> {
            surface.getModeManager ().setActiveMode (newMode);
            this.updateMode (null);
            this.updateMode (newMode);
        });

        this.configuration.addSettingObserver (AbstractConfiguration.ENABLE_VU_METERS, () -> {
            final Mode activeMode = surface.getModeManager ().getActiveOrTempMode ();
            if (activeMode != null)
                activeMode.updateDisplay ();
            ((HUIDisplay) surface.getDisplay ()).forceFlush ();
        });
    }


    /** {@inheritDoc} */
    @Override
    protected void createViews ()
    {
        final HUIControlSurface surface = this.getSurface ();
        surface.getViewManager ().registerView (Views.CONTROL, new ControlOnlyView<> (surface, this.model));
    }


    /** {@inheritDoc} */
    @Override
    protected void registerTriggerCommands ()
    {
        // Assignments to the main device
        final HUIControlSurface surface = this.getSurface ();

        // Channel commands
        for (int i = 0; i < 8; i++)
        {
            this.addButton (ButtonID.get (ButtonID.FADER_TOUCH_1, i), "Fader " + (i + 1), new FaderTouchCommand (i, this.model, surface), HUIControlSurface.HUI_FADER1 + i * 8);
            this.addButton (ButtonID.get (ButtonID.ROW_SELECT_1, i), "Select " + (i + 1), new SelectCommand<> (i, this.model, surface), HUIControlSurface.HUI_SELECT1 + i * 8);
            this.addButton (ButtonID.get (ButtonID.ROW4_1, i), "Mute " + (i + 1), new MuteCommand<> (i, this.model, surface), HUIControlSurface.HUI_MUTE1 + i * 8);
            this.addButton (ButtonID.get (ButtonID.ROW3_1, i), "Solo " + (i + 1), new SoloCommand<> (i, this.model, surface), HUIControlSurface.HUI_SOLO1 + i * 8);
            // HUI_AUTO1, not supported
            this.addButton (ButtonID.get (ButtonID.ROW1_1, i), "VSelect " + (i + 1), new ButtonRowModeCommand<> (0, i, this.model, surface), HUIControlSurface.HUI_VSELECT1 + i * 8);
            // HUI_INSERT1, not supported
            this.addButton (ButtonID.get (ButtonID.ROW2_1, i), "Arm " + (i + 1), new RecArmCommand<> (i, this.model, surface), HUIControlSurface.HUI_ARM1 + i * 8);
        }

        // Key commands
        this.addButton (ButtonID.CONTROL, "Control", NopCommand.INSTANCE, HUIControlSurface.HUI_KEY_CTRL_CLT);
        this.addButton (ButtonID.SHIFT, "Shift", NopCommand.INSTANCE, HUIControlSurface.HUI_KEY_SHIFT_AD);
        // HUI_KEY_EDITMODE, not supported
        this.addButton (ButtonID.UNDO, "Undo", new UndoCommand<> (this.model, surface), HUIControlSurface.HUI_KEY_UNDO);
        this.addButton (ButtonID.ALT, "Alt", NopCommand.INSTANCE, HUIControlSurface.HUI_KEY_ALT_FINE);
        this.addButton (ButtonID.SELECT, "Select", NopCommand.INSTANCE, HUIControlSurface.HUI_KEY_OPTION_A);
        // HUI_KEY_EDITTOOL, not supported
        this.addButton (ButtonID.SAVE, "Save", new SaveCommand<> (this.model, surface), HUIControlSurface.HUI_KEY_SAVE);

        // Window commands
        this.addButton (ButtonID.MIXER, "Mixer", new PaneCommand<> (PaneCommand.Panels.MIXER, this.model, surface), HUIControlSurface.HUI_WINDOW_MIX);
        this.addButton (ButtonID.NOTE_EDITOR, "Note", new PaneCommand<> (PaneCommand.Panels.NOTE, this.model, surface), HUIControlSurface.HUI_WINDOW_EDIT);
        this.addButton (ButtonID.AUTOMATION_EDITOR, "Automation", new PaneCommand<> (PaneCommand.Panels.AUTOMATION, this.model, surface), HUIControlSurface.HUI_WINDOW_TRANSPRT);
        // HUI_WINDOW_MEM_LOC, not supported
        this.addButton (ButtonID.TOGGLE_DEVICE, "Device", new PaneCommand<> (PaneCommand.Panels.DEVICE, this.model, surface), HUIControlSurface.HUI_WINDOW_STATUS);
        // HUI_WINDOW_ALT, not supported

        // Bank navigation
        this.addButton (ButtonID.MOVE_TRACK_LEFT, "Channel Left", new ModeCursorCommand<> (Direction.LEFT, this.model, surface), HUIControlSurface.HUI_CHANL_LEFT);
        this.addButton (ButtonID.MOVE_BANK_LEFT, "Bank Left", new ModeCursorCommand<> (Direction.DOWN, this.model, surface), HUIControlSurface.HUI_BANK_LEFT);
        this.addButton (ButtonID.MOVE_TRACK_RIGHT, "Channel Right", new ModeCursorCommand<> (Direction.RIGHT, this.model, surface), HUIControlSurface.HUI_CHANL_RIGHT);
        this.addButton (ButtonID.MOVE_BANK_RIGHT, "Bank Right", new ModeCursorCommand<> (Direction.UP, this.model, surface), HUIControlSurface.HUI_BANK_RIGHT);

        // Assignment (mode selection)
        // HUI_ASSIGN1_OUTPUT, not supported
        // HUI_ASSIGN1_INPUT, not supported
        this.addButton (ButtonID.PAN_SEND, "Panorama", new ModeSelectCommand<> (this.model, surface, Modes.PAN), HUIControlSurface.HUI_ASSIGN1_PAN);
        this.addButton (ButtonID.SEND1, "Send 1", new ModeSelectCommand<> (this.model, surface, Modes.SEND1), HUIControlSurface.HUI_ASSIGN1_SEND_A);
        this.addButton (ButtonID.SEND2, "Send 2", new ModeSelectCommand<> (this.model, surface, Modes.SEND2), HUIControlSurface.HUI_ASSIGN1_SEND_B);
        this.addButton (ButtonID.SEND3, "Send 3", new ModeSelectCommand<> (this.model, surface, Modes.SEND3), HUIControlSurface.HUI_ASSIGN1_SEND_C);
        this.addButton (ButtonID.SEND4, "Send 4", new ModeSelectCommand<> (this.model, surface, Modes.SEND4), HUIControlSurface.HUI_ASSIGN1_SEND_D);
        this.addButton (ButtonID.SEND5, "Send 5", new ModeSelectCommand<> (this.model, surface, Modes.SEND5), HUIControlSurface.HUI_ASSIGN1_SEND_E);

        // Assignment 2
        // HUI_ASSIGN2_ASSIGN, not supported
        // HUI_ASSIGN2_DEFAULT, not supported
        // HUI_ASSIGN2_SUSPEND, not supported
        // HUI_ASSIGN2_SHIFT, not supported
        // HUI_ASSIGN2_MUTE, not supported
        // HUI_ASSIGN2_BYPASS, not supported
        // HUI_ASSIGN2_RECRDYAL, not supported

        // Cursor arrows
        this.addButton (ButtonID.ARROW_DOWN, "Down", new ZoomAndKeysCursorCommand (Direction.DOWN, this.model, surface), HUIControlSurface.HUI_CURSOR_DOWN);
        this.addButton (ButtonID.ARROW_LEFT, "Left", new ZoomAndKeysCursorCommand (Direction.LEFT, this.model, surface), HUIControlSurface.HUI_CURSOR_LEFT);
        this.addButton (ButtonID.ZOOM, "Toggle", new ZoomCommand (this.model, surface), HUIControlSurface.HUI_CURSOR_MODE);
        this.addButton (ButtonID.ARROW_RIGHT, "Right", new ZoomAndKeysCursorCommand (Direction.RIGHT, this.model, surface), HUIControlSurface.HUI_CURSOR_RIGHT);
        this.addButton (ButtonID.ARROW_UP, "Up", new ZoomAndKeysCursorCommand (Direction.UP, this.model, surface), HUIControlSurface.HUI_CURSOR_UP);
        // HUI_WHEEL_SCRUB, not supported
        // HUI_WHEEL_SHUTTLE, not supported

        // Navigation
        // HUI_TRANSPORT_TALKBACK, not supported
        this.addButton (ButtonID.REWIND, "<<", new WindCommand<> (this.model, surface, false), HUIControlSurface.HUI_TRANSPORT_REWIND);
        this.addButton (ButtonID.FORWARD, ">>", new WindCommand<> (this.model, surface, true), HUIControlSurface.HUI_TRANSPORT_FAST_FWD);
        this.addButton (ButtonID.STOP, "Stop", new StopCommand<> (this.model, surface), HUIControlSurface.HUI_TRANSPORT_STOP);
        this.addButton (ButtonID.PLAY, "Play", new PlayCommand<> (this.model, surface), HUIControlSurface.HUI_TRANSPORT_PLAY);
        this.addButton (ButtonID.RECORD, "Record", new RecordCommand<> (this.model, surface), HUIControlSurface.HUI_TRANSPORT_RECORD);
        // HUI_TRANSPORT_RETURN_TO_ZERO, not supported
        // HUI_TRANSPORT_TO_END, not supported
        // HUI_TRANSPORT_ON_LINE, not supported
        this.addButton (ButtonID.LOOP, "Loop", new ToggleLoopCommand<> (this.model, surface), HUIControlSurface.HUI_TRANSPORT_LOOP);
        // HUI_TRANSPORT_QICK_PUNCH, not supported
        // HUI_TRANSPORT_AUDITION, not supported
        this.addButton (ButtonID.METRONOME, "Metronome", new MetronomeCommand<> (this.model, surface), HUIControlSurface.HUI_TRANSPORT_PRE);
        // HUI_TRANSPORT_IN, not supported
        // HUI_TRANSPORT_OUT, not supported
        this.addButton (ButtonID.TAP_TEMPO, "Tap Tempo", new TapTempoCommand<> (this.model, surface), HUIControlSurface.HUI_TRANSPORT_POST);

        // Control room
        // HUI_CONTROL_ROOM_INPUT_3, not supported
        // HUI_CONTROL_ROOM_INPUT_2, not supported
        // HUI_CONTROL_ROOM_INPUT_1, not supported
        // HUI_CONTROL_ROOM_MUTE, not supported
        // HUI_CONTROL_ROOM_DISCRETE, not supported
        // HUI_CONTROL_ROOM_OUTPUT_3, not supported
        // HUI_CONTROL_ROOM_OUTPUT_2, not supported
        // HUI_CONTROL_ROOM_OUTPUT_1, not supported
        // HUI_CONTROL_ROOM_DIM, not supported
        // HUI_CONTROL_ROOM_MONO, not supported

        // Num-block
        // HUI_NUM_0, not supported
        // HUI_NUM_1, not supported
        // HUI_NUM_4, not supported
        // HUI_NUM_2, not supported
        // HUI_NUM_5, not supported
        // HUI_NUM_DOT, not supported
        // HUI_NUM_3, not supported
        // HUI_NUM_6, not supported
        // HUI_NUM_ENTER, not supported
        // HUI_NUM_PLUS, not supported
        // HUI_NUM_7, not supported
        // HUI_NUM_8, not supported
        // HUI_NUM_9, not supported
        // HUI_NUM_MINUS, not supported
        // HUI_NUM_CLR, not supported
        // HUI_NUM_SET, not supported
        // HUI_NUM_DIV, not supported
        // HUI_NUM_MULT , not supported

        // Auto enable
        // HUI_AUTO_ENABLE_PLUG_IN, not supported
        // HUI_AUTO_ENABLE_PAN, not supported
        // HUI_AUTO_ENABLE_FADER, not supported
        // HUI_AUTO_ENABLE_SENDMUTE, not supported
        // HUI_AUTO_ENABLE_SEND, not supported
        // HUI_AUTO_ENABLE_MUTE, not supported

        // Automation modes
        this.addButton (ButtonID.AUTOMATION_TRIM, "Trim", new AutomationCommand<> (2, this.model, surface), HUIControlSurface.HUI_AUTO_MODE_TRIM);
        this.addButton (ButtonID.AUTOMATION_LATCH, "Latch", new AutomationCommand<> (4, this.model, surface), HUIControlSurface.HUI_AUTO_MODE_LATCH);
        this.addButton (ButtonID.AUTOMATION_READ, "Read", new AutomationCommand<> (0, this.model, surface), HUIControlSurface.HUI_AUTO_MODE_READ);
        this.addButton (ButtonID.AUTOMATION_OFF, "Read", new AutomationCommand<> (0, this.model, surface), HUIControlSurface.HUI_AUTO_MODE_OFF);
        this.addButton (ButtonID.AUTOMATION_WRITE, "Write", new AutomationCommand<> (1, this.model, surface), HUIControlSurface.HUI_AUTO_MODE_WRITE);
        this.addButton (ButtonID.AUTOMATION_TOUCH, "Touch", new AutomationCommand<> (3, this.model, surface), HUIControlSurface.HUI_AUTO_MODE_TOUCH);

        // Status
        // HUI_STATUS_PHASE, not supported
        // HUI_STATUS_MONITOR, not supported
        // HUI_STATUS_AUTO, not supported
        // HUI_STATUS_SUSPEND, not supported
        // HUI_STATUS_CREATE, not supported
        // HUI_STATUS_GROUP, not supported

        // Edit
        // HUI_EDIT_PASTE, not supported
        // HUI_EDIT_CUT, not supported
        // HUI_EDIT_CAPTURE, not supported
        // HUI_EDIT_DELETE, not supported
        // HUI_EDIT_COPY, not supported
        // HUI_EDIT_SEPARATE, not supported

        // Function keys
        this.addButton (ButtonID.F1, "F1", new AssignableCommand (2, this.model, surface), HUIControlSurface.HUI_F1);
        this.addButton (ButtonID.F2, "F2", new AssignableCommand (3, this.model, surface), HUIControlSurface.HUI_F2);
        this.addButton (ButtonID.F3, "F3", new AssignableCommand (4, this.model, surface), HUIControlSurface.HUI_F3);
        this.addButton (ButtonID.F4, "F4", new AssignableCommand (5, this.model, surface), HUIControlSurface.HUI_F4);
        this.addButton (ButtonID.F5, "F5", new AssignableCommand (6, this.model, surface), HUIControlSurface.HUI_F5);
        this.addButton (ButtonID.F6, "F6", new AssignableCommand (7, this.model, surface), HUIControlSurface.HUI_F6);
        this.addButton (ButtonID.F7, "F7", new AssignableCommand (8, this.model, surface), HUIControlSurface.HUI_F7);
        this.addButton (ButtonID.F8, "F8", new AssignableCommand (9, this.model, surface), HUIControlSurface.HUI_F8_ESC);

        // DSP Edit
        // HUI_DSP_EDIT_INS_PARA, not supported
        // HUI_DSP_EDIT_ASSIGN, not supported
        // HUI_DSP_EDIT_SELECT_1, not supported
        // HUI_DSP_EDIT_SELECT_2, not supported
        // HUI_DSP_EDIT_SELECT_3, not supported
        // HUI_DSP_EDIT_SELECT_4, not supported
        // HUI_DSP_EDIT_BYPASS, not supported
        // HUI_DSP_EDIT_COMPARE, not supported

        // Footswitches
        this.addButton (ButtonID.FOOTSWITCH1, "Footswitch 1", new AssignableCommand (0, this.model, surface), HUIControlSurface.HUI_FS_RLAY1);
        this.addButton (ButtonID.FOOTSWITCH2, "Footswitch 2", new AssignableCommand (1, this.model, surface), HUIControlSurface.HUI_FS_RLAY2);
    }


    /** {@inheritDoc} */
    @Override
    public void startup ()
    {
        final HUIControlSurface surface = this.getSurface ();

        surface.getViewManager ().setActiveView (Views.CONTROL);
        surface.getModeManager ().setActiveMode (Modes.PAN);

        this.sendPing ();
    }


    private void sendPing ()
    {
        this.getSurface ().getOutput ().sendNote (0, 0);
        this.host.scheduleTask (this::sendPing, 1000);
    }

    // /** {@inheritDoc} */
    // @Override
    // protected void updateButtons ()
    // {
    // final HUIControlSurface surface = this.getSurface ();
    // final Modes mode = surface.getModeManager ().getActiveOrTempModeId ();
    // if (mode == null)
    // return;
    //
    // this.updateVUandFaders ();
    // this.updateSegmentDisplay ();
    //
    // TODO Set button states
    // final ITransport t = this.model.getTransport ();
    // surface.updateTrigger (HUIControlSurface.HUI_ASSIGN1_PAN, Modes.PAN.equals (mode) ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_ASSIGN1_SEND_A, Modes.SEND1.equals (mode) ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_ASSIGN1_SEND_B, Modes.SEND2.equals (mode) ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_ASSIGN1_SEND_C, Modes.SEND3.equals (mode) ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_ASSIGN1_SEND_D, Modes.SEND4.equals (mode) ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_ASSIGN1_SEND_E, Modes.SEND5.equals (mode) ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    //
    // final String automationWriteMode = t.getAutomationWriteMode ();
    // final boolean writingArrangerAutomation = t.isWritingArrangerAutomation ();
    //
    // surface.updateTrigger (HUIControlSurface.HUI_AUTO_MODE_OFF, !writingArrangerAutomation ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_AUTO_MODE_READ, !writingArrangerAutomation ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_AUTO_MODE_WRITE, writingArrangerAutomation
    // && TransportConstants.AUTOMATION_MODES_VALUES[2].equals (automationWriteMode) ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_AUTO_MODE_TRIM,
    // t.isWritingClipLauncherAutomation () ? HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_AUTO_MODE_TOUCH, writingArrangerAutomation
    // && TransportConstants.AUTOMATION_MODES_VALUES[1].equals (automationWriteMode) ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_AUTO_MODE_LATCH, writingArrangerAutomation
    // && TransportConstants.AUTOMATION_MODES_VALUES[0].equals (automationWriteMode) ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    //
    // surface.updateTrigger (HUIControlSurface.HUI_TRANSPORT_REWIND, ((WindCommand<?, ?>)
    // surface.getButton (ButtonID.REWIND)).isRewinding () ? HUI_BUTTON_STATE_ON :
    // HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_TRANSPORT_FAST_FWD, ((WindCommand<?, ?>)
    // surface.getButton (ButtonID.FORWARD)).isForwarding () ? HUI_BUTTON_STATE_ON :
    // HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_TRANSPORT_LOOP, t.isLoop () ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_TRANSPORT_STOP, !t.isPlaying () ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_TRANSPORT_PLAY, t.isPlaying () ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_TRANSPORT_RECORD, t.isRecording () ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    //
    // surface.updateTrigger (HUIControlSurface.HUI_TRANSPORT_PRE, t.isMetronomeOn () ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_TRANSPORT_POST, HUI_BUTTON_STATE_OFF);
    //
    // surface.updateTrigger (HUIControlSurface.HUI_CURSOR_MODE, surface.getConfiguration
    // ().isZoomState () ? HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    //
    // final ITrackBank tb = this.model.getCurrentTrackBank ();
    // for (int i = 0; i < 8; i++)
    // {
    // final ITrack track = tb.getItem (i);
    // final int offset = i * 8;
    // surface.updateTrigger (HUIControlSurface.HUI_SELECT1 + offset, track.isSelected () ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_ARM1 + offset, track.isRecArm () ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_SOLO1 + offset, track.isSolo () ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // surface.updateTrigger (HUIControlSurface.HUI_MUTE1 + offset, track.isMute () ?
    // HUI_BUTTON_STATE_ON : HUI_BUTTON_STATE_OFF);
    // }
    // }


    private void updateSegmentDisplay ()
    {
        if (!this.configuration.hasSegmentDisplay ())
            return;

        final ITransport t = this.model.getTransport ();
        String positionText = t.getPositionText ();
        positionText = positionText.substring (0, positionText.length () - 3);
        this.getSurface ().getSegmentDisplay ().setTransportPositionDisplay (positionText);
    }


    private void updateVUandFaders ()
    {
        final double upperBound = this.valueChanger.getUpperBound ();
        final boolean enableVUMeters = this.configuration.isEnableVUMeters ();
        final boolean hasMotorFaders = this.configuration.hasMotorFaders ();

        final ITrackBank tb = this.model.getCurrentTrackBank ();
        IMidiOutput output;
        final HUIControlSurface surface = this.getSurface ();
        output = surface.getOutput ();
        for (int channel = 0; channel < 8; channel++)
        {
            final ITrack track = tb.getItem (channel);

            // Update VU LEDs of channel
            if (enableVUMeters)
            {
                final int vuLeft = track.getVuLeft ();
                if (vuLeft != this.vuValuesL[channel])
                {
                    this.vuValuesL[channel] = vuLeft;
                    final int scaledValue = (int) Math.floor (vuLeft * 12 / upperBound);
                    output.sendPolyphonicAftertouch (channel, scaledValue);
                }
                final int vuRight = track.getVuRight ();
                if (vuRight != this.vuValuesR[channel])
                {
                    this.vuValuesR[channel] = vuRight;
                    final int scaledValue = (int) Math.floor (vuRight * 12 / upperBound);
                    output.sendPolyphonicAftertouch (0x10 + channel, scaledValue);
                }
            }

            // Update motor fader of channel
            if (hasMotorFaders)
                this.updateFaders (output, channel, track);
        }
    }


    private void updateFaders (final IMidiOutput output, final int channel, final ITrack track)
    {
        final int value = track.getVolume ();
        if (value != this.faderValues[channel])
        {
            this.faderValues[channel] = value;
            output.sendCC (channel, value / 128);
            output.sendCC (0x20 + channel, value % 128);
        }
    }


    private void updateMode (final Modes mode)
    {
        if (mode != null)
            this.updateIndication (mode);
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
        final boolean isPan = Modes.PAN == mode;
        final boolean isTrack = Modes.TRACK == mode;
        final boolean isDevice = Modes.DEVICE_PARAMS == mode;

        tb.setIndication (!isEffect);
        if (tbe != null)
            tbe.setIndication (isEffect);

        final ICursorDevice cursorDevice = this.model.getCursorDevice ();
        final ITrack selectedTrack = tb.getSelectedItem ();
        for (int i = 0; i < tb.getPageSize (); i++)
        {
            final boolean hasTrackSel = selectedTrack != null && selectedTrack.getIndex () == i && isTrack;
            final ITrack track = tb.getItem (i);
            track.setVolumeIndication (!isEffect && (isTrack || hasTrackSel));
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
}
