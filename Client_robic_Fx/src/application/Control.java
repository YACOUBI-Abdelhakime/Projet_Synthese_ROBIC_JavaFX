package application;

import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import javafx.event.ActionEvent;

import javafx.scene.control.TextArea;

import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

public class Control {
	@FXML
	private TextField hostnameV;
	@FXML
	private TextField portV;
	@FXML
	private Label fileName;
	@FXML
	private Button btnConnextion;
	@FXML
	private Button btnDeconnecter;
	@FXML
	private GridPane body;
	@FXML
	private TextArea textArea1;
	@FXML
	private TextArea textArea2;
	@FXML
	private Button b1, btnArreter, btnExecL, btnRetour1;


	Socket socket;
    public static BufferedReader br;
    public static PrintStream ps;
    public static String textTrace="";
    boolean envoyerAutre ;
    int indexLine = 0;
    String scripts[];
    public static boolean serverClosed = true; 
    File fileToLoad = null;
    
	
	// Event Listener on Button[#btnDeconnecter].onAction
	@FXML
	public void deconnecter(ActionEvent event) {
		try {
			ps.println(JSON.Java2Json(new Message("cmd","bye")));
			socket.close();
			ps.close();
	    	br.close();
	    	body.setDisable(true);
	    	serverClosed = true; 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// Event Listener on Button[#btnArreter].onAction
	@FXML
	public void arreterExec(ActionEvent event) {
		System.out.println("+++++++fonction arreter() ");
		ps.println(JSON.Java2Json(new Message("cmd","Arreter")));
	}

	// Event Listener on MenuItem.onAction
	@FXML
	public void openFile(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		FileInputStream fis;

        //only allow text files to be selected using chooser
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt")
        );

        //set initial directory somewhere user will recognise
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        //let user select file
        fileToLoad = fileChooser.showOpenDialog(null);
        
        
        if (fileToLoad != null) {
        	//System.out.println("file = "+fileToLoad.getPath());
        	fileName.setText(fileToLoad.getPath().toString());
        }else {
        	System.out.println("file = null");
        	return;
        }
        try {
			fis = new FileInputStream(fileToLoad);
			int n;
			String text = "";
			//byte buf[] = new byte[512];
			while((n = fis.read()) > 0) {
				text += (char) n;
			}
			textArea1.setText(text);
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        
        
	}
	// Event Listener on MenuItem.onAction
	@FXML
	public void saveFile(ActionEvent event) {
		try {
			if(fileToLoad != null) {//enregistrer dans le fichier courant
				FileOutputStream fos = new FileOutputStream(fileToLoad);
				
				String text = textArea1.getText();
	
				fos.write(text.getBytes());
				fos.close();
			}else {
				FileChooser fileChooser = new FileChooser();
				FileOutputStream fos;

		        //only allow text files to be selected using chooser
		        fileChooser.getExtensionFilters().add(
		                new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt")
		        );

		        //set initial directory somewhere user will recognise
		        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

		        //let user select file
		        fileToLoad = fileChooser.showOpenDialog(null);
		        if(fileToLoad!=null) {
		        	fos = new FileOutputStream(fileToLoad);
		        	fileName.setText(fileToLoad.getPath().toString());
					String text = textArea1.getText();
		
					fos.write(text.getBytes());
					fos.close();
		        }
		        
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// Event Listener on MenuItem.onAction
		@FXML
		public void fermerFile(ActionEvent event) {
			fileToLoad = null;
			fileName.setText(" ");
		}
	// Event Listener on Button[#btnConnextion].onAction
	@FXML
	public void connexion(ActionEvent event) {
		int port = Integer.parseInt(portV.getText().toString());
    	String host = hostnameV.getText();
    	
    	try {
			socket = new Socket(host, port);
	    	ps = new PrintStream(socket.getOutputStream());
	    	br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    	
	    	body.setDisable(false);
	    	//disableBtns(false);
	    	
	    	textArea2.setText("Trace d'exécution");
	    	Responce threadRes = new Responce(textArea2);
	    	threadRes.start();
	    	serverClosed = false; 
	    	btnDeconnecter.setDisable(false);
	    	
    	}  catch (Exception e) {
    		//e.printStackTrace();
    		textArea2.setText("Erreur de connexion essayer après !!!");
		}
	}
	// Event Listener on Button[#b1].onAction
	@FXML
	public void b1_exec(ActionEvent event) {
		if(serverClosed) {
			disableBtns(true);
			return ;
		}
		
		
		ps.println(JSON.Java2Json(new Message("cmd","fin")));
		
		if(indexLine-1 >= 0) {
			scripts[indexLine-1] = scripts[indexLine-1].substring(3);
			updateTextScripts(scripts);
			indexLine = 0;
			btnExecL.setText("Exécuter line 1");
			btnExecL.setDisable(false);
		}
		
		String cmd = textArea1.getText().trim();
    	textTrace="";
		
		Message msg = new Message("script",cmd);
    	
    	String json = JSON.Java2Json(msg); 
    	
    	ps.println(json);
    	
	}
	// Event Listener on Button[#btnArreter].onAction
	@FXML
	public void execLine(ActionEvent event) {
		if(serverClosed) {
			disableBtns(true);
			return ;
		}
		
		if(indexLine == 0) {
			scripts = textArea1.getText().trim().split("\n");
			textTrace="";
			ps.println(JSON.Java2Json(new Message("cmd","debut")));
		}
		
		if( indexLine < scripts.length ) {
			String cmd = scripts[indexLine];
			scripts[indexLine] = "=> "+scripts[indexLine];
			if(indexLine-1 >=0) {
				scripts[indexLine-1] = scripts[indexLine-1].substring(3);
			}
			
			updateTextScripts(scripts);
			
			indexLine++;
			Message msg = new Message("script",cmd);
	    	
	    	String json = JSON.Java2Json(msg); 
	    	
	    	ps.println(json);
	    	
	    	//readFromServer();
	    	btnExecL.setText("Exécuter line "+(indexLine+1));
	    	
	    	if(scripts.length == indexLine ) {
	    		ps.println(JSON.Java2Json(new Message("cmd","fin")));
	    		btnExecL.setText("Scripts fini");
				btnExecL.setDisable(true);
	    	}
		}
	}
	
	// Event Listener on Button[#btnArreter].onAction
	@FXML
	public void retourLine1(ActionEvent event) {

		if(serverClosed) {
			disableBtns(true);
			return ;
		}
		
		btnExecL.setText("Exécuter line 1");
		if(indexLine-1 >= 0) {
			scripts[indexLine-1] = scripts[indexLine-1].substring(3);
			updateTextScripts(scripts);
			//System.out.println("INDEXLINE = "+(indexLine-1));
			ps.println(JSON.Java2Json(new Message("cmd","fin par lignes")));
			indexLine = 0;
			scripts = textArea1.getText().trim().split("\n");
			textTrace="";
			textArea2.setText(textTrace);
		}
		
		btnExecL.setDisable(false);
		
		
	}
	
	public void updateTextScripts(String scripts[]) {
		String tmpAllRes = "";
		for(String line : scripts) {
			tmpAllRes += line+"\n";			
		}
		System.out.println("tmpAllRes ==> \n"+tmpAllRes);
		textArea1.setText(tmpAllRes);
		
	}
	
	public void readFromServer() {
		int state;
		String tmpRes,tmpAllRes="";
		try {
			do{
				tmpRes = br.readLine();//System.out.println("+>> "+tmpRes);
		        state = Integer.valueOf(tmpRes.substring(0, tmpRes.indexOf(" ")));
		        tmpRes = ">> "+tmpRes.substring(2).trim()+"\n";
		        tmpAllRes = tmpRes.equals(">> \n") ? "" : tmpRes;
		       
		        textTrace += tmpAllRes;
				textArea2.setText(textTrace);
		        
			}while (state == 1); 

			//textTrace += tmpAllRes;
			//textArea2.setText(textTrace);
			
		} catch (IOException e) {
			//server error
			textTrace += ">> Serveur est deconncté !!";
			textArea2.setText(textTrace);
		}
	}
	
	void disableBtns(boolean res) {
		btnArreter.setDisable(res);
		btnExecL.setDisable(res);
		btnRetour1.setDisable(res);
		b1.setDisable(res);
	}
	
}
