package lk.ijse.dep.service;

public class BoardWithIndex {
    private Board board;
    private int index;

    public BoardWithIndex(Board board, int index) {
        this.board = board;
        this.index = index;
    }

    public Board getBoard() {
        return board;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "BoardWithIndex{" +
                "board=" + board +
                ", index=" + index +
                '}';
    }

}
