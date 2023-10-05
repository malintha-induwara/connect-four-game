package lk.ijse.dep.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MCTS {

    private final Board board;


    private final int computations;

    public MCTS(Board board, int computations) {
        this.board = board;
        this.computations = computations;
    }

    public int findTheMove(){
        System.out.println("Start");

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

        int move=bestNode.move;

        return move;

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
            Board nextMove=getRandomNextBoard(node);
            Node child=new Node(nextMove,node.piece);
            child.parent=node;
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

    private Board getRandomNextBoard(Node node) {
        List<Board> legalMoves=getLegalMoves(node);
        Random random=new Random();
        //Maybe If method can be here
        int randomIndex=random.nextInt(legalMoves.size());
        return legalMoves.get(randomIndex);
    }

    private Node expandNode(Node selectedNode) {

        boolean gameStatus=isTheGameOngoing(selectedNode.board); //True //Flase
        if (!gameStatus){
            return selectedNode;
        }
       else {
            List<Board> nextLegalMoves=getLegalMoves(selectedNode);
            List<Integer> nextLegalMovesIndex=getLegalMovesIndex(selectedNode);
            for (int i = 0; i < nextLegalMoves.size(); i++) {
                Board move=nextLegalMoves.get(i);
                Node childNode=new Node(move,(selectedNode.piece==Piece.BLUE)?Piece.GREEN:Piece.BLUE);
                childNode.move=nextLegalMovesIndex.get(i);
                childNode.parent=selectedNode;
                childNode.move=i;
                selectedNode.addChild(childNode);
            }
            Random random=new Random();
            int randomIndex=random.nextInt(nextLegalMoves.size());
            return selectedNode.children.get(randomIndex);
        }
    }

    private List<Integer> getLegalMovesIndex(Node selectedNode) {
        Node node=selectedNode;
        List<Integer> nextMovesIndex=new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            if (node.board.isLegalMove(i)){
                nextMovesIndex.add(i);
            }
        }

      for (int i = 0; i < nextMovesIndex.size(); i++) {
            System.out.println("index: "+ nextMovesIndex.get(i));
        }

        return  nextMovesIndex;
    }

    public List<Board> getLegalMoves(Node selectedNode) {

        Node node=selectedNode;
        List<Board> nextMoves = new ArrayList<>();

        //Find the next Player
        Piece nextPiece= (selectedNode.piece==Piece.BLUE)?Piece.GREEN:Piece.BLUE;

        for (int i = 0; i < 6; i++) {
            if (node.board.isLegalMove(i)){
                int raw=node.board.findNextAvailableSpot(i);
                Board copyBoard=copyBoardState(node.board);
                copyBoard.updateMove(i,raw,nextPiece);
                nextMoves.add(copyBoard);
                System.out.println("Moves: "+i);
                //System.out.println(Arrays.deepToString(copyBoard.getPieces()));
            }
        }



        return nextMoves;
    }

    public Node selectNode(Node tree) {
        Node currentNode=tree;
        while (currentNode.children.size()!=0){
            currentNode=UCT.findBestNodeWithUCT(currentNode);
        }
        return currentNode;
    }



    public boolean isTheGameOngoing(Board board){
        Winner winner=board.findWinner();
        if (winner.getWinningPiece()!=Piece.EMPTY){
            return false;
        } else if (!board.existLegalMoves()) {
            return false;
        }
        return true;
    }




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
