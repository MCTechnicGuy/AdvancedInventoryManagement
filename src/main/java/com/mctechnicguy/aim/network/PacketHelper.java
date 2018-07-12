package com.mctechnicguy.aim.network;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.ModInfo;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class PacketHelper {
	
	public static final SimpleNetworkWrapper wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(ModInfo.ID);

	public static void sendPacketToServer(IMessage packet) {
		wrapper.sendToServer(packet);
	}

	public static void sendPacketToClient(IMessage packet, EntityPlayerMP player) {
		wrapper.sendTo(packet, player);
	}

	public static void registerPackets() {
        AdvancedInventoryManagement.proxy.registerPackets();
	}
}
