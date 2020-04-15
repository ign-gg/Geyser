package org.geysermc.connector.network.translators.inventory;

import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientClickWindowButtonPacket;
import com.nukkitx.protocol.bedrock.data.InventoryActionData;
import com.nukkitx.protocol.bedrock.data.InventorySource;
import org.geysermc.connector.inventory.Inventory;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.network.translators.inventory.updater.CursorInventoryUpdater;
import org.geysermc.connector.network.translators.java.JavaDeclareRecipesTranslator;

import java.util.List;

public class StonecutterInventoryTranslator extends BlockInventoryTranslator {


    public StonecutterInventoryTranslator() {
        super(2, "minecraft:stonecutter[facing=north]", null, new CursorInventoryUpdater());
    }

    @Override
    public void translateActions(GeyserSession session, Inventory inventory, List<InventoryActionData> actions) {
        for (InventoryActionData action: actions) {
            System.out.println("Source: " + action.getSource());
            System.out.println("To item: " + action.getToItem());
            System.out.println("From item: " + action.getFromItem());
            System.out.println(JavaDeclareRecipesTranslator.STONECUTTER_RECIPES.get(action.getFromItem().getId()));

            if (action.getSource().getType() == InventorySource.Type.NON_IMPLEMENTED_TODO) {
                ClientClickWindowButtonPacket clientClickWindowButtonPacket = new ClientClickWindowButtonPacket(inventory.getId(), 0);
                session.getDownstream().getSession().send(clientClickWindowButtonPacket);
                actions.remove(action);
            }
        }
        super.translateActions(session, inventory, actions);
    }

    // The inventory being opened is handled for us in Bedrock
    @Override
    public void openInventory(GeyserSession session, Inventory inventory) { }

    @Override
    public int bedrockSlotToJava(InventoryActionData action) {
        final int slot = super.bedrockSlotToJava(action);
        if (action.getSource().getContainerId() == 124) {
            switch (slot) {
                case 3:
                    return 0;
                case 50:
                    return 1;
            }
        }
        return slot;
    }

    @Override
    public int javaSlotToBedrock(int slot) {
        switch (slot) {
            case 0:
                return 3;
            case 1:
                return 50;
        }
        return super.javaSlotToBedrock(slot);
    }

}
