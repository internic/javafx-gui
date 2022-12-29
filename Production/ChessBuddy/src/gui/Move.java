package gui;

import java.util.ArrayList;

public class Move {

    public static final String[] NUMBER_TO_LETTER_TABLE = {"a","b","c","d","e","f","g","h"}; //used in conversion to notation
    public static final int NO_CASTLE = 0;
    public static final int KING_SIDE_CASTLE = 1;
    public static final int QUEEN_SIDE_CASTLE = 2;

    private final byte[][] board; //byte board to be stored
    private final int[] oldPos = new int[2];
    private final int[] newPos = new int[2];
    private Piece pieceMoved;
    private final int castleStatus;
    private String notation;
    private boolean capture = false;

    private boolean canKingSideCastle;
    private boolean canQueenSideCastle;

    public void setCanKingSideCastle(boolean canKingSideCastle) {
        this.canKingSideCastle = canKingSideCastle;
    }

    public void setCanQueenSideCastle(boolean canQueenSideCastle) {
        this.canQueenSideCastle = canQueenSideCastle;
    }

    public byte[][] getBoard() {
        return board;
    }

    public int[] getOldPos() {
        return oldPos;
    }

    public int[] getNewPos() {
        return newPos;
    }

    public Piece getPieceMoved() {
        return pieceMoved;
    }

    public void setPieceMoved(Piece pieceMoved) {
        this.pieceMoved = pieceMoved;
    }

    public boolean hasPieceMoved() {
        return pieceMoved != null;
    }

    public boolean isCastle() {
        return castleStatus>0;
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public Move(int oldRow, int oldCol, int newRow, int newCol, Piece moved, byte[][] boardIn,
                boolean canKingSideCastleIn, boolean canQueenSideCastleIn, boolean forWhite, boolean taken) {
        oldPos[0] = oldRow;
        oldPos[1] = oldCol;
        newPos[0] = newRow;
        newPos[1] = newCol;
        pieceMoved = moved;
        castleStatus = NO_CASTLE;
        board = boardIn;
        canKingSideCastle = canKingSideCastleIn;
        canQueenSideCastle = canQueenSideCastleIn;
        if(!taken) {
            notation = calcPiecePrefix(pieceMoved,oldPos,newPos) + getCharacterNotation(newPos[1]) + (newPos[0]+1);
        } else {
            notation = calcPiecePrefix(pieceMoved,oldPos,newPos) + "x" + getCharacterNotation(newPos[1]) + (newPos[0]+1);
        }
        if(forWhite) {
            if(moved.getTile().getCol() == Game.LOWER_BOUNDARY && moved.getTile().getRow() == Game.LOWER_BOUNDARY) {
                canQueenSideCastle = false;
            } else if(moved.getTile().getCol() == Game.UPPER_BOUNDARY && moved.getTile().getRow() == Game.LOWER_BOUNDARY) {
                canKingSideCastle = false;
            }
        } else {
            if(moved.getTile().getCol() == Game.LOWER_BOUNDARY && moved.getTile().getRow() == Game.UPPER_BOUNDARY) {
                canQueenSideCastle = false;
            } else if(moved.getTile().getCol() == Game.UPPER_BOUNDARY && moved.getTile().getRow() == Game.UPPER_BOUNDARY) {
                canKingSideCastle = false;
            }
        }
    }

    public Move(int oldRow, int oldCol, int newRow, int newCol, int castleType, byte[][] boardIn) {
        oldPos[0] = oldRow;
        oldPos[1] = oldCol;
        newPos[0] = newRow;
        newPos[1] = newCol;
        castleStatus = castleType;
        board = boardIn;
        canKingSideCastle = false;
        canQueenSideCastle = false;
        notation = isQueenSide(castleStatus) ? "0-0-0" : "0-0";
    }

    @Deprecated
    public void setNotation() {
        if(castleStatus == NO_CASTLE) {
            String notationToAdd;
            if(!capture) {
                notationToAdd = calcPiecePrefix(pieceMoved,oldPos,newPos) + getCharacterNotation(newPos[1]) + (newPos[0]+1);
            } else {
                notationToAdd = calcPiecePrefix(pieceMoved,oldPos,newPos) + "x" + getCharacterNotation(newPos[1]) + (newPos[0]+1);
            }
            notation = notationToAdd;
        } else {
            notation = isQueenSide(castleStatus) ? "0-0-0" : "0-0";
        }
    }

    public final String calcPiecePrefix(Piece piece, int[] old, int[] loc) {
        String str = pieceMoved.getNotation();
        boolean matchingCol = false;
        boolean matchingRow = false;
        ArrayList<int[]> commonPieceLocations = piece.calcCommonPieceLocations(loc);
        if(!commonPieceLocations.isEmpty()) {
            for (int[] location : commonPieceLocations) {
                if (location[0] == piece.getTile().getRow()) {
                    matchingRow = true;
                }
                if (location[1] == piece.getTile().getCol()) {
                    matchingCol = true;
                }
            }
            if (matchingRow) {
                str = str + getCharacterNotation(old[1]);
            }
            if (matchingCol) {
                str = str + (old[0]+1);
            }
            if(!matchingRow && !matchingCol) {
                str = str + getCharacterNotation(old[1]);
            }
        }
        return str;
    }

    public static String getCharacterNotation(int num) {
        return NUMBER_TO_LETTER_TABLE[num];
    }

    public void addRecent(ArrayList<int[]> recentlyMoved) {
        recentlyMoved.add(oldPos);
        recentlyMoved.add(newPos);
    }

    public static boolean isQueenSide(int type) {
        return type == QUEEN_SIDE_CASTLE;
    }

    public void setEnPassantNot() {
        notation = notation + "e.p.";
    }

    public void setCheckNot() {
        notation = notation + "+";
    }

    public void setCheckMateNot() {
        notation = notation + "#";
    }

    public void setPromotionNot(Piece promotionTo) {
        notation = notation + promotionTo.getNotation();
    }

    public void setStaleMateNot() {
        notation = notation + "$";
    }

    public void setCapture(boolean captureIn) {
        capture = captureIn;
    }

    public boolean canKingSideCastle() {
        return canKingSideCastle;
    }

    public boolean canQueenSideCastle() {
        return canQueenSideCastle;
    }

    public boolean canEnPassant(boolean forWhite) {
        if(pieceMoved != null) {
            return pieceMoved.isPawn() && ((forWhite && oldPos[0] == 1 && newPos[0] == 3) ||
                    (!forWhite && oldPos[0] == 6 && newPos[0] == 4));
        } else {
            return false;
        }
    }

}
