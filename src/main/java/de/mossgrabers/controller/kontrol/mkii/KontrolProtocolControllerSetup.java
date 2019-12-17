// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.kontrol.mkii;

import de.mossgrabers.controller.kontrol.mkii.command.trigger.KontrolRecordCommand;
import de.mossgrabers.controller.kontrol.mkii.command.trigger.StartClipOrSceneCommand;
import de.mossgrabers.controller.kontrol.mkii.controller.KontrolProtocolColorManager;
import de.mossgrabers.controller.kontrol.mkii.controller.KontrolProtocolControlSurface;
import de.mossgrabers.controller.kontrol.mkii.controller.SlowValueChanger;
import de.mossgrabers.controller.kontrol.mkii.mode.MixerMode;
import de.mossgrabers.controller.kontrol.mkii.mode.ParamsMode;
import de.mossgrabers.controller.kontrol.mkii.mode.SendMode;
import de.mossgrabers.controller.kontrol.mkii.view.ControlView;
import de.mossgrabers.framework.command.continuous.KnobRowModeCommand;
import de.mossgrabers.framework.command.core.NopCommand;
import de.mossgrabers.framework.command.trigger.application.RedoCommand;
import de.mossgrabers.framework.command.trigger.application.UndoCommand;
import de.mossgrabers.framework.command.trigger.clip.NewCommand;
import de.mossgrabers.framework.command.trigger.clip.QuantizeCommand;
import de.mossgrabers.framework.command.trigger.clip.StartSceneCommand;
import de.mossgrabers.framework.command.trigger.clip.StopClipCommand;
import de.mossgrabers.framework.command.trigger.mode.ModeMultiSelectCommand;
import de.mossgrabers.framework.command.trigger.track.MuteCommand;
import de.mossgrabers.framework.command.trigger.track.SoloCommand;
import de.mossgrabers.framework.command.trigger.transport.MetronomeCommand;
import de.mossgrabers.framework.command.trigger.transport.PlayCommand;
import de.mossgrabers.framework.command.trigger.transport.StopCommand;
import de.mossgrabers.framework.command.trigger.transport.TapTempoCommand;
import de.mossgrabers.framework.command.trigger.transport.ToggleLoopCommand;
import de.mossgrabers.framework.command.trigger.transport.WriteArrangerAutomationCommand;
import de.mossgrabers.framework.configuration.AbstractConfiguration;
import de.mossgrabers.framework.configuration.ISettingsUI;
import de.mossgrabers.framework.controller.AbstractControllerSetup;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.ContinuousID;
import de.mossgrabers.framework.controller.IControlSurface;
import de.mossgrabers.framework.controller.ISetupFactory;
import de.mossgrabers.framework.controller.hardware.BindType;
import de.mossgrabers.framework.daw.ICursorDevice;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.IParameterBank;
import de.mossgrabers.framework.daw.ISceneBank;
import de.mossgrabers.framework.daw.ISendBank;
import de.mossgrabers.framework.daw.ITrackBank;
import de.mossgrabers.framework.daw.ITransport;
import de.mossgrabers.framework.daw.ModelSetup;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.midi.IMidiAccess;
import de.mossgrabers.framework.daw.midi.IMidiOutput;
import de.mossgrabers.framework.mode.Mode;
import de.mossgrabers.framework.mode.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.utils.FrameworkException;
import de.mossgrabers.framework.utils.OperatingSystem;
import de.mossgrabers.framework.view.ViewManager;
import de.mossgrabers.framework.view.Views;


