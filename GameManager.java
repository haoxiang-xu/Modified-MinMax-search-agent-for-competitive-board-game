
import java.util.ArrayList;
import java.util.Scanner;

public class GameManager{
    public static Scanner input;
    public static final boolean DISPLAY_DETAIL =    true;
    public static final boolean DISPLAY_NONE =      false;
    public static void main(String []args) throws InterruptedException {
        runMINMAXGROUPVSRANDOM(36);
    
        close();
    }

    public static int Run(int chessMatTYpe, int whitePlayerType, int blackPlayerType, boolean displayDetail) throws InterruptedException{
        long startTime=System.nanoTime();
        ChessMat chessMat;
        int currentTurn = C.WHITE_MOVE; 
        int[] actionSequence = null;
        
        //Inital Game Board
        if(chessMatTYpe == C.SIMPLE_AMAZONS_CHESS_MAT){
            chessMat = new ChessMat(6,6);
            chessMat.initialSimpleAmazonChessMat();
        }else{
            chessMat = new ChessMat(10,10);
            chessMat.initialAmazonChessMat();
        }

        while(!chessMat.isTerminal(currentTurn)){
            if(displayDetail){
                System.out.println(chessMat);
            }
            if(currentTurn==C.WHITE_MOVE){
                if(whitePlayerType==C.HUMAN_PLAYER){
                    actionSequence=askHumanPlayerMove(chessMat,currentTurn,displayDetail);
                }else{
                    actionSequence=askAgentPlayerMove(chessMat,currentTurn,whitePlayerType,displayDetail);
                }
            }else{
                if(blackPlayerType==C.HUMAN_PLAYER){
                    actionSequence=askHumanPlayerMove(chessMat,currentTurn,displayDetail);
                }else{
                    actionSequence=askAgentPlayerMove(chessMat,currentTurn,blackPlayerType,displayDetail);
                }
            }   

                //ApplyActions
            applyAction(chessMat, actionSequence,currentTurn,displayDetail);

                //Move to next turn
            if(currentTurn == C.WHITE_MOVE){
                currentTurn = C.BLACK_MOVE;
            }else{
                currentTurn = C.WHITE_MOVE;
            }
        }
        //END
        return terminalResult(chessMat,currentTurn,whitePlayerType,blackPlayerType,startTime);
    }

