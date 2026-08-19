package edu.eci.arsw.primefinder;

import java.util.LinkedList;
import java.util.List;

public class PrimeFinderThread extends Thread {

    private final int start;
    private final int end;
    private final List<Integer> primes = new LinkedList<>();
    private final Control control;

    public PrimeFinderThread(int start, int end, Control control) {
        this.start = start;
        this.end = end;
        this.control = control;
    }

    @Override
    public void run() {
        try {
            for (int number = start; number < end; number++) {
                control.awaitIfPaused();

                if (isPrime(number)) {
                    primes.add(number);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            control.workerFinished();
        }
    }

    boolean isPrime(int number) {
        if (number < 2) {
            return false;
        }
        if (number == 2) {
            return true;
        }
        if (number % 2 == 0) {
            return false;
        }

        for (int divisor = 3; divisor <= number / divisor; divisor += 2) {
            if (number % divisor == 0) {
                return false;
            }
        }
        return true;
    }

    public int getPrimeCount() {
        return primes.size();
    }
}