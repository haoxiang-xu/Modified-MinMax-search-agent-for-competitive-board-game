import java.util.ArrayList;
import java.util.Random;

public class GraphSearchAgent {

    private ChessMat chessMat;
    private int startTurn;
    private State root;
    private int[][] actions;

    private ArrayList<int[]> DEPTH1_1OVER4;
    private ArrayList<int[]> DEPTH2_1OVER4;
    private ArrayList<int[]> DEPTH1_CLOSEU;
    private ArrayList<int[]> ALPHA_BETA_05;
    private ArrayList<int[]> ALPHA_BETA_06;
    private ArrayList<int[]> ALPHA_BETA_07;
    private ArrayList<int[]> ALPHA_BETA_08;
    private ArrayList<int[]> TERRITORY_BAS;
    private ArrayList<int[]> RANDOM_ACTION;

    public GraphSearchAgent(ChessMat chessMat,int startTurn,int unknow){
        this.chessMat=new ChessMat(chessMat);
        this.startTurn=startTurn;
        this.root=new State(chessMat,-1,null,null);
        this.actions=new int[12][6];
        this.DEPTH1_1OVER4=new ArrayList<int[]>();
        this.DEPTH2_1OVER4=new ArrayList<int[]>();
        this.DEPTH1_CLOSEU=new ArrayList<int[]>();
        this.ALPHA_BETA_05=new ArrayList<int[]>();
        this.ALPHA_BETA_06=new ArrayList<int[]>();
        this.ALPHA_BETA_07=new ArrayList<int[]>();
        this.ALPHA_BETA_08=new ArrayList<int[]>();
        this.TERRITORY_BAS=new ArrayList<int[]>();
        this.RANDOM_ACTION=new ArrayList<int[]>();
    }

