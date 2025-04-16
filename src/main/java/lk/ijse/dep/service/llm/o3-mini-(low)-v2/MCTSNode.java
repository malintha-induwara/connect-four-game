
package lk.ijse.dep.service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MCTSNode {

    public Piece[][] state;
    public MCTSNode parent;
    public List<MCTSNode> children;
    public List<Integer> untriedMoves;
    public int move; // the move (column index) that led to this node; for root this is -1.
    public Piece playerTurn; // the player whose turn it is at this node.
    public Piece playerJustMoved; // the player who made the move to reach this node (null for root).
    public double wins;
    public int visits;

    private final int COLS = Board.NUM_OF_COLS;
    private final int ROWS = Board.NUM_OF_ROWS;

    /**
     * Constructs a new node.
     *
     * @param state       the board state at this node.
     * @param parent      the parent node (null for root).
     * @param move        the move (column) that was applied to reach this node.
     * @param playerTurn  the player whose turn it is at this node.
     */
    public MCTSNode(Piece[][] state, MCTSNode parent, int move, Piece playerTurn) {
        this.state = state;
        this.parent = parent;
        this.move = move;
        this.playerTurn = playerTurn;
        // Set the player who just moved (for root, there is no move yet).
        if (parent == null) {
            this.playerJustMoved = null;
        } else {
            this.playerJustMoved = parent.playerTurn;
        }
        this.wins = 0;
        this.visits = 0;
        this.children = new ArrayList<>();
        this.untriedMoves = getLegalMoves(state);
    }

    /**
     * Returns true if there are no more moves to try from this state.
     */
    public boolean isFullyExpanded() {
        return untriedMoves.isEmpty();
    }

    /**
     * Uses the UCT (Upper Confidence Bound for Trees) formula to select a child node.
     */
    public MCTSNode selectChild(double explorationConstant) {
        MCTSNode selected = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        for (MCTSNode child : children) {
            double uctValue = (child.wins / (child.visits + 1e-6))
                    + explorationConstant * Math.sqrt(Math.log(this.visits + 1) / (child.visits + 1e-6));
            if (uctValue > bestValue) {
                bestValue = uctValue;
                selected = child;
            }
        }
        return selected;
    }

    /**
     * Returns a list of legal moves (columns that are not full) in the given state.
     */
    private List<Integer> getLegalMoves(Piece[][] state) {
        List<Integer> moves = new ArrayList<>();
        for (int col = 0; col < COLS; col++) {
            if (state[col][ROWS - 1] == Piece.EMPTY) {
                moves.add(col);
            }
        }
        return moves;
    }
}


