import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class StableMatching {
    private final int POPULATION_SIZE = 11;
    private final double MUTATION_PERCENTAGE = 0.2;
    private final int MUTATION_AMOUNT = (int) Math.ceil(POPULATION_SIZE * MUTATION_PERCENTAGE);
    private final int MUTATION_EXECUTION_PERCENTAGE = 2;
    private final int MAX_REPETITIONS = 8;
    private int numberOfRepetitions = 0;

    /**
     * Executa a logica inteira do algoritmo genetico
     */
    public void execute() {
        List<String> file = readFile("./src/tests/carga.txt");
        if (file == null) return;
        int numberOfStudents = getNumberOfStudents(file);
        List<int[]> charge = getCharge(file);
        List<int[]> aPreferences = getPreferences(charge, 0, numberOfStudents);
        List<int[]> bPreferences = getPreferences(charge, numberOfStudents, numberOfStudents * 2);
        Integer[][] population = generatePopulation(numberOfStudents);
        Integer[][] intermediary = new Integer[POPULATION_SIZE][numberOfStudents + 1];
        int lastBest = 0;
        int best = 0;

        for (int g = 0; g < 200; g++) {
            System.out.println("=============================================================================");
            System.out.println("Geracao:" + g);

            print(population, numberOfStudents + 1, "Inicio: ");

            aptitude(population, aPreferences, bPreferences, charge, numberOfStudents);
            print(population, numberOfStudents + 1, "Aptidao: ");

            best = getBest(population, intermediary, numberOfStudents);
            print(intermediary, numberOfStudents + 1, "Eletismo: ");

            if (foundSolution(best, lastBest, g, population, numberOfStudents)) break;

            crossover(population, intermediary, numberOfStudents);
            population = intermediary;
            print(population, numberOfStudents + 1, "Crossover: ");

            if (getNextInt(MUTATION_EXECUTION_PERCENTAGE) == 0) {
                mutation(population, numberOfStudents);
                print(population, numberOfStudents + 1, "Mutacao: ");
            }
            System.out.println("=============================================================================");
            System.out.println("\n\n");
        }
    }

    /**
     * Le um arquivo de um diretorio
     * @param path diretorio do arquivo
     * @return lista com as linhas do arquivo
     */
    private List<String> readFile(String path) {
        try {
            return Files.readAllLines(Path.of(path));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return null;
    }

    /**
     * Busca o numero de estudantes na primeira linha do arquivo de entrada
     * @param file lista com as linhas do arquivo
     * @return numero de estudantes
     */
    private int getNumberOfStudents(List<String> file) {
        return Integer.parseInt(file.get(0));
    }

    /**
     * Monta uma lista contendo as preferencias dos alunos a partir do arquivo de entrada
     * @param file lista com as linhas do arquivo
     * @return lista com as preferencias dos alunos
     */
    private List<int[]> getCharge(List<String> file) {
        file.remove(0);

        return file.stream()
                .filter(line -> !line.isBlank())
                .map(line -> {
                    String[] preferences = line.split(" ");
                    return Arrays.stream(preferences).mapToInt(Integer::parseInt).toArray();
                })
                .collect(Collectors.toList());
    }

    private List<int[]> getPreferences(List<int[]> charge, int init, int numberOfStudents) {
        List<int[]> preferences = new ArrayList<>();
        for (int i = init; i < numberOfStudents; i++) {
            int[] arr = charge.get(i);
            int[] values = new int[arr.length - 1];
            int count = 1;
            for (int j = 0; j < arr.length - 1; j++) {
                values[j] = arr[count++] - 1;
            }
            preferences.add(values);
        }
        return preferences;
    }

    /**
     * Popula a matriz population com valores aleatorios
     * @param numberOfStudents numero de estudantes
     * @return retorna a matriz populada aleatoriamente
     */
    private Integer[][] generatePopulation(int numberOfStudents) {
        Integer[][] population = new Integer[POPULATION_SIZE][numberOfStudents + 1];

        for (int i = 0; i < population.length; i++) {
            List<Integer> randoms = new ArrayList<>();
            for (int j = 0; j < numberOfStudents; j++) {
                int random = 0;
                do {
                    random = getNextInt(numberOfStudents);
                } while (randoms.contains(random));
                randoms.add(random);
                population[i][j] = random;
            }
        }

        return population;
    }

    /**
     * Busca um valor aleatorio, que seja menor do que o parametro informado
     * @param bound limite superior da busca
     * @return numero aleatorio
     */
    private int getNextInt(int bound) {
        Random random = new Random();

        return random.nextInt(bound);
    }

    /**
     * Executa calculo de aptidao
     * @param population matriz contendo a populacao
     * @param charge lista com dados do arquivo de entrada
     * @param numberOfStudents numero de estudantes
     */
    private void aptitude(Integer[][] population, List<int[]> aPreferences, List<int[]> bPreferences, List<int[]> charge, int numberOfStudents) {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            int matchSum = 0;
            for (int j = 0; j < numberOfStudents; j++) {
                int preference = findPreference(aPreferences.get(j), population[i][j]);
                matchSum += preference;

                preference = findPreference(bPreferences.get(j), population[i][j]);
                matchSum += preference;
            }
            population[i][numberOfStudents] = matchSum;
        }
    }

    private int findPreference(int[] preferences, int e) {
        for (int i = 0; i < preferences.length; i++) {
            if (preferences[i] == e ) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Busca cromossomo com melhor aptidao e copia o mesmo para a primeira linha da matriz intermediaria
     * @param population matriz contendo a populacao
     * @param intermediary matriz intermediaria
     * @param numberOfStudents numero de estudantes
     * @return retorna posicao do melhor cromossomo encontrado
     */
    private int getBest(Integer[][] population, Integer[][] intermediary, int numberOfStudents) {
        int min = population[0][numberOfStudents];
        int best = 0;

        for (int i = 1; i < POPULATION_SIZE; i++) {
            if (population[i][numberOfStudents] < min) {
                min = population[i][numberOfStudents];
                best = i;
            }
        }

        for (int i = 0; i < numberOfStudents + 1; i++) {
            intermediary[0][i] = population[best][i];
        }
        System.out.println("Melhor: " + best);

        return best;
    }

    /**
     * Busca se a aptidao perfeita foi encontrada, no caso é quando chega a 0
     * @param best posicao do cromossomo que teve melhor aptidao no eletismo
     * @param population matriz contendo a populacao
     * @param numberOfStudents numero de estudantes
     * @return true se a aptidao for igual a 0 e false se nao for
     */
    private boolean foundSolution(int best, int lastBest, int g, Integer[][] population, int numberOfStudents) {
        if (best == lastBest) {
            numberOfRepetitions++;
            if (numberOfRepetitions == MAX_REPETITIONS) {
                System.out.println("Parada na geracao " + g + " por numero de repeticoes.");
                printSolution(best, population, numberOfStudents);
                return true;
            }
        } else {
            numberOfRepetitions = 0;
        }

        if(population[best][numberOfStudents] == 0) {
            System.out.println("\nAchou a solucao otima na geracao " + g + ". Ela corresponde ao cromossomo: " + best);
            printSolution(best, population, numberOfStudents);
            return true;
        }

        return false;
    }

    private void printSolution(int best, Integer[][] population, int numberOfStudents) {
        System.out.print("Solucao codificada: ");
        System.out.print("[");
        for (int i = 0; i <= numberOfStudents; i++) {
            if (i == numberOfStudents) {
                System.out.print(population[best][i] + "]");
                break;
            }
            System.out.print(population[best][i] + ", ");
        }
        System.out.print("\nSua aptidao eh: " + population[best][numberOfStudents]);
        System.out.println("\n");
        System.out.println("Solucao decodificada: ");
        for (int i = 0; i < numberOfStudents; i++){
            System.out.println("Aluno M" + (i + 1) + " com Aluno N" + (population[best][i] + 1));
        }
    }

    /**
     * Executa Crossover CX
     * @param population matriz contendo a populacao
     * @param intermediary matriz intermediaria que sera atualizada
     * @param numberOfStudents numero de estudantes
     */
    private void crossover(Integer[][] population, Integer[][] intermediary, int numberOfStudents) {
        for (int i = 1; i < POPULATION_SIZE; i += 2) {
            Integer[] chromosome1 = tournament(population, numberOfStudents);
            Integer[] chromosome2 = tournament(population, numberOfStudents);
            Integer[] child1 = new Integer[numberOfStudents + 1];
            Integer[] child2 = new Integer[numberOfStudents + 1];
            int last = chromosome2[0];
            child1[0] = chromosome1[0];
            child2[0] = chromosome2[0];

            while (!contains(child1, last)) {
                for (int j = 0; j < chromosome1.length - 1; j++) {
                    if (chromosome1[j] == last) {
                        child1[j] = chromosome1[j];
                        child2[j] = chromosome2[j];
                        last = chromosome2[j];
                        break;
                    }
                }
            }

            for (int j = 0; j < chromosome1.length - 1; j++) {
                if (child1[j] == null || child2[j] == null) {
                    child1[j] = chromosome2[j];
                    child2[j] = chromosome1[j];
                }
            }

            for (int j = 0; j < numberOfStudents; j++) {
                intermediary[i][j] = child1[j];
                if (i + 1 < POPULATION_SIZE)
                    intermediary[i + 1][j] = child2[j];
            }
        }
        for (int i = 0; i < POPULATION_SIZE; i++) {
            intermediary[i][numberOfStudents] = 0;
        }
    }

    /**
     * Verifica se um elemento existe no array
     * @param array array com dados
     * @param element elemento a ser buscado
     * @return true se encontrar o elemento e false se nao encontrar o elemento
     */
    private boolean contains(Integer[] array, int element) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null && array[i] == element) {
                return true;
            }
        }
        return false;
    }

    /**
     * Executa o torneio para obter o melhor cromossomo entre dois aleatorios
     * @param population matriz contendo a populacao
     * @param numberOfStudents numero de estudantes
     * @return cromossomo com a melhor aptidao
     */
    private Integer[] tournament(Integer[][] population, int numberOfStudents) {
        int chromosome1 = getNextInt(POPULATION_SIZE - 1);
        int chromosome2 = getNextInt(POPULATION_SIZE - 1);

        if (population[chromosome1][numberOfStudents] < population[chromosome2][numberOfStudents]) {
            return population[chromosome1];
        }
        return population[chromosome2];
    }

    /**
     * Executa a mutacao em um cromossomo aleatorio
     * @param population matriz contendo a populacao
     * @param numberOfStudents numero de estudantes
     */
    public void mutation(Integer[][] population, int numberOfStudents) {
        int amount = getNextInt(MUTATION_AMOUNT) + 1;
        for (int i = 0; i < amount; i++) {
            int individual = getNextInt(POPULATION_SIZE - 1);
            int position1 = 0;
            int position2 = 0;
            do {
               position1 = getNextInt(numberOfStudents);
               position2 = getNextInt(numberOfStudents);
            } while(position1 == position2 || position1 == 0 || position2 == 0);
            int aux = population[individual][position1];
            population[individual][position1] = population[individual][position2];
            population[individual][position2] = aux;
            System.out.println("Cromossomo " + individual + " sofreu mutação na carga de indice " + position1 + " para " + position2 );
        }
    }

    /**
     * Exibe conteudo de uma matriz
     * @param matrix matrix que sera visualizada
     * @param numberOfColumns numero de colunas da matriz
     * @param message mensagem exibida logo antes da exibicao da matriz
     */
    private void print(Integer[][] matrix, int numberOfColumns, String message) {
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