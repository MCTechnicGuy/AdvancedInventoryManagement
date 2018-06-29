package com.mctechnicguy.aim;

import com.mctechnicguy.aim.capability.CapabilityPlayerAccess;
import com.mctechnicguy.aim.event.AIMEventHandler;
import com.mctechnicguy.aim.network.PacketHelper;
import com.mctechnicguy.aim.util.ModCompatHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

@Mod(modid=ModInfo.ID, name=ModInfo.NAME, version=ModInfo.VERSION)

public class AdvancedInventoryManagement {
	
	//Config-Values
	public static boolean DOES_USE_POWER;
	public static int BASE_POWER_USAGE;
	public static int POWER_PER_MACHINE;
	public static int POWER_PER_CABLE;
	public static int POWER_PER_BRIDGE;
	public static int POWER_PER_POTION_GENERATION;
	public static int MAX_CABLE_LENGHT;
	public static int MAX_NETWORK_ELEMENTS;
	public static int MAX_NETWORK_CABLES;
	public static int CORE_UPDATE_TIME;
	public static int XP_PER_BUCKET;
	public static boolean USE_LIQUID_XP;
	public static float EU_TO_RF;
	public static int MAX_GENERATOR_POWER_OUTPUT;
	public static int MAX_SOLAR_POWER_OUTPUT;
	public static float SOLAR_POWER_BIOME_MULTIPLIER;
	public static int POWER_PER_MOTION_EDIT;
	public static int POWER_PER_SLOT_SELECTION;
	public static int POWER_PER_TELEPORT;
	public static boolean ALLOW_TELEPORT_BETWEEN_DIMENSIONS;
	public static boolean TRAVERSABLE_CABLES;

	//Gui-IDs
	public static final int guiIDCore = 0;
	public static final int guiIDNetworkInfo = 1;
	public static final int guiIDGuide = 2;
	public static final int guiIDAdvancedNetworkInfo = 3;
	
	public static Logger log;
	
	//Tabs
	@Nonnull
	public static CreativeTabs AIMTab = new CreativeTabs("AIMTab") {
		@Nonnull
		public ItemStack getTabIconItem() {
			return new ItemStack(ModElementList.blockAIMCore);
		}
	};
	
	//Scanner-Damage
	@Nonnull
	public static DamageSource scannerDamage = new DamageSource("scannerDamage").setDamageBypassesArmor().setDamageIsAbsolute();

