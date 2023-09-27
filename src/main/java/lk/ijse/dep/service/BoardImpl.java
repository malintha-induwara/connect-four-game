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
        return boardUI;
    }

    @Override
    public int findNextAvailableSpot(int col) {

        int freeSpaceIndex=0;

        for (int i = 0; i < pieces[col].length; i++) {
            if (pieces[col][i]!=Piece.EMPTY){
                freeSpaceIndex++;
            }
            else{
                return freeSpaceIndex;
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

        boolean isMoveAvailable=false;

        for (int i = 0; i < pieces.length; i++) {
            isMoveAvailable=isLegalMoves(i);
            if (isMoveAvailable){
                return isMoveAvailable;
            }
        }

        return isMoveAvailable;
    }

    @Override
    public void updateMove(int col, Piece move) {

    }

    @Override
    public Winner findWinner() {
        return null;
    }

}
