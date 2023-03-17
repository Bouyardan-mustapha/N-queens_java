package nqueens;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Methods {
	private static final int POPULATION_SIZE = 100;
    private static final double MUTATION_RATE = 0.01;
    private static final int TOURNAMENT_SIZE = 5;
    private static final int MAX_GENERATIONS = 1000;

    private static final Random random = new Random();
	static int[] method1(int n) {
		int[] solution = null;
		

        // Initialize population
        ArrayList<int[]> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(generateChromosome(n));
        }

        // Evolve population
        int generation = 1;
        while (generation <= MAX_GENERATIONS) {
            // Evaluate fitness of population
            ArrayList<Double> fitness = new ArrayList<>();
            for (int[] chromosome : population) {
                fitness.add(getFitness(chromosome));
            }

            // Select parents for crossover
            ArrayList<int[]> parents = new ArrayList<>();
            for (int i = 0; i < POPULATION_SIZE / 2; i++) {
                int[] parent1 = tournamentSelection(population, fitness);
                int[] parent2 = tournamentSelection(population, fitness);
                parents.add(parent1);
                parents.add(parent2);
            }

            // Crossover and mutation
            ArrayList<int[]> offspring = new ArrayList<>();
            for (int i = 0; i < parents.size() - 1; i += 2) {
                int[] child1 = crossover(parents.get(i), parents.get(i + 1));
                int[] child2 = crossover(parents.get(i + 1), parents.get(i));
                mutate(child1);
                mutate(child2);
                offspring.add(child1);
                offspring.add(child2);
            }

            // Replace old population with offspring
            population = offspring;

            // Check if solution is found
             solution = getSolution(population);
            if (solution != null) {
                System.out.println("Solution found in generation " + generation + ": " + arrayToString(solution));
                

                

                break;
            }

            generation++;
        }

        if (generation > MAX_GENERATIONS) {
            System.out.println("No solution found");
        }
		
		
		return solution;
		
	}
	
	
	private static int[] generateChromosome(int n) {
        int[] chromosome = new int[n];
        for (int i = 0; i < n; i++) {
            chromosome[i] = random.nextInt(n);
        }
        return chromosome;
        }

    private static double getFitness(int[] chromosome) {
        int n = chromosome.length;
        AtomicInteger conflicts = new AtomicInteger(0); // Use AtomicInteger for thread safety
        
        // Parallelize calculation of conflicts
        IntStream.range(0, n)
                 .parallel()
                 .forEach(i -> {
                     Thread currentThread = Thread.currentThread();
                     long threadId = currentThread.getId();
                     
                     for (int j = i + 1; j < n; j++) {
                         if (chromosome[i] == chromosome[j] || Math.abs(chromosome[i] - chromosome[j]) == j - i) {
                             conflicts.incrementAndGet();
                             //System.out.println("Thread " + threadId + " detected conflict between index " + i + " and " + j);
                         }
                     }
                 });
                 
        return 1.0 / (conflicts.get() + 1);
    }



    private static int[] tournamentSelection(ArrayList<int[]> population, ArrayList<Double> fitness) {
        int[] bestChromosome = null;
        double bestFitness = 0;
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            int index = random.nextInt(population.size());
            double chromosomeFitness = fitness.get(index);
            if (bestChromosome == null || chromosomeFitness > bestFitness) {
                bestChromosome = population.get(index);
                bestFitness = chromosomeFitness;
            }
        }
        return bestChromosome;
    }

    private static int[] crossover(int[] parent1, int[] parent2) {
        int n = parent1.length;
        int[] child = new int[n];
        int crossoverPoint = random.nextInt(n);
        for (int i = 0; i < crossoverPoint; i++) {
            child[i] = parent1[i];
        }
        for (int i = crossoverPoint; i < n; i++) {
            child[i] = parent2[i];
        }
        return child;
    }

    private static void mutate(int[] chromosome) {
        IntStream.range(0, chromosome.length).parallel().forEach(i -> {
            Thread currentThread = Thread.currentThread();
            //System.out.println("Thread ID " + currentThread.getId() + " executing mutation at index " + i);
            if (random.nextDouble() < MUTATION_RATE) {
                chromosome[i] = random.nextInt(chromosome.length);
            }
        });
    }


    private static int[] getSolution(ArrayList<int[]> population) {
        for (int[] chromosome : population) {
            if (getFitness(chromosome) == 1.0) {
                return chromosome;
            }
        }
        return null;
    }

    static  String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i : array) {
            sb.append(i).append(" ");
        }
        return sb.toString();
    }

}
