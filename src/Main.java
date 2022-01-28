import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    public static final long MAX_NUMBER = 100000000;
    public static final int NUMBER_OF_THREADS = 8;

    /**
     * List of all the primes
     * Used for sorting and getting the largest 10
     * and finding the total number of primes
     */
    public static List<Long> primes;

    /**
     * I start the sum at 160 because
     * 2, 3, 5, 7, 11, ... 31 are all
     * seeded in the algorithm and their sum
     * is 160.
     */
    public static long sumOfPrimes = 160;

    /**
     * Tracks the current number the thread is on
     */
    public static AtomicLong number = new AtomicLong(33);

    public static void main(String[] args) throws InterruptedException {
        primes = new ArrayList<>();
        /**
         * These are the seeded values
         */
        primes.add(2L);
        primes.add(3L);
        primes.add(5L);
        primes.add(7L);
        primes.add(11L);
        primes.add(13L);
        primes.add(17L);
        primes.add(19L);
        primes.add(23L);
        primes.add(29L);
        primes.add(31L);

        long start = System.currentTimeMillis();
        WheelThread[] pthreads = new WheelThread[NUMBER_OF_THREADS];
        // Generate and start the threads
        for(int i = 0; i < NUMBER_OF_THREADS; i++) {
            pthreads[i] = new WheelThread();
            pthreads[i].start();
        }
        for (WheelThread p : pthreads) {
            // Join threads (wait for each one to finish)
            p.join();
        }
        long end = System.currentTimeMillis();
        // Each thread has it's own list of primes and it's own sum
        // So add that to the total thus "synchronizing"
        for (WheelThread p : pthreads) {
            primes.addAll(p.getPrimes());
            sumOfPrimes += p.getSumOfPrimes();
        }


        /**
         * Output
         */
        int noOfPrimes = primes.size();
        try {
            File primeFile = new File("primes.txt");
            primeFile.createNewFile();
            FileWriter writer = new FileWriter(primeFile);
            writer.write(String.format("%dms\t%s\t%s\n",
                    (end - start),
                    DecimalFormat.getInstance().format(noOfPrimes),
                    DecimalFormat.getInstance().format(sumOfPrimes)));
            primes.sort(Collections.reverseOrder());
            for(int i = 9; i >= 0; i--) {
                writer.write(String.format("%d\n", primes.get(i)));
            }
            writer.close();
        } catch (IOException e) {

        }


    }

    static class WheelThread extends Thread {

        // Each thread has its own instance of prime list and sumOfPrimes to avoid having to use locking features
        private List<Long> primes;
        private long sumOfPrimes;

        public WheelThread() {
            primes = new ArrayList<>();
        }

        @Override
        public void run() {
            // While the current number is less than the max number check if it is prime
            long number = Main.number.getAndIncrement();
            while(number <= MAX_NUMBER) {
                if(this.isPrime(number)) {
                    // add to list and sumOfPrimes
                    sumOfPrimes += number;
                    primes.add(number);
                }
                number = Main.number.getAndIncrement();
            }
        }

        /**
         * Uses a wheel sieve to check if a number is prime
         * @param N the number to check
         * @return True if N is prime and false if not
         */
        private boolean isPrime(long N) {
            boolean isPrime = true;

            /**
             * The LCM of the basis is 30
             * Thus all numbers not multiples of the basis
             * and less than 30 and end in 1, 3, and 7 are prime
             */
            int []arr = { 7, 11, 13, 17,19, 23, 29, 31 };

            // Base Case
            if (N < 2) {
                isPrime = false;
            }

            // Check the candidate number against the basis
            if (N % 2 == 0 || N % 3 == 0
                    || N % 5 == 0) {
                isPrime = false;
            }

            // Check the wheel, i represents which layer of the wheel we are on
            // 30 is chosen as it is the LCM of 2, 3, and 5
            for (int i = 0; i < Math.sqrt(N); i += 30) {

                // For each number in the wheel
                for (int c : arr) {

                    // No candidate prime can have a factor greater than it's square root
                    // as any such number would have a pair less than the square root
                    if ((c+i) > Math.sqrt(N)) {
                        break;
                    } else {
                        // The candidate number cannot be a multiple of any number on the wheel
                        if (N % (c + i) == 0) {
                            isPrime = false;
                            break;
                        }
                    }

                    // If we found N to be composite, no point continuing
                    if (!isPrime)
                        break;
                }
                // Break from this loop as well
                if(!isPrime)
                    break;
            }

            return isPrime;
        }

        public List<Long> getPrimes() {
            return primes;
        }

        public long getSumOfPrimes() {
            return sumOfPrimes;
        }
    }


}
