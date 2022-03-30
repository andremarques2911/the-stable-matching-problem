import java.util.Random;

public class StableMatching {
    //  1  2  3
    // [3, 1, 2]
    int[] carga = { 3, 1, 2};


    public static void main(String[] args) {
        int[][] populacao = geraPopuluacao();
    }

    public static int[][] geraPopuluacao() {
        int[][] populacao = new int[10][10];
        Random random = new Random();

        for (int i = 0; i < populacao.length; i++) {
            for (int j = 0; j < 20; j++) {
                populacao[i][j] = random.nextInt(2);
            }
        }
    }

}