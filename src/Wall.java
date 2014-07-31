import java.io.*;
import java.util.*;

/**
 * @author Nikolay Kuzan
 */
public class Wall {
    public static final String UTF8_BOM = "\uFEFF";
    public static final int MIN_BRICK_LENGTH = 1;
    public static final int MAX_BRICK_LENGTH = 8;
    public static final int MIN_COUNT_OF_BRICKS_SORTS = 1;

    public static void main(String[] args) {

        //Check if we have a file to open
        if (args.length == 0) {
            System.out.println("Please, specify the file name");
            return;
        }
        String fileName = args[0];

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));

            // Getting width and height of the wall
            Map<String, Integer> wallWidthAndHeight = getWidthAndHeight(reader);

            // Getting an array, which describes the shape of the wall expended in one "floor"
            int[] wallStructure = getWallStructure(reader, wallWidthAndHeight.get("width"), wallWidthAndHeight.get("height"));

            // Getting the count of different sorts(types) of bricks
            int countOfBricksSort = getCountOfBricksSorts(reader);

            // Getting the bricks
            Map<Integer, Integer> bricks = getBricks(reader, countOfBricksSort);

            // Verifying that the wall of specified configuration can be constructed from specified set of bricks
            // and printing result to standard output
            System.out.println(isPossibleConstructWall(wallStructure, wallWidthAndHeight.get("width"), bricks));

        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Can't find the file with data");
        } catch (IOException e) {
            System.out.println("ERROR: ");
        } catch (RuntimeException e) {
            System.out.println("ERROR: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("ERROR: Can't close the file with data");
                }
            }
        }
    }

    /**
     * Reads the width and height of wall's shape matrix from buffered reader
     * corresponding data file
     *
     * @param reader the BufferedReader from which read the incoming data
     * @return a map with width and height of wall's shape matrix.
     *         In this map key - "width" or "height",
     *                     value - specific value of the height and width
     * @throws IOException if an I/O error occurs
     * @throws RuntimeException if data format received from reader is incorrect
     */
    public static Map<String, Integer> getWidthAndHeight(BufferedReader reader) throws IOException {

        Map<String, Integer> wallWidthAndHeight = new HashMap<String, Integer>();
        String line = removeUTF8BOM(reader.readLine());

        String[] split = line.split(" ");

        if (split.length != 2 || line.endsWith(" ")) {
            throw new RuntimeException("Width and height of wall's shape matrix isn't two positive integers separated by space on their own line");
        }

        try {
            wallWidthAndHeight.put("width", Integer.parseInt(split[0]));
            wallWidthAndHeight.put("height", Integer.parseInt(split[1]));
        } catch (NumberFormatException e) {
            throw new RuntimeException("width or height of wall's shape matrix isn't positive numbers", e);
        }

        if (wallWidthAndHeight.get("width") <= 0 || wallWidthAndHeight.get("height") <= 0) {
            throw new RuntimeException("Width or height of wall's shape matrix is not positive numbers");
        }

        return wallWidthAndHeight;
    }

    /**
     * Reads the wall configuration into an array of 1 and 0
     * A configuration of the wall may be any, but it should be described by a matrix
     * with specified width and height and is composed of '0' and '1'
     *
     * @param reader the BufferedReader from which read the incoming data
     * @param width specified in input data width of the wall
     * @param height specified in input data height of the wall
     *
     * @return an array of integers, which describes the shape of the wall expended in one "floor"
     * @throws IOException if an I/O error occurs
     * @throws RuntimeException if wall structure is defined incorrectly
     */
    public static int[] getWallStructure(BufferedReader reader, int width, int height) throws IOException {

        //wall configuration expends to one dimensional array
        int[] wallStructure = new int[width * height];

        List<Integer> wallStructureList = new ArrayList<Integer>();

        for (int i = 0; i < height; i++) {
            String wallLine = reader.readLine();

            //if we have not enough information, and the file has ended
            if (wallLine == null) {
                throw new RuntimeException("Wall structure is defined incorrectly");
            } else if (wallLine.length() != width) {
                throw new RuntimeException("Wall structure is defined incorrectly");
            }

            for (int j = 0; j < wallLine.length(); j++) {
                char c = wallLine.charAt(j);
                //configuration of the wall must be described only by '1' and '0' symbols
                if (c == '1' || c == '0') {
                    wallStructureList.add(Character.getNumericValue(c));
                } else {
                    throw new RuntimeException("Wall's shape matrix isn't formed just of '1' and '0' symbols");
                }
            }
        }

        for (int i = 0; i < wallStructureList.size(); i++) {
            wallStructure[i] = wallStructureList.get(i);
        }
        return wallStructure;
    }

    /**
     * Reads the count of bricks' sorts - the positive integer
     *
     * @param reader the BufferedReader from which read the incoming data
     * @return count of bricks sorts (types)
     * @throws IOException if an I/O error occurs
     * @throws RuntimeException if count of bricks sorts has incorrect format
     */
    public static int getCountOfBricksSorts(BufferedReader reader) throws IOException {
        int countOfBricksSorts;

        try {
            countOfBricksSorts = Integer.parseInt(reader.readLine());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Line with the count of bricks' sorts is incorrect", e);
        }

        //count of bricks sorts should be positive integer
        if (countOfBricksSorts < MIN_COUNT_OF_BRICKS_SORTS) {
            throw new RuntimeException("The count of bricks' sorts isn't correct value");
        }
        return countOfBricksSorts;
    }

    /**
     * Reads the description of the bricks, which are available.
     *
     * Input data from file contains specific number of lines each of which has follow format:
     * "lengthOfBrick countOfBricksWithThisLength"
     * Each brick has the linear form and discrete length from 1 to 8
     * This method reads this description and forms the map where:
     *      key   - length of brick
     *      value - count of bricks with this length
     *
     * @param reader the BufferedReader from which read the incoming data
     * @param countOfBricksSort the count of different types of bricks
     *
     * @return the map of lines each containing two positive integers separated by space -
     *         length of brick and count of such the bricks in the set
     *
     * @throws IOException if an I/O error occurs
     * @throws RuntimeException if lines of length of brick and count of such the bricks
     *         in the set have incorrect format
     */
    public static Map<Integer, Integer> getBricks(BufferedReader reader, int countOfBricksSort) throws IOException {

        // We use TreeMap with reverse order because for the construction of the wall
        // in the first place will be used large bricks, and then small
        Map<Integer, Integer> bricksMap = new TreeMap<Integer, Integer>(Collections.reverseOrder());

        for (int i = 0; i < countOfBricksSort; i++) {
            String line = reader.readLine();

            //if we have not enough information, and the file has ended
            if (line == null) {
                throw new RuntimeException("Data format from file is incorrect");
            }

            String[] split = line.split(" ");
            if (split.length != 2 || line.endsWith(" ")) {
                throw new RuntimeException("List of bricks has incorrect format.\n" +
                        "       Each line should contain two positive integers separated by space");
            }

            try {
                int lengthOfBrick = Integer.parseInt(split[0]);
                int countOfBricks = Integer.parseInt(split[1]);

                // Each brick has the linear form and discrete length from 1 to 8
                // Count of bricks each type can not be less then 1
                if (lengthOfBrick < MIN_BRICK_LENGTH || lengthOfBrick > MAX_BRICK_LENGTH || countOfBricks <= 0) {
                    throw new NumberFormatException();
                }
                // Description of the bricks set can not contains two or more lines with the same length of bricks
                if (bricksMap.containsKey(lengthOfBrick)) {
                    throw new RuntimeException("List of bricks has incorrect format (few lines with the same length of brick)");
                }
                bricksMap.put(lengthOfBrick, countOfBricks);

            } catch (NumberFormatException e) {
                throw new RuntimeException("Each line should contain two positive integers separated by space.\n" +
                        "       Brick’s length can be from " + MIN_BRICK_LENGTH + " to " + MAX_BRICK_LENGTH, e);
            }
        }
        //if lines more then countOfBricksSort

        if (reader.ready()){
            throw new RuntimeException("List of bricks has incorrect format.\n" +
                    "       Count of lines should be " + countOfBricksSort);
        }

        return bricksMap;
    }

    /**
     * Verifies if wall of some configuration can be constructed from some set of bricks.
     *
     * Construction of the wall as follows:
     * - From the map "bricks" taken the type of bricks with the largest length.
     * - In the array, which describes the shape of the wall expended in one "floor", for every brick of this type
     *   looking for a place.
     * - If such place is found - brick is removed from the set of available bricks and elements of wall's array, that
     *   match the brick, filled with the value "-1".
     * - If there is another bricks of the same size in the set, then looking for a place for them in the wall's array
     * - If there are no bricks of the same size in the set, we take smaller bricks.
     * - Operation of looking for a place is repeated for all the bricks in the set.
     * - After the all the bricks have been tested for the possibility of their use, the array representing the wall analyzed:
     *      - if the array contains the value "1" (the cells in the wall that were not filled with bricks), the method
     *        returns the string "no" (wall can not be constructed);
     *      - if the array contains no value "1" (all cells are filled with bricks), the method returns
     *        the string "yes" (wall can be constructed);
     *
     *
     * @param wallStructure an array of integers, which describes the shape of the wall expended in one "floor"
     * @param wallWidth width of the wall to be built
     * @param bricks the map of lines each containing two positive integers separated by space -
     *        length of brick and count of such the bricks in the set
     *
     * @return "yes" if wall can be constructed
     *         "no"  if wall can not be constructed
     */
    public static String isPossibleConstructWall(int[] wallStructure, int wallWidth, Map<Integer, Integer> bricks) {

        for (Map.Entry<Integer, Integer> entry : bricks.entrySet()) {
            int brickSize = entry.getKey();
            int countOfBricks = entry.getValue();

            int begin = -1, end;
            int countOfAvailablePixels = 0;

            for (int i = 0; i < wallStructure.length; i++) {
                if (wallStructure[i] == 1) {
                    if (begin == -1) {
                        begin = i;
                    }
                    countOfAvailablePixels++;
                    if (countOfAvailablePixels == brickSize) {
                        end = i;
                        if (countOfBricks > 0) {
                            for (int j = begin; j <= end; j++) {
                                wallStructure[j] = -1;
                            }
                            begin = -1;
                            countOfAvailablePixels = 0;
                            countOfBricks--;
                        }
                    }
                } else if (wallStructure[i] == 0) {
                    begin = -1;
                    countOfAvailablePixels = 0;
                }

                if ((i % wallWidth) == (wallWidth - 1)) {
                    begin = -1;
                    countOfAvailablePixels = 0;
                }
            }
        }
        for (int i = 0; i < wallStructure.length; i++) {
            if (wallStructure[i] == 1) return "no";
        }
        return "yes";
    }

    /**
     * Remove UTF8_BOM symbol from string which was obtained from a text file
     *
     * @param s string which may contain UTF8_BOM symbol
     * @return string without UTF8_BOM symbol
     */
    private static String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }
}