    private static int[] askHumanPlayerMove(ChessMat chessMat, int currentTurn, boolean displayDetail){
        long startTime=System.nanoTime();
        int[] actionList = new int[6];
        int from_x = -1;
        int from_y = -1;
        int to_x = -1;
        int to_y = -1;
        int bloc_x = -1;
        int bloc_y = -1;
        boolean valid = false;
        if(currentTurn==C.WHITE_MOVE || currentTurn==C.WHITE_BLOC){
            System.out.println("[WHITE_MOVE_TURN]");
        }else{
            System.out.println("[BLACK_MOVE_TURN]");
        }
        //Chess on selected
        while(!valid){
            System.out.println("[FROM INDEX X]");
            from_x = askInput(0, 9);
            System.out.println("[FROM INDEX Y]");
            from_y = askInput(0, 9);
            valid = checkChessOnPosition(chessMat,from_x,from_y,currentTurn);
        }
        actionList[0]=from_x;
        actionList[1]=from_y;
        valid = false;
        //Chess to move
        while(!valid){
            System.out.println("[TO INDEX   X]");
            to_x = askInput(0, 9);
            System.out.println("[TO INDEX   Y]");
            to_y = askInput(0, 9);
            valid = checkMoveActionValid(chessMat,from_x,from_y,to_x,to_y,currentTurn);
        }
        actionList[2]=to_x;
        actionList[3]=to_y;  
        valid = false;     
        //Chess to bloc
        while(!valid){
            System.out.println("[TO BLOCK   X]");
            bloc_x = askInput(0, 9);
            System.out.println("[TO BLOCK   Y]");
            bloc_y = askInput(0, 9);
            valid = checkBlocActionValid(chessMat,from_x,from_y,to_x,to_y,bloc_x,bloc_y,currentTurn);
        }    
        actionList[4]=bloc_x;
        actionList[5]=bloc_y;
        long stopTime=System.nanoTime();
        if(displayDetail==DISPLAY_DETAIL){
            if(stopTime-startTime>1000000000){
                System.out.print("[ "+(stopTime-startTime)/1000000000+"s ]");
            }else{
                System.out.print("[ "+(stopTime-startTime)/1000000+"ms ]");
            }
        }
        return actionList;
    }
    private static int[] askAgentPlayerMove(ChessMat chessMat, int currentTurn,int agentPlaterType, boolean displayDetail) throws InterruptedException{
        long startTime=System.nanoTime();
        boolean valid = false;
        int[] actionSequence=new int[6];
        while(!valid){
            if(agentPlaterType>=161 && agentPlaterType<=171){
                MinMaxSearchAgent agentPlayer = new MinMaxSearchAgent(chessMat,currentTurn,agentPlaterType);
                actionSequence=agentPlayer.getAction();
            }else if(agentPlaterType==200){
                RandomAgent agentPlayer = new RandomAgent(chessMat,currentTurn,agentPlaterType);
                actionSequence=agentPlayer.getAction();
            }else if(agentPlaterType==172){
                GraphSearchAgent agentPlayer = new GraphSearchAgent(chessMat,currentTurn,agentPlaterType);
                try{actionSequence=agentPlayer.getAction();}catch(InterruptedException e){e.printStackTrace();}
            }else if(agentPlaterType==198){
                MonteCarloTreeSearch agentPlayer = new MonteCarloTreeSearch(chessMat,currentTurn,agentPlaterType);
                try{actionSequence=agentPlayer.getAction();}catch(InterruptedException e){e.printStackTrace();}
            }else if(agentPlaterType==199){
                SearchAgentX agentPlayer = new SearchAgentX(chessMat,currentTurn,agentPlaterType);
                actionSequence=agentPlayer.getAction();
            }else if(agentPlaterType==197){
                SearchAgentY agentPlayer = new SearchAgentY(chessMat,currentTurn,agentPlaterType);
                actionSequence=agentPlayer.getAction();
            }else{
                MinMaxSearchAgent agentPlayer = new MinMaxSearchAgent(chessMat,currentTurn,agentPlaterType);
                actionSequence=agentPlayer.getAction();
            }    

            valid=checkChessOnPosition(chessMat,actionSequence[C.FROM_X],actionSequence[C.FROM_Y],currentTurn);
            if(valid==false){
                System.out.println("[SEARCH AGENT ERROR: EMPTY BLOCK ON SELECTED]");
                break;
            }
            valid=checkMoveActionValid(chessMat,actionSequence[C.FROM_X],actionSequence[C.FROM_Y],actionSequence[C.TO_X],actionSequence[C.TO_Y],currentTurn);
            if(valid==false){
                System.out.println("[SEARCH AGENT ERROR: INVALID POSITION TO MOVE]");
                break;
            }
            valid=checkBlocActionValid(chessMat,actionSequence[C.FROM_X],actionSequence[C.FROM_Y],actionSequence[C.TO_X],actionSequence[C.TO_Y],actionSequence[C.BLOC_X],actionSequence[C.BLOC_Y],currentTurn);
            if(valid==false){
                System.out.println("[SEARCH AGENT ERROR: INVALID POSITION TO BLOC]");
                break;
            }
        }

        long stopTime=System.nanoTime();
        if(displayDetail==DISPLAY_DETAIL){
            if(stopTime-startTime>1000000000){
                System.out.print("[ "+(stopTime-startTime)/1000000000+"s ]");
            }else{
                System.out.print("[ "+(stopTime-startTime)/1000000+"ms ]");
            }
        }
        return actionSequence;
    }

