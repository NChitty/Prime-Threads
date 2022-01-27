import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    public static final long MAX_NUMBER = 100000000;
    public static final int NUMBER_OF_THREADS = 8;

    /**
     * List of all the primes
     */
    public static List<Long> primes;

    public static long sumOfPrimes = 160;

    public static AtomicLong number = new AtomicLong(33);

    public static void main(String[] args) throws InterruptedException {
        primes = new ArrayList<>();
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
        for(int i = 0; i < NUMBER_OF_THREADS; i++) {
            pthreads[i] = new WheelThread();
            pthreads[i].start();
        }
        for (WheelThread p : pthreads) {
            p.join();
        }
        long end = System.currentTimeMillis();
        for (WheelThread p : pthreads) {
            primes.addAll(p.getPrimes());
            sumOfPrimes += p.getSumOfPrimes();
        }
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

    static class WheelThread extends Thread {

        private List<Long> primes;
        private long sumOfPrimes;

        public WheelThread() {
            primes = new ArrayList<>();
        }

        @Override
        public void run() {
            long number = Main.number.getAndIncrement();
            while(number <= MAX_NUMBER) {
                if(this.isPrime(number)) {
                    sumOfPrimes += number;
                    primes.add(number);
                }
                number = Main.number.getAndIncrement();
            }
        }

        private boolean isPrime(long N) {
            boolean isPrime = true;

            // The Wheel for checking
            // prime number
            int []arr = { 7, 11, 13, 17,19, 23, 29, 31 };

            // Base Case
            if (N < 2) {
                isPrime = false;
            }

            // Check for the number taken
            // as basis
            if (N % 2 == 0 || N % 3 == 0
                    || N % 5 == 0) {
                isPrime = false;
            }

            // Check for Wheel
            // Here i, acts as the layer
            // of the wheel
            for (int i = 0; i < Math.sqrt(N); i += 30) {

                // Check for the list of
                // Sieve in arr[]
                for (int c : arr) {

                    // If number is greater
                    // than sqrt(N) break
                    if ((c+i) > Math.sqrt(N)) {
                        break;
                    }

                    // Check if N is a multiple
                    // of prime number in the
                    // wheel
                    else {
                        if (N % (c + i) == 0) {
                            isPrime = false;
                            break;
                        }
                    }

                    // If at any iteration
                    // isPrime is false,
                    // break from the loop
                    if (!isPrime)
                        break;
                }
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
