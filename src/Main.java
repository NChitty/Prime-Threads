import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    public static final long MAX_NUMBER = 100000000;
    public static final int NUMBER_OF_THREADS = 8;
    public static final long RANGE = (long) ((MAX_NUMBER-2)/8 + .5);

    public static AtomicLong number = new AtomicLong(2);
    public static long sumOfPrimes = 0;

    public static List<Long> primes;


    public static void main(String[] args) {
        primes = new ArrayList<>();
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
        for(PrimeThread pt : pthreads) {
            primes.addAll(pt.getPrimes());
            sumOfPrimes += pt.getSum();
        }
        int noOfPrimes = primes.size();
        System.out.printf(
                "%.3fs\t%s\t%s\n",
                (end - start)/1000.0,
                DecimalFormat.getInstance().format(noOfPrimes),
                DecimalFormat.getInstance().format(sumOfPrimes)
        );
        primes.sort(Collections.reverseOrder());
        for(int i = 9; i >= 0; i--) {
            System.out.println(primes.get(i));
        }
    }

    static class PrimeThread extends Thread {

        private long number;
        private List<Long> primes;
        private long sumOfPrimes = 0;

        public PrimeThread(String name) {
            super(name);
            primes = new ArrayList<>();
        }

        @Override
        public void run() {
            number = Main.number.getAndIncrement();
            while(number < MAX_NUMBER) {
                long test = 2;
                boolean prime = true;
                while(test <= (long) (Math.sqrt(this.number) + .5)) {
                    prime = number % test != 0;
                    if(!prime)
                        break;
                    test++;
                }
                if(prime) {
                    this.primes.add(this.number);
                    sumOfPrimes += this.number;
                }
                number = Main.number.getAndIncrement();
            }
        }

        public List<Long> getPrimes() {
            return primes;
        }

        public long getSum() {
            return sumOfPrimes;
        }

    }

}
