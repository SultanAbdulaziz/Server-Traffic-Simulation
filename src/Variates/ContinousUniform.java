package Variates;

public class ContinousUniform extends Variate{
    private double a;
    private double b;

    public ContinousUniform(int seed, double a, double b){
        super(seed);
        this.a = a;
        this.b = b;
    }

    public double generateNumber(){
        double u = uniGenerator.generate();
        return a + (b-a) * u;
    }
}
