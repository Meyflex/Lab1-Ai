package tddc17;

import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.List;
import java.util.ArrayList;
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
                Pos o = (Pos)other;
                return o.x == x && o.y == y;
            }
            return false;
        }

        public Pos(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Pos top() {
            return new Pos(x, y - 1);
        }

        public Pos bottom() {
            return new Pos(x, y + 1);
        }

        public Pos left() {
            return new Pos(x - 1, y);
        }

        public Pos right() {
            return new Pos(x + 1, y);
        }

        public boolean isNeighbour(Pos other) {
            return Math.abs(x - other.x) + Math.abs(y - other.y) == 1;
        }
    }

    public static  class Move {
      public Pos from;
      public Pos to;

      public Move(Pos from , Pos to){
          this.from = new Pos(from.x, from.y);
          this.to = new Pos(to.x, to.y);
      }
      // get move direction
      public int getDir() {
        if (from.x < to.x) return EAST;
        if (from.x > to.x) return WEST;
        if (from.y < to.y) return SOUTH;
        if (from.y > to.y) return NORTH;
        return -1;
      }
    }

    // returns the backtracking direction
    public int backtrack() {
        if (pathFromStart.size() <= 1) return 0;
        Pos nextPos = pathFromStart.get(pathFromStart.size() - 2);
        lastPos = pos;
        pos = nextPos;
        return (new Move(lastPos, pos)).getDir();
    }

    // init the path and exploration lists with the starting space.
    public void init() {
        if (pathFromStart.isEmpty() || !pathFromStart.get(pathFromStart.size() - 1).equals(pos))
            pathFromStart.add(new MyAgentState.Pos(pos.x, pos.y));
        explored.add(new MyAgentState.Pos(pos.x, pos.y));
        addExplore();
    }

    public List<Move> toExplore = new ArrayList<>();
    public List<Pos> explored = new ArrayList<>();
    public List<Pos> pathFromStart = new ArrayList<>();

    // updates the pathFromStart list
    public void updatePath() {
        // if we didn't move, do nothing.
        if (pathFromStart.size() > 0 && pos.equals(pathFromStart.get(pathFromStart.size() - 1))) return;
        // if we went back, pop from the path.
        if (pathFromStart.size() > 1 && pos.equals(pathFromStart.get(pathFromStart.size() - 2))) {
            pathFromStart.remove(pathFromStart.size() - 1);
            return;
        }
        // otherwise, append to the path.
        pathFromStart.add(new MyAgentState.Pos(pos.x, pos.y));
    }

    // for backtrack state. if non null, we will backtrack
    public Pos backtrackTo = null;

    // for movement state
    public boolean needsToFinishMovement = false;
    public int nextAlignment = -1;

    // adds a move to the toExplore list
    public void toExploreSet(Pos from, Pos to) {
        // we need to check if a move to this space already exists.
        int i = toExplore.size() - 1;
        while (i >= 0) {
            if (toExplore.get(i).to.equals(to)) break;
            i--;
        }
        if (i < 0) {
            // if it doesn't, just add the move
            toExplore.add(new Move(pos, to));
        } else {
            // if it does, replace the existing move. (for less backtracking)
            toExplore.remove(i);
            toExplore.add(new Move(pos, to));
        }
    }

    // add adjacent spaces to the toExplore list
    public void addExplore() {
        if (!explored.contains(pos.top())) {
            toExploreSet(pos, pos.top());
        }
        if (!explored.contains(pos.left())) {
            toExploreSet(pos, pos.left());
        }
        if (!explored.contains(pos.bottom())) {
            toExploreSet(pos, pos.bottom());
        }
        if (!explored.contains(pos.right())) {
            toExploreSet(pos, pos.right());
        }
    }

    // Will look for possible shortcuts in the backtrack path
    // Basically if a space in the path is neighbour with another space
    // further in the path, just remove the spaces in between from the path.
    public void optimizeBacktrack() {
        int ito = pathFromStart.lastIndexOf(backtrackTo);
        int b = 0;
        int B = pathFromStart.size() - 1 - b;
        while (B > ito) {
            int skipN = 0;
            int skipFrom = -1;
            for (int i = B - 2; i >= ito; i--) {
                if (pathFromStart.get(i).isNeighbour(pathFromStart.get(B))) {
                    skipFrom = i + 1;
                    skipN = B - skipFrom;
                }
            }
            while (skipN > 0) {
                pathFromStart.remove(skipFrom);
                skipN--;
            }
            b++;
            B = pathFromStart.size() - 1 - b;
        }
    }

    // returns the next direction to go (or -1 if we're done)
    public int explore () {
        // if we don't have anything to explore, we still have to backtrack to the start.
        if (toExplore.size() == 0) {
            if (pathFromStart.size() == 1) return -1;
            backtrackTo = pathFromStart.get(0);
            optimizeBacktrack();
            return backtrack();
        }
        // peek top of stack
        Move nextMove = toExplore.get(toExplore.size() - 1);
        // if we can't access it, backtrack to the last spot where it could be accessed from
        if (!nextMove.from.equals(pos)) {
            backtrackTo = nextMove.from;
            optimizeBacktrack();
            return backtrack();
        }
        // else pop the move and explore the space
        toExplore.remove(nextMove);
        explored.add(nextMove.to);
        lastPos = pos;
        pos = nextMove.to;
        return nextMove.getDir();
    }

    public Pos pos = new Pos(1, 1);
    public Pos lastPos = new Pos(1, 1);

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
                    pos.y--;
                    break;
                case MyAgentState.EAST:
                    pos.x++;
                    break;
                case MyAgentState.SOUTH:
                    pos.y++;
                    break;
                case MyAgentState.WEST:
                    pos.x--;
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
        }
    }
}

