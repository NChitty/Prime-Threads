import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;

public class Main {

    public static final long MAX_NUMBER = 1000000;
    public static final int NUMBER_OF_THREADS = 8;

    /**
     * List of all the primes
     */
    public static List<Long> primes;

    public static long sumOfPrimes;

    public static volatile BitSet initialSieve = new BitSet((int) Math.sqrt(MAX_NUMBER));
    public static volatile BitSet rangeSieve = new BitSet((int) Math.sqrt(MAX_NUMBER));

    public static void main(String[] args) throws InterruptedException {
        primes = new ArrayList<>();

        initialSieve.set(0);
        initialSieve.set(1);
        initialSieve.set(4);
        initialSieve.set(6);
        initialSieve.set(8);
        initialSieve.set(9);
        initialSieve.set(10);
        initialSieve.set(12);
        initialSieve.set(14);
        initialSieve.set(15);
        initialSieve.set(16);
        initialSieve.set(18);
        initialSieve.set(20);
        initialSieve.set(21);
        rangeSieve.set(0);
        rangeSieve.set(1);
        rangeSieve.set(4);
        rangeSieve.set(6);
        rangeSieve.set(8);
        rangeSieve.set(9);
        rangeSieve.set(10);
        rangeSieve.set(12);
        rangeSieve.set(14);
        rangeSieve.set(15);
        rangeSieve.set(16);
        rangeSieve.set(18);
        rangeSieve.set(20);
        rangeSieve.set(21);

        long start = System.currentTimeMillis();
        long prime;
        long startRange = 0;
        long endRange = (long) Math.sqrt(MAX_NUMBER);
        for(int i = 0; i < Math.sqrt(MAX_NUMBER); i++) {
            int currentIndex = 0;
            int rangeIndex = 0;
            Thread[] threads = new Thread[NUMBER_OF_THREADS];
            while(currentIndex < Math.sqrt(endRange)) {
                for(int j = 0; j < NUMBER_OF_THREADS; j++) {
                    currentIndex = initialSieve.nextClearBit(currentIndex+1);
                    if(currentIndex > Math.sqrt(endRange)) break;
                    threads[j] = new Thread(new SieveRunnable(currentIndex, startRange, endRange));
                    threads[j].start();
                }
                for(int j = 0; j < NUMBER_OF_THREADS; j++) {
                    if(threads[j] != null)
                        threads[j].join();
                }
            }
            while(rangeIndex < endRange-startRange) {
                rangeIndex = rangeSieve.nextClearBit(rangeIndex);
                prime = rangeSieve.nextClearBit(rangeIndex) + startRange;
                if(prime < endRange) {
                    primes.add(prime);
                    sumOfPrimes += prime;
                } else
                    break;
                rangeIndex++;
            }
            rangeSieve.clear();
            startRange = endRange;
            endRange += Math.sqrt(MAX_NUMBER);
        }
        long end = System.currentTimeMillis();

        int noOfPrimes = primes.size();
        System.out.printf(
                "%dms\t%s\t%s\n",
                (end - start),
                DecimalFormat.getInstance().format(noOfPrimes),
                DecimalFormat.getInstance().format(sumOfPrimes)
        );

        primes.sort(Collections.reverseOrder());
        for(int i = 9; i >= 0; i--) {
            System.out.println(primes.get(i));
        }
    }

    static class SieveRunnable implements Runnable {

        private long base;
        private long start;
        private long end;

        public SieveRunnable(int currentIndex, long rangeStart, long rangeEnd) {
            this.base = currentIndex;
            this.start = rangeStart;
            this.end = rangeEnd;
        }

        @Override
        public void run() {
            // Gives the lowest multiple of base in the range
            long multiple = this.start == 0 ? this.base + this.base :
                    (this.start % this.base) == 0 ? this.start : (this.start - (this.start%this.base) + this.base);
            while(multiple <= end) {
                if(this.start == 0)
                    initialSieve.set((int) multiple, true);
                rangeSieve.set((int) (multiple - start), true);
                multiple += this.base;
            }
        }
    }


}
