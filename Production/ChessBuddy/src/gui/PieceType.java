package gui;

import gui.pieces.*;

public enum PieceType {

    NoPiece {
        @Override
        public Piece createPiece(Tile tile, String path) {
            return null;
        }
    },
    WhitePawn {
        @Override
        public Piece createPiece(Tile tile, String path) {
            return new Pawn(true, tile, path);
        }
    },
    WhiteBishop {
        @Override
        public Piece createPiece(Tile tile, String path) {
            return new Bishop(true, tile, path);
        }

    },
    WhiteKnight {
        @Override
        public Piece createPiece(Tile tile, String path) {
            return new Knight(true, tile, path);
        }
    },
    WhiteRook {
        @Override
        public Piece createPiece(Tile tile, String path) {
            return new Rook(true, tile, path);
        }
    },
    WhiteQueen {
        @Override
        public Piece createPiece(Tile tile, String path) {
            return new Queen(true, tile, path);
        }
    },
    WhiteKing {
        @Override
        public Piece createPiece(Tile tile, String path) {
            return new King(true, tile, path);
        }
    },
    BlackPawn {
        @Override
        public Piece createPiece(Tile tile, String path) {
            return new Pawn(false, tile, path);
        }
    },
    BlackBishop {
        @Override
        public Piece createPiece(Tile tile, String path) {
            return new Bishop(false, tile, path);
        }
    },
    BlackKnight {
        @Override
        public Piece createPiece(Tile tile, String path) {
            return new Knight(false, tile, path);
        }
    },
    BlackRook {
        @Override
        public Piece createPiece(Tile tile, String path) {
            return new Rook(false, tile, path);
        }
    },
    BlackQueen {
        @Override
        public Piece createPiece(Tile tile, String path) {
            return new Queen(false, tile, path);
        }
    },
    BlackKing {
        @Override
        public Piece createPiece(Tile tile, String path) {
            return new King(false, tile, path);
        }
    };

    public abstract Piece createPiece(Tile tile, String path);

}
