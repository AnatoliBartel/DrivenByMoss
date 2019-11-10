// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.apc.view;

import de.mossgrabers.controller.apc.APCConfiguration;
import de.mossgrabers.controller.apc.command.trigger.APCBrowserCommand;
import de.mossgrabers.controller.apc.controller.APCColors;
import de.mossgrabers.controller.apc.controller.APCControlSurface;
import de.mossgrabers.framework.command.TriggerCommandID;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.grid.PadGrid;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.AbstractView;
import de.mossgrabers.framework.view.SceneView;
import de.mossgrabers.framework.view.ViewManager;
import de.mossgrabers.framework.view.Views;


/**
 * Provides additional features/functions on the grid.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class ShiftView extends AbstractView<APCControlSurface, APCConfiguration> implements SceneView
{
    private static final Views [] VIEW_IDS  =
    {
        Views.SESSION,
        Views.PLAY,
        Views.DRUM,
        Views.SEQUENCER,
        Views.RAINDROPS
    };

    private static final int []   TRANSLATE =
    {
        0,
        2,
        4,
        6,
        1,
        3,
        5,
        -1,
        -1,
        10,
        8,
        -1,
        11,
        9,
        7,
        -1
    };


    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public ShiftView (final APCControlSurface surface, final IModel model)
    {
        super ("Shift", surface, model);
    }


    /** {@inheritDoc} */
    @Override
    public void drawGrid ()
    {
        final PadGrid padGrid = this.surface.getPadGrid ();
        for (int i = 7; i < 64; i++)
            padGrid.light (36 + i, APCColors.APC_COLOR_BLACK);

        // Add tracks
        for (int i = 0; i < 3; i++)
            padGrid.light (36 + 32 + i, APCColors.COLOR_KEY_WHITE);
        padGrid.light (36 + 35, APCColors.COLOR_KEY_SELECTED);
        padGrid.light (36 + 36, APCColors.COLOR_KEY_SELECTED);
        padGrid.light (36 + 38, APCColors.COLOR_KEY_BLACK);
        padGrid.light (36 + 39, APCColors.COLOR_KEY_BLACK);

        // Draw the keyboard
        final int scaleOffset = this.model.getScales ().getScaleOffset ();
        // 0'C', 1'G', 2'D', 3'A', 4'E', 5'B', 6'F', 7'Bb', 8'Eb', 9'Ab', 10'Db', 11'Gb'
        padGrid.light (36, scaleOffset == 0 ? APCColors.COLOR_KEY_SELECTED : APCColors.COLOR_KEY_WHITE);
        padGrid.light (36 + 1, scaleOffset == 2 ? APCColors.COLOR_KEY_SELECTED : APCColors.COLOR_KEY_WHITE);
        padGrid.light (36 + 2, scaleOffset == 4 ? APCColors.COLOR_KEY_SELECTED : APCColors.COLOR_KEY_WHITE);
        padGrid.light (36 + 3, scaleOffset == 6 ? APCColors.COLOR_KEY_SELECTED : APCColors.COLOR_KEY_WHITE);
        padGrid.light (36 + 4, scaleOffset == 1 ? APCColors.COLOR_KEY_SELECTED : APCColors.COLOR_KEY_WHITE);
        padGrid.light (36 + 5, scaleOffset == 3 ? APCColors.COLOR_KEY_SELECTED : APCColors.COLOR_KEY_WHITE);
        padGrid.light (36 + 6, scaleOffset == 5 ? APCColors.COLOR_KEY_SELECTED : APCColors.COLOR_KEY_WHITE);
        padGrid.light (36 + 9, scaleOffset == 10 ? APCColors.COLOR_KEY_SELECTED : APCColors.COLOR_KEY_BLACK);
        padGrid.light (36 + 10, scaleOffset == 8 ? APCColors.COLOR_KEY_SELECTED : APCColors.COLOR_KEY_BLACK);
        padGrid.light (36 + 12, scaleOffset == 11 ? APCColors.COLOR_KEY_SELECTED : APCColors.COLOR_KEY_BLACK);
        padGrid.light (36 + 13, scaleOffset == 9 ? APCColors.COLOR_KEY_SELECTED : APCColors.COLOR_KEY_BLACK);
        padGrid.light (36 + 14, scaleOffset == 7 ? APCColors.COLOR_KEY_SELECTED : APCColors.COLOR_KEY_BLACK);
    }


    /** {@inheritDoc} */
    @Override
    public void onGridNote (final int note, final int velocity)
    {
        if (velocity == 0)
            return;

        final int index = note - 36;
        if (index > 15)
        {
            switch (index)
            {
                case 32:
                    this.model.getApplication ().addInstrumentTrack ();
                    break;
                case 33:
                    this.model.getApplication ().addAudioTrack ();
                    break;
                case 34:
                    this.model.getApplication ().addEffectTrack ();
                    break;
                case 35:
                case 36:
                    final APCBrowserCommand browseCommand = (APCBrowserCommand) this.surface.getViewManager ().getActiveView ().getTriggerCommand (TriggerCommandID.BROWSE);
                    browseCommand.startBrowser (true, index == 35);
                    break;
                case 38:
                    this.model.getApplication ().undo ();
                    break;
                case 39:
                    this.model.getApplication ().redo ();
                    break;
                default:
                    // Not used
                    break;
            }
            return;
        }

        // Scale Base note selection
        final int pos = TRANSLATE[index];
        if (pos == -1)
            return;
        this.model.getScales ().setScaleOffset (pos);
        this.surface.getConfiguration ().setScaleBase (Scales.BASES[pos]);
        this.surface.getDisplay ().notify (Scales.BASES[pos]);
        this.surface.getViewManager ().getActiveView ().updateNoteMapping ();
    }


    /** {@inheritDoc} */
    @Override
    public void onScene (final int scene, final ButtonEvent event)
    {
        if (event != ButtonEvent.DOWN)
            return;

        final ViewManager viewManager = this.surface.getViewManager ();
        viewManager.setPreviousView (VIEW_IDS[scene]);
        this.surface.getDisplay ().notify (viewManager.getView (VIEW_IDS[scene]).getName ());

        if (Views.SESSION.equals (VIEW_IDS[scene]))
            return;

        final ITrack selectedTrack = this.model.getSelectedTrack ();
        if (selectedTrack != null)
            viewManager.setPreferredView (selectedTrack.getPosition (), VIEW_IDS[scene]);
    }


    /** {@inheritDoc} */
    @Override
    public void updateSceneButton (final int scene)
    {
        // TODO

        final Views previousViewId = this.surface.getViewManager ().getPreviousViewId ();
        this.surface.updateTrigger (APCControlSurface.APC_BUTTON_SCENE_LAUNCH_1, Views.SESSION.equals (previousViewId) ? APCColors.COLOR_VIEW_SELECTED : APCColors.COLOR_VIEW_UNSELECTED);
        this.surface.updateTrigger (APCControlSurface.APC_BUTTON_SCENE_LAUNCH_2, Views.PLAY.equals (previousViewId) ? APCColors.COLOR_VIEW_SELECTED : APCColors.COLOR_VIEW_UNSELECTED);
        this.surface.updateTrigger (APCControlSurface.APC_BUTTON_SCENE_LAUNCH_3, Views.DRUM.equals (previousViewId) ? APCColors.COLOR_VIEW_SELECTED : APCColors.COLOR_VIEW_UNSELECTED);
        this.surface.updateTrigger (APCControlSurface.APC_BUTTON_SCENE_LAUNCH_4, Views.SEQUENCER.equals (previousViewId) ? APCColors.COLOR_VIEW_SELECTED : APCColors.COLOR_VIEW_UNSELECTED);
        this.surface.updateTrigger (APCControlSurface.APC_BUTTON_SCENE_LAUNCH_5, Views.RAINDROPS.equals (previousViewId) ? APCColors.COLOR_VIEW_SELECTED : APCColors.COLOR_VIEW_UNSELECTED);
    }


    @Override
    public String getSceneButtonColor (final int scene)
    {
        // TODO Auto-generated method stub
        return ColorManager.BUTTON_STATE_OFF;
    }
}