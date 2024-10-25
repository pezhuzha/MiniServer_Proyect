package nas;
import java.io.File;
import java.io.IOException;
import java.io.*;
import java.net.Socket;
public class Task implements Runnable {
	private Socket s;
	private File root;
	Task (Socket soc,File ro){
		this.s=soc;
		this.root=ro;
	}
	public void run() {
		String res=null;
		File aux[]=null,subdir=null;
		try(InputStream is=s.getInputStream();
			OutputStream os=s.getOutputStream();
			BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
			BufferedReader br=new BufferedReader(new InputStreamReader(is,"UTF-8")))
		{
			res=br.readLine();
			switch(res.charAt(0)) {
			case 'm'://mkdir
				break;
			case 's'://send
				break;
			case 'r'://rm
				break;
			case 'l'://ls
				res=br.readLine();
				if(res.isBlank()) {
				aux=root.listFiles();}
				else {
					subdir=new File(root.getAbsolutePath()+File.separator+res);
					aux=subdir.listFiles();
				}
				bw.write(aux.length+"\n");
				for(int i=0;i<aux.length;i++) {
					if(aux[i].isDirectory()) {
						bw.write(aux[i].getName()+File.separator+"\n");
					}
					else {
						bw.write(aux[i].getName()+"\n");
					}
					bw.flush();
				}
				break;
			}
			
			
			
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
