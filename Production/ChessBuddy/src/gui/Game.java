package gui;

import javafx.animation.FadeTransition;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Game {

    public static final int LOWER_BOUNDARY = 0;
    public static final int UPPER_BOUNDARY = 7;
    public static final int HEIGHT = 8;
    public static final int WIDTH = 8;

    public static final Color GREEN = Color.rgb(76, 159, 112, 1);
    public static final Color RED = Color.rgb(254, 95, 85, 1);
    public static final int IN_PROGRESS = 0;
    public static final int CHECKMATE = 1;
    public static final int STALEMATE = 2;
    public static final int NO_TIMER = -1;

    private double boardSize;
    private double elementHeight;
    private double scoreBoardHeight;
    private double topBarHeight;
    private double tileSize; //calculate GUI sizes based of ChessBuddy application scale
    private double barWidth;
    private double barHeight;

    private ChessBuddy app; //dependency on application

    private final GameInfo gameInfo = new GameInfo(); //deals with previous moves and ByteBoard storage

    private final Board board = new Board(); //the current Board

    private VBox sideBar;
    private NotationBoard notationTable;
    private final Circle whiteCircle;
    private final Circle blackCircle;
    private final AnchorPane root;
    private final Stage stage;

    private Tile selectedTile; //the current selected tile all moves are relative to this tile
    private final ArrayList<Selectable> selectable = new ArrayList<>(); //all active selectable
    private final ArrayList<Tile> highlightedTiles = new ArrayList<>();
    private boolean whiteBoardPosition;
    private boolean inCheck = false;
    private boolean isWhiteTurn = true;
    private boolean moveReadyState = true;
    private boolean finished = false;
    private boolean canRender = true;
    private int gameResult = IN_PROGRESS;
    private int timerType = NO_TIMER;
    public static final int[][] TIMER_INFO = {{30*60,20},{15*60,10},{3*60,2},{60,0}};




    protected Game(boolean whiteStart, Stage stageIn, ChessBuddy app) {
        setApp(app);
        whiteCircle = new Circle();
        blackCircle = new Circle();
        whiteCircle.setRadius(4*app.getScale());
        blackCircle.setRadius(4*app.getScale());
        whiteBoardPosition = whiteStart;
        root = new AnchorPane();
        stage = stageIn;
    }

    public static Game constructGame(boolean whiteStart, Stage stageIn, ChessBuddy app) {
        Game game = new Game(whiteStart, stageIn, app);
        game.initBoard(whiteStart);
        game.initRoot();
        game.preGame();
        return game;
    }

    public final void initBoard(boolean whiteStart) {
        if(whiteStart) {
            board.initWhiteBoard(GameInfo.INITIAL_BOARD,this);
        } else {
            board.initBlackBoard(GameInfo.INITIAL_BOARD,this);
        }
    }

    public void initRoot() {
        setSideBar(constructScoreBoard());
        //HBox topBar = constructTopBar();
        //HBox topBorder = constructTopBorder();
        VBox topPlayerPanel = constructTopPlayerPanelContainer();
        VBox bottomPlayerPanel = constructBottomPlayerPanelContainer();
        VBox mainButtons = constructTopPanel();
        VBox fenShow = constructFENTextAreaContainer();
        //AnchorPane.setTopAnchor(topBar, 1.0);
        //AnchorPane.setTopAnchor(getBoardGUI(), topBarHeight + 20.0);
        AnchorPane.setTopAnchor(getBoardGUI(), 20.0);
        AnchorPane.setLeftAnchor(getBoardGUI(), 20.0);

        AnchorPane.setTopAnchor(topPlayerPanel, (20.0));
        AnchorPane.setLeftAnchor(topPlayerPanel, (20+boardSize)+(stage.getWidth()-10-boardSize-barWidth)/2);

        AnchorPane.setTopAnchor(mainButtons, (97.0));
        AnchorPane.setLeftAnchor(mainButtons, (20+boardSize)+(stage.getWidth()-10-boardSize-barWidth)/2);

//        AnchorPane.setTopAnchor(sideBar, ((stage.getHeight() -
//                (elementHeight + scoreBoardHeight + elementHeight))/2)+10);
        AnchorPane.setTopAnchor(sideBar, (175.0));
        AnchorPane.setLeftAnchor(sideBar, (20+boardSize)+(stage.getWidth()-10-boardSize-barWidth)/2);

        AnchorPane.setTopAnchor(fenShow, (565.0));
        AnchorPane.setLeftAnchor(fenShow, (20+boardSize)+(stage.getWidth()-10-boardSize-barWidth)/2);

        AnchorPane.setTopAnchor(bottomPlayerPanel, (643.0));
        AnchorPane.setLeftAnchor(bottomPlayerPanel, (20+boardSize)+(stage.getWidth()-10-boardSize-barWidth)/2);

        root.setId("root");
        root.getChildren().addAll(sideBar,topPlayerPanel,mainButtons, fenShow,bottomPlayerPanel, getBoardGUI());
    }

    private void setApp(ChessBuddy app) {
        this.app = app;
        tileSize = 97*app.getScale(); //calculate GUI sizes based of ChessBuddy application scale
        boardSize = tileSize * HEIGHT;
        elementHeight = 80*app.getScale();
        scoreBoardHeight = 275*app.getScale();
        topBarHeight = 0; //*app.getScale();
        barWidth = (app.getWidth() - 85 - HEIGHT* tileSize);
        barHeight = app.getScale()*(app.getHeight()/1.5);
    }

    public double getElementHeight() {
        return elementHeight;
    }

    public double getScoreBoardHeight() {
        return scoreBoardHeight;
    }

    public double getTopBarHeight() {
        return topBarHeight;
    }

    public double getTileSize() {
        return tileSize;
    }

    public double getBarWidth() {
        return barWidth;
    }

    public double getBarHeight() {
        return barHeight;
    }

    public double getBoardSize() {
        return boardSize;
    }

    public final ChessBuddy getApp() {
        return app;
    }

    public ArrayList<Tile> getAttackingKing() {
        return board.getAttackingKing();
    }

    public ArrayList<Tile> getAttackWhiteListed() {
        return board.getAttackWhiteListed();
    }

    public Piece getBlackKing() {
        return board.getBlackKing();
    }

    public Piece getWhiteKing() {
        return board.getWhiteKing();
    }

    public ArrayList<Tile> kingMoves() {
        return board.getKingCanMove();
    }

    public Pane getBoardGUI() {
        return board.getBoardGUI();
    }

    public int getTimerType() {
        return timerType;
    }

    public final void setTimerType(int timerType) {
        this.timerType = timerType;
    }

    public boolean canRender() {
        return canRender;
    }

    public void setCanRender(boolean canRender) {
        this.canRender = canRender;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isFinished() {
        return finished;
    }

    public Stage getStage() {
        return stage;
    }

    public boolean isMoveReady() {
        return moveReadyState;
    }

    public void setMoveReadyState(boolean moveReadyState) {
        this.moveReadyState = moveReadyState;
    }

    public ArrayList<Selectable> getSelectable() {
        return selectable;
    }

    public VBox getSideBar() {
        return sideBar;
    }

    public void setSideBar(VBox sideBar) {
        this.sideBar = sideBar;
    }

    public NotationBoard getNotationTable() {
        return notationTable;
    }

    public void setNotationTable(NotationBoard notationTable) {
        this.notationTable = notationTable;
    }

    public void setIsWhiteTurn(boolean isWhiteTurn) {
        this.isWhiteTurn = isWhiteTurn;
    }

    public final boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public boolean inCheck() {
        return inCheck;
    }

    public AnchorPane getRoot() {
        return root;
    }

    public Tile[][] getTiles() {
        return board.getTiles();
    }

    public final GameInfo getGameInfo() {
        return gameInfo;
    }

    public void setSelectedTile(Tile selected) {
        this.selectedTile = selected;
    }

    public Tile getSelectedTile() {
        return selectedTile;
    }

    public void addToBoardGUI(Node n) {
        getBoardGUI().getChildren().add(n);
    }

    public void addSelectable(Tile tile) {
        Selectable selectable = new Selectable(tile, this, Selectable.LIGHT_GREY,
                Selectable.LIGHT_GREY, Selectable.GREY, Selectable.LIGHT_GREY, app){
            @Override
            public void move() {
                app.getClip().play();
                makeMove(tile);
                clearSelectable();
            }
        };
        selectable.relocate(tile.getXReal(), tile.getYReal());
        selectable.setHighlightsNoHover();
        this.addToBoardGUI(selectable);
        this.selectable.add(selectable);
    }

    public void addEnPassantSelectable(Tile tile, int offset) {
        Selectable selectable = new Selectable(tile, this, Selectable.LIGHT_GREY,
                Selectable.LIGHT_GREY, Selectable.GREY, Selectable.LIGHT_GREY, app) {
            @Override
            public void move() {
                app.getClip().play();
                makeMoveEnPassant(tile, offset);
                clearSelectable();
            }
        };
        selectable.relocate(tile.getXReal(), tile.getYReal());
        selectable.setHighlightsNoHover();
        this.addToBoardGUI(selectable);
        this.selectable.add(selectable);
    }

    public void addPromotionSelectable(Tile tile) {
        Selectable selectable = new Selectable(tile, this, Selectable.LIGHT_GREY,
                Selectable.LIGHT_GREY, Selectable.GREY, Selectable.LIGHT_GREY, app){
            @Override
            public void move() {
                boolean isWhite = getSelectedTile().getPiece().isWhite();
                promotionSelection(isWhite,app);
            }
        };
        selectable.relocate(tile.getXReal(), tile.getYReal());
        selectable.setHighlightsNoHover();
        this.addToBoardGUI(selectable);
        this.selectable.add(selectable);
    }

    public void addCastleSelectable(Tile tile, boolean forWhite, boolean kingSide) {
        Selectable selectable = new Selectable(tile, this, Selectable.LIGHT_GREY,
                Selectable.LIGHT_GREY, Selectable.GREY, Selectable.LIGHT_GREY, app){
            @Override
            public void move() {
                app.getClip().play();
                makeMoveCastle(forWhite, kingSide);
                clearSelectable();
            }
        };
        selectable.relocate(tile.getXReal(), tile.getYReal());
        selectable.setCrownIcon();
        this.addToBoardGUI(selectable);
        this.selectable.add(selectable);
    }

    public void addVisualize(Tile tile) {
        Selectable selectable = new Selectable(tile, this, Selectable.LIGHT_GREY,
                Selectable.LIGHT_GREY, Selectable.GREY, Selectable.LIGHT_GREY, app);
        selectable.relocate(tile.getXReal(), tile.getYReal());
        selectable.setHighlightsNoHover();
        this.addToBoardGUI(selectable);
        this.selectable.add(selectable);
    }

    private void renderTurn() {
        if(gameInfo.isLastTurnWhite()) {
            whiteCircle.setFill(GREEN);
            blackCircle.setFill(RED);
        } else {
            whiteCircle.setFill(RED);
            blackCircle.setFill(GREEN);
        }
    }

    public void clearSelectable() {
        if(selectedTile != null) {
            selectedTile.setUnselected();
            selectedTile = null;
        }
        selectable.forEach((Selectable selectable) -> getBoardGUI().getChildren().remove(selectable));
        selectable.clear();
    }

    public void highlightRecentTiles() {
        highlightedTiles.forEach(Tile::setUnHighLighted);
        highlightedTiles.clear();
        ArrayList<int[]> coordinates = gameInfo.getRecentlyMovedTileCoordinates();
        coordinates.forEach((coordinate)->{
            highlightedTiles.add(board.getTiles()[coordinate[0]][coordinate[1]]);
            board.getTiles()[coordinate[0]][coordinate[1]].setHighLighted();
        });
    }

    public Move getLastMove() {
        ArrayList<Move> moves = getGameInfo().getMoves();
        int num = getGameInfo().getMoveNum();
        if(!moves.isEmpty() && num > -1) {
            return moves.get(num);
        } else {
            return null;
        }
    }

    public final void preGame() {
        board.calculateMoves(isWhiteTurn());
        renderTurn();
    }

    public boolean inCheck(Piece king, int row, int col) {
        return board.inCheck(king, row, col);
    }

    private void preMove() {
        board.calculateMoves(isWhiteTurn());
        if(!board.getAttackingKing().isEmpty()) {
            inCheck = true;
            board.getKing(isWhiteTurn()).getTile().setInCheck();
            if(board.getKingCanMove().isEmpty() && !board.hasLegalMoves(isWhiteTurn()))  {
                gameInfo.setRecentCheckMate();
                gameResult = CHECKMATE;
                onGameFinished();
            } else {
                gameInfo.setRecentCheck();
            }
        } else if(!board.hasLegalMoves(isWhiteTurn())) {
            gameInfo.setRecentStaleMate();
            gameResult = STALEMATE;
            onGameFinished();
        }
        renderTurn();
        highlightRecentTiles();
    }

    public void onGameFinished() {
        setFinished(true);
    }

    public void makeMove(Tile tile) {
        move(tile);
        if(gameResult == CHECKMATE) {
            String msg = isWhiteTurn() ? "Checkmate : 0-1" : "Checkmate : 1-0";
            notationTable.addFinishedMessage(msg);
        } else if(gameResult == STALEMATE) {
            notationTable.addFinishedMessage("Stalemate : Draw");
        }
        gameInfo.updateFEN();
    }

    public void makeMoveEnPassant(Tile tile, int offset) {
        moveEnPassant(tile,offset);
        if(gameResult == CHECKMATE) {
            String msg = isWhiteTurn() ? "Checkmate : 0-1" : "Checkmate : 1-0";
            notationTable.addFinishedMessage(msg);
        } else if(gameResult == STALEMATE) {
            notationTable.addFinishedMessage("Stalemate : Draw");
        }
    }

    public void makeMovePromotion(Tile tile, Piece piece) {
        movePromotion(tile, piece);
        if(gameResult == CHECKMATE) {
            String msg = isWhiteTurn() ? "Checkmate : 0-1" : "Checkmate : 1-0";
            notationTable.addFinishedMessage(msg);
        } else if(gameResult == STALEMATE) {
            notationTable.addFinishedMessage("Stalemate : Draw");
        }
    }

    public void makeMoveCastle(boolean forWhite, boolean kingSide) {
        moveCastle(forWhite, kingSide);
        if(gameResult == CHECKMATE) {
            String msg = isWhiteTurn() ? "Checkmate : 0-1" : "Checkmate : 1-0";
            notationTable.addFinishedMessage(msg);
        } else if(gameResult == STALEMATE) {
            notationTable.addFinishedMessage("Stalemate : Draw");
        }
    }

    private void move(Tile tile) {
        int oldNot = gameInfo.getMoveNum();
        board.getBlackKing().getTile().setOffCheck();
        board.getWhiteKing().getTile().setOffCheck();
        inCheck = false;
        Tile selected = selectedTile;
        Piece taken = tile.getPiece();
        gameInfo.makeMove(selected,tile);
        selected.movePiece(tile);
        if(taken != null) {
            removeTaken(taken);
            gameInfo.setRecentCapture();
        }
        isWhiteTurn = !isWhiteTurn;
        preMove();
        notationTable.addLastToGUI();
        notationTable.selectEntry(gameInfo.getMoveNum(), oldNot);
    }

    private void moveEnPassant(Tile tile, int offset) {
        int oldNot = gameInfo.getMoveNum();
        board.getBlackKing().getTile().setOffCheck();
        board.getWhiteKing().getTile().setOffCheck();
        inCheck = false;
        Tile selected = selectedTile;
        Piece taken = board.getTiles()[tile.getRow()+offset][tile.getCol()].getPiece();
        gameInfo.makeMoveEnPassant(selected,tile,taken);
        selected.movePieceEnPassant(tile,board.getTiles()[tile.getRow()+offset][tile.getCol()]);
        removeTaken(taken);
        isWhiteTurn = !isWhiteTurn;
        gameInfo.setRecentEnPassant();
        preMove();
        notationTable.addLastToGUI();
        notationTable.selectEntry(gameInfo.getMoveNum(), oldNot);
    }

    private void movePromotion(Tile tile, Piece promotionTo) {
        int oldNot = gameInfo.getMoveNum();
        board.getBlackKing().getTile().setOffCheck();
        board.getWhiteKing().getTile().setOffCheck();
        inCheck = false;
        Tile selected = selectedTile;
        Piece taken = tile.getPiece();
        gameInfo.makeMovePromotion(selected,tile,promotionTo);
        selected.movePiece(tile);
        if(taken != null) {
            removeTaken(taken);
            gameInfo.setRecentCapture();
        }
        promotionDelay(tile.getPiece(),promotionTo);
        tile.setPiece(promotionTo);
        if (isWhiteTurn()) {
            board.getBlackNotKing().remove(taken);
            board.getWhiteNotKing().add(promotionTo);
        } else {
            board.getWhiteNotKing().remove(taken);
            board.getBlackNotKing().add(promotionTo);
        }
        isWhiteTurn = !isWhiteTurn;
        gameInfo.setRecentPromotion(promotionTo);
        preMove();
        notationTable.addLastToGUI();
        notationTable.selectEntry(gameInfo.getMoveNum(), oldNot);
    }

    private void moveCastle(boolean forWhite, boolean kingSide) {
        int oldNot = gameInfo.getMoveNum();
        board.getBlackKing().getTile().setOffCheck();
        board.getWhiteKing().getTile().setOffCheck();
        inCheck = false;
        if(forWhite) {
            if(kingSide) {
                //king side white castle
                board.getTiles()[LOWER_BOUNDARY][UPPER_BOUNDARY].getPiece().toFront(); //rook to front
                gameInfo.makeMoveCastleKingSide(board.getWhiteKing(), board.getTiles()[LOWER_BOUNDARY][UPPER_BOUNDARY].getPiece());
                board.getWhiteKing().getTile().movePiece(board.getTiles()[LOWER_BOUNDARY][UPPER_BOUNDARY-1]);
                board.getTiles()[LOWER_BOUNDARY][UPPER_BOUNDARY].movePiece(board.getTiles()[LOWER_BOUNDARY][UPPER_BOUNDARY-2]);
            } else {
                //queen side white castle
                board.getTiles()[LOWER_BOUNDARY][LOWER_BOUNDARY].getPiece().toFront(); //rook to front
                gameInfo.makeMoveCastleQueenSide(board.getWhiteKing(), board.getTiles()[LOWER_BOUNDARY][LOWER_BOUNDARY].getPiece());
                board.getWhiteKing().getTile().movePiece(board.getTiles()[LOWER_BOUNDARY][LOWER_BOUNDARY+2]);
                board.getTiles()[LOWER_BOUNDARY][LOWER_BOUNDARY].movePiece(board.getTiles()[LOWER_BOUNDARY][LOWER_BOUNDARY+3]);
            }
        } else {
            if(kingSide) {
                //king side black castle
                board.getTiles()[UPPER_BOUNDARY][UPPER_BOUNDARY].getPiece().toFront(); //rook to front
                gameInfo.makeMoveCastleKingSide(board.getBlackKing(), board.getTiles()[UPPER_BOUNDARY][UPPER_BOUNDARY].getPiece());
                board.getBlackKing().getTile().movePiece(board.getTiles()[UPPER_BOUNDARY][UPPER_BOUNDARY-1]);
                board.getTiles()[UPPER_BOUNDARY][UPPER_BOUNDARY].movePiece(board.getTiles()[UPPER_BOUNDARY][UPPER_BOUNDARY-2]);
            } else {
                //queen side black castle
                board.getTiles()[UPPER_BOUNDARY][LOWER_BOUNDARY].getPiece().toFront(); //rook to front
                gameInfo.makeMoveCastleQueenSide(board.getBlackKing(), board.getTiles()[UPPER_BOUNDARY][LOWER_BOUNDARY].getPiece());
                board.getBlackKing().getTile().movePiece(board.getTiles()[UPPER_BOUNDARY][LOWER_BOUNDARY+2]);
                board.getTiles()[UPPER_BOUNDARY][LOWER_BOUNDARY].movePiece(board.getTiles()[UPPER_BOUNDARY][LOWER_BOUNDARY+3]);
            }
        }
        isWhiteTurn = !isWhiteTurn;
        preMove();
        notationTable.addLastToGUI();
        notationTable.selectEntry(gameInfo.getMoveNum(), oldNot);
    }

    private void removeTaken(Piece taken) {
        if (isWhiteTurn()) {
            board.getBlackNotKing().remove(taken);
        } else {
            board.getWhiteNotKing().remove(taken);
        }
        Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    FadeTransition ft = new FadeTransition(Duration.millis(150), taken);
                    ft.setFromValue(1.0);
                    ft.setToValue(0.1);
                    ft.play();
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {
                }
                return null;
            }
        };
        sleeper.setOnSucceeded((WorkerStateEvent event) -> getBoardGUI().getChildren().remove(taken));
        new Thread(sleeper).start();
    }

    private void promotionDelay(Piece oldPiece, Piece newPiece) {
        Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {
                }
                return null;
            }
        };
        sleeper.setOnSucceeded((WorkerStateEvent event) -> {
            getBoardGUI().getChildren().remove(oldPiece);
            getBoardGUI().getChildren().add(newPiece);
        });
        new Thread(sleeper).start();
    }

    public boolean canCastle(Piece king, boolean kingSide) {
        return gameInfo.canCastle(king.isWhite(), kingSide);
    }

    public void copyFENToClip() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        StringSelection strSel = new StringSelection(gameInfo.getMoveFEN());
        clipboard.setContents(strSel, null);
    }


    public void savePGNAsFile() {
        String result;
        if(finished) {
            if(isWhiteTurn()) {
                result = "[Result 0-1]";
            } else {
                result = "[Result 1-0]";
            }
        } else {
            result = "[Result *]";
        }
        String PGN = gameInfo.getGamePGN(result);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Game as PGN");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text", "*.txt"),
                new FileChooser.ExtensionFilter("Notation", "*.pgn")
        );
        Stage newWindow = new Stage();
        newWindow.setResizable(false);

        newWindow.setX(stage.getX() + stage.getWidth()/3);
        newWindow.setY(stage.getY() + stage.getHeight()/3);
        newWindow.initOwner(stage);
        newWindow.initModality(Modality.APPLICATION_MODAL);
        File file = fileChooser.showSaveDialog(newWindow);
        if(file != null) {
            try {
                if (!file.createNewFile()) {
                    file.delete();
                    file.createNewFile();
                }
                writeStringToFile(PGN, file);
            } catch (IOException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void writeStringToFile(String string, File file) {
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file,false))) {
                writer.write(string);
            }
        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void reRenderBoard() {
        root.getChildren().remove(getBoardGUI());
        if(whiteBoardPosition) {
            board.initWhiteBoard(gameInfo.getBoardByNumber(gameInfo.getMoveNum()),this);
        } else {
            board.initBlackBoard(gameInfo.getBoardByNumber(gameInfo.getMoveNum()),this);
        }
        AnchorPane.setTopAnchor(getBoardGUI(), topBarHeight + 20.0);
        AnchorPane.setLeftAnchor(getBoardGUI(), 15.0);
        root.getChildren().add(getBoardGUI());
        preMove();
    }

    public void flipBoardGUI() {
        root.getChildren().remove(getBoardGUI());
        if(whiteBoardPosition) {
            board.initBlackBoard(gameInfo.getBoardByNumber(gameInfo.getMoveNum()),this);
        } else {
            board.initWhiteBoard(gameInfo.getBoardByNumber(gameInfo.getMoveNum()),this);
        }
        AnchorPane.setTopAnchor(getBoardGUI(), topBarHeight + 20.0);
        AnchorPane.setLeftAnchor(getBoardGUI(), 15.0);
        root.getChildren().add(getBoardGUI());
        whiteBoardPosition = !whiteBoardPosition;
        preMove();
    }

    public void takeBackMove() {
        int oldNum = gameInfo.getMoveNum();
        if(gameInfo.canTakeBack()) {
            root.getChildren().remove(getBoardGUI());
            if(whiteBoardPosition) {
                board.initWhiteBoard(gameInfo.getBeforeLastBoard(),this);
            } else {
                board.initBlackBoard(gameInfo.getBeforeLastBoard(),this);
            }
            AnchorPane.setTopAnchor(getBoardGUI(), topBarHeight + 20.0);
            AnchorPane.setLeftAnchor(getBoardGUI(), 15.0);
            root.getChildren().add(getBoardGUI());
            gameInfo.takeBackMove();
            isWhiteTurn = gameInfo.getMoveNum() % 2 != 0;
            preMove();
            notationTable.removeLastFromGUI();
            if(oldNum < gameInfo.getMoveNum()) {
                notationTable.selectEntry(gameInfo.getMoveNum(),oldNum);
            } else {
                notationTable.selectEntry(gameInfo.getMoveNum());
            }
            moveReadyState = true;
            finished = false;
            notationTable.removeFinishedMessage();
            gameResult = IN_PROGRESS;
        }
    }

    public void goLeft() {
        if(gameInfo.canGoLeft()) {
            root.getChildren().remove(getBoardGUI());
            if (whiteBoardPosition) {
                board.initWhiteBoard(gameInfo.getBoardByNumber(gameInfo.getMoveNum()-1),this);
            } else {
                board.initBlackBoard(gameInfo.getBoardByNumber(gameInfo.getMoveNum()-1),this);
            }
            AnchorPane.setTopAnchor(getBoardGUI(), topBarHeight + 20.0);
            AnchorPane.setLeftAnchor(getBoardGUI(), 15.0);
            root.getChildren().add(getBoardGUI());
            gameInfo.goLeft();
            isWhiteTurn = gameInfo.getMoveNum() % 2 != 0;
            preMove();
            notationTable.selectEntry(gameInfo.getMoveNum(), gameInfo.getMoveNum()+1);
            moveReadyState = false;
        }
    }

    public void goRight() {
        if(gameInfo.canGoRight()) {
            root.getChildren().remove(getBoardGUI());
            if (whiteBoardPosition) {
                board.initWhiteBoard(gameInfo.getBoardByNumber(gameInfo.getMoveNum()+1),this);
            } else {
                board.initBlackBoard(gameInfo.getBoardByNumber(gameInfo.getMoveNum()+1),this);
            }
            AnchorPane.setTopAnchor(getBoardGUI(), topBarHeight + 20.0);
            AnchorPane.setLeftAnchor(getBoardGUI(), 15.0);
            root.getChildren().add(getBoardGUI());
            gameInfo.goRight();
            isWhiteTurn = gameInfo.getMoveNum() % 2 != 0;
            preMove();
            notationTable.selectEntry(gameInfo.getMoveNum(), gameInfo.getMoveNum()-1);
            moveReadyState = gameInfo.getMoveNum() == gameInfo.getMoveCount()-1;
        }
    }

    public void goFarLeft() {
        if(gameInfo.canGoLeft()) {
            int oldNum = gameInfo.getMoveNum();
            root.getChildren().remove(getBoardGUI());
            if (whiteBoardPosition) {
                board.initWhiteBoard(GameInfo.INITIAL_BOARD,this);
            } else {
                board.initBlackBoard(GameInfo.INITIAL_BOARD,this);
            }
            AnchorPane.setTopAnchor(getBoardGUI(), topBarHeight + 20.0);
            AnchorPane.setLeftAnchor(getBoardGUI(), 15.0);
            root.getChildren().add(getBoardGUI());
            gameInfo.goFarLeft();
            isWhiteTurn = gameInfo.getMoveNum() % 2 != 0;
            preMove();
            notationTable.selectEntry(-1, oldNum);
            moveReadyState = false;
        }
    }

    public void goFarRight() {
        if(gameInfo.canGoRight()) {
            int oldNum = gameInfo.getMoveNum();
            root.getChildren().remove(getBoardGUI());
            if (whiteBoardPosition) {
                board.initWhiteBoard(gameInfo.getLastBoard(),this);
            } else {
                board.initBlackBoard(gameInfo.getLastBoard(),this);
            }
            AnchorPane.setTopAnchor(getBoardGUI(), topBarHeight + 20.0);
            AnchorPane.setLeftAnchor(getBoardGUI(), 15.0);
            root.getChildren().add(getBoardGUI());
            gameInfo.goFarRight();
            isWhiteTurn = gameInfo.getMoveNum() % 2 != 0;
            preMove();
            notationTable.selectEntry(gameInfo.getMoveCount()-1, oldNum);
            moveReadyState = gameInfo.getMoveNum() == gameInfo.getMoveCount()-1;
        }
    }

    public void goTo(int num) {
        if(num != gameInfo.getMoveNum()) {
            int oldNum = gameInfo.getMoveNum();
            root.getChildren().remove(getBoardGUI());
            if (whiteBoardPosition) {
                board.initWhiteBoard(gameInfo.getBoardByNumber(num),this);
            } else {
                board.initBlackBoard(gameInfo.getBoardByNumber(num),this);
            }
            AnchorPane.setTopAnchor(getBoardGUI(), topBarHeight + 20.0);
            AnchorPane.setLeftAnchor(getBoardGUI(), 15.0);
            root.getChildren().add(getBoardGUI());
            gameInfo.goTo(num);
            isWhiteTurn = gameInfo.getMoveNum() % 2 != 0;
            preMove();
            notationTable.selectEntry(gameInfo.getMoveNum(), oldNum);
            moveReadyState = gameInfo.getMoveNum() == gameInfo.getMoveCount()-1;
        }
    }

    public void resetGame() {
        stage.getScene().setRoot(app.createPlayPane(stage));
    }

    public HBox constructTopBorder() {
        HBox buttons = new HBox();
        buttons.setId("darkborder");
        buttons.setMinSize(app.getWidth(),1);
        buttons.setMaxSize(app.getWidth(),1);
        return buttons;
    }

    public HBox constructButtonPanel() {
        HBox buttons = new HBox();
        buttons.setId("topborder");
        buttons.setMinSize(barWidth,elementHeight);
        buttons.setMaxSize(barWidth,elementHeight);
        buttons.setSpacing(20*app.getScale());
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(constructFarLeftButton(),constructLeftButton(),
                constructRightButton(), constructFarRightButton());
        return buttons;
    }

    //// construct group of main buttons
    public HBox constructButtonPanelMain() {
        HBox buttons = new HBox();
        buttons.setId("topborder");
        buttons.setMinSize(barWidth,elementHeight);
        buttons.setMaxSize(barWidth,elementHeight);
        buttons.setSpacing(20*app.getScale());
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(constructFlipButtonAlt(),constructBackButtonAlt(),
                constructExportPGNAlt(),constructResetButtonAlt());
        return buttons;
    }

    ///// construct export PGN for main panel
    public Button constructExportPGNAlt() {
        Button exportButton = new Button();

        exportButton.setPrefHeight(50.0);
        exportButton.setPrefWidth(70.0);
        ImageView image = new ImageView(new Image("/resources/images/save_game.png"));
        image.setFitHeight(26.0);
        image.setFitWidth(26.0);
        image.setPickOnBounds(true);
        image.setPreserveRatio(true);

        exportButton.setGraphic(image);
        exportButton.setFocusTraversable(false);
        exportButton.setId("boardbutton");
        exportButton.setOnAction((event)-> savePGNAsFile());
        return exportButton;
    }



    //// Construct flip button for main panel
    public Button constructFlipButtonAlt() {
        Button flipButton = new Button();

        flipButton.setPrefHeight(50.0);
        flipButton.setPrefWidth(70.0);
        ImageView image = new ImageView(new Image("/resources/images/flip_board.png"));

        image.setFitHeight(30.0);
        image.setFitWidth(30.0);
        image.setPickOnBounds(true);
        image.setPreserveRatio(true);
        flipButton.setGraphic(image);
        flipButton.setFocusTraversable(false);
        flipButton.setId("boardbutton");
        flipButton.setOnAction((event)-> flipBoardGUI());
        return flipButton;
    }


    //// Construct reset button for main panel
    public Button constructResetButtonAlt() {
        Button resetButton = new Button();

        resetButton.setPrefHeight(50.0);
        resetButton.setPrefWidth(70.0);
        ImageView image = new ImageView(new Image("/resources/images/reset_game.png"));
        image.setFitHeight(26.0);
        image.setFitWidth(26.0);
        image.setPickOnBounds(true);
        image.setPreserveRatio(true);

        resetButton.setGraphic(image);
        resetButton.setFocusTraversable(false);
        resetButton.setId("boardbutton");
        resetButton.setOnAction((event)-> resetGame());
        return resetButton;
    }

    public Button constructLeftButton() {
        Button leftButton = new Button();

        leftButton.setPrefHeight(50.0);
        leftButton.setPrefWidth(70.0);
        ImageView image = new ImageView(new Image("/resources/images/single_left.png" ));
        image.setFitHeight(18.0);
        image.setFitWidth(18.0);
        image.setPickOnBounds(true);
        image.setPreserveRatio(true);

        leftButton.setGraphic(image);
        leftButton.setFocusTraversable(false);
        leftButton.setId("boardbutton");
        leftButton.setOnAction((event)-> goLeft());
        return leftButton;
    }

    ////// CONSTRUCT FAR LEFT BUTTON //////
    public Button constructFarLeftButton() {
        Button farleftButton = new Button();
        farleftButton.setPrefHeight(50.0);
        farleftButton.setPrefWidth(70.0);
        ImageView image = new ImageView(new Image("/resources/images/double_left.png" ));
        image.setFitHeight(29.0);
        image.setFitWidth(29.0);
        image.setPickOnBounds(true);
        image.setPreserveRatio(true);

        farleftButton.setGraphic(image);
        farleftButton.setFocusTraversable(false);
        farleftButton.setId("boardbutton");
        farleftButton.setOnAction((event)-> goFarLeft());
        return farleftButton;
    }


    public Button constructRightButton() {
        Button rightButton = new Button();

        rightButton.setPrefHeight(50.0);
        rightButton.setPrefWidth(70.0);
        ImageView image = new ImageView(new Image("/resources/images/single_right.png" ));
        image.setFitHeight(18.0);
        image.setFitWidth(18.0);
        image.setPickOnBounds(true);
        image.setPreserveRatio(true);

        rightButton.setGraphic(image);
        rightButton.setFocusTraversable(false);
        rightButton.setId("boardbutton");
        rightButton.setOnAction((event)-> goRight());
        return rightButton;
    }

    ////// CONSTRUCT FAR RIGHT BUTTON //////
    public Button constructFarRightButton() {
        Button farrightButton = new Button();
        farrightButton.setPrefHeight(50.0);
        farrightButton.setPrefWidth(70.0);
        ImageView image = new ImageView(new Image("/resources/images/double_right.png" ));
        image.setFitHeight(29.0);
        image.setFitWidth(29.0);
        image.setPickOnBounds(true);
        image.setPreserveRatio(true);

        farrightButton.setGraphic(image);
        farrightButton.setFocusTraversable(false);
        farrightButton.setId("boardbutton");
        farrightButton.setOnAction((event)-> goFarRight());

        return farrightButton;
    }


    //// Construct back button for main panel
    public Button constructBackButtonAlt() {
        Button backButton = new Button();

        backButton.setPrefHeight(50.0);
        backButton.setPrefWidth(70.0);

        ImageView image = new ImageView(new Image("/resources/images/move_back.png"));
        image.setFitHeight(25.0);
        image.setFitWidth(25.0);
        image.setPickOnBounds(true);
        image.setPreserveRatio(true);

        backButton.setGraphic(image);
        backButton.setFocusTraversable(false);
        backButton.setId("boardbutton");
        backButton.setOnAction((event)-> takeBackMove());
        return backButton;
    }

    ////// construct FEN showing textarea //////
    public VBox constructFENTextAreaContainer() {
        VBox fenTextAreaCont = new VBox();
        fenTextAreaCont.setId("scoreoutsets");
        HBox fenTextArea = constructFENTextArea(); ///
        fenTextAreaCont.getChildren().addAll(fenTextArea);
        return fenTextAreaCont;

    }

    ////// the FEN showing textarea itself //////
    public HBox constructFENTextArea() {
        HBox fenTextArea = new HBox();
        fenTextArea.setId("fenarea");
        fenTextArea.getChildren().addAll(constructTextArea());
        return fenTextArea;
    }

    ////// construct the text area for FEN //////
    public TextArea constructTextArea() {
        TextArea fenTextArea = new TextArea();
        fenTextArea.setFocusTraversable(false);
        fenTextArea.setId("fenarea");
        fenTextArea.setMinSize(barWidth,elementHeight);
        fenTextArea.setMaxSize(barWidth,elementHeight);
        fenTextArea.setEditable(false);
        fenTextArea.setWrapText(true);
        fenTextArea.textProperty().bind(gameInfo.fenProperty());
        fenTextArea.setFont(new Font("Inter", 16.0));

        return fenTextArea;
    }


    ////// construct top player panel for player 2 ///////
    public VBox constructTopPlayerPanelContainer() {
        VBox topPlayerPanel = new VBox();
        topPlayerPanel.setId("scoreoutsets");
        //topPlayerPanel.setPrefHeight(60.0);
        //topPlayerPanel.setPrefWidth(300.0);
        topPlayerPanel.setSpacing(10.0);
        topPlayerPanel.setAlignment(Pos.CENTER);
        HBox playerPanel = constructTopPlayerPanel(); ///
        topPlayerPanel.getChildren().addAll(playerPanel);
        return topPlayerPanel;
    }

    ////// the top player panel itself ///////
    public HBox constructTopPlayerPanel() {
        HBox imgLabel = new HBox();
        imgLabel.setId("topborder");
        imgLabel.setMinSize(barWidth,elementHeight);
        imgLabel.setMaxSize(barWidth,elementHeight);
        imgLabel.setSpacing(20*app.getScale());
        imgLabel.setAlignment(Pos.CENTER_LEFT);
        imgLabel.setPadding(new Insets(0,0,0,20*app.getScale()));
        imgLabel.getChildren().addAll(constructTopPlayerPanelImage(), constructTopPlayerPanelLabel());
        return imgLabel;
    }

    ////// the top player panel image ///////
    public ImageView constructTopPlayerPanelImage() {
        ImageView image = new ImageView(new Image("/resources/images/black_icon_player.png"));
        image.setFitHeight(46.0);
        image.setFitWidth(46.0);
        image.setPickOnBounds(true);
        image.setPreserveRatio(true);
        return image;
    }

    ////// the top player panel label ///////
    public Label constructTopPlayerPanelLabel() {
        Label label = new Label();
        label.setId("playerlabel");
        label.setMinSize(barWidth,elementHeight);
        label.setMaxSize(barWidth,elementHeight);
        label.setText("Player 2");
        //label.setPadding(new Insets(0,10,0,0*app.getScale()));
        label.setGraphic(blackCircle);
        label.setAlignment(Pos.CENTER_LEFT);
        label.setContentDisplay(ContentDisplay.RIGHT);
        return label;
    }


    ////// construct bottom player panel for player 1 ///////
    public VBox constructBottomPlayerPanelContainer() {
        VBox bottomPlayerPanel = new VBox();
        bottomPlayerPanel.setId("scoreoutsets");
        //bottomPlayerPanel.setPrefHeight(60.0);
        //bottomPlayerPanel.setPrefWidth(300.0);
        bottomPlayerPanel.setSpacing(10.0);
        bottomPlayerPanel.setAlignment(Pos.CENTER);
        HBox playerPanel = constructBottomPlayerPanel(); ///
        bottomPlayerPanel.getChildren().addAll(playerPanel);
        return bottomPlayerPanel;
    }

    ////// the bottom player panel itself ///////
    public HBox constructBottomPlayerPanel() {
        HBox imgLabel = new HBox();
        imgLabel.setId("topborder");
        imgLabel.setMinSize(barWidth,elementHeight);
        imgLabel.setMaxSize(barWidth,elementHeight);
        imgLabel.setSpacing(20*app.getScale());
        imgLabel.setAlignment(Pos.CENTER_LEFT);
        imgLabel.setPadding(new Insets(0,0,0,20*app.getScale()));
        imgLabel.getChildren().addAll(constructBottomPlayerPanelImage(), constructBottomPlayerPanelLabel());
        return imgLabel;
    }

    ////// the bottom player panel image ///////
    public ImageView constructBottomPlayerPanelImage() {
        ImageView image = new ImageView(new Image("/resources/images/white_icon_player.png"));
        image.setFitHeight(46.0);
        image.setFitWidth(46.0);
        image.setPickOnBounds(true);
        image.setPreserveRatio(true);
        return image;
    }

    ////// the bottom player panel label ///////
    public Label constructBottomPlayerPanelLabel() {
        Label label = new Label();
        label.setId("playerlabel");
        label.setMinSize(barWidth,elementHeight);
        label.setMaxSize(barWidth,elementHeight);
        label.setText("Player 1");
        //label.setPadding(new Insets(0,10,0,0*app.getScale()));
        label.setGraphic(whiteCircle);
        label.setAlignment(Pos.CENTER_LEFT);
        label.setContentDisplay(ContentDisplay.RIGHT);
        return label;
    }


    ////// construct top panel for main buttons
    public VBox constructTopPanel() {
        VBox topPanel = new VBox();
        topPanel.setId("scoreoutsets");
        topPanel.setPrefHeight(50.0);
        topPanel.setPrefWidth(300.0);
        topPanel.setSpacing(10.0);
        topPanel.setAlignment(Pos.CENTER);
        HBox mainButtons = constructButtonPanelMain();
        topPanel.getChildren().addAll(mainButtons);
        return topPanel;
    }

    public VBox constructScoreBoard() {
        VBox sidebar = new VBox();
        sidebar.setId("scoreoutsets");
        sidebar.setMinSize(barWidth, elementHeight + scoreBoardHeight + elementHeight);
        sidebar.setMaxSize(barWidth, elementHeight + scoreBoardHeight + elementHeight);
        sidebar.setAlignment(Pos.CENTER);
        HBox titles = constructTitles();
        HBox bottomButtons = constructButtonPanel();
        setUpNotationGUI();
        HBox notationHBox = new HBox();
        notationHBox.getChildren().add(notationTable);
        notationHBox.setPadding(new Insets(0,barWidth*0.1,0,barWidth*0.1));
        sidebar.getChildren().addAll(titles,notationHBox, bottomButtons);
        return sidebar;
    }


    public final void setUpNotationGUI() {
        notationTable = constructNotationTable();
    }

    public final NotationBoard constructNotationTable() {
        VBox vertical = new VBox();
        NotationBoard table = new NotationBoard(gameInfo.getMoves(), vertical, this, app);
        table.setId("scrollborder");
        table.setFocusTraversable(false);
        double width = barWidth*0.88;
        vertical.setId("scrollpanebg");
        vertical.setPadding(new Insets(25,20*app.getScale(),25,20*app.getScale()));
        vertical.setFocusTraversable(false);
        table.setContent(vertical);
        table.setMinSize(width, scoreBoardHeight);
        table.setMaxSize(width, scoreBoardHeight);
        table.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);
        table.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        table.fitToWidthProperty().set(true);
        table.fitToHeightProperty().set(true);
        return table;
    }

    public final HBox constructTitles() {
        HBox titles = new HBox();
        titles.setMinSize(barWidth, elementHeight);
        titles.setMaxSize(barWidth, elementHeight);
        Label white = new Label(" White");
        white.setFont(new Font("InterMedium", 20*app.getScale()));
        white.setAlignment(Pos.CENTER);
        white.setMinSize(barWidth/2, elementHeight);
        white.setMaxSize(barWidth/2, elementHeight);
        white.setId("lightborderright");
        Label black = new Label(" Black");
        black.setFont(new Font("InterMedium", 20*app.getScale()));
        black.setAlignment(Pos.CENTER);
        black.setMinSize(barWidth/2, elementHeight);
        black.setMaxSize(barWidth/2, elementHeight);
        black.setId("lightborderleft");
        titles.getChildren().addAll(white, black);
        return titles;
    }

}

