package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.ModElementList;
import com.mctechnicguy.aim.blocks.BlockScannerBase;
import com.mctechnicguy.aim.client.render.NetworkInfoOverlayRenderer;
import com.mctechnicguy.aim.util.AIMUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TileEntityScanner extends TileEntityAIMDevice implements ITickable {

	@Nonnull
    private EnumScanStatus status = EnumScanStatus.OFFLINE;
	private int ActionTicksLeft = 0;
	private EntityPlayer scannedPlayer;
	private int ChatCooldown = 200;
	private int ScanCooldown = 0;
	private boolean scanFailure;
	
	public boolean hasFastRenderer()
    {
        return status == EnumScanStatus.OFFLINE;
    }
	
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writePacketCoreData(nbtTag);
		nbtTag.setByte("status", (byte)status.id);
		nbtTag.setInteger("ticksLeft", this.ActionTicksLeft);
		return new SPacketUpdateTileEntity(this.getPos(), 0, nbtTag);
	}

    @SideOnly(Side.CLIENT)
	public void onDataPacket(NetworkManager net, @Nonnull SPacketUpdateTileEntity packet) {
		this.readPacketCoreData(packet.getNbtCompound());
		this.status = EnumScanStatus.fromID(packet.getNbtCompound().getByte("status"));
		this.ActionTicksLeft = packet.getNbtCompound().getInteger("ticksLeft");
	}
	
	public void doStartCheck(@Nonnull EntityPlayer player) {
		if (player.isDead || player.deathTime > 0 || ScanCooldown > 0) return;
		if (isActive()) return;
		List<EntityPlayer> list = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.pos.up(), this.pos.up(3).south().east()));
		if (!list.contains(player)) return;
		//The entity exists and is on the scanner.
		
		if (list.size() > 1) {
			if (ChatCooldown > 0) return;
			for (EntityPlayer P : list)
			AIMUtils.sendChatMessage("message.scannererr.toomanyplayers", P, TextFormatting.RED);
			ChatCooldown = 200;
			return;
		}
		//There is only one player on the Scanner.
		
		if (!this.isSetupCorrect()) {
			if(ChatCooldown <= 0) {
				AIMUtils.sendChatMessage("message.scannererr.wrongsetup", player, TextFormatting.RED);
				ChatCooldown = 200;
			}
			return;
		}
		//There are enough ScannerBases
		
		if (!this.hasServerCore()) {
			if(ChatCooldown <= 0) {
				AIMUtils.sendChatMessage("message.noCore", player, TextFormatting.RED);
				ChatCooldown = 200;
			}
			return;
		}
		//The scanner is connected to a core.
		
		if (this.getCore().Power <= 0 && AdvancedInventoryManagement.DOES_USE_POWER) {
			if(ChatCooldown <= 0) {
				AIMUtils.sendChatMessage("message.scannererr.noPower", player, TextFormatting.RED);
				ChatCooldown = 200;
			}
			return;
		}
		//The network has enough Power.
		
		if (!this.isPlayerAccessAllowed(player)) return;
		//The player may change the network settings.

		if (!this.isPoweredByRedstone()) {
			if(ChatCooldown <= 0) {
				AIMUtils.sendChatMessage("message.scannererr.needsRedstone", player, TextFormatting.GREEN);
				ChatCooldown = 200;
			}
			return;
		}
		//Redstone signal is applied!
		this.startScan(player);
	}
	
	private boolean isPoweredByRedstone() {
		if (world.isBlockIndirectlyGettingPowered(pos) > 0) return true;
		for (EnumFacing face : EnumFacing.HORIZONTALS) {
			if (world.isBlockIndirectlyGettingPowered(pos.offset(face)) > 0) return true;
		}
		if (world.isBlockIndirectlyGettingPowered(pos.east().north()) > 0) return true;
		if (world.isBlockIndirectlyGettingPowered(pos.east().south()) > 0) return true;
		if (world.isBlockIndirectlyGettingPowered(pos.west().north()) > 0) return true;
        return world.isBlockIndirectlyGettingPowered(pos.west().south()) > 0;
    }

	private final boolean isSetupCorrect() {
		for (EnumFacing face : EnumFacing.HORIZONTALS) {
			if (world.getBlockState(pos.offset(face)) != ModElementList.blockScannerBase.getDefaultState()) return false;
		}
		if (world.getBlockState(pos.east().north()) != ModElementList.blockScannerBase.getDefaultState()) return false;
		if (world.getBlockState(pos.east().south()) != ModElementList.blockScannerBase.getDefaultState()) return false;
		if (world.getBlockState(pos.west().north()) != ModElementList.blockScannerBase.getDefaultState()) return false;
        return world.getBlockState(pos.west().south()) == ModElementList.blockScannerBase.getDefaultState();
    }

	private final void onScanInterrupted(@Nonnull EntityPlayer player) {
		player.attackEntityFrom(AdvancedInventoryManagement.scannerDamage, 4F);
		scanFailure = true;
	}
	
	private final void onScanCancelled(String problem) {
		if (getScannedPlayer() != null) {
			if (status == EnumScanStatus.LASER_CIRCLING) scannedPlayer.attackEntityFrom(AdvancedInventoryManagement.scannerDamage, 4F);
			AIMUtils.sendChatMessage("message.scannererr." + problem, scannedPlayer, TextFormatting.RED);
		}
		ScanCooldown = 50;
		this.resetBaseBlocks();
		this.status = EnumScanStatus.OFFLINE;
		this.ActionTicksLeft = EnumScanStatus.OFFLINE.duration;
		
		world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		this.markDirty();
	}
	
	private final void resetBaseBlocks() {
		for (EnumFacing f : EnumFacing.HORIZONTALS) {
			if (world.getBlockState(pos.offset(f)).getBlock() instanceof BlockScannerBase) world.setBlockState(pos.offset(f), ModElementList.blockScannerBase.getDefaultState(), 2);
		}
		if (world.getBlockState(pos.north().east()).getBlock() instanceof BlockScannerBase)world.setBlockState(pos.north().east(), ModElementList.blockScannerBase.getDefaultState(), 2);
		if (world.getBlockState(pos.north().west()).getBlock() instanceof BlockScannerBase)world.setBlockState(pos.north().west(), ModElementList.blockScannerBase.getDefaultState(), 2);
		if (world.getBlockState(pos.south().east()).getBlock() instanceof BlockScannerBase)world.setBlockState(pos.south().east(), ModElementList.blockScannerBase.getDefaultState(), 2);
		if (world.getBlockState(pos.south().west()).getBlock() instanceof BlockScannerBase)world.setBlockState(pos.south().west(), ModElementList.blockScannerBase.getDefaultState(), 2);
	}
	
	private final void onScanFinished() {
		if (getScannedPlayer() == null) return;
		if (!this.hasServerCore()) {
			AIMUtils.sendChatMessage("message.scannererr.dataSendingFailed", scannedPlayer, TextFormatting.RED);
		} else if (scanFailure) {
			AIMUtils.sendChatMessage("message.scannererr.playerNotIdentified", scannedPlayer, TextFormatting.RED);
			scanFailure = false;
		} else if (this.getCore().playerConnectedID != null && getScannedPlayer().getUniqueID().compareTo(this.getCore().playerConnectedID) == 0) {
			AIMUtils.sendChatMessage("message.scannererr.playerEqual", scannedPlayer, TextFormatting.RED);
		} else if (!this.isPlayerAccessAllowed(scannedPlayer)) {
			AIMUtils.sendChatMessage("message.scannererr.accessdenied", scannedPlayer, TextFormatting.RED);
		} else {
			this.getCore().setConnectedPlayer(scannedPlayer);
			AIMUtils.sendChatMessageWithArgs("message.scannerfinish", scannedPlayer, TextFormatting.GREEN, scannedPlayer.getDisplayName());
		}	
		ScanCooldown = 200;
		this.resetBaseBlocks();
		
		world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		this.markDirty();
	}
	
	private final void startScan(EntityPlayer player) {
		this.ChatCooldown = 200;
		this.scannedPlayer = player;
		this.scannedPlayer.setPositionAndUpdate(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
		
		world.setBlockState(pos.north(), ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_TOP), 2);
		world.setBlockState(pos.south(), ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_BOTTOM), 2);
		world.setBlockState(pos.east(), ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_RIGHT), 2);
		world.setBlockState(pos.west(), ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_LEFT), 2);
		world.setBlockState(pos.north().east(), ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_3), 2);
		world.setBlockState(pos.north().west(), ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_2), 2);
		world.setBlockState(pos.south().west(), ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_1), 2);
		world.setBlockState(pos.south().east(), ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_4), 2);
		
		this.changeToNextStatus();
		world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		this.markDirty();
	}
	
	@Nonnull
    public EnumScanStatus getStatus() {
		return status;
	}
	
	@Nullable
    public EntityPlayer getScannedPlayer() {
		if (scannedPlayer == null || scannedPlayer.isDead || scannedPlayer.deathTime > 0) return null;
		return scannedPlayer;
	}
	
	private void changeToNextStatus() {
		status = EnumScanStatus.getNextStatus(status);
		ActionTicksLeft = status.duration;
		this.markDirty();
	}
	
	public int getActionTicksLeft() {
		return ActionTicksLeft;
	}

	private int getTicksLeftUntilCompletion() {
	    int ticksLeft = 390;
	    for (int i = 0; i <= status.id; i++) {
	        ticksLeft -= EnumScanStatus.fromID(i).duration;
        }
        ticksLeft += this.getActionTicksLeft();
	    return ticksLeft;
    }

	@Override
	public void update() {
		
		if (this.ChatCooldown > 0) this.ChatCooldown--;
		if (this.ScanCooldown > 0) this.ScanCooldown--;
		if (!isActive()) return;
		
		if (!world.isRemote) {
			List<EntityPlayer> list = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.pos.up(), this.pos.up(3).south().east()));
			if (getScannedPlayer() == null || list.isEmpty() || !list.contains(getScannedPlayer())) {
				this.onScanCancelled("playerMissing");
				return;
			} else if (isSetupBroken()) {
				this.onScanCancelled("wrongsetup");
				return;
			}
			else if (list.size() > 1) {
				for (EntityPlayer p : list) {
					if (p != scannedPlayer) this.onScanInterrupted(p);
				}
			}
			
			if (this.hasServerCore() && AdvancedInventoryManagement.DOES_USE_POWER) this.getCore().Power-= 10;
			if (this.hasServerCore() && this.getCore().Power <= 0) {
				this.getCore().Power = 0;
				this.onScanCancelled("noPower");
				return;
			}
			
		}
		
		this.ActionTicksLeft--;
		if (this.ActionTicksLeft <= 0) {
			if (status == EnumScanStatus.DOORS_CLOSE && !world.isRemote) this.onScanFinished();
			if (world.isRemote) {
				if (isSetupBroken()) {
                    IBlockState defaultState = ModElementList.blockScannerBase.getDefaultState();
					world.setBlockState(pos.north(), defaultState.withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_TOP), 2);
					world.setBlockState(pos.south(), defaultState.withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_BOTTOM), 2);
					world.setBlockState(pos.east(), defaultState.withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_RIGHT), 2);
					world.setBlockState(pos.west(), defaultState.withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_LEFT), 2);
					world.setBlockState(pos.north().east(), defaultState.withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_3), 2);
					world.setBlockState(pos.north().west(), defaultState.withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_2), 2);
					world.setBlockState(pos.south().west(), defaultState.withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_1), 2);
					world.setBlockState(pos.south().east(), defaultState.withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_4), 2);
				}
			}
			this.changeToNextStatus();
		}
		
	}

	private final boolean isSetupBroken() {
        IBlockState defaultState = ModElementList.blockScannerBase.getDefaultState();
		if (world.getBlockState(pos.east()) != defaultState.withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_RIGHT)) return true;
		if (world.getBlockState(pos.south()) != defaultState.withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_BOTTOM)) return true;
		if (world.getBlockState(pos.west()) != defaultState.withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_LEFT)) return true;
		if (world.getBlockState(pos.north()) != defaultState.withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_TOP)) return true;
		if (world.getBlockState(pos.east().north()) != defaultState.withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_3)) return true;
		if (world.getBlockState(pos.east().south()) != defaultState.withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_4)) return true;
		if (world.getBlockState(pos.west().north()) != defaultState.withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_2)) return true;
        return world.getBlockState(pos.west().south()) != defaultState.withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_1);
    }

	public boolean isActive() {
		return status != EnumScanStatus.OFFLINE;
	}


    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatusInformation(NetworkInfoOverlayRenderer renderer) {
        renderer.renderStatusString(this.hasClientCore() ? this.isActive() ? "aimoverlay.scannerstatus.active" : "aimoverlay.scannerstatus.ready" : "aimoverlay.scannerstatus.offline",
                this.hasClientCore() ? this.isActive() ? TextFormatting.GREEN : TextFormatting.YELLOW : TextFormatting.RED);
        if (this.isActive()) {
            renderer.renderTileValues("scannerticks", TextFormatting.GREEN, false, (int)(this.getTicksLeftUntilCompletion() / 20));
        }

    }

    public enum EnumScanStatus {

		OFFLINE(0, -1),
		DOORS_OPEN(1, 45),
		LASER_OUT(2, 50),
		LASER_CIRCLING(3, 200),
		LASER_IN(4, 50),
		DOORS_CLOSE(5, 45);
		
		public int id;
		public int duration;
		
		EnumScanStatus(int id, int duration) {
			this.id = id;
			this.duration = duration;
		}
		
		@Nonnull
        public static EnumScanStatus getNextStatus(@Nonnull EnumScanStatus currentStatus) {
			int newID = currentStatus.id + 1;
			if (newID >= 6) newID = 0;
			return fromID(newID);
		}
		
		@Nonnull
        public static EnumScanStatus fromID(int id) {
			switch(id) {
			case 0: return OFFLINE;
			case 1: return DOORS_OPEN;
			case 2: return LASER_OUT;
			case 3: return LASER_CIRCLING;
			case 4: return LASER_IN;
			case 5: return DOORS_CLOSE;
			default: return OFFLINE;
			}
		}

	}

}
