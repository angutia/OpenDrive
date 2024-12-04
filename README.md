## Diseño específico
* Por diseño, el cliente siempre pide actualizaciones antes de mandar las suyas.
* Puerto servidor 8000
* Puerto cliente 6666
* El cliente guarda un historial de los archivos que tenía en la última actualización.
* Antes de cerrar el cliente, se actualiza una última vez.
## Especificación de protocolo cliente - servidor
Notificar un cambio:
```
CLIENTE: PUSH 
         <objeto de clase FileEvent>
SERVIDOR: OK/ERROR <error>
```
Conseguir cambios:
```
CLIENTE: GET <nombre_archivo>
SERVIDOR: OK/ERROR <error>
SERVIDOR: <objeto de clase FileEvent>
CLIENTE: OK/ERROR <error> #Notifica que ha acabado de actualizar el archivo
```
Conseguir la última actualización de todos los archivos:
```
CLIENTE: GETALL
SERVIDOR: MODIFICATION <fecha_modificación> <nombre_archivo>
          DELETION <fecha_modificación2> <nombre_archivo2>#Si el archivo ha sido borrado
          ...
          END
```
Notificar finalización de conexión:
````
CLIENTE: EXIT
SERVER: OK
````

## Especificación de protocolo cliente - cliente
Conseguir un archivo:
```
CLIENTE 1: GET <nombre_archivo>
CLIENTE 2: OK/ERROR <error> #En caso de no tener el archivo, por ejemplo
CLIENTE 2: <bytes_archivo>
```
