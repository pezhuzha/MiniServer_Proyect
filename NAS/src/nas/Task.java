package nas;

import java.io.File;
import java.io.IOException;
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
							bw.write("OK\n");
						}
						else {
							bw.write("BAD\n");
						}
						bw.flush();

						break;
					case 's'://sended to server
						res=br.readLine();
						path=root.getAbsolutePath()+File.separator+res;
						File comprobarRuta=new File(path);
						if(comprobarRuta.exists()) {
							bw.write("EXISTS\n");
							bw.flush();
						}else{
							bw.write("OK\n");
							bw.flush();
							FileOutputStream archivorecibido=new FileOutputStream(comprobarRuta);
							int ended=is.read();
							while(ended!=-1) {
								archivorecibido.write(ended);
								ended=is.read();
							}
							archivorecibido.flush();
							archivorecibido.close();
							bw.write("OK \n");
							bw.flush();
						}
						break;
					case 'r'://rm
						res=br.readLine();
						path=root.getAbsolutePath()+File.separator+res;
						
						File delete=new File(path);
						if(delete.exists()) {
							if(delete.delete()) {
								bw.write("OK \n");
								bw.flush();
							}else {
								bw.write("BAD \n");
								bw.flush();
							}
						}
						else {
							bw.write("NOEXISTS \n");
							bw.flush();
						}
						
						break;
					case 'l'://ls
						res=br.readLine();
						if(res.isBlank()) //caso root
						{
							aux=root.listFiles();
						}
						else if(res.contains("..")) //no dejar que acceda a directorios superiores
						{
							bw.write("UNREADABLE\n");
							bw.flush();
						}
						else //normal
						{
							path=root.getAbsolutePath()+File.separator+res;
							subdir=new File(path);
							if(subdir!=null && subdir.canRead() && subdir.isDirectory()) {
								aux=subdir.listFiles();}
							else {//caso de leer algo que no es directorio o no se pude leer por permisos
								bw.write("BAD\n");
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
						else {
							bw.write("BAD\n");
							bw.flush();
						}
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
