/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;
import java.util.concurrent.BlockingQueue;
/**
 *
 * @author hcadavid
 * @author Santiago Cajamarca
 * @author Sebastian Gonzalez
 */
public class Consumer extends Thread {

    private final BlockingQueue<Integer> queue;

    public Consumer(
            BlockingQueue<Integer> queue
    ) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                Thread.sleep(1000);

                Integer element = queue.take();

                System.out.println(
                        "Consumer consumes " + element
                                + " | Stock: " + queue.size()
                );

            } catch (InterruptedException ex) {
                interrupt();
            }
        }
    }
}