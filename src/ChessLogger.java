import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Logs chess moves
 * <p>
 * Moves are stored in memory, and then
 */
public class ChessLogger {

    /**
     * Class for each item in logger
     */
    private static class ChessLoggerItem {

        private final int               turn;
        private final Coordinates       src;
        private final Coordinates       dest;
        private final ChessEngine.Piece movedPiece;
        private final ChessEngine.Piece caughtPiece;
        private       String            description;
        private       String            notation;

        ChessLoggerItem(int turn, Coordinates src, Coordinates dest, ChessEngine.Piece movedPiece) {
            this.turn        = turn;
            this.src         = src;
            this.dest        = dest;
            this.movedPiece  = movedPiece;
            this.caughtPiece = null;
        }

        ChessLoggerItem(int turn, Coordinates src, Coordinates dest, ChessEngine.Piece movedPiece, ChessEngine.Piece caughtPiece) {
            this.turn        = turn;
            this.src         = src;
            this.dest        = dest;
            this.movedPiece  = movedPiece;
            this.caughtPiece = caughtPiece;
        }

        /**
         * Get human-readable description of each log
         * @return Description
         */
        public String getDescription() {
            if (description == null) {
                // Generate new description string if empty
                description = "Turn " + turn + ": " + movedPiece.color + " " + movedPiece.type;
                if (caughtPiece != null) {
                    description += " caught a " + caughtPiece.color + " " + caughtPiece.type + " at " + dest;
                }
            }

            return description;
        }

        /**
         * Get notation for each moves
         * @return Notation
         */
        public String getNotation() {
            if (notation == null) {
                // Generate new notation string if empty
                if (movedPiece.color == ChessColorType.White) {
                    notation = "1. " + (movedPiece.type != ChessPieceType.Pawn ? movedPiece.type : "") + movedPiece.pos;
                } else {
                    notation = " " + (movedPiece.type != ChessPieceType.Pawn ? movedPiece.type : "") + movedPiece.pos + "\n";
                }
            }

            return notation;
        }
    }

    // List of log items
    private final List<ChessLoggerItem> logs = new ArrayList<>();

    /**
     * Log a move
     * @param turn Turn count
     * @param src Source coordinates
     * @param dest Destination coordinates
     * @param movedPiece Moved piece
     */
    public void addMove(int turn, Coordinates src, Coordinates dest, ChessEngine.Piece movedPiece) {
        logs.add(new ChessLoggerItem(turn, src, dest, movedPiece));
    }

    /**
     * Log a move where a piece is caught
     * @param turn Turn count
     * @param src Source coordinates
     * @param dest Destination coordinates
     * @param movedPiece Moved piece
     * @param caughtPiece Caught piece
     */
    public void addMove(int turn, Coordinates src, Coordinates dest, ChessEngine.Piece movedPiece, ChessEngine.Piece caughtPiece) {
        logs.add(new ChessLoggerItem(turn, src, dest, movedPiece, caughtPiece));
    }

    /**
     * Write log to file
     * @param frame Chess GUI
     * @param useNotation True to use chess notations, False to use human-readable notations
     */
    private void writeLogToFile(JFrame frame, boolean useNotation) {
        // Show a file selection dialog
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select file destination...");
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.toPath().endsWith(".txt");
            }
            @Override
            public String getDescription() {
                return "Text file (*.txt)";
            }
        });

        int uc = fc.showOpenDialog(frame);

        // Valid file seleciton
        if (uc == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            try {
                // Write to file
                PrintStream ps = new PrintStream(file);

                for (ChessLoggerItem item : logs) {
                    ps.print(useNotation ? item.getNotation() : item.getDescription());
                }

                ps.close();

            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Cannot write to the file!\n\n" + e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

class ChessLogShowerGUI extends JFrame {

}