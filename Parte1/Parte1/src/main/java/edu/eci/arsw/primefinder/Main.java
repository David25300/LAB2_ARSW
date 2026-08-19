package edu.eci.arsw.primefinder;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            long intervalMs = readInterval(scanner);

            Control control = Control.newControl(intervalMs, scanner);
            control.start();
            control.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("El hilo principal fue interrumpido");
        }
    }

    private static long readInterval(Scanner scanner) {
        while (true) {
            System.out.print("Ingrese el intervlo t en milisegundos: ");
            String input = scanner.nextLine().trim();

            try {
                long intervalMs = Long.parseLong(input);
                if (intervalMs > 0) {
                    return intervalMs;
                }
            } catch (NumberFormatException ignored) {

            }

            System.out.println("El intervalo debe ser un numero entero mayor que cero");
        }
    }
}