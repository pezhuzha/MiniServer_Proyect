package nas_client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
			case 0:
				System.out.println("Error no has insertado un número");
				break;
			case 1:
				
				System.out.println("Nombre del directorio a crear");
				
				bw.write("m\n");//mkdir
				bw.flush();
				
				name=sysin.readLine();
				
				bw.write(name+"\n");
				bw.flush();
				
				resp=br.readLine();
				
				if(resp=="OK") {
					System.out.println("El directorio se ha creado");
				}
				else if(resp=="BAD") {
					System.out.println("Error el directorio no se ha podido crear");
				}
				break;
			case 2:
				System.out.println("Nombre del archivo a enviar");
				
				bw.write("s\n");//send
				bw.flush();
				
				
				break;
			case 3:
				System.out.println("Nombre del archivo a eliminar");
				
				bw.write("r\n");//rm
				bw.flush();
				
				break;
			case 4:
				System.out.println("Nombre del directorio a listar(Enter si es root)");
				
				bw.write("l\n");//ls
				bw.flush();
				
				name=sysin.readLine();
				
				bw.write(name+"\n");
				bw.flush();
				
				resp=br.readLine();
				int length=parsetoint(resp);
				
				for(int i=0;i<length;i++) {
					System.out.println(br.readLine());
				}

				
				break;
			}
			}while(selected!=9);
			
			
		} catch (IOException e) {
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
