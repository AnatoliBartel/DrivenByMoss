// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.slmkiii;

import de.mossgrabers.controller.slmkiii.command.continuous.VolumeFaderCommand;
import de.mossgrabers.controller.slmkiii.command.trigger.ButtonAreaCommand;
import de.mossgrabers.controller.slmkiii.command.trigger.SLMkIIICursorCommand;
import de.mossgrabers.controller.slmkiii.command.trigger.TrackModeCommand;
import de.mossgrabers.controller.slmkiii.controller.SLMkIIIColorManager;
import de.mossgrabers.controller.slmkiii.controller.SLMkIIIControlSurface;
import de.mossgrabers.controller.slmkiii.controller.SLMkIIIScales;
import de.mossgrabers.controller.slmkiii.mode.BrowserMode;
import de.mossgrabers.controller.slmkiii.mode.OptionsMode;
import de.mossgrabers.controller.slmkiii.mode.SequencerResolutionMode;
import de.mossgrabers.controller.slmkiii.mode.device.ParametersMode;
import de.mossgrabers.controller.slmkiii.mode.track.PanMode;
import de.mossgrabers.controller.slmkiii.mode.track.SendMode;
import de.mossgrabers.controller.slmkiii.mode.track.TrackMode;
import de.mossgrabers.controller.slmkiii.mode.track.VolumeMode;
import de.mossgrabers.controller.slmkiii.view.ColorView;
import de.mossgrabers.controller.slmkiii.view.DrumView;
import de.mossgrabers.controller.slmkiii.view.SessionView;
import de.mossgrabers.framework.command.SceneCommand;
import de.mossgrabers.framework.command.continuous.KnobRowModeCommand;
import de.mossgrabers.framework.command.core.NopCommand;
import de.mossgrabers.framework.command.trigger.ShiftCommand;
import de.mossgrabers.framework.command.trigger.mode.ButtonRowModeCommand;
import de.mossgrabers.framework.command.trigger.mode.ModeCursorCommand.Direction;
import de.mossgrabers.framework.command.trigger.mode.ModeSelectCommand;
import de.mossgrabers.framework.command.trigger.transport.PlayCommand;
import de.mossgrabers.framework.command.trigger.transport.RecordCommand;
import de.mossgrabers.framework.command.trigger.transport.StopCommand;
import de.mossgrabers.framework.command.trigger.transport.ToggleLoopCommand;
import de.mossgrabers.framework.command.trigger.transport.WindCommand;
import de.mossgrabers.framework.configuration.ISettingsUI;
import de.mossgrabers.framework.controller.AbstractControllerSetup;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.ContinuousID;
import de.mossgrabers.framework.controller.ISetupFactory;
import de.mossgrabers.framework.controller.hardware.BindType;
import de.mossgrabers.framework.controller.valuechanger.DefaultValueChanger;
import de.mossgrabers.framework.daw.IBrowser;
import de.mossgrabers.framework.daw.ICursorDevice;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.IParameterBank;
import de.mossgrabers.framework.daw.ISendBank;
import de.mossgrabers.framework.daw.ITrackBank;
import de.mossgrabers.framework.daw.ITransport;
import de.mossgrabers.framework.daw.ModelSetup;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.midi.DeviceInquiry;
import de.mossgrabers.framework.daw.midi.IMidiAccess;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.daw.midi.IMidiOutput;
import de.mossgrabers.framework.mode.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.ViewManager;
import de.mossgrabers.framework.view.Views;


