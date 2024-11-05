package nas;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

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
			try(ServerSocket ss=new ServerSocket(55555)) {
				while(!Thread.interrupted()) {
					try {
						Thread th=new Thread(new Task(ss.accept(),root));
						th.start();
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
	}
}
