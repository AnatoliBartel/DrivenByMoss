// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.daw.data.empty;

import de.mossgrabers.framework.daw.EmptyBank;
import de.mossgrabers.framework.daw.ISlotBank;
import de.mossgrabers.framework.daw.constants.RecordQuantization;
import de.mossgrabers.framework.daw.data.ISlot;
import de.mossgrabers.framework.daw.data.ITrack;


/**
 * Default data for an empty track.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class EmptyTrack extends EmptyChannel implements ITrack
{
    /** The singleton. */
    public static final ITrack INSTANCE = new EmptyTrack ();

    private final ISlotBank    slotBank = new EmptySlotBank ();


    /**
     * Constructor.
     */
    private EmptyTrack ()
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public boolean isGroup ()
    {
        return false;
    }


    /** {@inheritDoc} */
    @Override
    public boolean isRecArm ()
    {
        return false;
    }


    /** {@inheritDoc} */
    @Override
    public boolean isMonitor ()
    {
        return false;
    }


    /** {@inheritDoc} */
    @Override
    public boolean isAutoMonitor ()
    {
        return false;
    }


    /** {@inheritDoc} */
    @Override
    public boolean canHoldNotes ()
    {
        return false;
    }


    /** {@inheritDoc} */
    @Override
    public boolean canHoldAudioData ()
    {
        return false;
    }


    /** {@inheritDoc} */
    @Override
    public String getCrossfadeMode ()
    {
        return "AB";
    }


    /** {@inheritDoc} */
    @Override
    public boolean isPlaying ()
    {
        return false;
    }


    /** {@inheritDoc} */
    @Override
    public void setRecArm (final boolean value)
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public void toggleRecArm ()
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public void setMonitor (final boolean value)
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public void toggleMonitor ()
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public void setAutoMonitor (final boolean value)
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public void toggleAutoMonitor ()
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public void changeCrossfadeModeAsNumber (final int control)
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public void setCrossfadeMode (final String mode)
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public int getCrossfadeModeAsNumber ()
    {
        // Intentionally empty
        return 0;
    }


    /** {@inheritDoc} */
    @Override
    public void setCrossfadeModeAsNumber (final int modeValue)
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public void toggleCrossfadeMode ()
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public void stop ()
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public void returnToArrangement ()
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public ISlotBank getSlotBank ()
    {
        // Intentionally empty
        return this.slotBank;
    }


    /** {@inheritDoc} */
    @Override
    public void createClip (final int slotIndex, final int lengthInBeats)
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public boolean isRecordQuantizationNoteLength ()
    {
        return false;
    }


    /** {@inheritDoc} */
    @Override
    public void toggleRecordQuantizationNoteLength ()
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public RecordQuantization getRecordQuantizationGrid ()
    {
        return RecordQuantization.RES_OFF;
    }


    /** {@inheritDoc} */
    @Override
    public void setRecordQuantizationGrid (RecordQuantization recordQuantization)
    {
        // Intentionally empty
    }

    class EmptySlotBank extends EmptyBank<ISlot> implements ISlotBank
    {
        /** {@inheritDoc} */
        @Override
        public ISlot getEmptySlot (final int startFrom)
        {
            return null;
        }


        /** {@inheritDoc} */
        @Override
        public ISlot getItem (final int index)
        {
            return EmptySlot.INSTANCE;
        }
    }
}
