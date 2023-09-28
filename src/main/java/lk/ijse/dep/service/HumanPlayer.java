package lk.ijse.dep.service;

import lk.ijse.dep.controller.BoardController;

public class HumanPlayer extends Player{
    public HumanPlayer(Board board) {
        super(board);
    }

    @Override
    public void movePiece(int col) {

        if (board.isLegalMove(col)) {
                board.updateMove(col,Piece.BLUE);
                board.getBoardUI().update(col,true);
        }
        Winner winner=board.findWinner();
        if (winner.getWinningPiece()!=Piece.EMPTY){
            board.getBoardUI().notifyWinner(winner);
        }
        else if (!board.existLegalMoves()){
            board.getBoardUI().notifyWinner(new Winner(Piece.EMPTY));
        }

    }
}
