Uso del programa.

NAS_server 
  Necesita un argumento que es la ruta que se considera como su "/" donde se guarda todos los archivos. Ej. cd / ; mkdir NAS ; "nombre del archivo compilado" /NAS
  No se pueden acceder a directorios superiores que "/"
  Recomendaciones: No poner un directorio donde que contenga nombres sensibles (Se envia la ruta completa para cd). Ej MALO. "nombre del archivo compilado" /home/nombrecompleto ; C:/Windows/Users/nombrecompleto
  Se puede cerrar escribiendo
    INTERRUMPIR
    Confirmar

NAS_client
  No necesita de argumentos
  Necesita hacer cambio a la linea 14 para poder conectarse al servidor
