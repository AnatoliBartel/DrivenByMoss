// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.kontrol.mkii.mode;

import de.mossgrabers.controller.kontrol.mkii.KontrolProtocolConfiguration;
import de.mossgrabers.controller.kontrol.mkii.TrackType;
import de.mossgrabers.controller.kontrol.mkii.controller.KontrolProtocolControlSurface;
import de.mossgrabers.framework.controller.IValueChanger;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.ISendBank;
import de.mossgrabers.framework.daw.data.ISend;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.empty.EmptySend;
import de.mossgrabers.framework.mode.track.AbstractTrackMode;


/**
 * The send mode.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class SendMode extends AbstractTrackMode<KontrolProtocolControlSurface, KontrolProtocolConfiguration>
{
    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model The model
     */
    public SendMode (final KontrolProtocolControlSurface surface, final IModel model)
    {
        super ("Send", surface, model, false);
    }


    /** {@inheritDoc} */
    @Override
    public void onKnobValue (final int index, final int value)
    {
        final ITrack track = this.model.getCurrentTrackBank ().getSelectedItem ();
        if (track == null)
            return;
        final ISend send = track.getSendBank ().getItem (index);
        if (this.isAbsolute)
            send.setValue (value);
        else
            send.changeValue (value);
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay ()
    {
        final IValueChanger valueChanger = this.model.getValueChanger ();
        final ITrack selectedTrack = this.getBank ().getSelectedItem ();
        final ISendBank sendBank = selectedTrack == null ? null : selectedTrack.getSendBank ();

        final int [] vuData = new int [16];
        for (int i = 0; i < 8; i++)
        {
            final ISend send = sendBank == null ? EmptySend.INSTANCE : sendBank.getItem (i);

            this.surface.sendKontrolTrackSysEx (KontrolProtocolControlSurface.KONTROL_TRACK_AVAILABLE, send.doesExist () ? TrackType.RETURN_BUS : TrackType.EMPTY, i);
            this.surface.sendKontrolTrackSysEx (KontrolProtocolControlSurface.KONTROL_TRACK_SELECTED, send.isSelected () ? 1 : 0, i);
            this.surface.sendKontrolTrackSysEx (KontrolProtocolControlSurface.KONTROL_TRACK_RECARM, 0, i);
            this.surface.sendKontrolTrackSysEx (KontrolProtocolControlSurface.KONTROL_TRACK_VOLUME_TEXT, 0, i, send.getDisplayedValue (8));
            this.surface.sendKontrolTrackSysEx (KontrolProtocolControlSurface.KONTROL_TRACK_PAN_TEXT, 0, i, send.getDisplayedValue (8));
            this.surface.sendKontrolTrackSysEx (KontrolProtocolControlSurface.KONTROL_TRACK_NAME, 0, i, getName (selectedTrack, send));

            final int j = 2 * i;
            vuData[j] = valueChanger.toMidiValue (send.getModulatedValue ());
            vuData[j + 1] = valueChanger.toMidiValue (send.getModulatedValue ());

            this.surface.updateContinuous (KontrolProtocolControlSurface.KONTROL_TRACK_VOLUME + i, valueChanger.toMidiValue (send.getValue ()));
            this.surface.updateContinuous (KontrolProtocolControlSurface.KONTROL_TRACK_PAN + i, valueChanger.toMidiValue (send.getValue ()));
        }
        this.surface.sendKontrolTrackSysEx (KontrolProtocolControlSurface.KONTROL_TRACK_VU, 2, 0, vuData);

        final int scrollTracksState = (sendBank != null && sendBank.canScrollPageBackwards () ? 1 : 0) + (sendBank != null && sendBank.canScrollPageForwards () ? 2 : 0);
        final int scrollScenesState = 0;

        final KontrolProtocolConfiguration configuration = this.surface.getConfiguration ();
        this.surface.updateContinuous (KontrolProtocolControlSurface.KONTROL_NAVIGATE_BANKS, scrollTracksState);
        this.surface.updateContinuous (KontrolProtocolControlSurface.KONTROL_NAVIGATE_TRACKS, configuration.isFlipTrackClipNavigation () ? scrollScenesState : scrollTracksState);
        this.surface.updateContinuous (KontrolProtocolControlSurface.KONTROL_NAVIGATE_CLIPS, configuration.isFlipTrackClipNavigation () ? scrollTracksState : scrollScenesState);
        this.surface.updateContinuous (KontrolProtocolControlSurface.KONTROL_NAVIGATE_SCENES, configuration.isFlipTrackClipNavigation () ? scrollTracksState : scrollScenesState);
    }


    /** {@inheritDoc} */
    @Override
    public void selectPreviousItem ()
    {
        final ITrack selectedTrack = this.getBank ().getSelectedItem ();
        if (selectedTrack != null)
            selectedTrack.getSendBank ().selectPreviousPage ();
    }


    /** {@inheritDoc} */
    @Override
    public void selectNextItem ()
    {
        final ITrack selectedTrack = this.getBank ().getSelectedItem ();
        if (selectedTrack != null)
            selectedTrack.getSendBank ().selectNextPage ();
    }


    /** {@inheritDoc} */
    @Override
    public void selectPreviousItemPage ()
    {
        this.selectPreviousItem ();
    }


    /** {@inheritDoc} */
    @Override
    public void selectNextItemPage ()
    {
        this.selectNextItem ();
    }


    private static String getName (final ITrack track, final ISend send)
    {
        if (track == null)
            return "";
        return "Track " + (track.getPosition () + 1) + "\nFX " + (send.getPosition () + 1) + "\n\n" + send.getName ();
    }
}