    //Action checkers
    private static boolean checkChessOnPosition(ChessMat chessMat, int from_x,int from_y,int currentTurn){
        int chessType = chessMat.getChess(from_x,from_y);
        if(chessType == C.WHITE_QUEEN && currentTurn == C.WHITE_MOVE){
            return true;
        }else if(chessType == C.BLACK_QUEEN && currentTurn == C.BLACK_MOVE){
            return true;
        }else{
            return false;
        }
    }
    private static boolean checkMoveActionValid(ChessMat chessMat,int from_x,int from_y,int to_x,int to_y,int currentTurn){
        ArrayList<Position> positions = chessMat.getAllMoveablePositionsForChess(new Position(from_x,from_y));
        for(int i=0; i<positions.size(); i++){
            if(positions.get(i).x==to_x && positions.get(i).y==to_y){
                return true;
            }
        }
        return false;
    }
    private static boolean checkBlocActionValid(ChessMat chessMat, int from_x,int from_y, int to_x,int to_y,int bloc_x, int bloc_y, int currentTurn){
        ChessMat new_chessMat = new ChessMat(chessMat);
        new_chessMat.overWrite(new_chessMat.getChess(from_x,from_y),new Position(to_x,to_y));
        new_chessMat.overWrite(C.EMPTY_BLOCK,new Position(from_x,from_y));
        ArrayList<Position> positions = new_chessMat.getAllMoveablePositionsForChess(new Position(to_x,to_y));
        for(int i=0; i<positions.size(); i++){
            if(positions.get(i).x==bloc_x && positions.get(i).y==bloc_y){
                return true;
            }
        }
        return false;
    }

    private static void applyAction(ChessMat chessMat,int[] actionSequence,int currentTurn,boolean diplayDetail){
        //MOVETO
        chessMat.overWrite(chessMat.getChess(actionSequence[0],actionSequence[1]),new Position(actionSequence[2],actionSequence[3]));
        //EMPTYFROM
        chessMat.overWrite(C.EMPTY_BLOCK,new Position(actionSequence[0],actionSequence[1]));
        //BLOC
        chessMat.overWrite(C.AMAZON_BLOCK,new Position(actionSequence[4],actionSequence[5]));
        if(diplayDetail){
            if(currentTurn==C.WHITE_MOVE){
                System.out.println("[WHITE TURN: ACTION MADE FROM [" +actionSequence[0]+","+actionSequence[1]+"] TO ["+actionSequence[2]+","+actionSequence[3]+"] BLOC ["+actionSequence[4]+","+actionSequence[5]+"]");
            }else{
                System.out.println("[BLACK TURN: ACTION MADE FROM [" +actionSequence[0]+","+actionSequence[1]+"] TO ["+actionSequence[2]+","+actionSequence[3]+"] BLOC ["+actionSequence[4]+","+actionSequence[5]+"]");
            }
        }else{
            System.out.print(">");
        }
    }

    private static int askInput(int min, int max){
        input = new Scanner(System.in);
        int userInput = -999;
        while(userInput<min || userInput>max){
            userInput = input.nextInt();
        }
        return userInput;
    }
    private static void close(){
        input.close();
        System.out.println("[SYSTEM CLOSE");
    }
    private static int terminalResult(ChessMat chessMat,int currentTurn,int whitePlayerType,int blackPlayerType,long startTime){
        System.out.println("\n"+chessMat);
        long stopTime=System.nanoTime();
        if(currentTurn == C.WHITE_MOVE){
            System.out.print("[ "+(stopTime-startTime)/1000000000+"s ]");
            System.out.println("["+C.PLAYER_TYPE[blackPlayerType-160]+"_WIN && "+C.PLAYER_TYPE[whitePlayerType-160]+"_LOSE]");
            return -1;
        }else{
            System.out.print("[ "+(stopTime-startTime)/1000000000+"s ]");
            System.out.println("["+C.PLAYER_TYPE[whitePlayerType-160]+"_WIN && "+C.PLAYER_TYPE[blackPlayerType-160]+"_LOSE]");
            return 1;
        }
    }

