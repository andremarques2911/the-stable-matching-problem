import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class StableMatching {
    private final int POPULATION_SIZE = 19;
    private final int MUTATION_PERCENTAGE = 1;
    private final int MUTATION_AMOUNT = 10;

    /**
     * Executa a logica inteira do algoritmo genetico
     */
    public void execute() {
        List<String> file = readFile("./src/pares10.txt");
        if (file == null) return;
        int numberOfStudents = getNumberOfStudents(file);
        List<int[]> charge = getCharge(file);
        int[][] population = generatePopulation(numberOfStudents);
        int[][] intermediary = new int[POPULATION_SIZE][numberOfStudents + 1];

        for (int g = 0; g < 4; g++) {
            System.out.println("=============================================================================");
            System.out.println("Geracao:" + g);

            print(population, numberOfStudents + 1, "Inicio: ");

            aptitude(population, charge, numberOfStudents);
//            print(population, numberOfStudents + 1, "Aptidao: ");

            int best = getBest(population, intermediary, numberOfStudents);
//            print(population, numberOfStudents + 1, "Eletismo: ");

            if (foundSolution(best, population, numberOfStudents)) break;

            crossover(population, intermediary, numberOfStudents);
            population = intermediary;

            if (getNextInt(MUTATION_PERCENTAGE) == 0) {
                mutation(population, numberOfStudents);
//                print(population, numberOfStudents + 1, "Mutacao: ");
            }
            System.out.println("=============================================================================");
            System.out.println("\n\n\n");
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
                .map(line -> {
                    String[] preferences = line.split(" ");
                    return Arrays.stream(preferences).mapToInt(Integer::parseInt).toArray();
                })
                .collect(Collectors.toList());
    }

    /**
     * Popula a matriz population com valores aleatorios
     * @param numberOfStudents numero de estudantes
     * @return retorna a matriz populada aleatoriamente
     */
    private int[][] generatePopulation(int numberOfStudents) {
        int[][] population = new int[POPULATION_SIZE][numberOfStudents + 1];

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

    private List<Integer> generateCongruentLinear(double a, double c, double m, int x, int size) {
        List<Integer> randoms = new ArrayList<>();
        double previous = x;
        double xi;
        for (int i = 0; i < size; i++) {
            xi = ((a * previous) + c) % m;
            randoms.add((int) xi);
            previous = xi;
        }
        return randoms;
    }

    /**
     * Executa calculo de aptidao
     * @param population matriz contendo a populacao
     * @param charge lista com dados do arquivo de entrada
     * @param numberOfStudents numero de estudantes
     */
    private void aptitude(int[][] population, List<int[]> charge, int numberOfStudents) {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            int matchSum = 0;
            for (int j = 0; j < numberOfStudents; j++) {
                for (int k = 1; k < numberOfStudents; k++) {
                    if (population[i][j] + 1 == charge.get(j)[k]) {
                        matchSum += k;
                        break;
                    }
                }

                for (int k = 1; k < numberOfStudents; k++) {
                    if (population[i][j] + 1 == charge.get(j + numberOfStudents)[k]) {
                        matchSum += k;
                        break;
                    }
                }
            }
            population[i][numberOfStudents] = matchSum;
        }
    }

    /**
     * Busca cromossomo com melhor aptidao e copia o mesmo para a primeira linha da matriz intermediaria
     * @param population matriz contendo a populacao
     * @param intermediary matriz intermediaria
     * @param numberOfStudents numero de estudantes
     * @return retorna posicao do melhor cromossomo encontrado
     */
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

    /**
     * Busca se a aptidao perfeita foi encontrada, no caso é quando chega a 0
     * @param best posicao do cromossomo que teve melhor aptidao no eletismo
     * @param population matriz contendo a populacao
     * @param numberOfStudents numero de estudantes
     * @return true se a aptidao for igual a 0 e false se nao for
     */
    private boolean foundSolution(int best, int[][] population, int numberOfStudents) {
        if (population[best][numberOfStudents] == 0) {
            System.out.println("\nAchou a solução ótima. Ela corresponde ao cromossomo: " + best);
            return true;
        }

        return false;
    }

    /**
     * Executa Crossover CX
     * @param population matriz contendo a populacao
     * @param intermediary matriz intermediaria que sera atualizada
     * @param numberOfStudents numero de estudantes
     */
    private void crossover(int[][] population, int[][] intermediary, int numberOfStudents) {
        for (int i = 1; i < POPULATION_SIZE; i+=2) {
            int[] chromosome1 = tournament(population, numberOfStudents);
            int[] chromosome2 = tournament(population, numberOfStudents);
            Integer[] child1 = new Integer[numberOfStudents + 1];
            Integer[] child2 = new Integer[numberOfStudents + 1];
            int last = chromosome2[0];
            child1[0] = chromosome1[0];
            child2[0] = chromosome2[0];

            while (!contains(child1, last)) {
                for (int j = 0; j < chromosome1.length; j++) {
                    if (chromosome1[j] == last) {
                        child1[j] = chromosome1[j];
                        child2[j] = chromosome2[j];
                        last = chromosome2[j];
                        break;
                    }
                }
            }

            for (int j = 0; j < chromosome1.length; j++) {
                if (child1[j] == null && child2[j] == null) {
                    child1[j] = chromosome2[j];
                    child2[j] = chromosome1[j];
                }
            }

            for (int j = 0; j < numberOfStudents + 1; j++) {
                intermediary[i][j] = child1[j];
                intermediary[i + 1][j] = child2[j];
            }
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
    private int[] tournament(int[][] population, int numberOfStudents) {
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
    public void mutation(int[][] population, int numberOfStudents) {
        int amount = getNextInt(MUTATION_AMOUNT) + 1;
        for (int i = 0; i < amount; i++) {
            int individual = getNextInt(POPULATION_SIZE - 1);
            int position1 = 0;
            int position2 = 0;
            do {
               position1 = getNextInt(numberOfStudents);
               position2 = getNextInt(numberOfStudents);
            } while(position1 == position2);
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