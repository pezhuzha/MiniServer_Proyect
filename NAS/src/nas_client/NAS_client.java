package nas_client;

import java.io.*;
import java.net.Socket;

public class NAS_client {

	public static void main(String args[]) {
		String in=null;//selected option
		String name=null;//file name
		String resp=null;//getrespuesta ok
		int selected;//parse of String in
		File f=null;
		try (Socket s=new Socket("127.0.0.1",55555);
				BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"));
				BufferedReader br=new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));
				BufferedReader sysin=new BufferedReader(new InputStreamReader(System.in)))
		{	
			do {
				s.setKeepAlive(true);
				s.setSoTimeout(500);
				System.out.println(
						"Menu:\n"
								+ "		1:Entrar en el directorio\n"
								+ "		2:Listar archivos del servidor\n"
								+ "		3:Crear directorio en el servidor\n"
								+ "		4:Eliminar archivo/directorio en el servidor\n"
								+ "		5:Enviar archivo al servidor\n"
								+ "		6:Recibir archivo del servidor\n"
								+ "		9:Salir");
				in=sysin.readLine();
				selected=parsetoint(in);
				switch(selected) {
				case 1:

					bw.write("cd\n");//cd
					bw.flush();

					System.out.println("\nNombre del directorio a entrar(Enter si es directorio actual)");


					name=sysin.readLine();

					bw.write(name+"\n");
					bw.flush();

					do{
						System.out.println(br.readLine());
					}while(br.ready());
					break;
				case 2:
					bw.write("ls\n");//ls
					bw.flush();

					System.out.println("Nombre del directorio a listar(Enter si es root)");					

					name=sysin.readLine();

					bw.write(name+"\n");
					bw.flush();

					do{
						System.out.println(br.readLine());
					}while(br.ready());


					break;

				case 3:
					bw.write("mkdir\n");//mkdir
					bw.flush();

					System.out.println("Nombre del directorio a crear");

					name=sysin.readLine();

					bw.write(name+"\n");
					bw.flush();

					do{
						System.out.println(br.readLine());
					}while(br.ready());

					break;
				case 4:
					bw.write("rm\n");//rm
					bw.flush();

					System.out.println("Nombre del archivo/directorio a eliminar");

					name=sysin.readLine();

					bw.write(name+"\n");
					bw.flush();

					do{
						System.out.println(br.readLine());
					}while(br.ready());

					break;
				case 5:
					System.out.println("Nombre del archivo a enviar");

					name=sysin.readLine();
					f=new File(name);

					if(f.isFile()) {
						bw.write("send\n");//send to server
						bw.flush();

						bw.write(f.getName()+"\n");
						bw.flush();

						resp=br.readLine();
						if(resp.equalsIgnoreCase("OK")) {//si no existe ningun archivo en el servidor con el mismo nombre
							try(BufferedReader filereader=new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"))){
								do {
									bw.write(filereader.readLine()+"\n");
									bw.flush();
								}while(filereader.ready());
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
				case 6:
					bw.write("recive\n");//getfile from server
					bw.flush();

					System.out.println("Nombre del archivo a recibir");

					name=sysin.readLine();

					bw.write(name+"\n");
					bw.flush();

					resp=br.readLine();

					if(resp.equalsIgnoreCase("OK")) {
						File currentdir=new File(".");//guarda el archivo en el directorio actual
						f=new File(currentdir.getCanonicalPath()+File.separator+name);

						try(BufferedWriter archivorecibido=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f.getName()),"UTF-8"))){
							do {
								archivorecibido.write(br.readLine()+"\n");
							} while(br.ready());
							archivorecibido.flush();

							System.out.println("Archivo creado exitosamente");
						}
						catch(IOException e1) {
							e1.printStackTrace();
						}

					}else {
						System.out.println(resp);
					}
					break;
				case 9:
					s.close();
					break;
				default:
					System.out.println("Error no has insertado un número valido");
					break;
				}
			}while(selected!=9 && !s.isClosed());
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
