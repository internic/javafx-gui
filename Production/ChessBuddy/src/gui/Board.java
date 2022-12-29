
package gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;

import static gui.Game.HEIGHT;
import static gui.Game.WIDTH;

public class Board {
    
    public static final String[] NUMBER_TO_LETTER_TABLE = {"a","b","c","d","e","f","g","h"};
    public static final PieceType[] NOTATION_TO_CONSTRUCTOR_TABLE = {PieceType.NoPiece, //convert ByteBoard to objects
        PieceType.WhitePawn, PieceType.WhiteBishop, PieceType.WhiteKnight,
        PieceType.WhiteRook, PieceType.WhiteQueen, PieceType.WhiteKing, 
        PieceType.BlackPawn, PieceType.BlackBishop, PieceType.BlackKnight,
        PieceType.BlackRook, PieceType.BlackQueen, PieceType.BlackKing};
    public static final int UPPER_BOUND = 8;
    public static final int LOWER_BOUND = -1;
    
    private Pane boardGUI;
    private final Tile[][] tiles = new Tile[HEIGHT][WIDTH]; //board tiles
    private final ArrayList<Piece> blackNotKing = new ArrayList<>(); //pieces
    private final ArrayList<Piece> whiteNotKing = new ArrayList<>();
    private Piece blackKing; //kings
    private Piece whiteKing;
    private final ArrayList<Tile> attackingKing = new ArrayList<>(); //tiles attacking King
    private final ArrayList<Tile> attackWhiteListed = new ArrayList<>(); //available tiles during attack
    private final ArrayList<Tile> kingCanMove = new ArrayList<>(); //available tiles for King

    public static int normalize(int num) {
        return num == 0 ? 0 : num/Math.abs(num);
    }

    protected static boolean withinBounds(int row, int col) {
        return (row < UPPER_BOUND && row > LOWER_BOUND) && (col < UPPER_BOUND && col > LOWER_BOUND);
    }

    public static String getCharacterNotation(int num) {
        return NUMBER_TO_LETTER_TABLE[num];
    }

