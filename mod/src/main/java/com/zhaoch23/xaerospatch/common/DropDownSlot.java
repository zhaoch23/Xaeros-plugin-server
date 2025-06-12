package com.zhaoch23.xaerospatch.common;

import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

public abstract class DropDownSlot extends RightClickOption {

    public boolean isButton = true;

    public DropDownSlot(String name, int index, IRightClickableElement target, boolean isButton) {
        super(name, index, target);
        this.isButton = isButton;
    }

    public boolean isButton() {
        return isButton;
    }

    public void setButton(boolean button) {
        isButton = button;
    }

}
