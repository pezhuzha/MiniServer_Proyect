package nas;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Task implements Runnable {
	private Socket s;

	private File root;

	//conhashmap: File es el archivo que no quieres que se borre y String puede ser el nombre del Thread actual que esta borrando el archivo/directorio o 
	//la variable READ que lo usas para marcar que alguno de los hilos lo esta usando y no quiere que le borres el archivo/directorio
	private ConcurrentHashMap<File,String> conhashmap;// se usa para intentar conseguir integridad en los archivos

	private final String READ="/*/x-.";//se usa para poder denegar la eliminacion de un archivo o directorio

	Task(Socket soc, File ro,ConcurrentHashMap<File,String> hm) {
		this.s = soc;
		this.root = ro;
		this.conhashmap=hm;
	}

	public void run() {
		String res=null;//recoger las respuestas que le envia el cliente
		String path=null;
		File aux[]=null;//listar directorios
		File subdir=null;
		File comprobarRuta=null;
		File pseudoroot=root;
		try(InputStream is=s.getInputStream();
				OutputStream os=s.getOutputStream();
				BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
				BufferedReader br=new BufferedReader(new InputStreamReader(is,"UTF-8")))
		{	
			s.setSoTimeout(600000);//Si te conectas y no excribes en 10 min te cierra la conexion
			do {
				res=br.readLine();
				if (res!=null) {
					switch(res) {

					case "cd"://cd
						res=br.readLine();
						if(!res.isBlank()) //puedes hacer esto porque pseudoroot=root
						{
							path=pseudoroot.getCanonicalPath()+File.separator+res;
							pseudoroot=new File(path);
						}
						if(!pseudoroot.exists()) {
							bw.write("No existe el directorio "+pseudoroot.getName()+"\n");
							bw.flush();
							pseudoroot=new File(root.getCanonicalPath());
						}
						else if(!(pseudoroot.getCanonicalPath()).contains(root.getCanonicalPath())) //no dejar que acceda a directorios que no sean subdirectorios de root
						{
							bw.write("No se puede acceder al directorio "+pseudoroot.getName()+"\n");
							bw.flush();
							pseudoroot=new File(root.getCanonicalPath());
						}
						else {
							bw.write("OK\n");
							bw.flush();

							bw.write("Directorio actual: "+pseudoroot.getCanonicalPath()+"\n");
							bw.flush();
						}
						break;
					case "ls"://ls
						res=br.readLine();
						if(res.isBlank()) //caso root
						{
							subdir=pseudoroot;
						}else {
							path=pseudoroot.getCanonicalPath()+File.separator+res;
							subdir=new File(path);}
						if(!(subdir.getCanonicalPath()).contains(root.getCanonicalPath())) //no dejar que acceda a directorios que no sean subdirectorios de root
						{
							bw.write("No se puede acceder al directorio "+subdir.getName()+"\n");
							bw.flush();
						}
						else if(subdir!=null && subdir.canRead() && subdir.isDirectory()) {
							aux=subdir.listFiles();
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
						}
						else {//caso de leer algo que no es directorio o no se pude leer por permisos
							bw.write("No existe este directorio "+subdir.getName()+"\n");
							bw.flush();
						}
						break;
					case "mkdir"://mkdir
						res=br.readLine();
						path=pseudoroot.getAbsolutePath()+File.separator+res;
						if(new File(path).mkdirs()) {
							bw.write("El directorio "+res+" se ha creado\n");
						}
						else {
							bw.write("Error, el directorio "+res+" no se ha podido crear\n");
						}
						bw.flush();

						break;
					case "rm"://rm
						res=br.readLine();
						path=pseudoroot.getAbsolutePath()+File.separator+res;

						File delete=new File(path);


						if(delete.exists()) {
							if(conhashmap.putIfAbsent(delete, Thread.currentThread().toString())!=null) {
								bw.write("No se puede borrar el archivo "+delete.getName()+", otro usuario esta intentado borrar/enviar el archivo\n");
								bw.flush();
							}
							else if(delete.delete()) {
								bw.write("Archivo "+delete.getName()+" borrado correctamente\n");
								bw.flush();
							}else {
								bw.write("No se puede borrar el archivo "+delete.getName()+"\n");
								bw.flush();
							}
						}
						else {
							bw.write("No se puede borrar el archivo "+delete.getName()+", no existe\n");
							bw.flush();
						}

						conhashmap.remove(delete, Thread.currentThread().toString());
						break;
					case "send"://sended to server
						res=br.readLine();
						path=pseudoroot.getAbsolutePath()+File.separator+res;
						comprobarRuta=new File(path);
						if(comprobarRuta.exists()) {//existe un archivo con el mismo nombre 
							bw.write("El archivo "+res+" ya existe (Es necesario borralo primero)\n");
							bw.flush();
						}else{
							if(dontdelete(comprobarRuta.getParentFile())) {//metodo auxiliar para guardar en el hashmap
								bw.write("OK\n");
								bw.flush();
								try(BufferedWriter archivorecibido=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(comprobarRuta),"UTF-8"))){
									do {
										archivorecibido.write(br.readLine()+"\n");
									}while(br.ready());
									archivorecibido.flush();
									bw.write("Archivo "+comprobarRuta.getName()+" creado exitosamente\n");
									bw.flush();
								}
								catch(IOException e1) {
									e1.printStackTrace();
								}
								candelete(comprobarRuta.getParentFile());
							}
							else {
								bw.write("El directorio al que se envia esta siendo borrado\n");
								bw.flush();
							}
						}
						break;
					case "recive"://getfile from server
						res=br.readLine();
						path=pseudoroot.getAbsolutePath()+File.separator+res;
						comprobarRuta=new File(path);
						if(comprobarRuta.exists()) {
							if(dontdelete(comprobarRuta)) {
								bw.write("OK\n");
								try(BufferedReader archivoEnviar=new BufferedReader(new InputStreamReader(new FileInputStream(comprobarRuta),"UTF-8"))){
									 do{
										bw.write(archivoEnviar.readLine()+"\n");
									}while(archivoEnviar.ready());
									bw.flush();
								}
								catch(IOException e1) {
									e1.printStackTrace();
								}
								candelete(comprobarRuta);
							}
							else {
								bw.write("El archivo "+comprobarRuta.getName()+" esta siendo borrado\n");
								bw.flush();
							}
						}else{
							bw.write("El archivo "+comprobarRuta.getName()+" no existe\n");
							bw.flush();
						}
						break;
					}
				}

			}
			while(!s.isClosed() && !Thread.interrupted());
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
	public boolean dontdelete(File aux) {//en el hashmap impides que otros borren el archivo/directorio, no para remove
		String uso;
		synchronized(conhashmap){
			if((uso=conhashmap.get(aux))==null || uso.contains(READ)) {
				conhashmap.put(aux,conhashmap.get(aux) +READ);
			}
		}
		return conhashmap.get(aux).contains(READ);
	} 
	public void candelete(File aux) {//quitas tu "voto" de no borrar el archivo,no para remove
		String uso;
		synchronized(conhashmap){
			uso=conhashmap.get(aux);
			uso.replaceFirst(READ, "");//quitas 1 "READ"
			if(uso.isBlank()) {
				conhashmap.remove(aux, conhashmap.get(aux));
			}
			else {
				conhashmap.replace(aux, conhashmap.get(aux), uso);
			}
		}
	} 
}
