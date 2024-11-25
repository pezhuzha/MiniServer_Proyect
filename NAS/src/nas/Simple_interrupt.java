package nas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Simple_interrupt implements Runnable{

	public void run() {
		String getMessage=null;
		try {
			while(getMessage==null || !getMessage.contentEquals("Confirmar")) {
				BufferedReader sysin=new BufferedReader(new InputStreamReader(System.in));
				while(getMessage==null || !getMessage.contentEquals("INTERRUMPIR")) {
					System.out.println("Si quieres para la ejecución escriba INTERRUMPIR");
					getMessage=sysin.readLine();
				}
				while(getMessage.contentEquals("INTERRUMPIR")) {
					System.out.println("Para confirmar escriba Confirmar");
					getMessage=sysin.readLine();
				}
			}
			Thread th[]=new Thread[Thread.activeCount()];
			Thread.enumerate(th);
			for(int i=0;i<th.length;i++) {
				th[i].interrupt();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
