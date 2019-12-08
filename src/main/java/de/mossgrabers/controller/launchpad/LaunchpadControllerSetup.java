// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.launchpad;

import de.mossgrabers.controller.launchpad.command.continuous.FaderCommand;
import de.mossgrabers.controller.launchpad.command.trigger.ClickCommand;
import de.mossgrabers.controller.launchpad.command.trigger.LaunchpadCursorCommand;
import de.mossgrabers.controller.launchpad.command.trigger.LaunchpadDuplicateCommand;
import de.mossgrabers.controller.launchpad.command.trigger.MuteCommand;
import de.mossgrabers.controller.launchpad.command.trigger.PanCommand;
import de.mossgrabers.controller.launchpad.command.trigger.PlayAndNewCommand;
import de.mossgrabers.controller.launchpad.command.trigger.RecordArmCommand;
import de.mossgrabers.controller.launchpad.command.trigger.SelectDeviceViewCommand;
import de.mossgrabers.controller.launchpad.command.trigger.SelectNoteViewCommand;
import de.mossgrabers.controller.launchpad.command.trigger.SelectSessionViewCommand;
import de.mossgrabers.controller.launchpad.command.trigger.SendsCommand;
import de.mossgrabers.controller.launchpad.command.trigger.ShiftCommand;
import de.mossgrabers.controller.launchpad.command.trigger.SoloCommand;
import de.mossgrabers.controller.launchpad.command.trigger.StopClipCommand;
import de.mossgrabers.controller.launchpad.command.trigger.TrackSelectCommand;
import de.mossgrabers.controller.launchpad.command.trigger.VolumeCommand;
import de.mossgrabers.controller.launchpad.controller.LaunchpadColorManager;
import de.mossgrabers.controller.launchpad.controller.LaunchpadControlSurface;
import de.mossgrabers.controller.launchpad.controller.LaunchpadScales;
import de.mossgrabers.controller.launchpad.definition.ILaunchpadControllerDefinition;
import de.mossgrabers.controller.launchpad.definition.LaunchpadProControllerDefinition;
import de.mossgrabers.controller.launchpad.mode.RecArmMode;
import de.mossgrabers.controller.launchpad.mode.SendMode;
import de.mossgrabers.controller.launchpad.mode.StopClipMode;
import de.mossgrabers.controller.launchpad.mode.TrackMode;
import de.mossgrabers.controller.launchpad.view.BrowserView;
import de.mossgrabers.controller.launchpad.view.DeviceView;
import de.mossgrabers.controller.launchpad.view.DrumView;
import de.mossgrabers.controller.launchpad.view.DrumView4;
import de.mossgrabers.controller.launchpad.view.DrumView64;
import de.mossgrabers.controller.launchpad.view.DrumView8;
import de.mossgrabers.controller.launchpad.view.PanView;
import de.mossgrabers.controller.launchpad.view.PianoView;
import de.mossgrabers.controller.launchpad.view.PlayView;
import de.mossgrabers.controller.launchpad.view.RaindropsView;
import de.mossgrabers.controller.launchpad.view.SendsView;
import de.mossgrabers.controller.launchpad.view.SequencerView;
import de.mossgrabers.controller.launchpad.view.SessionView;
import de.mossgrabers.controller.launchpad.view.ShiftView;
import de.mossgrabers.controller.launchpad.view.UserView;
import de.mossgrabers.controller.launchpad.view.VolumeView;
import de.mossgrabers.framework.command.SceneCommand;
import de.mossgrabers.framework.command.aftertouch.AftertouchAbstractViewCommand;
import de.mossgrabers.framework.command.trigger.application.DeleteCommand;
import de.mossgrabers.framework.command.trigger.application.UndoCommand;
import de.mossgrabers.framework.command.trigger.clip.QuantizeCommand;
import de.mossgrabers.framework.command.trigger.mode.ModeCursorCommand.Direction;
import de.mossgrabers.framework.command.trigger.transport.RecordCommand;
import de.mossgrabers.framework.command.trigger.view.ViewMultiSelectCommand;
import de.mossgrabers.framework.configuration.ISettingsUI;
import de.mossgrabers.framework.controller.AbstractControllerSetup;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.ContinuousID;
import de.mossgrabers.framework.controller.ISetupFactory;
import de.mossgrabers.framework.controller.hardware.BindType;
import de.mossgrabers.framework.controller.valuechanger.DefaultValueChanger;
import de.mossgrabers.framework.daw.ICursorDevice;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.IParameterBank;
import de.mossgrabers.framework.daw.ISendBank;
import de.mossgrabers.framework.daw.ITrackBank;
import de.mossgrabers.framework.daw.ModelSetup;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.midi.IMidiAccess;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.daw.midi.IMidiOutput;
import de.mossgrabers.framework.mode.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.mode.track.MuteMode;
import de.mossgrabers.framework.mode.track.PanMode;
import de.mossgrabers.framework.mode.track.SoloMode;
import de.mossgrabers.framework.mode.track.VolumeMode;
import de.mossgrabers.framework.view.AbstractView;
import de.mossgrabers.framework.view.View;
import de.mossgrabers.framework.view.ViewManager;
import de.mossgrabers.framework.view.Views;