    public Pane getBoardGUI() {
        return boardGUI;
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public ArrayList<Piece> getBlackNotKing() {
        return blackNotKing;
    }

    public ArrayList<Piece> getWhiteNotKing() {
        return whiteNotKing;
    }

    public Piece getBlackKing() {
        return blackKing;
    }

    public Piece getWhiteKing() {
        return whiteKing;
    }

    public ArrayList<Tile> getAttackingKing() {
        return attackingKing;
    }

    public ArrayList<Tile> getAttackWhiteListed() {
        return attackWhiteListed;
    }

    public ArrayList<Tile> getKingCanMove() {
        return kingCanMove;
    }
    
    public Piece getKing(boolean white) {
        return white ? whiteKing : blackKing;
    }

    public boolean hasLegalMoves(boolean white) {
        ArrayList<Piece> pieces = white ? whiteNotKing : blackNotKing;
        boolean legal = false;
        for(Piece piece : pieces) {
            if(piece.hasLegalMoves()) {
                legal = true;
            }
        }
        Piece king = white ? whiteKing : blackKing;
        if(king.hasLegalMoves()) {
            legal = true;
        }
        return legal;
    }

    public void calculateMoves(boolean white) {
        Piece king = white ? whiteKing : blackKing;
        attackingKing(king,attackingKing);
        kingCanMove(king,attackingKing,kingCanMove);
        king.calcAvailableMoves();
        attackWhiteListed(attackingKing,king,attackWhiteListed);
        if(white) {
            whiteNotKing.forEach(Piece::calcAvailableMoves);
        } else {
            blackNotKing.forEach(Piece::calcAvailableMoves);
        }
    }

    public boolean inCheck(Piece king, int row, int col) {
        
        int[][] diagonals = {{1,1},{-1,1},{1,-1},{-1,-1}};
        int[][] horizontals = {{1,0},{-1,0},{0,-1},{0,1}};
        int[][] knightOffsets = {{1,2},{2,1},{-1,2},{-2,1},{1,-2},{2,-1},{-1,-2},{-2,-1}};
        
        //diagonals: queen, bishop, pawn
        for (int[] diagonal : diagonals) {
            int i = 1;
            boolean canContinue = true;
            while (canContinue) {
                if (withinBounds(row+(i*diagonal[0]),col+(i*diagonal[1]))) {
                    Tile tile = tiles[row+(i*diagonal[0])][col+(i*diagonal[1])];
                    if (tile.hasPiece() && tile.getPiece() != king
                            && (tile.getPiece().isWhite() != king.isWhite())) {
                        Piece piece = tile.getPiece();
                        if (piece.isBishop() || piece.isQueen()
                                || (piece.isPawn() && king.isWhite() != piece.isWhite() && i == 1)) {
                            return true;
                        }
                        canContinue = false;
                    }
                    if ((tile.hasPiece() && tile.getPiece() != king)) {
                        canContinue = false;
                    }
                } else {
                    canContinue = false;
                }
                i++;
            }
        }

        //horizontals: rook and queen
        for (int[] horizontal : horizontals) {
            int i = 1;
            boolean canContinue = true;
            while (canContinue) {
                if (withinBounds(row+(i*horizontal[0]),col+(i*horizontal[1]))) {
                    Tile tile = tiles[row+(i*horizontal[0])][col+(i*horizontal[1])];
                    if (tile.hasPiece() && tile.getPiece() != king
                            && (tile.getPiece().isWhite() != king.isWhite())) {
                        Piece piece = tile.getPiece();
                        if (piece.isRook() || piece.isQueen()) {
                            return true;
                        }
                        canContinue = false;
                    }
                    if ((tile.hasPiece() && tile.getPiece() != king)) {
                        canContinue = false;
                    }
                } else {
                    canContinue = false;
                }
                i++;
            }
        }
        
        //L-shaped: knight
        for(int[] offset : knightOffsets) {
            if(withinBounds(row+offset[0],col+offset[1])) {
                Tile tile = tiles[row+offset[0]][col+offset[1]];
                if(tile.hasPiece()) {
                    Piece piece = tile.getPiece();
                    if(piece.isKnight() && tile.getPiece() != king &&  
                            tile.getPiece().isWhite() != king.isWhite()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void attackingKing(Piece king, ArrayList<Tile> attackingMoves) {
        attackingMoves.clear();
        int row = king.getTile().getRow();
        int col = king.getTile().getCol();
        int[][] diagonals = {{1,1},{-1,1},{1,-1},{-1,-1}};
        int[][] horizontals = {{1,0},{-1,0},{0,-1},{0,1}};
        int[][] knightOffsets = {{1,2},{2,1},{-1,2},{-2,1},{1,-2},{2,-1},{-1,-2},{-2,-1}};
        
        //diagonals: queen, bishop, pawn
        for (int[] diagonal : diagonals) {
            int i = 1;
            boolean canContinue = true;
            while (canContinue) {
                if (withinBounds(row+(i*diagonal[0]),col+(i*diagonal[1]))) {
                    Tile tile = tiles[row+(i*diagonal[0])][col+(i*diagonal[1])];
                    if (tile.hasPiece() && (tile.getPiece().isWhite() != king.isWhite())) {
                        Piece piece = tile.getPiece();
                        if (piece.isBishop() || piece.isQueen()
                                || (piece.isPawn() && king.isWhite() != piece.isWhite() && i == 1)) {
                            attackingMoves.add(tile);
                        }
                        canContinue = false;
                    }
                    if ((tile.hasPiece() && tile.getPiece() != king)) {
                        canContinue = false;
                    }
                } else {
                    canContinue = false;
                }
                i++;
            }
        }

        //horizontals: rook and queen
        for (int[] horizontal : horizontals) {
            int i = 1;
            boolean canContinue = true;
            while (canContinue) {
                if (withinBounds(row+(i*horizontal[0]),col+(i*horizontal[1]))) {
                    Tile tile = tiles[row+(i*horizontal[0])][col+(i*horizontal[1])];
                    if (tile.hasPiece() && (tile.getPiece().isWhite() != king.isWhite())) {
                        Piece piece = tile.getPiece();
                        if (piece.isRook() || piece.isQueen()) {
                            attackingMoves.add(tile);
                        }
                        canContinue = false;
                    }
                    if ((tile.hasPiece() && tile.getPiece() != king)) {
                        canContinue = false;
                    }
                } else {
                    canContinue = false;
                }
                i++;
            }
        }
        
        //L-shaped: knight
        for(int[] offset : knightOffsets) {
            if(withinBounds(row+offset[0],col+offset[1])) {
                Tile tile = tiles[row+offset[0]][col+offset[1]];
                if(tile.hasPiece()) {
                    Piece piece = tile.getPiece();
                    if(piece.isKnight() && tile.getPiece().isWhite() != king.isWhite()) {
                        attackingMoves.add(tile);
                    }
                }
            }
        }
    }

    public final void kingCanMove(Piece king, ArrayList<Tile> attackingKing, ArrayList<Tile> canMove) {
        canMove.clear();
        int row = king.getTile().getRow();
        int col = king.getTile().getCol();
        
        int[][] diagonalOffsets = {{1,1},{1,-1},{-1,1},{-1,-1}};
        int[][] dkc = {{2,2},{1,2},{2,1},{0,2},{2,0}}; //diagonal king check
        int[][] horizontalOffsets = {{1,0},{0,1},{-1,0},{0,-1}};
        int[][][] hkc = {{{2,1},{2,0},{2,-1}},{{1,2},{0,2},{-1,2}},
            {{-2,1},{-2,0},{-2,-1}},{{1,-2},{0,-2},{-1,-2}}}; //horizontal king check
        
        //diagonals
        for(int[] offset : diagonalOffsets) {
            if(withinBounds(row+offset[0],col+offset[1])) {
                Tile tileToMove = tiles[row+offset[0]][col+offset[1]];
                if((!tileToMove.hasPiece() 
                || (tileToMove.getPiece().isWhite() != king.isWhite()))
                && !inCheck(king, row+offset[0], col+offset[1])
                && (!withinBounds(row+(dkc[0][0]*offset[0]),col+(dkc[0][1]*offset[1]))
                        || !tiles[row+(dkc[0][0]*offset[0])][col+(dkc[0][1]*offset[1])].hasKing())
                && (!withinBounds(row+(dkc[1][0]*offset[0]),col+(dkc[1][1]*offset[1])) 
                        || !tiles[row+(dkc[1][0]*offset[0])][col+(dkc[1][1]*offset[1])].hasKing())
                && (!withinBounds(row+(dkc[2][0]*offset[0]),col+(dkc[2][1]*offset[1])) 
                        || !tiles[row+(dkc[2][0]*offset[0])][col+(dkc[2][1]*offset[1])].hasKing())
                && (!withinBounds(row+(dkc[3][0]*offset[0]),col+(dkc[3][1]*offset[1])) 
                        || !tiles[row+(dkc[3][0]*offset[0])][col+(dkc[3][1]*offset[1])].hasKing())
                && (!withinBounds(row+(dkc[4][0]*offset[0]),col+(dkc[4][1]*offset[1])) 
                        || !tiles[row+(dkc[4][0]*offset[0])][col+(dkc[4][1]*offset[1])].hasKing())) {
                    canMove.add(tiles[row+offset[0]][col+offset[1]]);
                }
            }
        }
        
        //horizontals
        int i= 0;
        for(int[] offset : horizontalOffsets) {
            if(withinBounds(row+offset[0],col+offset[1])) {
                Tile tileToMove = tiles[row+offset[0]][col+offset[1]];
                if((!tileToMove.hasPiece() 
                || (tileToMove.getPiece().isWhite() != king.isWhite()))
                && !inCheck(king, row+offset[0], col+offset[1])
                && (!withinBounds(row+(hkc[i][0][0]),col+(hkc[i][0][1]))
                        || !tiles[row+(hkc[i][0][0])][col+(hkc[i][0][1])].hasKing())
                && (!withinBounds(row+(hkc[i][1][0]),col+(hkc[i][1][1])) 
                        || !tiles[row+(hkc[i][1][0])][col+(hkc[i][1][1])].hasKing())
                && (!withinBounds(row+(hkc[i][2][0]),col+(hkc[i][2][1])) 
                        || !tiles[row+(hkc[i][2][0])][col+(hkc[i][2][1])].hasKing())) {
                    canMove.add(tiles[row+offset[0]][col+offset[1]]);
                }
            }
            i++;
        }
    }

    public void attackWhiteListed(ArrayList<Tile> attackingKing, Piece king, ArrayList<Tile> whiteListed) {
        whiteListed.clear();
        if(attackingKing.size() != 1) {
            return;
        }
        Tile attack = attackingKing.get(0);
        if(attack.hasPiece() && attack.getPiece().isKnight()) {
            whiteListed.add(attack);
            return;
        }
        int row = king.getTile().getRow(); //start at king
        int col = king.getTile().getCol();
        int r = normalize(attack.getRow() - king.getTile().getRow());
        int c = normalize(attack.getCol() - king.getTile().getCol());
        boolean atAttacker = false; //loop until at attacker
        while (!atAttacker) {
            if(withinBounds(row+r,col+c)) {
                Tile t = tiles[row+r][col+c];
                if (t.getPiece() == attack.getPiece()) {
                    atAttacker = true;
                }
                whiteListed.add(t); //add all tiles to whiteList
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
            } else {
                atAttacker = true;
            }
        }
    }

    public final void initWhiteBoard(byte[][] board, Game game) {
        blackNotKing.clear();
        whiteNotKing.clear();
        Pane boardUI = new Pane();
        boolean isLight = false;
        //i is row, j is column
        for(int i = 0; i < HEIGHT; i++) {
            for(int j = 0; j < WIDTH; j++) {
                Tile tile = new Tile(isLight, i, j, true, game);
                tiles[i][j] = tile;
                boardUI.getChildren().add(tile);
                isLight = !isLight;
                Piece piece = NOTATION_TO_CONSTRUCTOR_TABLE[board[i][j]].createPiece(tiles[i][j],game.getApp().getPath());
                if(piece != null) {
                    if(piece.isWhite()) {
                        if(piece.isKing()) {
                            whiteKing = piece;
                        } else {
                            whiteNotKing.add(piece);
                        }
                    } else {
                        if(piece.isKing()) {
                            blackKing = piece;
                        } else {
                            blackNotKing.add(piece);
                        }
                    }
                    tiles[i][j].setPiece(piece);
                    boardUI.getChildren().add(piece);
                    piece.toFront();
                }
                if(i == 0) {
                    Label notationLabel = new Label(getCharacterNotation(j));
                    notationLabel.setId("tinyfont");
                    StackPane.setAlignment(notationLabel, Pos.BOTTOM_LEFT);
                    tile.addLabel(notationLabel);
                } 
                if(j == (HEIGHT - 1)) {
                    Label notationLabel = new Label(Integer.toString(i+1));
                    notationLabel.setId("tinyfont");
                    StackPane.setAlignment(notationLabel, Pos.TOP_RIGHT);
                    tile.addLabel(notationLabel);
                }
            }
            isLight = !isLight;
        }
        boardGUI = boardUI;
        getBoardGUI().setId("board");
    }

    public final void initBlackBoard(byte[][] board, Game game) {
        blackNotKing.clear();
        whiteNotKing.clear();
        Pane boardUI = new Pane();
        boolean IsLight = false;
        //i is row, j is column
        for(int i = 0; i < HEIGHT; i++) {
            for(int j = 0; j < WIDTH; j++) {
                Tile tile = new Tile(IsLight, i, j, false, game);
                tiles[i][j] = tile;
                boardUI.getChildren().add(tile);
                IsLight = !IsLight;
                Piece piece = NOTATION_TO_CONSTRUCTOR_TABLE[board[i][j]].createPiece(tiles[i][j],game.getApp().getPath());
                if(piece != null) {
                    if(piece.isWhite()) {
                        if(piece.isKing()) {
                            whiteKing = piece;
                        } else {
                            whiteNotKing.add(piece);
                        }
                    } else {
                        if(piece.isKing()) {
                            blackKing = piece;
                        } else {
                            blackNotKing.add(piece);
                        }
                    }
                    tiles[i][j].setPiece(piece);
                    boardUI.getChildren().add(piece);
                    piece.toFront();
                }
                if(i == WIDTH-1) {
                    Label notationLabel = new Label(getCharacterNotation(j));
                    StackPane.setAlignment(notationLabel, Pos.BOTTOM_LEFT);
                    tile.addLabel(notationLabel);
                } 
                if(j == 0) {
                    Label notationLabel = new Label(Integer.toString(i+1));
                    StackPane.setAlignment(notationLabel, Pos.TOP_RIGHT);
                    tile.addLabel(notationLabel);
                }
            }
            IsLight = !IsLight;
        }
        boardGUI = boardUI;
        getBoardGUI().setId("board");
    }
    
}
