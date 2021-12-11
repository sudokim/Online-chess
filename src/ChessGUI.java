import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public class ChessGUI extends JFrame {

    /**
     * A dialog for promoting pawn
     * Choose beetween queen, rook, bishop, and knight
     */
    public static class PawnPromotionDialog extends JDialog {

        private ChessPieceType result;

        /**
         * Construct a new dialog
         *
         * @param coords Coordinates of the pawn
         * @param color  Color of the pawn
         */
        PawnPromotionDialog(JFrame parent, Coordinates coords, ChessColorType color) {
            super(parent, "Promote Pawn", true);

            JPanel panel = new JPanel(new GridLayout(2, 1));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            panel.add(new JLabel("Promote a pawn at " + coords + " to:"));
            panel.add(new JPanel(new GridLayout(1, 4, 10, 0)) {
                {
                    Dimension ButtonSize = new Dimension(50, 50);

                    this.add(new JButton() {{
                        setText((color == ChessColorType.Black) ? "♛" : "♕");
                        setToolTipText("Queen");
                        setFont(new Font("Monospaced", Font.PLAIN, sizeButtonFont));
                        setBackground(colorDestinations);
                        setMargin(new Insets(0, 0, 0, 0));
                        setPreferredSize(ButtonSize);
                        setForeground(color == ChessColorType.Black ? colorBlack : colorWhite);

                        addActionListener(l -> buttonClicked(ChessPieceType.Queen));
                    }});
                    this.add(new JButton() {{
                        setText((color == ChessColorType.Black) ? "♜" : "♖");
                        setToolTipText("Rook");
                        setFont(new Font("Monospaced", Font.PLAIN, sizeButtonFont));
                        setBackground(colorDestinations);
                        setMargin(new Insets(0, 0, 0, 0));
                        setPreferredSize(ButtonSize);
                        setForeground(color == ChessColorType.Black ? colorBlack : colorWhite);

                        addActionListener(l -> buttonClicked(ChessPieceType.Rook));
                    }});
                    this.add(new JButton() {{
                        setText((color == ChessColorType.Black) ? "♞" : "♘");
                        setToolTipText("Knight");
                        setFont(new Font("Monospaced", Font.PLAIN, sizeButtonFont));
                        setBackground(colorDestinations);
                        setMargin(new Insets(0, 0, 0, 0));
                        setPreferredSize(ButtonSize);
                        setForeground(color == ChessColorType.Black ? colorBlack : colorWhite);

                        addActionListener(l -> buttonClicked(ChessPieceType.Knight));
                    }});
                    this.add(new JButton() {{
                        setText((color == ChessColorType.Black) ? "♝" : "♗");
                        setToolTipText("Bishop");
                        setFont(new Font("Monospaced", Font.PLAIN, sizeButtonFont));
                        setBackground(colorDestinations);
                        setMargin(new Insets(0, 0, 0, 0));
                        setPreferredSize(ButtonSize);
                        setForeground(color == ChessColorType.Black ? colorBlack : colorWhite);

                        addActionListener(l -> buttonClicked(ChessPieceType.Bishop));
                    }});
                }
            });

            this.add(panel);
            pack();
            setResizable(false);
            setLocationRelativeTo(parent);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        }

        /**
         * Show the promotion dialog and return a new piece
         *
         * @return New piece based on user selection
         */
        public ChessPieceType showDialog() {
            this.setVisible(true);
            return result;
        }

        private void buttonClicked(ChessPieceType selection) {
            result = selection;
            this.dispose();
        }
    }

    /**
     * SwingWorker for make GUI not hang while waiting for the opponent to make a move
     */
    public class WaitOpponent extends SwingWorker<String, Integer> {

        @Override
        protected String doInBackground() throws Exception {
            return null;
        }

        WaitOpponent() {

        }
    }

    // Constants

    // Color for pieces
    private final static Color     colorBlack            = new Color(21, 21, 21);
    private final static Color     colorWhite            = new Color(225, 225, 225);
    private final static Color     colorDestinations     = new Color(90, 109, 126);
    // Size of button
    private final static Dimension sizeButton            = new Dimension(50, 50);
    // Font size of button
    private final static int       sizeButtonFont        = 30;
    // Size of window
    private final static Dimension sizeWindow            = new Dimension(520, 550);
    // Size of labels
    private final static Dimension sizeIndicatorLabel    = new Dimension(100, 20);
    private final static Dimension sizeNumberLabel       = new Dimension(20, 50);
    private final static Dimension sizeAlphabetLabel     = new Dimension(50, 20);
    private final static Dimension sizePopupMessageLabel = new Dimension(500, 20);
    private final static Font      fontButton            = new Font(Font.MONOSPACED, Font.PLAIN, 36);
    private final static Font      fontPopupMessageTitle = new Font(Font.DIALOG, Font.BOLD, 18);

    // Variables

    // Chess engine
    private final ChessEngine      engine;
    // Main panel
    private final JPanel           panelMain               = new JPanel(new GridBagLayout());
    // Turn indicator
    private final JLabel           labelTurnColorIndicator = new JLabel();
    // Turn count indicator
    private final JLabel           labelTurnCountIndicator = new JLabel();
    // Popup message title and body
    private final JLabel           labelPopupMessageTitle  = new JLabel();
    private final JLabel           labelPopupMessageBody   = new JLabel();
    // Array of buttons
    private final JButton[][]      buttons                 = new JButton[8][8];
    // Activated buttons for destinations
    private final Set<Coordinates> activatedDestinations   = new HashSet<>();


    ChessGUI(ChessEngine game) {

        engine = game;

        // Draw board
        labelTurnColorIndicator.setPreferredSize(new Dimension(sizeIndicatorLabel));
        labelTurnColorIndicator.setHorizontalAlignment(SwingConstants.LEFT);
        labelTurnCountIndicator.setPreferredSize(new Dimension(sizeIndicatorLabel));
        labelTurnCountIndicator.setHorizontalAlignment(SwingConstants.RIGHT);

        // Add panels to grid
        addComponentToGrid(labelTurnColorIndicator, 0, 0, 3, 1);
        addComponentToGrid(labelTurnCountIndicator, 6, 0, 3, 1);
        addComponentToGrid(addNumberLabels(), 0, 2, 1, 8);
        addComponentToGrid(addAlphabetLabels(), 1, 1, 8, 1);
        addComponentToGrid(addPieceButtons(), 1, 2, 8, 8);
        addComponentToGrid(addAlphabetLabels(), 1, 10, 8, 1);
        addComponentToGrid(addNumberLabels(), 9, 2, 1, 8);

        // Window settings
        panelMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(panelMain);
        setJMenuBar(addMenuBar());
        setTitle("Online Chess");
        setSize(sizeWindow);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        pack();
    }

    // Functions for drawing board

    /**
     * @return A JPanel with 8 x 8 buttons
     */
    private JPanel addPieceButtons() {
        JButton btn;
        JPanel  board = new JPanel(new GridLayout(8, 8));

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                btn = new JButton();

                this.buttons[row][col] = btn;
                btn.putClientProperty("pos", new Coordinates(row, col));
                btn.addActionListener(this::buttonClickHandler);

                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setFont(new Font("Monospaced", Font.PLAIN, sizeButtonFont));
                btn.setBackground((row + col) % 2 == 0 ? new Color(161, 127, 114) : new Color(119, 89, 72));
                btn.setBorderPainted(false);
                btn.setEnabled(false);
                btn.setHorizontalAlignment(SwingConstants.CENTER);
                btn.setPreferredSize(sizeButton);

                board.add(btn);
            }
        }

        board.setBorder(BorderFactory.createLineBorder(Color.black, 2));

        return board;
    }

    /**
     * @return A JPanel with vertical labels '8' to '1'
     */
    private JPanel addNumberLabels() {
        JLabel lbl;
        JPanel panel = new JPanel(new GridLayout(8, 1));

        for (int i = 8; i >= 1; i--) {
            lbl = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            lbl.setPreferredSize(sizeNumberLabel);
            panel.add(lbl);
        }

        return panel;
    }

    /**
     * @return A JPanel with horizontal labels 'a' to 'h'
     */
    private JPanel addAlphabetLabels() {
        JLabel lbl;
        JPanel panel = new JPanel(new GridLayout(1, 8));

        for (int i = 0; i < 8; i++) {
            lbl = new JLabel(String.valueOf((char) (i + 'a')), SwingConstants.CENTER);
            lbl.setPreferredSize(sizeAlphabetLabel);
            panel.add(lbl);
        }

        return panel;
    }

    /**
     * @return A JMenuBar with several options
     */
    private JMenuBar addMenuBar() {
        JMenuBar menu = new JMenuBar();

        JMenu game = new JMenu("Game");

        JMenuItem newLocalGame = new JMenuItem("New local game...");
        newLocalGame.addActionListener(l -> {
            if (engine.isGameRunning) {
                if (JOptionPane.showConfirmDialog(
                    this, "Discard current game?", "Discard Game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
                ) == 0) engine.newLocalGame();
            } else engine.newLocalGame();
        });
        newLocalGame.setName("NewLocalGame");

        JMenuItem newOnlineGame = new JMenuItem("New online game...");
        newOnlineGame.addActionListener(l -> {

        });
        newOnlineGame.setName("NewOnlineGame");

        JMenuItem quitGame = new JMenuItem("Quit");
        quitGame.addActionListener(l -> {
            if (JOptionPane.showConfirmDialog(
                this, "Really quit?", "Quit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
            ) == 0) dispose();
        });

        game.add(newLocalGame);
        game.add(newOnlineGame);
        game.add(new JSeparator());
        game.add(quitGame);

        JMenu help = new JMenu("Help");

        JMenuItem aboutGame = new JMenuItem("About");
        aboutGame.addActionListener(l -> JOptionPane.showMessageDialog(
            this, "Online Chess Game\nTerm project for Java Programming Lab @ SKKU\n\n"
                  + "License: GPL-3.0-only\n\n"
                  + "Chess Icons from 'GNOME Chess', GPL-3.0\n\n(c) 2021 Hyunsoo Kim",
            "About Online Chess", JOptionPane.INFORMATION_MESSAGE
        ));

        help.add(aboutGame);

        menu.add(game);
        menu.add(help);

        return menu;
    }

    /**
     * Adds obj to grid
     *
     * @param obj    Object to add
     * @param x      x position on grid
     * @param y      y position on grid
     * @param width  width of the object
     * @param height height of the object
     */
    private void addComponentToGrid(JComponent obj, int x, int y, int width, int height) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx      = x;
        c.gridy      = y;
        c.gridwidth  = width;
        c.gridheight = height;
        c.fill       = GridBagConstraints.BOTH;

        this.panelMain.add(obj, c);
    }

    // Button click listener
    private void buttonClickHandler(ActionEvent l) {
        engine.selectPiece((Coordinates) ((JButton) l.getSource()).getClientProperty("pos"));
    }

    // Manipulate board

    /**
     * Clear all pieces on board
     */
    public void clearPieces() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JButton btn = buttons[row][col];

                btn.setBackground((row + col) % 2 == 0
                                  ? new Color(161, 127, 114)
                                  : new Color(119, 89, 72));
                btn.setText("");
                btn.setEnabled(false);
            }
        }
    }

    /**
     * Add a piece on board
     *
     * @param piece A piece to add
     */
    public void updatePiece(ChessEngine.Piece piece) {
        JButton btn = buttons[piece.pos.row][piece.pos.col];

        btn.setText(piece.icon);
        btn.setForeground(piece.color == ChessColorType.Black ? colorBlack : colorWhite);
    }

    /**
     * Activate buttons for movement
     */
    public void activateDestinations() {
        clearActivatedDestinations();

        if (engine.possibleDestinations != null) {
            for (Coordinates coords : engine.possibleDestinations) {
                activatedDestinations.add(coords);

                JButton btn = buttons[coords.row][coords.col];

                btn.setEnabled(true);
                btn.setBackground(colorDestinations);
            }
        }
    }

    /**
     * Clear activated buttons for movement and restore original state
     */
    public void clearActivatedDestinations() {
        for (Coordinates coords : activatedDestinations) {
            JButton btn = buttons[coords.row][coords.col];

            btn.setEnabled(false);
            // Restore original color
            btn.setBackground((coords.row + coords.col) % 2 == 0
                              ? new Color(161, 127, 114)
                              : new Color(119, 89, 72));
        }

        activatedDestinations.clear();
        enableButtonsForCurrentTurn();
    }

    /**
     * Enable the buttons of specific colors when the game is running
     */
    public void enableButtonsForCurrentTurn() {
        if (engine.isGameRunning) {
            engine.pieces.forEach((coords, piece) -> buttons[piece.pos.row][piece.pos.col]
                .setEnabled(piece.color == engine.currentTurnColor));
        }
    }

    /**
     * Move a piece on board
     *
     * @param src  Coordinates of the piece to move
     * @param dest Destination
     */
    public void movePiece(Coordinates src, Coordinates dest) {
        JButton srcButton  = buttons[src.row][src.col];
        JButton destButton = buttons[dest.row][dest.col];

        destButton.setText(srcButton.getText());
        destButton.setForeground(srcButton.getForeground());

        srcButton.setText("");
        srcButton.setEnabled(false);
        destButton.setEnabled(true);
    }

    /**
     * Remove a piece from board
     *
     * @param piece A piece to remove
     */
    public void removePiece(ChessEngine.Piece piece) {
        JButton btn = buttons[piece.pos.row][piece.pos.col];

        btn.setText("");
        btn.setEnabled(false);
    }

    /**
     * Update JLabels
     */
    public void updateGameStatusLabels() {
        labelTurnColorIndicator.setText("Turn " + engine.currentTurnCount);
        labelTurnCountIndicator.setText(engine.currentTurnColor.toString() + " Turn");
    }

    /**
     * Game end trigger
     *
     * @param endedBy Color of the winner
     */
    public void gameEnded(ChessColorType endedBy) {
        // Disable all buttons
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                buttons[row][col].setEnabled(false);
            }
        }

        // Show dialog
        JOptionPane.showMessageDialog(this,
                                      "Game ended!\n\n" + endedBy + " wins!",
                                      "Game ended", JOptionPane.INFORMATION_MESSAGE);

        engine.isGameRunning = false;

        // TODO: Close connection
    }

    /**
     * Game ended unexpectedly due to error
     *
     * @param reason Game end cause
     */
    public void gameEndedUnexpectedly(String reason) {
        // Disable all buttons
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                buttons[row][col].setEnabled(false);
            }
        }

        // Show dialog
        JOptionPane.showMessageDialog(this,
                                      "Game ended unexpectedly:\n\n" + reason,
                                      "Game ended unexpectedly", JOptionPane.ERROR_MESSAGE);

        // TODO: Close connection
    }
}
