Uso del programa

NAS_server 

  Necesita un argumento que es la ruta que se considera como su "/" donde se guarda todos los archivos. La ruta tiene que estar previamente creada
  
  Ej.
  
    cd / ; mkdir NAS ; ./NAS_server /NAS
  
  No se pueden acceder a directorios superiores que "/"

  Necesita hacer cambio a la linea 24 para poder especificar el puerto del servidor
  
  Se puede parar la ejecución escribiendo en la terminal
  
    INTERRUMPIR
    Confirmar

NAS_client

  No necesita de argumentos
  
  Necesita hacer cambio a la linea 14 para poder conectarse a la IP y puerto servidor
