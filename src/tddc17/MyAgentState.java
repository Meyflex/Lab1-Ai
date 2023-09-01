package tddc17;

import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.List;
import java.util.Random;

class MyAgentState
{
    public int[][] world = new int[30][30];
    public int initialized = 0;
    final int UNKNOWN 	= 0;
    final int WALL 		= 1;
    final int CLEAR 	= 2;
    final int DIRT		= 3;
    final int HOME		= 4;
    final int ACTION_NONE 			= 0;
    final int ACTION_MOVE_FORWARD 	= 1;
    final int ACTION_TURN_RIGHT 	= 2;
    final int ACTION_TURN_LEFT 		= 3;
    final int ACTION_SUCK	 		= 4;

    public static class Pos {
        public int x;
        public int y;

        @Override
        public boolean equals(Object other) {
            if (other instanceof Pos) {
                Pos o  = (Pos)other;
                return o.x == x && o.y == y;
            }
            return false;
        }
        public Pos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    public static  class Move {
      public Pos from;
      public Pos to;

      public Move(Pos From , Pos To ){
          from= From;
          to=To;
      }
      public int getDir() {
        // TODO
        return 0;
      }
    }

    public int backtrack() {
        // check last pos in path and return direction to it
        return -1;
    }

    public List<Move> toExplore;
    public List<Pos> explored;
    public List<Pos> pathFromStart;

    public Pos backtrackTo = null;

    public boolean needsToFinishMovement = false;
    public int nextAlignment = -1;

    public void addExplore() {
        Pos top = new Pos(agent_x_position, agent_y_position - 1);
        Pos bottom = new Pos(agent_x_position, agent_y_position + 1);
        Pos left = new Pos(agent_x_position - 1, agent_y_position);
        Pos right = new Pos(agent_x_position + 1, agent_y_position);
        if (!toExplore.contains(top) && !explored.contains(top)) {
            toExplore.add(new Move(new Pos(agent_x_position, agent_y_position), top));
        }
        if (!toExplore.contains(bottom) && !explored.contains(bottom)) {
            toExplore.add(new Move(new Pos(agent_x_position, agent_y_position), bottom));
        }
        if (!toExplore.contains(left) && !explored.contains(left)) {
            toExplore.add(new Move(new Pos(agent_x_position, agent_y_position), left));
        }
        if (!toExplore.contains(right) && !explored.contains(right)) {
            toExplore.add(new Move(new Pos(agent_x_position, agent_y_position), right));
        }
    }
    public int explore () {
        // peek top of stack
        // if empty just backtrack to start.
        Pos from = toExplore.get(toExplore.size() - 1).from;
        if (from.x != agent_x_position || from.y != agent_y_position) {
            backtrackTo= from;
            return -1;
        }
        Move nextMove = toExplore.get(toExplore.size() - 1);
        toExplore.remove(nextMove);
        explored.add(nextMove.to);
        return nextMove.getDir();
    }


    public int agent_x_position = 1;
    public int agent_y_position = 1;
    public int agent_last_action = ACTION_NONE;

    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;
    public int agent_direction = EAST;

    MyAgentState()
    {
        for (int i=0; i < world.length; i++)
            for (int j=0; j < world[i].length ; j++)
                world[i][j] = UNKNOWN;
        world[1][1] = HOME;
        agent_last_action = ACTION_NONE;
    }
    // Based on the last action and the received percept updates the x & y agent position
    public void updatePosition(DynamicPercept p)
    {
        Boolean bump = (Boolean)p.getAttribute("bump");

        if (agent_last_action==ACTION_MOVE_FORWARD && !bump)
        {
            switch (agent_direction) {
                case MyAgentState.NORTH:
                    agent_y_position--;
                    break;
                case MyAgentState.EAST:
                    agent_x_position++;
                    break;
                case MyAgentState.SOUTH:
                    agent_y_position++;
                    break;
                case MyAgentState.WEST:
                    agent_x_position--;
                    break;
            }
        }

    }

