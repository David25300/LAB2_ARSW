## Escuela Colombiana de Ingeniería
### Arquitecturas de Software – ARSW

#### Santiago Cajamarca y Sebastian Gonzalez.

##### Parte I – Antes de terminar la clase.

Control de hilos con wait/notify. Productor/consumidor.

### 1. Revise el funcionamiento del programa y ejecútelo. Mientras esto ocurren, ejecute jVisualVM y revise el consumo de CPU del proceso correspondiente. A qué se debe este consumo?, cual es la clase responsable?

![Punto1_Parte2.png](../Imagenes/Punto1_Parte2.png)

El consumo elevado de CPU se debe a que el hilo consumidor realiza espera activa. Como el productor genera elementos lentamente y el consumidor trabaja rápidamente, la cola permanece vacía la mayor parte del tiempo. Sin embargo, el consumidor continúa ejecutando su ciclo while y consulta repetidamente si la cola tiene elementos. La clase responsable es Consumer, específicamente su método run(), porque el hilo permanece en estado ejecutable aunque no tenga elementos para consumir.

### 2. Haga los ajustes necesarios para que la solución use más eficientemente la CPU, teniendo en cuenta que -por ahora- la producción es lenta y el consumo es rápido. Verifique con JVisualVM que el consumo de CPU se reduzca.

Para reducir el consumo de CPU, se agregó un mecanismo de coordinación mediante wait() y notifyAll(). Cuando la cola está vacía, el consumidor ejecuta wait(), libera el monitor de la cola y queda en estado WAITING. Después de agregar un elemento, el productor ejecuta notifyAll() para despertar al consumidor. De esta manera se elimina la espera activa y el consumidor deja de usar CPU mientras no hay datos disponibles. Al revisar nuevamente el proceso con VisualVM, se observa una reducción del consumo de CPU.

![Punto2_Parte2.png](../Imagenes/Punto2_Parte2.png)

A diferencia de la imagne en el punto 1, el consumo de CPU es mucho menor (paso de 3.5% a 0.0%), porque el consumidor ya no ejecuta comprobaciones repetitivas mientras no hay elementos disponibles.

### 3. Haga que ahora el productor produzca muy rápido, y el consumidor consuma lento. Teniendo en cuenta que el productor conoce un límite de Stock (cuantos elementos debería tener, a lo sumo en la cola), haga que dicho límite se respete. Revise el API de la colección usada como cola para ver cómo garantizar que dicho límite no se supere. Verifique que, al poner un límite pequeño para el 'stock', no haya consumo alto de CPU ni errores.

Para invertir las velocidades, se eliminó la pausa del productor y se agregó una pausa de un segundo al consumidor. También se reemplazó la cola por una ArrayBlockingQueue cuya capacidad se establece mediante stockLimit. El productor agrega elementos mediante put(), operación que lo bloquea cuando la cola alcanza su capacidad máxima. El consumidor retira elementos mediante take(). De esta forma, el límite del stock nunca se supera, no se presentan excepciones por llenar la cola y tampoco existe espera activa. En VisualVM se puede observar que el productor queda bloqueado cuando la cola está llena y el consumidor permanece en espera temporizada durante su pausa, por lo que el consumo de CPU se mantiene bajo.

![Punto3_Parte2.png](../Imagenes/Punto3_Parte2.png)
