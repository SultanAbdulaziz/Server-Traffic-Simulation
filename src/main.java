import java.util.Scanner;

import Variates.*;
import simUtil.InputHandler;
import SimulatorEngine.Models.*;

void main(){
    InputHandler ih = new InputHandler(new Scanner(System.in));
    ih.display("Simulation Type", new String[]{
        "Single Server Infinite Population (Task 1)",
        "Single Server Finite Population (Task 2)",
        "Closed Network (Task 5)"
    });
    int choice = ih.getInt("Choose: ", 1, 3);

    Model m = switch(choice){
        case 1 -> new SingleServerInfinitePopulation(
            readVariate(ih, "inter-arrival time"),
            readVariate(ih, "service time"),
            ih.getInt("Customer limit m: ", 1, Integer.MAX_VALUE));
        case 2 -> new SingleServerfinitePopulation(
            readVariate(ih, "think time"),
            readVariate(ih, "service time"),
            ih.getInt("Population N: ", 1, Integer.MAX_VALUE),
            ih.getDouble("Simulation end time term_sim: ", 0, Double.MAX_VALUE));
        default -> {
            Variate[] services = new Variate[4];
            for(int i = 0; i < 4; i++)
                services[i] = readVariate(ih, "service time at Server " + (i + 1));
            yield new ClosedNetworkModel(
                services,
                ih.getInt("Population N: ", 1, Integer.MAX_VALUE),
                ih.getInt("Routing seed: ", 1, Integer.MAX_VALUE - 1),
                ih.getDouble("Simulation end time term_sim: ", 0, Double.MAX_VALUE));
        }
    };

    boolean trace = ih.getInt("Trace mode? (1 = yes, 0 = no): ", 0, 1) == 1;
    System.out.println(m.runModel(trace));
}

Variate readVariate(InputHandler ih, String name){
    ih.display("Distribution for " + name, new String[]{"Exponential", "Continuous Uniform"});
    int type = ih.getInt("Choose: ", 1, 2);
    int seed = ih.getInt("Seed: ", 1, Integer.MAX_VALUE - 1);
    if(type == 1)
        return new Exponential(seed, ih.getDouble("Rate (lambda): ", 1e-9, Double.MAX_VALUE));
    double a = ih.getDouble("a: ", 0, Double.MAX_VALUE);
    double b = ih.getDouble("b: ", a, Double.MAX_VALUE);
    return new ContinousUniform(seed, a, b);
}
