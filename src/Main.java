package battleship;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static class Player {
        public final int number;

        public final Board board = new Board();

        public Player(int number) {
            this.number = number;
        }
    }

    private static class Board {
        public static final int BOARD_HEIGHT = 10;
        public static final int BOARD_WIDTH = 10;

        public final Location[][] locations = new Location[BOARD_HEIGHT][BOARD_WIDTH];

        public final ArrayList<Ship> ships = new ArrayList<Ship>();

        public int shipLocationsHit = 0;
        public int shipLocationsTotal = 0;

        public Board() {
        }
    }

    private static class Location {
        public Ship ship;

        public String status;
        public static String STATUS_NOT_CHECKED = "O";
        public static String STATUS_HIT = "X";
        public static String STATUS_MISS = "M";

        public static String CONTENTS_UNKNOWN = "~";

        public Location() {
            this.ship = new Ship(Location.CONTENTS_UNKNOWN);
            this.status = Location.STATUS_NOT_CHECKED;
        }
    }

    private static class Ship {
        public final String name; // "~" for unknown/no ship or the ship type name.

        public int locationsHit = 0;
        public int locationsTotal = 0;

        public Ship(String name) {
            this.name = name;
        }

        public Ship(String name, int locationsTotal) {
            this.name = name;
            this.locationsTotal = locationsTotal;
        }
    }

    private static final String MESSAGE_COORDINATE_ERROR = "You entered the wrong coordinates!";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Player[] players = new Player[3]; // Not using index 0.

        // Set up player 1.
        players[1] = new Player(1);
        setUpPlayer(scanner, players[1]);

        // Wait for user to press Enter and then clear screen.
        System.out.println();
        waitForEnter(scanner);

        // Set up player 2.
        players[2] = new Player(2);
        setUpPlayer(scanner, players[2]);

        // Wait for user to press Enter and then clear screen.
        System.out.println();
        waitForEnter(scanner);

        // Loop until a player wins.
        boolean won = false;
        while (true) {
            // Get player 1 move.
            won = enterMove(scanner, 1, players);
            if (won) {
                break;
            }

            // Get player 2 move.
            won = enterMove(scanner, 2, players);
            if (won) {
                break;
            }
        }

        System.out.println();
        System.out.println("Game Over");
    }

    private static void setUpPlayer(Scanner scanner, Player player) {
        System.out.printf("Player %d, place your ships on the game field", player.number);
        System.out.println();
        System.out.println();

        // Reset board and print to console.
        resetBoard(player.board);
        printBoard(player.board, false);

        // Initialize ships.
        initializeShips(player.board);

        // Have user place all ships on board.
        for (Ship ship : player.board.ships) {
            enterShipPosition(scanner, ship, player.board);
            System.out.println();
            printBoard(player.board, false);
        }
    }

    private static void resetBoard(Board board) {
        // Reset all locations on board to no ship state.
        for (int row = 0; row < Board.BOARD_HEIGHT; row++) {
            for (int column = 0; column < Board.BOARD_WIDTH; column++) {
                board.locations[row][column] = new Location();
            }
        }
    }

    private static void printBoard(Board board, boolean fogOfWar) {
        // Print board to console.
        StringBuilder line = new StringBuilder("  ");
        for (int column = 0; column < Board.BOARD_WIDTH; column++) {
            line.append(column + 1).append(" ");
        }
        System.out.println(line);
        for (int row = 0; row < Board.BOARD_HEIGHT; row++) {
            line = new StringBuilder((char) (row + 65) + " ");
            for (int column = 0; column < Board.BOARD_WIDTH; column++) {
                line.append(getLocationDisplayValue(board.locations[row][column], fogOfWar)).append(" ");
            }
            System.out.println(line);
        }
    }

    private static String getLocationDisplayValue(Location location, boolean fogOfWar) {
        // Determine what to show for location and return.
        if (fogOfWar) {
            if (location.status.equals(Location.STATUS_NOT_CHECKED)) {
                return Location.CONTENTS_UNKNOWN;
            } else {
                return location.status;
            }
        } else {
            if (location.ship.name.equals(Location.CONTENTS_UNKNOWN) && location.status.equals(Location.STATUS_NOT_CHECKED)) {
                return Location.CONTENTS_UNKNOWN;
            } else {
                return location.status;
            }
        }
    }

    private static void initializeShips(Board board) {
        // Add available ships in prompt order.
        board.ships.add(new Ship("Aircraft Carrier", 5));
        board.ships.add(new Ship("Battleship", 4));
        board.ships.add(new Ship("Submarine", 3));
        board.ships.add(new Ship("Cruiser", 3));
        board.ships.add(new Ship("Destroyer", 2));

        // Set number of board locations covered by ships.
        for (Ship ship : board.ships) {
            board.shipLocationsTotal += ship.locationsTotal;
        }
    }

    private static void enterShipPosition(Scanner scanner, Ship ship, Board board) {
        String prompt = String.format("Enter the coordinates of the %s (%d cells):", ship.name, ship.locationsTotal);

        // Loop until ship is placed.
        String[] coordinates;
        while (true) {
            // Enter ship coordinates.
            System.out.println();
            System.out.println(prompt);
            System.out.println();
            coordinates = scanner.nextLine().toUpperCase().split(" ");

            // Verify coordinates are valid.
            if (coordinates.length != 2) {
                prompt = getErrorMessage(MESSAGE_COORDINATE_ERROR);
                continue;
            }

            prompt = "";
            for (String coordinate : coordinates) {
                prompt = isCoordinateValid(coordinate);
                if (!prompt.equals("")) {
                    break;
                }
            }
            if (!prompt.equals("")) {
                continue;
            }

            // Verify ship placement is valid and place ship if valid.
            prompt = placeShipOnBoard(coordinates, ship, board);
            if (!prompt.equals("")) {
                continue;
            }

            break;
        }
    }

    private static String placeShipOnBoard(String[] coordinates, Ship ship, Board board) {
        // Convert coordinates to rows and columns. (Not zero based yet.)
        int row1 = coordinates[0].charAt(0) - 64;
        int column1 = Integer.parseInt(coordinates[0].substring(1));

        int row2 = coordinates[1].charAt(0) - 64;
        int column2 = Integer.parseInt(coordinates[1].substring(1));

        // Figure out if row or column is the same.
        int start;
        int end;
        if (row1 == row2) {
            // If row and column are both the same, we have a problem.
            if (column1 == column2) {
                return getErrorMessage("Wrong ship location!");
            // Ship is horizontal.
            } else {
                // Sort columns.
                if (column1 > column2) {
                    start = column2;
                    end = column1;
                } else {
                    start = column1;
                    end = column2;
                }
                // Verify coordinates produce proper ship length.
                start--;
                if ((end - start) == ship.locationsTotal) {
                    // Verify ship fits on board. (Rules are non-standard. See spec.)
                    for (int column = start; column < end; column++) {
                        if (!doesShipFit(row1 - 1, column, ship, board)) {
                            return getErrorMessage("You placed it too close to another one.");
                        }
                    }
                    // Place ship on board.
                    for (int column = start; column < end; column++) {
                        board.locations[row1 - 1][column].ship = ship;
                    }
                } else {
                    return getErrorMessage(String.format("Wrong length of the %s!", ship.name));
                }
            }
        } else {
            // Ship is vertical.
            if (column1 == column2) {
                // Sort rows.
                if (row1 > row2) {
                    start = row2;
                    end = row1;
                } else {
                    start = row1;
                    end = row2;
                }
                // Verify coordinates produce proper ship length.
                start--;
                if ((end - start) == ship.locationsTotal) {
                    // Verify ship fits on board. (Rules are non-standard. See spec.)
                    for (int row = start; row < end; row++) {
                        if (!doesShipFit(row, column1 - 1, ship, board)) {
                            return getErrorMessage("You placed it too close to another one.");
                        }
                    }
                    // Place ship on board.
                    for (int row = start; row < end; row++) {
                        board.locations[row][column1 - 1].ship = ship;
                    }
                } else {
                    return getErrorMessage(String.format("Wrong length of the %s!", ship.name));
                }
            // If row and column are both different, we have a problem.
            } else {
                return getErrorMessage("Wrong ship location!");
            }
        }

        return "";
    }

    private static boolean doesShipFit(int locationRow, int locationColumn, Ship ship, Board board) {
        // Verify location is available and there is no adjacent ship.
        for (int row = locationRow - 1; row <= locationRow + 1; row++) {
            if ((row >= 0) && (row < Board.BOARD_HEIGHT)) {
                for (int column = locationColumn - 1; column <= locationColumn + 1; column++) {
                    if ((column >= 0) && (column < Board.BOARD_WIDTH)) {
                        if ((!board.locations[row][column].ship.name.equals(Location.CONTENTS_UNKNOWN)) && (!(board.locations[row][column].ship == ship))) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private static boolean enterMove(Scanner scanner, int playerNumber, Player[] players) {
        // Determine what boards to display at top and bottom.
        int top;
        int bottom;
        if (playerNumber == 1) {
            top = 2;
            bottom = 1;
        } else {
            top = 1;
            bottom = 2;
        }

        // Print boards to console.
        printBoard(players[top].board, true);
        System.out.println("---------------------");
        printBoard(players[bottom].board, false);

        String prompt = String.format("Player %d, it's your turn:", playerNumber);

        // Loop until coordinate is valid.
        String coordinate;
        while (true) {
            // Enter shot coordinate.
            System.out.println();
            System.out.println(prompt);
            System.out.println();
            coordinate = scanner.nextLine().toUpperCase();

            // Verify coordinate is valid.
            prompt = isCoordinateValid(coordinate);
            if (!prompt.equals("")) {
                continue;
            }

            break;
        }

        // Handle shot.
        boolean won = handleShot(coordinate, playerNumber, players);

        if (!won) {
            // Wait for user to press Enter and then clear screen.
            waitForEnter(scanner);
        }

        return won;
    }

    private static boolean handleShot(String coordinate, int playerNumber, Player[] players) {
        boolean won = false;

        // Determine board to update.
        int defender;
        Board board;
        if (playerNumber == 1) {
            defender = 2;
        } else {
            defender = 1;
        }
        board = players[defender].board;

        // Convert coordinate to row and column. (Not zero based yet.)
        int row = coordinate.charAt(0) - 64;
        int column = Integer.parseInt(coordinate.substring(1));

        // Make row and column zero based.
        row--;
        column--;

        // Is there no ship at location?
        System.out.println();
        if (board.locations[row][column].ship.name.equals(Location.CONTENTS_UNKNOWN)) {
            // Update board with miss.
            board.locations[row][column].status = Location.STATUS_MISS;
            // Let user know situation.
            System.out.println("You missed!");
        } else {
            // Is this first hit on location?
            if (board.locations[row][column].status.equals(Location.STATUS_NOT_CHECKED)) {
                // Update ship and board hit counts.
                board.locations[row][column].ship.locationsHit++;
                board.shipLocationsHit++;
            }

            // Update board with hit.
            board.locations[row][column].status = Location.STATUS_HIT;
            // Is ship not yet fully hit?
            // Let user know situation.
            if (board.locations[row][column].ship.locationsHit < board.locations[row][column].ship.locationsTotal) {
                System.out.println("You hit a ship!");
            } else {
                System.out.println("You sank a ship!");
                // Are board ships fully hit?
                if (board.shipLocationsHit >= board.shipLocationsTotal) {
                    // Let user know situation.
                    System.out.println("You sank the last ship. You won. Congratulations!");
                    won = true;
                }
            }
        }

        return won;
    }

    private static void waitForEnter(Scanner scanner) {
        // Wait for user to press Enter.
        System.out.println("Press Enter and pass the move to another player");
        scanner.nextLine();

        // Clear screen.
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static String isCoordinateValid(String coordinate) {
        // Verify format of coordinate and that it exists on board.
        try {
            char row = coordinate.charAt(0);
            if ((row < 'A') || (row > (char) (Board.BOARD_HEIGHT + 64))) {
                return getErrorMessage(MESSAGE_COORDINATE_ERROR);
            }
        } catch (StringIndexOutOfBoundsException e) {
            return getErrorMessage(MESSAGE_COORDINATE_ERROR);
        }

        String columnString = coordinate.substring(1);
        try {
            int columnInteger = Integer.parseInt(columnString);
            if ((columnInteger < 1) || (columnInteger > Board.BOARD_WIDTH)) {
                return getErrorMessage(MESSAGE_COORDINATE_ERROR);
            }
        } catch (NumberFormatException e) {
            return getErrorMessage(MESSAGE_COORDINATE_ERROR);
        }

        return "";
    }

    private static String getErrorMessage(String message) {
        // Format error message to match spec.
        return String.format("Error! %s Try again:", message);
    }
}
