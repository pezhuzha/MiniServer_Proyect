package nas;

import java.io.*;
import java.net.Socket;

public class Task implements Runnable {
	private Socket s;
	private File root;

	Task(Socket soc, File ro) {
		this.s = soc;
		this.root = ro;
	}

	public void run() {
		String res=null;
		String path=null;
		File aux[]=null;
		File subdir=null;
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
						path=root.getAbsolutePath()+File.separator+res;
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
						path=root.getAbsolutePath()+File.separator+res;
						File comprobarRuta=new File(path);
						if(comprobarRuta.exists()) {//existe un archivo con el mismo nombre 
							bw.write("El archivo ya existe (Es necesario borralo primero)\n");
							bw.flush();
						}else{
							bw.write("OK\n");
							bw.flush();
							try(BufferedWriter archivorecibido=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(comprobarRuta),"UTF-8"))){
							while(br.ready()) {
								archivorecibido.write(br.readLine());
							}
							archivorecibido.flush();	
							bw.write("Archivo creado exitosamente\n");
							bw.flush();
							}
							catch(IOException e1) {
								e1.printStackTrace();
							}	
						}
						break;
					case 'r'://rm
						res=br.readLine();
						path=root.getAbsolutePath()+File.separator+res;
						
						File delete=new File(path);
						if(delete.exists()) {
							if(delete.delete()) {
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
						
						break;
					case 'l'://ls
						res=br.readLine();
						if(res.isBlank()) //caso root
						{
							aux=root.listFiles();
						}
						else //normal
						{
							path=root.getCanonicalPath()+File.separator+res;
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
					case 'c'://cd+ls
						do {
						res=br.readLine();
						if(subdir==null) {
						subdir=new File(root.getCanonicalPath());}
						if(!res.isBlank()) {
						path=subdir.getCanonicalPath()+File.separator+res;
						subdir=new File(path);//pseudoroot
						}
						if(!(subdir.getCanonicalPath()).contains(root.getCanonicalPath())) //no dejar que acceda a directorios que no sean subdirectorios de root
						{
							subdir=new File(root.getCanonicalPath());
							bw.write("No se puede acceder al directorio\n");
							bw.flush();
						}
						else //normal
						{
							if(subdir!=null && subdir.canRead() && subdir.isDirectory()) {
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
							bw.write(subdir.getCanonicalPath()+"\n");
							for(int i=0;i<aux.length;i++) {
								if(aux[i].isDirectory()) {
									bw.write(aux[i].getName()+File.separator+"\n");
								}
								else {
									bw.write(aux[i].getName()+"\n");
								}
							bw.flush();
							}
						}
						}while(br.readLine().equalsIgnoreCase("END"));
						break;
					}
				}
			}
			while(!s.isClosed());


		}
		catch(IOException e1) {
			e1.printStackTrace();
		}
		finally {
			try {
				s.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
