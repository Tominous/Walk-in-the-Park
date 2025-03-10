package dev.efnilite.witp.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.efnilite.witp.WITP;
import dev.efnilite.witp.schematic.Vector3D;
import dev.efnilite.witp.util.config.Configuration;
import dev.efnilite.witp.util.config.Option;
import dev.efnilite.witp.util.task.Tasks;
import dev.efnilite.witp.util.wrapper.EventWrapper;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.messaging.ChannelNotRegisteredException;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * General utilities
 *
 * @author Efnilite
 */
public class Util {

    private static Economy economy;
    private static final char[] OID = "1234567890abcdefghijklmnopqrstuvwxyz".toCharArray(); // Online IDentifier
    private static final char[] RANDOM_DIGITS = "1234567890".toCharArray();

    /**
     * Returns a zero'd location.
     *
     * @return a location where everything is 0
     */
    public static Location zero() {
        return new Location(null, 0, 0, 0);
    }

    /**
     * Returns the active void generator plugin
     *
     * @return the void generator currently in use
     */
    public static @Nullable String getVoidGenerator() {
        if (Bukkit.getPluginManager().getPlugin("WVoidGen") != null) {
            return "WVoidGen";
        } else if (Bukkit.getPluginManager().getPlugin("VoidGen") != null) {
            return "VoidGen";
        } else {
            return null;
        }
    }

