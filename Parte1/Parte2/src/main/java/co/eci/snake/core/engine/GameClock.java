package co.eci.snake.core.engine;

import co.eci.snake.core.GameState;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class GameClock implements AutoCloseable {
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private final long periodMillis;
  private final Runnable tick;
  private final java.util.concurrent.atomic.AtomicReference<GameState> state = new AtomicReference<>(GameState.STOPPED);

  //para pausar y saber cuantas serpientes siguen vivas
  private int activeRunners = 0;
  private int parkedRunners = 0;

  public GameClock(long periodMillis, Runnable tick) {
    if (periodMillis <= 0) throw new IllegalArgumentException("periodMillis must be > 0");
    this.periodMillis = periodMillis;
    this.tick = java.util.Objects.requireNonNull(tick, "tick");
  }

  public synchronized void start() {
    if (state.compareAndSet(GameState.STOPPED, GameState.RUNNING)) {
      scheduler.scheduleAtFixedRate(() -> {
        if (state.get() == GameState.RUNNING) tick.run();
      }, 0, periodMillis, TimeUnit.MILLISECONDS);
      notifyAll();
    }
  }

  public synchronized void pause()  { state.set(GameState.PAUSED); }

  public synchronized void resume() {
    state.set(GameState.RUNNING);
    notifyAll(); //despierta a todas las serpientes
  }

  public GameState state() {return  state.get();}

  public void stop()   { state.set(GameState.STOPPED); }
  @Override public void close() { scheduler.shutdownNow(); }

  //cad snake se marca al arrancar y se quita al morir
  public synchronized void registerRunner()   { activeRunners++; }
  public synchronized void deregisterRunner() {
    activeRunners--;
    notifyAll();
  }

  //el hilo se bloquea hasta que se despause quitando el busy-wait que teniamos
  public void awaitRunning() throws InterruptedException {
    synchronized (this) {
      while (state.get() != GameState.RUNNING) {
        parkedRunners++;
        notifyAll(); // por si alguien está esperando en awaitAllPaused()
        try {
          wait();
        } finally {
          parkedRunners--;
        }
      }
    }
  }

  //espera hasta que todas las serpientes esten pausadas
  public synchronized void awaitAllPaused() throws InterruptedException {
    while (!(state.get() == GameState.PAUSED && parkedRunners >= activeRunners)) {
      wait();
    }
  }
}
