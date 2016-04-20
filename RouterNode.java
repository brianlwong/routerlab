import java.lang.String;

public class RouterNode {
    private int myID, spaces;
    private int[] costs = new int[RouterSimulator.NUM_NODES];
    private int[] rVector = new int[RouterSimulator.NUM_NODES];  // routing vector
    private int[][] table = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES];
    private GuiTextArea GUI;
    private RouterSimulator simulator;
    private F displayString = new F();

  public RouterNode(int ID, RouterSimulator simulator, int[] costs) {
        myID = ID;
        this.simulator = simulator;
        GUI = new GuiTextArea("  Router "+ ID + "  ");
        System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);

        int[] distVec = new int[RouterSimulator.NUM_NODES];

        for(int i = 0; i < RouterSimulator.NUM_NODES; i++){
            distVec[i] = table[i][myID];
        }

        // sending distance vector to neighbors
        for(int i = 0; i < RouterSimulator.NUM_NODES; i++){
              if(costs[i] != RouterSimulator.INFINITY && costs[i] != 0){  // if i-th is a neighbor
                  this.sendUpdate(new RouterPacket(myID, i, distVec));
              }
        }
        initializeTable();
        printGUIDistanceTable();
  }

  private void initializeTable(){
      for(int i = 0; i < RouterSimulator.NUM_NODES; i++)
      {
          table[i][myID] = costs[i];
          if(RouterSimulator.POISON){
              rVector[i] = -1;
          }

          if(RouterSimulator.POISON && costs[i] != RouterSimulator.INFINITY){
              rVector[i] = i;
          }

          for(int j = 0; j < RouterSimulator.NUM_NODES; j++){
              if(i != j && j != myID){
                  //if nodes are not themselves, initialize to 999
                  table[i][j] = RouterSimulator.INFINITY;
              }
          }
      }
  }

  private void sendUpdate(RouterPacket pkt) {
        simulator.toLayer2(pkt);
    }

  public void receiveUpdate(RouterPacket pkt) {
      //flag to indicate if there has been a change in a node
      boolean updatedFlag = false;
      //link cost has changed

      if( pkt.sourceid == pkt.destid){
          updatedFlag = true;
      }

      // updating distance vector in table for neighbor
      for(int i = 0; i < RouterSimulator.NUM_NODES; i++){
          table[i][pkt.sourceid] = pkt.mincost[i];
      }

      int min;
      // update distance vector
      for(int i = 0; i < RouterSimulator.NUM_NODES; i++){
          // skip when i equals myID (nodes have 0 distance to themselves)
          if(i == myID)
              continue;
          //setting minimum path to 999
          min = RouterSimulator.INFINITY;
          for(int j = 0; j < RouterSimulator.NUM_NODES; j++){
              if(costs[j] < RouterSimulator.INFINITY && j != myID){
                  //if path plus cost < 999, update table to be new minimum
                  if(table[i][j] + costs[j] < min ){
                      min = table[i][j] + costs[j];
                      if(RouterSimulator.POISON)
                          rVector[i] = j;
                  }
              }
          }
          //if current table value != minimum found, update to reflect minimum
          if( table[i][myID] != min){
              updatedFlag = true;
              table[i][myID] = min;
          }
      }

      int[] myDistVec = new int[RouterSimulator.NUM_NODES];

      // send distance vector update to neighbors
      if(updatedFlag){
          for(int i = 0; i < RouterSimulator.NUM_NODES; i++){
              if(costs[i] != RouterSimulator.INFINITY && costs[i] != 0){
                  for(int j = 0; j < RouterSimulator.NUM_NODES; j++){
                      if(RouterSimulator.POISON && rVector[j] == i && rVector[j] != j)
                          myDistVec[j] = RouterSimulator.INFINITY;
                      else
                          myDistVec[j] = table[j][myID];
                  }
                  this.sendUpdate(new RouterPacket(myID, i, myDistVec));
              }
          }
      }
  }

  public void updateLinkCost(int dest, int newCost){
      costs[dest] = newCost;

      int[] myDistVec = new int[RouterSimulator.NUM_NODES];
      for(int i = 0; i < RouterSimulator.NUM_NODES; i++){
          if( i == dest)
              myDistVec[i] = newCost;
          else
              myDistVec[i] = table[i][myID];
          System.out.print(myDistVec[i] + " ");
      }

      System.out.println();

      this.receiveUpdate(new RouterPacket(myID, myID, myDistVec));
      System.out.println("Route to " + dest +" costs " + newCost);
  }

  public void printGUIDistanceTable() {
      //formatting display on GUI window
      spaces = space(RouterSimulator.NUM_NODES) + 4;
	  GUI.println("Current table for " + myID + "  at time " + simulator.getClocktime());
      String dispText = "node | ";
      for(int i = 0; i < RouterSimulator.NUM_NODES; i++){
          if(costs[i] != RouterSimulator.INFINITY){
              dispText += displayString.format("       " + i + " | ", spaces);
          }
      }

      GUI.println(dispText);

      dispText = "";
      for(int i = 0; i < RouterSimulator.NUM_NODES * spaces *2; i++){
          dispText += '-';
      }
      GUI.println(dispText);

      for(int i = 0; i < RouterSimulator.NUM_NODES; i++){
            dispText = displayString.format(i, spaces +2) + " | ";
            for(int j = 0; j < RouterSimulator.NUM_NODES; j++){
                  if(costs[j] != RouterSimulator.INFINITY){
                      if(table[i][j] >= RouterSimulator.INFINITY){
                          dispText += displayString.format("   " + RouterSimulator.INFINITY + " | ", spaces);
                      }
                      else{
                          dispText += displayString.format("       " + table[i][j] + " |  ", spaces);
                      }
                  }
            }
            GUI.println(dispText);
      }
  }

  private int space(int i){
      int j = 0;
      while(i % 10 > 0){
          j++;
          i /= 10;
      }
      return j;
  }
}

