package org.example;

import java.util.*;

public class EightQueens {

    public static void main(String[] args) {
        System.out.println("Розв'язок LDFS:");
        Board randomBoardLDFS = generateRandomBoard();
        SearchMetrics metricsLDFS = new SearchMetrics();
        int depthLimit = 8;
        Board solutionLDFS = ldfs(randomBoardLDFS, depthLimit, metricsLDFS);
        if (solutionLDFS != null) {
            solutionLDFS.printBoard();
        } else {
            System.out.println("Розв'язок не знайдено для LDFS");
        }
        System.out.println("Кількість ітерацій (LDFS): " + metricsLDFS.iterations);
        System.out.println("Кількість згенерованих станів (LDFS): " + metricsLDFS.generatedStates);
        System.out.println("Кількість глухих кутів (LDFS): " + metricsLDFS.deadEnds);
        System.out.println("Кількість вузлів у пам'яті (LDFS): " + metricsLDFS.nodesInMemory);

        System.out.println("\nРозв'язок A*:");
        Board randomBoardAStar = generateRandomBoard();
        SearchMetrics metricsAStar = new SearchMetrics();
        Board solutionAStar = aStar(randomBoardAStar, metricsAStar);
        if (solutionAStar != null) {
            solutionAStar.printBoard();
        } else {
            System.out.println("Розв'язок не знайдено для A*");
        }
        System.out.println("Кількість ітерацій (A*): " + metricsAStar.iterations);
        System.out.println("Кількість згенерованих станів (A*): " + metricsAStar.generatedStates);
        System.out.println("Кількість глухих кутів (A*): " + metricsAStar.deadEnds);
        System.out.println("Кількість вузлів у пам'яті (A*): " + metricsAStar.nodesInMemory);
    }

    public static Board generateRandomBoard() {
        Board board = new Board();
        Random random = new Random();

        for (int row = 0; row < 8; row++) {
            int col = random.nextInt(8);
            board.placeQueen(row, col);
        }
        return board;
    }

    public static Board ldfs(Board board, int depthLimit, SearchMetrics metrics) {
        return ldfsHelper(board, 0, depthLimit, metrics, new HashSet<>());
    }

    private static Board ldfsHelper(Board board, int depth, int depthLimit, SearchMetrics metrics, Set<String> visited) {
        metrics.iterations++;
        metrics.nodesInMemory = Math.max(metrics.nodesInMemory, visited.size());

        if (board.isGoal()) {
            return board;
        }

        if (depth == depthLimit) {
            metrics.deadEnds++;
            return null;
        }

        String boardState = Arrays.toString(board.getQueens());
        if (visited.contains(boardState)) {
            return null;
        }
        visited.add(boardState);

        for (Board next : board.getNextStates()) {
            String nextState = Arrays.toString(next.getQueens());
            if (!visited.contains(nextState)) {
                metrics.generatedStates++;
                Board result = ldfsHelper(next, depth + 1, depthLimit, metrics, visited);
                if (result != null) {
                    return result;
                }
            }
        }

        visited.remove(boardState);
        return null;
    }

    public static Board aStar(Board board, SearchMetrics metrics) {
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(node -> node.priority));
        Map<String, Integer> visited = new HashMap<>();

        priorityQueue.add(new Node(board, 0, board.calculateConflicts()));
        visited.put(Arrays.toString(board.getQueens()), 0);
        metrics.nodesInMemory = Math.max(metrics.nodesInMemory, priorityQueue.size());

        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();
            Board currentBoard = currentNode.board;
            metrics.iterations++;

            if (currentBoard.isGoal()) {
                return currentBoard;
            }

            boolean hasNewState = false;

            for (Board next : currentBoard.getNextStates()) {
                metrics.generatedStates++;
                int g = currentNode.g + 1;
                int h = next.calculateConflicts();
                int f = g + h;
                String nextState = Arrays.toString(next.getQueens());

                if (!visited.containsKey(nextState) || visited.get(nextState) > g) {
                    priorityQueue.add(new Node(next, g, f));
                    visited.put(nextState, g);
                    metrics.nodesInMemory = Math.max(metrics.nodesInMemory, priorityQueue.size());
                    hasNewState = true;
                }
            }

            if (!hasNewState) {
                metrics.deadEnds++;
            }
        }
        return null;
    }

    private static class Node {
        Board board;
        int g;
        int priority;

        Node(Board board, int g, int priority) {
            this.board = board;
            this.g = g;
            this.priority = priority;
        }
    }

    static class Board {
        private int[] queens;

        public Board() {
            queens = new int[8];
            Arrays.fill(queens, -1);
        }

        public void placeQueen(int row, int col) {
            queens[row] = col;
        }

        public boolean isGoal() {
            return calculateConflicts() == 0;
        }

        public int[] getQueens() {
            return queens;
        }

        public int calculateConflicts() {
            int conflicts = 0;
            for (int i = 0; i < queens.length; i++) {
                if (queens[i] == -1) continue;
                for (int j = i + 1; j < queens.length; j++) {
                    if (queens[j] == -1) continue;
                    if (queens[i] == queens[j] || Math.abs(queens[i] - queens[j]) == Math.abs(i - j)) {
                        conflicts++;
                    }
                }
            }
            return conflicts;
        }

        public List<Board> getNextStates() {
            List<Board> nextStates = new ArrayList<>();
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (queens[row] != col) {
                        Board newBoard = new Board();
                        System.arraycopy(queens, 0, newBoard.queens, 0, 8);
                        newBoard.placeQueen(row, col);
                        nextStates.add(newBoard);
                    }
                }
            }
            return nextStates;
        }

        public void printBoard() {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (queens[i] == j) {
                        System.out.print("Q ");
                    } else {
                        System.out.print(". ");
                    }
                }
                System.out.println();
            }
        }
    }

    static class SearchMetrics {
        int iterations = 0;
        int generatedStates = 0;
        int deadEnds = 0;
        int nodesInMemory = 0;
    }
}