import java.util.Map;


/**
 * Support for several Novation Launchpad controllers.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class LaunchpadControllerSetup extends AbstractControllerSetup<LaunchpadControlSurface, LaunchpadConfiguration>
{
    private final ILaunchpadControllerDefinition definition;
    private int                                  frontColor = -1;


    /**
     * Constructor.
     *
     * @param host The DAW host
     * @param factory The factory
     * @param globalSettings The global settings
     * @param documentSettings The document (project) specific settings
     * @param definition The Launchpad definition
     */
    public LaunchpadControllerSetup (final IHost host, final ISetupFactory factory, final ISettingsUI globalSettings, final ISettingsUI documentSettings, final ILaunchpadControllerDefinition definition)
    {
        super (factory, host, globalSettings, documentSettings);

        this.definition = definition;
        this.colorManager = new LaunchpadColorManager ();
        this.valueChanger = new DefaultValueChanger (128, 1, 0.5);
        this.configuration = new LaunchpadConfiguration (host, this.valueChanger, definition);
    }


    /** {@inheritDoc} */
    @Override
    protected void createScales ()
    {
        this.scales = new LaunchpadScales (this.valueChanger, 36, 100, 8, 8);
    }


    /** {@inheritDoc} */
    @Override
    protected void createModel ()
    {
        final ModelSetup ms = new ModelSetup ();
        ms.setHasFullFlatTrackList (true);
        this.model = this.factory.createModel (this.colorManager, this.valueChanger, this.scales, ms);
        final ITrackBank trackBank = this.model.getTrackBank ();
        trackBank.addSelectionObserver ( (index, isSelected) -> this.handleTrackChange (isSelected));
    }


    /** {@inheritDoc} */
    @Override
    protected void createSurface ()
    {
        final IMidiAccess midiAccess = this.factory.createMidiAccess ();
        final IMidiOutput output = midiAccess.createOutput ();
        final IMidiInput input = midiAccess.createInput ("Pads", "80????" /* Note off */,
                "90????" /* Note on */);
        final LaunchpadControlSurface surface = new LaunchpadControlSurface (this.host, this.colorManager, this.configuration, output, input, this.definition);
        this.surfaces.add (surface);
        surface.setLaunchpadToStandalone ();
    }


    /** {@inheritDoc} */
    @Override
    protected void createObservers ()
    {
        this.getSurface ().getViewManager ().addViewChangeListener ( (previousViewId, activeViewId) -> this.updateIndication (null));
        this.createScaleObservers (this.configuration);
    }


    /** {@inheritDoc} */
    @Override
    protected void createModes ()
    {
        final LaunchpadControlSurface surface = this.getSurface ();
        final ModeManager modeManager = surface.getModeManager ();
        modeManager.registerMode (Modes.REC_ARM, new RecArmMode (surface, this.model));
        modeManager.registerMode (Modes.TRACK_SELECT, new TrackMode (surface, this.model));
        modeManager.registerMode (Modes.MUTE, new MuteMode<> (surface, this.model));
        modeManager.registerMode (Modes.SOLO, new SoloMode<> (surface, this.model));
        modeManager.registerMode (Modes.VOLUME, new VolumeMode<> (surface, this.model, true));
        modeManager.registerMode (Modes.PAN, new PanMode<> (surface, this.model, true));
        modeManager.registerMode (Modes.SEND, new SendMode (surface, this.model));
        modeManager.registerMode (Modes.STOP_CLIP, new StopClipMode (surface, this.model));
    }


    /** {@inheritDoc} */
    @Override
    protected void createViews ()
    {
        final LaunchpadControlSurface surface = this.getSurface ();
        final ViewManager viewManager = surface.getViewManager ();
        viewManager.registerView (Views.BROWSER, new BrowserView (surface, this.model));
        viewManager.registerView (Views.DEVICE, new DeviceView (surface, this.model));
        viewManager.registerView (Views.DRUM, new DrumView (surface, this.model));
        viewManager.registerView (Views.DRUM4, new DrumView4 (surface, this.model));
        viewManager.registerView (Views.DRUM8, new DrumView8 (surface, this.model));
        viewManager.registerView (Views.TRACK_PAN, new PanView (surface, this.model));
        viewManager.registerView (Views.DRUM64, new DrumView64 (surface, this.model));
        viewManager.registerView (Views.PLAY, new PlayView (surface, this.model));
        viewManager.registerView (Views.PIANO, new PianoView (surface, this.model));
        viewManager.registerView (Views.RAINDROPS, new RaindropsView (surface, this.model));
        viewManager.registerView (Views.TRACK_SENDS, new SendsView (surface, this.model));
        viewManager.registerView (Views.SEQUENCER, new SequencerView (surface, this.model));
        viewManager.registerView (Views.SESSION, new SessionView (surface, this.model));
        viewManager.registerView (Views.TRACK_VOLUME, new VolumeView (surface, this.model));
        viewManager.registerView (Views.SHIFT, new ShiftView (surface, this.model));
        if (this.definition.isPro () && this.host.hasUserParameters ())
            viewManager.registerView (Views.CONTROL, new UserView (surface, this.model));
    }


    /** {@inheritDoc} */
    @Override
    protected void registerTriggerCommands ()
    {
        final LaunchpadControlSurface surface = this.getSurface ();

        final Map<ButtonID, Integer> buttonIDs = this.definition.getButtonIDs ();

        this.addButton (ButtonID.SHIFT, "Shift", new ShiftCommand (this.model, surface), buttonIDs.get (ButtonID.SHIFT).intValue ());

        if (this.definition.isPro () && this.host.hasUserParameters ())
            this.addButton (ButtonID.USER, "User", new ViewMultiSelectCommand<> (this.model, surface, true, Views.CONTROL), LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_USER);

        this.addButton (ButtonID.UP, "Up", new LaunchpadCursorCommand (Direction.UP, this.model, surface), buttonIDs.get (ButtonID.UP).intValue ());
        this.addButton (ButtonID.DOWN, "Down", new LaunchpadCursorCommand (Direction.DOWN, this.model, surface), buttonIDs.get (ButtonID.DOWN).intValue ());
        this.addButton (ButtonID.LEFT, "Left", new LaunchpadCursorCommand (Direction.LEFT, this.model, surface), buttonIDs.get (ButtonID.LEFT).intValue ());
        this.addButton (ButtonID.RIGHT, "Right", new LaunchpadCursorCommand (Direction.RIGHT, this.model, surface), buttonIDs.get (ButtonID.RIGHT).intValue ());

        this.addButton (ButtonID.SESSION, "Session", new SelectSessionViewCommand (this.model, surface), buttonIDs.get (ButtonID.SESSION).intValue ());
        this.addButton (ButtonID.NOTE, "Note", new SelectNoteViewCommand (this.model, surface), buttonIDs.get (ButtonID.NOTE).intValue ());
        this.addButton (ButtonID.DEVICE, "Device", new SelectDeviceViewCommand (this.model, surface), buttonIDs.get (ButtonID.DEVICE).intValue ());

        // The following buttons are only available on the Pro but the commands are used by all
        // Launchpad models!
        this.addButton (ButtonID.METRONOME, "Metronome", new ClickCommand (this.model, surface), LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_CLICK);
        this.addButton (ButtonID.UNDO, "Undo", new UndoCommand<> (this.model, surface), LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_UNDO);
        this.addButton (ButtonID.DELETE, "Delete", new DeleteCommand<> (this.model, surface), LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_DELETE);
        this.addButton (ButtonID.QUANTIZE, "Quantize", new QuantizeCommand<> (this.model, surface), LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_QUANTIZE);
        this.addButton (ButtonID.DUPLICATE, "Duplicate", new LaunchpadDuplicateCommand (this.model, surface), LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_DUPLICATE);
        this.addButton (ButtonID.DOUBLE, "Double", new PlayAndNewCommand (this.model, surface), LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_DOUBLE);
        this.addButton (ButtonID.RECORD, "Record", new RecordCommand<> (this.model, surface), LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_RECORD);
        this.addButton (ButtonID.REC_ARM, "Rec Arm", new RecordArmCommand (this.model, surface), LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_REC_ARM);
        this.addButton (ButtonID.TRACK, "Track", new TrackSelectCommand (this.model, surface), LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_TRACK);
        this.addButton (ButtonID.MUTE, "Mute", new MuteCommand (this.model, surface), LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_MUTE);
        this.addButton (ButtonID.SOLO, "Solo", new SoloCommand (this.model, surface), LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_SOLO);
        this.addButton (ButtonID.VOLUME, "Volume", new VolumeCommand (this.model, surface), LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_VOLUME);
        this.addButton (ButtonID.PAN_SEND, "Panorama", new PanCommand (this.model, surface), LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_PAN);
        this.addButton (ButtonID.SENDS, "Sends", new SendsCommand (this.model, surface), LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_SENDS);
        this.addButton (ButtonID.STOP_CLIP, "Stop Clip", new StopClipCommand (this.model, surface), LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_STOP_CLIP);

        for (int i = 0; i < 8; i++)
        {
            final ButtonID buttonID = ButtonID.get (ButtonID.SCENE1, i);
            this.addButton (buttonID, "Scene " + (i + 1), new SceneCommand<> (i, this.model, surface), buttonIDs.get (buttonID).intValue ());
        }
    }


    /** {@inheritDoc} */
    @Override
    protected BindType getTriggerBindType (final ButtonID buttonID)
    {
        if (ButtonID.isSceneButton (buttonID))
            return this.definition.sceneButtonsUseCC () ? BindType.CC : BindType.NOTE;
        return super.getTriggerBindType (buttonID);
    }


    /** {@inheritDoc} */
    @SuppressWarnings(
    {
        "rawtypes",
        "unchecked"
    })
    @Override
    protected void registerContinuousCommands ()
    {
        final LaunchpadControlSurface surface = this.getSurface ();
        for (int i = 0; i < 8; i++)
            this.addFader (ContinuousID.get (ContinuousID.FADER1, i), "Fader " + (i + 1), new FaderCommand (i, this.model, surface), BindType.CC, LaunchpadControlSurface.LAUNCHPAD_FADER_1 + i);
        final ViewManager viewManager = surface.getViewManager ();

        final Views [] views =
        {
            Views.PLAY,
            Views.PIANO,
            Views.DRUM,
            Views.DRUM64
        };
        for (final Views viewID: views)
        {
            final AbstractView view = (AbstractView) viewManager.getView (viewID);
            view.registerAftertouchCommand (new AftertouchAbstractViewCommand (view, this.model, surface));
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void layoutControls ()
    {
        final LaunchpadControlSurface surface = this.getSurface ();

        surface.getButton (ButtonID.PAD1).setBounds (104.75, 632.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD2).setBounds (179.25, 632.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD3).setBounds (253.0, 632.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD4).setBounds (326.5, 632.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD5).setBounds (401.0, 632.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD6).setBounds (473.75, 632.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD7).setBounds (550.0, 632.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD8).setBounds (623.5, 632.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD9).setBounds (104.75, 558.25, 61.0, 60.0);
        surface.getButton (ButtonID.PAD10).setBounds (179.25, 558.25, 61.0, 60.0);
        surface.getButton (ButtonID.PAD11).setBounds (253.0, 558.25, 61.0, 60.0);
        surface.getButton (ButtonID.PAD12).setBounds (326.5, 558.25, 61.0, 60.0);
        surface.getButton (ButtonID.PAD13).setBounds (401.0, 558.25, 61.0, 60.0);
        surface.getButton (ButtonID.PAD14).setBounds (473.75, 558.25, 61.0, 60.0);
        surface.getButton (ButtonID.PAD15).setBounds (550.0, 558.25, 61.0, 60.0);
        surface.getButton (ButtonID.PAD16).setBounds (623.5, 558.25, 61.0, 60.0);
        surface.getButton (ButtonID.PAD17).setBounds (104.75, 486.5, 61.0, 60.0);
        surface.getButton (ButtonID.PAD18).setBounds (179.25, 486.5, 61.0, 60.0);
        surface.getButton (ButtonID.PAD19).setBounds (253.0, 486.5, 61.0, 60.0);
        surface.getButton (ButtonID.PAD20).setBounds (326.5, 486.5, 61.0, 60.0);
        surface.getButton (ButtonID.PAD21).setBounds (401.0, 486.5, 61.0, 60.0);
        surface.getButton (ButtonID.PAD22).setBounds (473.75, 486.5, 61.0, 60.0);
        surface.getButton (ButtonID.PAD23).setBounds (550.0, 486.5, 61.0, 60.0);
        surface.getButton (ButtonID.PAD24).setBounds (623.5, 486.5, 61.0, 60.0);
        surface.getButton (ButtonID.PAD25).setBounds (104.75, 409.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD26).setBounds (179.25, 409.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD27).setBounds (253.0, 409.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD28).setBounds (326.5, 409.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD29).setBounds (401.0, 409.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD30).setBounds (473.75, 409.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD31).setBounds (550.0, 409.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD32).setBounds (623.5, 409.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD33).setBounds (104.75, 335.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD34).setBounds (179.25, 335.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD35).setBounds (253.0, 335.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD36).setBounds (326.5, 335.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD37).setBounds (401.0, 335.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD38).setBounds (473.75, 335.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD39).setBounds (550.0, 335.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD40).setBounds (623.5, 335.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD41).setBounds (104.75, 262.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD42).setBounds (179.25, 262.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD43).setBounds (253.0, 262.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD44).setBounds (326.5, 262.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD45).setBounds (401.0, 262.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD46).setBounds (473.75, 262.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD47).setBounds (550.0, 262.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD48).setBounds (623.5, 262.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD49).setBounds (104.75, 186.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD50).setBounds (179.25, 186.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD51).setBounds (253.0, 186.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD52).setBounds (326.5, 186.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD53).setBounds (401.0, 186.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD54).setBounds (473.75, 186.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD55).setBounds (550.0, 186.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD56).setBounds (623.5, 186.75, 61.0, 60.0);
        surface.getButton (ButtonID.PAD57).setBounds (104.75, 115.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD58).setBounds (179.25, 115.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD59).setBounds (253.0, 115.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD60).setBounds (326.5, 115.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD61).setBounds (401.0, 115.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD62).setBounds (473.75, 115.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD63).setBounds (550.0, 115.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAD64).setBounds (623.5, 115.0, 61.0, 60.0);
        surface.getButton (ButtonID.SHIFT).setBounds (31.75, 115.0, 61.0, 60.0);
        surface.getButton (ButtonID.USER).setBounds (623.5, 44.5, 61.0, 60.0);
        surface.getButton (ButtonID.UP).setBounds (104.75, 44.5, 61.0, 60.0);
        surface.getButton (ButtonID.DOWN).setBounds (179.25, 44.5, 61.0, 60.0);
        surface.getButton (ButtonID.LEFT).setBounds (253.0, 44.5, 61.0, 60.0);
        surface.getButton (ButtonID.RIGHT).setBounds (326.5, 44.5, 61.0, 60.0);
        surface.getButton (ButtonID.SESSION).setBounds (401.0, 44.5, 61.0, 60.0);
        surface.getButton (ButtonID.NOTE).setBounds (473.75, 44.5, 61.0, 60.0);
        surface.getButton (ButtonID.DEVICE).setBounds (550.0, 44.5, 61.0, 60.0);
        surface.getButton (ButtonID.METRONOME).setBounds (31.75, 186.75, 61.0, 60.0);
        surface.getButton (ButtonID.UNDO).setBounds (31.75, 262.75, 61.0, 60.0);
        surface.getButton (ButtonID.DELETE).setBounds (31.75, 335.75, 61.0, 60.0);
        surface.getButton (ButtonID.QUANTIZE).setBounds (31.75, 409.0, 61.0, 60.0);
        surface.getButton (ButtonID.DUPLICATE).setBounds (31.75, 486.5, 61.0, 60.0);
        surface.getButton (ButtonID.DOUBLE).setBounds (31.75, 558.25, 61.0, 60.0);
        surface.getButton (ButtonID.RECORD).setBounds (31.75, 632.0, 61.0, 60.0);
        surface.getButton (ButtonID.REC_ARM).setBounds (104.0, 704.0, 61.0, 60.0);
        surface.getButton (ButtonID.TRACK).setBounds (179.25, 704.0, 61.0, 60.0);
        surface.getButton (ButtonID.MUTE).setBounds (253.0, 704.0, 61.0, 60.0);
        surface.getButton (ButtonID.SOLO).setBounds (326.5, 704.0, 61.0, 60.0);
        surface.getButton (ButtonID.VOLUME).setBounds (401.0, 704.0, 61.0, 60.0);
        surface.getButton (ButtonID.PAN_SEND).setBounds (473.75, 704.0, 61.0, 60.0);
        surface.getButton (ButtonID.SENDS).setBounds (550.0, 704.0, 61.0, 60.0);
        surface.getButton (ButtonID.STOP_CLIP).setBounds (623.5, 704.0, 61.0, 60.0);
        surface.getButton (ButtonID.SCENE1).setBounds (697.75, 115.0, 61.0, 60.0);
        surface.getButton (ButtonID.SCENE2).setBounds (697.75, 186.75, 61.0, 60.0);
        surface.getButton (ButtonID.SCENE3).setBounds (697.75, 262.75, 61.0, 60.0);
        surface.getButton (ButtonID.SCENE4).setBounds (697.75, 335.75, 61.0, 60.0);
        surface.getButton (ButtonID.SCENE5).setBounds (697.75, 409.0, 61.0, 60.0);
        surface.getButton (ButtonID.SCENE6).setBounds (697.75, 486.5, 61.0, 60.0);
        surface.getButton (ButtonID.SCENE7).setBounds (697.75, 632.0, 53.5, 52.5);
        surface.getButton (ButtonID.SCENE8).setBounds (697.75, 558.25, 53.5, 52.5);

        surface.getContinuous (ContinuousID.FADER1).setBounds (83.5, 765.0, 65.5, 92.5);
        surface.getContinuous (ContinuousID.FADER2).setBounds (166.0, 765.0, 65.5, 92.5);
        surface.getContinuous (ContinuousID.FADER3).setBounds (248.25, 765.0, 65.5, 92.5);
        surface.getContinuous (ContinuousID.FADER4).setBounds (330.75, 765.0, 65.5, 92.5);
        surface.getContinuous (ContinuousID.FADER5).setBounds (413.25, 765.0, 65.5, 92.5);
        surface.getContinuous (ContinuousID.FADER6).setBounds (495.5, 765.0, 65.5, 92.5);
        surface.getContinuous (ContinuousID.FADER7).setBounds (578.0, 765.0, 65.5, 92.5);
        surface.getContinuous (ContinuousID.FADER8).setBounds (660.5, 765.0, 65.5, 92.5);
    }


    /** {@inheritDoc} */
    @Override
    public void startup ()
    {
        this.getSurface ().getViewManager ().setActiveView (Views.PLAY);
    }

    // TODO
    // final ViewManager viewManager = surface.getViewManager ();
    // final View activeView = viewManager.getActiveView ();
    // if (activeView != null)
    // {
    // // TODO ((LaunchpadCursorCommand) activeView.getTriggerCommand
    // // (TriggerCommandID.ARROW_DOWN)).updateArrows ();
    // // for (int i = 0; i < this.model.getSceneBank ().getPageSize (); i++)
    // // ((SceneView) activeView).updateSceneButton (i);
    // }
    //
    // final boolean isShift = surface.isShiftPressed ();
    // // TODO needs to be configured per controller model
    // // surface.setTrigger (surface.getTriggerId (ButtonID.SHIFT), isShift ?
    // // LaunchpadColors.LAUNCHPAD_COLOR_WHITE : LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO);
    //
    // // Update the front or logo LED with the color of the current track
    // final ITrack selTrack = this.model.getSelectedTrack ();
    // final int index = selTrack == null ? -1 : selTrack.getIndex ();
    // final ITrack track = index == -1 ? null : this.model.getCurrentTrackBank ().getItem (index);
    // final int color = track != null && track.doesExist () ? this.colorManager.getColorIndex
    // (DAWColors.getColorIndex (track.getColor ())) : 0;
    // if (this.definition.isPro ())
    // {
    // if (color != this.frontColor)
    // {
    // surface.sendLaunchpadSysEx ("0A 63 " + StringUtils.toHexStr (color));
    // this.frontColor = color;
    // }
    // }
    // else
    // surface.setTrigger (LaunchpadControlSurface.LAUNCHPAD_LOGO, color);
    //
    // if (!this.definition.isPro ())
    // return;
    //
    // final ModeManager modeManager = surface.getModeManager ();
    // final ITransport transport = this.model.getTransport ();
    //
    // surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_CLICK, isShift ?
    // LaunchpadColors.LAUNCHPAD_COLOR_GREEN_SPRING : transport.isMetronomeOn () ?
    // LaunchpadColors.LAUNCHPAD_COLOR_GREEN_HI : LaunchpadColors.LAUNCHPAD_COLOR_GREEN_LO);
    // surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_UNDO, isShift ?
    // LaunchpadColors.LAUNCHPAD_COLOR_GREEN_SPRING : LaunchpadColors.LAUNCHPAD_COLOR_GREEN_LO);
    // surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_DELETE, isShift ?
    // LaunchpadColors.LAUNCHPAD_COLOR_BLACK : LaunchpadColors.LAUNCHPAD_COLOR_GREEN_LO);
    // surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_QUANTIZE, isShift ?
    // LaunchpadColors.LAUNCHPAD_COLOR_BLACK : LaunchpadColors.LAUNCHPAD_COLOR_GREEN_LO);
    // surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_DUPLICATE, isShift ?
    // LaunchpadColors.LAUNCHPAD_COLOR_GREEN_SPRING : LaunchpadColors.LAUNCHPAD_COLOR_GREEN_LO);
    // surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_DOUBLE, isShift ?
    // LaunchpadColors.LAUNCHPAD_COLOR_GREEN_SPRING : LaunchpadColors.LAUNCHPAD_COLOR_GREEN_LO);
    // final boolean flipRecord = surface.getConfiguration ().isFlipRecord ();
    // surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_RECORD, isShift &&
    // !flipRecord || !isShift && flipRecord ? transport.isLauncherOverdub () ?
    // LaunchpadColors.LAUNCHPAD_COLOR_ROSE : LaunchpadColors.LAUNCHPAD_COLOR_RED_AMBER :
    // transport.isRecording () ? LaunchpadColors.LAUNCHPAD_COLOR_RED_HI :
    // LaunchpadColors.LAUNCHPAD_COLOR_RED_LO);
    //
    // surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_REC_ARM,
    // modeManager.isActiveOrTempMode (Modes.REC_ARM) ? LaunchpadColors.LAUNCHPAD_COLOR_RED : index
    // == 0 ? LaunchpadColors.LAUNCHPAD_COLOR_WHITE : LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO);
    // surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_TRACK,
    // modeManager.isActiveOrTempMode (Modes.TRACK_SELECT) ? LaunchpadColors.LAUNCHPAD_COLOR_GREEN :
    // index == 1 ? LaunchpadColors.LAUNCHPAD_COLOR_WHITE :
    // LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO);
    // surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_MUTE,
    // modeManager.isActiveOrTempMode (Modes.MUTE) ? LaunchpadColors.LAUNCHPAD_COLOR_YELLOW : index
    // == 2 ? LaunchpadColors.LAUNCHPAD_COLOR_WHITE : LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO);
    // surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_SOLO,
    // modeManager.isActiveOrTempMode (Modes.SOLO) ? LaunchpadColors.LAUNCHPAD_COLOR_BLUE : index ==
    // 3 ? LaunchpadColors.LAUNCHPAD_COLOR_WHITE : LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO);
    // surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_VOLUME,
    // viewManager.isActiveView (Views.TRACK_VOLUME) ? LaunchpadColors.LAUNCHPAD_COLOR_CYAN : index
    // == 4 ? LaunchpadColors.LAUNCHPAD_COLOR_WHITE : LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO);
    // surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_PAN,
    // viewManager.isActiveView (Views.TRACK_PAN) ? LaunchpadColors.LAUNCHPAD_COLOR_SKY : index == 5
    // ? LaunchpadColors.LAUNCHPAD_COLOR_WHITE : LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO);
    // surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_SENDS,
    // viewManager.isActiveView (Views.TRACK_SENDS) ? LaunchpadColors.LAUNCHPAD_COLOR_ORCHID : index
    // == 6 ? LaunchpadColors.LAUNCHPAD_COLOR_WHITE : LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO);
    // surface.setTrigger (LaunchpadProControllerDefinition.LAUNCHPAD_BUTTON_STOP_CLIP,
    // modeManager.isActiveOrTempMode (Modes.STOP_CLIP) ? LaunchpadColors.LAUNCHPAD_COLOR_ROSE :
    // index == 7 ? LaunchpadColors.LAUNCHPAD_COLOR_WHITE :
    // LaunchpadColors.LAUNCHPAD_COLOR_GREY_LO);
    // }


    /** {@inheritDoc} */
    @Override
    protected void updateIndication (final Modes mode)
    {
        final ViewManager viewManager = this.getSurface ().getViewManager ();
        final boolean isVolume = viewManager.isActiveView (Views.TRACK_VOLUME);
        final boolean isPan = viewManager.isActiveView (Views.TRACK_PAN);
        final boolean isSends = viewManager.isActiveView (Views.TRACK_SENDS);
        final boolean isDevice = viewManager.isActiveView (Views.DEVICE);

        final ITrackBank tb = this.model.getTrackBank ();
        final ITrackBank tbe = this.model.getEffectTrackBank ();
        final ICursorDevice cursorDevice = this.model.getCursorDevice ();
        final View view = viewManager.getActiveView ();
        final int selSend = view instanceof SendsView ? ((SendsView) view).getSelectedSend () : -1;
        final boolean isSession = view instanceof SessionView && !isVolume && !isPan && !isSends;

        final boolean isEffect = this.model.isEffectTrackBankActive ();

        tb.setIndication (!isEffect && isSession);
        if (tbe != null)
            tbe.setIndication (isEffect && isSession);

        final IParameterBank parameterBank = cursorDevice.getParameterBank ();
        for (int i = 0; i < 8; i++)
        {
            final ITrack track = tb.getItem (i);
            track.setVolumeIndication (!isEffect && isVolume);
            track.setPanIndication (!isEffect && isPan);
            final ISendBank sendBank = track.getSendBank ();
            for (int j = 0; j < 8; j++)
                sendBank.getItem (j).setIndication (!isEffect && isSends && selSend == j);

            if (tbe != null)
            {
                final ITrack fxTrack = tbe.getItem (i);
                fxTrack.setVolumeIndication (isEffect && isVolume);
                fxTrack.setPanIndication (isEffect && isPan);
            }

            parameterBank.getItem (i).setIndication (isDevice);
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

        // Recall last used view (if we are not in session mode)
        final ViewManager viewManager = this.getSurface ().getViewManager ();
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
}
