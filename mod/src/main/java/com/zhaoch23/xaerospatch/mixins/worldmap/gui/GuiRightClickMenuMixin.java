package com.zhaoch23.xaerospatch.mixins.worldmap.gui;

import com.zhaoch23.xaerospatch.common.DropDownSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xaero.map.gui.dropdown.DropDownWidget;
import xaero.map.gui.dropdown.IDropDownContainer;
import xaero.map.gui.dropdown.IDropDownWidgetCallback;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

import java.util.ArrayList;
import java.util.List;

@Mixin(
        value = xaero.map.gui.dropdown.rightclick.GuiRightClickMenu.class,
        remap = false
)
public abstract class GuiRightClickMenuMixin extends DropDownWidget {

    private static final int SLOT_HEIGHT = 11;
    public int hoveredSlot = -1;
    @Shadow
    private ArrayList<RightClickOption> actionOptions;
    @Shadow
    private boolean removed;
    @Shadow
    private GuiScreen screen;


    protected GuiRightClickMenuMixin(String[] options, int x, int y, int w, Integer selected, boolean openingUp, IDropDownWidgetCallback callback, IDropDownContainer container, boolean hasEmptyOption, String narrationTitle) {
        super(options, x, y, w, selected, openingUp, callback, container, hasEmptyOption, narrationTitle);
    }


    /**
     * Calculate the offsets of the horizontal slots
     *
     * @return The offsets of the horizontal slots
     */
    public List<Integer> calculateSlotOffsets() {
        List<Integer> offsets = new ArrayList<>();
        int pOffset = 0;
        offsets.add(pOffset);
        for (RightClickOption option : this.actionOptions) {
            String optionText = option.getDisplayName();

            // Split the option text into lines
            int numLines = optionText.split("\n").length;
            pOffset += numLines * SLOT_HEIGHT;
            offsets.add(pOffset);
        }
        return offsets;
    }

    /**
     * Get the hovered slot index
     *
     * @param mouseOnMenuX The x position of the mouse on the menu
     * @param mouseOnMenuY The y position of the mouse on the menu
     * @param slotOffsets  The offsets of the horizontal slots
     * @return -1 if not hovered, otherwise the hovered slot index
     */
    public int getHoveredSlot(int mouseOnMenuX, int mouseOnMenuY, List<Integer> slotOffsets) {
        int height = slotOffsets.get(slotOffsets.size() - 1);
        if (mouseOnMenuX < 0 || mouseOnMenuX > width || mouseOnMenuY < 0 || mouseOnMenuY >= height) {
            return -1;
        }

        int hovered = -1;
        for (int i = 0; i < slotOffsets.size() - 1; i++) {
            if (mouseOnMenuY >= slotOffsets.get(i) && mouseOnMenuY < slotOffsets.get(i + 1)) {
                hovered = i;
            }
        }
        RightClickOption option = actionOptions.get(hovered);
        // Cannot hover a slot that is an information option
        return (hovered == 0 || (option instanceof DropDownSlot && !((DropDownSlot) option).isButton())) ? -1 : hovered;
    }

    public String shortenText(String text, boolean shortenFromTheRight) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        boolean shorten = false;
        for (int textWidth = fontRenderer.getStringWidth(text);
             textWidth > width - 2;
             textWidth = fontRenderer.getStringWidth("..." + text)) {
            text = shortenFromTheRight ? text.substring(0, text.length() - 1) : text.substring(1);
            shorten = true;
        }
        if (!shorten) {
            return text;
        }
        return shortenFromTheRight ? "..." + text : text + "...";
    }

    /**
     * Draw a slot
     *
     * @param text    The text to draw
     * @param x       The x position of the slot
     * @param y       The y position of the slot
     * @param yDest   The y position of the slot destination
     * @param hovered Whether the slot is hovered
     * @param isFirst Whether the slot is the first slot
     */
    public void drawSlot(String text, int x, int y, int yDest, boolean hovered, boolean isFirst) {
        int slotBackground;
        if (isFirst) {
            slotBackground = selectedHoveredBackground;
        } else {
            slotBackground = hovered ? 0xFF323232 : 0xC8000000;
        }

        drawRect(x, y, x + width, yDest, slotBackground);
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            drawCenteredString(
                    Minecraft.getMinecraft().fontRenderer,
                    shortenText(line, true),
                    x + 1 + width / 2,
                    y + 2 + i * SLOT_HEIGHT,
                    0x00FFFFFF
            );
        }
        drawHorizontalLine(x + 1, x + width + 1, yDest - 1, 0xFF323232);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, int scaledHeight) {
        if (mouseButton != 0) {
            return false;
        }

        if (isClosed() || hoveredSlot < 0 || hoveredSlot >= actionOptions.size()) {
            return true;
        }

        if (!this.removed) {
            actionOptions.get(hoveredSlot).onSelected(screen);
            this.setClosed(true);
        }

        return true;
    }

    @Override
    public void render(int mouseX, int mouseY, int scaledHeight, boolean closedOnly) {
        if (isClosed() && closedOnly) {
            return;
        }

        List<Integer> slotOffsets = this.calculateSlotOffsets();

        int yOffsetMax = slotOffsets.get(slotOffsets.size() - 1);
        int yPos = y;
        if (!openingUp && y + yOffsetMax + 1 > scaledHeight) {
            yPos = scaledHeight - yOffsetMax - 1;
        } else if (openingUp && y - yOffsetMax - 1 < 0) {
            yPos = yOffsetMax - y;
        }
        int xPos = getXWithOffset();

        hoveredSlot = this.getHoveredSlot(mouseX - xPos, mouseY - yPos, slotOffsets);

        // Draw the slots
        for (int i = 0; i < this.actionOptions.size(); ++i) {
            int slotY = openingUp ? yPos - slotOffsets.get(i) : yPos + slotOffsets.get(i);
            int slotYDest = openingUp ? yPos - slotOffsets.get(i + 1) : yPos + slotOffsets.get(i + 1);
            this.drawSlot(this.actionOptions.get(i).getDisplayName(), xPos, slotY, slotYDest, hoveredSlot == i, i == 0);
        }

        // Draw a border
        int trim = isClosed() ? -6250336 : -1;
        drawVerticalLine(xPos, yPos, yPos + yOffsetMax, trim);
        drawVerticalLine(xPos + width, yPos, yPos + yOffsetMax, trim);
        drawHorizontalLine(xPos, xPos + width, yPos, trim);
        drawHorizontalLine(xPos, xPos + width, yPos + yOffsetMax, trim);
    }

}