    public int[] getAction() throws InterruptedException{
        //int OPP_rootTerminal1_4=OPPTerminalCheck1_4(root);
        //int SEL_rootTerminal1_4=SELTerminalCheck1_4(root);
        //ArrayList<State> ThreateningStates=new ArrayList<State>();
        ArrayList<State> SEL_PossibleStates_depth1=root.getAllPossibleStates(startTurn,true);

        if(SEL_PossibleStates_depth1.isEmpty()){
            System.out.print("P");
            int[] action=getUnderClosedAction();
            if(action!=null){
                return action;
            }
        }

        //THREADS DECLARE
        Thread depth1_chess_terminal;
        Thread depth1_chess_closeup;
        Thread depth2_chess_terminal;
        Thread alpha_beta_search005_backup;
        Thread alpha_beta_search006_backup;
        Thread alpha_beta_search007_backup;
        Thread alpha_beta_search008_backup;
        Thread territory_base_packup;
        Thread random_action_backup;
 
        depth1_chess_terminal = new Thread(){
            @Override
            public void run(){
                depth1_chess_terminal(SEL_PossibleStates_depth1);
                System.out.print(">");
            }        
        };
        depth1_chess_closeup = new Thread(){
            @Override
            public void run(){
                depth1_chess_closeup(SEL_PossibleStates_depth1);
                System.out.print(">");
            }        
        };
        depth2_chess_terminal = new Thread(){
            @Override
            public void run(){
                depth2_chess_terminal(SEL_PossibleStates_depth1);
                System.out.print(">");
            }        
        };
        alpha_beta_search005_backup = new Thread(){
            @Override
            public void run(){
                MinMaxSearchAgent agentPlayer = new MinMaxSearchAgent(chessMat,startTurn,C.MINMAX_AGENT_PLAYER005);
                ALPHA_BETA_05.add(agentPlayer.getAction());
                System.out.print(">");
            }        
        };
        alpha_beta_search006_backup = new Thread(){
            @Override
            public void run(){
                MinMaxSearchAgent agentPlayer = new MinMaxSearchAgent(chessMat,startTurn,C.MINMAX_AGENT_PLAYER006);
                ALPHA_BETA_06.add(agentPlayer.getAction());
                System.out.print(">");
            }        
        };
        alpha_beta_search007_backup = new Thread(){
            @Override
            public void run(){
                MinMaxSearchAgent agentPlayer = new MinMaxSearchAgent(chessMat,startTurn,C.MINMAX_AGENT_PLAYER007);
                ALPHA_BETA_07.add(agentPlayer.getAction());
                System.out.print(">");
            }        
        };
        alpha_beta_search008_backup = new Thread(){
            @Override
            public void run(){
                MinMaxSearchAgent agentPlayer = new MinMaxSearchAgent(chessMat,startTurn,C.MINMAX_AGENT_PLAYER008);
                ALPHA_BETA_08.add(agentPlayer.getAction());
                System.out.print(">");
            }        
        };
        territory_base_packup = new Thread(){
            @Override
            public void run(){
                getTerritoryBasedAction(SEL_PossibleStates_depth1);
                System.out.print(">");
            }   
        };
        random_action_backup  = new Thread(){
            @Override
            public void run(){
                RANDOM_ACTION.add(getRandomAction());
                System.out.print(">");
            }              
        };

        //THREADS START
        try{
        depth1_chess_terminal.start();
        depth1_chess_closeup.start();
        depth2_chess_terminal.start();
        alpha_beta_search005_backup.start();
        alpha_beta_search006_backup.start();
        alpha_beta_search007_backup.start();
        alpha_beta_search008_backup.start();
        territory_base_packup.start();
        random_action_backup.start();
        }catch(RuntimeException e){
            e.printStackTrace();
        }

        //JOIN THREADS
        try{
            depth1_chess_terminal.join();
            depth1_chess_closeup.join();
            depth2_chess_terminal.join();
            alpha_beta_search005_backup.join();
            alpha_beta_search006_backup.join();
            alpha_beta_search007_backup.join();
            alpha_beta_search008_backup.join();
            territory_base_packup.join();
            random_action_backup.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        return global_parliament();
    }

    private void depth1_chess_terminal(ArrayList<State> SEL_PossibleStates_depth1){
        int[] action=new int[6];
        int OPP_rootTerminal1_4=OPPTerminalCheck1_4(root);

        for(State sel_state_depth1:SEL_PossibleStates_depth1){
            //[RETURN TERMINAL OPP ACTION]OPP 1/4 TERMINAL
            if(OPPTerminalCheck1_4(sel_state_depth1)>OPP_rootTerminal1_4){
                action=sel_state_depth1.action;
                DEPTH1_1OVER4.add(action);
            }
        }
    }
    private void depth1_chess_closeup(ArrayList<State> SEL_PossibleStates_depth1){
        ArrayList<Position> OPPchesses;
        ArrayList<Position> SELchesses;
        int OPP_rootCloseUp=0;
        int OPP_depth1CloseUp;
        if(startTurn==C.WHITE_MOVE){
            OPPchesses = this.chessMat.getAllTypeChessPositions(C.BLACK_QUEEN);
        }else{
            OPPchesses = this.chessMat.getAllTypeChessPositions(C.WHITE_QUEEN);
        }

        for(Position chess:OPPchesses){if(chessMat.isClosed(chess)){OPP_rootCloseUp++;}}

        for(State sel_state_depth1:SEL_PossibleStates_depth1){
            //[RETURN TERMINAL OPP ACTION]OPP 1/4 TERMINAL
            OPP_depth1CloseUp=0;
            for(Position chess:OPPchesses){if(sel_state_depth1.chessMat.isClosed(chess)){OPP_depth1CloseUp++;}}
            if(OPP_depth1CloseUp>OPP_rootCloseUp){
                if(startTurn==C.WHITE_MOVE){
                    SELchesses = sel_state_depth1.chessMat.getAllTypeChessPositions(C.WHITE_QUEEN);
                }else{
                    SELchesses = sel_state_depth1.chessMat.getAllTypeChessPositions(C.BLACK_QUEEN);
                }
                double SLETerritory=0;
                double OPPTerritory=0;
                for(Position c:SELchesses){SLETerritory=SLETerritory+sel_state_depth1.chessMat.getTerritoryScore(c);}
                for(Position c:OPPchesses){OPPTerritory=OPPTerritory+sel_state_depth1.chessMat.getTerritoryScore(c);}
                if(SLETerritory>=OPPTerritory){DEPTH1_CLOSEU.add(sel_state_depth1.action);}
            }
        }
    }
    private void depth2_chess_terminal(ArrayList<State> SEL_PossibleStates_depth1){
        int counter=6;
        ArrayList<Position> OPPchesses;
        ArrayList<Position> SELchesses;
        int OPP_rootTerminal1_4=OPPTerminalCheck1_4(root);
        for(State sel_state_depth1:SEL_PossibleStates_depth1){
            ArrayList<State> SEL_PossibleStates_depth2 = sel_state_depth1.getAllPossibleStates(startTurn);
            for(State sel_state_depth2:SEL_PossibleStates_depth2){
                //OPP 1/8 TERMINAL
                if(counter<=0){break;}
                if(OPPTerminalCheck1_4(sel_state_depth2)>OPP_rootTerminal1_4 && !(OPPTerminalCheck1_4(sel_state_depth2)<OPP_rootTerminal1_4)){
                    if(startTurn==C.WHITE_MOVE){
                        OPPchesses = sel_state_depth2.chessMat.getAllTypeChessPositions(C.BLACK_QUEEN);
                        SELchesses = sel_state_depth2.chessMat.getAllTypeChessPositions(C.WHITE_QUEEN);
                    }else{
                        OPPchesses = sel_state_depth2.chessMat.getAllTypeChessPositions(C.WHITE_QUEEN);
                        SELchesses = sel_state_depth2.chessMat.getAllTypeChessPositions(C.BLACK_QUEEN);
                    }
                    double SLETerritory=0;
                    double OPPTerritory=0;
                    for(Position c:SELchesses){SLETerritory=SLETerritory+sel_state_depth2.chessMat.getTerritoryScore(c);}
                    for(Position c:OPPchesses){OPPTerritory=OPPTerritory+sel_state_depth2.chessMat.getTerritoryScore(c);}
                    if(SLETerritory>=OPPTerritory){DEPTH2_1OVER4.add(sel_state_depth2.action);counter--;}
                }
            }
        }
    }
    private void getTerritoryBasedAction(ArrayList<State> SEL_PossibleStates_depth1){
        double maxT=0;
        State maxState=null;
        ArrayList<Position> selchesses=new ArrayList<Position>();
        ArrayList<Position> oppchesses=new ArrayList<Position>();
        for(State s:SEL_PossibleStates_depth1){
            if(startTurn==C.WHITE_MOVE || startTurn==C.WHITE_BLOC){
                selchesses = s.chessMat.getAllTypeChessPositions(C.WHITE_QUEEN);
                oppchesses = s.chessMat.getAllTypeChessPositions(C.BLACK_QUEEN);
            }else{
                selchesses = s.chessMat.getAllTypeChessPositions(C.BLACK_QUEEN);
                oppchesses = s.chessMat.getAllTypeChessPositions(C.WHITE_QUEEN);
            }
            double T=0;
            for(Position c:selchesses){T=T+chessMat.getTerritoryScore(c);}
            for(Position c:oppchesses){T=T-chessMat.getTerritoryScore(c);}
            if(T>maxT){
                maxT=T;
                maxState=s;
            }
        }
        if(maxState!=null){TERRITORY_BAS.add(maxState.action);}
    }
    public int[] getRandomAction(){
        Random rand = new Random();
        State root = new State(chessMat,-1,null,null);
        ArrayList<State> PossibleStates = root.getAllPossibleStates(startTurn);
        int random_index=rand.nextInt(PossibleStates.size());
        return PossibleStates.get(random_index).action;
    }
    public int[] getUnderClosedAction() throws InterruptedException{
        ArrayList<Position> SELChess=new ArrayList<Position>();
        if(startTurn==C.WHITE_MOVE){
            SELChess = this.chessMat.getAllTypeChessPositions(C.WHITE_QUEEN);
        }else{
            SELChess = this.chessMat.getAllTypeChessPositions(C.BLACK_QUEEN);
        }
        int[] action=new int[6];
        for(Position c:SELChess){
            action=chessMat.getClosedAction(c);
            if(action[2]!=-1 && action[3]!=-1){
                return action;
            }
        }
        return null;
    }

    private int[] global_parliament() throws InterruptedException{
        ArrayList<int[]> actionBundle = new ArrayList<int[]>();
        //THIS WILL HAVE REALLY MANY ACTIONS
        actionBundle.addAll(DEPTH1_1OVER4);
        actionBundle.addAll(DEPTH1_CLOSEU);
        //THIS WILL HAVE INFINIT ACTIONS
        actionBundle.addAll(DEPTH2_1OVER4);
        actionBundle.addAll(ALPHA_BETA_05);
        actionBundle.addAll(ALPHA_BETA_06);
        actionBundle.addAll(ALPHA_BETA_07);
        actionBundle.addAll(ALPHA_BETA_08);
        actionBundle.addAll(TERRITORY_BAS);
        MonteCarloTreeSearch agentPlayer = new MonteCarloTreeSearch(chessMat,startTurn,-1);
        return agentPlayer.getAction(actionBundle);
    }
    private int[] minmax_parliament(){
        int maxAgreed=1;
        int[] maxAgreedAction=ALPHA_BETA_08.get(0);

        ArrayList<int[]> MINMAXACTIONS = new ArrayList<int[]>();
        MINMAXACTIONS.add(ALPHA_BETA_08.get(0));
        MINMAXACTIONS.add(ALPHA_BETA_06.get(0));
        MINMAXACTIONS.add(ALPHA_BETA_05.get(0));
        MINMAXACTIONS.add(ALPHA_BETA_07.get(0));

        for(int i=0; i<=MINMAXACTIONS.size(); i++){
            int actionAgreed=0;
            for(int j=i; j<MINMAXACTIONS.size(); j++){
                if(isEqual(actions[i], actions[j])){actionAgreed++;}
            }
            if(actionAgreed>maxAgreed){
                maxAgreed=actionAgreed;
                maxAgreedAction=actions[i];
            }
        }
        if(!isThreatingAction(maxAgreedAction)){
            return maxAgreedAction;
        }else{
            return new int[6];
        }

    }
    
    public int OPPTerminalCheck1_4(State state){
        ArrayList<Position> chesses;
        int chessTerminal=0;
        if(startTurn==C.WHITE_MOVE){chesses=state.chessMat.getAllTypeChessPositions(C.BLACK_QUEEN);}else{chesses=state.chessMat.getAllTypeChessPositions(C.WHITE_QUEEN);}
        //CHECK NUMBER OF CHESS TERMINAL
        for(Position chess:chesses){
            if(state.chessMat.isChessTerminal(chess)){
                chessTerminal++;
            }
        }
        return chessTerminal;
    }
    public int SELTerminalCheck1_4(State state){
        ArrayList<Position> chesses;
        int chessTerminal=0;
        if(startTurn==C.WHITE_MOVE){chesses=state.chessMat.getAllTypeChessPositions(C.WHITE_QUEEN);}else{chesses=state.chessMat.getAllTypeChessPositions(C.BLACK_QUEEN);}
        //CHECK NUMBER OF CHESS TERMINAL
        for(Position chess:chesses){
            if(state.chessMat.isChessTerminal(chess)){
                chessTerminal++;
            }
        }
        return chessTerminal;
    }
    public boolean isThreatingAction(int[] action){
        if(SELTerminalCheck1_4(root)<SELTerminalCheck1_4(State.applyAction(root, action))){
            return true;
        }
        return false;
    }

    private boolean isEmpty(int[] action){
        for(int i=0; i<action.length; i++){
            if(action[i]!=0){return false;}
        }
        return true;
    }
    private boolean isEqual(int[] arrayA, int[] arrayB){
        if(arrayA.length!=arrayB.length){return false;}
        for(int i=0; i<arrayA.length; i++){
            if(arrayA[i]!=arrayB[i]){return false;}
        }
        return true;
    }
}
