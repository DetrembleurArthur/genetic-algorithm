# genetic-algorithm

## Compilation

Pour compiler et exécuter le projet: `mvn clean compile exec:java`
Les arguments à modifier sont dans le fichier pom.xml dans les balises <argument>

```xml
<arguments>
    <argument>12</argument> #width
    <argument>6</argument> #height
    <argument>env-x-y.txt</argument> #filename
    <argument>100</argument> #T
    <argument>COORD_Y_TO_DOWN</argument> #type de fichier d'environnement
</arguments>
```

Le type de fichier d'environnement peut être:

```bash
COORD_Y_TO_DOWN
# contient les coordonnées des éléments de l'environnement (x y)
# la coordonnée y augmente de haut en bas
7 5 => départ
8 5 => arrivée
9 5 => Murs...
10 5
11 5
0 1
0 2
0 3
0 4
11 1
11 2
11 3
11 4
3 4
6 4
7 4
9 4
10 4
7 3
9 3
10 3
10 2

COORD_Y_TO_UP
# contient les coordonnées des éléments de l'environnement (x y)
# la coordonnée y augmente de bas en haut
# inverse l'axe y par rapport à l'exemple précédent
TILES
# contient toutes les cases d'un environnement
1 1 1 1 1 1 1 1 1 1 1 1
1 0 0 0 0 0 0 0 0 0 3 1
1 0 0 0 0 0 0 0 0 0 1 1
1 0 0 0 0 0 0 1 0 1 1 1
1 2 0 1 0 0 1 1 0 1 1 1
1 1 1 1 1 1 1 1 1 1 1 1

```

Il est possible de placer "random" à la place d'un nom de fichier d'environnement.

Le programme va alors générer un environnement aléatoire basé sur la taille renseignée par l'utilisateur.

Cet environnement sera également enregistré sur la machine.



## Déploiement

Pour déployer l'application: `mvn clean package`
Pour exécuter ce .jar : `cd target`
`java -jar .\genetic-1.0-SNAPSHOT-jar-with-dependencies.jar 12 6 ..\env-x-y.txt 100 COORD_Y_TO_DOWN`

Attention, le programme requiert un fichier genetic_game.properties.

Ce dernier doit se trouver au même endroit que le JAR.

Le fichier contient les données de paramétrage de l'algorithme génétique:

