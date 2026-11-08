package Variates;
import static java.lang.Math.log;

public class Exponential extends Variate{
    private double lambda;

    public Exponential(int seed, double lambda){
        super(seed);
        this.lambda = lambda;
    }

    public double generateNumber(){
        double u = uniGenerator.generate();
        return -(1/lambda) * log(1-u);
    }
}

