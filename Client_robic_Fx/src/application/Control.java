package application;

import javafx.fxml.FXML;

import javafx.scene.control.Button;

import java.io.File;

import javafx.event.ActionEvent;

import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

public class Control {
	@FXML
	private TextArea textArea1;
	@FXML
	private TextArea textArea2;
	@FXML
	private Button b1;

	public void initialize() {
    }

    public void b1_exec(ActionEvent event) {
    	System.out.println("b1_exec");
    	System.out.println("textArea1 = "+textArea1.getText());
    }
    
    public void openFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();

        //only allow text files to be selected using chooser
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt")
        );

        //set initial directory somewhere user will recognise
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        //let user select file
        File fileToLoad = fileChooser.showOpenDialog(null);
        
        if (fileToLoad != null) {
        	System.out.println("file = "+fileToLoad.getPath());
        }
        else {
        	System.out.println("file = null");
        }
        

     }


    public void saveFile(ActionEvent event) {
    }
}
