package nas;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;

public class NAS_server {
	private static	File root=null;
	public static void main(String args[]) {
		if(args.length==1) {
			root=new File(args[0]);
			if(!root.isDirectory()) {
				System.out.println("No es un directorio");
				System.exit(-1);
			}
			if(!root.canRead() || !root.canWrite()) {
				System.out.println("No se puede leer o escribir en el directorio");
				System.exit(-1);
			}
			ConcurrentHashMap<File,String> chm=new ConcurrentHashMap<>();
			new Thread(new Simple_interrupt()).start();
			try(ServerSocket ss=new ServerSocket(55555)) {//Socket que usa el servidor
				ss.setSoTimeout(2000);//añadir timeout para poder cerrar el socket con un Thread.interrupt()
				while(!Thread.interrupted()) {
					try {
						Thread th=new Thread(new Task(ss.accept(),root,chm));
						th.start();
					}
					catch(SocketTimeoutException e) {//Te da una exception is no aceptas una conexion en 2 segundos ...
					}
					catch(IOException e1) {
						e1.printStackTrace();
					}
				}
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("El programa necesita un argumento que es el directorio donde trabaja");
		}
		Thread th[]=new Thread[Thread.activeCount()];
		Thread.enumerate(th);
		for(int i=0;i<th.length;i++) {
			th[i].interrupt();
		}
	}
}
