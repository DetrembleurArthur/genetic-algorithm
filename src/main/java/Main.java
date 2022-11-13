import com.arthur.hepl.env.Creature;
import com.arthur.hepl.env.EnvCalculationResult;
import com.arthur.hepl.env.Environnement;
import com.arthur.hepl.env.Movements;
import com.arthur.hepl.generic.*;
import org.joml.Vector2i;

import java.util.Random;

public class Main
{
    private static void simpleCreatureAnimation(String[] args)
    {
        Creature creature = new Creature();
        Environnement env = Environnement.buildFromArgs(args, creature);
        assert env != null;
        env.animate(creature, 500);
    }

    private static void geneticCreatureProcess(String[] args)
    {
        Environnement env = Environnement.buildFromArgs(args);
        if(env != null)
        {
            GeneticAlgorithm<Movements, Double, Vector2i> algorithm = new GeneticAlgorithm<>();
            GeneRandomizer<Movements> randomizer = Movements::random;
            TournamentSelection<Movements, Double> tournamentSelection = new TournamentSelection<>(5, algorithm);
            FitnessCalculator<Double, Vector2i> fitnessCalculator = (genome, solution) -> {
                Creature creature = new Creature();
                genome.getGenes().forEach(move -> creature.addMovement((Movements) move));
                EnvCalculationResult result = env.calculateFinalPosition(creature, null);
                double distScore = 1.0 / (result.getDistanceWithEndPosition() + 1.0);
                double movesUsedScore = 1.0 / (result.getMovesUsed() + 1.0);
                double ticksScore = 1.0 / result.getTickCount();
                return distScore + movesUsedScore + ticksScore;
            };
            algorithm.setBestKeepNumber(5);
            algorithm.setCrossoverRate(0.5);
            algorithm.setMutationRate(0.05);
            algorithm.setGenomeSize(10);
            algorithm.setMaxIterations(20000);
            algorithm.setPopulationSize(25);
            algorithm.setSolution(env.getEndPosition());
            algorithm.setSolutionFitness(1.236);
            algorithm.setFitnessCalculator(fitnessCalculator);
            algorithm.setRandomizer(randomizer);
            algorithm.setSelection(tournamentSelection);
            algorithm.setStopGeneticCriteria((bestFitness, solutionFitness) -> bestFitness >= solutionFitness);

            algorithm.run();
            algorithm.stopThreadPool();

            Genome<Movements> genome = algorithm.getFinalGenome();


            Creature creature = new Creature();
            genome.getGenes().forEach(creature::addMovement);
            env.animate(creature, 500);

            for(Movements move : genome.getGenes())
            {
                System.out.println(move);
            }
        }
    }

    public static void string()
    {
        Random random = new Random();
        GeneticAlgorithm<Character, Integer, String> algorithm = new GeneticAlgorithm<>();
        GeneRandomizer<Character> randomizer = () -> random.nextBoolean() ? '0' : '1';
        TournamentSelection<Character, Integer> tournamentSelection = new TournamentSelection<>(5, algorithm);
        FitnessCalculator<Integer, String> fitnessCalculator = (genome, solution) -> {
            int sum = 0;
            for(int i = 0; i < solution.length(); i++)
            {
                if(solution.charAt(i) == (char)genome.getGenes().get(i))
                    sum++;
            }
            return sum;
        };
        String solution = "0101010101010101010101010101010101010101010101010101010101010101";
        algorithm.setBestKeepNumber(5);
        algorithm.setCrossoverRate(0.5);
        algorithm.setMutationRate(0.05);
        algorithm.setGenomeSize(solution.length());
        algorithm.setMaxIterations(10000);
        algorithm.setPopulationSize(25);
        algorithm.setSolution(solution);
        algorithm.setSolutionFitness(solution.length());
        algorithm.setFitnessCalculator(fitnessCalculator);
        algorithm.setRandomizer(randomizer);
        algorithm.setSelection(tournamentSelection);

        algorithm.run();
        algorithm.stopThreadPool();

        Genome<Character> genome = algorithm.getFinalGenome();
        for(Character c : genome.getGenes())
        {
            System.out.print(c);
        }
        System.out.println();
    }

    public static void number()
    {
        Random random = new Random();
        GeneticAlgorithm<Integer, Integer, Integer> algorithm = new GeneticAlgorithm<>();
        GeneRandomizer<Integer> randomizer = () -> random.nextInt(10000);
        TournamentSelection<Integer, Integer> tournamentSelection = new TournamentSelection<>(5, algorithm);
        FitnessCalculator<Integer, Integer> fitnessCalculator = (genome, solution) -> {
            int sum = genome.getGenes().stream()
                    .mapToInt((g) -> (Integer)g)
                    .sum();
            int solutionFitness = algorithm.getSolutionFitness();
            return solutionFitness - Math.abs(solutionFitness - sum);
        };
        algorithm.setBestKeepNumber(5);
        algorithm.setCrossoverRate(0.5);
        algorithm.setMutationRate(0.05);
        algorithm.setGenomeSize(5);
        algorithm.setMaxIterations(10000);
        algorithm.setPopulationSize(25);
        algorithm.setSolution(1000);
        algorithm.setSolutionFitness(1000);
        algorithm.setFitnessCalculator(fitnessCalculator);
        algorithm.setRandomizer(randomizer);
        algorithm.setSelection(tournamentSelection);

        algorithm.run();
        algorithm.stopThreadPool();

        Genome<Integer> genome = algorithm.getFinalGenome();
        int sum = 0;
        for(Integer number : genome.getGenes())
        {
            System.out.println(number);
            sum += number;
        }
        System.out.println("sum: " + sum);
    }

    public static void main(String[] args)
    {
        geneticCreatureProcess(args);
        //string();
        //number();
    }
}