/**
 * Setup for the Komplete Kontrol NIHIA protocol.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class KontrolProtocolControllerSetup extends AbstractControllerSetup<KontrolProtocolControlSurface, KontrolProtocolConfiguration>
{
    private final int version;
    private String    kompleteInstance = "";


    /**
     * Constructor.
     *
     * @param host The DAW host
     * @param factory The factory
     * @param globalSettings The global settings
     * @param documentSettings The document (project) specific settings
     * @param version The version number of the NIHIA protocol to support
     */
    public KontrolProtocolControllerSetup (final IHost host, final ISetupFactory factory, final ISettingsUI globalSettings, final ISettingsUI documentSettings, final int version)
    {
        super (factory, host, globalSettings, documentSettings);

        this.version = version;
        this.colorManager = new KontrolProtocolColorManager ();
        this.valueChanger = new SlowValueChanger (1024, 5, 1);
        this.configuration = new KontrolProtocolConfiguration (host, this.valueChanger);
    }


    /** {@inheritDoc} */
    @Override
    public void init ()
    {
        if (OperatingSystem.get () == OperatingSystem.LINUX)
            throw new FrameworkException ("Komplete Kontrol MkII is not supported on Linux since there is no Native Instruments DAW Integration Host.");

        super.init ();
    }


    /** {@inheritDoc} */
    @Override
    public void flush ()
    {
        // Do not flush until handshake has finished
        if (!this.getSurface ().isConnectedToNIHIA ())
            return;

        final KontrolProtocolControlSurface surface = this.getSurface ();
        final String kompleteInstanceNew = this.getKompleteInstance ();
        if (!this.kompleteInstance.equals (kompleteInstanceNew))
        {
            this.kompleteInstance = kompleteInstanceNew;
            surface.sendKontrolTrackSysEx (KontrolProtocolControlSurface.KONTROL_TRACK_INSTANCE, 0, 0, kompleteInstanceNew);
        }

        final ITrackBank bank = this.model.getCurrentTrackBank ();

        final boolean hasSolo = this.model.hasSolo ();
        for (int i = 0; i < 8; i++)
        {
            final ITrack track = bank.getItem (i);
            surface.sendKontrolTrackSysEx (KontrolProtocolControlSurface.KONTROL_TRACK_MUTE, track.isMute () ? 1 : 0, i);
            surface.sendKontrolTrackSysEx (KontrolProtocolControlSurface.KONTROL_TRACK_SOLO, track.isSolo () ? 1 : 0, i);
            surface.sendKontrolTrackSysEx (KontrolProtocolControlSurface.KONTROL_TRACK_MUTED_BY_SOLO, !track.isSolo () && hasSolo ? 1 : 0, i);
        }

        final ITrackBank tb = this.model.getCurrentTrackBank ();
        final ITrack selectedTrack = tb.getSelectedItem ();
        surface.sendCommand (KontrolProtocolControlSurface.KONTROL_SELECTED_TRACK_AVAILABLE, selectedTrack != null ? TrackType.toTrackType (selectedTrack.getType ()) : 0);
        surface.sendCommand (KontrolProtocolControlSurface.KONTROL_SELECTED_TRACK_MUTED_BY_SOLO, selectedTrack != null && !selectedTrack.isSolo () && hasSolo ? 1 : 0);

        super.flush ();
    }


    /** {@inheritDoc} */
    @Override
    protected void createScales ()
    {
        this.scales = new Scales (this.valueChanger, 0, 128, 128, 1);
        this.scales.setChromatic (true);
    }


    /** {@inheritDoc} */
    @Override
    protected void createModel ()
    {
        final ModelSetup ms = new ModelSetup ();
        ms.setHasFullFlatTrackList (true);
        ms.setNumFilterColumnEntries (0);
        ms.setNumResults (0);
        ms.setNumDeviceLayers (0);
        ms.setNumDrumPadLayers (0);
        ms.setNumMarkers (0);
        this.model = this.factory.createModel (this.colorManager, this.valueChanger, this.scales, ms);
    }


    /** {@inheritDoc} */
    @Override
    protected void createSurface ()
    {
        final IMidiAccess midiAccess = this.factory.createMidiAccess ();
        final IMidiOutput output = midiAccess.createOutput ();
        midiAccess.createInput (1, "Keyboard", "80????" /* Note off */, "90????" /* Note on */,
                "B0????" /* Sustainpedal + Modulation + Strip */, "D0????" /* Channel Aftertouch */,
                "E0????" /* Pitchbend */);
        this.surfaces.add (new KontrolProtocolControlSurface (this.host, this.colorManager, this.configuration, output, midiAccess.createInput (null), this.version));
    }


    /** {@inheritDoc} */
    @Override
    protected void createViews ()
    {
        final KontrolProtocolControlSurface surface = this.getSurface ();
        final ViewManager viewManager = surface.getViewManager ();
        viewManager.registerView (Views.CONTROL, new ControlView (surface, this.model));
    }


    /** {@inheritDoc} */
    @Override
    protected void createModes ()
    {
        final KontrolProtocolControlSurface surface = this.getSurface ();
        final ModeManager modeManager = surface.getModeManager ();

        modeManager.registerMode (Modes.VOLUME, new MixerMode (surface, this.model));
        modeManager.registerMode (Modes.SEND, new SendMode (surface, this.model));
        modeManager.registerMode (Modes.DEVICE_PARAMS, new ParamsMode (surface, this.model));
    }


    /** {@inheritDoc} */
    @Override
    protected void createObservers ()
    {
        this.getSurface ().getModeManager ().addModeListener ( (oldMode, newMode) -> this.updateIndication (newMode));

        this.configuration.addSettingObserver (AbstractConfiguration.KNOB_SPEED_NORMAL, () -> this.valueChanger.setFractionValue (this.configuration.getKnobSpeedNormal ()));
        this.configuration.addSettingObserver (AbstractConfiguration.KNOB_SPEED_SLOW, () -> this.valueChanger.setSlowFractionValue (this.configuration.getKnobSpeedSlow ()));
    }


    /** {@inheritDoc} */
    @Override
    protected void registerTriggerCommands ()
    {
        final KontrolProtocolControlSurface surface = this.getSurface ();
        final ITransport t = this.model.getTransport ();

        this.addButton (ButtonID.PLAY, "Play", new PlayCommand<> (this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_PLAY, t::isPlaying);
        this.addButton (ButtonID.NEW, "Shift+\nPlay", new NewCommand<> (this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_RESTART);
        this.addButton (ButtonID.RECORD, "Record", new KontrolRecordCommand (true, this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_RECORD, this.model::hasRecordingState);
        this.addButton (ButtonID.REC_ARM, "Shift+\nRecord", new KontrolRecordCommand (false, this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_COUNT_IN, this.model::hasRecordingState);
        this.addButton (ButtonID.STOP, "Stop", new StopCommand<> (this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_STOP, () -> !t.isPlaying ());

        this.addButton (ButtonID.LOOP, "Loop", new ToggleLoopCommand<> (this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_LOOP, t::isLoop);
        this.addButton (ButtonID.METRONOME, "Metronome", new MetronomeCommand<> (this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_METRO, t::isMetronomeOn);
        this.addButton (ButtonID.TAP_TEMPO, "Tempo", new TapTempoCommand<> (this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_TEMPO);

        // Note: Since there is no pressed-state with this device, in the sim-GUI the following
        // buttons are always on
        this.addButton (ButtonID.UNDO, "Undo", new UndoCommand<> (this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_UNDO, () -> true);
        this.addButton (ButtonID.REDO, "Redo", new RedoCommand<> (this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_REDO, () -> true);
        this.addButton (ButtonID.QUANTIZE, "Quantize", new QuantizeCommand<> (this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_QUANTIZE, () -> true);
        this.addButton (ButtonID.AUTOMATION, "Automation", new WriteArrangerAutomationCommand<> (this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_AUTOMATION, t::isWritingArrangerAutomation);

        this.addButton (ButtonID.DELETE, "Modes", new ModeMultiSelectCommand<> (this.model, surface, Modes.VOLUME, Modes.SEND, Modes.DEVICE_PARAMS), 15, KontrolProtocolControlSurface.KONTROL_CLEAR, () -> true);

        this.addButton (ButtonID.CLIP, "Start Clip", new StartClipOrSceneCommand (this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_PLAY_SELECTED_CLIP);
        this.addButton (ButtonID.STOP_CLIP, "Stop Clip", new StopClipCommand<> (this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_STOP_CLIP);
        // Not implemented in NIHIA
        this.addButton (ButtonID.SCENE1, "Play Scene", new StartSceneCommand<> (this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_PLAY_SCENE);

        // KONTROL_RECORD_SESSION - Not implemented in NIHIA

        this.addButton (ButtonID.MUTE, "Mute", new MuteCommand<> (this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_SELECTED_TRACK_MUTE, () -> {
            final ITrackBank tb = this.model.getCurrentTrackBank ();
            final ITrack selectedTrack = tb.getSelectedItem ();
            return selectedTrack != null && selectedTrack.isMute () ? 1 : 0;
        });
        this.addButton (ButtonID.SOLO, "Solo", new SoloCommand<> (this.model, surface), 15, KontrolProtocolControlSurface.KONTROL_SELECTED_TRACK_SOLO, () -> {
            final ITrackBank tb = this.model.getCurrentTrackBank ();
            final ITrack selectedTrack = tb.getSelectedItem ();
            return selectedTrack != null && selectedTrack.isSolo () ? 1 : 0;
        });

        this.addButtons (surface, 0, 8, ButtonID.ROW_SELECT_1, "Select", (event, index) -> {
            if (event == ButtonEvent.DOWN)
                this.model.getCurrentTrackBank ().getItem (index).select ();
        }, 15, KontrolProtocolControlSurface.KONTROL_TRACK_SELECTED, index -> this.model.getTrackBank ().getItem (index).isSelected () ? 1 : 0);

        this.addButtons (surface, 0, 8, ButtonID.ROW1_1, "Mute", (event, index) -> {
            if (event == ButtonEvent.DOWN)
                this.model.getTrackBank ().getItem (index).toggleMute ();
        }, 15, KontrolProtocolControlSurface.KONTROL_TRACK_MUTE, index -> this.model.getTrackBank ().getItem (index).isMute () ? 1 : 0);

        this.addButtons (surface, 0, 8, ButtonID.ROW2_1, "Solo", (event, index) -> {
            if (event == ButtonEvent.DOWN)
                this.model.getTrackBank ().getItem (index).toggleSolo ();
        }, 15, KontrolProtocolControlSurface.KONTROL_TRACK_SOLO, index -> this.model.getTrackBank ().getItem (index).isSolo () ? 1 : 0);

        this.addButtons (surface, 0, 8, ButtonID.ROW3_1, "Arm", (event, index) -> {
            if (event == ButtonEvent.DOWN)
                this.model.getTrackBank ().getItem (index).toggleRecArm ();
        }, 15, KontrolProtocolControlSurface.KONTROL_TRACK_RECARM, index -> this.model.getTrackBank ().getItem (index).isRecArm () ? 1 : 0);

        this.addButton (ButtonID.F1, "", NopCommand.INSTANCE, 15, KontrolProtocolControlSurface.KONTROL_SELECTED_TRACK_AVAILABLE);
        this.addButton (ButtonID.F2, "", NopCommand.INSTANCE, 15, KontrolProtocolControlSurface.KONTROL_SELECTED_TRACK_MUTED_BY_SOLO);
    }


    /** {@inheritDoc} */
    @Override
    protected void registerContinuousCommands ()
    {
        final KontrolProtocolControlSurface surface = this.getSurface ();

        this.addFader (ContinuousID.HELLO, "Hello", surface::handshakeSuccess, BindType.CC, 15, KontrolProtocolControlSurface.CMD_HELLO);

        this.addFader (ContinuousID.MOVE_TRACK_BANK, "Move Track Bank", value -> {
            // These are the left/right buttons
            final Mode activeMode = this.getSurface ().getModeManager ().getActiveOrTempMode ();
            if (activeMode == null)
                return;
            if (value == 1)
                activeMode.selectNextItemPage ();
            else
                activeMode.selectPreviousItemPage ();
        }, BindType.CC, 15, KontrolProtocolControlSurface.KONTROL_NAVIGATE_BANKS);

        this.addFader (ContinuousID.MOVE_TRACK, "Move Track", value -> {
            if (this.getSurface ().getModeManager ().isActiveMode (Modes.VOLUME))
            {
                // This is encoder left/right
                if (this.configuration.isFlipTrackClipNavigation ())
                {
                    if (this.configuration.isFlipClipSceneNavigation ())
                        this.navigateScenes (value);
                    else
                        this.navigateClips (value);
                }
                else
                    this.navigateTracks (value);
                return;
            }

            final Mode activeMode = this.getSurface ().getModeManager ().getActiveOrTempMode ();
            if (activeMode == null)
                return;
            if (value == 1)
                activeMode.selectNextItem ();
            else
                activeMode.selectPreviousItem ();
        }, BindType.CC, 15, KontrolProtocolControlSurface.KONTROL_NAVIGATE_TRACKS);

        this.addFader (ContinuousID.NAVIGATE_CLIPS, "Navigate Clips", value -> {
            if (this.getSurface ().getModeManager ().isActiveMode (Modes.VOLUME))
            {
                // This is encoder up/down
                if (this.configuration.isFlipTrackClipNavigation ())
                    this.navigateTracks (value);
                else
                {
                    if (this.configuration.isFlipClipSceneNavigation ())
                        this.navigateScenes (value);
                    else
                        this.navigateClips (value);
                }
                return;
            }

            final Mode activeMode = this.getSurface ().getModeManager ().getActiveOrTempMode ();
            if (activeMode == null)
                return;
            if (value == 1)
                activeMode.selectNextItemPage ();
            else
                activeMode.selectPreviousItemPage ();
        }, BindType.CC, 15, KontrolProtocolControlSurface.KONTROL_NAVIGATE_CLIPS);

        this.addFader (ContinuousID.NAVIGATE_SCENES, "Navigate Scenes", value -> {
            if (this.configuration.isFlipTrackClipNavigation ())
                this.navigateTracks (value);
            else
            {
                if (this.configuration.isFlipClipSceneNavigation ())
                    this.navigateClips (value);
                else
                    this.navigateScenes (value);
            }
        }, BindType.CC, 15, KontrolProtocolControlSurface.KONTROL_NAVIGATE_SCENES);

        this.addRelativeKnob (ContinuousID.MOVE_TRANSPORT, "Move Transport", value -> this.changeTransportPosition (value, 0), BindType.CC, 15, KontrolProtocolControlSurface.KONTROL_NAVIGATE_MOVE_TRANSPORT);
        this.addRelativeKnob (ContinuousID.MOVE_LOOP, "Move Loop", this::changeLoopPosition, BindType.CC, 15, KontrolProtocolControlSurface.KONTROL_NAVIGATE_MOVE_LOOP);

        // Only on S models
        this.addFader (ContinuousID.NAVIGATE_VOLUME, "Navigate Volume", value -> this.changeTransportPosition (value, 1), BindType.CC, 15, KontrolProtocolControlSurface.KONTROL_CHANGE_SELECTED_TRACK_VOLUME);
        this.addFader (ContinuousID.NAVIGATE_PAN, "Navigate Pan", value -> this.changeTransportPosition (value, 2), BindType.CC, 15, KontrolProtocolControlSurface.KONTROL_CHANGE_SELECTED_TRACK_PAN);

        for (int i = 0; i < 8; i++)
        {
            final int index = i;
            final KnobRowModeCommand<KontrolProtocolControlSurface, KontrolProtocolConfiguration> knobCommand = new KnobRowModeCommand<> (index, this.model, surface);

            this.addRelativeKnob (ContinuousID.get (ContinuousID.KNOB1, i), "Knob " + (i + 1), knobCommand, BindType.CC, 15, KontrolProtocolControlSurface.KONTROL_TRACK_VOLUME + i);

            this.addRelativeKnob (ContinuousID.get (ContinuousID.FADER1, i), "Fader " + (i + 1), value -> {
                if (this.getSurface ().getModeManager ().isActiveMode (Modes.VOLUME))
                    this.model.getTrackBank ().getItem (index).changePan (value);
                else
                    knobCommand.execute (value);
            }, BindType.CC, 15, KontrolProtocolControlSurface.KONTROL_TRACK_PAN + i);
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void layoutControls ()
    {
        final KontrolProtocolControlSurface surface = this.getSurface ();

        surface.getButton (ButtonID.PLAY).setBounds (21.0, 158.5, 31.75, 22.75);
        surface.getButton (ButtonID.NEW).setBounds (21.0, 188.5, 31.75, 22.75);
        surface.getButton (ButtonID.RECORD).setBounds (63.75, 158.25, 31.75, 22.75);
        surface.getButton (ButtonID.REC_ARM).setBounds (63.75, 188.25, 31.75, 22.75);
        surface.getButton (ButtonID.STOP).setBounds (106.5, 158.5, 31.75, 22.75);
        surface.getButton (ButtonID.LOOP).setBounds (21.0, 128.75, 31.75, 22.75);
        surface.getButton (ButtonID.METRONOME).setBounds (63.75, 128.5, 31.75, 22.75);
        surface.getButton (ButtonID.TAP_TEMPO).setBounds (106.5, 128.75, 31.75, 22.75);
        surface.getButton (ButtonID.UNDO).setBounds (21.0, 43.0, 31.75, 22.75);
        surface.getButton (ButtonID.REDO).setBounds (21.0, 75.5, 31.75, 22.75);
        surface.getButton (ButtonID.QUANTIZE).setBounds (63.75, 43.0, 31.75, 22.75);
        surface.getButton (ButtonID.AUTOMATION).setBounds (106.5, 43.0, 31.75, 22.75);
        surface.getButton (ButtonID.DELETE).setBounds (212.25, 128.75, 31.75, 22.75);
        surface.getButton (ButtonID.CLIP).setBounds (530.75, 45.75, 31.75, 22.75);
        surface.getButton (ButtonID.STOP_CLIP).setBounds (568.25, 45.75, 31.75, 22.75);
        surface.getButton (ButtonID.SCENE1).setBounds (606.0, 45.75, 31.75, 22.75);
        surface.getButton (ButtonID.MUTE).setBounds (194.0, 43.0, 24.25, 22.75);
        surface.getButton (ButtonID.SOLO).setBounds (226.25, 43.0, 24.25, 22.75);

        surface.getButton (ButtonID.ROW_SELECT_1).setBounds (276.0, 43.0, 39.75, 16.0);
        surface.getButton (ButtonID.ROW_SELECT_2).setBounds (330.5, 43.0, 39.75, 16.0);
        surface.getButton (ButtonID.ROW_SELECT_3).setBounds (385.0, 43.0, 39.75, 16.0);
        surface.getButton (ButtonID.ROW_SELECT_4).setBounds (439.5, 43.0, 39.75, 16.0);
        surface.getButton (ButtonID.ROW_SELECT_5).setBounds (494.0, 43.0, 39.75, 16.0);
        surface.getButton (ButtonID.ROW_SELECT_6).setBounds (548.5, 43.0, 39.75, 16.0);
        surface.getButton (ButtonID.ROW_SELECT_7).setBounds (602.75, 43.0, 39.75, 16.0);
        surface.getButton (ButtonID.ROW_SELECT_8).setBounds (657.25, 43.0, 39.75, 16.0);
        surface.getButton (ButtonID.ROW1_1).setBounds (276.0, 67.5, 39.75, 16.0);
        surface.getButton (ButtonID.ROW1_2).setBounds (330.5, 67.5, 39.75, 16.0);
        surface.getButton (ButtonID.ROW1_3).setBounds (385.0, 67.5, 39.75, 16.0);
        surface.getButton (ButtonID.ROW1_4).setBounds (439.5, 67.5, 39.75, 16.0);
        surface.getButton (ButtonID.ROW1_5).setBounds (494.0, 67.5, 39.75, 16.0);
        surface.getButton (ButtonID.ROW1_6).setBounds (548.5, 67.5, 39.75, 16.0);
        surface.getButton (ButtonID.ROW1_7).setBounds (602.75, 67.5, 39.75, 16.0);
        surface.getButton (ButtonID.ROW1_8).setBounds (657.25, 67.5, 39.75, 16.0);
        surface.getButton (ButtonID.ROW2_1).setBounds (276.0, 92.25, 39.75, 16.0);
        surface.getButton (ButtonID.ROW2_2).setBounds (330.5, 92.25, 39.75, 16.0);
        surface.getButton (ButtonID.ROW2_3).setBounds (385.0, 92.25, 39.75, 16.0);
        surface.getButton (ButtonID.ROW2_4).setBounds (439.5, 92.25, 39.75, 16.0);
        surface.getButton (ButtonID.ROW2_5).setBounds (494.0, 92.25, 39.75, 16.0);
        surface.getButton (ButtonID.ROW2_6).setBounds (548.5, 92.25, 39.75, 16.0);
        surface.getButton (ButtonID.ROW2_7).setBounds (602.75, 92.25, 39.75, 16.0);
        surface.getButton (ButtonID.ROW2_8).setBounds (657.25, 92.25, 39.75, 16.0);
        surface.getButton (ButtonID.ROW3_1).setBounds (276.0, 116.75, 39.75, 16.0);
        surface.getButton (ButtonID.ROW3_2).setBounds (330.5, 116.75, 39.75, 16.0);
        surface.getButton (ButtonID.ROW3_3).setBounds (385.0, 116.75, 39.75, 16.0);
        surface.getButton (ButtonID.ROW3_4).setBounds (439.5, 116.75, 39.75, 16.0);
        surface.getButton (ButtonID.ROW3_5).setBounds (494.0, 116.75, 39.75, 16.0);
        surface.getButton (ButtonID.ROW3_6).setBounds (548.5, 116.75, 39.75, 16.0);
        surface.getButton (ButtonID.ROW3_7).setBounds (602.75, 116.75, 39.75, 16.0);
        surface.getButton (ButtonID.ROW3_8).setBounds (657.25, 116.75, 39.75, 16.0);

        surface.getButton (ButtonID.CLIP).setBounds (28.0, 258.25, 31.75, 22.75);
        surface.getButton (ButtonID.STOP_CLIP).setBounds (65.5, 258.25, 31.75, 22.75);
        surface.getButton (ButtonID.SCENE1).setBounds (103.25, 258.25, 31.75, 22.75);

        surface.getButton (ButtonID.F1).setBounds (637.5, 1.25, 31.75, 22.75);
        surface.getButton (ButtonID.F2).setBounds (675.25, 1.25, 31.75, 22.75);

        surface.getContinuous (ContinuousID.MOVE_TRACK_BANK).setBounds (208.5, 218.0, 10.0, 66.0);
        surface.getContinuous (ContinuousID.MOVE_TRACK).setBounds (227.0, 218.0, 10.0, 66.0);
        surface.getContinuous (ContinuousID.NAVIGATE_CLIPS).setBounds (245.5, 218.75, 10.0, 66.0);
        surface.getContinuous (ContinuousID.NAVIGATE_SCENES).setBounds (264.25, 218.0, 10.0, 66.0);
        surface.getContinuous (ContinuousID.MOVE_TRANSPORT).setBounds (282.75, 218.0, 10.0, 66.0);
        surface.getContinuous (ContinuousID.MOVE_LOOP).setBounds (301.25, 218.0, 10.0, 66.0);
        surface.getContinuous (ContinuousID.NAVIGATE_VOLUME).setBounds (319.75, 218.0, 10.0, 66.0);
        surface.getContinuous (ContinuousID.NAVIGATE_PAN).setBounds (338.25, 218.0, 10.0, 66.0);

        surface.getContinuous (ContinuousID.KNOB1).setBounds (284.0, 143.25, 28.0, 29.25);
        surface.getContinuous (ContinuousID.FADER1).setBounds (284.0, 178.5, 28.0, 29.25);
        surface.getContinuous (ContinuousID.KNOB2).setBounds (338.25, 143.25, 28.0, 29.25);
        surface.getContinuous (ContinuousID.FADER2).setBounds (338.25, 178.5, 28.0, 29.25);
        surface.getContinuous (ContinuousID.KNOB3).setBounds (392.5, 143.25, 28.0, 29.25);
        surface.getContinuous (ContinuousID.FADER3).setBounds (392.75, 178.5, 28.0, 29.25);
        surface.getContinuous (ContinuousID.KNOB4).setBounds (446.75, 143.25, 28.0, 29.25);
        surface.getContinuous (ContinuousID.FADER4).setBounds (447.0, 178.5, 28.0, 29.25);
        surface.getContinuous (ContinuousID.KNOB5).setBounds (501.25, 143.25, 28.0, 29.25);
        surface.getContinuous (ContinuousID.FADER5).setBounds (501.25, 178.5, 28.0, 29.25);
        surface.getContinuous (ContinuousID.KNOB6).setBounds (555.5, 143.25, 28.0, 29.25);
        surface.getContinuous (ContinuousID.FADER6).setBounds (555.75, 178.5, 28.0, 29.25);
        surface.getContinuous (ContinuousID.KNOB7).setBounds (609.75, 143.25, 28.0, 29.25);
        surface.getContinuous (ContinuousID.FADER7).setBounds (610.0, 178.5, 28.0, 29.25);
        surface.getContinuous (ContinuousID.KNOB8).setBounds (664.25, 143.25, 28.0, 29.25);
        surface.getContinuous (ContinuousID.FADER8).setBounds (664.25, 178.5, 28.0, 29.25);
    }


    /** {@inheritDoc} */
    @Override
    public void startup ()
    {
        final IControlSurface<KontrolProtocolConfiguration> surface = this.getSurface ();
        surface.getViewManager ().setActiveView (Views.CONTROL);
        surface.getModeManager ().setActiveMode (Modes.VOLUME);

        this.getSurface ().initHandshake ();
    }


    /** {@inheritDoc} */
    @Override
    protected void updateIndication (final Modes mode)
    {
        if (this.currentMode != null && this.currentMode == mode)
            return;

        if (mode != null)
            this.currentMode = mode;

        final ITrackBank tb = this.model.getTrackBank ();
        final boolean isVolume = Modes.VOLUME == this.currentMode;
        final boolean isSend = Modes.SEND == this.currentMode;
        final boolean isDevice = Modes.isDeviceMode (this.currentMode) || Modes.isLayerMode (this.currentMode);

        final ICursorDevice cursorDevice = this.model.getCursorDevice ();
        final ITrack selectedTrack = tb.getSelectedItem ();
        final IParameterBank parameterBank = cursorDevice.getParameterBank ();
        for (int i = 0; i < tb.getPageSize (); i++)
        {
            final boolean hasTrackSel = selectedTrack != null && selectedTrack.getIndex () == i;

            final ITrack track = tb.getItem (i);
            track.setVolumeIndication (isVolume);
            track.setPanIndication (isVolume);

            final ISendBank sendBank = track.getSendBank ();
            for (int j = 0; j < sendBank.getPageSize (); j++)
                sendBank.getItem (j).setIndication (isSend && hasTrackSel);

            parameterBank.getItem (i).setIndication (isDevice);
        }
    }


    /**
     * Get the name of an Komplete Kontrol instance on the current track, or an empty string
     * otherwise. A track contains a Komplete Kontrol instance if: There is an instance of a plugin
     * whose name starts with Komplete Kontrol and the first parameter label exposed by the plugin
     * is NIKBxx, where xx is a number between 00 and 99 If the conditions are satisfied. First
     * checks the selected device, if that is no KK device, the first instrument device is checked.
     *
     * @return The instance name, which is the actual label of the first parameter (e.g. NIKB01). An
     *         empty string if none is present
     */
    private String getKompleteInstance ()
    {
        ICursorDevice device = this.model.getCursorDevice ();
        if (device.doesExist () && device.getName ().startsWith ("Komplete Kontrol"))
            return device.getID ();

        device = this.model.getInstrumentDevice ();
        if (device.doesExist () && device.getName ().startsWith ("Komplete Kontrol"))
            return device.getID ();

        return "";
    }


    /**
     * Navigate to the previous or next scene (if any).
     *
     * @param value 1 to move left, 127 to move right
     */
    private void navigateScenes (final int value)
    {
        final ISceneBank sceneBank = this.model.getSceneBank ();
        if (sceneBank == null)
            return;
        if (value == 1)
            sceneBank.selectNextItem ();
        else if (value == 127)
            sceneBank.selectPreviousItem ();
    }


    /**
     * Navigate to the previous or next clip of the selected track (if any).
     *
     * @param value 1 to move left, 127 to move right
     */
    private void navigateClips (final int value)
    {
        final ITrack selectedTrack = this.model.getSelectedTrack ();
        if (selectedTrack == null)
            return;
        if (value == 1)
            selectedTrack.getSlotBank ().selectNextItem ();
        else if (value == 127)
            selectedTrack.getSlotBank ().selectPreviousItem ();
    }


    /**
     * Navigate to the previous or next track (if any).
     *
     * @param value 1 to move left else move right
     */
    private void navigateTracks (final int value)
    {
        if (value == 1)
            this.model.getTrackBank ().selectNextItem ();
        else
            this.model.getTrackBank ().selectPreviousItem ();
    }


    private void changeTransportPosition (final int value, final int mode)
    {
        final boolean increase = mode == 0 ? value == 1 : value <= 63;
        this.model.getTransport ().changePosition (increase);
    }


    private void changeLoopPosition (final int value)
    {
        // Changing of loop position is not possible. Therefore, change position fine grained
        this.model.getTransport ().changePosition (value <= 63, true);
    }
}
