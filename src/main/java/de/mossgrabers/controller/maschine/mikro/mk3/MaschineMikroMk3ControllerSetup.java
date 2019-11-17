// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.maschine.mikro.mk3;

import de.mossgrabers.controller.maschine.mikro.mk3.command.trigger.AddDeviceCommand;
import de.mossgrabers.controller.maschine.mikro.mk3.command.trigger.GridButtonCommand;
import de.mossgrabers.controller.maschine.mikro.mk3.command.trigger.ProjectButtonCommand;
import de.mossgrabers.controller.maschine.mikro.mk3.command.trigger.RibbonCommand;
import de.mossgrabers.controller.maschine.mikro.mk3.command.trigger.ToggleDuplicateButtonCommand;
import de.mossgrabers.controller.maschine.mikro.mk3.command.trigger.ToggleFixedVelCommand;
import de.mossgrabers.controller.maschine.mikro.mk3.command.trigger.VolumePanSendCommand;
import de.mossgrabers.controller.maschine.mikro.mk3.controller.MaschineMikroMk3ControlSurface;
import de.mossgrabers.controller.maschine.mikro.mk3.mode.BrowseMode;
import de.mossgrabers.controller.maschine.mikro.mk3.mode.PositionMode;
import de.mossgrabers.controller.maschine.mikro.mk3.mode.TempoMode;
import de.mossgrabers.controller.maschine.mikro.mk3.view.ClipView;
import de.mossgrabers.controller.maschine.mikro.mk3.view.DrumView;
import de.mossgrabers.controller.maschine.mikro.mk3.view.MuteView;
import de.mossgrabers.controller.maschine.mikro.mk3.view.ParameterView;
import de.mossgrabers.controller.maschine.mikro.mk3.view.PlayView;
import de.mossgrabers.controller.maschine.mikro.mk3.view.SceneView;
import de.mossgrabers.controller.maschine.mikro.mk3.view.SelectView;
import de.mossgrabers.controller.maschine.mikro.mk3.view.SoloView;
import de.mossgrabers.framework.command.continuous.KnobRowModeCommand;
import de.mossgrabers.framework.command.trigger.BrowserCommand;
import de.mossgrabers.framework.command.trigger.application.PaneCommand;
import de.mossgrabers.framework.command.trigger.application.UndoCommand;
import de.mossgrabers.framework.command.trigger.clip.NewCommand;
import de.mossgrabers.framework.command.trigger.clip.NoteRepeatCommand;
import de.mossgrabers.framework.command.trigger.clip.QuantizeCommand;
import de.mossgrabers.framework.command.trigger.mode.KnobRowTouchModeCommand;
import de.mossgrabers.framework.command.trigger.mode.ModeSelectCommand;
import de.mossgrabers.framework.command.trigger.transport.MetronomeCommand;
import de.mossgrabers.framework.command.trigger.transport.PlayCommand;
import de.mossgrabers.framework.command.trigger.transport.RecordCommand;
import de.mossgrabers.framework.command.trigger.transport.StopCommand;
import de.mossgrabers.framework.command.trigger.transport.ToggleLoopCommand;
import de.mossgrabers.framework.command.trigger.transport.WriteArrangerAutomationCommand;
import de.mossgrabers.framework.command.trigger.transport.WriteClipLauncherAutomationCommand;
import de.mossgrabers.framework.command.trigger.view.ViewMultiSelectCommand;
import de.mossgrabers.framework.configuration.AbstractConfiguration;
import de.mossgrabers.framework.configuration.ISettingsUI;
import de.mossgrabers.framework.controller.AbstractControllerSetup;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.ContinuousID;
import de.mossgrabers.framework.controller.DefaultValueChanger;
import de.mossgrabers.framework.controller.ISetupFactory;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.grid.PadGrid;
import de.mossgrabers.framework.daw.ICursorDevice;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.ISendBank;
import de.mossgrabers.framework.daw.ITrackBank;
import de.mossgrabers.framework.daw.ModelSetup;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.midi.IMidiAccess;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.daw.midi.IMidiOutput;
import de.mossgrabers.framework.daw.midi.INoteRepeat;
import de.mossgrabers.framework.mode.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.mode.device.SelectedDeviceMode;
import de.mossgrabers.framework.mode.track.SelectedPanMode;
import de.mossgrabers.framework.mode.track.SelectedSendMode;
import de.mossgrabers.framework.mode.track.SelectedVolumeMode;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.view.ViewManager;
import de.mossgrabers.framework.view.Views;


