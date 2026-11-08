import java.util.ArrayList;

public class Variates {
    public enum VariateDist{
        DISCRETE_UNIFORM, 
        GEOMETRIC, 
        CONTINUOUS_UNIFORM, 
        EXPONENTIAL 
    }
    private RandomNumberGenerator uniGenerator;
    private VariateDist type;
    private double a;
    private double b;
    private double lambda;
    private double p;

    public class InvalidVariate extends RuntimeException{
        public InvalidVariate(String message){
            super(message);
        }
    }

    public Variates(int seed, VariateDist t){
        uniGenerator = new RandomNumberGenerator(seed);
        type = t;
    }

    public Variates(int seed, VariateDist t, double a, double b){
        if(t != VariateDist.DISCRETE_UNIFORM && t != VariateDist.CONTINUOUS_UNIFORM)
            throw new InvalidVariate("distribution with 2 values that isn't DISCRETE_UNIFORM or CONTINUOUS_UNIFORM is invalid");
        this(seed, t);
        this.a = a;
        this.b = b;
    }

    public Variates(int seed, VariateDist t, double num){
        if(t != VariateDist.GEOMETRIC && t != VariateDist.EXPONENTIAL)
            throw new InvalidVariate("distribution with 1 value that isn't EXPONENTIAL or GEOMETRIC is invalid");
        this(seed, t);
        type = t;
        if(t == VariateDist.EXPONENTIAL)
            this.lambda = num;
        else
            this.p = num;
    }

    public double generateVariate(){
        switch (this.type){
            case VariateDist.DISCRETE_UNIFORM:
                return generateDiscreteUniform();
            case VariateDist.GEOMETRIC:
                return generateGeometric();
            case VariateDist.CONTINUOUS_UNIFORM:
                return generateContinousUniform();
            case VariateDist.EXPONENTIAL:
                return generateExponential();
            default: return 0.0;
        }
    }

    private double generateDiscreteUniform(){
        return 0.0;
    }
    private double generateGeometric(){
        return 0.0;
    }
    private double generateContinousUniform(){
        return 0.0;
    }
    private double generateExponential(){
        return 0.0;
    }
}