    /**
     * Random ID for game logging
     *
     * @return a string with a random ID
     */
    public static String randomOID() {
        StringBuilder random = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            random.append(OID[ThreadLocalRandom.current().nextInt(OID.length - 1)]);
        }
        return random.toString();
    }

    /**
     * Random digits
     *
     * @return a string with an amount of random digits
     */
    public static String randomDigits(int amount) {
        StringBuilder random = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            random.append(RANDOM_DIGITS[ThreadLocalRandom.current().nextInt(RANDOM_DIGITS.length - 1)]);
        }
        return random.toString();
    }

    /**
     * Gets the difficulty of a schematic according to schematics.yml
     *
     * @param   fileName
     *          The name of the file (parkour-x.nbt)
     *
     * @return the difficulty, ranging from 0 to 1
     */
    public static double getDifficulty(String fileName) {
        int index = Integer.parseInt(fileName.split("-")[1].replaceAll(".witp", ""));
        return WITP.getConfiguration().getFile("structures").getDouble("difficulty." + index);
    }

    public static String parseDifficulty(double difficulty) {
        if (difficulty > 1) {
            Verbose.error("Invalid difficuly, above 1: " + difficulty);
            return "unknown";
        }
        if (difficulty <= 0.3) {
            return "easy";
        } else if (difficulty <= 0.5) {
            return "medium";
        } else if (difficulty <= 0.7) {
            return "hard";
        } else if (difficulty >= 0.8) {
            return "very hard";
        } else {
            return "unknown";
        }
    }

    /**
     * Sorts a HashMap by value
     * Source: https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values
     *
     * @return a sorted HashMap
     */
    public static <K, V extends Comparable<? super V>> HashMap<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        HashMap<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * Deposits money to a player using Vault
     *
     * @param   player
     *          The player
     *
     * @param   amount
     *          The amount
     */
    public static void depositPlayer(Player player, double amount) {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            if (economy == null) {
                RegisteredServiceProvider<Economy> service = Bukkit.getServicesManager().getRegistration(Economy.class);
                if (service != null) {
                    economy = service.getProvider();
                    economy.depositPlayer(player, amount);
                } else {
                    Verbose.error("There was an error while trying to fetch the Vault economy!");
                }
                return;
            }
            economy.depositPlayer(player, amount);
        }
    }

    /**
     * Gets the direction from a facing (e.g. north, south, west)
     *
     * @param   face
     *          The string direction (north, south, east and west)
     *
     * @return a vector that indicates the direction
     */
    public static Vector getDirection(String face) {
        switch (face.toLowerCase()) {
            case "north":
                return new Vector(0, 0, -1);
            case "south":
                return new Vector(0, 0, 1);
            case "east":
                return new Vector(1, 0, 0);
            case "west":
                return new Vector(-1, 0, 0);
            default:
                Verbose.error("Invalid direction (direction used: " + face + ")");
                return new Vector(1, 0, 0);
        }
    }

    /**
     * Gets the chunks from a BoundingBox
     *
     * @param   box
     *          The BoundingBox
     *
     * @param   world
     *          Za warudo
     *
     * @return the list of chunks
     */
    public static List<Chunk> getChunks(BoundingBox box, World world) {
        List<Chunk> chunks = new ArrayList<>();
        Location mal = box.getMax().toLocation(world);
        Location mil = box.getMin().toLocation(world);

        int mix = mil.getChunk().getX();
        int max = mal.getChunk().getX();
        int miz = mil.getChunk().getZ();
        int maz = mal.getChunk().getZ();

        for (int x = mix; x <= max; x++) {
            for (int z = miz; z <= maz; z++) {
                chunks.add(world.getChunkAt(x, z));
            }
        }
        return chunks;
    }

    /**
     * Calls an event
     *
     * @param   wrapper
     *          The event instance
     */
    public static boolean callEvent(EventWrapper wrapper) {
        return wrapper.call();
    }

    /**
     * Sends a player to a BungeeCord server
     *
     * @param   player
     *          The player to be sent
     *
     * @param   server
     *          The server name
     */
    public static void sendPlayer(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        try {
            player.sendPluginMessage(WITP.getInstance(), "BungeeCord", out.toByteArray());
        } catch (ChannelNotRegisteredException ex) {
            Verbose.error("Tried to send " + player.getName() + " to server " + server + " but this server is not registered!");
            player.kickPlayer("There was an error while trying to move you to server " + server + ", please rejoin.");
        }
    }

    /**
     * Gets the size of a ConfigurationSection
     *
     * @param   file
     *          The file
     *
     * @param   path
     *          The path
     *
     * @return the size
     */
    public static @Nullable List<String> getNode(FileConfiguration file, String path) {
        ConfigurationSection section = file.getConfigurationSection(path);
        if (section == null) {
            return null;
        }
        return new ArrayList<>(section.getKeys(false));
    }

    /**
     * Color something
     */
    public static String color(String string) {
        if (!string.equals("")) {
            return ChatColor.translateAlternateColorCodes('&', HexColours.translate(string));
        }
        return string;
    }

    /**
     * Colors strings (uses & as color marker)
     *
     * @param   strings
     *          The strings
     *
     * @return the strings
     */
    public static String[] color(String... strings) {
        String[] ret = new String[strings.length];
        int i = 0;
        for (String string : strings) {
            ret[i++] = Util.color(string);
        }
        return ret;
    }

    /**
     * Color a list of strings (uses & as color marker)
     *
     * @param   strings
     *          The string to be colored
     *
     * @return the strings, but colored
     */
    public static List<String> color(List<String> strings) {
        List<String> ret = new ArrayList<>();
        for (String string : strings) {
            ret.add(Util.color(string));
        }
        return ret;
    }

    /**
     * Parses a String of locations to a list of Location objects
     *
     * @param   string
     *          The string
     *
     * @return the list of Location objects
     */
    public static List<Location> parseLocations(String string) {
        String[] split = string.split("->");
        List<Location> locs = new ArrayList<>();
        for (String s : split) {
            locs.add(Util.parseLocation(s));
        }
        return locs;
    }

    /**
     * Capitalizes the first letter in a word
     *
     * @param   string
     *          The string
     *
     * @return the string but with 1st letter capitalized
     */
    public static String capitalizeFirst(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    /**
     * Formats ints with dots (1000 -> 1.000)
     *
     * @param   value
     *          The int
     *
     * @return the int but with seperation dots
     */
    public static String formatInt(int value) {
        return String.format("%,d", value);
    }

    /**
     * Checks if the cuboid box contains any Material other then the given one
     *
     * @param   position
     *          The first position
     *
     * @param   position2
     *          The second position
     *
     * @param   material
     *          The material
     *
     * @return true -> the box contains something other then the given material
     */
    public static boolean excludeMaterial(Location position, Location position2, Material material) {
        World w = position.getWorld();
        Location location = new Location(w, 0, 0, 0);
        int max = Math.max(position.getBlockX(), position2.getBlockX());
        int mix = Math.min(position.getBlockX(), position2.getBlockX());
        int may = Math.max(position.getBlockY(), position2.getBlockY());
        int miy = Math.min(position.getBlockY(), position2.getBlockY());
        int maz = Math.max(position.getBlockZ(), position2.getBlockZ());
        int miz = Math.min(position.getBlockZ(), position2.getBlockZ());
        for (int x = mix; x <= max; x++) {
            for (int y = miy; y <= may; y++) {
                for (int z = miz; z <= maz; z++) {
                    location.setX(x);
                    location.setY(y);
                    location.setZ(z);

                    if (location.getBlock().getType() != material) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Gets the blocks between 2 locations
     *
     * @param   position
     *          The first position
     *
     * @param   position2
     *          The second position
     *
     * @return the locations of all the blocks between the positions
     */
    public static List<Block> getBlocks(Location position, Location position2) {
        World w = position.getWorld();
        List<Block> add = new ArrayList<>();
        Location location = new Location(w, 0, 0, 0);
        int max = Math.max(position.getBlockX(), position2.getBlockX());
        int mix = Math.min(position.getBlockX(), position2.getBlockX());
        int may = Math.max(position.getBlockY(), position2.getBlockY());
        int miy = Math.min(position.getBlockY(), position2.getBlockY());
        int maz = Math.max(position.getBlockZ(), position2.getBlockZ());
        int miz = Math.min(position.getBlockZ(), position2.getBlockZ());
        for (int x = mix; x <= max; x++) {
            for (int y = miy; y <= may; y++) {
                for (int z = miz; z <= maz; z++) {
                    location.setX(x);
                    location.setY(y);
                    location.setZ(z);

                    if (location.getBlock().getType() != Material.AIR) {
                        add.add(location.clone().getBlock());
                    }
                }
            }
        }
        return add;
    }

    public static void getBlocksAsync(Location pos1, Location pos2, Consumer<List<Block>> consumer) {
        Tasks.asyncTask(() -> consumer.accept(getBlocks(pos1, pos2)));
    }

    /**
     * Reverses a boolean in a string
     */
    public static String reverseBoolean(String first) {
        if (first.contains("true")) {
            return "false";
        } else if (first.contains("false")) {
            return "true";
        } else {
            throw new IllegalStateException("Boolean value does not contain false or true (Util#reverseBoolean)");
        }
    }

    /**
     * Color a boolean (true = green, false = red)
     */
    public static String colorBoolean(String string) {
        if (string.contains("true")) {
            return Util.color("&atrue");
        } else if (string.contains("false")) {
            return Util.color("&cfalse");
        } else {
            throw new IllegalStateException("Boolean value is not false or true (Util#colorBoolean)");
        }
    }

    /**
     * Is a location in a cuboid?
     */
    public static boolean isInCuboid(BoundingBox box, Location location) {
        return box.clone().expand(0.1, 0.1, 0.1).contains(location.toVector());
    }

    /**
     * Gets the max of the locations
     *
     * @param   pos1
     *          The first location
     *
     * @param   pos2
     *          The second location
     *
     * @return  the max values of the locations
     */
    public static Location max(Location pos1, Location pos2) {
        World world = pos1.getWorld() == null ? pos2.getWorld() : pos1.getWorld();
        return new Location(world, Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
    }

    /**
     * Gets the min of the locations
     *
     * @param   pos1
     *          The first location
     *
     * @param   pos2
     *          The second location
     *
     * @return  the min values of the locations
     */
    public static Location min(Location pos1, Location pos2) {
        World world = pos1.getWorld() == null ? pos2.getWorld() : pos1.getWorld();
        return new Location(world, Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
    }

    public static Location min(List<Location> positions) {
        Location min = new Location(null, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        for (Location position : positions) {
            if (position.getWorld() != null) {
                min.setWorld(position.getWorld());
            }
            min.setX(Math.min(position.getX(), min.getX()));
            min.setY(Math.min(position.getY(), min.getY()));
            min.setZ(Math.min(position.getZ(), min.getZ()));
        }
        return min;
    }

    public static Location max(List<Location> positions) {
        Location max = new Location(null, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
        for (Location position : positions) {
            if (position.getWorld() != null) {
                max.setWorld(position.getWorld());
            }
            max.setX(Math.max(position.getX(), max.getX()));
            max.setY(Math.max(position.getY(), max.getY()));
            max.setZ(Math.max(position.getZ(), max.getZ()));
        }
        return max;
    }

    /**
     * Gets the player's held item
     *
     * @param   player
     *          The player
     *
     * @return the player's held item
     */
    public static ItemStack getHeldItem(Player player) {
        PlayerInventory inventory = player.getInventory();
        return inventory.getItemInMainHand().getType() == Material.AIR ? inventory.getItemInOffHand() : inventory.getItemInMainHand();
    }

    /**
     * Gets a Vector from a String
     *
     * @param   vector
     *          The Vector in String format
     *
     * @return the Vector
     */
    public static Vector3D parseVector(String vector) {
        String[] split = vector.replaceAll("[()]", "").split(",");
        return new Vector3D(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }

    /**
     * Creates a string version of a Location.
     *
     * @param   location
     *          The location
     *
     * @return string version
     */
    public static String toString(Location location, boolean formatted) {
        if (!formatted) {
            return "(" + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getWorld().getName() + ")";
        } else {
            return "(" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")";
        }
    }

    /**
     * Send a text to a player from the lang.yml file in the default language
     * (if the player isn't a {@link dev.efnilite.witp.player.ParkourUser}, knowing their preferred language is impossible)
     *
     * @param   player
     *          The player
     *
     * @param   path
     *          The path
     */
    public static void sendDefaultLang(Player player, String path, String... replaceable) {
        String message = WITP.getConfiguration().getString("lang", "messages." + Option.DEFAULT_LANG + "." + path);
        if (message == null) {
            Verbose.error("Path " + path + " has no message in language " + Option.DEFAULT_LANG + "!");
            return;
        }
        for (String s : replaceable) {
            message = message.replaceFirst("%[a-z]", s);
        }
        player.sendMessage(message);
    }

    public static String getDefaultLang(String path) {
        String message = WITP.getConfiguration().getString("lang", "messages." + Option.DEFAULT_LANG + "." + path);
        if (message == null) {
            Verbose.error("Path " + path + " has no message in language " + Option.DEFAULT_LANG + "!");
            return "";
        }
        return message;
    }

    /**
     * Creates a string version of a Location.
     *
     * @param   vector
     *          The location
     *
     * @return string version
     */
    public static String toString(Vector vector) {
        return "(" + vector.getBlockX() + "," + vector.getBlockY() + "," + vector.getBlockZ() + ")";
    }

    /**
     * Get a location from a string
     *
     * @param   location
     *          The string
     *
     * @return the location from the string
     */
    public static Location parseLocation(String location) {
        String[] values = location.replaceAll("[()]", "").replaceAll(", ", " ").replaceAll(",", " ").split(" ");
        World world = Bukkit.getWorld(values[3]);
        if (world == null) {
            Verbose.error("Detected an invalid world: " + values[3]);
            return new Location(Bukkit.getWorlds().get(0), Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]));
        }
        return new Location(Bukkit.getWorld(values[3]), Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]));
    }

    /**
     * Gets the NMS version
     *
     * @return the nms version
     */
    public static String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }
}