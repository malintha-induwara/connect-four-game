package lk.ijse.dep.service;

import java.util.ArrayList;
import java.util.List;

public class Node {

    public Board board;

    public int visit;

    public int score;

    List<Node> children = new ArrayList<>();

    Node parent= null;

    public Piece piece;

    public int move;

    public Node(Board board, Piece piece) {
        this.board = board;
        this.piece = piece;
    }

    public Node getChildWithMaxScore() {
        Node result = children.get(0);
        for (int i = 1; i < children.size(); i++) {
            if (children.get(i).score > result.score) {
                result = children.get(i);
            }
        }
        return result;
    }

    public void addChild(Node node) {
        children.add(node);
    }




}
