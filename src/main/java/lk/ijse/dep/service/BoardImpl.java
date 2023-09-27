package lk.ijse.dep.service;

public class BoardImpl implements Board {

    private final Piece [][] pieces;

    private final BoardUI boardUI;

    public BoardImpl(BoardUI boardUI) {
        this.boardUI = boardUI;
        this.pieces=new Piece[6][5];

        //initialize all the pieces
        for (int i = 0; i < pieces.length; i++) {
            for (int j = 0; j < pieces[i].length; j++) {
                pieces[i][j]=Piece.EMPTY;
            }
        }
    }
    public Piece[][] getPieces() {
        return pieces;
    }
    public BoardUI getBoardUI() {
        return this.boardUI;
    }

    @Override
    public int findNextAvailableSpot(int col) {

        for (int i = 0; i < pieces[col].length; i++) {
            if (pieces[col][i]==Piece.EMPTY){
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isLegalMoves(int col) {
        int index=findNextAvailableSpot(col);
        return index != -1;
    }

    @Override
    public boolean existLegalMoves() {
        for (int i = 0; i < pieces.length; i++) {
            if (isLegalMoves(i)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateMove(int col, Piece move) {
        int index=findNextAvailableSpot(col);
        pieces[col][index]=move;
    }

    @Override
    public Winner findWinner() {

        



        return null;
    }

}
