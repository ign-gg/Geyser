package org.geysermc.connector.network.translators.inventory;

import com.nukkitx.protocol.bedrock.data.InventoryActionData;
import org.geysermc.connector.inventory.Inventory;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.network.translators.inventory.updater.CursorInventoryUpdater;

public class CartographyInventoryTranslator extends BlockInventoryTranslator {


    public CartographyInventoryTranslator() {
        super(2, "minecraft:stonecutter[facing=north]", null, new CursorInventoryUpdater());
    }

    // The inventory being opened is handled for us in Bedrock
    @Override
    public void openInventory(GeyserSession session, Inventory inventory) { }

    @Override
    public int bedrockSlotToJava(InventoryActionData action) {
        final int slot = super.bedrockSlotToJava(action);
        System.out.println("Container: " + action.getSource());
        System.out.println("Bedrock slot: " + slot);
        if (action.getSource().getContainerId() == 124) {
            switch (slot) {
                case 2:
                    return 0;
                case 50:
                    return 1;
            }
        }
        return slot;
    }

    @Override
    public int javaSlotToBedrock(int slot) {
        System.out.println("Java slot: " + slot);
        switch (slot) {
            case 0:
                return 2;
            case 50:
                return 1;
        }
        return super.javaSlotToBedrock(slot);
    }

}