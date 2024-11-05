package nas_client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class NAS_client {

	public static void main(String args[]) {
		String in=null,name=null,resp=null;
		int selected;
		try (Socket s=new Socket("127.0.0.1",55555);
				InputStream is=s.getInputStream();
				OutputStream os=s.getOutputStream();
				BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
				BufferedReader br=new BufferedReader(new InputStreamReader(is,"UTF-8"));
				BufferedReader sysin=new BufferedReader(new InputStreamReader(System.in)))
		{	
			do {
				System.out.println("Menu:\n"
						+ "		1:Crear directorio\n"
						+ "		2:Enviar archivo\n"
						+ "		3:Eliminar archivo\n"
						+ "		4:Listar archivos\n"
						+ "		9:Salir\n");
				in=sysin.readLine();
				selected=parsetoint(in);
				switch(selected) {
				case 1:
					System.out.println("Nombre del directorio a crear");

					bw.write("m\n");//mkdir
					bw.flush();

					name=sysin.readLine();

					bw.write(name+"\n");
					bw.flush();

					resp=br.readLine();

					if(resp.equalsIgnoreCase("OK")) {
						System.out.println("El directorio se ha creado");
					}
					else if(resp.equalsIgnoreCase("BAD")) {
						System.out.println("Error, el directorio no se ha podido crear");
					}
					break;
				case 2:
					System.out.println("Nombre del archivo a enviar");

					bw.write("s\n");//send to server
					bw.flush();

					name=sysin.readLine();
					File f=new File(name);
					if(f.isFile()) {
						String fileshortname[]=name.split(File.separator);
						String filename=fileshortname[fileshortname.length-1];//nombre corto para no enviar toda la ruta absoluta del ordenador
						bw.write(filename+"\n");
						resp=br.readLine();
						if(resp.equalsIgnoreCase("OK")) {//si no existe ningun archivo en el servidor con el mismo nombre
							try(FileInputStream fos=new FileInputStream(f);){
								byte b[]=fos.readAllBytes();
								os.write(b);
								os.flush();
							}
							catch(IOException ei1) {
								ei1.printStackTrace();
							}
							if(resp.equalsIgnoreCase("OK")) {
								System.out.println("Archivo creado exitosamente");
							}
						}
						else if(resp.equalsIgnoreCase("EXISTS")){//existe un archivo con el mismo nombre 
							System.out.println("El archivo ya existe (Es necesario borralo primero)");
						}
					}
					else {
						System.out.println("Error, no se puede enviar un directorio");}


					break;
				case 3:
					System.out.println("Nombre del archivo/directorio a eliminar");

					bw.write("r\n");//rm
					bw.flush();
					
					name=sysin.readLine();
					
					bw.write(name+"\n");
					bw.flush();
					
					if(resp.equalsIgnoreCase("OK")) {
						System.out.println("Archivo borrado correctamente\n");
						}
					else if(resp.equalsIgnoreCase("BAD")) {
						System.out.println("No se puede borrar el archivo\nSi es un directorio tiene que estar vacio\n");
					}
					else if(resp.equalsIgnoreCase("NOEXISTS")) {
						System.out.println("No se puede borrar el archivo, no existe\n");
					}
					

					break;
				case 4:
					System.out.println("Nombre del directorio a listar(Enter si es root)");

					bw.write("l\n");//ls
					bw.flush();

					name=sysin.readLine();

					bw.write(name+"\n");
					bw.flush();

					resp=br.readLine();
					if(resp.equalsIgnoreCase("OK")) {
						resp=br.readLine();
						while(br.ready()){
							System.out.println(br.readLine());
						}
						}
					else if(resp.equalsIgnoreCase("BAD")) {
						System.out.println("No existe este directorio\n");
					}
					else if(resp.equalsIgnoreCase("UNREADABLE")) {
						System.out.println("No se puede acceder a \"..\"\n");
					}
					
					break;
				default:
					System.out.println("Error no has insertado un número");
					break;
				}
			}while(selected!=9);
			System.out.println("Programa terminado");
			return;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static int parsetoint(String probint) {
		int integer=0;
		try{integer=Integer.parseInt(probint);}
		catch(NumberFormatException e1) {
		}
		return integer;
	}
}
