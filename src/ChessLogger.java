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

        public String getDescription() {
            if (description == null) {
                // Generate new description string
                description = "Turn " + turn + ": " + movedPiece.color + " " + movedPiece.type;
                if (caughtPiece != null) {
                    description += " caught a " + caughtPiece.color + " " + caughtPiece.type + " at " + dest;
                }
            }

            return description;
        }

        public String getNotation() {
            if (notation == null) {
                // Generate new notation string
                if (movedPiece.color == ChessColorType.White) {
                    notation = "1. " + (movedPiece.type != ChessPieceType.Pawn ? movedPiece.type : "") + movedPiece.pos;
                } else {
                    notation = " " + (movedPiece.type != ChessPieceType.Pawn ? movedPiece.type : "") + movedPiece.pos + "\n";
                }
            }

            return notation;
        }
    }

    private final List<ChessLoggerItem> logs = new ArrayList<>();

    public void addMove(int turn, Coordinates src, Coordinates dest, ChessEngine.Piece movedPiece) {
        logs.add(new ChessLoggerItem(turn, src, dest, movedPiece));
    }

    public void addMove(int turn, Coordinates src, Coordinates dest, ChessEngine.Piece movedPiece, ChessEngine.Piece caughtPiece) {
        logs.add(new ChessLoggerItem(turn, src, dest, movedPiece, caughtPiece));
    }

    private void writeLogToFile(JFrame frame, boolean useNotation) {
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

        if (uc == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            try {
                PrintStream ps = new PrintStream(file);

                for (ChessLoggerItem item : logs) {
                    ps.print(useNotation ? item.getNotation() : item.getDescription());
                }

                ps.close();

            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Cannot write to the file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

