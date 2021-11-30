import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChessEngine {

    enum ChessColors {
        White, Black
    }

    // Chess GUI
    public ChessGUI GUI = new ChessGUI(this);

    // Variables
    public ChessColors currentTurnColor;
    public int         currentTurnCount;
    public boolean     isGameRunning = false;
    public boolean     isOnlineGame;

    public boolean          isPieceSelected;
    public Piece            selectedPiece;
    public Set<Coordinates> possibleDestinations;

    // Map of pieces
    public Map<Coordinates, Piece> pieces;


    public void newLocalGame() {
        initBoard();

        currentTurnColor = ChessColors.White;
        currentTurnCount = 1;
        isGameRunning    = true;
        isOnlineGame     = false;
        isPieceSelected  = false;

        GUI.enableButtonsForCurrentTurn();
        GUI.updateGameStatusLabels();
    }


    /**
     * Initialize a board
     */
    private void initBoard() {
        pieces = new HashMap<>(33);

        // Black
        initFirstRow(ChessColors.Black, 0);
        initPawns(ChessColors.Black, 1);

        // White
        initFirstRow(ChessColors.White, 7);
        initPawns(ChessColors.White, 6);

        GUI.clearPieces();
        for (Piece piece : pieces.values()) {
            GUI.addPiece(piece);
        }
    }

    private void initFirstRow(ChessColors color, int row) {
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
        pieces.put(pos, new King(color, pos));

        pos = new Coordinates(row, 5);
        pieces.put(pos, new Bishop(color, pos));

        pos = new Coordinates(row, 6);
        pieces.put(pos, new Knight(color, pos));

        pos = new Coordinates(row, 7);
        pieces.put(pos, new Rook(color, pos));

    }

    private void initPawns(ChessColors color, int row) {
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
        Piece piece = pieces.get(src);

        piece.hasMoved = true;
        piece.pos      = dest;

        pieces.put(dest, piece);
        pieces.remove(src);
    }

    private void switchTurn() {
        possibleDestinations.clear();
        isPieceSelected = false;
        selectedPiece   = null;

        if (currentTurnColor == ChessColors.White) {
            currentTurnColor = ChessColors.Black;
        } else {
            currentTurnCount++;
            currentTurnColor = ChessColors.White;
        }

        GUI.enableButtonsForCurrentTurn();
        GUI.updateGameStatusLabels();
    }


    public abstract class Piece {
        ChessColors color;
        Coordinates pos;
        String      icon;
        boolean     hasMoved = false;

        Piece(ChessColors color, Coordinates pos) {
            this.color = color;
            this.pos   = pos;
        }

        /**
         * A string representation of the piece
         *
         * @return Piece information in string
         */
        public String toString() {
            return (this.color == ChessColors.Black ? "Black " : "White ") + this.getClass().getSimpleName()
                   + " at " + this.pos;
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
        Rook(ChessColors color, Coordinates pos) {
            super(color, pos);
            this.icon = (this.color == ChessColors.Black ? "♜" : "♖");
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

        Knight(ChessColors color, Coordinates pos) {
            super(color, pos);
            this.icon = (this.color == ChessColors.Black ? "♞" : "♘");
        }

        @Override
        public Set<Coordinates> getPossibleMovements() {
            return moveOnceAll(knightMovesSet);
        }
    }

    public class Bishop extends Piece {
        Bishop(ChessColors color, Coordinates pos) {
            super(color, pos);
            this.icon = (this.color == ChessColors.Black ? "♝" : "♗");
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
        Queen(ChessColors color, Coordinates pos) {
            super(color, pos);
            this.icon = (this.color == ChessColors.Black ? "♛" : "♕");
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

        King(ChessColors color, Coordinates pos) {
            super(color, pos);
            this.icon = (this.color == ChessColors.Black ? "♚" : "♔");
        }

        @Override
        public Set<Coordinates> getPossibleMovements() {
            return moveOnceAll(kingMovesSet);
        }
    }

    public class Pawn extends Piece {
        Pawn(ChessColors color, Coordinates pos) {
            super(color, pos);
            this.icon = (this.color == ChessColors.Black ? "♟" : "♙");
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
            if (color == ChessColors.Black){
                // Move down
                if (!hasMoved) {
                    if (isWithinRangeAndEmpty(pos.add(2, 0))) result.add(pos.add(2,0));
                }
                if (isWithinRangeAndEmpty(pos.add(1, 0))) result.add(pos.add(1,0));
            } else {
                // Move up
                if (!hasMoved) {
                    if (isWithinRangeAndEmpty(pos.add(-2,0))) result.add(pos.add(-2, 0));
                }
                if (isWithinRangeAndEmpty(pos.add(-1, 0))) result.add(pos.add(-1,0));
            }

            // Diagonal move
            int d_row = color == ChessColors.Black ? 1 : -1;
            if (isWithinRangeAndHasEnemy(pos.add(d_row, -1))) result.add(pos.add(d_row, -1));
            if (isWithinRangeAndHasEnemy(pos.add(d_row, 1))) result.add(pos.add(d_row, 1));

            return result;
        }
    }
}
