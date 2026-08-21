/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class StartProduction {

    public static void main(String[] args) {

        int stockLimit = 3;

        BlockingQueue<Integer> queue =
                new ArrayBlockingQueue<>(stockLimit);

        Producer producer =
                new Producer(queue, stockLimit);

        Consumer consumer =
                new Consumer(queue);

        producer.start();
        consumer.start();
    }
}