```properties
best.keep.number=5
crossover.rate=0.5
mutation.rate=0.05
genome.size=10
max.iterations=10000
population.size=25
tournament.size=5
#fitness idéale à atteindre (l'algo s'arrête si elle est dépassée)
solution.fitness=3.236111
#le nombre de threads lancés dans le pool executor
thread.pool.size=4
#bloque le programme à chaque itération si activé (l'utilisateur peut ainsi vérifier le status de l'algorithme et lancer n itérations à la fois)
# l'utilisateur écrit "n <nombre de génération>" pour lancer un certain nombre de génération
# l'utilisateur écrit "s" pour voir la meilleure fitness de l'algorithme et pour animer la meilleure créature
manually.controlled=false
#le temps entre chaque mouvement de créature
env.anim.time=500
#enregistre à chaque itération les fitness de chaque génome dans un fichier (voir record.file)
record.population=false
record.file=population.csv
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

Cette nomenclature est la même dans les autres classes du package "generic"

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
//représente l'algorithme de sélection de gènes pour le crossover
private Selection<T> selection;
//voir: les données du fichier properties
private boolean manuallyControlled = false;
//fonction appelée à chaque fois que l'utilisateur veut consulter le status de l'algorithme
private StatusRunner<T> statusRunner;
//résultat de l'algorithme (fitness, génome final, itérations, temps en ms)
private GeneticResult<T, R> geneticResult;
//permet d'enregistrer les données des génomes à chaque itération (voir genetic_game.properties)
private Recorder recorder;
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
    Genome<T> select();   
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

### Interface StatusRunner

```java
public interface StatusRunner<T>
{
    void run(Genome<T> genome);
}
```

Méthode appelée à chaque fois que l'utilisateur veut consulter le status de l'algorithme génétique.



## Parallélisation

Afin d'économiser du temps de calcul un pool de thread est utilisé afin de calculer les fitness des différents génomes.

Les fitness sont calculées:

+ Au démarrage de l'algorithme, lorsque la population est générée

  ```java
  for (Genome<T> genome : population.getGenomes())
  {
      Future<R> fitnessFuture = executor.submit(() -> fitnessCalculator.calculateFitness(genome, solution));
      FitnessCache<T, R> cache = new FitnessCache<>(genome, fitnessFuture);
      fitnessCache.add(cache);
  }
  ```

  

+ Lorsqu'un génome vient d'être créé après crossover et mutation

  ```java
  for (int i = bestKeepNumber; i < populationSize; i++)
  {
      Genome<T> genome1 = selection.select();
      Genome<T> genome2 = selection.select();
      Genome<T> child = crossover(genome1, genome2);
      mutate(child);
      Future<R> fitnessFuture = executor.submit(() -> fitnessCalculator.calculateFitness(child, solution));
      FitnessCache<T, R> cache = new FitnessCache<>(child, fitnessFuture);
      fitnessCacheBackup.add(cache);
      temp.getGenomes().add(child);
  }
  ```

Chaque calcule de fitness est envoyé au thread executor. Ce dernier renvoie directement un Futur qui permettra lorsque l'algorithme aura besoin d'avoir la fitness de récupérer cette dernière.

Ce futur est associé avec le génome dans un objet FitnessCache.

La population de génome n'est pas représenté par la classe Population mais bien par une liste de FitnessCache.

Associer le génome avec sa fitness est plus rapide que de recalculer sa fitness à chaque fois que l'on veut y accéder.

En fin de compte la classe Population ne sert qu'à initialiser les génomes.

En ce qui concerne les protections d'accès concurrents, deux verrous ont été placés:

+ Méthode calculateFinalPosition de l'environnement

```java
Vector2i finalPosition;
Vector2i officialEndPosition;
int tickCountLimit;
synchronized (this)
{
    finalPosition = new Vector2i(startPosition);
    officialEndPosition = new Vector2i(endPosition);
    tickCountLimit = maxTickCount;
}
```

+ Lors de l'accès à une case du tableau

```java
public synchronized byte getCase(Vector2i position)
{
    if (!outOfBand(position))
        return grid[position.y][position.x];
    return Cases.OOB;
}
```



## Calcul des fitness

La fitness de chaque créature est calculée selon la formule suivante:

Avec D étant la distance entre la créature et l'arrivée

Avec M étant le nombre de mouvements utilisés par la créature

Avec T étant le nombre de ticks utilisés par la créature

```
fitness = D * 3 + (M + T)
```

D a un poids plus fort que les deux autres paramètre étant donnée qu'il représente le critère le plus important à satisfaire pour la créature.

## Performance

Après avoir exécuté 100x l'algorithme génétique pour un environnement donné de taille (12, 6), le temps moyen d'exécution est de:

714ms pour 2928 itérations en moyenne.

4 threads étaient actifs dans le pool executor.



## Recherche d'un critère d'arrêt

Afin de trouver un bon critère d'arrêt (fitness limite), je laisse tourner l'algorithme jusqu'à son nombre d'itération maximum.

Ensuite, je récolte les fitness de tous les génomes pour chaque itération et je regarde si elles tendent vers un maximum.

Je réitère cette opération et je regarde quelle est la fitness maximum qui apparait.

Cette dernière devient alors mon critère d'arrêt.



## Détails supplémentaires

Au début d'une itération,lorsque l'on récolte les X meilleurs génomes de la population, je procède comme suit:

```java
List<FitnessCache<T, R>> sorted = fitnessCache
                .stream()
                .sorted(fitnessComparator)
                .limit(bestKeepNumber)
                .collect(Collectors.toList());
```

Je trie la population de manière décroissante via le fitnessComparator (c'est pourquoi ce dernier compare de manière inversée):

```java
private final Comparator<FitnessCache<T, R>> fitnessComparator = (o1, o2) -> {
        try
        {
            return o2.getFitnessFuture().get().compareTo(o1.getFitnessFuture().get());
        } catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        return 0;
};
```

Le "limit" me permettant de ne conserver que les fitness les plus grandes.

Le fait que le comparateur compare à l'envers, implique que lorsque l'on souhaite récupérer le génome le plus grand en terme de fitness, il faut chercher le minimum de la population:

```java
public FitnessCache<T, R> getFittest()
{
	return fitnessCache.stream()
		.min(fitnessComparator) => car le fitnessComparator est inversé
		.orElse(null);
}
```

Il en est de même dans la classe TournamentSelection



Lors de l'animation d'une créature sur la grille, chaque mouvement est affiché et peut être considéré comme environnemental ou non.

Les mouvements environnementaux concernent les mouvements dus à la gravité.

Lors de l'affichage des mouvement, un mouvement peut aussi être affiché sous deux dimensions (real et origin)

real: mouvement que la créature applique avec succès.

origin: mouvement que la créature a voulu appliqué mais qui n'est pas possible.

Par exemple, si une créature veut aller vers la droite mais qu'un mûr si trouve,

le mouvement "real" sera BLOCKED (la créature n'a pas pu bouger), mais le mouvement "origin" sera RIGHT.



## Exemples d'application 

*Dans ces exemples, le fichier genetic_game n'est pas utilisé*

### Trouver une chaine de bits (string)

```java
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
```

### Trouver une suite de nombres dont la somme vaut 1000

```java
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
System.out.println("sum: " + sum);Random random = new Random();
```
