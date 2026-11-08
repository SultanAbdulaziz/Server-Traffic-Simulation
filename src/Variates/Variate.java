package Variates;

public abstract class Variate{
    protected RandomNumberGenerator uniGenerator;

    public Variate(int seed){
        uniGenerator = new RandomNumberGenerator(seed);
    }

    public abstract double generateNumber();
}
