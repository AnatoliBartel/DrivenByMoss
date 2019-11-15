// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.sl;

import de.mossgrabers.controller.sl.command.continuous.DeviceKnobRowCommand;
import de.mossgrabers.controller.sl.command.continuous.FaderCommand;
import de.mossgrabers.controller.sl.command.continuous.TapTempoInitMkICommand;
import de.mossgrabers.controller.sl.command.continuous.TapTempoMkICommand;
import de.mossgrabers.controller.sl.command.continuous.TouchpadCommand;
import de.mossgrabers.controller.sl.command.continuous.TrackKnobRowCommand;
import de.mossgrabers.controller.sl.command.trigger.ButtonRowSelectCommand;
import de.mossgrabers.controller.sl.command.trigger.ButtonRowViewCommand;
import de.mossgrabers.controller.sl.command.trigger.P1ButtonCommand;
import de.mossgrabers.controller.sl.command.trigger.P2ButtonCommand;
import de.mossgrabers.controller.sl.command.trigger.TransportButtonCommand;
import de.mossgrabers.controller.sl.controller.SLControlSurface;
import de.mossgrabers.controller.sl.controller.SLDisplay;
import de.mossgrabers.controller.sl.mode.FixedMode;
import de.mossgrabers.controller.sl.mode.FrameMode;
import de.mossgrabers.controller.sl.mode.FunctionMode;
import de.mossgrabers.controller.sl.mode.MasterMode;
import de.mossgrabers.controller.sl.mode.PlayOptionsMode;
import de.mossgrabers.controller.sl.mode.SessionMode;
import de.mossgrabers.controller.sl.mode.TrackMode;
import de.mossgrabers.controller.sl.mode.TrackTogglesMode;
import de.mossgrabers.controller.sl.mode.ViewSelectMode;
import de.mossgrabers.controller.sl.mode.VolumeMode;
import de.mossgrabers.controller.sl.mode.device.DeviceParamsMode;
import de.mossgrabers.controller.sl.mode.device.DevicePresetsMode;
import de.mossgrabers.controller.sl.view.ControlView;
import de.mossgrabers.controller.sl.view.PlayView;
import de.mossgrabers.framework.command.ContinuousCommandID;
import de.mossgrabers.framework.configuration.ISettingsUI;
import de.mossgrabers.framework.controller.AbstractControllerSetup;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.DefaultValueChanger;
import de.mossgrabers.framework.controller.ISetupFactory;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.grid.PadGrid;
import de.mossgrabers.framework.daw.ICursorDevice;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.IParameterBank;
import de.mossgrabers.framework.daw.ISendBank;
import de.mossgrabers.framework.daw.ITrackBank;
import de.mossgrabers.framework.daw.ModelSetup;
import de.mossgrabers.framework.daw.data.IMasterTrack;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.midi.IMidiAccess;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.daw.midi.IMidiOutput;
import de.mossgrabers.framework.mode.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.view.ViewManager;
import de.mossgrabers.framework.view.Views;


