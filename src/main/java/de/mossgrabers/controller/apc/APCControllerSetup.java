// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.apc;

import de.mossgrabers.controller.apc.command.trigger.APCBrowserCommand;
import de.mossgrabers.controller.apc.command.trigger.APCQuantizeCommand;
import de.mossgrabers.controller.apc.command.trigger.APCRecordCommand;
import de.mossgrabers.controller.apc.command.trigger.SelectTrackSendOrClipLengthCommand;
import de.mossgrabers.controller.apc.command.trigger.SendModeCommand;
import de.mossgrabers.controller.apc.command.trigger.SessionRecordCommand;
import de.mossgrabers.controller.apc.command.trigger.StopAllClipsOrBrowseCommand;
import de.mossgrabers.controller.apc.controller.APCColorManager;
import de.mossgrabers.controller.apc.controller.APCControlSurface;
import de.mossgrabers.controller.apc.mode.BrowserMode;
import de.mossgrabers.controller.apc.mode.PanMode;
import de.mossgrabers.controller.apc.mode.SendMode;
import de.mossgrabers.controller.apc.view.DrumView;
import de.mossgrabers.controller.apc.view.PlayView;
import de.mossgrabers.controller.apc.view.RaindropsView;
import de.mossgrabers.controller.apc.view.SequencerView;
import de.mossgrabers.controller.apc.view.SessionView;
import de.mossgrabers.controller.apc.view.ShiftView;
import de.mossgrabers.framework.command.SceneCommand;
import de.mossgrabers.framework.command.continuous.CrossfaderCommand;
import de.mossgrabers.framework.command.continuous.FaderAbsoluteCommand;
import de.mossgrabers.framework.command.continuous.KnobRowModeCommand;
import de.mossgrabers.framework.command.continuous.MasterFaderAbsoluteCommand;
import de.mossgrabers.framework.command.continuous.PlayPositionCommand;
import de.mossgrabers.framework.command.continuous.TempoCommand;
import de.mossgrabers.framework.command.trigger.application.PaneCommand;
import de.mossgrabers.framework.command.trigger.application.PaneCommand.Panels;
import de.mossgrabers.framework.command.trigger.application.PanelLayoutCommand;
import de.mossgrabers.framework.command.trigger.application.RedoCommand;
import de.mossgrabers.framework.command.trigger.application.UndoCommand;
import de.mossgrabers.framework.command.trigger.clip.NewCommand;
import de.mossgrabers.framework.command.trigger.clip.StopClipCommand;
import de.mossgrabers.framework.command.trigger.device.DeviceLayerLeftCommand;
import de.mossgrabers.framework.command.trigger.device.DeviceLayerRightCommand;
import de.mossgrabers.framework.command.trigger.device.DeviceOnOffCommand;
import de.mossgrabers.framework.command.trigger.device.DeviceParamsKnobRowCommand;
import de.mossgrabers.framework.command.trigger.device.SelectNextDeviceOrParamPageCommand;
import de.mossgrabers.framework.command.trigger.device.SelectPreviousDeviceOrParamPageCommand;
import de.mossgrabers.framework.command.trigger.mode.CursorCommand;
import de.mossgrabers.framework.command.trigger.mode.ModeCursorCommand.Direction;
import de.mossgrabers.framework.command.trigger.mode.ModeSelectCommand;
import de.mossgrabers.framework.command.trigger.track.CrossfadeModeCommand;
import de.mossgrabers.framework.command.trigger.track.MasterCommand;
import de.mossgrabers.framework.command.trigger.track.MuteCommand;
import de.mossgrabers.framework.command.trigger.track.RecArmCommand;
import de.mossgrabers.framework.command.trigger.track.SoloCommand;
import de.mossgrabers.framework.command.trigger.transport.MetronomeCommand;
import de.mossgrabers.framework.command.trigger.transport.PlayCommand;
import de.mossgrabers.framework.command.trigger.transport.StopCommand;
import de.mossgrabers.framework.command.trigger.transport.TapTempoCommand;
import de.mossgrabers.framework.command.trigger.view.ToggleShiftViewCommand;
import de.mossgrabers.framework.configuration.ISettingsUI;
import de.mossgrabers.framework.controller.AbstractControllerSetup;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.ContinuousID;
import de.mossgrabers.framework.controller.DefaultValueChanger;
import de.mossgrabers.framework.controller.ISetupFactory;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.hardware.BindType;
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
import de.mossgrabers.framework.mode.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.view.View;
import de.mossgrabers.framework.view.ViewManager;
import de.mossgrabers.framework.view.Views;


