// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.push.view;

import de.mossgrabers.controller.push.PushConfiguration;
import de.mossgrabers.controller.push.controller.PushControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.color.ColorEx;
import de.mossgrabers.framework.controller.grid.PadGrid;
import de.mossgrabers.framework.daw.DAWColors;
import de.mossgrabers.framework.daw.IClip;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.IMasterTrack;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.mode.AbstractMode;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.AbstractSequencerView;
import de.mossgrabers.framework.view.AbstractView;
import de.mossgrabers.framework.view.SceneView;


/**
 * The Color view.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class ColorView extends AbstractView<PushControlSurface, PushConfiguration> implements SceneView
{
    /** What should the color be selected for? */
    public enum SelectMode
    {
        /** Select a track color. */
        MODE_TRACK,
        /** Select a layer color. */
        MODE_LAYER,
        /** Select a clip color. */
        MODE_CLIP
    }


    private SelectMode mode;


    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public ColorView (final PushControlSurface surface, final IModel model)
    {
        super ("Color", surface, model);
        this.mode = SelectMode.MODE_TRACK;
    }


    /**
     * Set the color selections mode.
     *
     * @param mode The selection mode
     */
    public void setMode (final SelectMode mode)
    {
        this.mode = mode;
    }


    /** {@inheritDoc} */
    @Override
    public void drawGrid ()
    {
        final PadGrid padGrid = this.surface.getPadGrid ();
        final DAWColors [] dawColors = DAWColors.values ();
        for (int i = 0; i < 64; i++)
            padGrid.light (36 + i, i < dawColors.length ? dawColors[i].name () : PadGrid.GRID_OFF);
    }


    /** {@inheritDoc} */
    @Override
    public void onGridNote (final int note, final int velocity)
    {
        if (velocity == 0)
            return;

        final int color = note - 36;
        final DAWColors [] dawColors = DAWColors.values ();
        if (color < dawColors.length)
        {
            final ColorEx entry = dawColors[color].getColor ();
            switch (this.mode)
            {
                case MODE_TRACK:
                    final ITrack t = this.model.getSelectedTrack ();
                    if (t == null)
                    {
                        final IMasterTrack master = this.model.getMasterTrack ();
                        if (master.isSelected ())
                            master.setColor (entry);
                    }
                    else
                        t.setColor (entry);
                    break;

                case MODE_LAYER:
                    this.model.getCursorDevice ().getLayerOrDrumPadBank ().getSelectedItem ().setColor (entry);
                    break;

                case MODE_CLIP:
                    final IClip clip = this.model.getClip ();
                    if (clip.doesExist ())
                        clip.setColor (entry);
                    break;
            }
        }
        this.surface.getViewManager ().restoreView ();
    }


    /** {@inheritDoc} */
    @Override
    public void onScene (final int scene, final ButtonEvent event)
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public String getButtonColorID (final ButtonID buttonID)
    {
        final int scene = buttonID.ordinal () - ButtonID.SCENE1.ordinal ();
        if (scene < 0 || scene >= 8)
            return AbstractMode.BUTTON_COLOR_OFF;

        return AbstractSequencerView.COLOR_RESOLUTION_OFF;
    }
}