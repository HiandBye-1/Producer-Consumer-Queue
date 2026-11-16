package src;

import java.util.concurrent.CountDownLatch;

import java.util.concurrent.locks.ReentrantLock;

public class RaceCondition{
    public static void main(String[] args) throws Exception {
        runRaceCondition();
        runMutex();
    }

    public static void runRaceCondition() throws Exception{
        Counter sharedCounter = new Counter();
        CountDownLatch latch = new CountDownLatch(2);

        Thread thread1 = new Thread(() -> { int counter = sharedCounter.counter.get();
                                             latch.countDown();

                                             try{
                                                 latch.await();
                                             }catch(Exception e){
                                                Thread.currentThread().interrupt();
                                                return;
                                             }

                                             sharedCounter.counter.set(counter + 1);
                                             
                                          });
        Thread thread2 = new Thread(() -> { int counter = sharedCounter.counter.get();
                                             latch.countDown();

                                             try{
                                                 latch.await();
                                             }catch(Exception e){
                                                Thread.currentThread().interrupt();
                                                return;
                                             }

                                             sharedCounter.counter.set(counter+1);
                                            

                                          });
        

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        System.out.println("The counter for Race Condition is: " + sharedCounter.counter.get());
    }

    public static void runMutex() throws Exception{
        ReentrantLock lock = new ReentrantLock();

        Counter sharedCounter = new Counter();

        Thread thread1 = new Thread(() -> {
            lock.lock();
            try{
                sharedCounter.counter.set(sharedCounter.counter.get() + 1);

            }finally{
                lock.unlock();
            }

        });


        Thread thread2 = new Thread(() -> {
            lock.lock();
            try{
                sharedCounter.counter.set(sharedCounter.counter.get() + 1);

            }finally{
                lock.unlock();
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        System.out.println("The counter for Mutex is: " + sharedCounter.counter.get());
    }
}