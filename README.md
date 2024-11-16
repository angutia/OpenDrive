## Diseño específico
* Por diseño, el cliente siempre pide actualizaciones antes de mandar las suyas.
* Puerto 80000
## Especificación de protocolo
Notificar un cambio:
```
CLIENTE: PUSH 
         <objeto de clase FileEvent>
SERVIDOR: OK/ERROR <error>
```
Conseguir cambios:
```
CLIENTE: GET <nombre_archivo>
SERVIDOR: <objeto de clase FileEvent>
CLIENTE: OK/ERROR <error> #Notifica que ha acabado de actualizar el archivo
```
Conseguir la última actualización de todos los archivos:
```
CLIENTE: GETALL
SERVIDOR: MODIFICATION <nombre_archivo> <fecha_modificación>
          DELETION <nombre_archivo2> <fecha_modificación2> #Si el archivo ha sido borrado
          ...
          END
```

Notificar un renombrado:
```
CLIENTE: RENAME <nombre_viejo> <nombre_nuevo> <fecha_modificación>
SERVIDOR: OK/ERROR <error>
```
Notificar finalización de conexión:
````
CLIENTE: EXIT
SERVER: OK
````