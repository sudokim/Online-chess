import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Logs chess moves
 * <p>
 * Moves are stored in memory, and then
 */
public class ChessLogger {

    ChessLogger(ArrayList<ChessLoggerItem> logs) {
        this.logs = logs;
    }

    ChessLogger() {
        this.logs = new ArrayList<>();
    }

    /**
     * Class for each item in logger
     */
    public static class ChessLoggerItem implements Serializable {

        private final int            turn;
        private final Coordinates    src;
        private final Coordinates    dest;
        private final Piece          movedPiece;
        private final Piece          caughtPiece;
        private final ChessPieceType promotion;
        private       String         description;

        ChessLoggerItem(int turn, Coordinates src, Coordinates dest, Piece movedPiece, ChessPieceType promotionTo) {
            this.turn        = turn;
            this.src         = src;
            this.dest        = dest;
            this.movedPiece  = movedPiece;
            this.caughtPiece = null;
            this.promotion   = promotionTo;
        }

        ChessLoggerItem(int turn, Coordinates src, Coordinates dest, Piece movedPiece, Piece caughtPiece, ChessPieceType promotionTo) {
            this.turn        = turn;
            this.src         = src;
            this.dest        = dest;
            this.movedPiece  = movedPiece;
            this.caughtPiece = caughtPiece;
            this.promotion   = promotionTo;
        }

        /**
         * Get human-readable description of each log
         *
         * @return Description
         */
        public String getDescription() {
            if (description == null) {
                // Generate new description string if empty
                description = "Turn " + turn + ": " + movedPiece.color + " " + movedPiece.type;
                if (caughtPiece != null) {
                    description += " caught a " + caughtPiece.color + " " + caughtPiece.type + " at " + dest;
                } else {
                    description += " moved to " + dest;
                }
                if (promotion != null) {
                    description += ", and was promoted to " + promotion;
                }
            }

            return description;
        }
    }

    public ArrayList<ChessLoggerItem> getLogs() {
        return logs;
    }

    public void addLog(ChessLoggerItem item) {
        logs.add(item);
    }

    // List of log items
    private final ArrayList<ChessLoggerItem> logs;

    /**
     * Log a move
     *
     * @param turn       Turn count
     * @param src        Source coordinates
     * @param dest       Destination coordinates
     * @param movedPiece Moved piece
     */
    public ChessLoggerItem addMove(int turn, Coordinates src, Coordinates dest, Piece movedPiece, ChessPieceType promotionTo) {
        ChessLoggerItem cli = new ChessLoggerItem(turn, src, dest, movedPiece, promotionTo);
        logs.add(cli);

        return cli;
    }

    /**
     * Log a move where a piece is caught
     *
     * @param turn        Turn count
     * @param src         Source coordinates
     * @param dest        Destination coordinates
     * @param movedPiece  Moved piece
     * @param caughtPiece Caught piece
     */
    public ChessLoggerItem addMove(int turn, Coordinates src, Coordinates dest, Piece movedPiece, Piece caughtPiece, ChessPieceType promotionTo) {
        ChessLoggerItem cli = new ChessLoggerItem(turn, src, dest, movedPiece, caughtPiece, promotionTo);
        logs.add(cli);

        return cli;
    }

    /**
     * Write log to file
     *
     * @param parent      Chess GUI
     */
    private void writeLogToFile(JFrame parent) {
        File file = FileDialog.save(parent, "Save log file", "Text file (*.txt)", "txt");

        if (file != null) {
            try {
                // Write to file
                PrintStream ps = new PrintStream(file);

                for (ChessLoggerItem item : logs) {
                    ps.print(item.getDescription());
                }

                ps.close();

            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent, "Cannot write to the file!\n\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