/**
 * Support for the Akai APC40 mkI and APC40 mkII controllers.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class APCControllerSetup extends AbstractControllerSetup<APCControlSurface, APCConfiguration>
{
    private final boolean isMkII;


    /**
     * Constructor.
     *
     * @param host The DAW host
     * @param factory The factory
     * @param globalSettings The global settings
     * @param documentSettings The document (project) specific settings
     * @param isMkII True if is mkII
     */
    public APCControllerSetup (final IHost host, final ISetupFactory factory, final ISettingsUI globalSettings, final ISettingsUI documentSettings, final boolean isMkII)
    {
        super (factory, host, globalSettings, documentSettings);
        this.isMkII = isMkII;
        this.colorManager = new APCColorManager (isMkII);
        this.valueChanger = new DefaultValueChanger (128, 1, 0.5);
        this.configuration = new APCConfiguration (host, this.valueChanger);
    }


    /** {@inheritDoc} */
    @Override
    protected void createScales ()
    {
        this.scales = new Scales (this.valueChanger, 36, 76, 8, 5);
        this.scales.setDrumDefaultOffset (12);
    }


    /** {@inheritDoc} */
    @Override
    protected void createModel ()
    {
        final ModelSetup ms = new ModelSetup ();
        ms.setNumScenes (5);
        ms.setNumDrumPadLayers (12);
        this.model = this.factory.createModel (this.colorManager, this.valueChanger, this.scales, ms);
        final ITrackBank trackBank = this.model.getTrackBank ();
        trackBank.setIndication (true);
        trackBank.addSelectionObserver ( (index, isSelected) -> this.handleTrackChange (isSelected));
    }


    /** {@inheritDoc} */
    @Override
    protected void createSurface ()
    {
        final IMidiAccess midiAccess = this.factory.createMidiAccess ();
        final IMidiOutput output = midiAccess.createOutput ();
        final IMidiInput input = midiAccess.createInput (this.isMkII ? "Akai APC40 mkII" : "Akai APC40",
                "B040??" /* Sustainpedal */);
        final APCControlSurface surface = new APCControlSurface (this.host, this.colorManager, this.configuration, output, input, this.isMkII);
        this.surfaces.add (surface);
        for (int i = 0; i < 8; i++)
            surface.setLED (APCControlSurface.APC_KNOB_DEVICE_KNOB_LED_1 + i, 1);
    }


    /** {@inheritDoc} */
    @Override
    protected void createObservers ()
    {
        final APCControlSurface surface = this.getSurface ();
        surface.getViewManager ().addViewChangeListener ( (previousViewId, activeViewId) -> this.updateMode (null));
        surface.getModeManager ().addModeListener ( (previousModeId, activeModeId) -> this.updateMode (activeModeId));
        this.createScaleObservers (this.configuration);
    }


    /** {@inheritDoc} */
    @Override
    protected void createModes ()
    {
        final APCControlSurface surface = this.getSurface ();
        final ModeManager modeManager = surface.getModeManager ();
        modeManager.registerMode (Modes.PAN, new PanMode (surface, this.model));
        for (int i = 0; i < 8; i++)
            modeManager.registerMode (Modes.get (Modes.SEND1, i), new SendMode (surface, this.model, i));
        modeManager.registerMode (Modes.BROWSER, new BrowserMode (surface, this.model));
    }


    /** {@inheritDoc} */
    @Override
    protected void createViews ()
    {
        final APCControlSurface surface = this.getSurface ();
        final ViewManager viewManager = surface.getViewManager ();
        viewManager.registerView (Views.PLAY, new PlayView (surface, this.model));
        viewManager.registerView (Views.SESSION, new SessionView (surface, this.model));
        viewManager.registerView (Views.SEQUENCER, new SequencerView (surface, this.model));
        viewManager.registerView (Views.DRUM, new DrumView (surface, this.model));
        viewManager.registerView (Views.RAINDROPS, new RaindropsView (surface, this.model));
        viewManager.registerView (Views.SHIFT, new ShiftView (surface, this.model));
    }


    /** {@inheritDoc} */
    @Override
    protected void registerTriggerCommands ()
    {
        final APCControlSurface surface = this.getSurface ();
        final ViewManager viewManager = surface.getViewManager ();
        final ITransport t = this.model.getTransport ();

        this.addButton (ButtonID.SHIFT, "SHIFT", new ToggleShiftViewCommand<> (this.model, surface), APCControlSurface.APC_BUTTON_SHIFT);
        this.addButton (ButtonID.PLAY, "PLAY", new PlayCommand<> (this.model, surface), APCControlSurface.APC_BUTTON_PLAY, t::isPlaying, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);
        this.addButton (ButtonID.RECORD, "RECORD", new APCRecordCommand (this.model, surface), APCControlSurface.APC_BUTTON_RECORD, t::isRecording, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);
        this.addButton (ButtonID.TAP_TEMPO, "Tempo", new TapTempoCommand<> (this.model, surface), APCControlSurface.APC_BUTTON_TAP_TEMPO);
        this.addButton (ButtonID.QUANTIZE, "DEV.LOCK", new APCQuantizeCommand (this.model, surface), APCControlSurface.APC_BUTTON_REC_QUANT);
        this.addButton (ButtonID.PAN_SEND, "PAN", new ModeSelectCommand<> (this.model, surface, Modes.PAN), APCControlSurface.APC_BUTTON_PAN);
        this.addButton (ButtonID.MASTERTRACK, "Master", new MasterCommand<> (this.model, surface), APCControlSurface.APC_BUTTON_MASTER, this.model.getMasterTrack ()::isSelected, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);
        this.addButton (ButtonID.STOP_ALL_CLIPS, "Stop Clips", new StopAllClipsOrBrowseCommand<> (this.model, surface), APCControlSurface.APC_BUTTON_STOP_ALL_CLIPS);
        this.addButton (ButtonID.SEND1, "Send A", new SendModeCommand (0, this.model, surface), APCControlSurface.APC_BUTTON_SEND_A);
        this.addButton (ButtonID.SEND2, "Send B", new SendModeCommand (1, this.model, surface), APCControlSurface.APC_BUTTON_SEND_B);

        for (int i = 0; i < 8; i++)
        {
            final int index = i;
            this.addButton (ButtonID.get (ButtonID.ROW1_1, i), "Select " + (i + 1), new SelectTrackSendOrClipLengthCommand (i, this.model, surface), i, APCControlSurface.APC_BUTTON_TRACK_SELECTION, () -> getButtonState (index, APCControlSurface.APC_BUTTON_TRACK_SELECTION) ? 1 : 0, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);
            this.addButton (ButtonID.get (ButtonID.ROW2_1, i), "Solo " + (i + 1), new SoloCommand<> (i, this.model, surface), i, APCControlSurface.APC_BUTTON_SOLO, () -> getButtonState (index, APCControlSurface.APC_BUTTON_SOLO) ? 1 : 0, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);
            this.addButton (ButtonID.get (ButtonID.ROW3_1, i), "Mute " + (i + 1), new MuteCommand<> (i, this.model, surface), i, APCControlSurface.APC_BUTTON_ACTIVATOR, () -> getButtonState (index, APCControlSurface.APC_BUTTON_ACTIVATOR) ? 1 : 0, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);
            this.addButton (ButtonID.get (ButtonID.ROW4_1, i), "Arm " + (i + 1), new RecArmCommand<> (i, this.model, surface), i, APCControlSurface.APC_BUTTON_RECORD_ARM, () -> getButtonState (index, APCControlSurface.APC_BUTTON_RECORD_ARM) ? 1 : 0, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);
            this.addButton (ButtonID.get (ButtonID.ROW5_1, i), "X-fade " + (i + 1), new CrossfadeModeCommand<> (i, this.model, surface), i, APCControlSurface.APC_BUTTON_A_B, () -> getButtonState (index, APCControlSurface.APC_BUTTON_A_B) ? 1 : 0, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);
            final ButtonID stopButtonID = ButtonID.get (ButtonID.ROW6_1, i);
            this.addButton (stopButtonID, "Stop " + (i + 1), new StopClipCommand<> (i, this.model, surface), i, APCControlSurface.APC_BUTTON_CLIP_STOP, () -> surface.isPressed (stopButtonID) ? 1 : 0, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);
        }

        if (this.isMkII)
        {
            this.addButton (ButtonID.DEVICE_LEFT, "<- DEVICE", new DeviceLayerLeftCommand<> (this.model, surface), APCControlSurface.APC_BUTTON_CLIP_TRACK);
            this.addButton (ButtonID.BROWSE, "BANK", new APCBrowserCommand (this.model, surface), APCControlSurface.APC_BUTTON_BANK, this.model.getBrowser ()::isActive);
        }
        else
        {
            this.addButton (ButtonID.STOP, "STOP", new StopCommand<> (this.model, surface), APCControlSurface.APC_BUTTON_STOP);
            this.addButton (ButtonID.SEND3, "SEND C", new SendModeCommand (2, this.model, surface), APCControlSurface.APC_BUTTON_SEND_C);
            this.addButton (ButtonID.NEW, "Footswitch", new NewCommand<> (this.model, surface), APCControlSurface.APC_FOOTSWITCH_2);
        }

        this.addButton (ButtonID.DEVICE_RIGHT, "DEVICE ->", new DeviceLayerRightCommand<> (this.model, surface), APCControlSurface.APC_BUTTON_DEVICE_ON_OFF);
        this.addButton (ButtonID.CLIP, "SESSION", new SessionRecordCommand (this.model, surface), this.isMkII ? APCControlSurface.APC_BUTTON_SESSION : APCControlSurface.APC_BUTTON_MIDI_OVERDUB, t::isLauncherOverdub, ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON);
        this.addButton (ButtonID.METRONOME, "METRONOME", new MetronomeCommand<> (this.model, surface), this.isMkII ? APCControlSurface.APC_BUTTON_SEND_C : APCControlSurface.APC_BUTTON_METRONOME, t::isMetronomeOn);
        this.addButton (ButtonID.NUDGE_MINUS, "NUDGE-", new RedoCommand<> (this.model, surface), this.isMkII ? APCControlSurface.APC_BUTTON_NUDGE_MINUS : APCControlSurface.APC_BUTTON_NUDGE_PLUS);
        this.addButton (ButtonID.NUDGE_PLUS, "NUDGE+", new UndoCommand<> (this.model, surface), this.isMkII ? APCControlSurface.APC_BUTTON_NUDGE_PLUS : APCControlSurface.APC_BUTTON_NUDGE_MINUS);
        this.addButton (ButtonID.LAYOUT, "DETAIL VIEW", new PanelLayoutCommand<> (this.model, surface), this.isMkII ? APCControlSurface.APC_BUTTON_METRONOME : APCControlSurface.APC_BUTTON_DETAIL_VIEW);
        this.addButton (ButtonID.DEVICE_ON_OFF, "DEV. ON/OFF", new DeviceOnOffCommand<> (this.model, surface), this.isMkII ? APCControlSurface.APC_BUTTON_DETAIL_VIEW : APCControlSurface.APC_BUTTON_DEVICE_ON_OFF, this.model.getCursorDevice ()::isEnabled);
        this.addButton (ButtonID.TOGGLE_DEVICES_PANE, "CLIP/DEV.VIEW", new PaneCommand<> (Panels.DEVICE, this.model, surface), this.isMkII ? APCControlSurface.APC_BUTTON_MIDI_OVERDUB : APCControlSurface.APC_BUTTON_CLIP_TRACK);

        this.addButton (ButtonID.BANK_LEFT, "Device Left", new SelectPreviousDeviceOrParamPageCommand<> (this.model, surface), APCControlSurface.APC_BUTTON_DEVICE_LEFT);
        this.addButton (ButtonID.BANK_RIGHT, "Device Right", new SelectNextDeviceOrParamPageCommand<> (this.model, surface), APCControlSurface.APC_BUTTON_DEVICE_RIGHT);
        this.addButton (ButtonID.ARROW_DOWN, "Arrow Down", new CursorCommand<> (Direction.DOWN, this.model, surface), APCControlSurface.APC_BUTTON_DOWN);
        this.addButton (ButtonID.ARROW_UP, "Arrow Up", new CursorCommand<> (Direction.UP, this.model, surface), APCControlSurface.APC_BUTTON_UP);
        this.addButton (ButtonID.ARROW_LEFT, "Arrow Left", new CursorCommand<> (Direction.LEFT, this.model, surface), APCControlSurface.APC_BUTTON_LEFT);
        this.addButton (ButtonID.ARROW_RIGHT, "Arrow Right", new CursorCommand<> (Direction.RIGHT, this.model, surface), APCControlSurface.APC_BUTTON_RIGHT);

        for (int i = 0; i < 5; i++)
        {
            final ButtonID sceneButtonID = ButtonID.get (ButtonID.SCENE1, i);
            this.addButton (sceneButtonID, "Scene " + (i + 1), new SceneCommand<> (i, this.model, surface), APCControlSurface.APC_BUTTON_SCENE_LAUNCH_1 + i, () -> {
                final View activeView = viewManager.getActiveView ();
                return activeView != null ? activeView.getButtonColor (sceneButtonID) : 0;
            });
        }

        // TODO
        // this.updateDeviceKnobs ();

        // MK 1:
        // case APCControlSurface.APC_BUTTON_A_B:
        // return getCrossfadeButtonColor (track, trackExists));
        // if (isShift)
        // surface.updateTrigger (i, APCControlSurface.APC_BUTTON_RECORD_ARM,
        // getCrossfadeButtonColor (track, trackExists));

    }


    /** {@inheritDoc} */
    @Override
    protected void registerContinuousCommands ()
    {
        final APCControlSurface surface = this.getSurface ();

        this.addFader (ContinuousID.MASTER_KNOB, "Master", new MasterFaderAbsoluteCommand<> (this.model, surface), BindType.CC, APCControlSurface.APC_KNOB_MASTER_LEVEL);
        this.addRelativeKnob (ContinuousID.PLAY_POSITION, "Play Position", new PlayPositionCommand<> (this.model, surface), APCControlSurface.APC_KNOB_CUE_LEVEL);
        this.addFader (ContinuousID.CROSSFADER, "Crossfader", new CrossfaderCommand<> (this.model, surface), BindType.CC, APCControlSurface.APC_KNOB_CROSSFADER);

        for (int i = 0; i < 8; i++)
        {
            this.addFader (ContinuousID.get (ContinuousID.FADER1, i), "Fader" + (i + 1), new FaderAbsoluteCommand<> (i, this.model, surface), BindType.CC, i, APCControlSurface.APC_KNOB_TRACK_LEVEL);
            this.addRelativeKnob (ContinuousID.get (ContinuousID.KNOB1, i), "Knob " + (i + 1), new KnobRowModeCommand<> (i, this.model, surface), APCControlSurface.APC_KNOB_TRACK_KNOB_1 + i);
            this.addRelativeKnob (ContinuousID.get (ContinuousID.DEVICE_KNOB1, i), "Device Knob " + (i + 1), new DeviceParamsKnobRowCommand<> (i, this.model, surface), APCControlSurface.APC_KNOB_DEVICE_KNOB_1 + i);
        }

        if (this.isMkII)
            this.addRelativeKnob (ContinuousID.TEMPO, "Tempo", new TempoCommand<> (this.model, surface), APCControlSurface.APC_KNOB_TEMPO);
    }


    /** {@inheritDoc} */
    @Override
    protected void layoutControls ()
    {
        final APCControlSurface surface = this.getSurface ();

        surface.getButton (ButtonID.PAD1).setBounds (13.0, 194.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD2).setBounds (74.25, 194.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD3).setBounds (135.25, 194.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD4).setBounds (196.5, 194.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD5).setBounds (257.75, 194.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD6).setBounds (318.75, 194.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD7).setBounds (380.0, 194.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD8).setBounds (441.0, 194.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD9).setBounds (12.0, 165.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD10).setBounds (73.25, 165.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD11).setBounds (134.5, 165.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD12).setBounds (196.0, 165.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD13).setBounds (257.25, 165.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD14).setBounds (318.5, 165.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD15).setBounds (379.75, 165.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD16).setBounds (441.0, 165.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD17).setBounds (11.75, 135.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD18).setBounds (73.25, 135.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD19).setBounds (135.0, 135.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD20).setBounds (196.5, 135.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD21).setBounds (258.0, 135.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD22).setBounds (319.5, 135.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD23).setBounds (381.25, 135.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD24).setBounds (441.0, 137.5, 52.25, 18.5);
        surface.getButton (ButtonID.PAD25).setBounds (12.5, 108.75, 52.25, 18.5);
        surface.getButton (ButtonID.PAD26).setBounds (73.75, 108.75, 52.25, 18.5);
        surface.getButton (ButtonID.PAD27).setBounds (135.25, 108.75, 52.25, 18.5);
        surface.getButton (ButtonID.PAD28).setBounds (196.5, 108.75, 52.25, 18.5);
        surface.getButton (ButtonID.PAD29).setBounds (257.75, 108.75, 52.25, 18.5);
        surface.getButton (ButtonID.PAD30).setBounds (319.25, 108.75, 52.25, 18.5);
        surface.getButton (ButtonID.PAD31).setBounds (380.5, 108.75, 52.25, 18.5);
        surface.getButton (ButtonID.PAD32).setBounds (441.0, 108.75, 52.25, 18.5);
        surface.getButton (ButtonID.PAD33).setBounds (12.75, 78.0, 52.25, 18.5);
        surface.getButton (ButtonID.PAD34).setBounds (74.25, 78.0, 52.25, 18.5);
        surface.getButton (ButtonID.PAD35).setBounds (135.5, 78.0, 52.25, 18.5);
        surface.getButton (ButtonID.PAD36).setBounds (196.75, 78.0, 52.25, 18.5);
        surface.getButton (ButtonID.PAD37).setBounds (256.5, 78.0, 52.25, 18.5);
        surface.getButton (ButtonID.PAD38).setBounds (318.0, 78.0, 52.25, 18.5);
        surface.getButton (ButtonID.PAD39).setBounds (379.5, 78.0, 52.25, 18.5);
        surface.getButton (ButtonID.PAD40).setBounds (441.0, 78.0, 52.25, 18.5);
        surface.getButton (ButtonID.SHIFT).setBounds (688.25, 360.5, 33.25, 15.25);
        surface.getButton (ButtonID.PLAY).setBounds (626.75, 57.0, 23.5, 20.0);
        surface.getButton (ButtonID.RECORD).setBounds (688.25, 57.0, 23.5, 20.0);
        surface.getButton (ButtonID.TAP_TEMPO).setBounds (688.25, 99.0, 33.25, 15.25);
        surface.getButton (ButtonID.QUANTIZE).setBounds (627.0, 327.25, 33.25, 15.25);
        surface.getButton (ButtonID.PAN_SEND).setBounds (562.25, 64.0, 33.25, 15.25);
        surface.getButton (ButtonID.SEND1).setBounds (562.25, 96.0, 33.25, 15.25);
        surface.getButton (ButtonID.SEND2).setBounds (562.25, 132.0, 33.25, 15.25);
        surface.getButton (ButtonID.BROWSE).setBounds (746.5, 360.5, 33.25, 15.25);
        surface.getButton (ButtonID.BANK_LEFT).setBounds (562.25, 289.75, 33.25, 15.25);
        surface.getButton (ButtonID.BANK_RIGHT).setBounds (627.0, 289.75, 33.25, 15.25);
        surface.getButton (ButtonID.ARROW_DOWN).setBounds (581.25, 361.75, 33.25, 25.0);
        surface.getButton (ButtonID.ARROW_UP).setBounds (581.5, 386.25, 33.25, 25.0);
        surface.getButton (ButtonID.ARROW_LEFT).setBounds (562.5, 361.75, 17.0, 49.25);
        surface.getButton (ButtonID.ARROW_RIGHT).setBounds (616.0, 361.75, 17.0, 49.25);

        surface.getButton (ButtonID.SCENE1).setBounds (500.75, 78.0, 36.0, 18.5);
        surface.getButton (ButtonID.SCENE2).setBounds (500.75, 108.75, 36.0, 18.5);
        surface.getButton (ButtonID.SCENE3).setBounds (500.75, 137.5, 36.0, 18.5);
        surface.getButton (ButtonID.SCENE4).setBounds (500.75, 165.5, 36.0, 18.5);
        surface.getButton (ButtonID.SCENE5).setBounds (500.75, 194.5, 36.0, 18.5);
        surface.getButton (ButtonID.STOP_ALL_CLIPS).setBounds (502.25, 227.25, 33.25, 18.75);
        surface.getButton (ButtonID.MASTERTRACK).setBounds (500.75, 263.0, 36.0, 15.75);

        surface.getButton (ButtonID.ROW1_1).setBounds (12.5, 262.5, 50.5, 15.75);
        surface.getButton (ButtonID.ROW1_2).setBounds (74.0, 262.5, 50.5, 15.75);
        surface.getButton (ButtonID.ROW1_3).setBounds (135.25, 262.5, 50.5, 15.75);
        surface.getButton (ButtonID.ROW1_4).setBounds (196.75, 262.5, 50.5, 15.75);
        surface.getButton (ButtonID.ROW1_5).setBounds (258.25, 262.5, 50.5, 15.75);
        surface.getButton (ButtonID.ROW1_6).setBounds (319.5, 262.5, 50.5, 15.75);
        surface.getButton (ButtonID.ROW1_7).setBounds (381.0, 262.5, 50.5, 15.75);
        surface.getButton (ButtonID.ROW1_8).setBounds (442.5, 262.5, 50.5, 15.75);
        surface.getButton (ButtonID.ROW2_1).setBounds (14.0, 317.75, 19.5, 18.0);
        surface.getButton (ButtonID.ROW2_2).setBounds (75.25, 317.75, 19.5, 18.0);
        surface.getButton (ButtonID.ROW2_3).setBounds (136.5, 317.75, 19.5, 18.0);
        surface.getButton (ButtonID.ROW2_4).setBounds (197.5, 317.75, 19.5, 18.0);
        surface.getButton (ButtonID.ROW2_5).setBounds (258.75, 317.75, 19.5, 18.0);
        surface.getButton (ButtonID.ROW2_6).setBounds (320.0, 317.75, 19.5, 18.0);
        surface.getButton (ButtonID.ROW2_7).setBounds (381.25, 317.75, 19.5, 18.0);
        surface.getButton (ButtonID.ROW2_8).setBounds (442.5, 317.75, 19.5, 18.0);
        surface.getButton (ButtonID.ROW3_1).setBounds (14.25, 291.5, 19.5, 18.0);
        surface.getButton (ButtonID.ROW3_2).setBounds (75.5, 291.5, 19.5, 18.0);
        surface.getButton (ButtonID.ROW3_3).setBounds (136.5, 291.5, 19.5, 18.0);
        surface.getButton (ButtonID.ROW3_4).setBounds (197.75, 291.5, 19.5, 18.0);
        surface.getButton (ButtonID.ROW3_5).setBounds (259.0, 291.5, 19.5, 18.0);
        surface.getButton (ButtonID.ROW3_6).setBounds (320.0, 291.5, 19.5, 18.0);
        surface.getButton (ButtonID.ROW3_7).setBounds (381.25, 291.5, 19.5, 18.0);
        surface.getButton (ButtonID.ROW3_8).setBounds (442.5, 291.5, 19.5, 18.0);
        surface.getButton (ButtonID.ROW4_1).setBounds (44.5, 317.75, 19.5, 18.0);
        surface.getButton (ButtonID.ROW4_2).setBounds (105.75, 317.75, 19.5, 18.0);
        surface.getButton (ButtonID.ROW4_3).setBounds (167.0, 317.75, 19.5, 18.0);
        surface.getButton (ButtonID.ROW4_4).setBounds (228.25, 317.75, 19.5, 18.0);
        surface.getButton (ButtonID.ROW4_5).setBounds (289.5, 317.75, 19.5, 18.0);
        surface.getButton (ButtonID.ROW4_6).setBounds (350.75, 317.75, 19.5, 18.0);
        surface.getButton (ButtonID.ROW4_7).setBounds (411.75, 317.75, 19.5, 18.0);
        surface.getButton (ButtonID.ROW4_8).setBounds (473.0, 317.75, 19.5, 18.0);
        surface.getButton (ButtonID.ROW5_1).setBounds (44.75, 291.5, 19.5, 18.0);
        surface.getButton (ButtonID.ROW5_2).setBounds (106.0, 291.5, 19.5, 18.0);
        surface.getButton (ButtonID.ROW5_3).setBounds (167.25, 291.5, 19.5, 18.0);
        surface.getButton (ButtonID.ROW5_4).setBounds (228.25, 291.5, 19.5, 18.0);
        surface.getButton (ButtonID.ROW5_5).setBounds (289.5, 291.5, 19.5, 18.0);
        surface.getButton (ButtonID.ROW5_6).setBounds (350.75, 291.5, 19.5, 18.0);
        surface.getButton (ButtonID.ROW5_7).setBounds (411.75, 291.5, 19.5, 18.0);
        surface.getButton (ButtonID.ROW5_8).setBounds (473.0, 291.5, 19.5, 18.0);
        surface.getButton (ButtonID.ROW6_1).setBounds (21.25, 227.25, 31.5, 18.75);
        surface.getButton (ButtonID.ROW6_2).setBounds (82.75, 227.25, 31.5, 18.75);
        surface.getButton (ButtonID.ROW6_3).setBounds (144.25, 227.25, 31.5, 18.75);
        surface.getButton (ButtonID.ROW6_4).setBounds (205.75, 227.25, 31.5, 18.75);
        surface.getButton (ButtonID.ROW6_5).setBounds (267.0, 227.25, 31.5, 18.75);
        surface.getButton (ButtonID.ROW6_6).setBounds (328.5, 227.25, 31.5, 18.75);
        surface.getButton (ButtonID.ROW6_7).setBounds (390.0, 227.25, 31.5, 18.75);
        surface.getButton (ButtonID.ROW6_8).setBounds (451.5, 227.25, 31.5, 18.75);

        surface.getContinuous (ContinuousID.MASTER_KNOB).setBounds (500.25, 348.5, 40.5, 115.0);
        surface.getContinuous (ContinuousID.PLAY_POSITION).setBounds (497.75, 293.75, 40.25, 37.75);
        surface.getContinuous (ContinuousID.CROSSFADER).setBounds (651.25, 419.5, 104.0, 50.0);
        surface.getContinuous (ContinuousID.FADER1).setBounds (19.75, 348.5, 40.5, 115.0);
        surface.getContinuous (ContinuousID.KNOB1).setBounds (16.75, 19.0, 40.25, 37.75);
        surface.getContinuous (ContinuousID.DEVICE_KNOB1).setBounds (560.25, 173.75, 40.25, 37.75);
        surface.getContinuous (ContinuousID.FADER2).setBounds (80.75, 348.5, 40.5, 115.0);
        surface.getContinuous (ContinuousID.KNOB2).setBounds (78.5, 19.0, 40.25, 37.75);
        surface.getContinuous (ContinuousID.DEVICE_KNOB2).setBounds (620.75, 173.75, 40.25, 37.75);
        surface.getContinuous (ContinuousID.FADER3).setBounds (141.5, 348.5, 40.5, 115.0);
        surface.getContinuous (ContinuousID.KNOB3).setBounds (140.25, 19.0, 40.25, 37.75);
        surface.getContinuous (ContinuousID.DEVICE_KNOB3).setBounds (682.5, 173.75, 40.25, 37.75);
        surface.getContinuous (ContinuousID.FADER4).setBounds (202.5, 348.5, 40.5, 115.0);
        surface.getContinuous (ContinuousID.KNOB4).setBounds (202.0, 19.0, 40.25, 37.75);
        surface.getContinuous (ContinuousID.DEVICE_KNOB4).setBounds (744.25, 173.75, 40.25, 37.75);
        surface.getContinuous (ContinuousID.FADER5).setBounds (263.5, 348.5, 40.5, 115.0);
        surface.getContinuous (ContinuousID.KNOB5).setBounds (263.5, 19.0, 40.25, 37.75);
        surface.getContinuous (ContinuousID.DEVICE_KNOB5).setBounds (560.25, 235.75, 40.25, 37.75);
        surface.getContinuous (ContinuousID.FADER6).setBounds (324.25, 348.5, 40.5, 115.0);
        surface.getContinuous (ContinuousID.KNOB6).setBounds (325.25, 19.0, 40.25, 37.75);
        surface.getContinuous (ContinuousID.DEVICE_KNOB6).setBounds (620.75, 235.75, 40.25, 37.75);
        surface.getContinuous (ContinuousID.FADER7).setBounds (385.25, 348.5, 40.5, 115.0);
        surface.getContinuous (ContinuousID.KNOB7).setBounds (387.0, 19.0, 40.25, 37.75);
        surface.getContinuous (ContinuousID.DEVICE_KNOB7).setBounds (682.5, 235.75, 40.25, 37.75);
        surface.getContinuous (ContinuousID.FADER8).setBounds (446.25, 348.5, 40.5, 115.0);
        surface.getContinuous (ContinuousID.KNOB8).setBounds (448.75, 19.0, 40.25, 37.75);
        surface.getContinuous (ContinuousID.DEVICE_KNOB8).setBounds (744.25, 235.75, 40.25, 37.75);

        if (this.isMkII)
            surface.getContinuous (ContinuousID.TEMPO).setBounds (743.25, 106.75, 40.25, 37.75);
    }


    /** {@inheritDoc} */
    @Override
    public void startup ()
    {
        final APCControlSurface surface = this.getSurface ();
        surface.getModeManager ().setActiveMode (Modes.PAN);
        surface.getViewManager ().setActiveView (Views.PLAY);
    }


    private static String getCrossfadeButtonColor (final ITrack track, final boolean trackExists)
    {
        if (!trackExists)
            return ColorManager.BUTTON_STATE_OFF;

        final String crossfadeMode = track.getCrossfadeMode ();
        if ("AB".equals (crossfadeMode))
            return ColorManager.BUTTON_STATE_OFF;

        return "A".equals (crossfadeMode) ? ColorManager.BUTTON_STATE_ON : APCColorManager.BUTTON_STATE_BLINK;
    }


    private static boolean getMuteButtonState (final boolean isShift, final ITrack track)
    {
        return isShift ? track.isMonitor () : !track.isMute ();
    }


    private static boolean getSoloButtonState (final boolean isShift, final ITrack track)
    {
        return isShift ? track.isAutoMonitor () : track.isSolo ();
    }


    private void updateMode (final Modes mode)
    {
        final APCControlSurface surface = this.getSurface ();
        final Modes m = mode == null ? surface.getModeManager ().getActiveOrTempModeId () : mode;
        this.updateIndication (m);

        // TODO Update button LEDs
        // surface.updateTrigger (APCControlSurface.APC_BUTTON_PAN, Modes.PAN.equals (m) ?
        // ColorManager.BUTTON_STATE_ON : ColorManager.BUTTON_STATE_OFF);
        // if (surface.isMkII ())
        // {
        // surface.updateTrigger (APCControlSurface.APC_BUTTON_SEND_A, Modes.SEND1.equals (m) ||
        // Modes.SEND3.equals (m) || Modes.SEND5.equals (m) || Modes.SEND7.equals (m) ?
        // ColorManager.BUTTON_STATE_ON : ColorManager.BUTTON_STATE_OFF);
        // surface.updateTrigger (APCControlSurface.APC_BUTTON_SEND_B, Modes.SEND2.equals (m) ||
        // Modes.SEND4.equals (m) || Modes.SEND6.equals (m) || Modes.SEND8.equals (m) ?
        // ColorManager.BUTTON_STATE_ON : ColorManager.BUTTON_STATE_OFF);
        // }
        // else
        // {
        // surface.updateTrigger (APCControlSurface.APC_BUTTON_SEND_A, Modes.SEND1.equals (m) ||
        // Modes.SEND4.equals (m) || Modes.SEND7.equals (m) ? ColorManager.BUTTON_STATE_ON :
        // ColorManager.BUTTON_STATE_OFF);
        // surface.updateTrigger (APCControlSurface.APC_BUTTON_SEND_B, Modes.SEND2.equals (m) ||
        // Modes.SEND5.equals (m) || Modes.SEND8.equals (m) ? ColorManager.BUTTON_STATE_ON :
        // ColorManager.BUTTON_STATE_OFF);
        // surface.updateTrigger (APCControlSurface.APC_BUTTON_SEND_C, Modes.SEND3.equals (m) ||
        // Modes.SEND6.equals (m) ? ColorManager.BUTTON_STATE_ON : ColorManager.BUTTON_STATE_OFF);
        // }
    }


    private void updateDeviceKnobs ()
    {
        final APCControlSurface surface = this.getSurface ();
        final View view = surface.getViewManager ().getActiveView ();
        if (view == null)
            return;

        final IParameterBank parameterBank = this.model.getCursorDevice ().getParameterBank ();
        for (int i = 0; i < 8; i++)
        {
            // TODO
            // final ContinuousCommandID deviceKnobCommand = ContinuousCommandID.get
            // (ContinuousCommandID.DEVICE_KNOB1, i);
            // if (!((DeviceParamsKnobRowCommand<?, ?>) view.getContinuousCommand
            // (deviceKnobCommand)).isKnobMoving ())
            // surface.setLED (APCControlSurface.APC_KNOB_DEVICE_KNOB_1 + i, parameterBank.getItem
            // (i).getValue ());
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void updateIndication (final Modes mode)
    {
        final ITrackBank tb = this.model.getTrackBank ();
        final ITrackBank tbe = this.model.getEffectTrackBank ();
        final APCControlSurface surface = this.getSurface ();
        final boolean isSession = surface.getViewManager ().isActiveView (Views.SESSION);
        final boolean isEffect = this.model.isEffectTrackBankActive ();
        final boolean isPan = Modes.PAN.equals (mode);
        final boolean isShift = surface.isShiftPressed ();

        tb.setIndication (!isEffect && (isSession || isShift));
        if (tbe != null)
            tbe.setIndication (isEffect && (isSession || isShift));

        final ICursorDevice cursorDevice = this.model.getCursorDevice ();
        final IParameterBank parameterBank = cursorDevice.getParameterBank ();
        for (int i = 0; i < 8; i++)
        {
            final ITrack track = tb.getItem (i);
            track.setVolumeIndication (!isEffect);
            track.setPanIndication (!isEffect && isPan);
            final ISendBank sendBank = track.getSendBank ();
            for (int j = 0; j < 8; j++)
                sendBank.getItem (j).setIndication (!isEffect && (Modes.SEND1.equals (mode) && j == 0 || Modes.SEND2.equals (mode) && j == 1 || Modes.SEND3.equals (mode) && j == 2 || Modes.SEND4.equals (mode) && j == 3 || Modes.SEND5.equals (mode) && j == 4 || Modes.SEND6.equals (mode) && j == 5 || Modes.SEND7.equals (mode) && j == 6 || Modes.SEND8.equals (mode) && j == 7));

            if (tbe != null)
            {
                final ITrack fxTrack = tbe.getItem (i);
                fxTrack.setVolumeIndication (isEffect);
                fxTrack.setPanIndication (isEffect && isPan);
            }

            parameterBank.getItem (i).setIndication (true);
        }
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

        final APCControlSurface surface = this.getSurface ();
        // Recall last used view (if we are not in session mode)
        final ViewManager viewManager = surface.getViewManager ();
        if (!viewManager.isActiveView (Views.SESSION))
        {
            final ITrack selectedTrack = this.model.getSelectedTrack ();
            if (selectedTrack != null)
            {
                final Views preferredView = viewManager.getPreferredView (selectedTrack.getPosition ());
                viewManager.setActiveView (preferredView == null ? Views.PLAY : preferredView);
            }
        }

        if (viewManager.isActiveView (Views.PLAY))
            viewManager.getActiveView ().updateNoteMapping ();

        // Reset drum octave because the drum pad bank is also reset
        this.scales.resetDrumOctave ();
        if (viewManager.isActiveView (Views.DRUM))
            viewManager.getView (Views.DRUM).updateNoteMapping ();
    }


    /** {@inheritDoc} */
    @Override
    protected BindType getTriggerBindType (ButtonID buttonID)
    {
        return BindType.NOTE;
    }


    private boolean getButtonState (final int index, final int button)
    {
        // Activator, Solo, Record Arm

        final APCControlSurface surface = this.getSurface ();
        final int clipLength = surface.getConfiguration ().getNewClipLength ();
        final ModeManager modeManager = surface.getModeManager ();

        final ITrackBank tb = this.model.getCurrentTrackBank ();
        final ITrack track = tb.getItem (index);
        final boolean trackExists = track.doesExist ();

        final boolean isShift = surface.isShiftPressed ();

        switch (button)
        {
            case APCControlSurface.APC_BUTTON_TRACK_SELECTION:
                if (isShift)
                    return index == clipLength;
                final ITrack selTrack = tb.getSelectedItem ();
                final int selIndex = selTrack == null ? -1 : selTrack.getIndex ();
                return surface.isPressed (ButtonID.SEND1) ? modeManager.isActiveOrTempMode (Modes.get (Modes.SEND1, index)) : index == selIndex;

            case APCControlSurface.APC_BUTTON_SOLO:
                return trackExists && getSoloButtonState (isShift, track);

            case APCControlSurface.APC_BUTTON_ACTIVATOR:
                return trackExists && getMuteButtonState (isShift, track);

            case APCControlSurface.APC_BUTTON_RECORD_ARM:
                return trackExists && track.isRecArm ();

            default:
                return false;
        }
    }
}
