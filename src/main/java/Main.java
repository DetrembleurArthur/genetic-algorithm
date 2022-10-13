import com.baeldung.algorithms.ga.binary.SimpleGeneticAlgorithm;

public class Main
{
    public static void main(String[] args)
    {
        SimpleGeneticAlgorithm sga = new SimpleGeneticAlgorithm();
        sga.runAlgorithm(25, "0101010101010101010101010101010101010101010101010101010101010101");
    }
}
