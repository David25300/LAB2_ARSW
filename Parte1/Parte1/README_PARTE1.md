# Laboratorio ARSW - PrimeFinder con wait() y notifyAll()
Santiago Cajamarca y Santiago Cajamarca

## 1. Descripcion

Este proyecto busca numeros primos entre **0 y 30.000.000** utilizando **tres hilos trabajadores**.

Cada trabajador procesa una parte diferente del rango. Mientras los trabajadores realizan la busqueda, un hilo de control coordina pausas periodicas. Cada t milisegundos, el programa:

1. Solicita la pausa de todos los trabajadores.
2. Espera hasta comprobar que todos estan pausados o ya terminaron.
3. Muestra cuantos numeros primos se han encontrado.
4. Espera que el usuario presione **ENTER**.
5. Reanuda todos los trabajadores que siguen activos.



### Main.java

Sus responsabilidades son:

- Solicitar el intervalo t en milisegundos.
- Validar que el valor sea un entero mayor que cero.
- Crear e iniciar el hilo Control.
- Esperar a que termine la ejecucion completa.

### Control.java

Administra la concurrencia. Sus responsabilidades son:

- Crear los tres trabajadores.
- Dividir el rango de numeros.
- Solicitar una pausa cada t milisegundos.
- Esperar hasta que todos los trabajadores esten detenidos.
- Mostrar el total acumulado de primos.
- Esperar ENTER.
- Reanudar los trabajadores con notifyAll().

### PrimeFinderThread.java

Representa un hilo trabajador. Sus responsabilidades son:

- Recorrer el rango asignado.
- Verificar si cada numero es primo.
- Guardar los primos encontrados.
- Consultar el estado de pausa en cada iteracion.
- Informar al controlador cuando termina.

## 2. Funcionamiento general

El rango se divide de esta manera:

Trabajador 1: 0 a 9.999.999

Trabajador 2: 10.000.000 a 19.999.999

Trabajador 3: 20.000.000 a 30.000.000

Cuando los tres trabajadores completan sus rangos, el programa muestra el total final y termina automaticamente.

## 3. Diseno de sincronizacion

### Monitor compartido

La sincronizacion utiliza un unico monitor declarado en Control:

```java
private final Object monitor = new Object();
```

Las variables que representan el estado de la coordinacion se consultan y modifican dentro de:

```java
synchronized (monitor) {
    // Lectura o cambio del estado compartido
}
```

wait() y notifyAll() tambien se invocan sobre este mismo objeto. Por lo tanto, la condicion, el lock y las notificaciones pertenecen al mismo monitor.

### Condicion de pausa

La condicion principal es:

```java
private boolean pauseRequested;
```

- Si vale false, los trabajadores pueden continuar.
- Si vale true, los trabajadores deben esperar.

Cada trabajador consulta la condicion antes de procesar el siguiente numero:

```java
control.awaitIfPaused();
```

La espera se realiza así:

```java
while (pauseRequested) {
    monitor.wait();
}
```

### Ausencia de espera activa

No se utiliza un ciclo que consulte continuamente una bandera. Cuando un trabajador debe detenerse, ejecuta wait().

wait() bloquea al trabajador y libera temporalmente el monitor. El hilo permanece dormido hasta que recibe una notificacion y puede comprobar nuevamente la condicion.

### Confirmacion de todos los trabajadores

El controlador no muestra el resultado inmediatamente despues de solicitar la pausa. Primero espera hasta que todos los trabajadores esten pausados o hayan terminado:

```java
while (pausedWorkers + finishedWorkers < workers.length) {
    monitor.wait();
}
```

Esto evita contar los primos mientras un trabajador todavia esta modificando su lista.

Un trabajador terminado se incluye en finishedWorkers porque ya no puede entrar en pausa ni agregar nuevos elementos.

### Reanudacion con ENTER

ENTER no finaliza el programa. ENTER autoriza la continuacion de la busqueda.

Despues de recibir ENTER, Control ejecuta:

```java
synchronized (monitor) {
    pauseRequested = false;
    monitor.notifyAll();
}
```

Primero se elimina la condicion de pausa y luego notifyAll() despierta a todos los trabajadores que esperan sobre el monitor.

Se utiliza notifyAll() porque existen varios trabajadores esperando. notify() podria despertar solamente a uno y dejar a los demas detenidos.

## 4. Prevencion de lost wakeups

La solucion no depende solamente de la notificacion. El estado real se conserva en pauseRequested.

La comprobacion de la condicion y la llamada a wait() se realizan dentro del mismo bloque synchronized. El cambio de la condicion y notifyAll() tambien se realizan con el mismo monitor.

Ademas, wait() esta protegido por un while y no por un if:

```java
while (pauseRequested) {
    monitor.wait();
}
```

Esto permite volver a comprobar la condicion despues de despertar. Si ocurre un despertar espurio o la pausa sigue activa, el trabajador vuelve a esperar. Si la condicion ya permite continuar, el trabajador no se bloquea de nuevo.

El controlador aplica el mismo patron cuando espera que todos los trabajadores se detengan.

