# Parallel Wheel Sieve

## Introduction

The problem was given to find all the prime numbers between 1 and 10<sup>8</sup> using 8 threads. The first thought is how can one test the primality of a number.
Two methods are of note, trial division and sieves.

## Properties of Prime Numbers

To understand the thought process behind different algorithms, some properties of primes are necessary to understand. The definition of a prime number is loose but
the one I chose for this project is that a prime number is indivisible by all prime numbers before it. Consequently, 0 and 1 are not prime and the first prime number is 2.
Another way to think of this definition is that a prime number is not a multiple of any other prime number, this fact proves useful when thinking about sieves. Another
point to make that is important for optimizing algorithms is that each for each pair of numbers in a factorization, one lies below the square root of the number and the other lies
above the square root.

## Trial Division

This seemed to be the easiest algorithm to implement while also the least efficient. I started by allowing each thread to just do trial division on all numbers greater than
or equal to 2 up to the square root of the candidate number, this executed in about 15 seconds. To keep track of the candidate number, each thread would `getAndIncrement` from an AtomicLong
which in Java implicitly implements a lock. I thought that this is a likely cause for slow down so I instead gave each thread a range which was `MAX_NUMBER/NUMBER_OF_THREADS`.
The issue with this is that all the threads are not doing an equal amount of work so I had to find another solution. I chose to keep the AtomicLong and instead tried to cutdown the number of
computations a thread would have to do by only testing if the candidate number was divisible by previous primes, playing on the idea that a prime number is not a multiple of previous primes.
This posed a concurrency issue, how do I iterate over a list that different threads are constantly adding numbers to? How can I synchronize the lists across the threads? I tried locks
and semaphores. My idea was that while a thread was adding a number to the list, it acquired all the permits to the list and while iterating over the list, the thread only acquired one permit.
While this solved my issue, it was slightly slower at 17 seconds at which point I needed to find a different solution.

## Sieves

When I first saw this problem I started doing research on Prime Generation[^1]. Sieves are a common topic because as previously stated, no prime can be a multiple of a previous prime.
Thus, if you keep track of all the multiples of primes, what's left are prime numbers. This is the entire idea of the sieve. An optimization is that the algorithm only needs to "sieve"
prime numbers up to the square root of the max number. This is because of the pair of factors rule mentioned earlier. Thus while all multiples up to the number must be found, only the multiples
of the primes less than the square root of the max number must be sieved completely.

### Sieve of Eratosthenes[^2]

The linear algorithm goes as follows:
```
n = nextPrime(numbers)
multiple = n*n
while multiple < target
    mark as composite in numbers, multiple)

nextPrime(list):
    while list.current is marked
        list.current = list.current.next
    return current
```
The immediate question is why does the multiple start at the square of the prime number? The answer is that this is the first unmarked multiple of the prime. For example, 2 marks 4, 6, 8, 10... thus 3 does not
need to mark 6 a second time, the first unmarked multiple is it's square, 9.

My idea for parallelizing this was to use a thread pool and have each thread sieve from the list of primes. The first issue that I ran into was that the threadpool has a finite space
to queue Java Runnables. Simple enough solution, once that barrier is reached, wait until we can queue more Runnables. The issue however was that the main thread that was producing runnables
ran faster and thus before the worker threads could mark an number as composite the producer thread would find it as prime. The solution I came up with was a barrier that prevent the producing thread
from marking primes before any other thread had the opportunity to mark a number greater than it as a multiple. At this point I was having some issues debugging as I was getting the wrong number
of primes still so I gave up on this algorithm.

### Wheel Sieve[^3]

My last shot was to implement a wheel sieve which is an optimization of the sieve of Eratosthenes. It works on a basis of primes numbers (2, 3, and 5) who have a least common multiple of 30. The remaining primes less than 37 are:
7, 11, 13, 17, 19, 23, 29, and 31, this forms the wheel. No prime is a multiple of the basis so the the algorithm checks these first. At which point the candidate is checked against the wheel and additions of 30.
This is because all additions of 30 to this wheel are prime as well, drastically cutting down the number of comparisons that need to be made. The explanation is best understood by looking at the picture below.

![Wheel sieve](../blob/main/Wheel Factorization.png)

While the code for this method was readily available I made several optimization based on my understanding of primes. First, the available algorithm checked if the current number on the wheel was less than the square root of the
candidate number. I changed the algorithm to check if the current number and its layer on the wheel was less than the square root to exit early, saving time. I also added an exit from the outer loop because once a number is found to be
composite, there is no need to continuing executing. It was at this point I stopped trying to optimize the algorithm and instead turned to optimizing the parallelization aspect of the project.

## Parallelization Optimizations

I wanted to have as little blocking for each thread so I gave each thread it's own list of primes to append to and it's own sum. I kept the atomic long from the trial division attempt
as it was the only way I could think to keep track of what the next candidate number should be. I was able to get to an execution time of a little over 3.5 seconds. While this time will change
from machine to machine this is a 4-fold improvement over trial division. While unimplemented, further optimization may be finding better ways to manage the current candidate number, and using different
collections that can insert faster such as a LinkedList.

## Compiling and Running

Ensure that the Java Development Kit (found [here](https://www.oracle.com/java/technologies/downloads)) and Java Runtime Environment (and [here](https://www.java.com/en/download/manual.jsp)) are installed.
After, download the file Main.java and manifest.txt. Run the following commands sequentially in a command-line interface in the directory Main.java is in:
```
javac -d "Main.java"
jar -cvfm Primes.jar manifest.txt *.class
```
The project is now built. The produced jar-file can now be moved anywhere and ran with `java -jar Primes.jar`.

[^1]:[Generation of primes](https://en.wikipedia.org/wiki/Generation_of_primes)
[^2]:[Sieve of Eratosthenes](https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes)
[^3]:[Wheel Factorization Method](https://www.geeksforgeeks.org/wheel-factorization-algorithm/#:~:text=Wheel%20Factorization%20is%20the%20improvement,all%20numbers%20of%20the%20basis.)
