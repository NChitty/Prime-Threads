import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    public static final long MAX_NUMBER = 100000000;
    public static final int NUMBER_OF_THREADS = 8;

    public static AtomicLong number = new AtomicLong(3);
    public static AtomicLong sumOfPrimes = new AtomicLong();

    public static Semaphore primeList = new Semaphore(NUMBER_OF_THREADS, true);

    public static volatile List<Long> primes;


    public static void main(String[] args) {
        primes = new ArrayList<>();
        primes.add(2L);
        long start = System.currentTimeMillis();
        PrimeThread[] pthreads = new PrimeThread[NUMBER_OF_THREADS];
        for(int i = 0; i < NUMBER_OF_THREADS; i++) {
            pthreads[i] = new PrimeThread("Thread " + (i+1));
            pthreads[i].start();
        }
        for(PrimeThread t : pthreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        int noOfPrimes = primes.size();
        System.out.printf(
                "%fs\t%s\t%s\n",
                (end - start)/1000.0,
                DecimalFormat.getInstance().format(noOfPrimes),
                DecimalFormat.getInstance().format(sumOfPrimes.get())
        );
        primes.sort(Collections.reverseOrder());
        for(int i = 9; i >= 0; i--) {
            System.out.println(primes.get(i));
        }
    }

    static class PrimeThread extends Thread {

        private long number;

        public PrimeThread(String name) {
            super(name);
            this.number = Main.number.getAndAdd(2);
        }

        @Override
        public void run() {
            while(this.number < MAX_NUMBER) {
                boolean prime = true;
                try {
                    Main.primeList.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for(Long l : Main.primes) {
                    if(l > Math.ceil(Math.sqrt(this.number)))
                        break;
                    prime = this.number % l != 0;
                    if(!prime)
                        break;
                }
                Main.primeList.release();
                if(prime) {
                    try {
                        Main.primeList.acquire(NUMBER_OF_THREADS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Main.primes.add(this.number);
                    Main.sumOfPrimes.getAndAdd(this.number);
                    Main.primeList.release(NUMBER_OF_THREADS);
                }
                this.number = Main.number.getAndAdd(2);
            }
        }
    }

}
