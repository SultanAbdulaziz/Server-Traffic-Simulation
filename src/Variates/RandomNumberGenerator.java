package Variates;
import static java.lang.Math.pow;

public class RandomNumberGenerator{
    private int prevNumber;
    
    public RandomNumberGenerator(int seed){
        prevNumber = seed;
    }

    public double generate(){
        prevNumber = (int) (pow(7, 5) * prevNumber % (pow(2, 31) - 1));
        return (prevNumber/pow(2, 31));
    }
}
