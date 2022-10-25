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
            TournamentSelection<Movements> tournamentSelection = new TournamentSelection<>(5, algorithm);
            FitnessCalculator<Double, Vector2i> fitnessCalculator = (genome, solution) -> {
                Creature creature = new Creature();
                genome.getGenes().forEach(move -> creature.addMovement((Movements) move));
                EnvCalculationResult result = env.calculateFinalPosition(creature, null);
                double distScore = 1.0 / (result.getDistanceWithEndPosition() + 1.0);
                double movesUsedScore = 1.0 / (result.getMovesUsed() + 1);
                return distScore + movesUsedScore;
            };
            algorithm.setBestKeepNumber(5);
            algorithm.setCrossoverRate(0.5);
            algorithm.setMutationRate(0.05);
            algorithm.setGenomeSize(30);
            algorithm.setMaxIterations(5000);
            algorithm.setPopulationSize(25);
            algorithm.setSolution(env.getEndPosition());
            algorithm.setSolutionFitness(1.1);
            algorithm.setFitnessCalculator(fitnessCalculator);
            algorithm.setRandomizer(randomizer);
            algorithm.setSelection(tournamentSelection);
            algorithm.setStopGeneticCriteria((bestFitness, solutionFitness) -> bestFitness >= solutionFitness);

            algorithm.run();

            Genome<Movements> genome = algorithm.getFinalGenome();
            for(Movements move : genome.getGenes())
            {
                System.out.println(move);
            }

            Creature creature = new Creature();
            genome.getGenes().forEach(creature::addMovement);
            env.animate(creature, 1500);
        }
    }

    public static void main(String[] args)
    {
        geneticCreatureProcess(args);
    }
}
