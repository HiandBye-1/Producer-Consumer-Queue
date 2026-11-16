package src;
public class SmokeTest {
    public static void main(String[] args) throws Exception {
        Thread thread1 = new Thread(() -> {System.out.println("Hello from thread 1");});
        Thread thread2 = new Thread(() -> {System.out.println("Hello from thread 2");});
        
        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        System.out.println("All workers finished");
    }
}
