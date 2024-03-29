package be.sixefyle;

import be.sixefyle.arena.WorldManager;
import be.sixefyle.arena.pve.PveArenaListener;
import be.sixefyle.commands.*;
import be.sixefyle.entity.boss.BossListener;
import be.sixefyle.enums.Symbols;
import be.sixefyle.gui.GuiManager;
import be.sixefyle.items.ItemManager;
import be.sixefyle.listeners.*;
import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.collect.ImmutableMap;
import com.iridium.iridiumcore.Item;
import com.iridium.iridiumcore.dependencies.xseries.XMaterial;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.Upgrade;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.configs.inventories.InventoryConfig;
import com.iridium.iridiumskyblock.upgrades.UpgradeData;
import com.jeff_media.armorequipevent.ArmorEquipEvent;
import fr.skytasul.glowingentities.GlowingEntities;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Boss;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class UnlimitedGrind extends JavaPlugin {

    public static UnlimitedGrind getInstance() {
        return getPlugin(UnlimitedGrind.class);
    }
    private static Economy econ = null;
    private static HolographicDisplaysAPI holoApi = null;
    private static GlowingEntities glowingEntities;
    private static ProtocolListener protocolListener;

    @Override
    public void onEnable() {
        super.onEnable();
        protocolListener = new ProtocolListener(ProtocolLibrary.getProtocolManager());
        initConfig();
        initHologramAPI();
        initGlowingApi();
        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ArmorEquipEvent.registerListener(this);
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new BasicListeners(), this);
        pluginManager.registerEvents(new CombatListener(), this);
        pluginManager.registerEvents(new BlockGeneratorListener(), this);
        pluginManager.registerEvents(new SpawnerListener(), this);
        pluginManager.registerEvents(new ItemManager(), this);
        pluginManager.registerEvents(new PveArenaListener(), this);
        pluginManager.registerEvents(new StatsListener(), this);
        pluginManager.registerEvents(new EffectListener(), this);
        pluginManager.registerEvents(new IslandListener(), this);
        pluginManager.registerEvents(new AnvilListener(), this);
        pluginManager.registerEvents(new GuiManager(), this);
        pluginManager.registerEvents(new BossListener(), this);

        getCommand("ugreload").setExecutor(new ReloadCommand());
        getCommand("power").setExecutor(new PowerCommand());
        getCommand("randomgive").setExecutor(new RandomItemCommand());
        getCommand("randomgive").setTabCompleter(new RandomItemCommand());
        getCommand("arena").setExecutor(new ArenaCommand());

        getCommand("group").setExecutor(new GroupCommand());
        getCommand("group").setTabCompleter(new GroupCommand());

        initNewUpgrade();
        initNewMenuIcons();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        for (World world : Bukkit.getWorlds()) {
            if(world.getName().startsWith("arena_")){
                WorldManager.deleteWorld(world);
            }
        }
    }

    public void initConfig(){
        FileConfiguration config = this.getConfig();
        saveDefaultConfig();

        config.set("power.efficiency", 1.29912);
        config.set("power.efficiencyDamage", 1.00512);
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
        config.addDefault("lang.spawner.error.maxRareDropChance", "&cThe maximum rare drop chance as been reached!");

        config.set("lang.arena.gui.powerUpgrade.material", Material.REDSTONE.name());
        config.set("lang.arena.gui.powerUpgrade.pos", 2);
        config.set("lang.arena.gui.powerUpgrade.name", "&cPower Upgrade");
        config.set("lang.arena.gui.powerUpgrade.lore", new ArrayList<>() {{
            add("");
            add("&7Current Power: &c%power%");
            add("");
            add("&7Creature Health: &a%creatureHealth%%");
            add("&7Creature Damage: &c%creatureDamage%%");
            add("");
            add("&eLeft Click to increase Power by 50 !");
            add("&7Right Click to increase by 500");
            add("&cHold SHIFT to decrease");
            add("");
            add("&8Power increase mob health and damage.");
        }});

        config.set("lang.arena.gui.startButton.material", Material.IRON_SWORD.name());
        config.set("lang.arena.gui.startButton.pos", 4);
        config.set("lang.arena.gui.startButton.name", "&cStart Arena");
        config.set("lang.arena.gui.startButton.lore", new ArrayList<>() {{
            add("");
            add("&7Power: &c%power%");
            add("&7Start wave: &e%startWave%");
            add("&7Crystal cost: &a%skipWaveCost%");
            add("");
            add("&7Creature Health: &a%creatureHealth%%");
            add("&7Creature Damage: &c%creatureDamage%%");
            add("");
            add("%group%");
        }});

        config.set("lang.arena.gui.mapChange.material", Material.MAP.name());
        config.set("lang.arena.gui.mapChange.pos", 6);
        config.set("lang.arena.gui.mapChange.name", "&cChange Map");
        config.set("lang.arena.gui.mapChange.lore", new ArrayList<>() {{
            add("&cWIP.");
        }});

        config.set("lang.arena.gui.startingWave.material", Material.CLOCK.name());
        config.set("lang.arena.gui.startingWave.pos", 13);
        config.set("lang.arena.gui.startingWave.name", "&eStarting Wave");
        config.set("lang.arena.gui.startingWave.lore", new ArrayList<>() {{
            add("");
            add("&7Starting Wave: &c%startWave%");
            add("&7Crystals Cost: &a%skipWaveCost%");
            add("");
            add("&eClick to add 5 waves");
            add("&cHold SHIFT to decrease");
        }});

        config.addDefault("lang.item.error.notEnoughPower", "You need more power to equip this item!");

        config.set("creature.health", "&a" + Symbols.HEALTH.get() + " &a%currentHealth%&7/%maxHealth%");
        config.set("creature.power", "&c" + Symbols.POWER.get() + " %power%");
        config.set("creature.amount", "&ex%amount%");

        config.set("spawner.title.typeAndPower", "%mobType% - &c" + Symbols.POWER.get() + " %fPower%");
        config.set("spawner.title.amount", "&e%amount%&7/%maxAmount%");

        config.set("lang.item.name", "%prefix%%name% %suffix%");
        config.set("lang.item.power", "&7Item Power: &c" + Symbols.POWER.get() + "%power%");
        config.set("lang.item.condition", "%condition%");


        config.set("itemPassif.moreDamage.strength", 0.25);
        config.set("itemPassif.moreDamage.name", "&6More Damage");
        config.set("itemPassif.moreDamage.description", new ArrayList<>() {{
            add("&7Increase all damage by &e%strength%%&7");
        }});

        config.set("itemPassif.explosion.strength", 0.40);
        config.set("itemPassif.explosion.name", "&6Explosion");
        config.set("itemPassif.explosion.description", new ArrayList<>() {{
            add("&7Create an explosion on your target damaging all");
            add("&7creature on 5 blocks for &e%strength%%&7 of the damage you dealt");
        }});

        config.set("itemPassif.damageReduction.strength", 0.05);
        config.set("itemPassif.damageReduction.name", "&6Rock Solide");
        config.set("itemPassif.damageReduction.description", new ArrayList<>() {{
            add("&7Reduce all incoming damage by &e%strength%%");
        }});

        config.set("itemPassif.thunderStorm.strength", 1.0);
        config.set("itemPassif.thunderStorm.name", "&bThunder Storm");
        config.set("itemPassif.thunderStorm.description", new ArrayList<>() {{
            add("&7Small chance to let rain a thunder storm to all");
            add("&7nearby creatures dealing &b%strength%%&7 of the armor power");
        }});

        config.set("itemPassif.lethalBlock.strength", 0.05);
        config.set("itemPassif.lethalBlock.name", "&6Lethal Block");
        config.set("itemPassif.lethalBlock.itemPrefixName", "Green's");
        config.set("itemPassif.lethalBlock.description", new ArrayList<>() {{
            add("&7Each time you block an attack you gain");
            add("&7a &6Lethal Block&7 stack which increase the");
            add("&7damage by &e%strength%% of received damage&7 for");
            add("&7each stack until you release the shield");
        }});

        config.set("itemPassif.deadlyLink.strength", 3);
        config.set("itemPassif.deadlyLink.name", "&6Deadly Link");
        config.set("itemPassif.deadlyLink.description", new ArrayList<>() {{
            add("&7Once you hit a creature. he's gonna link up to &e%strength% nearby");
            add("&ecreatures&7 around him for 10sec. Every attack on a");
            add("&7linked creature gonna deal &e55% of the damage&7 to all");
            add("&7other linkeds. After 10sec all linkeds creatures will");
            add("&7explode and dealing &e100% of the shared damages&7");
        }});

        config.set("itemPassif.lifeConversion.strength", 2.5);
        config.set("itemPassif.lifeConversion.name", "&6Life Conversion");
        config.set("itemPassif.lifeConversion.itemPrefixName", "Azellio's");
        config.set("itemPassif.lifeConversion.description", new ArrayList<>() {{
            add("&7Reduce your maximum health to &c1 HP&7 and");
            add("&7increase your damage by &e%strength%%&7.");
        }});
        config.set("itemPassif.lifeConversion.lore", new ArrayList<>() {{
            add("Who need health if they can't hit you...");
        }});

        config.set("pve.arena.rareDropChance", 0.01);
        config.set("pve.arena.perWaveRareDropChanceIncrease", 0.0001); // 1 wave = 0.01% added to rare drop chance

        config.options().copyDefaults(true);
        saveConfig();
    }

    public void initNewMenuIcons(){
        InventoryConfig islandMenu = IridiumSkyblock.getInstance().getInventories().islandMenu;
        Item missionItem = islandMenu.items.get("is missions");
        Item borderItem = islandMenu.items.get("is border");
        islandMenu.items.remove("is border");
        islandMenu.items.replace("is missions", new Item(XMaterial.FLOWER_BANNER_PATTERN, 4, 1, missionItem.displayName, missionItem.lore));
        islandMenu.items.put("arena", new Item(XMaterial.END_CRYSTAL, 22, 1, "&c&lArena", List.of("&7Fight creature in an arena!")));
        islandMenu.items.put("is shop", new Item(XMaterial.GOLD_INGOT, borderItem.slot, 1, "&b&lShop", List.of("&7Buy somethings for your island!")));
    }

    public void initNewUpgrade(){
        IridiumSkyblock.getInstance().getUpgradesList().remove("PowerUpgrade");
        IridiumSkyblock.getInstance().getInventories().upgradesGUI.size = 36;

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
                .put(1, new UpgradeData(0, 10))
                .put(2, new UpgradeData(0, 10))
                .put(3, new UpgradeData(0, 10))
                .put(4, new UpgradeData(0, 10))
                .put(5, new UpgradeData(0, 10))
                .build());
        IridiumSkyblockAPI.getInstance().addUpgrade("GeneratorSpeedUpgrade", generatorSpeedUpgrade);
    }

    private void initHologramAPI(){
        holoApi = HolographicDisplaysAPI.get(getPlugin(UnlimitedGrind.class));
    }

    private void initGlowingApi(){
        glowingEntities = new GlowingEntities(this);
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

    public GlowingEntities getGlowingEntities() {
        return glowingEntities;
    }
}
