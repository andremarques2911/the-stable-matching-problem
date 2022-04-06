import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class StableMatching {
    private final int POPULATION_SIZE = 11;
    private final int MUTATION_PERCENTAGE = 5;
    private final int MUTATION_AMOUNT = 3;

    public void execute() {
        List<String> file = readFile("./src/carga.txt");
        if (file == null) return;
        int numberOfStudents = getNumberOfStudents(file);
        List<int[]> charge = getCharge(file);
        int[][] population = generatePopulation(numberOfStudents);
        int[][] intermediary = new int[POPULATION_SIZE][numberOfStudents + 1];

        for (int g = 0; g < 1; g++) {
            System.out.println("Geracao:" + g);

            print(population, numberOfStudents + 1, "Inicio: ");

            aptitude(numberOfStudents, charge, population);
            print(population, numberOfStudents + 1, "Aptidao: ");

            int best = getBest(population, intermediary, numberOfStudents);
            print(population, numberOfStudents + 1, "Eletismo: ");

            if (foundSolution(best, population, numberOfStudents)) break;

            crossover(population, intermediary);
            population = intermediary;

            if (getNextInt(MUTATION_PERCENTAGE) == 0) {
                mutation(population, numberOfStudents);
            }
        }
    }

    private List<String> readFile(String path) {
        try {
            return Files.readAllLines(Path.of(path));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return null;
    }

    private int getNumberOfStudents(List<String> file) {
        return Integer.parseInt(file.get(0));
    }

    private List<int[]> getCharge(List<String> file) {
        file.remove(0);

        return file.stream()
                .map(line -> {
                    String[] preferences = line.split(" ");
                    return Arrays.stream(preferences).mapToInt(Integer::parseInt).toArray();
                })
                .collect(Collectors.toList());
    }

    private int[][] generatePopulation(int numberOfStudents) {
        int[][] population = new int[POPULATION_SIZE][numberOfStudents + 1];

        for (int i = 0; i < population.length; i++) {
            for (int j = 0; j < numberOfStudents; j++) {
                population[i][j] = getNextInt(numberOfStudents);
            }
        }

        return population;
    }

    private int getNextInt(int bound) {
        Random random = new Random();

        return random.nextInt(bound);
    }

    private void aptitude(int numberOfStudents, List<int[]> charge, int[][] population) {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            int matchSum = 0;
            for (int j = 0; j < numberOfStudents; j++) {
                for (int k = 1; k < numberOfStudents; k++) {
                    if (population[i][j] == charge.get(j)[k]) {
                        matchSum += k;
                        break;
                    }
                }

                for (int k = 1; k < numberOfStudents; k++) {
                    if (population[i][j] == charge.get(j + numberOfStudents)[k]) {
                        matchSum += k;
                        break;
                    }
                }
            }
            population[i][numberOfStudents] = matchSum;
        }
    }

    private int getBest(int[][] population, int[][] intermediary, int numberOfStudents) {
        int min = population[0][numberOfStudents];
        int best = 0;

        for (int i = 1; i < POPULATION_SIZE; i++) {
            if (population[i][numberOfStudents] < min) {
                min = population[i][numberOfStudents];
                best = i;
            }
        }

        for (int i = 0; i < numberOfStudents; i++) {
            intermediary[0][i] = population[best][i];
        }
        System.out.println("Melhor: " + best);

        return best;
    }

    private boolean foundSolution(int best, int[][] population, int numberOfStudents) {
        if (population[best][numberOfStudents] == 0) {
            System.out.println("\nAchou a solução ótima. Ela corresponde ao cromossomo: " + best);
            return true;
        }

        return false;
    }

    private void crossover(int[][] population, int[][] intermediary) {

    }

    public void mutation(int[][] population, int numberOfStudents) {
        int amount = getNextInt(MUTATION_AMOUNT) + 1;
        for (int i = 0; i < amount; i++) {
            int individual = getNextInt(POPULATION_SIZE);
            int position = getNextInt(numberOfStudents);

            System.out.println("Cromossomo " + individual + " sofreu mutação na carga de indice " + position);
            population[individual][position] = population[individual][position] == 0 ? 1 : 0;
        }

    }

    private void print(int[][] matrix, int numberOfColumns, String message) {
        int j = 0;
        System.out.println(message);
        for (int i = 0; i < POPULATION_SIZE; i++) {
            for (j = 0; j < numberOfColumns; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }

}