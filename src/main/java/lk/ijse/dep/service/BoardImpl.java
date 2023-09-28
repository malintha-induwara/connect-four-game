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
    public boolean isLegalMove(int col) {
        int index=findNextAvailableSpot(col);
        return index != -1;
    }

    @Override
    public boolean existLegalMoves() {
        for (int i = 0; i < pieces.length; i++) {
            if (isLegalMove(i)){
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

        int count=0;

        //Vertically


        for (int i = 0; i < pieces.length; i++){
            for (int j = 0; j < pieces[i].length-1; j++){
                if (pieces[i][j]==pieces[i][j+1]){
                    count++;
                    if (count==3 && pieces[i][j]!=Piece.EMPTY){
                        //System.out.println("It has 4 Dots ");
                        //System.out.println("Column is"+i+"\n"+"Start Raw is "+(j-2)+"\nEnd raw "+(j+1));
                        //System.out.println("And the Number is"+ ar[i][j]);
                        return new Winner(pieces[i][j],i,(j-2),i,(j+1));

                    }
                }
                else{
                    count=0;
                }
            }
            count=0;
        }

        count=0;


        //Horizontally

        for (int i = 0; i < pieces[0].length; i++){
            for (int j = 0; j < pieces.length-1; j++){
                if (pieces[j][i]==pieces[j+1][i]){
                    count++;
                    if (count==3 && pieces[j][i]!=Piece.EMPTY){
                        //System.out.println("It has 4 Dots ");
                        //System.out.println("Raw is "+i+"\n"+"Start Col is "+(j-2)+"\nEnd Col is "+(j+1));
                        //System.out.println("And the Number is"+ ar[j][i]);
                        return  new Winner(pieces[j][i],(j-2),i,(j+1),i);

                    }
                }
                else{
                    count=0;
                }
            }
            count=0;
        }

        return new Winner(Piece.EMPTY);

    }

}
