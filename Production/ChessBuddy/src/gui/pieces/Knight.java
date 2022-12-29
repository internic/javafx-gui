
package gui.pieces;

import gui.Game;
import gui.GameInfo;
import gui.Piece;
import gui.Tile;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;

public final class Knight extends Piece{

    public String whiteKnight;
    public String blackKnight;
    
    public final void setPaths(String path) {
        whiteKnight = "/resources/" + path + "/whiteknight.png";
        blackKnight = "/resources/" + path + "/blackknight.png";
    }

    public Knight(boolean isWhite, Tile tile, String path) {
        super(isWhite, tile);
        setPaths(path);
        Image image;
        if(isWhite) {
            image = new Image(whiteKnight);
        } else {
            image = new Image(blackKnight);
        }
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(tileSize);
        imageView.setFitWidth(tileSize);
        this.getChildren().add(imageView);
    }
    
    @Override
    public void pieceAvailableMoves() {
        Game controller = getController();
        Tile[][] tiles = controller.getTiles();
        int row = getTile().getRow();
        int col = getTile().getCol();
        ArrayList<Tile> available = getAvailable();
        
        int[][] offsets = {{1,2},{2,1},{-1,2},{-2,1},{1,-2},{2,-1},{-1,-2},{-2,-1}};
        for(int[] offset : offsets) {
            if(withinBounds(row+offset[0],col+offset[1])) {
                Tile tile = tiles[row+offset[0]][col+offset[1]];
                if(!tile.hasPiece() || (tile.getPiece().isWhite() != isWhite())) {
                    available.add(tile);
                }
            }
        }
    }
    
    @Override
    public void pieceAvailableMoves(ArrayList<Tile> whiteList) {
        Game controller = getController();
        Tile[][] tiles = controller.getTiles();
        int row = getTile().getRow();
        int col = getTile().getCol();
        ArrayList<Tile> available = getAvailable();
        int[][] offsets = {{1,2},{2,1},{-1,2},{-2,1},{1,-2},{2,-1},{-1,-2},{-2,-1}};
        for(int[] offset : offsets) {
            if(withinBounds(row+offset[0],col+offset[1])) {
                Tile tile = tiles[row+offset[0]][col+offset[1]];
                if(whiteListed(whiteList, tile) && 
                        (!tile.hasPiece() || (tile.getPiece().isWhite() != isWhite()))) {
                    available.add(tile);
                }
            }
        }
    }
    
    @Override
    public ArrayList<int[]> calcCommonPieceLocations(int[] location) {
        Game controller = getController();
        Tile[][] tiles = controller.getTiles();
        int row = location[0];
        int col = location[1];
        ArrayList<int[]> locations = new ArrayList<>();
        int[][] offsets = {{1,2},{2,1},{-1,2},{-2,1},{1,-2},{2,-1},{-1,-2},{-2,-1}};
        for(int[] offset : offsets) {
            if(withinBounds(row+offset[0],col+offset[1])) {
                Tile tile = tiles[row+offset[0]][col+offset[1]];
                if((tile.hasPiece() && (tile.getPiece().isWhite() == isWhite()) 
                        && tile.getPiece().isKnight()) && tile.getPiece() != this) {
                    int[] loc = {row+offset[0],col+offset[1]};
                    locations.add(loc);
                }
            }
        }
        return locations;
    }
    
    @Override
    public boolean isKnight() {
        return true;
    }
    
    @Override
    public String getNotation() {
        return "N";
    }
    
    @Override
    public int getValue() {
        return 3;
    }
    
    @Override
    public byte getInfoCode() {
        return isWhite() ? GameInfo.WHITE_KNIGHT : GameInfo.BLACK_KNIGHT;
    }
}
