// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.sl.view;

import de.mossgrabers.controller.sl.SLConfiguration;
import de.mossgrabers.controller.sl.command.trigger.ButtonRowSelectCommand;
import de.mossgrabers.controller.sl.command.trigger.P2ButtonCommand;
import de.mossgrabers.controller.sl.controller.SLControlSurface;
import de.mossgrabers.controller.sl.mode.device.DeviceParamsMode;
import de.mossgrabers.framework.daw.ICursorDevice;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.ISlotBank;
import de.mossgrabers.framework.daw.ITrackBank;
import de.mossgrabers.framework.daw.ITransport;
import de.mossgrabers.framework.daw.data.ISlot;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.mode.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.ControlOnlyView;
import de.mossgrabers.framework.view.Views;

import java.util.List;


/**
 * The view for controlling the DAW.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class ControlView extends ControlOnlyView<SLControlSurface, SLConfiguration> implements SLView
{
    private boolean          isTempoDec;
    private boolean          isTempoInc;
    private TransportControl transportControl;


    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public ControlView (final SLControlSurface surface, final IModel model)
    {
        super (surface, model);
        this.transportControl = new TransportControl (surface, model);
    }


    /** {@inheritDoc} */
    @Override
    public void onButtonRow1 (final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.DOWN)
            return;

        final ModeManager modeManager = this.surface.getModeManager ();
        Modes activeModeId = modeManager.getActiveOrTempModeId ();
        if (Modes.VIEW_SELECT == activeModeId)
        {
            if (index == 1)
            {
                this.surface.getViewManager ().setActiveView (Views.PLAY);
                if (Modes.VOLUME.equals (modeManager.getPreviousModeId ()))
                    modeManager.restoreMode ();
                else
                    modeManager.setActiveMode (Modes.SESSION);
            }
            else
                modeManager.restoreMode ();
            this.surface.turnOffTransport ();
            return;
        }

        if (!Modes.FUNCTIONS.equals (activeModeId) && !Modes.FIXED.equals (activeModeId))
        {
            modeManager.setActiveMode (Modes.FUNCTIONS);
            activeModeId = Modes.FUNCTIONS;
        }

        if (Modes.FIXED.equals (activeModeId))
        {
            this.surface.getConfiguration ().setNewClipLength (index);
            return;
        }

        switch (index)
        {
            // Undo
            case 0:
                this.model.getApplication ().undo ();
                break;

            // Redo
            case 1:
                this.model.getApplication ().redo ();
                break;

            // Delete
            case 2:
                this.model.getApplication ().deleteSelection ();
                break;

            // Double
            case 3:
                this.model.getApplication ().duplicate ();
                break;

            // New
            case 4:
                final ITrack t = this.model.getSelectedTrack ();
                if (t == null)
                    return;
                final ISlotBank slotBank = t.getSlotBank ();
                final List<ISlot> slotIndexes = slotBank.getSelectedItems ();
                final int slotIndex = slotIndexes.isEmpty () ? 0 : slotIndexes.get (0).getIndex ();
                for (int i = 0; i < 8; i++)
                {
                    final int sIndex = (slotIndex + i) % 8;
                    final ISlot s = slotBank.getItem (sIndex);
                    if (!s.hasContent ())
                    {
                        final int lengthInBeats = this.surface.getConfiguration ().getNewClipLenghthInBeats (this.model.getTransport ().getQuartersPerMeasure ());
                        this.model.createNoteClip (t, s, lengthInBeats, true);
                        return;
                    }
                }
                this.surface.getDisplay ().notify ("In the current selected grid view there is no empty slot. Please scroll down.");
                break;

            // Open the VST window
            case 5:
                this.model.getCursorDevice ().toggleWindowOpen ();
                break;

            // Metronome
            case 6:
                this.model.getTransport ().toggleMetronome ();
                break;

            // Tap Tempo on MKII
            case 7:
                this.model.getTransport ().tapTempo ();
                break;

            default:
                // Not used
                break;
        }
    }


    /** {@inheritDoc} */
    @Override
    public void onButtonRow2 (final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.DOWN)
            return;

        final ModeManager modeManager = this.surface.getModeManager ();
        Modes cm = modeManager.getActiveOrTempModeId ();
        if (!Modes.TRACK_DETAILS.equals (cm) && !Modes.FRAME.equals (cm) && !Modes.BROWSER.equals (cm))
        {
            modeManager.setActiveMode (Modes.TRACK_DETAILS);
            cm = Modes.TRACK_DETAILS;
        }

        if (Modes.FRAME.equals (cm))
        {
            modeManager.getMode (Modes.FRAME).onButton (0, index, event);
            return;
        }
        else if (Modes.BROWSER.equals (cm))
        {
            modeManager.getMode (Modes.BROWSER).onButton (0, index, event);
            return;
        }

        ITrack track;
        switch (index)
        {
            // Mute
            case 0:
                track = this.model.getSelectedTrack ();
                if (track != null)
                    track.toggleMute ();
                break;

            // Solo
            case 1:
                track = this.model.getSelectedTrack ();
                if (track != null)
                    track.toggleSolo ();
                break;

            // Arm
            case 2:
                track = this.model.getSelectedTrack ();
                if (track != null)
                    track.toggleRecArm ();
                break;

            // Write
            case 3:
                this.model.getTransport ().toggleWriteArrangerAutomation ();
                break;

            // Browse
            case 4:
                this.model.getBrowser ().replace (this.model.getCursorDevice ());
                modeManager.setActiveMode (Modes.BROWSER);
                break;

            // Dis-/Enable device
            case 5:
                this.model.getCursorDevice ().toggleEnabledState ();
                break;

            // Previous device
            case 6:
                this.model.getCursorDevice ().selectPrevious ();
                break;

            // Next device
            case 7:
                this.model.getCursorDevice ().selectNext ();
                break;

            default:
                // Not used
                break;
        }
    }


    /** {@inheritDoc} */
    @Override
    public void onButtonRow3 (final int index, final ButtonEvent event)
    {
        if (!this.model.getMasterTrack ().isSelected ())
            this.selectTrack (index);
    }


    /** {@inheritDoc} */
    @Override
    public void onButtonRow4 (final int index, final ButtonEvent event)
    {
        switch (index)
        {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                this.transportControl.execute (index, event);
                break;

            case 6:
                // Decrease tempo
                if (event == ButtonEvent.DOWN)
                    this.isTempoDec = true;
                else if (event == ButtonEvent.UP)
                    this.isTempoDec = false;
                this.doChangeTempo ();
                break;

            case 7:
                // Increase tempo
                if (event == ButtonEvent.DOWN)
                    this.isTempoInc = true;
                else if (event == ButtonEvent.UP)
                    this.isTempoInc = false;
                this.doChangeTempo ();
                break;

            default:
                // Not used
                break;
        }
    }


    /** {@inheritDoc} */
    @Override
    public void onButtonRow5 (final int index, final ButtonEvent event)
    {
        this.transportControl.execute (index, event);
    }


    private void doChangeTempo ()
    {
        if (!this.isTempoInc && !this.isTempoDec)
            return;
        this.model.getTransport ().changeTempo (this.isTempoInc);
        this.surface.scheduleTask (this::doChangeTempo, 200);
    }


    /** {@inheritDoc} */
    @Override
    public void onButtonRow1Select ()
    {
        final ModeManager modeManager = this.surface.getModeManager ();
        final boolean selectFixed = Modes.FUNCTIONS.equals (modeManager.getActiveOrTempModeId ());
        modeManager.setActiveMode (selectFixed ? Modes.FIXED : Modes.FUNCTIONS);
        this.surface.getDisplay ().notify (selectFixed ? "Fixed Length" : "Functions");
    }


    /** {@inheritDoc} */
    @Override
    public void onButtonRow2Select ()
    {
        final ModeManager modeManager = this.surface.getModeManager ();
        final boolean selectFrame = Modes.TRACK_DETAILS.equals (modeManager.getActiveOrTempModeId ());
        modeManager.setActiveMode (selectFrame ? Modes.FRAME : Modes.TRACK_DETAILS);
        this.surface.getDisplay ().notify (selectFrame ? "Layouts & Panels" : "Track & Device");
    }


    /** {@inheritDoc} */
    @Override
    public void onButtonP1 (final boolean isUp, final ButtonEvent event)
    {
        if (event != ButtonEvent.DOWN)
            return;

        final ModeManager modeManager = this.surface.getModeManager ();
        final Modes activeModeId = modeManager.getActiveOrTempModeId ();
        if (Modes.FUNCTIONS.equals (activeModeId) || Modes.FIXED.equals (activeModeId))
            this.onButtonRow1Select ();
        else if (Modes.VOLUME.equals (activeModeId))
            new P2ButtonCommand (isUp, this.model, this.surface).execute (event);
        else if (Modes.TRACK.equals (activeModeId) || Modes.MASTER.equals (activeModeId))
            new ButtonRowSelectCommand<> (3, this.model, this.surface).execute (event);
        else if (Modes.TRACK_DETAILS.equals (activeModeId) || Modes.FRAME.equals (activeModeId))
            this.onButtonRow2Select ();
        else
        {
            if (isUp)
                ((DeviceParamsMode) modeManager.getMode (Modes.DEVICE_PARAMS)).nextPage ();
            else
                ((DeviceParamsMode) modeManager.getMode (Modes.DEVICE_PARAMS)).previousPage ();
        }
    }


    /** {@inheritDoc} */
    @Override
    public void updateButtons ()
    {
        final ITrackBank tb = this.model.getCurrentTrackBank ();
        final ICursorDevice cd = this.model.getCursorDevice ();
        final ITransport transport = this.model.getTransport ();
        final int clipLength = this.surface.getConfiguration ().getNewClipLength ();

        final Modes mode = this.surface.getModeManager ().getActiveOrTempModeId ();
        final boolean isTrack = Modes.TRACK.equals (mode);
        final boolean isTrackToggles = Modes.TRACK_DETAILS.equals (mode);
        final boolean isVolume = Modes.VOLUME.equals (mode);
        final boolean isMaster = Modes.MASTER.equals (mode);
        final boolean isFixed = Modes.FIXED.equals (mode);
        final boolean isFrame = Modes.FRAME.equals (mode);
        final boolean isPreset = Modes.BROWSER.equals (mode);
        final boolean isDevice = Modes.DEVICE_PARAMS.equals (mode);
        final boolean isFunctions = Modes.FUNCTIONS.equals (mode);

        // TODO Update button LEDs
        // if (Modes.VIEW_SELECT.equals (mode))
        // {
        // for (int i = 0; i < 8; i++)
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW1_1 + i,
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // }
        // else
        // {
        // // Button row 1: Clip length or functions
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW1_1, !isFunctions &&
        // clipLength == 0 ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW1_2, !isFunctions &&
        // clipLength == 1 ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW1_3, !isFunctions &&
        // clipLength == 2 ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW1_4, !isFunctions &&
        // clipLength == 3 ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW1_5, !isFunctions &&
        // clipLength == 4 ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW1_6, isFunctions &&
        // this.model.getCursorDevice ().isWindowOpen () || !isFunctions && clipLength == 5 ?
        // SLControlSurface.MKII_BUTTON_STATE_ON : SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW1_7, isFunctions &&
        // transport.isMetronomeOn () || !isFunctions && clipLength == 6 ?
        // SLControlSurface.MKII_BUTTON_STATE_ON : SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW1_8, !isFunctions &&
        // clipLength == 7 ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // }
        //
        // // Button row 2: Track toggles / Browse
        // if (Modes.BROWSER.equals (mode))
        // {
        // final int selMode = ((DevicePresetsMode) this.surface.getModeManager ().getMode
        // (Modes.BROWSER)).getSelectionMode ();
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW2_1,
        // SLControlSurface.MKII_BUTTON_STATE_ON);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW2_2,
        // SLControlSurface.MKII_BUTTON_STATE_ON);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW2_3, selMode ==
        // DevicePresetsMode.SELECTION_OFF ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW2_4, selMode ==
        // DevicePresetsMode.SELECTION_OFF ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW2_5, selMode ==
        // DevicePresetsMode.SELECTION_OFF ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW2_6, selMode ==
        // DevicePresetsMode.SELECTION_OFF ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW2_7, selMode ==
        // DevicePresetsMode.SELECTION_OFF ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW2_8,
        // SLControlSurface.MKII_BUTTON_STATE_ON);
        // }
        // else
        // {
        // final boolean isNoOverlayMode = !Modes.FRAME.equals (mode) && !Modes.BROWSER.equals
        // (mode);
        // final ITrack track = tb.getSelectedItem ();
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW2_1, isNoOverlayMode && track
        // != null && track.isMute () ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW2_2, isNoOverlayMode && track
        // != null && track.isSolo () ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW2_3, isNoOverlayMode && track
        // != null && track.isRecArm () ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW2_4,
        // transport.isWritingArrangerAutomation () ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW2_5,
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW2_6,
        // this.model.getCursorDevice ().isEnabled () ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW2_7, isNoOverlayMode &&
        // cd.canSelectPreviousFX () ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW2_8, isNoOverlayMode &&
        // cd.canSelectNextFX () ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // }
        //
        // // Button row 3: Selected track indication
        // for (int i = 0; i < 8; i++)
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW3_1 + i, tb.getItem
        // (i).isSelected () ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        //
        // // LED indications for device parameters
        // ((DeviceParamsMode) this.surface.getModeManager ().getMode (Modes.DEVICE_PARAMS)).setLEDs
        // ();
        //
        // // Transport buttons
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW4_3, !transport.isPlaying ()
        // ? SLControlSurface.MKII_BUTTON_STATE_ON : SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW4_4, transport.isPlaying () ?
        // SLControlSurface.MKII_BUTTON_STATE_ON : SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW4_5, transport.isLoop () ?
        // SLControlSurface.MKII_BUTTON_STATE_ON : SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROW4_6, transport.isRecording ()
        // ? SLControlSurface.MKII_BUTTON_STATE_ON : SLControlSurface.MKII_BUTTON_STATE_OFF);
        //
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROWSEL1, isFunctions || isFixed
        // ? SLControlSurface.MKII_BUTTON_STATE_ON : SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROWSEL2, isDevice ?
        // SLControlSurface.MKII_BUTTON_STATE_ON : SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROWSEL3, isTrackToggles ||
        // isFrame || isPreset ? SLControlSurface.MKII_BUTTON_STATE_ON :
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROWSEL4, isTrack || isMaster ?
        // SLControlSurface.MKII_BUTTON_STATE_ON : SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROWSEL6, isVolume ?
        // SLControlSurface.MKII_BUTTON_STATE_ON : SLControlSurface.MKII_BUTTON_STATE_OFF);
        // this.surface.updateTrigger (SLControlSurface.MKII_BUTTON_ROWSEL7,
        // SLControlSurface.MKII_BUTTON_STATE_OFF);
    }


    /** {@inheritDoc} */
    @Override
    public void onGridNote (final int note, final int velocity)
    {
        // Use drum pads for mode selection to support Remote Zero MkII
        if (this.surface.getConfiguration ().isDrumpadsAsModeSelection ())
        {
            if (velocity > 0)
            {
                final int index = note - 36;
                new ButtonRowSelectCommand<> (index > 3 ? 5 : index, this.model, this.surface).execute (ButtonEvent.DOWN);
            }
            return;
        }

        this.surface.sendMidiEvent (0x90, note, velocity);
    }
}