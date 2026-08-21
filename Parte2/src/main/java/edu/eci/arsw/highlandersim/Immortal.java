package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private int health;
    
    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private final Object myLock = new Object();

    private static final Object TIE_LOCK = new Object();

    private boolean paused = false;

    private boolean stopped = false;

    private boolean dead = false;

    private boolean terminated = false;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
    }

    public void run() {

        while (true) {

            synchronized (myLock) {
                while (paused && !terminated) {
                    stopped = true;
                    myLock.notifyAll();
                    try {
                        myLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                stopped = false;
                if (dead || terminated) {
                    return;
                }
            }

            Immortal im = pickOpponent();
            if (im == null) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            int thisHash = System.identityHashCode(this);
            int otherHash = System.identityHashCode(im);
            if (thisHash < otherHash) {
                synchronized (this.myLock) {
                    synchronized (im.myLock) {
                        if (!dead) {
                            this.fight(im);
                        }
                    }
                }
            } else if (thisHash > otherHash) {
                synchronized (im.myLock) {
                    synchronized (this.myLock) {
                        if (!dead) {
                            this.fight(im);
                        }
                    }
                }
            } else {
                synchronized (TIE_LOCK) {
                    synchronized (this.myLock) {
                        synchronized (im.myLock) {
                            if (!dead) {
                                this.fight(im);
                            }
                        }
                    }
                }
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    private Immortal pickOpponent() {
        int size = immortalsPopulation.size();
        if (size < 2) {
            return null;
        }
        int k = r.nextInt(size);
        Immortal chosen = (k < immortalsPopulation.size()) ? immortalsPopulation.get(k) : null;
        if (chosen == this) {
            k = (k + 1) % size;
            chosen = (k < immortalsPopulation.size()) ? immortalsPopulation.get(k) : null;
        }
        return chosen;
    }

    public void fight(Immortal i2) {

        if (i2.getHealth() > 0 && !i2.dead) {
            i2.changeHealth(i2.getHealth() - defaultDamageValue);
            this.health += defaultDamageValue;
            updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
            if (i2.getHealth() <= 0) {
                i2.kill();
                immortalsPopulation.remove(i2);
            }
        } else {
            updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
        }

    }

    private void kill() {
        synchronized (myLock) {
            dead = true;
            myLock.notifyAll();
        }
    }

    public void pause() {
        synchronized (myLock) {
            paused = true;
        }
    }

    public void awaitPause() {
        synchronized (myLock) {
            while (!(paused && stopped) && !dead && !terminated) {
                try {
                    myLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void terminate() {
        synchronized (myLock) {
            terminated = true;
            myLock.notifyAll();
        }
    }

    public void unpause() {
        synchronized (myLock) {
            paused = false;
            myLock.notifyAll();
        }
    }

    public int getHealth() {
        synchronized (myLock) {
            return health;
        }
    }

    public void changeHealth(int v) {
        synchronized (myLock) {
            health = v;
        }
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