    public void updateWorld(int x_position, int y_position, int info)
    {
        world[x_position][y_position] = info;
    }

    public void printWorldDebug()
    {
        for (int i=0; i < world.length; i++)
        {
            for (int j=0; j < world[i].length ; j++)
            {
                if (world[j][i]==UNKNOWN)
                    System.out.print(" ? ");
                if (world[j][i]==WALL)
                    System.out.print(" # ");
                if (world[j][i]==CLEAR)
                    System.out.print(" . ");
                if (world[j][i]==DIRT)
                    System.out.print(" D ");
                if (world[j][i]==HOME)
                    System.out.print(" H ");
            }
            System.out.println("");
        }
    }
}

class MyAgentProgram implements AgentProgram {

    private int initnialRandomActions = 10;
    private Random random_generator = new Random();

    // Here you can define your variables!
    public int iterationCounter = 100;
    public MyAgentState state = new MyAgentState();

    // moves the Agent to a random start position
    // uses percepts to update the Agent position - only the position, other percepts are ignored
    // returns a random action
    private Action moveToRandomStartPosition(DynamicPercept percept) {
        int action = random_generator.nextInt(6);
        initnialRandomActions--;
        state.updatePosition(percept);
        if(action==0) {
            state.agent_direction = ((state.agent_direction-1) % 4);
            if (state.agent_direction<0)
                state.agent_direction +=4;
            state.agent_last_action = state.ACTION_TURN_LEFT;
            return LIUVacuumEnvironment.ACTION_TURN_LEFT;
        } else if (action==1) {
            state.agent_direction = ((state.agent_direction+1) % 4);
            state.agent_last_action = state.ACTION_TURN_RIGHT;
            return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
        }
        state.agent_last_action=state.ACTION_MOVE_FORWARD;
        return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
    }


    @Override
    public Action execute(Percept percept) {

        // DO NOT REMOVE this if condition!!!
        if (initnialRandomActions>0) {
            return moveToRandomStartPosition((DynamicPercept) percept);
        } else if (initnialRandomActions==0) {
            // process percept for the last step of the initial random actions
            initnialRandomActions--;
            state.updatePosition((DynamicPercept) percept);
            System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
            state.agent_last_action=state.ACTION_SUCK;
            return LIUVacuumEnvironment.ACTION_SUCK;
        }

        // This example agent program will update the internal agent state while only moving forward.
        // START HERE - code below should be modified!

        System.out.println("x=" + state.agent_x_position);
        System.out.println("y=" + state.agent_y_position);
        System.out.println("dir=" + state.agent_direction);


        iterationCounter--;

        if (iterationCounter==0)
            return NoOpAction.NO_OP;

        if (state.needsToFinishMovement) {
            if (state.agent_direction != state.nextAlignment) {
                // TODO turn accordingly
                return NoOpAction.NO_OP;
            } else {
                state.needsToFinishMovement = false;
                return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
            }
        }

        DynamicPercept p = (DynamicPercept) percept;
        Boolean bump = (Boolean)p.getAttribute("bump");
        Boolean dirt = (Boolean)p.getAttribute("dirt");
        Boolean home = (Boolean)p.getAttribute("home");
        System.out.println("percept: " + p);

        if (dirt) {
            state.agent_last_action=state.ACTION_SUCK;
            return LIUVacuumEnvironment.ACTION_SUCK;
        }

        if (state.backtrackTo != null) {
            if (state.agent_x_position == state.backtrackTo.x && state.agent_y_position == state.backtrackTo.y) {
                state.backtrackTo = null;
            } else {
                int nextDir = state.backtrack();
                // same as explore
                return NoOpAction.NO_OP;
            }
        }

        int nextDir = state.explore();
        if (nextDir == -1) {
            int nextDir2 = state.backtrack();
            // same as explore
            return NoOpAction.NO_OP;
        }
        state.nextAlignment = nextDir;
        if (state.agent_direction != nextDir) {
            state.needsToFinishMovement = true;
            // TODO turn accordingly
            return NoOpAction.NO_OP;
        }

        return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
    }
}

