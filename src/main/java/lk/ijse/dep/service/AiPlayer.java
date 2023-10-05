package lk.ijse.dep.service;

public class AiPlayer extends Player{
    public AiPlayer(Board board) {
        super(board);
    }

    @Override
    public void movePiece(int col) {

//        do {
//            int randomNum = (int)(Math.random() * 6);
//            if(board.isLegalMove(randomNum)){
//                col=randomNum;
//                break;
//            }
//        }while (true);

        MCTS mcts=new MCTS(board,1);
        mcts.findTheMove();

        col=1;

        board.updateMove(col,Piece.GREEN);
        board.getBoardUI().update(col,false);
        Winner winner=board.findWinner();
        if (winner.getWinningPiece()!=Piece.EMPTY){
            board.getBoardUI().notifyWinner(winner);
        }
        else if (!board.existLegalMoves()){
            board.getBoardUI().notifyWinner(new Winner(Piece.EMPTY));
        }


    }
}
