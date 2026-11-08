package Variates;
import static java.lang.Math.floor;

public class DiscreteUniform extends Variate{
    private double a;
    private double b;

    public DiscreteUniform(int seed, double a, double b){
        super(seed);
        this.a = a;
        this.b = b;
    }

    public double generateNumber(){
        double u = uniGenerator.generate();
        return a + floor(u * (b-a+1));
    }
}

