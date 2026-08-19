package edu.eci.arsw.primefinder;

import java.util.Scanner;

public class Control extends Thread {

    private static final int NTHREADS = 3;
    private static final int MAX_VALUE = 30_000_000;

    private final Object monitor = new Object();
    private final PrimeFinderThread[] workers;
    private final long intervalMs;
    private final Scanner scanner;

    private boolean pauseRequested;
    private int pausedWorkers;
    private int finishedWorkers;

    private Control(long intervalMs, Scanner scanner) {
        if (intervalMs <= 0) {
            throw new IllegalArgumentException("t debe ser mayor que cro");
        }

        this.intervalMs = intervalMs;
        this.scanner = scanner;
        this.workers = new PrimeFinderThread[NTHREADS];

        int dataPerThread = MAX_VALUE / NTHREADS;
        for (int i = 0; i < NTHREADS; i++) {
            int start = i * dataPerThread;
            int end = (i == NTHREADS - 1) ? MAX_VALUE + 1 : (i + 1) * dataPerThread;
            workers[i] = new PrimeFinderThread(start, end, this);
        }
    }

    public static Control newControl(long intervalMs, Scanner scanner) {
        return new Control(intervalMs, scanner);
    }

    @Override
    public void run() {
        for (PrimeFinderThread worker : workers) {
            worker.start();
        }

        try {
            while (hasActiveWorkers()) {
                Thread.sleep(intervalMs);

                synchronized (monitor) {
                    if (finishedWorkers == workers.length) {
                        break;
                    }

                    pauseRequested = true;
                    monitor.notifyAll();

                    while (pausedWorkers + finishedWorkers < workers.length) {
                        monitor.wait();
                    }
                }

                System.out.println("\nTodos los hilos trabajadores estan pausados.");
                System.out.println("Primos encontrados: " + getTotalPrimes());
                System.out.print("Presione ENTER para reanudar...");
                scanner.nextLine();

                synchronized (monitor) {
                    pauseRequested = false;
                    monitor.notifyAll();
                }
            }

            for (PrimeFinderThread worker : workers) {
                worker.join();
            }

            System.out.println("\nBusqueda terminada.");
            System.out.println("Total de pirmos encontrados: " + getTotalPrimes());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            resumeWorkers();
        }
    }

    void awaitIfPaused() throws InterruptedException {
        synchronized (monitor) {
            boolean registeredAsPaused = false;

            try {
                while (pauseRequested) {
                    if (!registeredAsPaused) {
                        pausedWorkers++;
                        registeredAsPaused = true;
                        monitor.notifyAll();
                    }
                    monitor.wait();
                }
            } finally {
                if (registeredAsPaused) {
                    pausedWorkers--;
                    monitor.notifyAll();
                }
            }
        }
    }

    void workerFinished() {
        synchronized (monitor) {
            finishedWorkers++;
            monitor.notifyAll();
        }
    }

    private boolean hasActiveWorkers() {
        synchronized (monitor) {
            return finishedWorkers < workers.length;
        }
    }

    private int getTotalPrimes() {
        int total = 0;
        for (PrimeFinderThread worker : workers) {
            total += worker.getPrimeCount();
        }
        return total;
    }

    private void resumeWorkers() {
        synchronized (monitor) {
            pauseRequested = false;
            monitor.notifyAll();
        }
    }
}