## Diseño específico
* Por diseño, el cliente siempre pide actualizaciones antes de mandar las suyas.
* Puerto 80000
## Especificación de protocolo
Notificar un cambio:
```
CLIENTE: PUSH <nombre_archivo> <fecha_modificación>
SERVIDOR: OK/ERROR <error>
```
Conseguir cambios:
```
CLIENTE: GET <nombre_archivo>
SERVIDOR: FILE <nombre_archivo> <fecha_modificación> <ip1>,<ip2>,...
SERVIDOR: DELETE <nombre_archivo> <fecha_modificación>
CLIENTE: OK/ERROR <error> #Notifica que ha acabado de actualizar el archivo
```
Conseguir todos los cambios a partir de una fecha determinada:
```
CLIENTE: GETALL <timestamp>
SERVIDOR: FILE <nombre_archivo> <fecha_modificación> <ip1>,<ip2>,...
          FILE <nombre_archivo2> <fecha_modificación2> <ip2_1>,<ip2_2>,...
          ...
          END
```

Notificar un borrado:
```
CLIENTE: DELETE <nombre_archivo> <fecha_modificación>
SERVIDOR: OK/ERROR <error>
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