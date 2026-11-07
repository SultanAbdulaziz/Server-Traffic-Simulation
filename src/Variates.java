import java.util.ArrayList;

public class Variates {
    public enum VariateDist{
        DISCRETEUNIFORM, 
        GEOMETRIC, 
        CONTINUOUSUNIFORM, 
        EXPONENTIAL 
    }
    private RandomNumberGenerator uniGenerator;
    private VariateDist type;
    private double a;
    private double b;

    public Variates(int seed, VariateDist t){
        uniGenerator = new RandomNumberGenerator(seed);
        type = t;
        a = 0;
        b = 0;
    }

    public Variates(int seed, VariateDist t, double a, double b){
        uniGenerator = new RandomNumberGenerator(seed);
        type = t;
        this.a = a;
        this.b = b;
    }

    public double generateVariate(){
        switch (this.type){
            case VariateDist.DISCRETEUNIFORM:
                 break;
            case VariateDist.GEOMETRIC:
                 break;
            case VariateDist.CONTINUOUSUNIFORM:
                 break;
            case VariateDist.EXPONENTIAL:
                 break;
            default: return 0.0;
        }
    }

    private generateDiscreteUniform(){
        double uniformNumber = uniGenerator.generate();
    }
}
