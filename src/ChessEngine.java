import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ChessEngine {
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
    public HashMap<Coordinates, Piece> pieces;


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
        pieces.put(pos, new Rook(pieces, color, pos));

        pos = new Coordinates(row, 1);
        pieces.put(pos, new Knight(pieces, color, pos));

        pos = new Coordinates(row, 2);
        pieces.put(pos, new Bishop(pieces, color, pos));

        pos = new Coordinates(row, 3);
        pieces.put(pos, new Queen(pieces, color, pos));

        pos = new Coordinates(row, 4);
        King newKing = new King(pieces, color, pos);
        if (color == ChessColorType.Black) blackKing = newKing;
        else whiteKing = newKing;
        pieces.put(pos, newKing);

        pos = new Coordinates(row, 5);
        pieces.put(pos, new Bishop(pieces, color, pos));

        pos = new Coordinates(row, 6);
        pieces.put(pos, new Knight(pieces, color, pos));

        pos = new Coordinates(row, 7);
        pieces.put(pos, new Rook(pieces, color, pos));

    }

    private void initPawns(ChessColorType color, int row) {
        Coordinates pos;

        for (int col = 0; col < 8; col++) {
            pos = new Coordinates(row, col);
            pieces.put(pos, new Pawn(pieces, color, pos));
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
                        newPiece = new Bishop(pieces, srcPiece.color, srcPiece.pos);
                        break;
                    case Queen:
                        newPiece = new Queen(pieces, srcPiece.color, srcPiece.pos);
                        break;
                    case Rook:
                        newPiece = new Rook(pieces, srcPiece.color, srcPiece.pos);
                        break;
                    case Knight:
                        newPiece = new Knight(pieces, srcPiece.color, srcPiece.pos);
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
     * <p>
     * Save file structure:
     * turnCount(int), turnColor(ChessColorType), pairs of coordinates and piece(Coordinates, Piece),
     * List of logger items
     *
     * @param gameFile File destination
     * @return "" if successful, error message if unsuccessful
     */
    public String saveGame(File gameFile) {
        try (FileOutputStream fos = new FileOutputStream(gameFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {

            bos.write(0x77);

            oos.writeInt(currentTurnCount);
            oos.writeObject(currentTurnColor);

            oos.writeInt(pieces.size());
            for (Map.Entry<Coordinates, Piece> entry : pieces.entrySet()) {
                Coordinates key   = entry.getKey();
                Piece       value = entry.getValue();
                oos.writeObject(key);
                oos.writeObject(value);
            }

            oos.writeInt(logger.logs.size());
            for (ChessLogger.ChessLoggerItem item : logger.logs) {
                oos.writeObject(item);
            }

        } catch (IOException e) {
            return e.getMessage();
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
        int                                    turnCount;
        ChessColorType                         turnColor;
        int                                    piecesSize, logSize;
        HashMap<Coordinates, Piece>            pieces;
        ArrayList<ChessLogger.ChessLoggerItem> logs;

        Coordinates newPieceCoords;
        Piece       newPiece;

        try (FileInputStream fis = new FileInputStream(gameFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ObjectInputStream ois = new ObjectInputStream(bis)) {

            if (bis.read() != 0x77) return "Not a valid game file!";

            turnCount = ois.readInt();
            turnColor = (ChessColorType) ois.readObject();

            piecesSize = ois.readInt();
            pieces     = new HashMap<>(piecesSize);
            for (int i = 0; i < piecesSize; i++) {
                newPieceCoords = (Coordinates) ois.readObject();
                newPiece       = (Piece) ois.readObject();

                newPiece.pieces = pieces;

                pieces.put(newPieceCoords, newPiece);
            }

            logSize = ois.readInt();
            logs    = new ArrayList<>(logSize);
            for (int i = 0; i < logSize; i++) {
                logs.add((ChessLogger.ChessLoggerItem) ois.readObject());
            }

        } catch (IOException | ClassNotFoundException e) {
            return e.getMessage();
        }

        // Load new game state
        GUI.clearPieces();

        this.pieces = pieces;
        for (Piece piece : this.pieces.values()) {
            if (piece.type == ChessPieceType.King) {
                if (piece.color == ChessColorType.Black) {
                    blackKing = (King) piece;
                } else {
                    whiteKing = (King) piece;
                }
            }
            GUI.updatePiece(piece);
        }

        this.logger           = new ChessLogger(logs);
        this.currentTurnColor = turnColor;
        this.currentTurnCount = turnCount;
        this.isGameRunning    = true;
        this.isOnlineGame     = false;
        this.isPieceSelected  = false;

        GUI.enableButtonsForCurrentTurn();
        GUI.updateGameStatusLabels();

        return "";
    }
}
