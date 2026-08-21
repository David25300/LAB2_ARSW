package co.eci.snake.core;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Snake {
  private final Deque<Position> body = new ArrayDeque<>();
  private volatile Direction direction;
  private int maxLength = 5;
  //agregamos si esta viva y si esta muerta
  private volatile boolean alive = true;
  private volatile long diedAtNanos = -1;

  private Snake(Position start, Direction dir) {
    body.addFirst(start);
    this.direction = dir;
  }

  public static Snake of(int x, int y, Direction dir) {
    return new Snake(new Position(x, y), dir);
  }

  public Direction direction() { return direction; }

  public void turn(Direction dir) {
    if ((direction == Direction.UP && dir == Direction.DOWN) ||
        (direction == Direction.DOWN && dir == Direction.UP) ||
        (direction == Direction.LEFT && dir == Direction.RIGHT) ||
        (direction == Direction.RIGHT && dir == Direction.LEFT)) {
      return;
    }
    this.direction = dir;
  }

  public synchronized Position head() { return body.peekFirst(); }

  public synchronized Deque<Position> snapshot() { return new ArrayDeque<>(body); }

  //una copia del cuerpo de la serpiente
  public synchronized int length() { return body.size(); }

  public synchronized void advance(Position newHead, boolean grow) {
    body.addFirst(newHead);
    if (grow) maxLength++;
    while (body.size() > maxLength) body.removeLast();
  }

  //verifica si la serpiente chocara con la exepcion de que si come y ya no puede crecer la cola ya no esta por ende la cabeza puede ocupar ese lugar

  public synchronized boolean collidesWithSelf(Position next, boolean growing) {
    if (!growing && body.size() > 1 && next.equals(body.peekLast())) {
      return false;
    }
    return body.contains(next);
  }

  public synchronized boolean containsPosition(Position pos) {
    return body.contains(pos);
  }

  public boolean isAlive() {return alive;}

  public long diedAtNanos() {return diedAtNanos; }

  // Muestra la serpiente muerta
  public void die(){
    if (alive) {
      alive = false;
      diedAtNanos = System.nanoTime();
    }
  }

}
