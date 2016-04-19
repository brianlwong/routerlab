import javax.swing.*;

import java.util.Arrays;
import java.lang.String;

public class RouterNode {
    private int myID;
    private int n;
    private GuiTextArea myGUI;
    private RouterSimulator sim;
    private int[] costs = new int[RouterSimulator.NUM_NODES];
    private int[] via = new int[RouterSimulator.NUM_NODES];  // routing vector
    private int[][] table = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES];
    private F f = new F();

  //--------------------------------------------------
  public RouterNode(int ID, RouterSimulator sim, int[] costs) {
    myID = ID;
    this.sim = sim;
    myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");

      n = space(RouterSimulator.NUM_NODES) + 4;
      System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);


      int[] myDistVec = new int[RouterSimulator.NUM_NODES];
      for(int i = 0; i < RouterSimulator.NUM_NODES; i++)
          myDistVec[i] = table[i][myID];

      // send my distance vector to neighbors
      for(int i = 0; i < RouterSimulator.NUM_NODES; i++){
          if(costs[i] != RouterSimulator.INFINITY && costs[i] != 0){  // if i-th is a neighbor
              this.sendUpdate(new RouterPacket(myID, i, myDistVec));
          }
      }
      initializeTable();
      printDistanceTable();
  }
  //--------------------------------------------------
  private int space(int n){
      int i = 0;
      while(n % 10 > 0){
          n /= 10;
          i++;
      }
      return i;
  }

  //--------------------------------------------------
  private void initializeTable(){
      for(int i = 0; i < RouterSimulator.NUM_NODES; i++)
      {
          table[i][myID] = costs[i];
          if(RouterSimulator.POISON) via[i] = -1;

          if(RouterSimulator.POISON && costs[i] != RouterSimulator.INFINITY) // if neighbor
              via[i] = i;
          for(int j = 0; j < RouterSimulator.NUM_NODES; j++)
              if(i != j && j != myID)
                  table[i][j] = RouterSimulator.INFINITY;
      }
  }

  //--------------------------------------------------
  public void recvUpdate(RouterPacket pkt) {
      boolean changed = false;
      //link cost has changed

      if( pkt.sourceid == pkt.destid)
          changed = true;

      // update neighbor's distance vector in my table
      for(int i = 0; i < RouterSimulator.NUM_NODES; i++)
          table[i][pkt.sourceid] = pkt.mincost[i];


      int min;
      // update my DV
      for(int i = 0; i < RouterSimulator.NUM_NODES; i++){
          // skip when i equals myID
          if(i == myID)
              continue;
          min = RouterSimulator.INFINITY;
          for(int j = 0; j < RouterSimulator.NUM_NODES; j++){

              if(costs[j] < RouterSimulator.INFINITY && j != myID){
                  if(table[i][j] + costs[j] < min ){
                      min = table[i][j] + costs[j];
                      if(RouterSimulator.POISON)
                          via[i] = j;
                  }
              }
          }
          if( table[i][myID] != min){
              if(min == 60) System.out.println("HEJ!!!");
              changed = true;
              table[i][myID] = min;
          }
      }

      int[] myDistVec = new int[RouterSimulator.NUM_NODES];

      // send updates of my DV to neighbors
      if(changed){
          for(int i = 0; i < RouterSimulator.NUM_NODES; i++){
              if(costs[i] != RouterSimulator.INFINITY && costs[i] != 0){  // if neighbors

                  for(int j = 0; j < RouterSimulator.NUM_NODES; j++){
                      if(RouterSimulator.POISON && via[j] == i && via[j] != j)
                          myDistVec[j] = RouterSimulator.INFINITY;
                      else
                          myDistVec[j] = table[j][myID];
                  }

                  System.out.println("What (" + myID + ") sends to " + i +":\n");

                  for(int j = 0; j < RouterSimulator.NUM_NODES; j++)
                      System.out.print(myDistVec[j] + " ");
                  System.out.print("PRINT?\n");
                  this.sendUpdate(new RouterPacket(myID, i, myDistVec));
              }
          }
      }
  }
  

  //--------------------------------------------------
  private void sendUpdate(RouterPacket pkt) {
    sim.toLayer2(pkt);
  }
  

  //--------------------------------------------------
  public void printDistanceTable() {
	  myGUI.println("Current table for " + myID +
			"  at time " + sim.getClocktime());
    String str = "node | ";
    for(int i = 0; i < RouterSimulator.NUM_NODES; i++)
      if(costs[i] != RouterSimulator.INFINITY)
        str += f.format(i, n);
    myGUI.println(str);

    str = "";
    for(int i = 0; i < RouterSimulator.NUM_NODES * n*2; i++)
      str += '-';
    myGUI.println(str);

    for(int i = 0; i < RouterSimulator.NUM_NODES; i++){
      str = f.format(i, n+2) + " | ";
      for(int j = 0; j < RouterSimulator.NUM_NODES; j++){
        if(costs[j] != RouterSimulator.INFINITY){
          if(table[i][j] >= RouterSimulator.INFINITY)
            str += f.format(RouterSimulator.INFINITY, n);
          else
            str += f.format(table[i][j], n);
        }
      }
      myGUI.println(str);
    }
  }

  //--------------------------------------------------
  public void updateLinkCost(int dest, int newcost) {
      costs[dest] = newcost;
      //initializeTable();

      int[] myDistVec = new int[RouterSimulator.NUM_NODES];
      for(int i = 0; i < RouterSimulator.NUM_NODES; i++){
          if( i == dest)
              myDistVec[i] = newcost;
          else
              myDistVec[i] = table[i][myID];
          System.out.print(myDistVec[i] + " ");
      }

      System.out.println();

      this.recvUpdate(new RouterPacket(myID, myID, myDistVec));
      System.out.println(this.myID + " link change! Route to " + dest +" costs " + newcost);
  }

}
