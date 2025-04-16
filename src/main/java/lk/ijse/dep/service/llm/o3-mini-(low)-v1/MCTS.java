package lk.ijse.dep.service;

import java.util.*;

public class MCTS {
    private static final double EXPLORATION_CONSTANT = Math.sqrt(2);
    private int iterations;
    private final Piece aiPiece = Piece.GREEN;
    private final Piece humanPiece = Piece.BLUE;
    private final MCTSNode root;

    /**
     * Constructs an MCTS instance based on the current board.
     * We assume that when the AI is called its turn to play.
     */
    public MCTS(Board board, int iterations) {
        this.iterations = iterations;
        // Create a copy of the current board state.
        MCTSState state = new MCTSState(board.getPieces(), aiPiece);
        root = new MCTSNode(state, null, -1, null);
    }

    /**
     * Runs the MCTS iterations and returns the best column move.
     */
    public int findTheMove() {
        for (int i = 0; i < iterations; i++) {
            MCTSNode node = treePolicy(root);
            double reward = defaultPolicy(node.state);
            backup(node, reward);
        }
        // Pick the move with the highest win rate from the root.
        MCTSNode best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (MCTSNode child : root.children) {
            double score = child.visits == 0 ? 0 : child.wins / child.visits;
            if (score > bestScore) {
                bestScore = score;
                best = child;
            }
        }
        if (best == null) {
            // fallback in case none were expanded: choose a random legal move
            List<Integer> legalMoves = root.state.getLegalMoves();
            return legalMoves.get(new Random().nextInt(legalMoves.size()));
        }
        return best.move;
    }

    // SELECTION & EXPANSION
    private MCTSNode treePolicy(MCTSNode node) {
        while (!node.state.isTerminal()) {
            if (!node.isFullyExpanded()) {
                return node.expand();
            } else {
                node = node.bestChild(EXPLORATION_CONSTANT);
            }
        }
        return node;
    }

    // SIMULATION (ROLLOUT)
    private double defaultPolicy(MCTSState state) {
        MCTSState simulationState = state.copy();
        Random rnd = new Random();
        while (!simulationState.isTerminal()) {
            List<Integer> legalMoves = simulationState.getLegalMoves();
            int move = legalMoves.get(rnd.nextInt(legalMoves.size()));
            simulationState.doMove(move);
        }
        Piece winner = simulationState.getWinner();
        // Return reward from the AI perspective.
        if (winner == aiPiece) return 1.0;
        else if (winner == Piece.EMPTY) return 0.5;
        else return 0.0;
    }

    // BACK-PROPAGATION
    private void backup(MCTSNode node, double reward) {
        while (node != null) {
            node.visits++;
            // For non-root nodes, we know which player made the move to reach that state.
            if (node.player != null) {
                if (node.player == aiPiece) {
                    node.wins += reward;
                } else {
                    node.wins += (1 - reward);
                }
            } else {
                // For the root, treat as AI's perspective.
                node.wins += reward;
            }
            node = node.parent;
        }
    }

    // --------------------------
    // Inner classes for MCTS
    // --------------------------

    /**
     * MCTSNode represents a node in the search tree.
     */
    private class MCTSNode {
        MCTSState state;
        MCTSNode parent;
        List<MCTSNode> children;
        int visits;
        double wins;
        int move; // the column that was played to get to this state
        // player who made the move to reach this state.
        // For the root, this is null.
        Piece player;

        public MCTSNode(MCTSState state, MCTSNode parent, int move, Piece player) {
            this.state = state;
            this.parent = parent;
            this.move = move;
            this.player = player;
            this.children = new ArrayList<>();
            this.visits = 0;
            this.wins = 0;
        }

        public boolean isFullyExpanded() {
            return children.size() == state.getLegalMoves().size();
        }

