import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    public static final long MAX_NUMBER = 100000000;
    public static final int NUMBER_OF_THREADS = 8;
    public static final long RANGE = (long) ((MAX_NUMBER-2)/8 + .5);

    public static AtomicLong sumOfPrimes = new AtomicLong();

    public static volatile List<Long> primes;


    public static void main(String[] args) {
        primes = new ArrayList<>();
        long start = System.currentTimeMillis();
        PrimeThread[] pthreads = new PrimeThread[NUMBER_OF_THREADS];
        long startRange = 2;
        long endRange = 2 + RANGE;
        for(int i = 0; i < NUMBER_OF_THREADS; i++) {
            pthreads[i] = new PrimeThread("Thread " + (i+1), startRange, endRange);
            pthreads[i].start();
            startRange = endRange;
            endRange += RANGE;
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
        private long end;

        public PrimeThread(String name, long startRange, long endRange) {
            super(name);
            number = startRange;
            end = endRange;
        }

        @Override
        public void run() {
            for(; this.number < this.end; this.number++) {
                long test = 2;
                boolean prime = true;
                while(test <= Math.ceil(Math.sqrt(this.number))) {
                    prime = (Math.floorMod(number, test) != 0);
                    if(!prime)
                        break;
                    test++;
                }
                if(prime) {
                    Main.primes.add(this.number);
                    Main.sumOfPrimes.getAndAdd(this.number);
                }
            }
        }
    }

}
