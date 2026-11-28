import java.util.Scanner;

import Variates.*;
import simUtil.InputHandler;
import SimulatorEngine.Models.*;

void main() {
    InputHandler ih = new InputHandler(new Scanner(System.in));
    ih.display("Simulation Type", new String[]{
            "Single Server Infinite Population (Task 1)",
            "Single Server Finite Population (Task 2)",
            "M-Stages Tandem Queue (Task 3)",
            "Tandem Queue with Blocking (Task 4)",
            "Closed Network (Task 5)"
    });
    int choice = ih.getInt("Choose: ", 1, 5);

    Model m = switch (choice) {

        case 1 -> new SingleServerInfinitePopulation(
                readVariate(ih, "inter-arrival time"),
                readVariate(ih, "service time"),
                ih.getInt("Customer limit m: ", 1, Integer.MAX_VALUE));

        case 2 -> new SingleServerfinitePopulation(
                readVariate(ih, "think time"),
                readVariate(ih, "service time"),
                ih.getInt("Population N: ", 1, Integer.MAX_VALUE),
                ih.getDouble("Simulation end time term_sim: ", 0, Double.MAX_VALUE));

        case 3 -> new TandemQueue(
                readVariate(ih, "inter-arrival time"),
                readVariate(ih, "service time"),
                ih.getInt("Customer limit m: ", 1, Integer.MAX_VALUE),
                ih.getInt("Stage count M: ", 2, Integer.MAX_VALUE / 2)
                );

        case 4 -> new BlockingTandemQueue(
                readVariate(ih, "inter-arrival time"),
                readVariate(ih, "service time"),
                ih.getInt("Customer limit m: ", 1, Integer.MAX_VALUE),
                ih.getInt("Stage 2 capacity K: ", 1, Integer.MAX_VALUE));

        case 5 -> {
            Variate[] services = new Variate[4];
            for (int i = 0; i < 4; i++)
                services[i] = readVariate(ih, "service time at Server " + (i + 1));
            yield new ClosedNetworkModel(
                    services,
                    ih.getInt("Population N: ", 1, Integer.MAX_VALUE),
                    ih.getInt("Routing seed: ", 1, Integer.MAX_VALUE - 1),
                    ih.getDouble("Simulation end time term_sim: ", 0, Double.MAX_VALUE));
        }

        default -> throw new IllegalArgumentException("Invalid choice");
    };

    boolean trace = ih.getInt("Trace mode? (1 = yes, 0 = no): ", 0, 1) == 1;
    System.out.println(m.runModel(trace));
}

Variate readVariate(InputHandler ih, String name) {
    ih.display("Distribution for " + name, new String[]{
            "Exponential",
            "Continuous Uniform",
            "Deterministic (fixed values)",
            "Discrete Uniform",
            "Geometric"
    });
    int type = ih.getInt("Choose: ", 1, 5);
    if (type == 3)
        return new Deterministic(readValues(ih));

    int seed = ih.getInt("Seed: ", 1, Integer.MAX_VALUE - 1);
    switch (type) {
        case 1:
            return new Exponential(seed, ih.getDouble("Rate (lambda): ", 1e-9, Double.MAX_VALUE));
        case 2: {
            double a = ih.getDouble("a: ", 0, Double.MAX_VALUE);
            double b = ih.getDouble("b: ", a, Double.MAX_VALUE);
            return new ContinousUniform(seed, a, b);
        }
        case 4: {
            int a = ih.getInt("a (smallest integer): ", 0, Integer.MAX_VALUE - 1);
            int b = ih.getInt("b (largest integer): ", a, Integer.MAX_VALUE - 1);
            return new DiscreteUniform(seed, a, b);
        }
        default:
            return new Geometric(seed, ih.getDouble("Success probability p (0 < p < 1): ", 1e-9, 1 - 1e-9));
    }
}

double[] readValues(InputHandler ih) {
    while (true) {
        String[] parts = ih.getString("Values (separated by spaces or commas): ").split("[,\\s]+");
        try {
            double[] values = new double[parts.length];
            for (int i = 0; i < parts.length; i++) {
                values[i] = Double.parseDouble(parts[i]);
                if (values[i] < 0)
                    throw new NumberFormatException();
            }
            return values;
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Enter non-negative numbers only, e.g. 8 6 1 8");
        }
    }
}
