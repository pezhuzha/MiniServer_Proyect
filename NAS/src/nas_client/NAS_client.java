package nas_client;

import java.io.*;
import java.net.Socket;

public class NAS_client {

	public static void main(String args[]) {
		String in=null;//selected option
		String name=null;//file name
		String resp=null;//getrespuesta ok
		int selected;//parse of String in
		try (Socket s=new Socket("127.0.0.1",55555);
				InputStream is=s.getInputStream();
				OutputStream os=s.getOutputStream();
				BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
				BufferedReader br=new BufferedReader(new InputStreamReader(is,"UTF-8"));
				BufferedReader sysin=new BufferedReader(new InputStreamReader(System.in)))
		{	
			do {
				System.out.println(
						"Menu:\n"
						+ "		1:Crear directorio en el servidor\n"
						+ "		2:Enviar archivo al servidor\n"
						+ "		3:Eliminar archivo/directorio en el servidor\n"
						+ "		4:Listar archivos del servidor\n"
						+ "		5:Entrar en el directorio y listar archivos del servidor\n"
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

					System.out.println(br.readLine());
					
					break;
				case 2:
					System.out.println("Nombre del archivo a enviar");


					name=sysin.readLine();
					
					File f=new File(name);
					if(f.isFile()) {
						bw.write("s\n");//send to server
						bw.flush();
						bw.write(f.getName()+"\n");
						bw.flush();
						resp=br.readLine();
						if(resp.equalsIgnoreCase("OK")) {//si no existe ningun archivo en el servidor con el mismo nombre
							try(BufferedReader filereader=new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"))){
								while(filereader.ready()) {
									bw.write(filereader.readLine()+"\n");
									bw.flush();
								}
							}
							catch(IOException ei1) {
								ei1.printStackTrace();
							}
							System.out.println(br.readLine());
						}
						else {
							System.out.println(resp);
						}
					}
					else if(!f.exists()) {
						System.out.println("No existe un archivo con esta ruta");
					}
					else if(!f.isDirectory()){
						System.out.println("Error, no se puede enviar un directorio");}
					else {
						System.out.println("Error, desconocido");}


					break;
				case 3:
					System.out.println("Nombre del archivo/directorio a eliminar");

					bw.write("r\n");//rm
					bw.flush();
					
					name=sysin.readLine();
					
					bw.write(name+"\n");
					bw.flush();

					System.out.println(br.readLine());

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
						while(br.ready()){
							System.out.println(br.readLine());
						}
					}
					else  {
						System.out.println(resp);
					}
					
					break;
				case 5:
					
					bw.write("c\n");//cd+ls
					bw.flush();
					do {
					System.out.println("\nNombre del directorio a entrar(Enter si es directorio actual)");


					name=sysin.readLine();

					bw.write(name+"\n");
					bw.flush();

					resp=br.readLine();
					if(resp.equalsIgnoreCase("OK")) {
						while(br.ready()){
							System.out.println(br.readLine());
						}
						System.out.println("Si desea continuar (Y/n)");
						in=sysin.readLine();
					}
					else  {
						System.out.println(resp);
						in="n";
					}

					
					if(in.equalsIgnoreCase("y")) {	
						bw.write("END\n");
					}
					else {
						bw.write("CONTINUE\n");
					}
						bw.flush();
					}while(in.equalsIgnoreCase("y"));
					break;
				case 9:
					break;
				default:
					System.out.println("Error no has insertado un número valido");
					break;
				}
			}while(selected!=9);
			System.out.println("Programa terminado");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static int parsetoint(String probint) {
		int integer=-1;
		try{integer=Integer.parseInt(probint);}
		catch(NumberFormatException e1) {
		}
		return integer;
	}
}
