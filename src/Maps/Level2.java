package Maps;

import Enemies.BugEnemy;
import Enemies.DinosaurEnemy;
import Engine.ImageLoader;
import EnhancedMapTiles.DoubleJump1;

import EnhancedMapTiles.EndLevelBoxL2;
import EnhancedMapTiles.KeyL1;
import EnhancedMapTiles.Keypad;
import EnhancedMapTiles.HorizontalMovingPlatform;
import GameObject.Rectangle;
import Level.*;
import Tilesets.CommonTileset;
import Utils.Direction;
import EnhancedMapTiles.Sprint1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Represents a test map to be used in a level
public class Level2 extends Map {

    public Level2() {
        super("Level2.txt", new CommonTileset());
        this.playerStartPosition = getMapTile(2, 11).getLocation();
    }

    public void reloadMapFromFile() {
        loadMapFile();
    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();

        BugEnemy bugEnemy = new BugEnemy(getMapTile(16, 10).getLocation().subtractY(25), Direction.LEFT);
        enemies.add(bugEnemy);

        /*
         * DinosaurEnemy dinosaurEnemy = new DinosaurEnemy(getMapTile(19,
         * 1).getLocation().addY(2), getMapTile(22, 1).getLocation().addY(2),
         * Direction.RIGHT);
         * enemies.add(dinosaurEnemy);
         */

        return enemies;
    }

    public static void replaceAllWallTilesInFile() throws IOException {
        // path
        String path = "MapFiles/Level2.txt";

        // Read the map file using the updated path
        List<String> lines = Files.readAllLines(Paths.get(path));

        // Modify the lines by replacing all 17 tiles with 7
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            line = line.replaceAll(" 54 ", " 7 "); // Replace all 54 tiles with 7
            lines.set(i, line);
        }

        // Write the modified map back to the file
        Files.write(Paths.get(path), lines);

    }

    public static void resetMapToFile() throws IOException {
        // Path to the backup map
        String backupPath = "MapFiles/Level2backup.txt";
        String targetPath = "MapFiles/Level2.txt";

        // Read the backup map
        List<String> backupLines = Files.readAllLines(Paths.get(backupPath));

        // Overwrite the target map with the backup map's content
        Files.write(Paths.get(targetPath), backupLines);
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        HorizontalMovingPlatform hmp = new HorizontalMovingPlatform(
                ImageLoader.load("GreenPlatform.png"),
                getMapTile(23, 10).getLocation(),
                getMapTile(26, 10).getLocation(),
                TileType.JUMP_THROUGH_PLATFORM,
                3,
                new Rectangle(0, 6, 16, 4),
                Direction.RIGHT);
        enhancedMapTiles.add(hmp);

        HorizontalMovingPlatform hmp1 = new HorizontalMovingPlatform(
                ImageLoader.load("GreenPlatform.png"),
                getMapTile(25, 4).getLocation(),
                getMapTile(27, 4).getLocation(),
                TileType.JUMP_THROUGH_PLATFORM,
                3,
                new Rectangle(0, 6, 16, 4),
                Direction.RIGHT);
        enhancedMapTiles.add(hmp1);

        EndLevelBoxL2 endLevelBoxL2 = new EndLevelBoxL2(getMapTile(42, 12).getLocation());
        enhancedMapTiles.add(endLevelBoxL2);

        KeyL1 keyL1 = new KeyL1(getMapTile(2, 5).getLocation());
        keyL1.setMapReference(this); // Set the reference to this map
        enhancedMapTiles.add(keyL1);

        Sprint1 key2L1 = new Sprint1(getMapTile(12, 17).getLocation());
        key2L1.setMapReference(this); // Set the reference to this map
        enhancedMapTiles.add(key2L1);

        // DoubleJump key3L1 = new DoubleJump(getMapTile(12, 17).getLocation());
        // key3L1.setMapReference(this); // Set the reference to this map
        // enhancedMapTiles.add(key3L1);

        Keypad keypad = new Keypad(getMapTile(36, 13).getLocation());
        keypad.setMapReference(this); // Set the reference to this map
        enhancedMapTiles.add(keypad);

        return enhancedMapTiles;

    }


}
