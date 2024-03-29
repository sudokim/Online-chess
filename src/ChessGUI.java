import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

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

    // Constants

    // Color for pieces
    private final static Color     colorBlack         = new Color(21, 21, 21);
    private final static Color     colorWhite         = new Color(225, 225, 225);
    private final static Color     colorDestinations  = new Color(90, 109, 126);
    // Size of button
    private final static Dimension sizeButton         = new Dimension(50, 50);
    // Font size of button
    private final static int       sizeButtonFont     = 30;
    // Size of window
    private final static Dimension sizeWindow         = new Dimension(520, 580);
    // Size of labels
    private final static Dimension sizeIndicatorLabel = new Dimension(100, 20);
    private final static Dimension opponentLabel      = new Dimension(200, 20);
    private final static Dimension sizeNumberLabel    = new Dimension(20, 50);
    private final static Dimension sizeAlphabetLabel  = new Dimension(50, 20);

    // Variables

    // Chess engine
    private final ChessEngine      engine;
    // Main panel
    private final JPanel           panelMain               = new JPanel(new GridBagLayout());
    // Opponent indicator
    private final JLabel           labelOpponent           = new JLabel();
    // Opponent wait indicator
    private final JLabel           labelWaitOpponent       = new JLabel();
    // Turn indicator
    private final JLabel           labelTurnColorIndicator = new JLabel();
    // Turn count indicator
    private final JLabel           labelTurnCountIndicator = new JLabel();
    // Array of buttons
    private final JButton[][]      buttons                 = new JButton[8][8];
    // Activated buttons for destinations
    private final Set<Coordinates> activatedDestinations   = new HashSet<>();


    ChessGUI(ChessEngine engine) {
        this.engine = engine;

        // Draw board
        labelOpponent.setPreferredSize(new Dimension(opponentLabel));
        labelOpponent.setHorizontalAlignment(SwingConstants.LEFT);
        labelWaitOpponent.setPreferredSize(new Dimension(opponentLabel));
        labelWaitOpponent.setHorizontalAlignment(SwingConstants.RIGHT);
        hideWaitMessage();
        labelTurnColorIndicator.setPreferredSize(new Dimension(sizeIndicatorLabel));
        labelTurnColorIndicator.setHorizontalAlignment(SwingConstants.LEFT);
        labelTurnCountIndicator.setPreferredSize(new Dimension(sizeIndicatorLabel));
        labelTurnCountIndicator.setHorizontalAlignment(SwingConstants.RIGHT);

        // Add panels to grid
        addComponentToGrid(labelOpponent, 0, 0, 3, 1);
        addComponentToGrid(labelWaitOpponent, 0, 1, 4, 1);
        addComponentToGrid(labelTurnColorIndicator, 0, 2, 3, 1);
        addComponentToGrid(labelTurnCountIndicator, 6, 2, 3, 1);
        addComponentToGrid(addNumberLabels(), 0, 4, 1, 8);
        addComponentToGrid(addAlphabetLabels(), 1, 3, 8, 1);
        addComponentToGrid(addPieceButtons(), 1, 4, 8, 8);
        addComponentToGrid(addAlphabetLabels(), 1, 12, 8, 1);
        addComponentToGrid(addNumberLabels(), 9, 4, 1, 8);

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

        JMenuItem hostOnlineGame = new JMenuItem("Host an online game...");
        hostOnlineGame.addActionListener(l -> hostOnlineGame());
        hostOnlineGame.setName("HostOnlineGame");

        JMenuItem joinOnlineGame = new JMenuItem("Join an online game...");
        joinOnlineGame.addActionListener(l -> joinOnlineGame());
        joinOnlineGame.setName("JoinOnlineGame");

        JMenuItem saveGame = new JMenuItem("Save game...");
        saveGame.addActionListener(l -> {
            if (engine.isGameRunning) {
                // Select destination
                File destination = FileDialog.save(this, "Save game", "Chess game save file (*.jcg)", "jcg");

                if (destination == null) return;

                if (!destination.getAbsolutePath().endsWith(".jcg"))
                    destination = new File(destination.getAbsolutePath() + ".jcg");

                // Valid file destination
                String result = engine.saveGame(destination);

                if (Objects.equals(result, "")) {
                    JOptionPane.showMessageDialog(this, "Successfully saved game.", "Save game", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Could not save game.\n\n" + result, "Save game", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        saveGame.setName("SaveGame");

        JMenuItem loadGame = new JMenuItem("Load game...");
        loadGame.addActionListener(l -> {
            File source = FileDialog.open(this, "Load game", "Chess game save file (*.jcg)", "jcg");

            if (source == null) return;

            if (!source.canRead()) {
                JOptionPane.showMessageDialog(this, "Unable to read save file!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (engine.isGameRunning) {
                if (JOptionPane.showConfirmDialog(
                    this, "Discard current game?", "Discard Game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
                ) != 0) return;
            }

            // Valid file destination
            String result = engine.loadGame(source);

            if (Objects.equals(result, "")) {
                JOptionPane.showMessageDialog(this, "Successfully loaded game.", "Load game", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Could not load game.\n\n" + result, "Load game", JOptionPane.ERROR_MESSAGE);
            }
        });
        loadGame.setName("LoadGame");

        JMenuItem quitGame = new JMenuItem("Quit");
        quitGame.addActionListener(l -> {
            if (JOptionPane.showConfirmDialog(
                this, "Really quit?", "Quit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
            ) == 0) dispose();
        });

        game.add(newLocalGame);
        game.add(hostOnlineGame);
        game.add(joinOnlineGame);
        game.add(new JSeparator());
        game.add(saveGame);
        game.add(loadGame);
        game.add(new JSeparator());
        game.add(quitGame);

        JMenu help = new JMenu("Help");

        JMenuItem aboutGame = new JMenuItem("About");
        aboutGame.addActionListener(l -> JOptionPane.showMessageDialog(
            this, "Online Chess Game\nTerm project for Java Programming Lab @ SKKU\n\n"
                  + "(c) 2021 Hyunsoo Kim",
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

    public void setOpponentLabelText(String text) {
        labelOpponent.setText(text);
    }

    // Manipulate board

    public void showWaitMessage() {
        labelWaitOpponent.setText("Waiting for opponent to move...");
    }

    public void hideWaitMessage() {
        labelWaitOpponent.setText("");
    }

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
    public void updatePiece(Piece piece) {
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
     * Disable all buttons
     */
    public void disableAllButtons() {
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
                 buttons[row][col].setEnabled(false);
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
     * @param coords coordinates of button to remove
     */
    public void removePiece(Coordinates coords) {
        JButton btn = buttons[coords.row][coords.col];

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
        disableAllButtons();

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
        disableAllButtons();

        // Show dialog
        JOptionPane.showMessageDialog(this,
                                      "Game ended unexpectedly:\n\n" + reason,
                                      "Game ended unexpectedly", JOptionPane.ERROR_MESSAGE);

        // TODO: Close connection
    }

    /**
     * Host an online game
     */
    private void hostOnlineGame() {
        String portInput = (String) JOptionPane.showInputDialog(this, "Enter port (default: 5000):", "Host an online game", JOptionPane.QUESTION_MESSAGE, null, null, "5000");
        if (portInput == null || portInput.equals("")) return;

        int port;

        try {
            port = Integer.parseInt(portInput);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid port " + portInput, "Host an online game", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Valid hostname/port

        // Wait dialog
        JDialog dialog = new JDialog(ChessGUI.this, "Waiting for connection...", true);
        JPanel  panel  = new JPanel(new GridLayout(2, 1));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Waiting for a new connection at port " + port));
        JButton cancelButton = new JButton("Cancel");
        panel.add(cancelButton);

        dialog.add(panel);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(ChessGUI.this);
        dialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        SwingWorker<String, Integer> sw = new SwingWorker<>() {

            @Override
            protected String doInBackground() {
                return engine.hostOnlineGame(port);
            }

            @Override
            protected void done() {
                try {
                    String result = get();

                    if (result != null) {
                        JOptionPane.showMessageDialog(ChessGUI.this, "Error while joining game\n\n" + result, "Host an online game", JOptionPane.ERROR_MESSAGE);
                    } else {
                        // Successful connection
                        System.out.println("CONNECTED Host");
                        dialog.dispose();
                        engine.onlineGameStart();
                    }

                } catch (InterruptedException | CancellationException ignored) {
                    System.out.println("Cancelled");

                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };

        cancelButton.addActionListener(l -> {
            sw.cancel(true);
            dispose();
        });
        sw.execute();
        dialog.setVisible(true);
    }

    /**
     * Join an online game
     */
    private void joinOnlineGame() {
        // Get user input for address and port
        String hostname = JOptionPane.showInputDialog(this, "Enter the address of the host:", "Join an online game", JOptionPane.QUESTION_MESSAGE);

        if (hostname != null && !hostname.equals("")) {
            String portInput = (String) JOptionPane.showInputDialog(this, "Enter port (default: 5000):", "Join an online game", JOptionPane.QUESTION_MESSAGE, null, null, "5000");

            if (portInput != null && !portInput.equals("")) {
                int port;

                try {
                    port = Integer.parseInt(portInput);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid port " + portInput, "Join an online game", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Valid hostname/port

                // Setup dialog
                JDialog dialog = new JDialog(ChessGUI.this, "Waiting for host...", true);
                JPanel  panel  = new JPanel(new GridLayout(2, 1));
                panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                panel.add(new JLabel("Waiting for " + hostname + ":" + port));
                JButton cancelButton = new JButton("Cancel");
                panel.add(cancelButton);

                dialog.add(panel);
                dialog.pack();
                dialog.setResizable(false);
                dialog.setLocationRelativeTo(ChessGUI.this);
                dialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

                // Swingworker waiting for connection
                SwingWorker<String, Integer> sw = new SwingWorker<>() {

                    @Override
                    protected String doInBackground() {
                        return engine.joinOnlineGame(hostname, port);
                    }

                    @Override
                    protected void done() {
                        try {
                            String result = get();

                            if (result != null) {
                                JOptionPane.showMessageDialog(ChessGUI.this, "Error while joining game\n\n" + result, "Join an online game", JOptionPane.ERROR_MESSAGE);
                            } else {
                                // Successful connection
                                System.out.println("CONNECTED Client");
                            }
                            dialog.dispose();
                            engine.onlineGameStart();

                        } catch (InterruptedException ignored) {

                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                    }
                };

                cancelButton.addActionListener(l -> {
                    sw.cancel(true);
                    dispose();
                });
                sw.execute();
                dialog.setVisible(true);
            }
        }
    }

    /**
     * Show an information dialog
     */
    public void connectionEstablishedDialog(ChessColorType color) {
        JOptionPane.showMessageDialog(this, "Connection established!\n\nYour color is " + color + ".", "Connected", JOptionPane.INFORMATION_MESSAGE);
    }
}
