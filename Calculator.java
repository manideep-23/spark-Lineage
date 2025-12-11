class Calculator {
    private int lastResult = 0;   // shared mutable state!

    public int add(int a, int b) {
        lastResult = a + b;       // writing to shared variable
        return lastResult;
    }

    public int getLastResult() {
        return lastResult;
    }
}

public class NotThreadSafeExample {
    public static void main(String[] args) throws Exception {

        Calculator calc = new Calculator(); // SHARED object

        Runnable task1 = () -> {
            calc.add(10, 20);  // expected 30
            System.out.println("Task1 result = " + calc.getLastResult());
        };

        Runnable task2 = () -> {
            calc.add(5, 7);    // expected 12
            System.out.println("Task2 result = " + calc.getLastResult());
        };

        // Run both tasks simultaneously (like Spark executors)
        new Thread(task1).start();
        new Thread(task2).start();
    }
}
