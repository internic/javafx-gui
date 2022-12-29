package gui;

import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayList;

public abstract class Piece extends StackPane {


    public double tileSize; // size of pieces
    private boolean closeable = false; //will a mouse release close the piece
    private boolean drag; //is the piece currently being dragged
    private double mouseX, mouseY; //position of mouse
    private double oldX, oldY;
    private double initialX, initialY; //initial position when mouse is clicked
    private final boolean isWhite;
    private Tile tile; //tile piece is on
    private final ArrayList<Tile> available = new ArrayList<>(); //available tiles of piece used for render

    public double getTileSize() {
        return tileSize;
    }

    private void setTileSize(double scale) {
        this.tileSize = 100*scale; // size of pieces
    }

    public boolean isCloseable() {
        return closeable;
    }

    public void setCloseable(boolean closeable) {
        this.closeable = closeable;
    }

    public ArrayList<Tile> getAvailable() {
        return available;
    }

    public Tile getTile() {
        return tile;
    }

    public final Game getController() {
        return tile.getController();
    }

    public void setOldX(double oldX) {
        this.oldX = oldX;
    }

    public void setOldY(double oldY) {
        this.oldY = oldY;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public double getOldX() {
        return oldX;
    }

    public double getOldY() {
        return oldY;
    }

    public boolean isQueen() {
        return false;
    }

    public boolean isRook() {
        return false;
    }

    public boolean isBishop() {
        return false;
    }

    public boolean isKing() {
        return false;
    }

    public boolean isKnight() {
        return false;
    }

    public boolean isPawn() {
        return false;
    }

    public Piece(boolean isWhiteIn, Tile tileIn) {
        this.setStyle("-fx-cursor: hand;");
        isWhite = isWhiteIn;
        tile = tileIn;
        setTileSize(tile.getController().getApp().getScale());
        moveTo(tile);
        setOnMousePressed(e -> {
            doRender();
            toFront();
        });
        setOnMouseDragged((e)->{
            boolean isWhiteTurn = getController().isWhiteTurn();
            if(isWhiteTurn == isWhite() && !getController().isFinished() && getController().isMoveReady()) {
                if(drag) {
                    double x = e.getSceneX() - mouseX + initialX;
                    double y = e.getSceneY() - mouseY + initialY;
                    setTranslateX(x);
                    setTranslateY(y);
                } else {
                    drag = true;
                    toFront();
                    mouseX = e.getSceneX();
                    mouseY = e.getSceneY();
                    Bounds pieceBounds = this.localToScene(this.getBoundsInLocal());
                    double x = e.getSceneX() - (pieceBounds.getMinX() + (tile.getTileSize() / 2)) + oldX;
                    double y = e.getSceneY() - (pieceBounds.getMinY() + (tile.getTileSize() / 2)) + oldY;
                    initialX = x;
                    initialY = y;
                    setTranslateX(x);
                    setTranslateY(y);
                }
            }
        });
        setOnMouseReleased((e)->{
            if(drag) {
                double x = e.getSceneX() - mouseX + initialX;
                double y = e.getSceneY() - mouseY + initialY;
                for(Selectable selectable : getController().getSelectable()) {
                    if(isSelectableCollision(e.getSceneX(), e.getSceneY(), selectable)) {
                        oldX = x;
                        oldY = y;
                        selectable.move();
                        closeable = false;
                        return;
                    }
                }
                setTranslateX(oldX);
                setTranslateY(oldY);
                closeable = true;
            } else {
                if (closeable) {
                    closeable = false;
                    getController().clearSelectable();
                } else {
                    closeable = true;
                }
            }
            drag = false;
        });
    }

    private boolean isSelectableCollision(double x, double y, Selectable selectable) {
        Bounds selectableBounds = selectable.localToScene(selectable.getBoundsInLocal());
        return selectableBounds.contains(x, y);
    }

    public abstract String getNotation();

    public abstract byte getInfoCode();

    public abstract int getValue();

    public final void moveTo(Tile tile) {
        oldX = tile.getXReal();
        oldY = tile.getYReal();
        setTranslateX(oldX);
        setTranslateY(oldY);
    }

    public final void moveToSlowly(Tile tile) {
        double x = tile.getXReal() - oldX;
        double y = tile.getYReal() - oldY;
        int time = 150;
        if(distance(x,y) < (tile.getTileSize())/2) {
            time = 5;
        }
        TranslateTransition tt = new TranslateTransition(Duration.millis(time), this);
        tt.setByX(x);
        tt.setByY(y);
        tt.play();
        oldX = tile.getXReal();
        oldY = tile.getYReal();
    }

    public double distance(double x, double y) {
        return Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
    }

    public final boolean withinBounds(int row, int col) {
        return (row < 8 && row > -1) && (col < 8 && col > -1);
    }

    protected void doRender() {
        boolean isWhiteTurn = getController().isWhiteTurn();
        Tile last = getController().getSelectedTile();
        if(last != null && last.getPiece() != this) {
            last.getPiece().setCloseable(false);
        }
        getController().clearSelectable();
        getTile().setSelected();
        getController().setSelectedTile(getTile());
        if (isWhiteTurn == isWhite() && getController().isMoveReady() && !getController().isFinished()
                && getController().canRender()) {
            renderSelectable();
        } else if(isWhiteTurn == isWhite() && !getController().isMoveReady() && getController().canRender()) {
            renderVisualize();
        }
    }

    protected void renderSelectable() {
        available.forEach((availableTile) -> getController().addSelectable(availableTile));
    }

    protected void renderVisualize() {
        available.forEach((availableTile) -> getController().addVisualize(availableTile));
    }

    public abstract ArrayList<int[]> calcCommonPieceLocations(int[] location);

    public abstract void pieceAvailableMoves();

    public abstract void pieceAvailableMoves(ArrayList<Tile> whiteListed);

    public void calcAvailableMoves() {
        available.clear();
        ArrayList<Tile> attackingKing = getController().getAttackingKing();
        ArrayList<Tile> pinnedWhiteList = new ArrayList<>();
        ArrayList<Tile> attackWhiteListed = getController().getAttackWhiteListed();
        Piece king = getController().isWhiteTurn()
                ? getController().getWhiteKing() : getController().getBlackKing();
        boolean isPinned = isPinned(pinnedWhiteList, king);
        if (!isKing() && isPinned && attackingKing.size() < 1) {
            pieceAvailableMoves(pinnedWhiteList);
        } else if (!isKing() && attackingKing.size() == 1 && !isPinned) {
            pieceAvailableMoves(attackWhiteListed);
        } else if (attackingKing.isEmpty() || isKing()) {
            pieceAvailableMoves();
        }
    }

    public boolean isPinned(ArrayList<Tile> whiteList, Piece king) {
        if(isKing()) {
            return false;
        }
        Tile[][] tiles = getController().getTiles();
        int row = king.getTile().getRow(); //starts at king
        int col = king.getTile().getCol();
        if(!canNormalize(getTile().getRow() - king.getTile().getRow(), getTile().getCol() - king.getTile().getCol())) {
            return false;
        }
        int r = normalize(getTile().getRow() - king.getTile().getRow()); //normalized trajectory
        int c = normalize(getTile().getCol() - king.getTile().getCol());
        boolean atOppositePiece = false; //has the algorithm reached an opposite pinning piece
        boolean isPinned = false;
        boolean reachedThisPiece = false; //has the algorithm reached this piece
        int pieceCounter = 0;
        while (!atOppositePiece) {
            if(withinBounds(row+r, col+c)) {
                Tile t = tiles[row+r][col+c];
                whiteList.add(t);
                if (t.hasPiece()) {
                    pieceCounter++;
                    if(t.getPiece() == this) {
                        reachedThisPiece = true;
                    }
                    if(t.getPiece().isWhite() != isWhite()) {
                        atOppositePiece = true; //algorithm can terminate, a potential pinning piece has been found
                        if(reachedThisPiece && (t.getPiece().isBishop() || t.getPiece().isQueen()) //check if piece can pin
                                && (r != 0) && (c != 0)) {
                            if(pieceCounter == 2) {
                                isPinned = true;
                            }
                        }
                        if(reachedThisPiece && (t.getPiece().isRook() || t.getPiece().isQueen())
                                && (r == 0 || c == 0)) {
                            if(pieceCounter == 2) {
                                isPinned = true;
                            }
                        }
                    }
                }
            } else {
                atOppositePiece = true;
            }
            if (r > 0) { //travel in the direction of normalized trajectory
                r++;
            } else if (r < 0) {
                r--;
            }
            if (c > 0) {
                c++;
            } else if (c < 0) {
                c--;
            }
        }
        return isPinned;
    }

    public boolean canNormalize(int row, int col) {
        return Math.abs(row) == Math.abs(col) || row == 0 || col == 0;
    }

    public int normalize(int num) {
        return num == 0 ? 0 : num/Math.abs(num);
    }

    protected boolean whiteListed(ArrayList<Tile> whitelist, Tile tile) {
        return whitelist.contains(tile);
    }

    protected boolean hasLegalMoves() {
        return !available.isEmpty();
    }

}