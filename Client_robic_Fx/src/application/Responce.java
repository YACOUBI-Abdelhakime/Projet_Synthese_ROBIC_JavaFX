package application;

import java.io.IOException;

import javafx.scene.control.TextArea;

public class Responce extends Thread{
	private TextArea textArea2;
	
	public Responce(TextArea t) {
		textArea2 = t;
	}
	
	public void run() {
		String tmpRes;
		while(true) {
			try {
				tmpRes = ">> "+Control.br.readLine().trim()+"\n";
			
		        /*tmpRes = ">> "+tmpRes.substring(2).trim()+"\n";
		        tmpAllRes = tmpRes.equals(">> \n") ? "" : tmpRes;*/
		       
		        Control.textTrace += tmpRes;
				textArea2.setText(Control.textTrace);
			
			} catch (IOException e) { 
				//server error
				Control.textTrace += ">> Serveur est deconncté !!";
				textArea2.setText(Control.textTrace);
				Control.serverClosed = true; 
				break;
			}
		}
	}
}