	// Config-File
	private void InitConfiguration(FMLPreInitializationEvent event) {
		
		Configuration config = new Configuration(new File("config/AdvancedInventoryManagement.cfg"));
		config.load();
		DOES_USE_POWER = config.getBoolean("enable_system_power_use", "Power", true, "If this is true, an AIM-System will use RF-Power");
		BASE_POWER_USAGE = config.getInt("basePowerUsage", "Power", 10, 1, Integer.MAX_VALUE, "The base power-usage for any system");
		POWER_PER_MACHINE = config.getInt("powerPerMachine", "Power", 20, 1, Integer.MAX_VALUE, "The power-usage for any machine added to the network");
		POWER_PER_CABLE = config.getInt("powerPerCable", "Power", 1, 1, Integer.MAX_VALUE, "The power-usage for any cable added to the network");
		POWER_PER_BRIDGE = config.getInt("powerPerBridge", "Power", 50, 1, Integer.MAX_VALUE, "The power-usage for any signal-bridge-block added to the network");
		MAX_CABLE_LENGHT = config.getInt("max_cable_lenght", "Network", 10000, 20, Integer.MAX_VALUE, "How long the cables of a single Network can go.");
		MAX_NETWORK_CABLES = config.getInt("max_network_cables", "Network", 10000, 20, Integer.MAX_VALUE, "How many cables a single Network can have.");
		MAX_NETWORK_ELEMENTS = config.getInt("max_network_devices", "Network", 1000, 5, Integer.MAX_VALUE, "How many devices(Relays, Monitors...) a single Network can have.");
		CORE_UPDATE_TIME = config.getInt("core_update_time", "Network", 100, 10, Short.MAX_VALUE, "How many ticks an Inventory Management Core should wait between updates. "
				+ "(Lower values can hurt performance.)");

        POWER_PER_POTION_GENERATION = config.getInt("Power_per_Potion_Gen", "Potion Relay", 1000, 0, Integer.MAX_VALUE, "How many Power it takes to generate one Potion in the Potion Relay");
        XP_PER_BUCKET = config.getInt("xp_per_bucket", "Experience Relay", 50, 1, Integer.MAX_VALUE, "How many XP-Points are stored in one bucket (1000mb) of molten XP. This also counts for other mods xp-fluids.");
		USE_LIQUID_XP = config.getBoolean("Enable_liquid_xp_mode", "Experience Relay", true, "Allows the Experience Liquid Relay to use Liquid XP from OpenBlocks/EnderIO when in the right mode.");
		EU_TO_RF = config.getFloat("eu_per_rf", "Power", 4F, 0.001F, Float.MAX_VALUE, "How many EU should be converted to 1 RF");
		MAX_GENERATOR_POWER_OUTPUT = config.getInt("max_generator_power_output", "Power", 50, 0, Integer.MAX_VALUE, "The amount of Power in RF/t the solid fuel generator will output when at max heat.");
		MAX_SOLAR_POWER_OUTPUT = config.getInt("max_solar_power_output", "Power", 10, 0, Integer.MAX_VALUE, "The amount of Power in RF/t the solar generator will output at base level.");
        SOLAR_POWER_BIOME_MULTIPLIER = config.getFloat("solar_power_biome_multiplier", "Power", 1.5F, 0F, Float.MAX_VALUE, "The multiplier applied to the solar generator when placed in a hot (desert) biome.");
		POWER_PER_MOTION_EDIT = config.getInt("power_per_motion_edit", "Power", 300, 0, 1000000, "The amount of Power in RF needed to add 1 block / second to the players motion via the motion editor.");
		POWER_PER_SLOT_SELECTION = config.getInt("power_per_hotbar_selection_edit", "Power", 500, 0, Integer.MAX_VALUE, "The amount of Power in RF needed to force the connected player to select a specific slot via the hotbar selection editor.");
		POWER_PER_TELEPORT = config.getInt("power_per_teleport", "Power", 2000, 0, Integer.MAX_VALUE, "The amount of Power in RF needed to teleport a player using the Position Editor. The value is doubled when teleporting between dimensions.");
		ALLOW_TELEPORT_BETWEEN_DIMENSIONS = config.getBoolean("allow_teleport_between_dimensions", "Position Editor", true, "Whether or not to allow teleporting between different dimensions using the Position Editor");
		TRAVERSABLE_CABLES = config.getBoolean("traversable_cables", "Network", false, "Set this to true to make network cables traversable, so the player can walk through them.");
		config.save();
	}

	//Capabilities
	@Nullable
	@CapabilityInject(CapabilityPlayerAccess.class)
	public static final Capability<CapabilityPlayerAccess> PLAYER_ACCESS_CAP = null; 

	
	@Instance(ModInfo.ID)
	public static AdvancedInventoryManagement instance;
		
	@SidedProxy(clientSide="com.mctechnicguy.aim.ClientProxy", serverSide="com.mctechnicguy.aim.CommonProxy")
	public static CommonProxy proxy;

    static {
        FluidRegistry.enableUniversalBucket();
    }
	
	@EventHandler
	public void preInit(@Nonnull FMLPreInitializationEvent event) {
		log = event.getModLog();
		// Config
		this.InitConfiguration(event);
		log.log(Level.INFO, "Configuration loaded!");
		
		ModMetadata metadata = event.getModMetadata();
		metadata.url = ModInfo.URL;
	    metadata.autogenerated = false;
	    metadata.description = ModInfo.DESC;

	    ModElementList.createBlocks();
	    ModElementList.createItems();
        ModElementList.createFluids();

        proxy.registerFluid(ModElementList.blockMoltenXP, "moltenxp");

	    MinecraftForge.EVENT_BUS.register(new AIMEventHandler());
	    CapabilityManager.INSTANCE.register(CapabilityPlayerAccess.class, new CapabilityPlayerAccess.Storage(), new CapabilityPlayerAccess.Factory());
	    proxy.registerKeys();
        PacketHelper.registerPackets();
	    NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
	}
		
	@EventHandler
	public void load(FMLInitializationEvent event) {
        ModCompatHelper.searchForModFluids();
		ModCompatHelper.searchForCompatMods();
	}
		
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
		
}
