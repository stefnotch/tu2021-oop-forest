import forest2.AntBeetle;
import forest2.BarkBeetle;
import forest2.BugPopulation;
import forest2.*;

import java.util.Arrays;
import java.util.Random;

public class Test {

    public static void main(String[] args) {
        //createSimulation(10,5, 30);
        //createSimulation(20,10, 70);
        //createSimulation(30, 15, 150);
        for (int i = 0; i < 100; i++) {
            createSimulation(20, 20, 16);
            System.out.println("--------------------------------------------------------------------------------------");
        }
    }

    private static void createSimulation(int width, int height, int trees) {
        Forest forest = createForest(width, height, trees);
        forest.start();

        waitSimCompletion(forest);
        forest.print();
    }

    private static void waitSimCompletion(Forest forest) {
        while (forest.isRunning()) {
            try {
                Thread.sleep(120);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static final Random random = new Random();
    private static Forest createForest(int width, int height, int trees) {
        Forest.Cell[][] cells = new Forest.Cell[height][width];
        BugPopulation[][] bugs = new BugPopulation[height][width];
        for (Forest.Cell[] cell : cells) {
            Arrays.fill(cell, Forest.Cell.Tree);
        }


        for (int i = 0; i < trees; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);

            Vector2 coordinate = new Vector2(x, y);
            Forest.Cell tree = Forest.Cell.Tree;
            switch (random.nextInt(10)) {
                case 0:
                    bugs[coordinate.getY()][coordinate.getX()] = new BarkBeetle(0);
                    bugs[coordinate.getY()][coordinate.getX()].X = coordinate.getX();
                    bugs[coordinate.getY()][coordinate.getX()].Y = coordinate.getY();
                    break;
                case 1:
                    bugs[coordinate.getY()][coordinate.getX()] = new AntBeetle();
                    bugs[coordinate.getY()][coordinate.getX()].X = coordinate.getX();
                    bugs[coordinate.getY()][coordinate.getX()].Y = coordinate.getY();
                    break;
                default:
                    // no bug population
                    break;
            }
            cells[coordinate.getY()][coordinate.getX()] = tree;
        }
        cells[height/2][width/2] = Forest.Cell.Tree;
        bugs[height/2][width/2] = new BarkBeetle(0);
        bugs[height/2][width/2].X = width/2;
        bugs[height/2][width/2].Y = height/2;
        return new Forest(cells, bugs);
    }

}