/**
 * Support for the Novation SLMkIII controllers.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class SLMkIIIControllerSetup extends AbstractControllerSetup<SLMkIIIControlSurface, SLMkIIIConfiguration>
{
    // @formatter:off
    private static final int [] DRUM_MATRIX =
    {
        0,  1,  2,  3,  4,  5,  6,  7,
        8,  9, 10, 11, 12, 13, 14, 15,
       -1, -1, -1, -1, -1, -1, -1, -1,
       -1, -1, -1, -1, -1, -1, -1, -1,
       -1, -1, -1, -1, -1, -1, -1, -1,
       -1, -1, -1, -1, -1, -1, -1, -1,
       -1, -1, -1, -1, -1, -1, -1, -1,
       -1, -1, -1, -1, -1, -1, -1, -1
    };
    // @formatter:on


    /**
     * Constructor.
     *
     * @param host The DAW host
     * @param factory The factory
     * @param globalSettings The global settings
     * @param documentSettings The document (project) specific settings
     */
    public SLMkIIIControllerSetup (final IHost host, final ISetupFactory factory, final ISettingsUI globalSettings, final ISettingsUI documentSettings)
    {
        super (factory, host, globalSettings, documentSettings);

        this.colorManager = new SLMkIIIColorManager ();
        this.valueChanger = new DefaultValueChanger (1024, 8, 1);
        this.configuration = new SLMkIIIConfiguration (host, this.valueChanger);
    }


    /** {@inheritDoc} */
    @Override
    protected void createScales ()
    {
        this.scales = new SLMkIIIScales (this.valueChanger, 36, 52, 8, 2);
        this.scales.setDrumMatrix (DRUM_MATRIX);
        this.scales.setDrumNoteEnd (52);
    }


    /** {@inheritDoc} */
    @Override
    protected void createModel ()
    {
        final ModelSetup ms = new ModelSetup ();
        ms.setHasFlatTrackList (true);
        ms.setHasFullFlatTrackList (true);
        ms.setNumScenes (2);
        ms.setNumSends (8);
        this.model = this.factory.createModel (this.colorManager, this.valueChanger, this.scales, ms);
    }


    /** {@inheritDoc} */
    @Override
    protected void createSurface ()
    {
        final IMidiAccess midiAccess = this.factory.createMidiAccess ();
        final IMidiOutput output = midiAccess.createOutput ();
        final IMidiInput keyboardInput = midiAccess.createInput (1, "Keyboard", "8?????", "9?????", "B?????", "D?????", "E?????");
        final IHost hostProxy = this.model.getHost ();
        final IMidiInput input = midiAccess.createInput ("Pads", "8?????", "9?????");
        final SLMkIIIControlSurface surface = new SLMkIIIControlSurface (hostProxy, this.colorManager, this.configuration, output, input);
        this.surfaces.add (surface);

        surface.addPianoKeyboard (49);
        surface.getPianoKeyboard ().bind (keyboardInput);
    }


    /** {@inheritDoc} */
    @Override
    protected void createObservers ()
    {
        this.createScaleObservers (this.configuration);
        this.getSurface ().getModeManager ().addModeListener ( (oldMode, newMode) -> this.updateIndication (newMode));
    }


    /** {@inheritDoc} */
    @Override
    protected void createModes ()
    {
        final SLMkIIIControlSurface surface = this.getSurface ();
        final ModeManager modeManager = surface.getModeManager ();

        modeManager.registerMode (Modes.TRACK, new TrackMode (surface, this.model));
        modeManager.registerMode (Modes.VOLUME, new VolumeMode (surface, this.model));
        modeManager.registerMode (Modes.PAN, new PanMode (surface, this.model));
        for (int i = 0; i < 8; i++)
            modeManager.registerMode (Modes.get (Modes.SEND1, i), new SendMode (i, surface, this.model));
        modeManager.registerMode (Modes.DEVICE_PARAMS, new ParametersMode (surface, this.model));
        modeManager.registerMode (Modes.BROWSER, new BrowserMode (surface, this.model));

        modeManager.registerMode (Modes.FUNCTIONS, new OptionsMode (surface, this.model));
        modeManager.registerMode (Modes.GROOVE, new SequencerResolutionMode (surface, this.model));
    }


    /** {@inheritDoc} */
    @Override
    protected void createViews ()
    {
        final SLMkIIIControlSurface surface = this.getSurface ();
        final ViewManager viewManager = surface.getViewManager ();

        viewManager.registerView (Views.SESSION, new SessionView (surface, this.model));
        viewManager.registerView (Views.DRUM, new DrumView (surface, this.model));
        viewManager.registerView (Views.COLOR, new ColorView (surface, this.model));
    }


    /** {@inheritDoc} */
    @Override
    protected void registerTriggerCommands ()
    {
        final SLMkIIIControlSurface surface = this.getSurface ();
        final ITransport t = this.model.getTransport ();

        final WindCommand<SLMkIIIControlSurface, SLMkIIIConfiguration> rewindCommand = new WindCommand<> (this.model, surface, false);
        final WindCommand<SLMkIIIControlSurface, SLMkIIIConfiguration> forwardCommand = new WindCommand<> (this.model, surface, true);
        this.addButton (ButtonID.REWIND, "<<", rewindCommand, SLMkIIIControlSurface.MKIII_TRANSPORT_REWIND, rewindCommand::isRewinding, SLMkIIIColorManager.BUTTON_STATE_WIND_ON, SLMkIIIColorManager.BUTTON_STATE_WIND_HI);
        this.addButton (ButtonID.FORWARD, ">>", forwardCommand, SLMkIIIControlSurface.MKIII_TRANSPORT_FORWARD, forwardCommand::isForwarding, SLMkIIIColorManager.BUTTON_STATE_WIND_ON, SLMkIIIColorManager.BUTTON_STATE_WIND_HI);
        this.addButton (ButtonID.LOOP, "Loop", new ToggleLoopCommand<> (this.model, surface), SLMkIIIControlSurface.MKIII_TRANSPORT_LOOP, t::isLoop, SLMkIIIColorManager.BUTTON_STATE_LOOP_ON, SLMkIIIColorManager.BUTTON_STATE_LOOP_HI);
        this.addButton (ButtonID.STOP, "Stop", new StopCommand<> (this.model, surface), SLMkIIIControlSurface.MKIII_TRANSPORT_STOP, () -> !t.isPlaying (), SLMkIIIColorManager.BUTTON_STATE_STOP_ON, SLMkIIIColorManager.BUTTON_STATE_STOP_HI);
        this.addButton (ButtonID.PLAY, "Play", new PlayCommand<> (this.model, surface), SLMkIIIControlSurface.MKIII_TRANSPORT_PLAY, t::isPlaying, SLMkIIIColorManager.BUTTON_STATE_PLAY_ON, SLMkIIIColorManager.BUTTON_STATE_PLAY_HI);
        // TODO Test colors
        this.addButton (ButtonID.RECORD, "Record", new RecordCommand<> (this.model, surface), SLMkIIIControlSurface.MKIII_TRANSPORT_RECORD, () -> {
            final boolean isOn = this.isRecordShifted (surface) ? t.isLauncherOverdub () : t.isRecording ();
            return isOn ? 1 : 0;
        }, SLMkIIIColorManager.BUTTON_STATE_REC_ON, SLMkIIIColorManager.BUTTON_STATE_REC_HI, SLMkIIIColorManager.BUTTON_STATE_OVR_ON, SLMkIIIColorManager.BUTTON_STATE_OVR_HI);

        for (int i = 0; i < 8; i++)
        {
            this.addButton (ButtonID.get (ButtonID.ROW1_1, i), "Select " + (i + 1), new ButtonRowModeCommand<> (0, i, this.model, surface), SLMkIIIControlSurface.MKIII_DISPLAY_BUTTON_1 + i);
            this.addButton (ButtonID.get (ButtonID.ROW2_1, i), "Row 1 " + (i + 1), new ButtonAreaCommand (0, i, this.model, surface), SLMkIIIControlSurface.MKIII_BUTTON_ROW1_1 + i);
            this.addButton (ButtonID.get (ButtonID.ROW3_1, i), "ROw 2 " + (i + 1), new ButtonAreaCommand (1, i, this.model, surface), SLMkIIIControlSurface.MKIII_BUTTON_ROW2_1 + i);
        }

        final ModeSelectCommand<SLMkIIIControlSurface, SLMkIIIConfiguration> deviceModeSelectCommand = new ModeSelectCommand<> (this.model, surface, Modes.DEVICE_PARAMS);
        this.addButton (ButtonID.ARROW_UP, "Up", (event, value) -> {
            if (event != ButtonEvent.DOWN)
                return;
            final IBrowser browser = this.model.getBrowser ();
            if (browser != null && browser.isActive ())
                browser.stopBrowsing (!this.getSurface ().isShiftPressed ());
            final ModeManager modeManager = this.getSurface ().getModeManager ();
            if (modeManager.isActiveMode (Modes.DEVICE_PARAMS))
                ((ParametersMode) modeManager.getMode (Modes.DEVICE_PARAMS)).toggleShowDevices ();
            else
                deviceModeSelectCommand.execute (ButtonEvent.DOWN, 127);
        }, SLMkIIIControlSurface.MKIII_DISPLAY_UP);

        this.addButton (ButtonID.ARROW_DOWN, "Down", new TrackModeCommand (this.model, surface), SLMkIIIControlSurface.MKIII_DISPLAY_DOWN);

        this.addButton (ButtonID.SHIFT, "Shift", new ShiftCommand<> (this.model, surface), SLMkIIIControlSurface.MKIII_SHIFT);
        this.addButton (ButtonID.USER, "Options", new ModeSelectCommand<> (this.model, surface, Modes.FUNCTIONS, true), SLMkIIIControlSurface.MKIII_OPTIONS);

        this.addButton (ButtonID.OCTAVE_UP, "Octave Up", (event, value) -> {
            if (event == ButtonEvent.UP)
                surface.toggleMuteSolo ();
        }, SLMkIIIControlSurface.MKIII_BUTTONS_UP);
        this.addButton (ButtonID.OCTAVE_DOWN, "Octave Down", (event, value) -> {
            if (event == ButtonEvent.UP)
                surface.toggleMuteSolo ();
        }, SLMkIIIControlSurface.MKIII_BUTTONS_DOWN);

        this.addButton (ButtonID.ARROW_LEFT, "Left", new SLMkIIICursorCommand (Direction.LEFT, this.model, surface), SLMkIIIControlSurface.MKIII_TRACK_LEFT);
        this.addButton (ButtonID.ARROW_RIGHT, "Right", new SLMkIIICursorCommand (Direction.RIGHT, this.model, surface), SLMkIIIControlSurface.MKIII_TRACK_RIGHT);

        this.addButton (ButtonID.SCENE1, "Scene 1", new SceneCommand<> (0, this.model, surface), SLMkIIIControlSurface.MKIII_SCENE_1);
        this.addButton (ButtonID.SCENE2, "Scene 2", new SceneCommand<> (1, this.model, surface), SLMkIIIControlSurface.MKIII_SCENE_2);

        this.addButton (ButtonID.SCENE7, "Scene Up", (event, value) -> {
            if (event != ButtonEvent.DOWN)
                return;
            final ViewManager viewManager = this.getSurface ().getViewManager ();
            if (viewManager.isActiveView (Views.SESSION))
                this.model.getSceneBank ().scrollBackwards ();
            else if (viewManager.isActiveView (Views.DRUM))
            {
                final DrumView drumView = (DrumView) viewManager.getView (Views.DRUM);
                if (drumView.isPlayMode ())
                    drumView.onOctaveUp (ButtonEvent.DOWN);
                else
                    drumView.onLeft (ButtonEvent.DOWN);
            }
            else if (viewManager.isActiveView (Views.COLOR))
                ((ColorView) viewManager.getView (Views.COLOR)).setFlip (false);
        }, SLMkIIIControlSurface.MKIII_SCENE_UP);
        this.addButton (ButtonID.SCENE8, "Scene Down", (event, value) -> {
            if (event != ButtonEvent.DOWN)
                return;
            final ViewManager viewManager = this.getSurface ().getViewManager ();
            if (viewManager.isActiveView (Views.SESSION))
                this.model.getSceneBank ().scrollForwards ();
            else if (viewManager.isActiveView (Views.DRUM))
            {
                final DrumView drumView = (DrumView) viewManager.getView (Views.DRUM);
                if (drumView.isPlayMode ())
                    drumView.onOctaveDown (ButtonEvent.DOWN);
                else
                    drumView.onRight (ButtonEvent.DOWN);
            }
            else if (viewManager.isActiveView (Views.COLOR))
                ((ColorView) viewManager.getView (Views.COLOR)).setFlip (true);
        }, SLMkIIIControlSurface.MKIII_SCENE_DOWN);

        this.addButton (ButtonID.SESSION, "Grid", (event, value) -> {
            if (event != ButtonEvent.DOWN)
                return;
            final ViewManager viewManager = surface.getViewManager ();
            viewManager.setActiveView (viewManager.isActiveView (Views.SESSION) ? Views.DRUM : Views.SESSION);
            this.getSurface ().getDisplay ().notify (viewManager.isActiveView (Views.SESSION) ? "Session" : "Sequencer");
        }, SLMkIIIControlSurface.MKIII_GRID);

        this.addButton (ButtonID.DUPLICATE, "Duplicate", NopCommand.INSTANCE, SLMkIIIControlSurface.MKIII_DUPLICATE);
        this.addButton (ButtonID.DELETE, "Clear", NopCommand.INSTANCE, SLMkIIIControlSurface.MKIII_CLEAR);

    }


    /** {@inheritDoc} */
    @Override
    protected void registerContinuousCommands ()
    {
        final SLMkIIIControlSurface surface = this.getSurface ();
        for (int i = 0; i < 8; i++)
        {
            this.addRelativeKnob (ContinuousID.get (ContinuousID.KNOB1, i), "Knob " + (i + 1), new KnobRowModeCommand<> (i, this.model, surface), SLMkIIIControlSurface.MKIII_KNOB_1 + i);
            this.addFader (ContinuousID.get (ContinuousID.FADER1, i), "Fader " + (i + 1), new VolumeFaderCommand (i, this.model, surface), BindType.CC, SLMkIIIControlSurface.MKIII_FADER_1 + i);
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void layoutControls ()
    {
        final SLMkIIIControlSurface surface = this.getSurface ();

        surface.getPianoKeyboard ().setBounds (129.5, 201.0, 834.0, 157.0);

        surface.getTextDisplay ().getHardwareDisplay ().setBounds (277.25, 50.0, 280.5, 41.0);

        surface.getButton (ButtonID.PAD1).setBounds (273.25, 149.0, 24.25, 22.5);
        surface.getButton (ButtonID.PAD2).setBounds (302.75, 149.0, 24.25, 22.5);
        surface.getButton (ButtonID.PAD3).setBounds (332.25, 149.0, 24.25, 22.5);
        surface.getButton (ButtonID.PAD4).setBounds (361.75, 149.0, 24.25, 22.5);
        surface.getButton (ButtonID.PAD5).setBounds (391.25, 149.0, 24.25, 22.5);
        surface.getButton (ButtonID.PAD6).setBounds (420.75, 149.0, 24.25, 22.5);
        surface.getButton (ButtonID.PAD7).setBounds (450.25, 149.0, 24.25, 22.5);
        surface.getButton (ButtonID.PAD8).setBounds (479.75, 149.0, 24.25, 22.5);
        surface.getButton (ButtonID.PAD9).setBounds (273.5, 121.25, 24.25, 22.5);
        surface.getButton (ButtonID.PAD10).setBounds (303.0, 121.25, 24.25, 22.5);
        surface.getButton (ButtonID.PAD11).setBounds (332.5, 121.25, 24.25, 22.5);
        surface.getButton (ButtonID.PAD12).setBounds (362.0, 121.25, 24.25, 22.5);
        surface.getButton (ButtonID.PAD13).setBounds (391.5, 121.25, 24.25, 22.5);
        surface.getButton (ButtonID.PAD14).setBounds (420.75, 121.25, 24.25, 22.5);
        surface.getButton (ButtonID.PAD15).setBounds (450.25, 121.25, 24.25, 22.5);
        surface.getButton (ButtonID.PAD16).setBounds (479.75, 121.25, 24.25, 22.5);
        surface.getButton (ButtonID.REWIND).setBounds (821.25, 153.0, 21.0, 18.5);
        surface.getButton (ButtonID.FORWARD).setBounds (846.25, 153.0, 21.0, 18.5);
        surface.getButton (ButtonID.LOOP).setBounds (921.0, 153.0, 21.0, 18.5);
        surface.getButton (ButtonID.STOP).setBounds (871.0, 153.0, 21.0, 18.5);
        surface.getButton (ButtonID.PLAY).setBounds (896.0, 153.0, 21.0, 18.5);
        surface.getButton (ButtonID.RECORD).setBounds (946.0, 153.0, 21.0, 18.5);
        surface.getButton (ButtonID.ROW1_1).setBounds (274.25, 103.0, 23.75, 12.75);
        surface.getButton (ButtonID.ROW2_1).setBounds (573.75, 53.0, 22.0, 12.75);
        surface.getButton (ButtonID.ROW3_1).setBounds (573.75, 70.0, 22.0, 12.75);
        surface.getButton (ButtonID.ROW1_2).setBounds (303.5, 103.0, 23.75, 12.75);
        surface.getButton (ButtonID.ROW2_2).setBounds (603.75, 53.5, 22.0, 12.75);
        surface.getButton (ButtonID.ROW3_2).setBounds (603.75, 70.5, 22.0, 12.75);
        surface.getButton (ButtonID.ROW1_3).setBounds (332.75, 103.0, 23.75, 12.75);
        surface.getButton (ButtonID.ROW2_3).setBounds (633.75, 53.5, 22.0, 12.75);
        surface.getButton (ButtonID.ROW3_3).setBounds (633.75, 70.5, 22.0, 12.75);
        surface.getButton (ButtonID.ROW1_4).setBounds (362.0, 103.0, 23.75, 12.75);
        surface.getButton (ButtonID.ROW2_4).setBounds (663.75, 53.5, 22.0, 12.75);
        surface.getButton (ButtonID.ROW3_4).setBounds (663.75, 70.5, 22.0, 12.75);
        surface.getButton (ButtonID.ROW1_5).setBounds (391.25, 103.0, 23.75, 12.75);
        surface.getButton (ButtonID.ROW2_5).setBounds (693.75, 53.5, 22.0, 12.75);
        surface.getButton (ButtonID.ROW3_5).setBounds (693.75, 70.5, 22.0, 12.75);
        surface.getButton (ButtonID.ROW1_6).setBounds (420.5, 103.0, 23.75, 12.75);
        surface.getButton (ButtonID.ROW2_6).setBounds (723.75, 53.5, 22.0, 12.75);
        surface.getButton (ButtonID.ROW3_6).setBounds (723.75, 70.5, 22.0, 12.75);
        surface.getButton (ButtonID.ROW1_7).setBounds (450.0, 103.0, 23.75, 12.75);
        surface.getButton (ButtonID.ROW2_7).setBounds (753.75, 53.5, 22.0, 12.75);
        surface.getButton (ButtonID.ROW3_7).setBounds (753.75, 70.5, 22.0, 12.75);
        surface.getButton (ButtonID.ROW1_8).setBounds (479.25, 103.0, 23.75, 12.75);
        surface.getButton (ButtonID.ROW2_8).setBounds (783.75, 53.5, 22.0, 12.75);
        surface.getButton (ButtonID.ROW3_8).setBounds (783.75, 70.5, 22.0, 12.75);
        surface.getButton (ButtonID.ARROW_UP).setBounds (238.75, 53.0, 28.5, 12.75);
        surface.getButton (ButtonID.ARROW_DOWN).setBounds (238.75, 68.25, 28.5, 12.75);
        surface.getButton (ButtonID.SHIFT).setBounds (32.25, 63.0, 27.25, 13.75);
        surface.getButton (ButtonID.USER).setBounds (513.25, 103.0, 22.0, 12.75);
        surface.getButton (ButtonID.OCTAVE_UP).setBounds (74.5, 193.25, 27.25, 13.75);
        surface.getButton (ButtonID.OCTAVE_DOWN).setBounds (32.25, 193.25, 27.25, 13.75);
        surface.getButton (ButtonID.ARROW_LEFT).setBounds (32.25, 164.0, 27.25, 13.75);
        surface.getButton (ButtonID.ARROW_RIGHT).setBounds (74.5, 164.0, 27.25, 13.75);
        surface.getButton (ButtonID.SCENE1).setBounds (513.25, 121.25, 24.25, 22.5);
        surface.getButton (ButtonID.SCENE2).setBounds (513.25, 149.0, 24.25, 22.5);
        surface.getButton (ButtonID.SCENE7).setBounds (238.75, 120.25, 29.25, 22.5);
        surface.getButton (ButtonID.SCENE8).setBounds (238.75, 149.0, 29.25, 22.5);
        surface.getButton (ButtonID.SESSION).setBounds (238.75, 102.0, 29.5, 12.75);
        surface.getButton (ButtonID.DUPLICATE).setBounds (32.25, 126.75, 27.25, 13.75);
        surface.getButton (ButtonID.DELETE).setBounds (32.25, 144.5, 27.25, 13.75);

        surface.getContinuous (ContinuousID.KNOB1).setBounds (271.5, 11.25, 24.5, 23.75);
        surface.getContinuous (ContinuousID.KNOB2).setBounds (301.0, 11.25, 24.5, 23.75);
        surface.getContinuous (ContinuousID.KNOB3).setBounds (330.75, 11.25, 24.5, 23.75);
        surface.getContinuous (ContinuousID.KNOB4).setBounds (360.25, 11.25, 24.5, 23.75);
        surface.getContinuous (ContinuousID.KNOB5).setBounds (389.75, 11.25, 24.5, 23.75);
        surface.getContinuous (ContinuousID.KNOB6).setBounds (419.5, 11.25, 24.5, 23.75);
        surface.getContinuous (ContinuousID.KNOB7).setBounds (449.0, 11.25, 24.5, 23.75);
        surface.getContinuous (ContinuousID.KNOB8).setBounds (478.5, 11.25, 24.5, 23.75);
        surface.getContinuous (ContinuousID.FADER1).setBounds (575.75, 107.75, 21.25, 63.25);
        surface.getContinuous (ContinuousID.FADER2).setBounds (605.75, 107.75, 21.25, 63.25);
        surface.getContinuous (ContinuousID.FADER3).setBounds (635.5, 107.75, 21.25, 63.25);
        surface.getContinuous (ContinuousID.FADER4).setBounds (665.5, 107.75, 21.25, 63.25);
        surface.getContinuous (ContinuousID.FADER5).setBounds (695.5, 107.75, 21.25, 63.25);
        surface.getContinuous (ContinuousID.FADER6).setBounds (725.25, 107.75, 21.25, 63.25);
        surface.getContinuous (ContinuousID.FADER7).setBounds (755.25, 107.75, 21.25, 63.25);
        surface.getContinuous (ContinuousID.FADER8).setBounds (785.25, 107.75, 21.25, 63.25);
    }


    /** {@inheritDoc} */
    @Override
    public void startup ()
    {
        final SLMkIIIControlSurface surface = this.getSurface ();
        surface.getViewManager ().setActiveView (Views.SESSION);

        final ModeManager modeManager = surface.getModeManager ();
        modeManager.setActiveMode (Modes.TRACK);

        this.host.scheduleTask ( () -> surface.getMidiOutput ().sendSysex (DeviceInquiry.createQuery ()), 1000);
    }

    // /** {@inheritDoc} */
    // @Override
    // protected void updateButtons ()
    // {
    // final SLMkIIIControlSurface surface = this.getSurface ();
    // final ITransport t = this.model.getTransport ();
    // final boolean isShift = surface.isShiftPressed ();
    //
    // final ViewManager viewManager = surface.getViewManager ();

    // TODO Button color udpate

    // final ModeManager modeManager = surface.getModeManager ();
    //
    // surface.updateTrigger (SLMkIIIControlSurface.MKIII_DISPLAY_UP, getDeviceModeColor
    // (modeManager));
    // surface.updateTrigger (SLMkIIIControlSurface.MKIII_DISPLAY_DOWN, getTrackModeColor
    // (modeManager));
    //
    // surface.updateTrigger (SLMkIIIControlSurface.MKIII_BUTTONS_UP, surface.isMuteSolo () ?
    // SLMkIIIColors.SLMKIII_ORANGE : SLMkIIIColors.SLMKIII_ORANGE_HALF);
    // surface.updateTrigger (SLMkIIIControlSurface.MKIII_BUTTONS_DOWN, !surface.isMuteSolo () ?
    // SLMkIIIColors.SLMKIII_RED : SLMkIIIColors.SLMKIII_RED_HALF);
    //
    // surface.updateTrigger (SLMkIIIControlSurface.MKIII_DUPLICATE, surface.isPressed
    // (SLMkIIIControlSurface.MKIII_DUPLICATE) ? SLMkIIIColors.SLMKIII_AMBER :
    // SLMkIIIColors.SLMKIII_AMBER_HALF);
    // surface.updateTrigger (SLMkIIIControlSurface.MKIII_CLEAR, surface.isPressed
    // (SLMkIIIControlSurface.MKIII_CLEAR) ? SLMkIIIColors.SLMKIII_AMBER :
    // SLMkIIIColors.SLMKIII_AMBER_HALF);
    //
    // surface.updateTrigger (SLMkIIIControlSurface.MKIII_SCENE_UP, this.getSceneUpColor ());
    // surface.updateTrigger (SLMkIIIControlSurface.MKIII_SCENE_DOWN, this.getSceneDownColor
    // ());
    //
    // surface.updateTrigger (SLMkIIIControlSurface.MKIII_GRID, viewManager.isActiveView
    // (Views.SESSION) ? SLMkIIIColors.SLMKIII_GREEN : SLMkIIIColors.SLMKIII_BLUE);
    // surface.updateTrigger (SLMkIIIControlSurface.MKIII_OPTIONS,
    // modeManager.isActiveOrTempMode (Modes.FUNCTIONS) ? SLMkIIIColors.SLMKIII_BROWN_DARK :
    // SLMkIIIColors.SLMKIII_DARK_GREY);

    // final SLMkIIIDisplay display = surface.getDisplay ();
    // final ITrackBank tb = this.model.getTrackBank ();
    // final double max = this.valueChanger.getUpperBound ();for(
    // int i = 0;i<8;i++)
    // {
    // final ITrack track = tb.getItem (i);
    // final double [] color = track.getColor ();
    // display.setFaderLEDColor (SLMkIIIControlSurface.MKIII_FADER_LED_1 + i, track.getVolume () /
    // max, color);
    // }
    //
    // final View activeView = viewManager.getActiveView ();if(activeView!=null)
    // {
    // // TODO ((CursorCommand<?, ?>) activeView.getTriggerCommand
    // // (TriggerCommandID.ARROW_LEFT)).updateArrows ();
    // if (activeView instanceof SceneView)
    // {
    // // for (int i = 0; i < this.model.getSceneBank ().getPageSize (); i++)
    // // ((SceneView) activeView).updateSceneButton (i);
    // }
    // }
    //
    // this.updateSoloMuteButtons();
    // }


    /**
     * Update the 16 button LEDs.
     */
    public void updateSoloMuteButtons ()
    {
        final SLMkIIIControlSurface surface = this.getSurface ();
        final ITrackBank tb = this.model.getTrackBank ();
        for (int i = 0; i < 8; i++)
        {
            final ITrack track = tb.getItem (i);

            int color1;
            int color2;

            final boolean exists = track.doesExist ();
            if (surface.isMuteSolo ())
            {
                color1 = exists ? track.isMute () ? SLMkIIIColorManager.SLMKIII_ORANGE : SLMkIIIColorManager.SLMKIII_ORANGE_HALF : SLMkIIIColorManager.SLMKIII_BLACK;
                color2 = exists ? track.isSolo () ? SLMkIIIColorManager.SLMKIII_YELLOW : SLMkIIIColorManager.SLMKIII_YELLOW_HALF : SLMkIIIColorManager.SLMKIII_BLACK;
            }
            else
            {
                color1 = exists ? track.isMonitor () ? SLMkIIIColorManager.SLMKIII_GREEN : SLMkIIIColorManager.SLMKIII_GREEN_HALF : SLMkIIIColorManager.SLMKIII_BLACK;
                color2 = exists ? track.isRecArm () ? SLMkIIIColorManager.SLMKIII_RED : SLMkIIIColorManager.SLMKIII_RED_HALF : SLMkIIIColorManager.SLMKIII_BLACK;
            }

            // TODO
            // surface.updateTrigger (SLMkIIIControlSurface.MKIII_BUTTON_ROW1_1 + i, color1);
            // surface.updateTrigger (SLMkIIIControlSurface.MKIII_BUTTON_ROW2_1 + i, color2);
        }
    }


    private int getSceneDownColor ()
    {
        final ViewManager viewManager = this.getSurface ().getViewManager ();
        if (viewManager.isActiveView (Views.SESSION))
            return this.model.getSceneBank ().canScrollForwards () ? SLMkIIIColorManager.SLMKIII_GREEN : SLMkIIIColorManager.SLMKIII_BLACK;
        else if (viewManager.isActiveView (Views.DRUM))
            return ((DrumView) viewManager.getView (Views.DRUM)).isPlayMode () ? SLMkIIIColorManager.SLMKIII_BLUE : SLMkIIIColorManager.SLMKIII_SKY_BLUE;
        else if (viewManager.isActiveView (Views.COLOR))
            return !((ColorView) viewManager.getView (Views.COLOR)).isFlip () ? SLMkIIIColorManager.SLMKIII_RED : SLMkIIIColorManager.SLMKIII_BLACK;
        return SLMkIIIColorManager.SLMKIII_BLACK;
    }


    private int getSceneUpColor ()
    {
        final ViewManager viewManager = this.getSurface ().getViewManager ();
        if (viewManager.isActiveView (Views.SESSION))
            return this.model.getSceneBank ().canScrollBackwards () ? SLMkIIIColorManager.SLMKIII_GREEN : SLMkIIIColorManager.SLMKIII_BLACK;
        else if (viewManager.isActiveView (Views.DRUM))
            return ((DrumView) viewManager.getView (Views.DRUM)).isPlayMode () ? SLMkIIIColorManager.SLMKIII_BLUE : SLMkIIIColorManager.SLMKIII_SKY_BLUE;
        else if (viewManager.isActiveView (Views.COLOR))
            return ((ColorView) viewManager.getView (Views.COLOR)).isFlip () ? SLMkIIIColorManager.SLMKIII_RED : SLMkIIIColorManager.SLMKIII_BLACK;
        return SLMkIIIColorManager.SLMKIII_BLACK;
    }


    private static int getDeviceModeColor (final ModeManager modeManager)
    {
        if (modeManager.isActiveMode (Modes.DEVICE_PARAMS))
        {
            if (((ParametersMode) modeManager.getMode (Modes.DEVICE_PARAMS)).isShowDevices ())
                return SLMkIIIColorManager.SLMKIII_MINT;
            return SLMkIIIColorManager.SLMKIII_PURPLE;
        }
        return SLMkIIIColorManager.SLMKIII_WHITE_HALF;
    }


    private static int getTrackModeColor (final ModeManager modeManager)
    {
        if (modeManager.isActiveMode (Modes.TRACK))
            return SLMkIIIColorManager.SLMKIII_GREEN;
        if (modeManager.isActiveMode (Modes.VOLUME))
            return SLMkIIIColorManager.SLMKIII_BLUE;
        if (modeManager.isActiveMode (Modes.PAN))
            return SLMkIIIColorManager.SLMKIII_ORANGE;
        if (Modes.isSendMode (modeManager.getActiveModeId ()))
            return SLMkIIIColorManager.SLMKIII_YELLOW;

        return SLMkIIIColorManager.SLMKIII_WHITE_HALF;
    }


    /** {@inheritDoc} */
    @Override
    protected void updateIndication (final Modes mode)
    {
        if (this.currentMode != null && this.currentMode.equals (mode))
            return;

        if (mode != null)
            this.currentMode = mode;

        final ITrackBank tb = this.model.getTrackBank ();
        final SLMkIIIControlSurface surface = this.getSurface ();
        final boolean isSession = surface.getViewManager ().isActiveView (Views.SESSION);
        final boolean isTrackMode = Modes.TRACK.equals (this.currentMode);
        final boolean isPan = Modes.PAN.equals (this.currentMode);
        final boolean isDevice = Modes.isDeviceMode (this.currentMode) || Modes.isLayerMode (this.currentMode);

        tb.setIndication (isSession);

        final ICursorDevice cursorDevice = this.model.getCursorDevice ();
        final ITrack selectedTrack = tb.getSelectedItem ();
        final IParameterBank parameterBank = cursorDevice.getParameterBank ();
        for (int i = 0; i < tb.getPageSize (); i++)
        {
            final boolean hasTrackSel = selectedTrack != null && selectedTrack.getIndex () == i && isTrackMode;
            final ITrack track = tb.getItem (i);
            // Alayes true since faders are always active
            track.setVolumeIndication (true);
            track.setPanIndication (isPan || hasTrackSel);

            final ISendBank sendBank = track.getSendBank ();
            for (int j = 0; j < sendBank.getPageSize (); j++)
                sendBank.getItem (j).setIndication (this.currentMode.ordinal () - Modes.SEND1.ordinal () == j || hasTrackSel);

            parameterBank.getItem (i).setIndication (isDevice);
        }
    }
}