/**
 * Support for the Novation SLMkII Pro and SLMkII MkII controllers.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class SLControllerSetup extends AbstractControllerSetup<SLControlSurface, SLConfiguration>
{
    private static final int [] DRUM_MATRIX =
    {
        0,
        1,
        2,
        3,
        4,
        5,
        6,
        7,
        8,
        9,
        10,
        11,
        12,
        13,
        14,
        15,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1
    };

    private final boolean       isMkII;


    /**
     * Constructor.
     *
     * @param host The DAW host
     * @param factory The factory
     * @param globalSettings The global settings
     * @param documentSettings The document (project) specific settings
     * @param isMkII True if SLMkII
     */
    public SLControllerSetup (final IHost host, final ISetupFactory factory, final ISettingsUI globalSettings, final ISettingsUI documentSettings, final boolean isMkII)
    {
        super (factory, host, globalSettings, documentSettings);
        this.isMkII = isMkII;
        this.colorManager = new ColorManager ();
        this.colorManager.registerColorIndex (PadGrid.GRID_OFF, 0);
        this.valueChanger = new DefaultValueChanger (128, 1, 0.5);
        this.configuration = new SLConfiguration (host, this.valueChanger, isMkII);
    }


    /** {@inheritDoc} */
    @Override
    public void flush ()
    {
        super.flush ();

        this.updateIndication (this.getSurface ().getModeManager ().getActiveOrTempModeId ());
    }


    /** {@inheritDoc} */
    @Override
    protected void createScales ()
    {
        this.scales = new Scales (this.valueChanger, 36, 52, 8, 2);
        this.scales.setDrumMatrix (DRUM_MATRIX);
        this.scales.setDrumNoteEnd (52);
    }


    /** {@inheritDoc} */
    @Override
    protected void createModel ()
    {
        final ModelSetup ms = new ModelSetup ();
        ms.setNumSends (6);
        this.model = this.factory.createModel (this.colorManager, this.valueChanger, this.scales, ms);
        this.model.getTrackBank ().addSelectionObserver ( (index, isSelected) -> this.handleTrackChange (isSelected));
        this.model.getMasterTrack ().addSelectionObserver ( (index, isSelected) -> {
            if (!isSelected)
                return;
            final ModeManager modeManager = this.getSurface ().getModeManager ();
            if (!modeManager.isActiveOrTempMode (Modes.VOLUME))
                modeManager.setActiveMode (Modes.MASTER);
        });
    }


    /** {@inheritDoc} */
    @Override
    protected void createSurface ()
    {
        final IMidiAccess midiAccess = this.factory.createMidiAccess ();
        final IMidiOutput output = midiAccess.createOutput ();
        final IMidiInput input = midiAccess.createInput (this.isMkII ? "Novation SL MkII (Drumpads)" : "Novation SL MkI (Drumpads)", "90????", "80????");
        midiAccess.createInput (1, this.isMkII ? "Novation SL MkII (Keyboard)" : "Novation SL MkI (Keyboard)", "80????", "90????", "B0????", "D0????", "E0????");
        final IHost hostProxy = this.model.getHost ();
        final SLControlSurface surface = new SLControlSurface (hostProxy, this.colorManager, this.configuration, output, input, this.isMkII);
        surface.addTextDisplay (new SLDisplay (hostProxy, output));
        this.surfaces.add (surface);
    }


    /** {@inheritDoc} */
    @Override
    protected void createObservers ()
    {
        this.createScaleObservers (this.configuration);
    }


    /** {@inheritDoc} */
    @Override
    protected void createModes ()
    {
        final SLControlSurface surface = this.getSurface ();
        final ModeManager modeManager = surface.getModeManager ();
        modeManager.registerMode (Modes.FIXED, new FixedMode (surface, this.model));
        modeManager.registerMode (Modes.FRAME, new FrameMode (surface, this.model));
        modeManager.registerMode (Modes.FUNCTIONS, new FunctionMode (surface, this.model));
        modeManager.registerMode (Modes.MASTER, new MasterMode (surface, this.model));
        modeManager.registerMode (Modes.PLAY_OPTIONS, new PlayOptionsMode (surface, this.model));
        modeManager.registerMode (Modes.SESSION, new SessionMode (surface, this.model));
        modeManager.registerMode (Modes.TRACK, new TrackMode (surface, this.model));
        modeManager.registerMode (Modes.TRACK_DETAILS, new TrackTogglesMode (surface, this.model));
        modeManager.registerMode (Modes.VIEW_SELECT, new ViewSelectMode (surface, this.model));
        modeManager.registerMode (Modes.VOLUME, new VolumeMode (surface, this.model));
        modeManager.registerMode (Modes.DEVICE_PARAMS, new DeviceParamsMode (surface, this.model));
        modeManager.registerMode (Modes.BROWSER, new DevicePresetsMode (surface, this.model));
    }


    /** {@inheritDoc} */
    @Override
    protected void createViews ()
    {
        final SLControlSurface surface = this.getSurface ();
        final ViewManager viewManager = surface.getViewManager ();
        viewManager.registerView (Views.PLAY, new PlayView (surface, this.model));
        viewManager.registerView (Views.CONTROL, new ControlView (surface, this.model));
    }


    /** {@inheritDoc} */
    @Override
    protected void registerTriggerCommands ()
    {
        final SLControlSurface surface = this.getSurface ();
        for (int i = 0; i < 8; i++)
        {
            this.addButton (ButtonID.get (ButtonID.ROW1_1, i), "TODO", new ButtonRowViewCommand<> (0, i, this.model, surface), SLControlSurface.MKII_BUTTON_ROW1_1 + i);
            this.addButton (ButtonID.get (ButtonID.ROW2_1, i), "TODO", new ButtonRowViewCommand<> (1, i, this.model, surface), SLControlSurface.MKII_BUTTON_ROW2_1 + i);
            this.addButton (ButtonID.get (ButtonID.ROW3_1, i), "TODO", new ButtonRowViewCommand<> (2, i, this.model, surface), SLControlSurface.MKII_BUTTON_ROW3_1 + i);
            this.addButton (ButtonID.get (ButtonID.ROW4_1, i), "TODO", new ButtonRowViewCommand<> (3, i, this.model, surface), SLControlSurface.MKII_BUTTON_ROW4_1 + i);
            this.addButton (ButtonID.get (ButtonID.ROW_SELECT_1, i), "TODO", new ButtonRowSelectCommand<> (i, this.model, surface), SLControlSurface.MKII_BUTTON_ROWSEL1 + i);
        }

        this.addButton (ButtonID.REWIND, "<<", new ButtonRowViewCommand<> (4, 0, this.model, surface), SLControlSurface.MKII_BUTTON_REWIND);
        this.addButton (ButtonID.FORWARD, ">>", new ButtonRowViewCommand<> (4, 1, this.model, surface), SLControlSurface.MKII_BUTTON_FORWARD);
        this.addButton (ButtonID.STOP, "Stop", new ButtonRowViewCommand<> (4, 2, this.model, surface), SLControlSurface.MKII_BUTTON_STOP);
        this.addButton (ButtonID.PLAY, "Play", new ButtonRowViewCommand<> (4, 3, this.model, surface), SLControlSurface.MKII_BUTTON_PLAY);
        this.addButton (ButtonID.LOOP, "Loop", new ButtonRowViewCommand<> (4, 4, this.model, surface), SLControlSurface.MKII_BUTTON_LOOP);
        this.addButton (ButtonID.RECORD, "Record", new ButtonRowViewCommand<> (4, 6, this.model, surface), SLControlSurface.MKII_BUTTON_RECORD);
        this.addButton (ButtonID.ARROW_LEFT, "Left", new P1ButtonCommand (true, this.model, surface), SLControlSurface.MKII_BUTTON_P1_UP);
        this.addButton (ButtonID.ARROW_RIGHT, "Right", new P1ButtonCommand (false, this.model, surface), SLControlSurface.MKII_BUTTON_P1_DOWN);
        this.addButton (ButtonID.ARROW_UP, "Up", new P2ButtonCommand (true, this.model, surface), SLControlSurface.MKII_BUTTON_P2_UP);
        this.addButton (ButtonID.ARROW_DOWN, "Down", new P2ButtonCommand (false, this.model, surface), SLControlSurface.MKII_BUTTON_P2_DOWN);
        this.addButton (ButtonID.NOTE, "Play View", new TransportButtonCommand (this.model, surface), SLControlSurface.MKII_BUTTON_TRANSPORT);
    }


    /** {@inheritDoc} */
    @Override
    protected void registerContinuousCommands ()
    {
        final SLControlSurface surface = this.getSurface ();
        for (int i = 0; i < 8; i++)
        {
            this.addContinuousCommand (ContinuousCommandID.get (ContinuousCommandID.FADER1, i), SLControlSurface.MKII_SLIDER1 + i, new FaderCommand (i, this.model, surface));
            this.addContinuousCommand (ContinuousCommandID.get (ContinuousCommandID.DEVICE_KNOB1, i), SLControlSurface.MKII_KNOB_ROW1_1 + i, new DeviceKnobRowCommand (i, this.model, surface));
            this.addContinuousCommand (ContinuousCommandID.get (ContinuousCommandID.KNOB1, i), SLControlSurface.MKII_KNOB_ROW2_1 + i, new TrackKnobRowCommand (i, this.model, surface));
        }
        this.addContinuousCommand (ContinuousCommandID.TOUCHPAD_X, SLControlSurface.MKII_TOUCHPAD_X, new TouchpadCommand (true, this.model, surface));
        this.addContinuousCommand (ContinuousCommandID.TOUCHPAD_Y, SLControlSurface.MKII_TOUCHPAD_Y, new TouchpadCommand (false, this.model, surface));
        this.addContinuousCommand (ContinuousCommandID.HELLO, SLControlSurface.MKI_BUTTON_TAP_TEMPO, new TapTempoInitMkICommand (this.model, surface));
        this.addContinuousCommand (ContinuousCommandID.TEMPO, SLControlSurface.MKI_BUTTON_TAP_TEMPO_VALUE, new TapTempoMkICommand (this.model, surface));
    }


    /** {@inheritDoc} */
    @Override
    public void startup ()
    {
        // Initialise 2nd display
        final SLControlSurface surface = this.getSurface ();
        final ModeManager modeManager = surface.getModeManager ();
        modeManager.getMode (Modes.VOLUME).updateDisplay ();
        surface.getViewManager ().setActiveView (Views.CONTROL);
        modeManager.setActiveMode (Modes.TRACK);
    }


    /** {@inheritDoc} */
    @Override
    protected void updateIndication (final Modes mode)
    {
        if (this.currentMode != null && this.currentMode.equals (mode))
            return;
        this.currentMode = mode;

        final IMasterTrack mt = this.model.getMasterTrack ();
        mt.setVolumeIndication (Modes.MASTER.equals (mode));
        mt.setPanIndication (Modes.MASTER.equals (mode));

        final ITrackBank tb = this.model.getTrackBank ();
        final ITrackBank tbe = this.model.getEffectTrackBank ();
        final boolean isEffect = this.model.isEffectTrackBankActive ();
        final boolean isVolume = Modes.VOLUME.equals (mode);

        final ICursorDevice cursorDevice = this.model.getCursorDevice ();
        final IParameterBank parameterBank = cursorDevice.getParameterBank ();
        final ITrack selectedTrack = tb.getSelectedItem ();
        for (int i = 0; i < 8; i++)
        {
            final boolean hasTrackSel = selectedTrack != null && selectedTrack.getIndex () == i && Modes.TRACK.equals (mode);
            final ITrack track = tb.getItem (i);
            track.setVolumeIndication (!isEffect && (isVolume || hasTrackSel));
            track.setPanIndication (!isEffect && hasTrackSel);

            final ISendBank sendBank = track.getSendBank ();
            for (int j = 0; j < 6; j++)
                sendBank.getItem (j).setIndication (!isEffect && hasTrackSel);

            if (tbe != null)
            {
                final ITrack fxTrack = tbe.getItem (i);
                fxTrack.setVolumeIndication (isEffect);
                fxTrack.setPanIndication (isEffect);
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
        final ModeManager modeManager = this.getSurface ().getModeManager ();
        if (isSelected && modeManager.isActiveOrTempMode (Modes.MASTER))
            modeManager.setActiveMode (Modes.TRACK);
    }
}
