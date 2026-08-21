/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;
import java.util.concurrent.BlockingQueue;
import java.util.Random;
/**
 *
 * @author hcadavid
 * @author Santiago Cajamarca
 * @author Sebastian Gonzalez
 */


public class Producer extends Thread {

    private final BlockingQueue<Integer> queue;
    private int dataSeed;
    private final Random rand;
    private final long stockLimit;

    public Producer(
            BlockingQueue<Integer> queue,
            long stockLimit
    ) {
        this.queue = queue;
        this.dataSeed = 0;
        this.rand = new Random(
                System.currentTimeMillis()
        );
        this.stockLimit = stockLimit;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                dataSeed = dataSeed + rand.nextInt(100);

                queue.put(dataSeed);

                System.out.println(
                        "Producer added " + dataSeed
                                + " | Stock: " + queue.size()
                                + "/" + stockLimit
                );

            } catch (InterruptedException ex) {
                interrupt();
            }
        }
    }
}