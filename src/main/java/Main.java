import com.arthur.hepl.old.Environment;

public class Main
{
    public static void main(String[] args)
    {
        /*
        SimpleGeneticAlgorithm sga = new SimpleGeneticAlgorithm();
        sga.runAlgorithm(25, "0101010101010101010101010101010101010101010101010101010101010101");
        */
        Environment env = Environment.buildFromArgs(args);
        env.setTickMs(500);
        env.run();
    }
}
