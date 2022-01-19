import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    public static final long MAX_NUMBER = 100000000;
    public static final int NUMBER_OF_THREADS = 8;
    public static final int RANGE = (int) (MAX_NUMBER/NUMBER_OF_THREADS + .5);

    public static AtomicLong number = new AtomicLong(3);
    public static AtomicLong sumOfPrimes = new AtomicLong();

    public static volatile List<Long> composites;
    public static volatile List<Long> primes;
    public static BlockingQueue<Runnable> tasks;


    public static void main(String[] args) {
        composites = new ArrayList<>();
        primes = new ArrayList<>();
        primes.add(2l);
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        int noOfPrimes = primes.size();
        System.out.printf(
                "%fs\t%s\t%s\n",
                (end - start)/1000.0,
                DecimalFormat.getInstance().format(noOfPrimes),
                DecimalFormat.getInstance().format(sumOfPrimes.get())
        );
        Collections.sort(primes, Collections.reverseOrder());
        for(int i = 9; i >= 0; i--) {
            System.out.println(primes.get(i));
        }
    }

    static class PrimeThread extends Thread {
        @Override
        public void run() {
            super.run();
        }
    }

}
