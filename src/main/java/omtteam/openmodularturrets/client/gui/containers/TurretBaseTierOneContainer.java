package omtteam.openmodularturrets.client.gui.containers;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import omtteam.openmodularturrets.client.gui.customSlot.AmmoSlot;
import omtteam.openmodularturrets.tileentity.TurretBase;

public class TurretBaseTierOneContainer extends TurretBaseContainer {

    public TurretBaseTierOneContainer(InventoryPlayer inventoryPlayer, TurretBase te) {
        this.tileEntity = te;

        for (int x = 0; x < 9; x++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, x, 8 + x * 18, 142));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, 9 + x + y * 9, 8 + x * 18, 84 + y * 18));
            }
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlotToContainer(new AmmoSlot(tileEntity, x + y * 3, 8 + x * 18, 17 + y * 18));
            }
        }
    }
}