    //Running tests
    private static void runMINMAXGROUPVSRANDOM(int limit) throws InterruptedException{
        int[] mark=new int[14];
        for(int i=0; i<mark.length; i++){mark[i]=0;}

        Thread t1 = new Thread(){
            @Override
            public void run()
                {for(int i=0; i<limit; i++){
                    try {
                        mark[0]=mark[0]+Run(C.AMAZONS_CHESS_MAT, C.MINMAX_AGENT_PLAYER001, C.RANDOM_AGENT,DISPLAY_NONE);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t2 = new Thread(){
            @Override
            public void run()
                {for(int i=0; i<limit; i++){
                    try {
                        mark[1]=mark[1]+Run(C.AMAZONS_CHESS_MAT, C.MINMAX_AGENT_PLAYER002, C.RANDOM_AGENT,DISPLAY_NONE);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t3 = new Thread(){
            @Override
            public void run()
                {for(int i=0; i<limit; i++){
                    try {
                        mark[2]=mark[2]+Run(C.AMAZONS_CHESS_MAT, C.MINMAX_AGENT_PLAYER003, C.RANDOM_AGENT,DISPLAY_NONE);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t4 = new Thread(){
            @Override
            public void run()
                {for(int i=0; i<limit; i++){
                    try {
                        mark[3]=mark[3]+Run(C.AMAZONS_CHESS_MAT, C.MINMAX_AGENT_PLAYER004, C.RANDOM_AGENT,DISPLAY_NONE);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t5 = new Thread(){
            @Override
            public void run()
                {for(int i=0; i<limit; i++){
                    try {
                        mark[4]=mark[4]+Run(C.AMAZONS_CHESS_MAT, C.MINMAX_AGENT_PLAYER005, C.RANDOM_AGENT,DISPLAY_NONE);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t6 = new Thread(){
            @Override
            public void run()
                {for(int i=0; i<limit; i++){
                    try {
                        mark[5]=mark[5]+Run(C.AMAZONS_CHESS_MAT, C.MINMAX_AGENT_PLAYER006, C.RANDOM_AGENT,DISPLAY_NONE);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t7 = new Thread(){
            @Override
            public void run()
                {for(int i=0; i<limit; i++){
                    try {
                        mark[6]=mark[6]+Run(C.AMAZONS_CHESS_MAT, C.MINMAX_AGENT_PLAYER007, C.RANDOM_AGENT,DISPLAY_NONE);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t8 = new Thread(){
            @Override
            public void run()
                {for(int i=0; i<limit; i++){
                    try {
                        mark[7]=mark[7]+Run(C.AMAZONS_CHESS_MAT, C.MINMAX_AGENT_PLAYER008, C.RANDOM_AGENT,DISPLAY_NONE);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t9 = new Thread(){
            @Override
            public void run()
                {for(int i=0; i<limit; i++){
                    try {
                        mark[8]=mark[8]+Run(C.AMAZONS_CHESS_MAT, C.MINMAX_AGENT_PLAYER009, C.RANDOM_AGENT,DISPLAY_NONE);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t10 = new Thread(){
            @Override
            public void run()
                {for(int i=0; i<limit; i++){
                    try {
                        mark[9]=mark[9]+Run(C.AMAZONS_CHESS_MAT, C.MINMAX_AGENT_PLAYER010, C.RANDOM_AGENT,DISPLAY_NONE);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t11 = new Thread(){
            @Override
            public void run()
                {for(int i=0; i<limit; i++){
                    try {
                        mark[10]=mark[10]+Run(C.AMAZONS_CHESS_MAT, C.MINMAX_AGENT_PLAYER011, C.RANDOM_AGENT,DISPLAY_NONE);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t12 = new Thread(){
            @Override
            public void run()
                {for(int i=0; i<limit; i++){
                    try {
                        mark[11]=mark[11]+Run(C.AMAZONS_CHESS_MAT, C.MINMAX_AGENT_PLAYER013, C.RANDOM_AGENT,DISPLAY_NONE);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t13 = new Thread(){
            @Override
            public void run()
                {for(int i=0; i<limit; i++){
                    try {
                        mark[12]=mark[12]+Run(C.AMAZONS_CHESS_MAT, C.MINMAX_AGENT_PLAYER014, C.RANDOM_AGENT,DISPLAY_NONE);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t14 = new Thread(){
            @Override
            public void run()
                {for(int i=0; i<limit; i++){
                    try {
                        mark[13]=mark[13]+Run(C.AMAZONS_CHESS_MAT, C.MINMAX_AGENT_PLAYER015, C.RANDOM_AGENT,DISPLAY_NONE);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();
        t7.start();
        t8.start();
        t9.start();
        t10.start();
        t11.start();
        t12.start();
        t13.start();
        t14.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();
        t5.join();
        t6.join();
        t7.join();
        t8.join();
        t9.join();
        t10.join();
        t11.join();
        t12.join();
        t13.join();
        t14.join();

        for(int i=0; i<11; i++){System.out.println(C.PLAYER_TYPE[i+1] + "->"+ mark[i]);}
        System.out.println(C.PLAYER_TYPE[12+1] + "->"+ mark[11]);
        System.out.println(C.PLAYER_TYPE[13+1] + "->"+ mark[12]);
        System.out.println(C.PLAYER_TYPE[14+1] + "->"+ mark[13]);
    }
}