class MyAgentProgram implements AgentProgram {

    private int initnialRandomActions = 10;
    private Random random_generator = new Random();

    // Here you can define your variables!
    public int iterationCounter = 4000;
    public MyAgentState state = new MyAgentState();

    // gets the turn action depending on the current and target direction
    private Action getTurn(int cur, int next) {
        if (cur > next) {
            if (next == 0 && cur == 3) { // wrap around case
                state.agent_direction = 0;
                return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
            } else {
                state.agent_direction--;
                return LIUVacuumEnvironment.ACTION_TURN_LEFT;
            }
        } else if (cur < next) {
            if (cur == 0 && next == 3) { // wrap around case
                state.agent_direction = 3;
                return LIUVacuumEnvironment.ACTION_TURN_LEFT;
            } else {
                state.agent_direction++;
                return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
            }
        } else {
            return LIUVacuumEnvironment.ACTION_SUCK;
        }
    }

    // moves the Agent to a random start position
    // uses percepts to update the Agent position - only the position, other percepts are ignored
    // returns a random action
    private Action moveToRandomStartPosition(DynamicPercept percept) {
        int action = random_generator.nextInt(6);
        initnialRandomActions--;
        state.updatePosition(percept);
        // update path
        if (state.pathFromStart.isEmpty() || !state.pathFromStart.get(state.pathFromStart.size() - 1).equals(state.pos)) {
            state.pathFromStart.add(new MyAgentState.Pos(state.pos.x, state.pos.y));
        }
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
            if (state.pathFromStart.isEmpty() || !state.pathFromStart.get(state.pathFromStart.size() - 1).equals(state.pos)) {
                state.pathFromStart.add(new MyAgentState.Pos(state.pos.x, state.pos.y));
            }
            state.agent_last_action=state.ACTION_SUCK;
            return LIUVacuumEnvironment.ACTION_SUCK;
        }

        // This example agent program will update the internal agent state while only moving forward.
        // START HERE - code below should be modified!
        
        // Init the state on first action
        if (initnialRandomActions == -1) {
            initnialRandomActions--;
            state.init();
        }

        System.out.println("x=" + state.pos.x);
        System.out.println("y=" + state.pos.y);
        System.out.println("dir=" + state.agent_direction);

        iterationCounter--;

        if (iterationCounter==0) {
            return NoOpAction.NO_OP;
        }

        // If in movement state, turn until in right direction, then move forward
        if (state.needsToFinishMovement) {
            if (state.agent_direction != state.nextAlignment) {
                return getTurn(state.agent_direction, state.nextAlignment);
            } else {
                state.needsToFinishMovement = false;
                return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
            }
        }

        DynamicPercept p = (DynamicPercept) percept;
        Boolean bump = (Boolean)p.getAttribute("bump");
        Boolean dirt = (Boolean)p.getAttribute("dirt");
        Boolean home = (Boolean)p.getAttribute("home");

        // If we bumped, reset to last position, else add neighbours to the toExplore list.
        if (bump) {
            state.pos = state.lastPos;
        } else if (state.backtrackTo == null) {
            state.addExplore();
        }

        state.updatePath();

        // If we are on dirt, immediately suck without asking questions
        if (dirt) {
            state.agent_last_action=state.ACTION_SUCK;
            return LIUVacuumEnvironment.ACTION_SUCK;
        }

        // Get next direction depending on exploration or backtracking
        int nextDir;
        if (state.backtrackTo == null) {
            nextDir = state.explore();
        } else if (state.pos.equals(state.backtrackTo)) {
            // if we stopped backtracking, go back to exploring
            state.backtrackTo = null;
            nextDir = state.explore();
        } else {
            nextDir = state.backtrack();
        }

        // state.explore() returns -1 if we are done exploring
        if (nextDir == -1) return NoOpAction.NO_OP;

        state.nextAlignment = nextDir;

        // either move if we are aligned correctly, or go into movement state if we need to turn
        if (state.agent_direction == nextDir)
            return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;

        state.needsToFinishMovement = true;
        return getTurn(state.agent_direction, state.nextAlignment);
    }
}

