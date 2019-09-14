// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.slmkiii;

import de.mossgrabers.controller.slmkiii.command.continuous.VolumeFaderCommand;
import de.mossgrabers.controller.slmkiii.command.trigger.ButtonAreaCommand;
import de.mossgrabers.controller.slmkiii.command.trigger.SLMkIIICursorCommand;
import de.mossgrabers.controller.slmkiii.command.trigger.TrackModeCommand;
import de.mossgrabers.controller.slmkiii.controller.SLMkIIIColors;
import de.mossgrabers.controller.slmkiii.controller.SLMkIIIControlSurface;
import de.mossgrabers.controller.slmkiii.controller.SLMkIIIDisplay;
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
import de.mossgrabers.framework.command.ContinuousCommandID;
import de.mossgrabers.framework.command.SceneCommand;
import de.mossgrabers.framework.command.TriggerCommandID;
import de.mossgrabers.framework.command.continuous.KnobRowModeCommand;
import de.mossgrabers.framework.command.core.NopCommand;
import de.mossgrabers.framework.command.trigger.ShiftCommand;
import de.mossgrabers.framework.command.trigger.mode.ButtonRowModeCommand;
import de.mossgrabers.framework.command.trigger.mode.CursorCommand;
import de.mossgrabers.framework.command.trigger.mode.ModeCursorCommand.Direction;
import de.mossgrabers.framework.command.trigger.mode.ModeSelectCommand;
import de.mossgrabers.framework.command.trigger.transport.PlayCommand;
import de.mossgrabers.framework.command.trigger.transport.RecordCommand;
import de.mossgrabers.framework.command.trigger.transport.StopCommand;
import de.mossgrabers.framework.command.trigger.transport.ToggleLoopCommand;
import de.mossgrabers.framework.command.trigger.transport.WindCommand;
import de.mossgrabers.framework.configuration.ISettingsUI;
import de.mossgrabers.framework.controller.AbstractControllerSetup;
import de.mossgrabers.framework.controller.DefaultValueChanger;
import de.mossgrabers.framework.controller.ISetupFactory;
import de.mossgrabers.framework.controller.color.ColorManager;
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
import de.mossgrabers.framework.view.SceneView;
import de.mossgrabers.framework.view.View;
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

        this.colorManager = new ColorManager ();
        SLMkIIIColors.addColors (this.colorManager);

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
        midiAccess.createInput (1, "Keyboard", "8?????", "9?????", "B?????", "D?????", "E?????");
        final IHost hostProxy = this.model.getHost ();
        final IMidiInput input = midiAccess.createInput ("Pads", "8?????", "9?????");
        this.surfaces.add (new SLMkIIIControlSurface (hostProxy, this.colorManager, this.configuration, output, input));
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

        this.addTriggerCommand (TriggerCommandID.REWIND, SLMkIIIControlSurface.MKIII_TRANSPORT_REWIND, new WindCommand<> (this.model, surface, false));
        this.addTriggerCommand (TriggerCommandID.FORWARD, SLMkIIIControlSurface.MKIII_TRANSPORT_FORWARD, new WindCommand<> (this.model, surface, true));
        this.addTriggerCommand (TriggerCommandID.LOOP, SLMkIIIControlSurface.MKIII_TRANSPORT_LOOP, new ToggleLoopCommand<> (this.model, surface));
        this.addTriggerCommand (TriggerCommandID.STOP, SLMkIIIControlSurface.MKIII_TRANSPORT_STOP, new StopCommand<> (this.model, surface));
        this.addTriggerCommand (TriggerCommandID.PLAY, SLMkIIIControlSurface.MKIII_TRANSPORT_PLAY, new PlayCommand<> (this.model, surface));
        this.addTriggerCommand (TriggerCommandID.RECORD, SLMkIIIControlSurface.MKIII_TRANSPORT_RECORD, new RecordCommand<> (this.model, surface));

        for (int i = 0; i < 8; i++)
        {
            this.addTriggerCommand (TriggerCommandID.get (TriggerCommandID.ROW1_1, i), SLMkIIIControlSurface.MKIII_DISPLAY_BUTTON_1 + i, new ButtonRowModeCommand<> (0, i, this.model, surface));
            this.addTriggerCommand (TriggerCommandID.get (TriggerCommandID.ROW2_1, i), SLMkIIIControlSurface.MKIII_BUTTON_ROW1_1 + i, new ButtonAreaCommand (0, i, this.model, surface));
            this.addTriggerCommand (TriggerCommandID.get (TriggerCommandID.ROW3_1, i), SLMkIIIControlSurface.MKIII_BUTTON_ROW2_1 + i, new ButtonAreaCommand (1, i, this.model, surface));
        }

        final ModeSelectCommand<SLMkIIIControlSurface, SLMkIIIConfiguration> deviceModeSelectCommand = new ModeSelectCommand<> (this.model, surface, Modes.DEVICE_PARAMS);
        this.addTriggerCommand (TriggerCommandID.ARROW_UP, SLMkIIIControlSurface.MKIII_DISPLAY_UP, event -> {
            if (event != ButtonEvent.DOWN)
                return;
            final IBrowser browser = this.model.getBrowser ();
            if (browser != null && browser.isActive ())
                browser.stopBrowsing (!this.getSurface ().isShiftPressed ());
            final ModeManager modeManager = this.getSurface ().getModeManager ();
            if (modeManager.isActiveMode (Modes.DEVICE_PARAMS))
                ((ParametersMode) modeManager.getMode (Modes.DEVICE_PARAMS)).toggleShowDevices ();
            else
                deviceModeSelectCommand.execute (event);
        });

        this.addTriggerCommand (TriggerCommandID.ARROW_DOWN, SLMkIIIControlSurface.MKIII_DISPLAY_DOWN, new TrackModeCommand (this.model, surface));

        this.addTriggerCommand (TriggerCommandID.SHIFT, SLMkIIIControlSurface.MKIII_SHIFT, new ShiftCommand<> (this.model, surface));
        this.addTriggerCommand (TriggerCommandID.USER, SLMkIIIControlSurface.MKIII_OPTIONS, new ModeSelectCommand<> (this.model, surface, Modes.FUNCTIONS, true));

        this.addTriggerCommand (TriggerCommandID.OCTAVE_UP, SLMkIIIControlSurface.MKIII_BUTTONS_UP, event -> {
            if (event == ButtonEvent.UP)
                surface.toggleMuteSolo ();
        });
        this.addTriggerCommand (TriggerCommandID.OCTAVE_DOWN, SLMkIIIControlSurface.MKIII_BUTTONS_DOWN, event -> {
            if (event == ButtonEvent.UP)
                surface.toggleMuteSolo ();
        });

        this.addTriggerCommand (TriggerCommandID.ARROW_LEFT, SLMkIIIControlSurface.MKIII_TRACK_LEFT, new SLMkIIICursorCommand (Direction.LEFT, this.model, surface));
        this.addTriggerCommand (TriggerCommandID.ARROW_RIGHT, SLMkIIIControlSurface.MKIII_TRACK_RIGHT, new SLMkIIICursorCommand (Direction.RIGHT, this.model, surface));

        this.addTriggerCommand (TriggerCommandID.SCENE1, SLMkIIIControlSurface.MKIII_SCENE_1, new SceneCommand<> (0, this.model, surface));
        this.addTriggerCommand (TriggerCommandID.SCENE2, SLMkIIIControlSurface.MKIII_SCENE_2, new SceneCommand<> (1, this.model, surface));

        this.addTriggerCommand (TriggerCommandID.SCENE7, SLMkIIIControlSurface.MKIII_SCENE_UP, event -> {
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
        });
        this.addTriggerCommand (TriggerCommandID.SCENE8, SLMkIIIControlSurface.MKIII_SCENE_DOWN, event -> {
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
        });

        this.addTriggerCommand (TriggerCommandID.SELECT_SESSION_VIEW, SLMkIIIControlSurface.MKIII_GRID, event -> {
            if (event != ButtonEvent.DOWN)
                return;
            final ViewManager viewManager = surface.getViewManager ();
            viewManager.setActiveView (viewManager.isActiveView (Views.SESSION) ? Views.DRUM : Views.SESSION);
            this.getSurface ().getDisplay ().notify (viewManager.isActiveView (Views.SESSION) ? "Session" : "Sequencer");
        });

        this.addTriggerCommand (TriggerCommandID.DUPLICATE, SLMkIIIControlSurface.MKIII_DUPLICATE, NopCommand.INSTANCE);
        this.addTriggerCommand (TriggerCommandID.DELETE, SLMkIIIControlSurface.MKIII_CLEAR, NopCommand.INSTANCE);

    }


    /** {@inheritDoc} */
    @Override
    protected void registerContinuousCommands ()
    {
        final SLMkIIIControlSurface surface = this.getSurface ();
        for (int i = 0; i < 8; i++)
        {
            this.addContinuousCommand (ContinuousCommandID.get (ContinuousCommandID.KNOB1, i), SLMkIIIControlSurface.MKIII_KNOB_1 + i, new KnobRowModeCommand<> (i, this.model, surface));
            this.addContinuousCommand (ContinuousCommandID.get (ContinuousCommandID.FADER1, i), SLMkIIIControlSurface.MKIII_FADER_1 + i, new VolumeFaderCommand (i, this.model, surface));
        }
    }


    /** {@inheritDoc} */
    @Override
    public void startup ()
    {
        final SLMkIIIControlSurface surface = this.getSurface ();
        surface.getViewManager ().setActiveView (Views.SESSION);

        final ModeManager modeManager = surface.getModeManager ();
        modeManager.setActiveMode (Modes.TRACK);

        this.host.scheduleTask ( () -> surface.getOutput ().sendSysex (DeviceInquiry.createQuery ()), 1000);
    }


    /** {@inheritDoc} */
    @Override
    protected void updateButtons ()
    {
        final SLMkIIIControlSurface surface = this.getSurface ();
        final ITransport t = this.model.getTransport ();
        final boolean isShift = surface.isShiftPressed ();

        final ViewManager viewManager = surface.getViewManager ();
        final View view = viewManager.getView (Views.SESSION);
        surface.updateTrigger (SLMkIIIControlSurface.MKIII_TRANSPORT_REWIND, ((WindCommand<?, ?>) view.getTriggerCommand (TriggerCommandID.REWIND)).isRewinding () ? SLMkIIIColors.SLMKIII_YELLOW : SLMkIIIColors.SLMKIII_YELLOW_HALF);
        surface.updateTrigger (SLMkIIIControlSurface.MKIII_TRANSPORT_FORWARD, ((WindCommand<?, ?>) view.getTriggerCommand (TriggerCommandID.FORWARD)).isForwarding () ? SLMkIIIColors.SLMKIII_YELLOW : SLMkIIIColors.SLMKIII_YELLOW_HALF);
        surface.updateTrigger (SLMkIIIControlSurface.MKIII_TRANSPORT_LOOP, t.isLoop () ? SLMkIIIColors.SLMKIII_BLUE : SLMkIIIColors.SLMKIII_BLUE_HALF);
        surface.updateTrigger (SLMkIIIControlSurface.MKIII_TRANSPORT_STOP, !t.isPlaying () ? SLMkIIIColors.SLMKIII_GREY : SLMkIIIColors.SLMKIII_DARK_GREY);
        surface.updateTrigger (SLMkIIIControlSurface.MKIII_TRANSPORT_PLAY, t.isPlaying () ? SLMkIIIColors.SLMKIII_GREEN : SLMkIIIColors.SLMKIII_GREEN_HALF);
        surface.updateTrigger (SLMkIIIControlSurface.MKIII_TRANSPORT_RECORD, isShift ? t.isLauncherOverdub () ? SLMkIIIColors.SLMKIII_AMBER : SLMkIIIColors.SLMKIII_AMBER_HALF : t.isRecording () ? SLMkIIIColors.SLMKIII_RED : SLMkIIIColors.SLMKIII_RED_HALF);

        final ModeManager modeManager = surface.getModeManager ();

        surface.updateTrigger (SLMkIIIControlSurface.MKIII_DISPLAY_UP, getDeviceModeColor (modeManager));
        surface.updateTrigger (SLMkIIIControlSurface.MKIII_DISPLAY_DOWN, getTrackModeColor (modeManager));

        surface.updateTrigger (SLMkIIIControlSurface.MKIII_BUTTONS_UP, surface.isMuteSolo () ? SLMkIIIColors.SLMKIII_ORANGE : SLMkIIIColors.SLMKIII_ORANGE_HALF);
        surface.updateTrigger (SLMkIIIControlSurface.MKIII_BUTTONS_DOWN, !surface.isMuteSolo () ? SLMkIIIColors.SLMKIII_RED : SLMkIIIColors.SLMKIII_RED_HALF);

        surface.updateTrigger (SLMkIIIControlSurface.MKIII_DUPLICATE, surface.isPressed (SLMkIIIControlSurface.MKIII_DUPLICATE) ? SLMkIIIColors.SLMKIII_AMBER : SLMkIIIColors.SLMKIII_AMBER_HALF);
        surface.updateTrigger (SLMkIIIControlSurface.MKIII_CLEAR, surface.isPressed (SLMkIIIControlSurface.MKIII_CLEAR) ? SLMkIIIColors.SLMKIII_AMBER : SLMkIIIColors.SLMKIII_AMBER_HALF);

        surface.updateTrigger (SLMkIIIControlSurface.MKIII_SCENE_UP, this.getSceneUpColor ());
        surface.updateTrigger (SLMkIIIControlSurface.MKIII_SCENE_DOWN, this.getSceneDownColor ());

        surface.updateTrigger (SLMkIIIControlSurface.MKIII_GRID, viewManager.isActiveView (Views.SESSION) ? SLMkIIIColors.SLMKIII_GREEN : SLMkIIIColors.SLMKIII_BLUE);
        surface.updateTrigger (SLMkIIIControlSurface.MKIII_OPTIONS, modeManager.isActiveOrTempMode (Modes.FUNCTIONS) ? SLMkIIIColors.SLMKIII_BROWN_DARK : SLMkIIIColors.SLMKIII_DARK_GREY);

        final SLMkIIIDisplay display = surface.getDisplay ();
        final ITrackBank tb = this.model.getTrackBank ();
        final double max = this.valueChanger.getUpperBound ();
        for (int i = 0; i < 8; i++)
        {
            final ITrack track = tb.getItem (i);
            final double [] color = track.getColor ();
            display.setFaderLEDColor (SLMkIIIControlSurface.MKIII_FADER_LED_1 + i, track.getVolume () / max, color);
        }

        final View activeView = viewManager.getActiveView ();
        if (activeView != null)
        {
            ((CursorCommand<?, ?>) activeView.getTriggerCommand (TriggerCommandID.ARROW_LEFT)).updateArrows ();
            if (activeView instanceof SceneView)
                ((SceneView) activeView).updateSceneButtons ();
        }

        this.updateSoloMuteButtons ();
    }


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
                color1 = exists ? track.isMute () ? SLMkIIIColors.SLMKIII_ORANGE : SLMkIIIColors.SLMKIII_ORANGE_HALF : SLMkIIIColors.SLMKIII_BLACK;
                color2 = exists ? track.isSolo () ? SLMkIIIColors.SLMKIII_YELLOW : SLMkIIIColors.SLMKIII_YELLOW_HALF : SLMkIIIColors.SLMKIII_BLACK;
            }
            else
            {
                color1 = exists ? track.isMonitor () ? SLMkIIIColors.SLMKIII_GREEN : SLMkIIIColors.SLMKIII_GREEN_HALF : SLMkIIIColors.SLMKIII_BLACK;
                color2 = exists ? track.isRecArm () ? SLMkIIIColors.SLMKIII_RED : SLMkIIIColors.SLMKIII_RED_HALF : SLMkIIIColors.SLMKIII_BLACK;
            }

            surface.updateTrigger (SLMkIIIControlSurface.MKIII_BUTTON_ROW1_1 + i, color1);
            surface.updateTrigger (SLMkIIIControlSurface.MKIII_BUTTON_ROW2_1 + i, color2);
        }
    }


    private int getSceneDownColor ()
    {
        final ViewManager viewManager = this.getSurface ().getViewManager ();
        if (viewManager.isActiveView (Views.SESSION))
            return this.model.getSceneBank ().canScrollForwards () ? SLMkIIIColors.SLMKIII_GREEN : SLMkIIIColors.SLMKIII_BLACK;
        else if (viewManager.isActiveView (Views.DRUM))
            return ((DrumView) viewManager.getView (Views.DRUM)).isPlayMode () ? SLMkIIIColors.SLMKIII_BLUE : SLMkIIIColors.SLMKIII_SKY_BLUE;
        else if (viewManager.isActiveView (Views.COLOR))
            return !((ColorView) viewManager.getView (Views.COLOR)).isFlip () ? SLMkIIIColors.SLMKIII_RED : SLMkIIIColors.SLMKIII_BLACK;
        return SLMkIIIColors.SLMKIII_BLACK;
    }


    private int getSceneUpColor ()
    {
        final ViewManager viewManager = this.getSurface ().getViewManager ();
        if (viewManager.isActiveView (Views.SESSION))
            return this.model.getSceneBank ().canScrollBackwards () ? SLMkIIIColors.SLMKIII_GREEN : SLMkIIIColors.SLMKIII_BLACK;
        else if (viewManager.isActiveView (Views.DRUM))
            return ((DrumView) viewManager.getView (Views.DRUM)).isPlayMode () ? SLMkIIIColors.SLMKIII_BLUE : SLMkIIIColors.SLMKIII_SKY_BLUE;
        else if (viewManager.isActiveView (Views.COLOR))
            return ((ColorView) viewManager.getView (Views.COLOR)).isFlip () ? SLMkIIIColors.SLMKIII_RED : SLMkIIIColors.SLMKIII_BLACK;
        return SLMkIIIColors.SLMKIII_BLACK;
    }


    private static int getDeviceModeColor (final ModeManager modeManager)
    {
        if (modeManager.isActiveMode (Modes.DEVICE_PARAMS))
        {
            if (((ParametersMode) modeManager.getMode (Modes.DEVICE_PARAMS)).isShowDevices ())
                return SLMkIIIColors.SLMKIII_MINT;
            return SLMkIIIColors.SLMKIII_PURPLE;
        }
        return SLMkIIIColors.SLMKIII_WHITE_HALF;
    }


    private static int getTrackModeColor (final ModeManager modeManager)
    {
        if (modeManager.isActiveMode (Modes.TRACK))
            return SLMkIIIColors.SLMKIII_GREEN;
        if (modeManager.isActiveMode (Modes.VOLUME))
            return SLMkIIIColors.SLMKIII_BLUE;
        if (modeManager.isActiveMode (Modes.PAN))
            return SLMkIIIColors.SLMKIII_ORANGE;
        if (Modes.isSendMode (modeManager.getActiveModeId ()))
            return SLMkIIIColors.SLMKIII_YELLOW;

        return SLMkIIIColors.SLMKIII_WHITE_HALF;
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
