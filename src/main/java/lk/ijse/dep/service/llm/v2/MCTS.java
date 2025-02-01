package lk.ijse.dep.service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MCTS {

    private Piece[][] rootState;
    private int iterations;
    private final int COLS = Board.NUM_OF_COLS;
    private final int ROWS = Board.NUM_OF_ROWS;
    private final double EXPLORATION_CONSTANT = Math.sqrt(2);

    /**
     * Constructs an MCTS instance using a copy of the current board state.
     * The AI always plays with Piece.GREEN.
     */
    public MCTS(Board board, int iterations) {
        this.iterations = iterations;
        // Copy the current board state from the game board.
        this.rootState = copyState(board.getPieces());
    }

    /**
     * Runs the MCTS iterations and returns the chosen column (move).
     */
    public int findTheMove() {
        // At the root node it is AI’s turn (GREEN)
        MCTSNode root = new MCTSNode(rootState, null, -1, Piece.GREEN);

        for (int i = 0; i < iterations; i++) {
            // SELECTION: start at root and select child nodes using UCT.
            MCTSNode node = root;
            while (node.isFullyExpanded() && !isTerminal(node.state)) {
                node = node.selectChild(EXPLORATION_CONSTANT);
            }
            // EXPANSION: if the node is nonterminal and has untried moves, expand one.
            if (!isTerminal(node.state) && !node.untriedMoves.isEmpty()) {
                int move = node.untriedMoves.remove(ThreadLocalRandom.current().nextInt(node.untriedMoves.size()));
                Piece[][] newState = copyState(node.state);
                // Apply the move in the simulation.
                applyMove(newState, move, node.playerTurn);
                Piece nextPlayer = switchPlayer(node.playerTurn);
                MCTSNode child = new MCTSNode(newState, node, move, nextPlayer);
                node.children.add(child);
                node = child;
            }
            // SIMULATION: from the newly expanded node simulate a complete random game.
            double result = simulateRandomPlayout(copyState(node.state), node.playerTurn);
            // BACKPROPAGATION: update win/visit counts along the path.
            MCTSNode current = node;
            while (current != null) {
                current.visits++;
                if (current.playerJustMoved != null) {
                    // If the move that reached this node was made by the AI, add the simulation result;
                    // otherwise add (1 - result).
                    if (current.playerJustMoved == Piece.GREEN) {
                        current.wins += result;
                    } else {
                        current.wins += (1 - result);
                    }
                }
                current = current.parent;
            }
        }
        // Choose the child of the root with the highest visit count.
        MCTSNode best = null;
        for (MCTSNode child : root.children) {
            if (best == null || child.visits > best.visits) {
                best = child;
            }
        }
        return best.move;
    }

    /**
     * Performs a random playout (simulation) until a terminal state is reached.
     * Returns 1.0 if AI wins, 0.0 if AI loses, or 0.5 for a tie.
     */
    private double simulateRandomPlayout(Piece[][] state, Piece currentPlayer) {
        Piece winner = getWinner(state);
        while (winner == Piece.EMPTY && existLegalMoves(state)) {
            List<Integer> moves = getLegalMoves(state);
            if (moves.isEmpty()) break;
            int move = moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
            applyMove(state, move, currentPlayer);
            currentPlayer = switchPlayer(currentPlayer);
            winner = getWinner(state);
        }
        if (winner == Piece.GREEN) return 1.0;
        else if (winner == Piece.BLUE) return 0.0;
        else return 0.5;
    }

    /**
     * Creates a deep copy of the board state.
     */
    private Piece[][] copyState(Piece[][] state) {
        Piece[][] copy = new Piece[state.length][state[0].length];
        for (int i = 0; i < state.length; i++) {
            System.arraycopy(state[i], 0, copy[i], 0, state[i].length);
        }
        return copy;
    }

    /**
     * Applies a move (dropping a piece into the specified column) on the state.
     */
    private void applyMove(Piece[][] state, int col, Piece piece) {
        for (int i = 0; i < ROWS; i++) {
            if (state[col][i] == Piece.EMPTY) {
                state[col][i] = piece;
                break;
            }
        }
    }

    /**
     * Returns true if at least one legal move exists in the state.
     */
    private boolean existLegalMoves(Piece[][] state) {
        for (int col = 0; col < COLS; col++) {
            if (state[col][ROWS - 1] == Piece.EMPTY) return true;
        }
        return false;
    }

    /**
     * Returns a list of legal moves (columns that are not full) in the state.
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

    /**
     * Switches the current player.
     */
    private Piece switchPlayer(Piece current) {
        return current == Piece.GREEN ? Piece.BLUE : Piece.GREEN;
    }

    /**
     * Checks if the state is terminal (win or tie).
     */
    private boolean isTerminal(Piece[][] state) {
        return getWinner(state) != Piece.EMPTY || !existLegalMoves(state);
    }

    /**
     * Checks for a winning condition in the state.
     * This implementation checks for 4 consecutive pieces vertically or horizontally.
     */
    private Piece getWinner(Piece[][] state) {
        // Vertical check.
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS - 3; row++) {
                Piece p = state[col][row];
                if (p != Piece.EMPTY &&
                        p == state[col][row + 1] &&
                        p == state[col][row + 2] &&
                        p == state[col][row + 3])
                    return p;
            }
        }
        // Horizontal check.
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS - 3; col++) {
                Piece p = state[col][row];
                if (p != Piece.EMPTY &&
                        p == state[col + 1][row] &&
                        p == state[col + 2][row] &&
                        p == state[col + 3][row])
                    return p;
            }
        }
        return Piece.EMPTY;
    }
}
