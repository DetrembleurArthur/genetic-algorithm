import com.arthur.hepl.env.Creature;
import com.arthur.hepl.env.EnvCalculationResult;
import com.arthur.hepl.env.Environnement;
import com.arthur.hepl.env.Movements;
import com.arthur.hepl.generic.*;
import com.arthur.hepl.perf.Recorder;
import lombok.SneakyThrows;
import org.joml.Vector2i;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

//slide 22 du lab 2 (environnement
public class Main
{
    private static void simpleCreatureAnimation(String[] args)
    {
        Creature creature = args.length == 5 ? Creature.fromMoveString(args[4]) : new Creature();
        Environnement env = Environnement.buildFromArgs(args);
        assert env != null;
        env.animate(creature, 500, false);
    }

    public enum GameProp
    {
        BEST_KEEP_NUMBER("best.keep.number"),
        CROSSOVER_RATE("crossover.rate"),
        MUTATION_RATE("mutation.rate"),
        GENOME_SIZE("genome.size"),
        MAX_ITERATIONS("max.iterations"),
        POPULATION_SIZE("population.size"),
        TOURNAMENT_SIZE("tournament.size"),
        SOLUTION_FITNESS("solution.fitness"),
        MANUALLY_CONTROLLED("manually.controlled"),
        ENV_ANIM_TIME("env.anim.time"),
        RECORD_POPULATION("record.population"),
        RECORD_FILE("record.file"),
        THREAD_POOL_SIZE("thread.pool.size");
        final String name;

        GameProp(String name)
        {
            this.name = name;
        }
    }

    @SneakyThrows
    private static Properties getProperties(String filename)
    {
        final Properties properties = new Properties();
        File file = new File(filename);
        properties.load(new FileReader(filename));
        return properties;
    }

    private static void geneticCreatureProcess(String[] args)
    {
        Environnement env = Environnement.buildFromArgs(args);
        if (env != null)
        {
            Properties properties = getProperties("genetic_game.properties");
            GeneticAlgorithm<Movements, Double, Vector2i> algorithm = new GeneticAlgorithm<>();
            GeneRandomizer<Movements> randomizer = Movements::random;
            TournamentSelection<Movements, Double> tournamentSelection = new TournamentSelection<>(Integer.parseInt((String) properties.getOrDefault(GameProp.TOURNAMENT_SIZE.name, "5")), algorithm);
            FitnessCalculator<Double, Vector2i> fitnessCalculator = (genome, solution) -> {
                Creature creature = new Creature((ArrayList<Movements>) genome.getGenes());
                EnvCalculationResult result = env.calculateFinalPositionWithMap(creature, null);
                double distScore = 1.0 / (result.getDistanceWithEndPosition() + 1.0);
                double movesUsedScore = 1.0 / (result.getMovesUsed() + 1.0);
                double ticksScore = 1.0 / result.getTickCount();
                return distScore * 3 + movesUsedScore + ticksScore;
            };
            algorithm.initThreadPool(Integer.parseInt((String) properties.getOrDefault(GameProp.THREAD_POOL_SIZE.name, "5")));
            algorithm.setBestKeepNumber(Integer.parseInt((String) properties.getOrDefault(GameProp.BEST_KEEP_NUMBER.name, "5")));
            algorithm.setCrossoverRate(Double.parseDouble((String) properties.getOrDefault(GameProp.CROSSOVER_RATE.name, "0.5")));
            algorithm.setMutationRate(Double.parseDouble((String) properties.getOrDefault(GameProp.MUTATION_RATE.name, "0.05")));
            algorithm.setGenomeSize(Integer.parseInt((String) properties.getOrDefault(GameProp.GENOME_SIZE.name, "10")));
            algorithm.setMaxIterations(Integer.parseInt((String) properties.getOrDefault(GameProp.MAX_ITERATIONS.name, "2000")));
            algorithm.setPopulationSize(Integer.parseInt((String) properties.getOrDefault(GameProp.POPULATION_SIZE.name, "25")));
            algorithm.setSolution(env.getEndPosition());
            algorithm.setSolutionFitness(Double.parseDouble((String) properties.getOrDefault(GameProp.SOLUTION_FITNESS.name, "1.236")));
            algorithm.setFitnessCalculator(fitnessCalculator);
            algorithm.setRandomizer(randomizer);
            algorithm.setSelection(tournamentSelection);
            algorithm.setStopGeneticCriteria((bestFitness, solutionFitness) -> bestFitness >= solutionFitness);

            algorithm.setManuallyControlled(Boolean.parseBoolean((String) properties.getOrDefault(GameProp.MANUALLY_CONTROLLED.name, "false")));
            int envAnimTimeMs = Integer.parseInt((String) properties.getOrDefault(GameProp.ENV_ANIM_TIME.name, "1000"));
            algorithm.setStatusRunner(genome -> {
                Creature creature = new Creature(genome.getGenes());
                env.animate(creature, envAnimTimeMs, false);
            });

            if (Boolean.parseBoolean((String) properties.getOrDefault(GameProp.RECORD_POPULATION.name, "false")))
            {
                algorithm.setRecorder(new Recorder((String) properties.getOrDefault(GameProp.RECORD_FILE.name, "population.csv")));
            }

            algorithm.run();
            algorithm.stopThreadPool();

            var result = algorithm.getGeneticResult();
            Genome<Movements> genome = result.getGenome();


            Creature creature = new Creature(genome.getGenes());
            env.animate(creature, envAnimTimeMs, false);

            System.out.println("Mouvements:");
            for (Movements move : genome.getGenes())
            {
                System.out.println("\t" + move);
            }

            System.out.println("\nResume:");
            System.out.println("Iterations: " + result.getIterations());
            System.out.println("Temps: " + result.getTimeMs() + "ms");
            System.out.println("Fitness: " + result.getFitness());
        }
    }

    public static void main(String[] args) throws IOException
    {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        geneticCreatureProcess(args);
    }
}
