package com.mctechnicguy.aim.tileentity;

import java.util.List;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.ModElementList;
import com.mctechnicguy.aim.blocks.BlockScannerBase;
import com.mctechnicguy.aim.util.AIMUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
		this.writeCoreData(nbtTag);
		nbtTag.setByte("status", (byte)status.id);
		nbtTag.setInteger("ticksLeft", this.ActionTicksLeft);
		return new SPacketUpdateTileEntity(this.getPos(), 0, nbtTag);
	}

	public void onDataPacket(NetworkManager net, @Nonnull SPacketUpdateTileEntity packet) {
		this.readCoreData(packet.getNbtCompound());
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
		
		if (!this.hasCore()) {
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
		if (world.isBlockIndirectlyGettingPowered(pos.west().south()) > 0) return true;
		return false;
	}

	private final boolean isSetupCorrect() {
		for (EnumFacing face : EnumFacing.HORIZONTALS) {
			if (world.getBlockState(pos.offset(face)) != ModElementList.blockScannerBase.getDefaultState()) return false;
		}
		if (world.getBlockState(pos.east().north()) != ModElementList.blockScannerBase.getDefaultState()) return false;
		if (world.getBlockState(pos.east().south()) != ModElementList.blockScannerBase.getDefaultState()) return false;
		if (world.getBlockState(pos.west().north()) != ModElementList.blockScannerBase.getDefaultState()) return false;
		if (world.getBlockState(pos.west().south()) != ModElementList.blockScannerBase.getDefaultState()) return false;
		return true;
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
		if (!this.hasCore()) {
			AIMUtils.sendChatMessage("message.scannererr.dataSendingFailed", scannedPlayer, TextFormatting.RED);
		} else if (scanFailure) {
			AIMUtils.sendChatMessage("message.scannererr.playerNotIdentified", scannedPlayer, TextFormatting.RED);
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
    @Override
	public String getLocalizedName() {
		return "tile.scanner.name";
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

	@Nonnull
    @Override
	public ItemStack getDisplayStack() {
		return new ItemStack(ModElementList.blockScanner);
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
			} else if (!staysSetupCorrect()) {
				this.onScanCancelled("wrongsetup");
				return;
			}
			else if (list.size() > 1) {
				for (EntityPlayer p : list) {
					if (p != scannedPlayer) this.onScanInterrupted(p);
				}
			}
			
			if (this.hasCore() && AdvancedInventoryManagement.DOES_USE_POWER) this.getCore().Power-= 10;
			if (this.hasCore() && this.getCore().Power <= 0) {
				this.getCore().Power = 0;
				this.onScanCancelled("noPower");
				return;
			}
			
		}
		
		this.ActionTicksLeft--;
		if (this.ActionTicksLeft <= 0) {
			if (status == EnumScanStatus.DOORS_CLOSE && !world.isRemote) this.onScanFinished();
			if (world.isRemote) {
				if (!staysSetupCorrect()) {
					world.setBlockState(pos.north(), ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_TOP), 2);
					world.setBlockState(pos.south(), ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_BOTTOM), 2);
					world.setBlockState(pos.east(), ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_RIGHT), 2);
					world.setBlockState(pos.west(), ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_LEFT), 2);
					world.setBlockState(pos.north().east(), ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_3), 2);
					world.setBlockState(pos.north().west(), ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_2), 2);
					world.setBlockState(pos.south().west(), ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_1), 2);
					world.setBlockState(pos.south().east(), ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_4), 2);
				}
			}
			this.changeToNextStatus();
		}
		
	}

	private final boolean staysSetupCorrect() {
		if (world.getBlockState(pos.east()) != ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_RIGHT)) return false;
		if (world.getBlockState(pos.south()) != ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_BOTTOM)) return false;
		if (world.getBlockState(pos.west()) != ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_LEFT)) return false;
		if (world.getBlockState(pos.north()) != ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.STRAIGHT_TOP)) return false;
		if (world.getBlockState(pos.east().north()) != ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_3)) return false;
		if (world.getBlockState(pos.east().south()) != ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_4)) return false;
		if (world.getBlockState(pos.west().north()) != ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_2)) return false;
		if (world.getBlockState(pos.west().south()) != ModElementList.blockScannerBase.getDefaultState().withProperty(BlockScannerBase.POS, BlockScannerBase.EnumPos.CURVE_1)) return false;
		return true;
	}

	public boolean isActive() {
		return status != EnumScanStatus.OFFLINE;
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
