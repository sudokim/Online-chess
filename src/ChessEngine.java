import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ChessEngine {
    // Game status for serialization
    private class GameState implements Serializable {
        public int                     turnCount;
        public ChessColorType          turnColor;
        public Map<Coordinates, Piece> pieces;
        public ChessLogger             logger;

        GameState(int turnCount, ChessColorType turnColor, Map<Coordinates, Piece> pieces, ChessLogger logger) {
            this.turnCount = turnCount;
            this.turnColor = turnColor;
            this.pieces    = pieces;
            this.logger    = logger;
        }
    }

    // Chess GUI
    public ChessGUI GUI = new ChessGUI(this);

    // Chess logger
    public ChessLogger logger;

    // Variables
    public ChessColorType currentTurnColor;
    public int            currentTurnCount;
    public boolean        isGameRunning = false;
    public boolean        isOnlineGame;

    public boolean          isPieceSelected;
    public Piece            selectedPiece;
    public Set<Coordinates> possibleDestinations;

    private King whiteKing;
    private King blackKing;

    // Map of pieces
    public Map<Coordinates, Piece> pieces;


    // Start new game
    public void newLocalGame() {
        initBoard();

        currentTurnColor = ChessColorType.White;
        currentTurnCount = 1;
        isGameRunning    = true;
        isOnlineGame     = false;
        isPieceSelected  = false;

        GUI.enableButtonsForCurrentTurn();
        GUI.updateGameStatusLabels();
    }

    public void newOnlineGame() {

    }


    /**
     * Initialize a board
     */
    private void initBoard() {
        pieces = new HashMap<>(33);
        logger = new ChessLogger();

        // Black
        initFirstRow(ChessColorType.Black, 0);
        initPawns(ChessColorType.Black, 1);

        // White
        initFirstRow(ChessColorType.White, 7);
        initPawns(ChessColorType.White, 6);

        GUI.clearPieces();
        for (Piece piece : pieces.values()) {
            GUI.updatePiece(piece);
        }
    }

    private void initFirstRow(ChessColorType color, int row) {
        Coordinates pos;

        pos = new Coordinates(row, 0);
        pieces.put(pos, new Rook(color, pos));

        pos = new Coordinates(row, 1);
        pieces.put(pos, new Knight(color, pos));

        pos = new Coordinates(row, 2);
        pieces.put(pos, new Bishop(color, pos));

        pos = new Coordinates(row, 3);
        pieces.put(pos, new Queen(color, pos));

        pos = new Coordinates(row, 4);
        King newKing = new King(color, pos);
        if (color == ChessColorType.Black) blackKing = newKing;
        else whiteKing = newKing;
        pieces.put(pos, newKing);

        pos = new Coordinates(row, 5);
        pieces.put(pos, new Bishop(color, pos));

        pos = new Coordinates(row, 6);
        pieces.put(pos, new Knight(color, pos));

        pos = new Coordinates(row, 7);
        pieces.put(pos, new Rook(color, pos));

    }

    private void initPawns(ChessColorType color, int row) {
        Coordinates pos;

        for (int col = 0; col < 8; col++) {
            pos = new Coordinates(row, col);
            pieces.put(pos, new Pawn(color, pos));
        }
    }

    // Game functions
    public void selectPiece(Coordinates pos) {
        Piece piece = pieces.get(pos);

        if (isPieceSelected) {
            // Move piece
            if (possibleDestinations.contains(pos)) {
                // Selected button to move
                GUI.movePiece(selectedPiece.pos, pos);
                movePiece(selectedPiece.pos, pos);
                GUI.clearActivatedDestinations();

                switchTurn();
                return;

            } else {
                // Selected button to reselect
                GUI.clearActivatedDestinations();
            }
        }

        // Select piece
        Set<Coordinates> destinations = piece.getPossibleMovements();

        if (destinations.size() > 0) {
            possibleDestinations = destinations;
            isPieceSelected      = true;
            selectedPiece        = piece;

            GUI.activateDestinations();
        }

    }

    private void movePiece(Coordinates src, Coordinates dest) {
        Piece srcPiece  = pieces.get(src);
        Piece destPiece = pieces.get(dest);

        // King dead - game over
        if ((destPiece == blackKing) || (destPiece == whiteKing)) {
            isGameRunning = false;
            GUI.gameEnded(currentTurnColor);
            return;
        }

        // Move piece by changing key for the piece
        srcPiece.hasMoved = true;
        srcPiece.pos      = dest;

        if (destPiece != null) {
            // Piece caught
            logger.addMove(currentTurnCount, src, dest, srcPiece, destPiece);
            pieces.remove(dest);
        } else {
            // Piece not caught
            logger.addMove(currentTurnCount, src, dest, srcPiece);
        }

        pieces.put(dest, srcPiece);
        pieces.remove(src);

        // Promote pawn if conditions met
        if (srcPiece.type == ChessPieceType.Pawn) {
            if (((Pawn) srcPiece).isPromotable()) {
                ChessGUI.PawnPromotionDialog dialog = new ChessGUI.PawnPromotionDialog(GUI, srcPiece.pos, srcPiece.color);
                ChessPieceType               result = dialog.showDialog();

                Piece newPiece;
                switch (result) {
                    case Bishop:
                        newPiece = new Bishop(srcPiece.color, srcPiece.pos);
                        break;
                    case Queen:
                        newPiece = new Queen(srcPiece.color, srcPiece.pos);
                        break;
                    case Rook:
                        newPiece = new Rook(srcPiece.color, srcPiece.pos);
                        break;
                    case Knight:
                        newPiece = new Knight(srcPiece.color, srcPiece.pos);
                        break;
                    default:
                        throw new RuntimeException("Unexpected promotion piece type " + result);
                }

                // TODO: Log promotion

                pieces.remove(srcPiece.pos);
                pieces.put(newPiece.pos, newPiece);

                // Update button
                GUI.updatePiece(newPiece);
            }
        }
    }

    private void switchTurn() {
        possibleDestinations.clear();
        isPieceSelected = false;
        selectedPiece   = null;

        if (isGameRunning) {
            if (currentTurnColor == ChessColorType.White) {
                currentTurnColor = ChessColorType.Black;
            } else {
                currentTurnCount++;
                currentTurnColor = ChessColorType.White;
            }

            GUI.enableButtonsForCurrentTurn();
            GUI.updateGameStatusLabels();
        }
    }


    /**
     * Save game state
     *
     * @param gameFile File destination
     * @return "" if successful, error message if unsuccessful
     */
    public String saveGame(File gameFile) {
        try (FileOutputStream fos = new FileOutputStream(gameFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {

//            oos.writeObject(new GameState(currentTurnCount, currentTurnColor, pieces, logger));
            oos.writeObject(currentTurnCount);
            oos.writeObject(currentTurnColor);
//            oos.writeObject(pieces);
            oos.writeObject(logger);

        } catch (IOException e) {
            e.printStackTrace();
//            return e.getMessage();
        }

        return "";
    }

    /**
     * Load game state
     *
     * @param gameFile File destination
     * @return "" if successful, error message if unsuccessful
     */
    public String loadGame(File gameFile) {
        int                               turnCount;
        ChessColorType                    turnColor;
        Map<Coordinates, Piece>           pieces;
        List<ChessLogger.ChessLoggerItem> logger;

        try (FileInputStream fis = new FileInputStream(gameFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ObjectInputStream ois = new ObjectInputStream(bis)) {

            turnCount = (int) ois.readObject();
            turnColor = (ChessColorType) ois.readObject();


        } catch (IOException | ClassNotFoundException e) {
            return e.getMessage();
        }

        // Load new game state
        GUI.clearPieces();

//        for (Piece piece : gs.pieces.values()) {
//            GUI.updatePiece(piece);
//        }

        this.currentTurnColor = turnColor;
        this.currentTurnCount = turnCount;
        this.isGameRunning    = true;
        this.isOnlineGame     = false;
        this.isPieceSelected  = false;

        GUI.enableButtonsForCurrentTurn();
        GUI.updateGameStatusLabels();

        return "";
    }


    // Pieces
    public abstract class Piece implements Serializable {
        public        ChessColorType color;
        public        Coordinates    pos;
        public        String         icon;
        public        ChessPieceType type;
        public        boolean        hasMoved = false;
        private final int            id;  // Unique id given by the start position

        Piece(ChessColorType color, Coordinates pos) {
            this.color = color;
            this.pos   = pos;
            this.id    = pos.hashCode();
        }

        /**
         * A string representation of the piece
         *
         * @return Piece information in string
         */
        public String toString() {
            return (this.color == ChessColorType.Black ? "Black " : "White ") + this.getClass().getSimpleName()
                   + " at " + this.pos;
        }

        public int hashCode() {
            return this.id;
        }

        /**
         * A set of all possible movements a piece can make
         *
         * @return A set of destinations
         */
        abstract public Set<Coordinates> getPossibleMovements();

        /**
         * Returns whether a new movement is valid
         * Valid movement: destination within board
         *
         * @param pos Position of new movement
         * @return Availability
         */
        protected boolean isValidMove(Coordinates pos) {
            if (!pos.isWithinRange()) return false;

            Piece opponentPiece = pieces.get(pos);

            if (opponentPiece == null) return true;

            return opponentPiece.color != this.color;
        }

        protected Set<Coordinates> keepMoving(int d_row, int d_col) {
            Set<Coordinates> result = new HashSet<>(7);
            Coordinates      coords;

            for (int row = pos.row + d_row, col = pos.col + d_col; ((row >= 0) && (row < 8)) && ((col >= 0) && (col < 8)); row += d_row, col += d_col) {
                coords = new Coordinates(row, col);

                if (isValidMove(coords)) {
                    result.add(coords);

                    if (pieces.containsKey(coords)) break;

                } else break;
            }

            return result;
        }

        protected Set<Coordinates> moveOnceAll(Set<Coordinates> coords) {
            return coords.stream().map(pos::add).filter(this::isValidMove).collect(Collectors.toSet());
        }
    }

    public class Rook extends Piece {
        Rook(ChessColorType color, Coordinates pos) {
            super(color, pos);
            this.icon = (this.color == ChessColorType.Black ? "♜" : "♖");
            this.type = ChessPieceType.Rook;
        }

        @Override
        public Set<Coordinates> getPossibleMovements() {
            // TODO: Implement Castling
            Set<Coordinates> result = new HashSet<>(14);

            result.addAll(keepMoving(-1, 0));
            result.addAll(keepMoving(0, -1));
            result.addAll(keepMoving(1, 0));
            result.addAll(keepMoving(0, 1));

            return result;
        }
    }

    public class Knight extends Piece {
        private final Set<Coordinates> knightMovesSet = Set.of(
            new Coordinates(-2, -1),
            new Coordinates(-2, 1),
            new Coordinates(2, -1),
            new Coordinates(2, 1),
            new Coordinates(-1, -2),
            new Coordinates(-1, 2),
            new Coordinates(1, -2),
            new Coordinates(1, 2)
        );

        Knight(ChessColorType color, Coordinates pos) {
            super(color, pos);
            this.icon = (this.color == ChessColorType.Black ? "♞" : "♘");
            this.type = ChessPieceType.Knight;
        }

        @Override
        public Set<Coordinates> getPossibleMovements() {
            return moveOnceAll(knightMovesSet);
        }
    }

    public class Bishop extends Piece {
        Bishop(ChessColorType color, Coordinates pos) {
            super(color, pos);
            this.icon = (this.color == ChessColorType.Black ? "♝" : "♗");
            this.type = ChessPieceType.Bishop;
        }

        @Override
        public Set<Coordinates> getPossibleMovements() {
            Set<Coordinates> result = new HashSet<>(14);

            result.addAll(keepMoving(-1, -1));
            result.addAll(keepMoving(-1, 1));
            result.addAll(keepMoving(1, -1));
            result.addAll(keepMoving(1, 1));

            return result;
        }
    }

    public class Queen extends Piece {
        Queen(ChessColorType color, Coordinates pos) {
            super(color, pos);
            this.icon = (this.color == ChessColorType.Black ? "♛" : "♕");
            this.type = ChessPieceType.Queen;
        }

        @Override
        public Set<Coordinates> getPossibleMovements() {
            Set<Coordinates> result = new HashSet<>(28);

            result.addAll(keepMoving(-1, -1));
            result.addAll(keepMoving(-1, 1));
            result.addAll(keepMoving(1, -1));
            result.addAll(keepMoving(1, 1));
            result.addAll(keepMoving(-1, 0));
            result.addAll(keepMoving(0, -1));
            result.addAll(keepMoving(1, 0));
            result.addAll(keepMoving(0, 1));

            return result;
        }
    }

    public class King extends Piece {
        private final Set<Coordinates> kingMovesSet = Set.of(
            new Coordinates(-1, -1),
            new Coordinates(-1, 0),
            new Coordinates(-1, 1),
            new Coordinates(0, -1),
            new Coordinates(0, 0),
            new Coordinates(0, 1),
            new Coordinates(1, -1),
            new Coordinates(1, 0),
            new Coordinates(1, 1)
        );

        King(ChessColorType color, Coordinates pos) {
            super(color, pos);
            this.icon = (this.color == ChessColorType.Black ? "♚" : "♔");
            this.type = ChessPieceType.Queen;
        }

        @Override
        public Set<Coordinates> getPossibleMovements() {
            return moveOnceAll(kingMovesSet);
        }
    }

    public class Pawn extends Piece {
        Pawn(ChessColorType color, Coordinates pos) {
            super(color, pos);
            this.icon = (this.color == ChessColorType.Black ? "♟" : "♙");
            this.type = ChessPieceType.Pawn;
        }

        public boolean isPromotable() {
            return this.color == ChessColorType.Black
                   ? this.pos.row == 7
                   : this.pos.row == 0;
        }

        private boolean isWithinRangeAndEmpty(Coordinates coords) {
            return coords.isWithinRange() && (pieces.get(coords) == null);
        }

        private boolean isWithinRangeAndHasEnemy(Coordinates coords) {
            return coords.isWithinRange() && ((pieces.get(coords) != null) && (pieces.get(coords).color != this.color));
        }

        @Override
        public Set<Coordinates> getPossibleMovements() {
            // TODO: Implement en passant
            Set<Coordinates> result = new HashSet<>(4);

            // Vertical move
            if (color == ChessColorType.Black) {
                // Move down
                if (isWithinRangeAndEmpty(pos.add(1, 0))) {
                    result.add(pos.add(1, 0));

                    if (!hasMoved) {
                        if (isWithinRangeAndEmpty(pos.add(2, 0))) result.add(pos.add(2, 0));
                    }
                }
            } else {
                // Move up
                if (isWithinRangeAndEmpty(pos.add(-1, 0))) {
                    result.add(pos.add(-1, 0));

                    if (!hasMoved) {
                        if (isWithinRangeAndEmpty(pos.add(-2, 0))) result.add(pos.add(-2, 0));
                    }
                }
            }

            // Diagonal move
            int d_row = color == ChessColorType.Black ? 1 : -1;
            if (isWithinRangeAndHasEnemy(pos.add(d_row, -1))) result.add(pos.add(d_row, -1));
            if (isWithinRangeAndHasEnemy(pos.add(d_row, 1))) result.add(pos.add(d_row, 1));

            return result;
        }
    }
}