/**
 * Support for the NI Maschine Mikro Mk3 controller.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class MaschineMikroMk3ControllerSetup extends AbstractControllerSetup<MaschineMikroMk3ControlSurface, MaschineMikroMk3Configuration>
{
    // @formatter:off
    /** The drum grid matrix. */
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
    public MaschineMikroMk3ControllerSetup (final IHost host, final ISetupFactory factory, final ISettingsUI globalSettings, final ISettingsUI documentSettings)
    {
        super (factory, host, globalSettings, documentSettings);
        this.colorManager = new ColorManager ();
        this.valueChanger = new DefaultValueChanger (128, 1, 0.5);
        this.configuration = new MaschineMikroMk3Configuration (host, this.valueChanger);
    }


    /** {@inheritDoc} */
    @Override
    protected void createScales ()
    {
        this.scales = new Scales (this.valueChanger, 36, 52, 4, 4);
        this.scales.setDrumMatrix (DRUM_MATRIX);
    }


    /** {@inheritDoc} */
    @Override
    protected void createModel ()
    {
        final ModelSetup ms = new ModelSetup ();
        ms.setNumTracks (16);
        ms.setNumDevicesInBank (16);
        ms.setNumScenes (16);
        ms.setNumSends (8);
        ms.setNumParams (16);
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
        final IMidiInput input = midiAccess.createInput ("Maschine Mikro Mk3", "80????", "90????");
        this.colorManager.registerColorIndex (PadGrid.GRID_OFF, 0);
        final MaschineMikroMk3ControlSurface surface = new MaschineMikroMk3ControlSurface (this.host, this.colorManager, this.configuration, output, input);
        this.surfaces.add (surface);
    }


    /** {@inheritDoc} */
    @Override
    protected void createObservers ()
    {
        final MaschineMikroMk3ControlSurface surface = this.getSurface ();

        surface.getViewManager ().addViewChangeListener ( (previousViewId, activeViewId) -> this.updateMode (null));
        surface.getModeManager ().addModeListener ( (previousModeId, activeModeId) -> this.updateMode (activeModeId));

        final INoteRepeat noteRepeat = this.getSurface ().getInput ().getDefaultNoteInput ().getNoteRepeat ();

        this.configuration.addSettingObserver (AbstractConfiguration.NOTEREPEAT_ACTIVE, () -> noteRepeat.setActive (this.configuration.isNoteRepeatActive ()));
        this.configuration.addSettingObserver (AbstractConfiguration.NOTEREPEAT_PERIOD, () -> noteRepeat.setPeriod (this.configuration.getNoteRepeatPeriod ().getValue ()));
        this.configuration.addSettingObserver (AbstractConfiguration.NOTEREPEAT_LENGTH, () -> noteRepeat.setNoteLength (this.configuration.getNoteRepeatLength ().getValue ()));

        this.createScaleObservers (this.configuration);
    }


    /** {@inheritDoc} */
    @Override
    protected void createModes ()
    {
        final MaschineMikroMk3ControlSurface surface = this.getSurface ();
        final ModeManager modeManager = surface.getModeManager ();

        modeManager.registerMode (Modes.BROWSER, new BrowseMode (surface, this.model));

        modeManager.registerMode (Modes.VOLUME, new SelectedVolumeMode<> (surface, this.model));
        modeManager.registerMode (Modes.PAN, new SelectedPanMode<> (surface, this.model));
        for (int i = 0; i < 8; i++)
            modeManager.registerMode (Modes.get (Modes.SEND1, i), new SelectedSendMode<> (i, surface, this.model));

        modeManager.registerMode (Modes.POSITION, new PositionMode (surface, this.model));
        modeManager.registerMode (Modes.TEMPO, new TempoMode (surface, this.model));

        modeManager.registerMode (Modes.DEVICE_PARAMS, new SelectedDeviceMode<> (surface, this.model));

        modeManager.setDefaultMode (Modes.VOLUME);
    }


    /** {@inheritDoc} */
    @Override
    protected void createViews ()
    {
        final MaschineMikroMk3ControlSurface surface = this.getSurface ();
        final ViewManager viewManager = surface.getViewManager ();

        viewManager.registerView (Views.SCENE_PLAY, new SceneView (surface, this.model));
        viewManager.registerView (Views.CLIP, new ClipView (surface, this.model));

        viewManager.registerView (Views.PLAY, new PlayView (surface, this.model));
        viewManager.registerView (Views.DRUM, new DrumView (surface, this.model));

        viewManager.registerView (Views.DEVICE, new ParameterView (surface, this.model));

        viewManager.registerView (Views.TRACK_SELECT, new SelectView (surface, this.model));
        viewManager.registerView (Views.TRACK_SOLO, new SoloView (surface, this.model));
        viewManager.registerView (Views.TRACK_MUTE, new MuteView (surface, this.model));
    }


    /** {@inheritDoc} */
    @Override
    protected void registerTriggerCommands ()
    {
        final MaschineMikroMk3ControlSurface surface = this.getSurface ();

        // Transport
        this.addButton (ButtonID.PLAY, "Play", new PlayCommand<> (this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_PLAY);
        this.addButton (ButtonID.RECORD, "Record", new RecordCommand<> (this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_REC);
        this.addButton (ButtonID.STOP, "Stop", new StopCommand<> (this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_STOP);
        this.addButton (ButtonID.LOOP, "Loop", new ToggleLoopCommand<> (this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_RESTART);
        this.addButton (ButtonID.UNDO, "Undo", new UndoCommand<> (this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_ERASE);
        this.addButton (ButtonID.METRONOME, "Metronome", new MetronomeCommand<> (this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_TAP_METRO);
        this.addButton (ButtonID.QUANTIZE, "Quantize", new QuantizeCommand<> (this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_FOLLOW);

        // Automation
        this.addButton (ButtonID.NEW, "New", new NewCommand<> (this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_GROUP);
        this.addButton (ButtonID.AUTOMATION, "Automation", new WriteClipLauncherAutomationCommand<> (this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_AUTO);
        this.addButton (ButtonID.AUTOMATION_WRITE, "Write", new WriteArrangerAutomationCommand<> (this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_LOCK);
        this.addButton (ButtonID.REPEAT, "Repeat", new NoteRepeatCommand<> (this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_NOTE_REPEAT);

        // Ribbon
        this.addButton (ButtonID.F1, "Pitch", new RibbonCommand (this.model, surface, MaschineMikroMk3Configuration.RIBBON_MODE_PITCH_DOWN, MaschineMikroMk3Configuration.RIBBON_MODE_PITCH_UP, MaschineMikroMk3Configuration.RIBBON_MODE_PITCH_DOWN_UP), MaschineMikroMk3ControlSurface.MIKRO_3_PITCH);
        this.addButton (ButtonID.F2, "Mod", new RibbonCommand (this.model, surface, MaschineMikroMk3Configuration.RIBBON_MODE_CC_1), MaschineMikroMk3ControlSurface.MIKRO_3_MOD);
        this.addButton (ButtonID.F3, "Perform", new RibbonCommand (this.model, surface, MaschineMikroMk3Configuration.RIBBON_MODE_CC_11), MaschineMikroMk3ControlSurface.MIKRO_3_PERFORM);
        this.addButton (ButtonID.F4, "Notes", new RibbonCommand (this.model, surface, MaschineMikroMk3Configuration.RIBBON_MODE_MASTER_VOLUME), MaschineMikroMk3ControlSurface.MIKRO_3_NOTES);

        // Encoder Modes
        this.addButton (ButtonID.FADER_TOUCH_1, "Encoder Push", new KnobRowTouchModeCommand<> (0, this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_ENCODER_PUSH);
        this.addButton (ButtonID.VOLUME, "Volume", new VolumePanSendCommand (this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_VOLUME);
        this.addButton (ButtonID.TAP_TEMPO, "Swing", new ModeSelectCommand<> (this.model, surface, Modes.POSITION), MaschineMikroMk3ControlSurface.MIKRO_3_SWING);
        this.addButton (ButtonID.USER, "Tempo", new ModeSelectCommand<> (this.model, surface, Modes.TEMPO), MaschineMikroMk3ControlSurface.MIKRO_3_TEMPO);
        this.addButton (ButtonID.DEVICE, "Plugin", new ModeSelectCommand<> (this.model, surface, Modes.DEVICE_PARAMS), MaschineMikroMk3ControlSurface.MIKRO_3_PLUGIN);
        this.addButton (ButtonID.DEVICE_ON_OFF, "Sampling", new PaneCommand<> (PaneCommand.Panels.DEVICE, this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_SAMPLING);

        // Browser
        this.addButton (ButtonID.ADD_TRACK, "Project", new ProjectButtonCommand (this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_PROJECT);
        this.addButton (ButtonID.ADD_EFFECT, "Favorites", new AddDeviceCommand (this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_FAVORITES);
        this.addButton (ButtonID.BROWSE, "Browser", new BrowserCommand<> (Modes.BROWSER, this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_BROWSER);

        // Pad modes
        this.addButton (ButtonID.ACCENT, "Accent", new ToggleFixedVelCommand (this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_FIXED_VEL);

        this.addButton (ButtonID.SCENE1, "Scene", new ViewMultiSelectCommand<> (this.model, surface, true, Views.SCENE_PLAY), MaschineMikroMk3ControlSurface.MIKRO_3_SCENE);
        this.addButton (ButtonID.CLIP, "PAttern", new ViewMultiSelectCommand<> (this.model, surface, true, Views.CLIP), MaschineMikroMk3ControlSurface.MIKRO_3_PATTERN);
        this.addButton (ButtonID.NOTE, "Events", new ViewMultiSelectCommand<> (this.model, surface, true, Views.PLAY, Views.DRUM), MaschineMikroMk3ControlSurface.MIKRO_3_EVENTS);
        this.addButton (ButtonID.TOGGLE_DEVICE, "VAriation", new ViewMultiSelectCommand<> (this.model, surface, true, Views.DEVICE), MaschineMikroMk3ControlSurface.MIKRO_3_VARIATION);
        this.addButton (ButtonID.DUPLICATE, "Duplicate", new ToggleDuplicateButtonCommand (this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_DUPLICATE);

        this.addButton (ButtonID.TRACK, "Select", new ViewMultiSelectCommand<> (this.model, surface, true, Views.TRACK_SELECT), MaschineMikroMk3ControlSurface.MIKRO_3_SELECT);
        this.addButton (ButtonID.SOLO, "Solo", new ViewMultiSelectCommand<> (this.model, surface, true, Views.TRACK_SOLO), MaschineMikroMk3ControlSurface.MIKRO_3_SOLO);
        this.addButton (ButtonID.MUTE, "Mute", new ViewMultiSelectCommand<> (this.model, surface, true, Views.TRACK_MUTE), MaschineMikroMk3ControlSurface.MIKRO_3_MUTE);

        this.addButton (ButtonID.ROW1_1, "Mode", new GridButtonCommand (0, this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_PAD_MODE);
        this.addButton (ButtonID.ROW1_2, "Keyboard", new GridButtonCommand (1, this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_KEYBOARD);
        this.addButton (ButtonID.ROW1_3, "Chords", new GridButtonCommand (2, this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_CHORDS);
        this.addButton (ButtonID.ROW1_4, "Step", new GridButtonCommand (3, this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_STEP);
    }


    /** {@inheritDoc} */
    @Override
    protected void registerContinuousCommands ()
    {
        final MaschineMikroMk3ControlSurface surface = this.getSurface ();
        this.addRelativeKnob (ContinuousID.KNOB1, "", new KnobRowModeCommand<> (0, this.model, surface), MaschineMikroMk3ControlSurface.MIKRO_3_ENCODER);
        // TODO this.addContinuousCommand (ContinuousCommandID.CROSSFADER,
        // MaschineMikroMk3ControlSurface.MIKRO_3_TOUCHSTRIP, new PitchbendCommand (this.model,
        // surface));
    }


    /** {@inheritDoc} */
    @Override
    public void startup ()
    {
        final MaschineMikroMk3ControlSurface surface = this.getSurface ();
        surface.getModeManager ().setActiveMode (Modes.VOLUME);
        surface.getViewManager ().setActiveView (Views.PLAY);
    }

    // /** {@inheritDoc} */
    // @Override
    // protected void updateButtons ()
    // {
    // final MaschineMikroMk3ControlSurface surface = this.getSurface ();
    //
    // TODO
    // final ITransport t = this.model.getTransport ();
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_PLAY, t.isPlaying () ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_REC, t.isRecording () ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_STOP,
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_RESTART, t.isLoop () ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_ERASE,
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_TAP_METRO, t.isMetronomeOn
    // () ? MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_FOLLOW,
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    //
    // final int ribbonMode = surface.getConfiguration ().getRibbonMode ();
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_PITCH, ribbonMode <=
    // MaschineMikroMk3Configuration.RIBBON_MODE_PITCH_DOWN_UP ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_MOD, ribbonMode ==
    // MaschineMikroMk3Configuration.RIBBON_MODE_CC_1 ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_PERFORM, ribbonMode ==
    // MaschineMikroMk3Configuration.RIBBON_MODE_CC_11 ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_NOTES, ribbonMode ==
    // MaschineMikroMk3Configuration.RIBBON_MODE_MASTER_VOLUME ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    //
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_GROUP,
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_AUTO,
    // t.isWritingClipLauncherAutomation () ? MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_LOCK,
    // t.isWritingArrangerAutomation () ? MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_NOTE_REPEAT,
    // this.getSurface ().getInput ().getDefaultNoteInput ().getNoteRepeat ().isActive () ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    //
    // final Modes modeID = this.getSurface ().getModeManager ().getActiveOrTempModeId ();
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_VOLUME, modeID != null &&
    // modeID.ordinal () <= Modes.SEND8.ordinal () ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_SWING, modeID != null &&
    // modeID.ordinal () == Modes.POSITION.ordinal () ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_TEMPO, modeID != null &&
    // modeID.ordinal () == Modes.TEMPO.ordinal () ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_PLUGIN, modeID != null &&
    // modeID.ordinal () == Modes.DEVICE_PARAMS.ordinal () ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_SAMPLING,
    // this.model.getCursorDevice ().isWindowOpen () ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    //
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_FIXED_VEL,
    // this.configuration.isAccentActive () ? MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    //
    // final ViewManager viewManager = this.getSurface ().getViewManager ();
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_SCENE,
    // viewManager.isActiveView (Views.SCENE_PLAY) ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_PATTERN,
    // viewManager.isActiveView (Views.CLIP) ? MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_EVENTS,
    // viewManager.isActiveView (Views.PLAY) || viewManager.isActiveView (Views.DRUM) ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_VARIATION,
    // viewManager.isActiveView (Views.DEVICE) ? MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON
    // : MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    //
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_DUPLICATE,
    // this.configuration.isDuplicateEnabled () ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_SELECT,
    // viewManager.isActiveView (Views.TRACK_SELECT) ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_SOLO,
    // viewManager.isActiveView (Views.TRACK_SOLO) ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_MUTE,
    // viewManager.isActiveView (Views.TRACK_MUTE) ?
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_ON :
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    //
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_PAD_MODE,
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_KEYBOARD,
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_CHORDS,
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // surface.updateTrigger (MaschineMikroMk3ControlSurface.MIKRO_3_STEP,
    // MaschineMikroMk3ControlSurface.MIKRO_3_STATE_OFF);
    // }


    private void updateMode (final Modes mode)
    {
        final Modes m = mode == null ? this.getSurface ().getModeManager ().getActiveOrTempModeId () : mode;
        if (this.currentMode != null && this.currentMode.equals (m))
            return;
        this.currentMode = m;
        this.updateIndication (m);
    }


    /** {@inheritDoc} */
    @Override
    protected void updateIndication (final Modes mode)
    {
        final ITrackBank tb = this.model.getTrackBank ();
        final ITrackBank tbe = this.model.getEffectTrackBank ();
        final MaschineMikroMk3ControlSurface surface = this.getSurface ();
        final ViewManager viewManager = surface.getViewManager ();

        final boolean isSession = viewManager.isActiveView (Views.SCENE_PLAY) || viewManager.isActiveView (Views.CLIP);
        final boolean isEffect = this.model.isEffectTrackBankActive ();

        final boolean isVolume = Modes.VOLUME.equals (mode);
        final boolean isPan = Modes.PAN.equals (mode);

        tb.setIndication (!isEffect && isSession);
        if (tbe != null)
            tbe.setIndication (isEffect && isSession);

        final ITrack selectedTrack = tb.getSelectedItem ();
        final int selIndex = selectedTrack == null ? -1 : selectedTrack.getIndex ();

        final ICursorDevice cursorDevice = this.model.getCursorDevice ();
        for (int i = 0; i < 16; i++)
        {
            final ITrack track = tb.getItem (i);
            track.setVolumeIndication (selIndex == i && !isEffect && isVolume);
            track.setPanIndication (selIndex == i && !isEffect && isPan);
            final ISendBank sendBank = track.getSendBank ();
            for (int j = 0; j < 8; j++)
                sendBank.getItem (j).setIndication (selIndex == i && !isEffect && (Modes.SEND1.equals (mode) && j == 0 || Modes.SEND2.equals (mode) && j == 1 || Modes.SEND3.equals (mode) && j == 2 || Modes.SEND4.equals (mode) && j == 3 || Modes.SEND5.equals (mode) && j == 4 || Modes.SEND6.equals (mode) && j == 5 || Modes.SEND7.equals (mode) && j == 6 || Modes.SEND8.equals (mode) && j == 7));

            if (tbe != null)
            {
                final ITrack fxTrack = tbe.getItem (i);
                fxTrack.setVolumeIndication (selIndex == i && isEffect && isVolume);
                fxTrack.setPanIndication (selIndex == i && isEffect && isPan);
            }

            cursorDevice.getParameterBank ().getItem (i).setIndication (true);
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

        final MaschineMikroMk3ControlSurface surface = this.getSurface ();
        final ViewManager viewManager = surface.getViewManager ();
        if (viewManager.isActiveView (Views.PLAY))
            viewManager.getActiveView ().updateNoteMapping ();

        // Reset drum octave because the drum pad bank is also reset
        this.scales.resetDrumOctave ();
        if (viewManager.isActiveView (Views.DRUM))
            viewManager.getView (Views.DRUM).updateNoteMapping ();

        this.updateIndication (this.currentMode);
    }
}
