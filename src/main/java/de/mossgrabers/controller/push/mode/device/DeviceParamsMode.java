// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.push.mode.device;

import de.mossgrabers.controller.push.controller.Push1Display;
import de.mossgrabers.controller.push.controller.PushColorManager;
import de.mossgrabers.controller.push.controller.PushControlSurface;
import de.mossgrabers.controller.push.mode.BaseMode;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.IValueChanger;
import de.mossgrabers.framework.controller.display.IGraphicDisplay;
import de.mossgrabers.framework.controller.display.ITextDisplay;
import de.mossgrabers.framework.daw.DAWColors;
import de.mossgrabers.framework.daw.IBank;
import de.mossgrabers.framework.daw.ICursorDevice;
import de.mossgrabers.framework.daw.IDeviceBank;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.IParameterBank;
import de.mossgrabers.framework.daw.IParameterPageBank;
import de.mossgrabers.framework.daw.data.IChannel;
import de.mossgrabers.framework.daw.data.IDevice;
import de.mossgrabers.framework.daw.data.IItem;
import de.mossgrabers.framework.daw.data.IParameter;
import de.mossgrabers.framework.mode.ModeManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.utils.StringUtils;


/**
 * Mode for editing device remote control parameters.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class DeviceParamsMode extends BaseMode
{
    private static final String [] MENU     =
    {
        "On",
        "Parameters",
        "Expanded",
        "Chains",
        "Banks",
        "Pin Device",
        "Window",
        "Up"
    };

    protected final String []      hostMenu = new String [MENU.length];
    protected boolean              showDevices;


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model The model
     */
    public DeviceParamsMode (final PushControlSurface surface, final IModel model)
    {
        super ("Parameters", surface, model);

        this.isTemporary = false;
        this.showDevices = true;

        System.arraycopy (MENU, 0, this.hostMenu, 0, MENU.length);
        final IHost host = this.model.getHost ();
        if (!host.hasPinning ())
            this.hostMenu[5] = "";
        if (!host.hasSlotChains ())
            this.hostMenu[3] = "";
    }


    /**
     * Show devices or the parameter banks of the cursor device for selection.
     *
     * @param enable True to enable
     */
    public void setShowDevices (final boolean enable)
    {
        this.showDevices = enable;
    }


    /**
     * Returns true if devices are shown otherwise parameter banks.
     *
     * @return True if devices are shown otherwise parameter banks
     */
    public boolean isShowDevices ()
    {
        return this.showDevices;
    }


    /** {@inheritDoc} */
    @Override
    public void onKnobValue (final int index, final int value)
    {
        this.model.getCursorDevice ().getParameterBank ().getItem (index).changeValue (value);
    }


    /** {@inheritDoc} */
    @Override
    public void onKnobTouch (final int index, final boolean isTouched)
    {
        this.isKnobTouched[index] = isTouched;

        final ICursorDevice cd = this.model.getCursorDevice ();
        final IParameter param = cd.getParameterBank ().getItem (index);
        if (isTouched && this.surface.isDeletePressed ())
        {
            this.surface.setTriggerConsumed (ButtonID.DELETE);
            param.resetValue ();
        }
        param.touchValue (isTouched);
        this.checkStopAutomationOnKnobRelease (isTouched);
    }


    /** {@inheritDoc} */
    @Override
    public void onFirstRow (final int index, final ButtonEvent event)
    {
        if (event == ButtonEvent.DOWN)
            return;

        if (event == ButtonEvent.UP)
        {
            final ICursorDevice cd = this.model.getCursorDevice ();
            if (!cd.doesExist ())
                return;

            if (!this.showDevices)
            {
                cd.getParameterPageBank ().selectPage (index);
                return;
            }

            if (this.surface.isPressed (ButtonID.DUPLICATE))
            {
                this.surface.setTriggerConsumed (ButtonID.DUPLICATE);
                cd.duplicate ();
                return;
            }

            if (this.surface.isPressed (ButtonID.DELETE))
            {
                this.surface.setTriggerConsumed (ButtonID.DELETE);
                cd.getDeviceBank ().getItem (index).remove ();
                return;
            }

            if (cd.getIndex () != index)
            {
                cd.getDeviceBank ().getItem (index).select ();
                return;
            }

            final ModeManager modeManager = this.surface.getModeManager ();
            if (!cd.hasLayers ())
            {
                ((DeviceParamsMode) modeManager.getMode (Modes.DEVICE_PARAMS)).setShowDevices (false);
                return;
            }

            final IChannel layer = cd.getLayerOrDrumPadBank ().getSelectedItem ();
            if (layer == null)
                cd.getLayerOrDrumPadBank ().getItem (0).select ();
            modeManager.setActiveMode (Modes.DEVICE_LAYER);
            return;
        }

        // LONG press - move upwards
        this.surface.setTriggerConsumed (ButtonID.get (ButtonID.ROW1_1, index));
        this.moveUp ();
    }


    /**
     * Move up the hierarchy.
     */
    protected void moveUp ()
    {
        final ModeManager modeManager = this.surface.getModeManager ();
        if (modeManager.isActiveOrTempMode (Modes.DEVICE_CHAINS))
        {
            modeManager.setActiveMode (Modes.DEVICE_PARAMS);
            return;
        }

        // There is no device on the track move upwards to the track view
        final ICursorDevice cd = this.model.getCursorDevice ();
        if (!cd.doesExist ())
        {
            this.surface.getButton (ButtonID.TRACK).trigger (ButtonEvent.DOWN);
            return;
        }

        // Parameter banks are shown -> show devices
        final DeviceParamsMode deviceParamsMode = (DeviceParamsMode) modeManager.getMode (Modes.DEVICE_PARAMS);
        if (!deviceParamsMode.isShowDevices ())
        {
            deviceParamsMode.setShowDevices (true);
            return;
        }

        // Devices are shown, if nested show the layers otherwise move up to the tracks
        if (cd.isNested ())
        {
            cd.selectParent ();
            this.model.getHost ().scheduleTask ( () -> {
                if (cd.hasLayers ())
                    modeManager.setActiveMode (Modes.DEVICE_LAYER);
                else
                    modeManager.setActiveMode (Modes.DEVICE_PARAMS);
                deviceParamsMode.setShowDevices (false);
                cd.selectChannel ();
            }, 300);
            return;
        }

        // Move up to the track
        if (this.model.isCursorDeviceOnMasterTrack ())
            this.surface.getButton (ButtonID.MASTERTRACK).trigger (ButtonEvent.DOWN);
        else
            this.surface.getButton (ButtonID.TRACK).trigger (ButtonEvent.DOWN);
    }


    /** {@inheritDoc} */
    @Override
    public int getButtonColor (final ButtonID buttonID)
    {
        final ICursorDevice cd = this.model.getCursorDevice ();

        int index = this.isButtonRow (0, buttonID);
        if (index >= 0)
        {
            if (!cd.doesExist ())
                return super.getButtonColor (buttonID);

            final int selectedColor = this.isPush2 ? PushColorManager.PUSH2_COLOR_ORANGE_HI : PushColorManager.PUSH1_COLOR_ORANGE_HI;
            final int existsColor = this.isPush2 ? PushColorManager.PUSH2_COLOR_YELLOW_LO : PushColorManager.PUSH1_COLOR_YELLOW_LO;
            final int offColor = this.isPush2 ? PushColorManager.PUSH2_COLOR_BLACK : PushColorManager.PUSH1_COLOR_BLACK;

            if (this.showDevices)
            {
                final IDeviceBank bank = cd.getDeviceBank ();
                return bank.getItem (index).doesExist () ? index == cd.getIndex () ? selectedColor : existsColor : offColor;
            }
            final IParameterPageBank bank = cd.getParameterPageBank ();
            final int selectedItemIndex = bank.getSelectedItemIndex ();
            return !bank.getItem (index).isEmpty () ? index == selectedItemIndex ? selectedColor : existsColor : offColor;
        }

        index = this.isButtonRow (1, buttonID);
        if (index >= 0)
        {
            final int white = this.isPush2 ? PushColorManager.PUSH2_COLOR2_WHITE : PushColorManager.PUSH1_COLOR2_WHITE;
            if (!cd.doesExist ())
                return index == 7 ? white : super.getButtonColor (buttonID);

            final int green = this.isPush2 ? PushColorManager.PUSH2_COLOR2_GREEN : PushColorManager.PUSH1_COLOR2_GREEN;
            final int grey = this.isPush2 ? PushColorManager.PUSH2_COLOR2_GREY_LO : PushColorManager.PUSH1_COLOR2_GREY_LO;
            final int orange = this.isPush2 ? PushColorManager.PUSH2_COLOR2_ORANGE : PushColorManager.PUSH1_COLOR2_ORANGE;
            final int off = this.isPush2 ? PushColorManager.PUSH2_COLOR_BLACK : PushColorManager.PUSH1_COLOR_BLACK;
            final int turquoise = this.isPush2 ? PushColorManager.PUSH2_COLOR2_TURQUOISE_HI : PushColorManager.PUSH1_COLOR2_TURQUOISE_HI;

            switch (index)
            {
                case 0:
                    return cd.isEnabled () ? green : grey;
                case 1:
                    return cd.isParameterPageSectionVisible () ? orange : white;
                case 2:
                    return cd.isExpanded () ? orange : white;
                case 3:
                    return this.surface.getModeManager ().isActiveOrTempMode (Modes.DEVICE_CHAINS) ? orange : white;
                case 4:
                    return this.showDevices ? white : orange;
                case 5:
                    return this.model.getHost ().hasPinning () ? cd.isPinned () ? turquoise : grey : off;
                case 6:
                    return cd.isWindowOpen () ? turquoise : grey;
                default:
                case 7:
                    return white;
            }
        }

        return super.getButtonColor (buttonID);
    }


    /** {@inheritDoc} */
    @Override
    public void onSecondRow (final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.DOWN)
            return;
        final ICursorDevice device = this.model.getCursorDevice ();
        final ModeManager modeManager = this.surface.getModeManager ();
        switch (index)
        {
            case 0:
                if (device.doesExist ())
                    device.toggleEnabledState ();
                break;
            case 1:
                if (device.doesExist ())
                    device.toggleParameterPageSectionVisible ();
                break;
            case 2:
                if (device.doesExist ())
                    device.toggleExpanded ();
                break;
            case 3:
                if (!this.model.getHost ().hasSlotChains ())
                    return;
                if (modeManager.isActiveOrTempMode (Modes.DEVICE_CHAINS))
                    modeManager.setActiveMode (Modes.DEVICE_PARAMS);
                else
                    modeManager.setActiveMode (Modes.DEVICE_CHAINS);
                break;
            case 4:
                if (!device.doesExist ())
                    return;
                if (!modeManager.isActiveOrTempMode (Modes.DEVICE_PARAMS))
                    modeManager.setActiveMode (Modes.DEVICE_PARAMS);
                this.showDevices = !this.showDevices;
                break;
            case 5:
                if (device.doesExist ())
                    device.togglePinned ();
                break;
            case 6:
                if (device.doesExist ())
                    device.toggleWindowOpen ();
                break;
            case 7:
                this.moveUp ();
                break;
            default:
                // Not used
                break;
        }
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay1 (final ITextDisplay display)
    {
        final ICursorDevice cd = this.model.getCursorDevice ();
        if (!this.checkExists1 (display, cd))
            return;

        // Row 1 & 2
        final IParameterBank parameterBank = cd.getParameterBank ();
        for (int i = 0; i < 8; i++)
        {
            final IParameter param = parameterBank.getItem (i);
            display.setCell (0, i, param.doesExist () ? StringUtils.fixASCII (param.getName ()) : "").setCell (1, i, param.getDisplayedValue (8));
        }

        // Row 3
        display.setBlock (2, 0, "Selected Device:").setBlock (2, 1, cd.getName ());

        // Row 4
        if (this.showDevices)
        {
            final IDeviceBank deviceBank = cd.getDeviceBank ();
            for (int i = 0; i < 8; i++)
            {
                final IDevice device = deviceBank.getItem (i);
                final StringBuilder sb = new StringBuilder ();
                if (device.doesExist ())
                {
                    if (i == cd.getIndex ())
                        sb.append (Push1Display.SELECT_ARROW);
                    sb.append (device.getName ());
                }
                display.setCell (3, i, sb.toString ());
            }
            return;
        }
        final IParameterPageBank bank = cd.getParameterPageBank ();
        final int selectedItemIndex = bank.getSelectedItemIndex ();
        for (int i = 0; i < bank.getPageSize (); i++)
        {
            final String item = bank.getItem (i);
            display.setCell (3, i, !item.isEmpty () ? (i == selectedItemIndex ? Push1Display.SELECT_ARROW : "") + item : "");
        }
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay2 (final IGraphicDisplay display)
    {
        final ICursorDevice cd = this.model.getCursorDevice ();
        if (!this.checkExists2 (display, cd))
            return;

        final String color = this.model.getCurrentTrackBank ().getSelectedChannelColorEntry ();
        final double [] bottomMenuColor = DAWColors.getColorEntry (color);

        final IDeviceBank deviceBank = cd.getDeviceBank ();
        final IParameterBank parameterBank = cd.getParameterBank ();
        final IParameterPageBank parameterPageBank = cd.getParameterPageBank ();
        final int selectedPage = parameterPageBank.getSelectedItemIndex ();
        final boolean hasPinning = this.model.getHost ().hasPinning ();
        final IValueChanger valueChanger = this.model.getValueChanger ();
        for (int i = 0; i < parameterBank.getPageSize (); i++)
        {
            final boolean isTopMenuOn = this.getTopMenuEnablement (cd, hasPinning, i);

            String bottomMenu;
            final String bottomMenuIcon;
            boolean isBottomMenuOn;
            if (this.showDevices)
            {
                final IDevice item = deviceBank.getItem (i);
                bottomMenuIcon = item.getName ();
                bottomMenu = item.doesExist () ? item.getName (12) : "";
                isBottomMenuOn = i == cd.getIndex ();
            }
            else
            {
                bottomMenuIcon = cd.getName ();
                bottomMenu = parameterPageBank.getItem (i);

                if (bottomMenu.length () > 12)
                    bottomMenu = bottomMenu.substring (0, 12);
                isBottomMenuOn = i == selectedPage;
            }

            final IParameter param = parameterBank.getItem (i);
            final boolean exists = param.doesExist ();
            final String parameterName = exists ? param.getName (9) : "";
            final int parameterValue = valueChanger.toDisplayValue (exists ? param.getValue () : 0);
            final String parameterValueStr = exists ? param.getDisplayedValue (8) : "";
            final boolean parameterIsActive = this.isKnobTouched[i];
            final int parameterModulatedValue = valueChanger.toDisplayValue (exists ? param.getModulatedValue () : -1);

            display.addParameterElement (this.hostMenu[i], isTopMenuOn, bottomMenu, bottomMenuIcon, bottomMenuColor, isBottomMenuOn, parameterName, parameterValue, parameterValueStr, parameterIsActive, parameterModulatedValue);
        }
    }


    protected boolean checkExists1 (final ITextDisplay display, final ICursorDevice cd)
    {
        if (cd.doesExist ())
            return true;
        display.setBlock (1, 0, "           Select").setBlock (1, 1, "a device or press").setBlock (1, 2, "'Add Effect'...  ").allDone ();
        return false;
    }


    protected boolean checkExists2 (final IGraphicDisplay display, final ICursorDevice cd)
    {
        if (cd.doesExist ())
            return true;
        for (int i = 0; i < 8; i++)
            display.addOptionElement (i == 2 ? "Please select a device or press 'Add Device'..." : "", i == 7 ? "Up" : "", true, "", "", false, true);
        return false;
    }


    protected boolean getTopMenuEnablement (final ICursorDevice cd, final boolean hasPinning, final int index)
    {
        switch (index)
        {
            case 0:
                return cd.isEnabled ();
            case 1:
                return cd.isParameterPageSectionVisible ();
            case 2:
                return cd.isExpanded ();
            case 3:
                return this.surface.getModeManager ().isActiveOrTempMode (Modes.DEVICE_CHAINS);
            case 4:
                return !this.surface.getModeManager ().isActiveOrTempMode (Modes.DEVICE_CHAINS) && !this.showDevices;
            case 5:
                return hasPinning && cd.isPinned ();
            case 6:
                return cd.isWindowOpen ();
            case 7:
                return true;
            default:
                // Not used
                return false;
        }
    }


    /** {@inheritDoc} */
    @Override
    protected IBank<? extends IItem> getBank ()
    {
        final ICursorDevice cursorDevice = this.model.getCursorDevice ();
        if (cursorDevice == null)
            return null;
        return this.showDevices ? cursorDevice.getDeviceBank () : cursorDevice.getParameterBank ();
    }
}