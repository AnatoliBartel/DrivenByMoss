// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.controller;

import de.mossgrabers.framework.command.core.ContinuousCommand;
import de.mossgrabers.framework.command.core.PitchbendCommand;
import de.mossgrabers.framework.command.core.TriggerCommand;
import de.mossgrabers.framework.configuration.AbstractConfiguration;
import de.mossgrabers.framework.configuration.Configuration;
import de.mossgrabers.framework.configuration.ISettingsUI;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.hardware.BindType;
import de.mossgrabers.framework.controller.hardware.IHwAbsoluteKnob;
import de.mossgrabers.framework.controller.hardware.IHwButton;
import de.mossgrabers.framework.controller.hardware.IHwFader;
import de.mossgrabers.framework.controller.hardware.IHwRelativeKnob;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.controller.valuechanger.RelativeEncoding;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.mode.Mode;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;


/**
 * Abstract base class for controller extensions.
 *
 * @param <C> The type of the configuration
 * @param <S> The type of the control surface
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public abstract class AbstractControllerSetup<S extends IControlSurface<C>, C extends Configuration> implements IControllerSetup<S, C>
{
    protected final List<S>       surfaces    = new ArrayList<> ();
    protected final IHost         host;
    protected final ISettingsUI   globalSettings;
    protected final ISettingsUI   documentSettings;
    protected final ISetupFactory factory;

    protected Scales              scales;
    protected IModel              model;
    protected C                   configuration;
    protected ColorManager        colorManager;
    protected IValueChanger       valueChanger;
    protected Modes               currentMode = null;


    /**
     * Constructor.
     *
     * @param factory The factory
     * @param host The host
     * @param globalSettings The global settings
     * @param documentSettings The document (project) specific settings
     */
    protected AbstractControllerSetup (final ISetupFactory factory, final IHost host, final ISettingsUI globalSettings, final ISettingsUI documentSettings)
    {
        this.factory = factory;
        this.host = host;
        this.globalSettings = globalSettings;
        this.documentSettings = documentSettings;
    }


    /** {@inheritDoc} */
    @Override
    public S getSurface ()
    {
        return this.surfaces.get (0);
    }


    /** {@inheritDoc} */
    @Override
    public S getSurface (final int index)
    {
        return this.surfaces.get (index);
    }


    /** {@inheritDoc} */
    @Override
    public IModel getModel ()
    {
        return this.model;
    }


    /** {@inheritDoc} */
    @Override
    public C getConfiguration ()
    {
        return this.configuration;
    }


    /** {@inheritDoc} */
    @Override
    public void init ()
    {
        this.initConfiguration ();
        this.createScales ();
        this.createModel ();
        this.createSurface ();
        this.createModes ();
        this.createObservers ();
        this.createViews ();
        this.registerTriggerCommands ();
        this.registerContinuousCommands ();
        this.layoutControls ();
        if (this.model != null)
            this.model.ensureClip ();
    }


    /** {@inheritDoc} */
    @Override
    public void exit ()
    {
        this.configuration.clearSettingObservers ();
        for (final S surface: this.surfaces)
            surface.shutdown ();
        this.host.println ("Exited.");
    }


    /** {@inheritDoc} */
    @Override
    public void flush ()
    {
        for (final S surface: this.surfaces)
            surface.flush ();
    }


    /**
     * Initialize the configuration settings.
     */
    protected void initConfiguration ()
    {
        this.configuration.init (this.globalSettings, this.documentSettings);
    }


    /**
     * Create the scales object.
     */
    protected void createScales ()
    {
        this.scales = new Scales (this.valueChanger, 36, 100, 8, 8);
    }


    /**
     * Create the model.
     */
    protected abstract void createModel ();


    /**
     * Create the surface.
     */
    protected abstract void createSurface ();


    /**
     * Create the modes.
     */
    protected void createModes ()
    {
        // Intentionally empty
    }


    /**
     * Create the views.
     */
    protected void createViews ()
    {
        // Intentionally empty
    }


    /**
     * Create the listeners.
     */
    protected void createObservers ()
    {
        // Intentionally empty
    }


    /**
     * Create and register the trigger commands.
     */
    protected void registerTriggerCommands ()
    {
        // Intentionally empty
    }


    /**
     * Create and register the continuous commands.
     */
    protected void registerContinuousCommands ()
    {
        // Intentionally empty
    }


    /**
     * Layout the controls on the virtual GUI.
     */
    protected void layoutControls ()
    {
        // Intentionally empty
    }


    /**
     * Create a hardware button on/off proxy on controller device 1, bind a trigger command to it
     * and bind it to a MIDI CC on MIDI channel 1. State colors are ON and HI.
     *
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param command The command to bind
     * @param midiControl The MIDI CC or note
     * @param supplier Callback for retrieving the on/off state of the light
     */
    protected void addButton (final ButtonID buttonID, final String label, final TriggerCommand command, final int midiControl, final BooleanSupplier supplier)
    {
        this.addButton (buttonID, label, command, 0, midiControl, supplier);
    }


    /**
     * Create a hardware button on/off proxy on controller device 1, bind a trigger command to it
     * and bind it to a MIDI CC on MIDI channel 1. State colors are ON and HI.
     *
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param command The command to bind
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @param supplier Callback for retrieving the on/off state of the light
     */
    protected void addButton (final ButtonID buttonID, final String label, final TriggerCommand command, final int midiChannel, final int midiControl, final BooleanSupplier supplier)
    {
        this.addButton (buttonID, label, command, midiChannel, midiControl, () -> supplier.getAsBoolean () ? 1 : 0, ColorManager.BUTTON_STATE_ON, ColorManager.BUTTON_STATE_HI);
    }


    /**
     * Create a hardware button proxy on controller device 1, bind a trigger command to it and bind
     * it to a MIDI CC on MIDI channel 1. The button has an on/off state.
     *
     * @param surface The control surface
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param command The command to bind
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @param supplier Callback for retrieving the on/off state of the light
     */
    protected void addButton (final S surface, final ButtonID buttonID, final String label, final TriggerCommand command, final int midiChannel, final int midiControl, final BooleanSupplier supplier)
    {
        this.addButton (surface, buttonID, label, command, midiChannel, midiControl, () -> supplier.getAsBoolean () ? 1 : 0, ColorManager.BUTTON_STATE_ON, ColorManager.BUTTON_STATE_HI);
    }


    /**
     * Create a hardware button on/off proxy on controller device 1, bind a trigger command to it
     * and bind it to a MIDI CC on MIDI channel 1.
     *
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param supplier Callback for retrieving the state of the light
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     * @param colorIdOn The color ID for on state
     * @param colorIdHi The color ID for off state
     */
    protected void addButton (final ButtonID buttonID, final String label, final TriggerCommand command, final int midiControl, final BooleanSupplier supplier, final String colorIdOn, final String colorIdHi)
    {
        this.addButton (buttonID, label, command, midiControl, () -> supplier.getAsBoolean () ? 1 : 0, colorIdOn, colorIdHi);
    }


    /**
     * Create a hardware button proxy on controller device 1, bind a trigger command to it and bind
     * it to a MIDI CC on MIDI channel 1. The button has an on/off state.
     *
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     */
    protected void addButton (final ButtonID buttonID, final String label, final TriggerCommand command, final int midiControl)
    {
        this.addButton (buttonID, label, command, 0, midiControl);
    }


    /**
     * Create a hardware button proxy on controller device 1, bind a trigger command to it and bind
     * it to a MIDI CC on MIDI channel 1. The button has an on/off state.
     *
     * @param surface The control surface
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     */
    protected void addButton (final S surface, final ButtonID buttonID, final String label, final TriggerCommand command, final int midiControl)
    {
        this.addButton (surface, buttonID, label, command, 0, midiControl, (IntSupplier) null, ColorManager.BUTTON_STATE_ON, ColorManager.BUTTON_STATE_HI);
    }


    /**
     * Create a hardware button proxy on controller device 1, bind a trigger command to it and bind
     * it to a MIDI CC on MIDI channel 1. The button has an on/off state.
     *
     * @param surface The control surface
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     */
    protected void addButton (final S surface, final ButtonID buttonID, final String label, final TriggerCommand command, final int midiChannel, final int midiControl)
    {
        this.addButton (surface, buttonID, label, command, midiChannel, midiControl, (IntSupplier) null, ColorManager.BUTTON_STATE_ON, ColorManager.BUTTON_STATE_HI);
    }


    /**
     * Create a hardware button proxy on controller device 1, bind a trigger command to it and bind
     * it to a MIDI CC. The button has an on/off state.
     *
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     */
    protected void addButton (final ButtonID buttonID, final String label, final TriggerCommand command, final int midiChannel, final int midiControl)
    {
        this.addButton (buttonID, label, command, midiChannel, midiControl, (IntSupplier) null, ColorManager.BUTTON_STATE_ON, ColorManager.BUTTON_STATE_HI);
    }


    /**
     * Create a hardware button proxy on controller device 1, bind a trigger command to it and bind
     * it to a MIDI CC on MIDI channel 1.
     *
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param supplier Callback for retrieving the state of the light
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     * @param colorIds The color IDs to map to the states
     */
    protected void addButton (final ButtonID buttonID, final String label, final TriggerCommand command, final int midiControl, final IntSupplier supplier, final String... colorIds)
    {
        this.addButton (0, buttonID, label, command, midiControl, supplier, colorIds);
    }


    /**
     * Create a hardware button proxy on controller device 1, bind a trigger command to it and bind
     * it to a MIDI CC on MIDI channel 1.
     *
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param supplier Callback for retrieving the state of the light
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     * @param colorIds The color IDs to map to the states
     */
    protected void addButton (final ButtonID buttonID, final String label, final TriggerCommand command, final int midiChannel, final int midiControl, final IntSupplier supplier, final String... colorIds)
    {
        this.addButton (0, buttonID, label, command, midiChannel, midiControl, supplier, colorIds);
    }


    /**
     * Create a hardware button proxy, bind a trigger command to it and bind it to a MIDI CC.
     *
     * @param deviceIndex The index of the device
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param supplier Callback for retrieving the state of the light
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     * @param colorIds The color IDs to map to the states
     */
    protected void addButton (final int deviceIndex, final ButtonID buttonID, final String label, final TriggerCommand command, final int midiChannel, final int midiControl, final IntSupplier supplier, final String... colorIds)
    {
        this.addButton (this.surfaces.get (deviceIndex), buttonID, label, command, midiChannel, midiControl, supplier, colorIds);
    }


    /**
     * Create a hardware button proxy on controller device 1, bind a trigger command to it and bind
     * it to a MIDI CC on MIDI channel 1.
     *
     * @param deviceIndex The index of the device
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param supplier Callback for retrieving the state of the light
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     * @param colorIds The color IDs to map to the states
     */
    protected void addButton (final int deviceIndex, final ButtonID buttonID, final String label, final TriggerCommand command, final int midiControl, final IntSupplier supplier, final String... colorIds)
    {
        this.addButton (this.surfaces.get (deviceIndex), buttonID, label, command, midiControl, supplier, colorIds);
    }


    /**
     * Create a hardware button proxy on controller device 1, bind a trigger command to it and bind
     * it to a MIDI CC on MIDI channel 1.
     *
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param supplier Callback for retrieving the state of the light
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     */
    protected void addButton (final ButtonID buttonID, final String label, final TriggerCommand command, final int midiControl, final IntSupplier supplier)
    {
        this.addButton (0, buttonID, label, command, midiControl, supplier);
    }


    /**
     * Create a hardware button proxy on controller device 1, bind a trigger command to it and bind
     * it to a MIDI CC on MIDI channel 1.
     *
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param supplier Callback for retrieving the state of the light
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     */
    protected void addButton (final ButtonID buttonID, final String label, final TriggerCommand command, final int midiChannel, final int midiControl, final IntSupplier supplier)
    {
        this.addButton (0, buttonID, label, command, midiChannel, midiControl, supplier);
    }


    /**
     * Create a hardware button proxy on controller device 1, bind a trigger command to it and bind
     * it to a MIDI CC on MIDI channel 1.
     *
     * @param deviceIndex The index of the device
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param supplier Callback for retrieving the state of the light
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     */
    protected void addButton (final int deviceIndex, final ButtonID buttonID, final String label, final TriggerCommand command, final int midiControl, final IntSupplier supplier)
    {
        this.addButton (deviceIndex, buttonID, label, command, 0, midiControl, supplier);
    }


    /**
     * Create a hardware button proxy on controller device 1, bind a trigger command to it and bind
     * it to a MIDI CC on MIDI channel 1.
     *
     * @param deviceIndex The index of the device
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param supplier Callback for retrieving the state of the light
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     */
    protected void addButton (final int deviceIndex, final ButtonID buttonID, final String label, final TriggerCommand command, final int midiChannel, final int midiControl, final IntSupplier supplier)
    {
        this.addButton (this.surfaces.get (deviceIndex), buttonID, label, command, midiChannel, midiControl, supplier);
    }


    /**
     * Create a hardware button proxy on controller device 1, bind a trigger command to it and bind
     * it to a MIDI CC on MIDI channel 1.
     *
     * @param surface The control surface
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param supplier Callback for retrieving the state of the light
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     * @param colorIds The color IDs to map to the states
     */
    protected void addButton (final S surface, final ButtonID buttonID, final String label, final TriggerCommand command, final int midiControl, final IntSupplier supplier, final String... colorIds)
    {
        this.addButton (surface, buttonID, label, command, 0, midiControl, supplier, colorIds);
    }


    /**
     * Create a hardware button proxy on controller device 1, bind a trigger command to it and bind
     * it to a MIDI CC on MIDI channel 1.
     *
     * @param surface The control surface
     * @param buttonID The ID of the button (for later access)
     * @param label The label of the button
     * @param supplier Callback for retrieving the state of the light
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     * @param colorIds The color IDs to map to the states
     */
    protected void addButton (final S surface, final ButtonID buttonID, final String label, final TriggerCommand command, final int midiChannel, final int midiControl, final IntSupplier supplier, final String... colorIds)
    {
        final IHwButton button = surface.createButton (buttonID, label);
        button.bind (command);
        if (midiControl < 0)
            return;
        button.bind (surface.getMidiInput (), this.getTriggerBindType (buttonID), midiChannel, midiControl);
        final IntSupplier intSupplier = () -> button.isPressed () ? 1 : 0;
        final IntSupplier supp = supplier == null ? intSupplier : supplier;
        this.addLight (surface, null, buttonID, button, midiChannel, midiControl, supp, colorIds);
    }


    /**
     * Creates a light.
     *
     * @param surface The control surface
     * @param outputID The ID of the light (for later access)
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @param supplier The supplier for the color state of the light
     * @param colorIds The color IDs to map to the states
     */
    protected void addLight (final S surface, final OutputID outputID, final int midiChannel, final int midiControl, final IntSupplier supplier, final String... colorIds)
    {
        this.addLight (surface, outputID, null, null, midiChannel, midiControl, supplier, colorIds);
    }


    /**
     * Creates a light and adds it to the given button.
     *
     * @param surface The control surface
     * @param outputID The ID of the light (for later access)
     * @param buttonID The ID of the button (for later access)
     * @param button The button to assign it to, may be null
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @param supplier The supplier for the color state of the light
     * @param colorIds The color IDs to map to the states
     */
    protected void addLight (final S surface, final OutputID outputID, final ButtonID buttonID, final IHwButton button, final int midiChannel, final int midiControl, final IntSupplier supplier, final String... colorIds)
    {
        surface.createLight (outputID, () -> {
            final int state = supplier.getAsInt ();
            // Color is the state if there are no colors provided!
            if (colorIds == null || colorIds.length == 0)
                return state;
            return this.colorManager.getColorIndex (state < 0 ? ColorManager.BUTTON_STATE_OFF : colorIds[state]);
        }, color -> surface.setTrigger (midiChannel, midiControl, color), state -> this.colorManager.getColor (state, buttonID), button);
    }


    /**
     * Get the default bind type for triggering buttons.
     *
     * @param buttonID The button ID
     * @return The default, returns CC as default
     */
    protected BindType getTriggerBindType (final ButtonID buttonID)
    {
        return BindType.CC;
    }


    /**
     * Create a hardware fader proxy on controller device 1, bind a continuous command to it and
     * bind it to a MIDI pitchbend.
     *
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param command The command to bind
     * @return The created fader
     */
    protected IHwFader addFader (final ContinuousID continuousID, final String label, final PitchbendCommand command)
    {
        return this.addFader (this.getSurface (), continuousID, label, command, 0);
    }


    /**
     * Create a hardware fader proxy on controller device 1, bind a continuous command to it and
     * bind it to a MIDI pitchbend.
     *
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param command The command to bind
     * @param midiChannel The MIDI channel
     * @return The created fader
     */
    protected IHwFader addFader (final ContinuousID continuousID, final String label, final PitchbendCommand command, final int midiChannel)
    {
        return this.addFader (this.getSurface (), continuousID, label, command, midiChannel);
    }


    /**
     * Create a hardware fader proxy on controller device 1, bind a continuous command to it and
     * bind it to a MIDI pitchbend.
     *
     * @param surface The control surface
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param command The command to bind
     * @param midiChannel The MIDI channel
     * @return The created fader
     */
    protected IHwFader addFader (final S surface, final ContinuousID continuousID, final String label, final PitchbendCommand command, final int midiChannel)
    {
        final IHwFader fader = surface.createFader (continuousID, label, true);
        fader.bind (command);
        fader.bind (surface.getMidiInput (), BindType.PITCHBEND, midiChannel, 0);
        return fader;
    }


    /**
     * Create a hardware fader proxy on controller device 1, bind a continuous command to it and
     * bind it to a MIDI CC on MIDI channel 1.
     *
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param command The command to bind
     * @param bindType The MIDI bind type
     * @param midiControl The MIDI CC or note
     */
    protected void addFader (final ContinuousID continuousID, final String label, final ContinuousCommand command, final BindType bindType, final int midiControl)
    {
        this.addFader (continuousID, label, command, bindType, 0, midiControl, true);
    }


    /**
     * Create a hardware fader proxy on controller device 1, bind a continuous command to it and
     * bind it to a MIDI CC on MIDI channel 1.
     *
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param command The command to bind
     * @param bindType The MIDI bind type
     * @param midiControl The MIDI CC or note
     * @param isVertical True if the fader is vertical, otherwise horizontal
     */
    protected void addFader (final ContinuousID continuousID, final String label, final ContinuousCommand command, final BindType bindType, final int midiControl, final boolean isVertical)
    {
        this.addFader (continuousID, label, command, bindType, 0, midiControl, isVertical);
    }


    /**
     * Create a hardware fader proxy on a controller, bind a continuous command to it and bind it to
     * a MIDI CC on MIDI channel 1.
     *
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param command The command to bind
     * @param bindType The MIDI bind type
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     */
    protected void addFader (final ContinuousID continuousID, final String label, final ContinuousCommand command, final BindType bindType, final int midiChannel, final int midiControl)
    {
        this.addFader (this.getSurface (), continuousID, label, command, bindType, midiChannel, midiControl, true);
    }


    /**
     * Create a hardware fader proxy on a controller, bind a continuous command to it and bind it to
     * a MIDI CC on MIDI channel 1.
     *
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param command The command to bind
     * @param bindType The MIDI bind type
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @param isVertical True if the fader is vertical, otherwise horizontal
     */
    protected void addFader (final ContinuousID continuousID, final String label, final ContinuousCommand command, final BindType bindType, final int midiChannel, final int midiControl, final boolean isVertical)
    {
        this.addFader (this.getSurface (), continuousID, label, command, bindType, midiChannel, midiControl, isVertical);
    }


    /**
     * Create a hardware fader proxy on a controller, bind a continuous command to it and bind it to
     * a MIDI CC.
     *
     * @param surface The control surface
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param command The command to bind
     * @param bindType The MIDI bind type
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     */
    protected void addFader (final S surface, final ContinuousID continuousID, final String label, final ContinuousCommand command, final BindType bindType, final int midiChannel, final int midiControl)
    {
        this.addFader (surface, continuousID, label, command, bindType, midiChannel, midiControl, true);
    }


    /**
     * Create a hardware fader proxy on a controller, bind a continuous command to it and bind it to
     * a MIDI CC.
     *
     * @param surface The control surface
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param command The command to bind
     * @param bindType The MIDI bind type
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @param isVertical True if the fader is vertical, otherwise horizontal
     */
    protected void addFader (final S surface, final ContinuousID continuousID, final String label, final ContinuousCommand command, final BindType bindType, final int midiChannel, final int midiControl, final boolean isVertical)
    {
        final IHwFader fader = surface.createFader (continuousID, label, isVertical);
        fader.bind (command);
        fader.bind (surface.getMidiInput (), bindType, midiChannel, midiControl);
    }


    /**
     * Create a hardware knob proxy on a controller, which sends absolute values, bind a continuous
     * command to it and bind it to a MIDI CC on MIDI channel 1.
     *
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     * @return The created knob
     */
    protected IHwAbsoluteKnob addAbsoluteKnob (final ContinuousID continuousID, final String label, final ContinuousCommand command, final int midiControl)
    {
        return this.addAbsoluteKnob (continuousID, label, command, BindType.CC, 0, midiControl);
    }


    /**
     * Create a hardware knob proxy on a controller, which sends absolute values, bind a continuous
     * command to it and bind it to a MIDI CC on MIDI channel 1.
     *
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param command The command to bind
     * @param bindType The MIDI bind type
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @return The created knob
     */
    protected IHwAbsoluteKnob addAbsoluteKnob (final ContinuousID continuousID, final String label, final ContinuousCommand command, final BindType bindType, final int midiChannel, final int midiControl)
    {
        return this.addAbsoluteKnob (this.getSurface (), continuousID, label, command, bindType, midiChannel, midiControl);
    }


    /**
     * Create a hardware knob proxy on a controller, which sends absolute values, bind a continuous
     * command to it and bind it to a MIDI CC.
     *
     * @param surface The control surface
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param command The command to bind
     * @param bindType The MIDI bind type
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @return The created knob
     */
    protected IHwAbsoluteKnob addAbsoluteKnob (final S surface, final ContinuousID continuousID, final String label, final ContinuousCommand command, final BindType bindType, final int midiChannel, final int midiControl)
    {
        final IHwAbsoluteKnob knob = surface.createAbsoluteKnob (continuousID, label);
        knob.bind (command);
        knob.bind (surface.getMidiInput (), bindType, midiChannel, midiControl);
        return knob;
    }


    /**
     * Create a hardware knob proxy on a controller, which sends relative values, bind a continuous
     * command to it and bind it to a MIDI CC on MIDI channel 1.
     *
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     * @return The created knob
     */
    protected IHwRelativeKnob addRelativeKnob (final ContinuousID continuousID, final String label, final ContinuousCommand command, final int midiControl)
    {
        return this.addRelativeKnob (continuousID, label, command, BindType.CC, 0, midiControl, RelativeEncoding.TWOS_COMPLEMENT);
    }


    /**
     * Create a hardware knob proxy on a controller, which sends relative values, bind a continuous
     * command to it and bind it to a MIDI CC on MIDI channel 1.
     *
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     * @param encoding The relative value encoding
     * @return The created knob
     */
    protected IHwRelativeKnob addRelativeKnob (final ContinuousID continuousID, final String label, final ContinuousCommand command, final int midiControl, final RelativeEncoding encoding)
    {
        return this.addRelativeKnob (continuousID, label, command, BindType.CC, 0, midiControl, encoding);
    }


    /**
     * Create a hardware knob proxy on a controller, which sends relative values, bind a continuous
     * command to it and bind it to a MIDI CC on MIDI channel 1.
     *
     * @param surface The control surface
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     * @return The created knob
     */
    protected IHwRelativeKnob addRelativeKnob (final S surface, final ContinuousID continuousID, final String label, final ContinuousCommand command, final int midiControl)
    {
        return this.addRelativeKnob (surface, continuousID, label, command, BindType.CC, 0, midiControl, RelativeEncoding.TWOS_COMPLEMENT);
    }


    /**
     * Create a hardware knob proxy on a controller, which sends relative values, bind a continuous
     * command to it and bind it to a MIDI CC on MIDI channel 1.
     *
     * @param surface The control surface
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param midiControl The MIDI CC or note
     * @param command The command to bind
     * @param encoding The relative value encoding
     * @return The created knob
     */
    protected IHwRelativeKnob addRelativeKnob (final S surface, final ContinuousID continuousID, final String label, final ContinuousCommand command, final int midiControl, final RelativeEncoding encoding)
    {
        return this.addRelativeKnob (surface, continuousID, label, command, BindType.CC, 0, midiControl, encoding);
    }


    /**
     * Create a hardware knob proxy on a controller, which sends relative values, bind a continuous
     * command to it and bind it to a MIDI CC on MIDI channel 1.
     *
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param command The command to bind
     * @param bindType The MIDI bind type
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @return The created knob
     */
    protected IHwRelativeKnob addRelativeKnob (final ContinuousID continuousID, final String label, final ContinuousCommand command, final BindType bindType, final int midiChannel, final int midiControl)
    {
        return this.addRelativeKnob (this.getSurface (), continuousID, label, command, bindType, midiChannel, midiControl, RelativeEncoding.TWOS_COMPLEMENT);
    }


    /**
     * Create a hardware knob proxy on a controller, which sends relative values, bind a continuous
     * command to it and bind it to a MIDI CC on MIDI channel 1.
     *
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param command The command to bind
     * @param bindType The MIDI bind type
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @param encoding The relative value encoding
     * @return The created knob
     */
    protected IHwRelativeKnob addRelativeKnob (final ContinuousID continuousID, final String label, final ContinuousCommand command, final BindType bindType, final int midiChannel, final int midiControl, final RelativeEncoding encoding)
    {
        return this.addRelativeKnob (this.getSurface (), continuousID, label, command, bindType, midiChannel, midiControl, encoding);
    }


    /**
     * Create a hardware knob proxy on a controller, which sends relative values, bind a continuous
     * command to it and bind it to a MIDI CC on MIDI channel 1.
     *
     * @param surface The control surface
     * @param continuousID The ID of the control (for later access)
     * @param label The label of the fader
     * @param command The command to bind
     * @param bindType The MIDI bind type
     * @param midiChannel The MIDI channel
     * @param midiControl The MIDI CC or note
     * @param encoding The relative value encoding
     * @return The created knob
     */
    protected IHwRelativeKnob addRelativeKnob (final S surface, final ContinuousID continuousID, final String label, final ContinuousCommand command, final BindType bindType, final int midiChannel, final int midiControl, final RelativeEncoding encoding)
    {
        final IHwRelativeKnob knob = surface.createRelativeKnob (continuousID, label, encoding);
        knob.bind (command);
        knob.bind (surface.getMidiInput (), bindType, midiChannel, midiControl);
        return knob;
    }


    /**
     * Update the DAW indications for the given mode.
     *
     * @param mode The new mode
     */
    protected abstract void updateIndication (final Modes mode);


    /**
     * Register observers for all scale settings. Stores the changed value in the scales object and
     * updates the actives views note mapping.
     *
     * @param conf The configuration
     */
    protected void createScaleObservers (final C conf)
    {
        conf.addSettingObserver (AbstractConfiguration.SCALES_SCALE, () -> {
            this.scales.setScaleByName (conf.getScale ());
            this.updateViewNoteMapping ();
        });
        conf.addSettingObserver (AbstractConfiguration.SCALES_BASE, () -> {
            this.scales.setScaleOffsetByName (conf.getScaleBase ());
            this.updateViewNoteMapping ();
        });
        conf.addSettingObserver (AbstractConfiguration.SCALES_IN_KEY, () -> {
            this.scales.setChromatic (!conf.isScaleInKey ());
            this.updateViewNoteMapping ();
        });
        conf.addSettingObserver (AbstractConfiguration.SCALES_LAYOUT, () -> {
            this.scales.setScaleLayoutByName (conf.getScaleLayout ());
            this.updateViewNoteMapping ();
        });
    }


    /**
     * Update the active views note mapping.
     */
    protected void updateViewNoteMapping ()
    {
        for (final S surface: this.surfaces)
        {
            final View view = surface.getViewManager ().getActiveView ();
            if (view != null)
                view.updateNoteMapping ();
        }
    }


    /**
     * Test if record mode (Arrange / Session) is flipped due to Shift press or configuration
     * setting.
     *
     * @param surface The surface
     * @return True if shifted
     */
    protected boolean isRecordShifted (final S surface)
    {
        final boolean isShift = surface.isShiftPressed ();
        final boolean isFlipRecord = this.configuration.isFlipRecord ();
        return isShift && !isFlipRecord || !isShift && isFlipRecord;
    }


    /**
     * Get the color for a button, which is controlled by the mode.
     *
     * @param buttonID The ID of the button
     * @return A color index
     */
    protected int getModeColor (final ButtonID buttonID)
    {
        final Mode mode = this.getSurface ().getModeManager ().getActiveOrTempMode ();
        return mode == null ? 0 : mode.getButtonColor (buttonID);
    }
}
