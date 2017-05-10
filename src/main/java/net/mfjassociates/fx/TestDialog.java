package net.mfjassociates.fx;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class TestDialog extends Application {

	public static void main(String[] args) throws IOException {
//		launch(args);
		File large=new File("D:\\Users\\mario\\Documents\\dropbox\\doc\\javatutorials.zip");
		File small=new File("D:\\Users\\mario\\Documents\\Abdiel slideshow Eine Kleine Nachtmusik.iso");
		File f=large;
		InputStream pis=new BufferedInputStream(new ProgressMonitorInputStream(null, "Reading "+f.getName()+" file", new FileInputStream(f)));
//		pis.close();
//		pis=new BufferedInputStream(new FileInputStream(f));
		int bs=4096*4096;
		int i=0;
		while (pis.read()!=-1) {
			if (i++%bs==0) System.out.println(String.format("Read %1$d byte", bs));
		}
		pis.close();
		Platform.exit();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void start(Stage primaryStage) throws Exception {
		Dialog<Boolean> dialog=new Dialog<>();
		Window owner=null;
		ProgressBar pbar=new ProgressBar();
		dialog.initModality(Modality.NONE);
		dialog.initOwner(owner);
		dialog.setHeaderText("Transferring abc.txt");
		dialog.setTitle("Transferring abc.txt");
		dialog.getDialogPane().setContent(pbar);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		dialog.resultProperty().addListener(value -> System.out.println("changed value "+((ObservableValue<Boolean>) value).getValue()));
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == ButtonType.OK)
				return Boolean.TRUE;
			return Boolean.FALSE;
		});
//		dialog.showAndWait().ifPresent(response -> System.out.println("Dialog returned "+response));
		dialog.show();
		
	}
	

}
