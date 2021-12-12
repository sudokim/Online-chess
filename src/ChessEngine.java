import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ChessEngine {
    // Chess GUI
    public ChessGUI GUI = new ChessGUI(this);

    // Variables
    public ChessColorType currentTurnColor;
    public int            currentTurnCount;
    public boolean        isGameRunning = false;

    public boolean          isPieceSelected;
    public Piece            selectedPiece;
    public Set<Coordinates> possibleDestinations;

    private King whiteKing;
    private King blackKing;

    // Variables for online games
    public  boolean        isOnlineGame;
    private Socket         socket;
    private ServerSocket   server;
    private ChessColorType myColor;
    private ChessColorType opponentColor;
    private OutputStream   os;
    private InputStream    is;

    // Map of pieces
    public HashMap<Coordinates, Piece> pieces;

    // Start new game
    public void newLocalGame() {
        if (isOnlineGame && isGameRunning) {
            endOnlineConnection();
        }

        initBoard();

        currentTurnColor = ChessColorType.White;
        currentTurnCount = 1;
        isGameRunning    = true;
        isOnlineGame     = false;
        isPieceSelected  = false;

        GUI.setOpponentLabelText("Local game");
        GUI.enableButtonsForCurrentTurn();
        GUI.updateGameStatusLabels();
    }

    public String hostOnlineGame(int port) {
        if (isOnlineGame && isGameRunning) {
            endOnlineConnection();
        }

        try {
            server = new ServerSocket(port);
            System.out.println("Listening on port " + port);
            socket = server.accept();
            System.out.println("Connection accepted");
            os = socket.getOutputStream();
            is = socket.getInputStream();

            GUI.setOpponentLabelText("Online game: " + socket.getInetAddress());
            GUI.disableAllButtons();
            initBoard();
            isGameRunning = true;
            isOnlineGame  = true;

            currentTurnCount = 1;
            currentTurnColor = ChessColorType.White;

            // Choose color
            // 0: Black, 1: White
            double rand = Math.random();
            if (rand < 0.5) {
                myColor       = ChessColorType.Black;
                opponentColor = ChessColorType.White;

                os.write(1);
            } else {
                myColor       = ChessColorType.White;
                opponentColor = ChessColorType.Black;

                os.write(0);
            }

            GUI.connectionEstablishedDialog(myColor);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String joinOnlineGame(String hostname, int port) {
        if (isOnlineGame && isGameRunning) {
            endOnlineConnection();
        }

        try {
            socket = new Socket(hostname, port);
            os     = socket.getOutputStream();
            is     = socket.getInputStream();

            GUI.setOpponentLabelText("Online game: " + socket.getInetAddress());
            GUI.disableAllButtons();
            initBoard();
            isGameRunning = true;
            isOnlineGame  = true;

            // Host goes first
            currentTurnCount = 1;
            currentTurnColor = ChessColorType.White;

            // Get my color
            // 0: Black, 1: White
            int color = is.read();
            if (color == 0) {
                myColor       = ChessColorType.Black;
                opponentColor = ChessColorType.White;
            } else {
                myColor       = ChessColorType.White;
                opponentColor = ChessColorType.Black;
            }

            GUI.connectionEstablishedDialog(myColor);

        } catch (IOException e) {
            return e.getClass().getName() + " " + e.getMessage();
        }
        return null;
    }


    /**
     * Initialize a board
     */
    private void initBoard() {
        pieces               = new HashMap<>(33);
        selectedPiece        = null;
        possibleDestinations = null;

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

    /**
     * Initialize first or last row
     * @param color Color of the pieces
     * @param row Row number
     */
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

    /**
     * Initialise row next to first or last row
     * @param color Color of the pieces
     * @param row Row number
     */
    private void initPawns(ChessColorType color, int row) {
        Coordinates pos;

        for (int col = 0; col < 8; col++) {
            pos = new Coordinates(row, col);
            pieces.put(pos, new Pawn(pieces, color, pos));
        }
    }

    // Game functions
    /**
     * Piece selected - move if within possibleDestinations, or show possibleDestinations on GUI
     * @param pos Position of the button
     */
    public void selectPiece(Coordinates pos) {
        Piece piece = pieces.get(pos);

        if (isPieceSelected) {
            // Move piece
            if (possibleDestinations != null && possibleDestinations.contains(pos)) {
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

    /**
     * Move piece from src to dest
     * @param src src
     * @param dest dest
     */
    private void movePiece(Coordinates src, Coordinates dest) {
        byte[]         response    = new byte[6];
        Piece          srcPiece    = pieces.get(src);
        Piece          destPiece   = pieces.get(dest);

        // King dead - game over
        if ((destPiece == blackKing) || (destPiece == whiteKing)) {
            isGameRunning = false;
            GUI.gameEnded(currentTurnColor);
            response[4] = 1;

        } else {
            // Move piece by changing key for the piece
            srcPiece.hasMoved = true;
            srcPiece.pos      = dest;

            pieces.put(dest, srcPiece);
            pieces.remove(src);

            // Promote pawn if conditions met
            if (srcPiece.type == ChessPieceType.Pawn) {
                if (((Pawn) srcPiece).isPromotable()) {
                    ChessGUI.PawnPromotionDialog dialog = new ChessGUI.PawnPromotionDialog(GUI, srcPiece.pos, srcPiece.color);
                    ChessPieceType               result = dialog.showDialog();

                    Piece newPiece;
                    switch (result) {
                        case Queen:
                            newPiece = new Queen(pieces, srcPiece.color, srcPiece.pos);
                            response[5] = 1;
                            break;
                        case Bishop:
                            newPiece = new Bishop(pieces, srcPiece.color, srcPiece.pos);
                            response[5] = 2;
                            break;
                        case Rook:
                            newPiece = new Rook(pieces, srcPiece.color, srcPiece.pos);
                            response[5] = 3;
                            break;
                        case Knight:
                            newPiece = new Knight(pieces, srcPiece.color, srcPiece.pos);
                            response[5] = 4;
                            break;
                        default:
                            throw new RuntimeException("Unexpected promotion piece type " + result);
                    }

                    newPiece.id = srcPiece.id;
                    pieces.remove(srcPiece.pos);
                    pieces.put(newPiece.pos, newPiece);

                    // Update button
                    GUI.updatePiece(newPiece);
                }
            } else {
                // No promotion
                response[5] = 0;
            }
            response[4] = 0;
        }

        response[0] = (byte) src.row;
        response[1] = (byte) src.col;
        response[2] = (byte) dest.row;
        response[3] = (byte) dest.col;

        if (isOnlineGame) {
            if (!isGameRunning) {
                // Player wins
                endOnlineConnection();
            }

            try {
                os.write(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Start game for online games
     */
    public void onlineGameStart() {
        GUI.updateGameStatusLabels();

        if (isOnlineGame) waitOpponentMoveOrMakeMove();
        else GUI.enableButtonsForCurrentTurn();
    }

    /**
     * Switch turns - switch color and increase turn count
     */
    private void switchTurn() {
        possibleDestinations = null;
        isPieceSelected      = false;
        selectedPiece        = null;

        if (isGameRunning) {
            // Local game
            if (currentTurnColor == ChessColorType.White) {
                currentTurnColor = ChessColorType.Black;
            } else {
                currentTurnCount++;
                currentTurnColor = ChessColorType.White;
            }

            GUI.updateGameStatusLabels();

            if (isOnlineGame) waitOpponentMoveOrMakeMove();
            else GUI.enableButtonsForCurrentTurn();
        }
    }

    // Online game - wait or move
    /**
     * Wait for Socket response, or move
     */
    private void waitOpponentMoveOrMakeMove() {
        if (currentTurnColor == myColor) {
            // Move
            GUI.enableButtonsForCurrentTurn();
            GUI.hideWaitMessage();

        } else {
            // Wait until the opponent moves
            GUI.disableAllButtons();
            GUI.showWaitMessage();

            SwingWorker<byte[], Void> sw = new SwingWorker<>() {
                @Override
                protected byte[] doInBackground() throws Exception {
                    return is.readNBytes(6);
                }

                @Override
                protected void done() {
                    // Bytewise communication
                    // [Src row][Src column][Dest row][Dest column][Caught king][Promote to]
                    byte[] answer;

                    try {
                        // Move piece based on received data
                        answer = get();

                        Coordinates src        = new Coordinates(answer[0], answer[1]);
                        Coordinates dest       = new Coordinates(answer[2], answer[3]);
                        boolean     kingCaught = answer[4] == 1;
                        int         promotion  = answer[5];

                        // Make move based on answer
                        Piece srcPiece = pieces.get(src);
                        Piece newPiece = null;
                        pieces.remove(src);
                        pieces.remove(dest);

                        switch (promotion) {
                            case 0:
                                pieces.put(dest, srcPiece);
                                srcPiece.pos = dest;
                                break;
                            case 1:
                                // To queen
                                newPiece = new Queen(pieces, srcPiece.color, dest);
                                pieces.put(dest, newPiece);
                                break;
                            case 2:
                                // To bishop
                                newPiece = new Bishop(pieces, srcPiece.color, dest);
                                pieces.put(dest, newPiece);
                                break;
                            case 3:
                                // To rook
                                newPiece = new Rook(pieces, srcPiece.color, dest);
                                pieces.put(dest, newPiece);
                                break;
                            case 4:
                                // To knight
                                newPiece = new Knight(pieces, srcPiece.color, dest);
                                pieces.put(dest, newPiece);
                                break;
                        }

                        if (kingCaught) {
                            // Game end
                            GUI.gameEnded(opponentColor);
                            GUI.disableAllButtons();
                            isGameRunning = false;
                            endOnlineConnection();
                        }

                        // Update buttons
                        if (newPiece != null)
                            newPiece.id = srcPiece.id;
                        GUI.removePiece(src);
                        GUI.updatePiece(pieces.get(dest));

                        switchTurn();

                    } catch (InterruptedException | ExecutionException e) {
                        GUI.gameEndedUnexpectedly(e.getMessage());
                        e.printStackTrace();
                    }
                }
            };

            sw.execute();
        }
    }

    /**
     * End connection to opponent
     */
    private void endOnlineConnection() {
        try {
            os.close();
            is.close();
            socket.close();
            if (server != null) server.close();

            GUI.setOpponentLabelText("");
            GUI.hideWaitMessage();

        } catch (IOException e) {
            e.printStackTrace();
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
        int                                    piecesSize;
        HashMap<Coordinates, Piece>            pieces;

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

        this.currentTurnColor = turnColor;
        this.currentTurnCount = turnCount;
        this.isGameRunning    = true;
        this.isOnlineGame     = false;
        this.isPieceSelected  = false;

        GUI.setOpponentLabelText("Local game");
        GUI.enableButtonsForCurrentTurn();
        GUI.updateGameStatusLabels();

        return "";
    }
}
