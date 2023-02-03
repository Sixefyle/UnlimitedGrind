package be.sixefyle;

import be.sixefyle.commands.PowerCommand;
import be.sixefyle.commands.ReloadCommand;
import be.sixefyle.enums.Symbols;
import be.sixefyle.listeners.BasicListeners;
import be.sixefyle.listeners.BlockGeneratorListener;
import be.sixefyle.listeners.CombatListener;
import be.sixefyle.listeners.SpawnerListener;
import com.google.common.collect.ImmutableMap;
import com.iridium.iridiumcore.Item;
import com.iridium.iridiumcore.dependencies.xseries.XMaterial;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.Upgrade;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.upgrades.UpgradeData;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class UnlimitedGrind extends JavaPlugin {

    public static UnlimitedGrind getInstance() {
        return getPlugin(UnlimitedGrind.class);
    }
    private static Economy econ = null;
    private static HolographicDisplaysAPI holoApi = null;


    @Override
    public void onEnable() {
        super.onEnable();
        initConfig();
        initHologramAPI();
        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getPluginManager().registerEvents(new BasicListeners(), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockGeneratorListener(), this);
        Bukkit.getPluginManager().registerEvents(new SpawnerListener(), this);

        getCommand("ugreload").setExecutor(new ReloadCommand());
        getCommand("power").setExecutor(new PowerCommand());

        initNewUpgrade();
    }

    public void initConfig(){
        FileConfiguration config = this.getConfig();
        saveDefaultConfig();

        config.set("power.efficiency", 1.00002);
        config.set("power.efficiencyDamage", 1.00045f);
        config.set("power.currencyConvertion", 1.000761);

        config.set("lang.spawner.gui.pickup.material", Material.BARRIER.name());
        config.set("lang.spawner.gui.pickup.pos", 8);
        config.set("lang.spawner.gui.pickup.name", "&cPickup Spawner");
        config.set("lang.spawner.gui.pickup.lore", new ArrayList<>() {{
            add("");
            add("&8You will not lose any upgrade and");
            add("&8spawner amount by picking it up!");
        }});

        config.set("lang.spawner.gui.powerUpgrade.material", Material.REDSTONE.name());
        config.set("lang.spawner.gui.powerUpgrade.pos", 12);
        config.set("lang.spawner.gui.powerUpgrade.name", "&cPower Upgrade");
        config.set("lang.spawner.gui.powerUpgrade.lore", new ArrayList<>() {{
            add("");
            add("&7Current Power: &c%power%");
            add("");
            add("&eLeft Click to increase Power by 10 ! (Cost: %powerUpgradeCost%)");
            add("&7Right Click to increase by 100 (Cost: %powerUpgradeCost100%)");
            add("&7Hold SHIFT to increase by 1,000 (Cost: %powerUpgradeCost1000%)");
            add("");
            add("&8Power increase mob health and damage.");
            add("&8It also increase all drop the creature will drop.");
        }});

        config.set("lang.spawner.gui.spawnerSpeed.material", Material.CLOCK.name());
        config.set("lang.spawner.gui.spawnerSpeed.pos", 13);
        config.set("lang.spawner.gui.spawnerSpeed.name", "&fSpawner Speed Upgrade");
        config.set("lang.spawner.gui.spawnerSpeed.lore", new ArrayList<>() {{
            add("");
            add("&7Min delay: %minTime% Ticks");
            add("&7Max delay: %maxTime% Ticks");
            add("");
            add("&eLeft click to reduce the max time delay !");
            add("&7Right click to reduce the min time delay !");
            add("");
            add("&8The minimum and maximum delay in ticks");
            add("&8before the next creature spawn ");

        }});

        config.set("lang.spawner.gui.spawnerAmount.material", Material.EGG.name());
        config.set("lang.spawner.gui.spawnerAmount.pos", 14);
        config.set("lang.spawner.gui.spawnerAmount.name", "&aSpawner Stack");
        config.set("lang.spawner.gui.spawnerAmount.lore", new ArrayList<>() {{
            add("");
            add("&7Current Amount: &a%amount%&7/%maxAmount%");
            add("");
            add("&8You can increase the spawner amount by right");
            add("&8clicking with the spawner creature type egg !");
        }});

        config.set("lang.spawner.gui.stackUpgrade.material", Material.SPAWNER.name());
        config.set("lang.spawner.gui.stackUpgrade.pos", 15);
        config.set("lang.spawner.gui.stackUpgrade.name", "&aMax Spawner Stack Upgrade");
        config.set("lang.spawner.gui.stackUpgrade.lore", new ArrayList<>() {{
            add("");
            add("&7Current level: &e%stackUpgradeLevel%&7/%maxStackUpgradeLevel%");
            add("&7Current Max Stack: %maxAmount%");
            add("");
            add("&eClick to buy 1 level!");
            add("");
            add("&8Increase the max stack by 10 for each level");
        }});

        config.set("lang.spawner.gui.rareLootUpgrade.material", Material.NETHER_STAR.name());
        config.set("lang.spawner.gui.rareLootUpgrade.pos", 11);
        config.set("lang.spawner.gui.rareLootUpgrade.name", "&1Rare Drop Upgrade");
        config.set("lang.spawner.gui.rareLootUpgrade.lore", new ArrayList<>() {{
            add("");
            add("&7Rare drop chance %rareDropChance%");
            add("");
            add("&eClick to buy 1 level!");
            add("");
            add("&8If the spawner is in silence mode, all the");
            add("&8creature spawned by it will make no noise!");
        }});

        config.set("lang.spawner.gui.spawnerSilence.material", Material.NOTE_BLOCK.name());
        config.set("lang.spawner.gui.spawnerSilence.pos", 18);
        config.set("lang.spawner.gui.spawnerSilence.name", "&aSpawner Silencer");
        config.set("lang.spawner.gui.spawnerSilence.lore", new ArrayList<>() {{
            add("");
            add("&7Silence mode is %silence%");
            add("");
            add("&eClick to change state!");
            add("");
            add("&8If the spawner is in silence mode, all the");
            add("&8creature spawned by it will make no noise!");
        }});

        config.set("lang.spawner.gui.pickedUp.name", "%mobType% Spawner");
        config.set("lang.spawner.gui.pickedUp.lore", new ArrayList<>() {{
            add("");
            add("&7Power: &c%power%");
            add("&7Amount: &e%amount%&7/%maxAmount%");
            add("&7Amount Upgrade: &e%stackUpgradeLevel%&7/%maxStackUpgradeLevel%");
            add("&7Rare Drop Chance: &b%rareDropChance%");
            add("&7Silence Mode: %silence%");
            add("");
        }});

        config.addDefault("lang.spawner.error.maxStackReached", "&cThe maximum spawner stack as been reached!");
        config.addDefault("lang.spawner.error.noEggs", "&cYou need to have an egg with the same type of the spawner!");
        config.addDefault("lang.spawner.error.minSpeedReached", "&cThe minimum spawner speed as been reached!");
        config.addDefault("lang.spawner.error.maxStackUpgradeReached", "&cThe maximum spawner stack as been reached!");

        config.set("creature.health", "&a" + Symbols.HEALTH.get() + " &a%currentHealth%&7/%maxHealth%");
        config.set("creature.power", "&c" + Symbols.POWER.get() + " &c%power%");
        config.set("creature.amount", "&ex%amount%");

        //getLogger().severe("Down here \\/");
        //getLogger().severe(config.getConfigurationSection("lang.spawner.gui").getKeys(true) + "");
        config.options().copyDefaults(true);
        saveConfig();
    }

    public void initNewUpgrade(){
        IridiumSkyblock.getInstance().getUpgradesList().remove("PowerUpgrade");
        IridiumSkyblock.getInstance().getInventories().upgradesGUI.size = 36;

        Map<Integer, UpgradeData> defaultLevel = new HashMap<>();
        int crystalCost = 10;
        for (int i = 1; i < 2; i++) { //TODO: trouver une autre facon
            defaultLevel.put(i, new UpgradeData(0, crystalCost));
            crystalCost = (int)(crystalCost + i * 1.1);
        }

        Upgrade<UpgradeData> powerUpgrade = new Upgrade<>(true, "Power Upgrade",
                new Item(XMaterial.IRON_SWORD, 22, 1, "&bPower Upgrade &7[&a%level%&2/∞&7]", Arrays.asList(
                        "&7Need more room to expand? Buy this",
                        "&7upgrade to increase your island size.",
                        "",
                        "&b&lInformation:",
                        "&b&l * &7Current Level: &b%level%",
                        "&b&l * &7Power Gained: 0 power",
                        "&b&l * &7Upgrade Cost: &b%crystalscost% Crystals",
                        "&b&lLevels:",
                        "&b&l * &7Gain &b10 Power per level",
                        "",
                        "&b&l[!] &bLeft Click to Purchase this Upgrade"
                )), defaultLevel);
        IridiumSkyblockAPI.getInstance().addUpgrade("PowerUpgrade", powerUpgrade);

        Upgrade<UpgradeData> generatorSpeedUpgrade = new Upgrade<>(true, "Generator Speed Upgrade",
                new Item(XMaterial.IRON_PICKAXE, 24, 1, "&bGenerator Speed Upgrade &7[&a%level%&2/5&7]", Arrays.asList(
                        "&7Need more room to expand? Buy this",
                        "&7upgrade to increase your island size.",
                        "",
                        "&b&lInformation:",
                        "&b&l * &7Current Level: &b%level%",
                        "&b&l * &7Upgrade Cost: &b%crystalscost% Crystals",
                        "&b&lLevels:",
                        "&b&l * &7Level 1: &b20 ticks delay",
                        "&b&l * &7Level 2: &b15 ticks delay",
                        "&b&l * &7Level 3: &b10 ticks delay",
                        "&b&l * &7Level 4: &b5 ticks delay",
                        "&b&l * &7Level 5: &b3 ticks delay",
                        "",
                        "&b&l[!] &bLeft Click to Purchase this Upgrade"
                )), ImmutableMap.<Integer, UpgradeData>builder()
                .put(1, new UpgradeData(0, 0))
                .put(2, new UpgradeData(0, 0))
                .put(3, new UpgradeData(0, 0))
                .put(4, new UpgradeData(0, 0))
                .put(5, new UpgradeData(0, 0))
                .build());
        IridiumSkyblockAPI.getInstance().addUpgrade("GeneratorSpeedUpgrade", generatorSpeedUpgrade);
    }

    private void initHologramAPI(){
        holoApi = HolographicDisplaysAPI.get(getPlugin(UnlimitedGrind.class));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static HolographicDisplaysAPI getHolographicApi() {
        return holoApi;
    }
}
