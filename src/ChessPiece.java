import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// Pieces
abstract class Piece implements Serializable {

    protected transient HashMap<Coordinates, Piece> pieces;
    protected           ChessColorType              color;
    protected           Coordinates                 pos;
    protected           String                      icon;
    protected           ChessPieceType              type;
    protected           boolean                     hasMoved = false;
    protected final     int                         id;  // Unique id given by the start position

    Piece(HashMap<Coordinates, Piece> pieces, ChessColorType color, Coordinates pos) {
        this.pieces = pieces;
        this.color  = color;
        this.pos    = pos;
        this.id     = pos.hashCode();
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

class Rook extends Piece {
    Rook(HashMap<Coordinates, Piece> pieces, ChessColorType color, Coordinates pos) {
        super(pieces, color, pos);
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

class Knight extends Piece {
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

    Knight(HashMap<Coordinates, Piece> pieces, ChessColorType color, Coordinates pos) {
        super(pieces, color, pos);
        this.icon = (this.color == ChessColorType.Black ? "♞" : "♘");
        this.type = ChessPieceType.Knight;
    }

    @Override
    public Set<Coordinates> getPossibleMovements() {
        return moveOnceAll(knightMovesSet);
    }
}

class Bishop extends Piece {
    Bishop(HashMap<Coordinates, Piece> pieces, ChessColorType color, Coordinates pos) {
        super(pieces, color, pos);
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

class Queen extends Piece {
    Queen(HashMap<Coordinates, Piece> pieces, ChessColorType color, Coordinates pos) {
        super(pieces, color, pos);
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

class King extends Piece {
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

    King(HashMap<Coordinates, Piece> pieces, ChessColorType color, Coordinates pos) {
        super(pieces, color, pos);
        this.icon = (this.color == ChessColorType.Black ? "♚" : "♔");
        this.type = ChessPieceType.Queen;
    }

    @Override
    public Set<Coordinates> getPossibleMovements() {
        return moveOnceAll(kingMovesSet);
    }
}

class Pawn extends Piece {
    Pawn(HashMap<Coordinates, Piece> pieces, ChessColorType color, Coordinates pos) {
        super(pieces, color, pos);
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