        public MCTSNode expand() {
            List<Integer> possibleMoves = state.getLegalMoves();
            Set<Integer> triedMoves = new HashSet<>();
            for (MCTSNode child : children) {
                triedMoves.add(child.move);
            }
            List<Integer> untriedMoves = new ArrayList<>();
            for (int m : possibleMoves) {
                if (!triedMoves.contains(m)) {
                    untriedMoves.add(m);
                }
            }
            int selectedMove = untriedMoves.get(new Random().nextInt(untriedMoves.size()));
            MCTSState nextState = state.copy();
            nextState.doMove(selectedMove);
            // The move that was just played was done by the opposite of nextState.turn.
            Piece childPlayer = nextState.turn == aiPiece ? humanPiece : aiPiece;
            MCTSNode childNode = new MCTSNode(nextState, this, selectedMove, childPlayer);
            children.add(childNode);
            return childNode;
        }

        public MCTSNode bestChild(double c) {
            MCTSNode best = null;
            double bestValue = Double.NEGATIVE_INFINITY;
            for (MCTSNode child : children) {
                double exploitation = child.wins / (child.visits + 1e-6);
                double exploration = c * Math.sqrt(Math.log(this.visits + 1) / (child.visits + 1e-6));
                double uctValue = exploitation + exploration;
                if (uctValue > bestValue) {
                    bestValue = uctValue;
                    best = child;
                }
            }
            return best;
        }
    }

    /**
     * MCTSState represents the game state for simulations.
     * It makes a deep copy of the board and keeps track of whose turn it is.
     *
     * Note: our game board is represented as a 2D array where:
     * - board.length is the number of columns (6)
     * - board[0].length is the number of rows (5)
     */
    private class MCTSState {
        Piece[][] board;
        Piece turn; // next player to move

        public MCTSState(Piece[][] board, Piece turn) {
            int cols = board.length;
            int rows = board[0].length;
            this.board = new Piece[cols][rows];
            for (int col = 0; col < cols; col++) {
                for (int row = 0; row < rows; row++) {
                    this.board[col][row] = board[col][row];
                }
            }
            this.turn = turn;
        }

        /**
         * Returns a list of legal moves (column indices where a piece can be dropped).
         */
        public List<Integer> getLegalMoves() {
            List<Integer> moves = new ArrayList<>();
            int cols = board.length;
            for (int col = 0; col < cols; col++) {
                if (getNextAvailableSpot(col) != -1) {
                    moves.add(col);
                }
            }
            return moves;
        }

        /**
         * Returns the next available row in the specified column,
         * or -1 if the column is full.
         */
        public int getNextAvailableSpot(int col) {
            int rows = board[col].length;
            for (int row = 0; row < rows; row++) {
                if (board[col][row] == Piece.EMPTY)
                    return row;
            }
            return -1;
        }

        /**
         * Plays the move (drop a piece in the specified column) and switches the turn.
         */
        public void doMove(int col) {
            int row = getNextAvailableSpot(col);
            if (row == -1) throw new IllegalStateException("Illegal move");
            board[col][row] = turn;
            turn = (turn == aiPiece ? humanPiece : aiPiece);
        }

        /**
         * Checks whether the game state is terminal (win or tie).
         */
        public boolean isTerminal() {
            return (getWinner() != Piece.EMPTY) || getLegalMoves().isEmpty();
        }

        /**
         * Checks for a winner in the board.
         * Only horizontal and vertical wins are checked.
         * Returns the winning Piece if found; otherwise, Piece.EMPTY.
         */
        public Piece getWinner() {
            int cols = board.length;
            int rows = board[0].length;
            // vertical check
            for (int col = 0; col < cols; col++) {
                int count = 0;
                Piece last = Piece.EMPTY;
                for (int row = 0; row < rows; row++) {
                    if (board[col][row] == last && board[col][row] != Piece.EMPTY) {
                        count++;
                        if (count >= 3) { // 3 consecutive increments means 4 in a row
                            return board[col][row];
                        }
                    } else {
                        count = 0;
                        last = board[col][row];
                    }
                }
            }
            // horizontal check
            for (int row = 0; row < rows; row++) {
                int count = 0;
                Piece last = Piece.EMPTY;
                for (int col = 0; col < cols; col++) {
                    if (board[col][row] == last && board[col][row] != Piece.EMPTY) {
                        count++;
                        if (count >= 3) {
                            return board[col][row];
                        }
                    } else {
                        count = 0;
                        last = board[col][row];
                    }
                }
            }
            return Piece.EMPTY;
        }

        public MCTSState copy() {
            return new MCTSState(board, turn);
        }
    }

}

