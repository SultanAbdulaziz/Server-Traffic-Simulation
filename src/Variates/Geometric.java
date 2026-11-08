package Variates;
import static java.lang.Math.log;
import static java.lang.Math.ceil;

public class Geometric extends Variate{
    private double probability;

    public Geometric(int seed, double probability){
        super(seed);
        this.probability = probability;
    }

    public double generateNumber(){
        double u = uniGenerator.generate();
        return ceil((log(u)) / (log(1-probability)));
    }
}

