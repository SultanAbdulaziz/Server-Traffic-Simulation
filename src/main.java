import java.util.Scanner;

import Variates.*;
import SimulatorEngine.*;
import SimulatorEngine.Models.Model;
import SimulatorEngine.Models.SingleServerInfinitePopulation;

public static void main(String args[]){
    /* 
    Scanner scanner = new Scanner(System.in);
    InputHandler ih = new InputHandler(scanner);
    String s[] = new String[2];
    s[0] = "task1";
    s[1] = "task2";
    ih.display("Title", s);
    */
    Variate v1 = new Exponential(10, 1);
    Variate v2 = new Exponential(11, 0.3);
    Model m = new SingleServerInfinitePopulation(v1, v2, 50);
    System.out.println(m.runModel(true));
   
}
