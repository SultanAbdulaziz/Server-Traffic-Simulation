import java.util.Scanner;

import Variates.*;
import SimulatorEngine.*;
import SimulatorEngine.Models.SingleServerInfinitePopulation;
import simUtil.InputHandler;

public static void main(String args[]){
    /* 
    Scanner scanner = new Scanner(System.in);
    InputHandler ih = new InputHandler(scanner);
    String s[] = new String[2];
    s[0] = "task1";
    s[1] = "task2";
    ih.display("Title", s);
    */
   Variate v = new Exponential(10, 1);
   Variate v2 = new Geometric(88, 0.6);
   Model m = new SingleServerInfinitePopulation(v, v2, 20);
   System.out.println(m.runModel(true));
   
}
