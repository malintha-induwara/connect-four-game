package lk.ijse.dep.service;

import java.util.*;

/**
 * A simple Monte Carlo Tree Search for Connect4 (no diagonals).
 * We treat each legal root move as a child,
 * apply UCT to select among them, and do random playouts.
 */
public class MCTS {

    private static final double EXPLORATION_CONSTANT = Math.sqrt(2);

    private final Piece[][] rootState;
    private final int iterations;
    private final Random random = new Random();

    /** A node represents one possible first‐move (column). */
    private class Node {
        int col;
        int wins = 0;
        int visits = 0;

        Node(int col) {
            this.col = col;
        }

        /** UCT formula */
        double uctValue(int totalVisits) {
            if (visits == 0) {
                return Double.POSITIVE_INFINITY;
            }
            double exploitation = (double) wins / visits;
            double exploration = EXPLORATION_CONSTANT
                    * Math.sqrt(Math.log(totalVisits) / visits);
            return exploitation + exploration;
        }
    }

    /**
     * @param board      the real board, from which we copy the current state
     * @param iterations how many rollouts to perform
     */
    public MCTS(Board board, int iterations) {
        this.iterations = iterations;
        Piece[][] orig = board.getPieces();
        // Board.NUM_OF_COLS x Board.NUM_OF_ROWS
        rootState = new Piece[Board.NUM_OF_COLS][Board.NUM_OF_ROWS];
        for (int c = 0; c < Board.NUM_OF_COLS; c++) {
            for (int r = 0; r < Board.NUM_OF_ROWS; r++) {
                rootState[c][r] = orig[c][r];
            }
        }
    }

    /**
     * Run MCTS and return the best column to play.
     */
    public int findTheMove() {
        // 1) Create a node for each legal root move
        List<Node> children = new ArrayList<>();
        for (int col = 0; col < Board.NUM_OF_COLS; col++) {
            if (isLegalMove(rootState, col)) {
                children.add(new Node(col));
            }
        }
        if (children.isEmpty()) {
            return -1; // no moves
        }

        // 2) Do one rollout per child to initialize
        int totalSims = 0;
        for (Node child : children) {
            int result = simulate(child.col);
            child.visits++;
            child.wins += result;
            totalSims++;
        }

        // 3) UCT loop
        for (int i = 0; i < iterations - children.size(); i++) {
            // selection
            Node selected = null;
            double bestUCT = Double.NEGATIVE_INFINITY;
            for (Node child : children) {
                double uct = child.uctValue(totalSims);
                if (uct > bestUCT) {
                    bestUCT = uct;
                    selected = child;
                }
            }
            // simulation
            int result = simulate(selected.col);
            selected.visits++;
            selected.wins += result;
            totalSims++;
        }

        // 4) pick the child with highest win rate
        Node best = Collections.max(children,
                Comparator.comparingDouble(n -> (double) n.wins / n.visits));
        return best.col;
    }

    /**
     * Perform one playout (rollout) starting with the given first move by AI.
     * @return 1 if AI (GREEN) wins, 0 otherwise
     */
    private int simulate(int firstCol) {
        // clone the root state
        Piece[][] state = copyState(rootState);
        // apply AI’s first move
        applyMove(state, firstCol, Piece.GREEN);
        // next to move is human (BLUE)
        Piece current = Piece.BLUE;

        // playout until terminal
        Winner w = findWinner(state);
        while (w.getWinningPiece() == Piece.EMPTY && existLegalMoves(state)) {
            List<Integer> legal = legalMoves(state);
            int mv = legal.get(random.nextInt(legal.size()));
            applyMove(state, mv, current);
            w = findWinner(state);
            // switch player
            current = (current == Piece.BLUE) ? Piece.GREEN : Piece.BLUE;
        }
        // reward = 1 only if AI wins
        return w.getWinningPiece() == Piece.GREEN ? 1 : 0;
    }

    // —— helper methods on our local Piece[][] state ——

    private Piece[][] copyState(Piece[][] s) {
        Piece[][] c = new Piece[Board.NUM_OF_COLS][Board.NUM_OF_ROWS];
        for (int i = 0; i < Board.NUM_OF_COLS; i++) {
            System.arraycopy(s[i], 0, c[i], 0, Board.NUM_OF_ROWS);
        }
        return c;
    }

    private boolean isLegalMove(Piece[][] s, int col) {
        return findNextAvailableSpot(s, col) != -1;
    }

    private boolean existLegalMoves(Piece[][] s) {
        for (int c = 0; c < Board.NUM_OF_COLS; c++) {
            if (isLegalMove(s, c)) return true;
        }
        return false;
    }

    private List<Integer> legalMoves(Piece[][] s) {
        List<Integer> m = new ArrayList<>();
        for (int c = 0; c < Board.NUM_OF_COLS; c++) {
            if (isLegalMove(s, c)) {
                m.add(c);
            }
        }
        return m;
    }

    private int findNextAvailableSpot(Piece[][] s, int col) {
        for (int r = 0; r < Board.NUM_OF_ROWS; r++) {
            if (s[col][r] == Piece.EMPTY) {
                return r;
            }
        }
        return -1;
    }

    private void applyMove(Piece[][] s, int col, Piece p) {
        int r = findNextAvailableSpot(s, col);
        if (r != -1) {
            s[col][r] = p;
        }
    }

    /**
     * Winner detection exactly as in BoardImpl (no diagonal check).
     */
    private Winner findWinner(Piece[][] s) {
        int count;
        // vertical
        for (int c = 0; c < Board.NUM_OF_COLS; c++) {
            count = 0;
            for (int r = 0; r < Board.NUM_OF_ROWS - 1; r++) {
                if (s[c][r] == s[c][r + 1] && s[c][r] != Piece.EMPTY) {
                    count++;
                    if (count == 3) {
                        // r+1 is top of 4 in a column
                        return new Winner(s[c][r], c, r - 2, c, r + 1);
                    }
                } else {
                    count = 0;
                }
            }
        }
        // horizontal
        for (int r = 0; r < Board.NUM_OF_ROWS; r++) {
            count = 0;
            for (int c = 0; c < Board.NUM_OF_COLS - 1; c++) {
                if (s[c][r] == s[c + 1][r] && s[c][r] != Piece.EMPTY) {
                    count++;
                    if (count == 3) {
                        return new Winner(s[c][r], c - 2, r, c + 1, r);
                    }
                } else {
                    count = 0;
                }
            }
        }
        return new Winner(Piece.EMPTY);
    }
}
