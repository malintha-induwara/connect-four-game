package lk.ijse.dep.service;

import java.util.*;

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

        MCTS mcts=new MCTS(board,4000);
        col=mcts.findTheMove();

        //System.out.println(col);
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

    //MCTS Algorithm itself and some Utility Classes
    private static class MCTS {

        private final Board board;


        //Iteration Count
        private final int computations;

        public MCTS(Board board, int computations) {
            this.board = board;
            this.computations = computations;
        }

        private int findTheMove(){
            int count=0;

            Node tree= new Node(board,Piece.BLUE);

            while (count<computations){

                //Selection
                Node selectedNode=selectNode(tree);

                //Expand
                Node nodeToExplore= expandNode(selectedNode);

                //Simulation
                Piece result=randomSimulation(nodeToExplore);

                //Backpropagation
                backPropagation(nodeToExplore,result);


                count++;
            }

            Node bestNode=tree.getChildWithMaxScore();

            int move=bestNode.getMove();

            return move;

        }

        //MCTS Required Methods

        private Node selectNode(Node tree) {
            Node currentNode=tree;
            while (currentNode.getChildren().size()!=0){
                currentNode=UCT.findBestNodeWithUCT(currentNode);
            }
            return currentNode;
        }

        private Node expandNode(Node selectedNode) {

            boolean gameStatus=isTheGameOngoing(selectedNode.getBoard()); //True //Flase
            if (!gameStatus){
                return selectedNode;
            }
            else {
                List<BoardWithIndex> nextLegalMoves=getLegalMoves(selectedNode);
                for (int i = 0; i < nextLegalMoves.size(); i++) {
                    Board move=nextLegalMoves.get(i).getBoard();
                    Node childNode=new Node(move,(selectedNode.piece==Piece.BLUE)?Piece.GREEN:Piece.BLUE);
                    childNode.setParent(selectedNode);
                    childNode.move=nextLegalMoves.get(i).getIndex();
                    selectedNode.addChild(childNode);
                }
                Random random=new Random();
                int randomIndex=random.nextInt(nextLegalMoves.size());
                return selectedNode.children.get(randomIndex);
            }
        }

        private Piece randomSimulation(Node nodeToExplore) {
            Board board=copyBoardState(nodeToExplore.board);
            Node node= new Node(board,nodeToExplore.piece);
            node.parent=nodeToExplore.parent;

            //System.out.println(node.parent);
            if (node.board.findWinner().getWinningPiece()==Piece.BLUE){
                node.parent.score=Integer.MIN_VALUE;
                return Piece.BLUE;
            }

            while (isTheGameOngoing(node.board)){
                BoardWithIndex nextMove=getRandomNextBoard(node);
                Node child=new Node(nextMove.getBoard(),node.piece);
                child.parent=node;
                child.move=nextMove.getIndex();
                node.addChild(child);
                node = child;
            }

            if (node.board.findWinner().getWinningPiece()==Piece.GREEN){
                return Piece.GREEN;
            }
            else if (node.board.findWinner().getWinningPiece()==Piece.BLUE){
                return Piece.BLUE;
            }
            else {
                return Piece.EMPTY; //Draw
            }
        }

        private void backPropagation(Node nodeToExplore, Piece result) {

            Node node=nodeToExplore;
            while (node!=null){
                node.visit++;
                if (node.piece==result){
                    node.score++;
                }
                node=node.parent;
            }

        }


        //Utility Methods



        //This Method is to get the next legal moves
        public List<BoardWithIndex> getLegalMoves(Node selectedNode) {

            Node node=selectedNode;
            List<BoardWithIndex> nextMoves = new ArrayList<>();

            //Find the next Player
            Piece nextPiece= (selectedNode.piece==Piece.BLUE)?Piece.GREEN:Piece.BLUE;

            for (int i = 0; i < 6; i++) {
                if (node.board.isLegalMove(i)){
                    int raw=node.board.findNextAvailableSpot(i);
                    Board copyBoard=copyBoardState(node.board);
                    copyBoard.updateMove(i,raw,nextPiece);
                    BoardWithIndex boardWithIndex=new BoardWithIndex(copyBoard,i);
                    nextMoves.add(boardWithIndex);
                    // System.out.println("Moves: "+i);
                    //System.out.println(Arrays.deepToString(copyBoard.getPieces()));
                }
            }

            return nextMoves;
        }

        //This Method is to get a random move from available moves
        private BoardWithIndex getRandomNextBoard(Node node) {
            List<BoardWithIndex> legalMoves=getLegalMoves(node);
            Random random=new Random();
            //Maybe If method can be here
            if (legalMoves.isEmpty()) {
                return null;
            }
            int randomIndex=random.nextInt(legalMoves.size());
            return legalMoves.get(randomIndex);
        }

        //This method is to check the game is finished or not
        public boolean isTheGameOngoing(Board board){
            Winner winner=board.findWinner();
            if (winner.getWinningPiece()!=Piece.EMPTY){
                return false;
            } else if (!board.existLegalMoves()) {
                return false;
            }
            return true;
        }

        //This method is to get a copy of a board object
        private Board copyBoardState(Board originalBoard) {
            // Create a new board and copy the state cell by cell
            Board newBoard = new BoardImpl(originalBoard.getBoardUI());
            for (int col = 0; col < Board.NUM_OF_COLS; col++) {
                for (int row = 0; row < Board.NUM_OF_ROWS; row++) {
                    Piece piece = originalBoard.getPieces()[col][row];
                    newBoard.updateMove(col, row, piece);
                }
            }
            return newBoard;
        }


    }

    //To Store Board Type Objects and their indexes
    private static class BoardWithIndex {
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

    //Node
    private static class Node{
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


        public Board getBoard() {
            return board;
        }

        public void setBoard(Board board) {
            this.board = board;
        }

        public int getVisit() {
            return visit;
        }

        public void setVisit(int visit) {
            this.visit = visit;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public Piece getPiece() {
            return piece;
        }

        public void setPiece(Piece piece) {
            this.piece = piece;
        }

        public int getMove() {
            return move;
        }

        public void setMove(int move) {
            this.move = move;
        }

        public List<Node> getChildren() {
            return children;
        }
    }

    //The UTC Formula to find the best nod
    private static class UCT {

        public static double uctValue(
                int totalVisit, double nodeWinScore, int nodeVisit) {
            if (nodeVisit == 0) {
                return Integer.MAX_VALUE;
            }
            return ((double) nodeWinScore / (double) nodeVisit)
                    + 1.41 * Math.sqrt(Math.log(totalVisit) / (double) nodeVisit);
        }

        public static Node findBestNodeWithUCT(Node node) {
            int parentVisit = node.visit;
            return Collections.max(
                    node.children,
                    Comparator.comparing(c -> uctValue(parentVisit,
                            c.score, c.visit)));
        }
    }



}
