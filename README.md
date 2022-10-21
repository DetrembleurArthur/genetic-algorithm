# genetic-algorithm

## Compilation

Pour compiler et exécuter le projet: `mvn clean compile exec:java`
Les arguments à modifier sont dans le fichier pom.xml dans les balises <argument>

```xml
<arguments>
    <argument>50</argument> #width
    <argument>30</argument> #height
    <argument>env.txt</argument> #filename
    <argument>20</argument> #T
    <argument>33332</argument> #mouvements
</arguments>
```

## Système d'axes

En ce qui concerne l'environnement de la créature, le système d'axe est le suivant:

Lorsque x est incrémenté, la créature se dirige vers la droite

Lorsque x est décrémenté, la créature se dirige vers la gauche

Lorsque y est incrémenté, la créature se dirige vers le bas

Lorsque y est décrémenté, la créature se dirige vers le haut

## Généricité de l'algorithme génétique

Au sein de la classe `GeneticAlgorithm` se trouvent plusieurs types "template":

+ T : représente le type d'un gène
+ R : représente le type des fitness calculés
+ S : représente le type de la solution

Cette nomenclature est la même dans les autres classes du package "genetic"

## Paramètres de l'algorithme génétique

```java
//taille de la population (nombre de génomes dans une population)
private int populationSize;
//nombre de gènes d'un génome
private int genomeSize;
//nombre d'itération maximum de l'algorithme
private int maxIterations;
//la solution à atteindre
private S solution;
//la fitness à atteindre (ou critère de comparaison pour stopper l'algorithme)
private R solutionFitness;
//nombre de meilleurs génomes à conserver à chaque itération
private int bestKeepNumber;
//probabilité de choisir le premier parent lors d'un crossover
private double crossoverRate;
//probabilité d'avoir une mutation par itération
private double mutationRate;
//génome final (représente le génome ayant atteint ou éatnt le plus proche de la solution)
private Genome<T> finalGenome;
```

## Précision sur les interfaces ajoutées

### Interface FitnessCalculator

```java
public interface FitnessCalculator<R, S>
{
    R calculateFitness(Genome<?> genome, S solution);
}
```

Cette dernière représente une interface dont le rôle est, lors de son implémentation, de calculer le fitness d'un génome sur base de la solution.

### Interface GeneRandomizer

```java
public interface GeneRandomizer<T>
{
    T randomize();
}
```

Cette interface permet de retourner un gène pour un génome de manière aléatoire

### Interface FitnessToDouble

```java
public interface FitnessToDouble<R>
{
    double map(R fitness);
}
```

Cette interface est à implémenter afin de pouvoir convertir une valeur de fitness en double. Cette fonctionnalité intervient au sein de la sélection par roulette lors de la récolte des effectifs cumulés des fitness.

### Interface Selection

```java
public interface Selection<T>
{
    Genome<T> select(Population<T> population);   
}
```

Cette dernière est à implémenter afin de fournir un mécanisme de sélection pré-crossover. (voir sélection par tournoi et roulette)

### Interface StopGenericCriteria

```java
public interface StopGeneticCriteria<R extends Comparable<R>>
{
    boolean mustBeStopped(R bestFitness, R solutionFitness);
}
```

Cette interface est à implémenter afin que l'algorithme sache quand s'arrêter. Il y a comparaison entre le meilleurs fitness de la population avec la fitness cible.

## Exemples d'application

### Trouver une chaine de bits (string)

```java
public static void main(String[] args)
{
    Random random = new Random();
    GeneticAlgorithm<Character, Integer, String> algorithm = new GeneticAlgorithm<>();
    GeneRandomizer<Character> randomizer = () -> random.nextBoolean() ? '0' : '1';
    TournamentSelection<Character> tournamentSelection = new TournamentSelection<>(5, algorithm);
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

    Genome<Character> genome = algorithm.getFinalGenome();
    for(Character c : genome.getGenes())
    {
        System.out.print(c);
    }
    System.out.println();
}
```

### Trouver une suite de nombres dont la somme vaut 1000

```java
public static void main(String[] args)
{
    Random random = new Random();
    GeneticAlgorithm<Integer, Integer, Integer> algorithm = new GeneticAlgorithm<>();
    GeneRandomizer<Integer> randomizer = () -> random.nextInt(10000);
    TournamentSelection<Integer> tournamentSelection = new TournamentSelection<>(5, algorithm);
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

    Genome<Integer> genome = algorithm.getFinalGenome();
    int sum = 0;
    for(Integer number : genome.getGenes())
    {
        System.out.println(number);
        sum += number;
    }
    System.out.println("sum: " + sum);
}
```
