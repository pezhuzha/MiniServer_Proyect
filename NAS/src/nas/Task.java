package nas;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Task implements Runnable {
	private Socket s;
	private File root;
	private ConcurrentHashMap<File,Thread> conhashmap;

	Task(Socket soc, File ro,ConcurrentHashMap<File,Thread> hm) {
		this.s = soc;
		this.root = ro;
		this.conhashmap=hm;
	}

	public void run() {
		String res=null;
		String path=null;
		File aux[]=null;
		File subdir=null;
		File comprobarRuta=null;
		File pseudoroot=root;
		try(InputStream is=s.getInputStream();
				OutputStream os=s.getOutputStream();
				BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
				BufferedReader br=new BufferedReader(new InputStreamReader(is,"UTF-8")))
		{
			do {
				res=br.readLine();
				if (res!=null) {
					switch(res.charAt(0)) {
					case 'm'://mkdir
						res=br.readLine();
						path=pseudoroot.getAbsolutePath()+File.separator+res;
						if(new File(path).mkdirs()) {
							bw.write("El directorio se ha creado\n");
						}
						else {
							bw.write("Error, el directorio no se ha podido crear\n");
						}
						bw.flush();

						break;
					case 's'://sended to server
						res=br.readLine();
						path=pseudoroot.getAbsolutePath()+File.separator+res;
						comprobarRuta=new File(path);
						if(comprobarRuta.exists()) {//existe un archivo con el mismo nombre 
							bw.write("El archivo ya existe (Es necesario borralo primero)\n");
							bw.flush();
						}else{
							if(conhashmap.putIfAbsent(comprobarRuta, Thread.currentThread())!=Thread.currentThread()) {
								bw.write("No se puede borrar el archivo, otro usuario esta intentado borrar/modificar un archivo\n");
								bw.flush();
							}else {
								bw.write("OK\n");
								bw.flush();
								try(BufferedWriter archivorecibido=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(comprobarRuta),"UTF-8"))){
									while(br.ready()) {
										archivorecibido.write(br.readLine()+"\n");
									}
									archivorecibido.flush();

									conhashmap.remove(comprobarRuta, Thread.currentThread());
									bw.write("Archivo creado exitosamente\n");
									bw.flush();
								}
								catch(IOException e1) {
									e1.printStackTrace();
								}}
						}
						break;
					case 'r'://rm
						res=br.readLine();
						path=pseudoroot.getAbsolutePath()+File.separator+res;

						File delete=new File(path);


						if(delete.exists()) {
							if(conhashmap.putIfAbsent(delete, Thread.currentThread())!=Thread.currentThread()) {
								bw.write("No se puede borrar el archivo, otro usuario esta intentado borrar/modificar un archivo\n");
								bw.flush();
							}
							else if(delete.delete()) {
								bw.write("Archivo borrado correctamente\n");
								bw.flush();
							}else {
								bw.write("No se puede borrar el archivo\n");
								bw.flush();
							}
						}
						else {
							bw.write("No se puede borrar el archivo, no existe\n");
							bw.flush();
						}

						conhashmap.remove(delete, Thread.currentThread());
						break;
					case 'l'://ls
						res=br.readLine();
						if(res.isBlank()) //caso root
						{
							aux=pseudoroot.listFiles();
						}
						else //normal
						{
							path=pseudoroot.getCanonicalPath()+File.separator+res;
							subdir=new File(path);
							if(!(subdir.getCanonicalPath()).contains(root.getCanonicalPath())) //no dejar que acceda a directorios que no sean subdirectorios de root
							{
								bw.write("No se puede acceder al directorio\n");
								bw.flush();
							}
							else if(subdir!=null && subdir.canRead() && subdir.isDirectory()) {
								aux=subdir.listFiles();}
							else {//caso de leer algo que no es directorio o no se pude leer por permisos
								bw.write("No existe este directorio\n");
								bw.flush();
							}
						}
						if(aux!=null) {
							bw.write("OK\n");
							bw.flush();

							//listar todos los contenidos del directorio pedido
							for(int i=0;i<aux.length;i++) {
								if(aux[i].isDirectory()) {
									bw.write(aux[i].getName()+File.separator+"\n");
								}
								else {
									bw.write(aux[i].getName()+"\n");
								}
							}
							bw.flush();
						}
						break;
					case 'c'://cd
						res=br.readLine();
						if(!res.isBlank()) 
						{
							path=pseudoroot.getCanonicalPath()+File.separator+res;
							pseudoroot=new File(path);//pseudoroot
						}
						if(!pseudoroot.exists()) {
							pseudoroot=new File(root.getCanonicalPath());
							bw.write("No existe el directorio\n");
							bw.flush();
						}
						else if(!(pseudoroot.getCanonicalPath()).contains(root.getCanonicalPath())) //no dejar que acceda a directorios que no sean subdirectorios de root
						{
							pseudoroot=new File(root.getCanonicalPath());
							bw.write("No se puede acceder al directorio\n");
							bw.flush();
						}
						else {
							bw.write("OK\n");
							bw.flush();

							bw.write("Directorio actual: "+pseudoroot.getCanonicalPath()+"\n");
							bw.flush();
						}

						break;
					case 'g'://getfile from server
						res=br.readLine();
						path=pseudoroot.getAbsolutePath()+File.separator+res;
						comprobarRuta=new File(path);
						if(comprobarRuta.exists()) {
							bw.write("OK\n");
							bw.flush();
							try(BufferedReader archivoEnviar=new BufferedReader(new InputStreamReader(new FileInputStream(comprobarRuta),"UTF-8"))){
								while(archivoEnviar.ready()) {
									bw.write(archivoEnviar.readLine()+"\n");
								}
								bw.flush();
							}
							catch(IOException e1) {
								e1.printStackTrace();
							}	
						}else{
							bw.write("El archivo no existe\n");
							bw.flush();
						}
						break;
					}
				}

			}
			while(!s.isClosed());
			System.out.println(s.isClosed());

		}
		catch(IOException e1) {
			e1.printStackTrace();
		}
		finally {
			try {
				if(s!=null) {
					s.close();}
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
