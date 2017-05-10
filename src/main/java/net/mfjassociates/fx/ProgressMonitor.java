package net.mfjassociates.fx;



import java.io.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.text.*;
import java.util.Locale;
import javax.accessibility.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;


/** A class to monitor the progress of some operation. If it looks
 * like the operation will take a while, a progress dialog will be popped up.
 * When the ProgressMonitor is created it is given a numeric range and a
 * descriptive string. As the operation progresses, call the setProgress method
 * to indicate how far along the [min,max] range the operation is.
 * Initially, there is no ProgressDialog. After the first millisToDecideToPopup
 * milliseconds (default 500) the progress monitor will predict how long
 * the operation will take.  If it is longer than millisToPopup (default 2000,
 * 2 seconds) a ProgressDialog will be popped up.
 * <p>
 * From time to time, when the Dialog box is visible, the progress bar will
 * be updated when setProgress is called.  setProgress won't always update
 * the progress bar, it will only be done if the amount of progress is
 * visibly significant.
 *
 * <p>
 *
 *
 * @see ProgressMonitorInputStream
 * @author Mario Jauvin
 */
public class ProgressMonitor extends Application
{
    private ProgressMonitor root;
    private Dialog<Boolean> dialog;
    private ProgressBar    myBar;
    private Label          noteLabel;
    private Window       parentComponent;
    private String          note;
    private String          message;
    private long            T0;
    private int             millisToDecideToPopup = 500;
    private int             millisToPopup = 2000;
    private int				min;
    private int             max=Integer.MAX_VALUE; // no max
    private ObjectProperty<Boolean> resultProperty = new SimpleObjectProperty<Boolean>(true);


    /**
     * Constructs a graphic object that shows progress, typically by filling
     * in a rectangular bar as the process nears completion.
     *
     * @param parentComponent the parent component for the dialog box
     * @param message a descriptive message that will be shown
     *        to the user to indicate what operation is being monitored.
     *        This does not change as the operation progresses.
     * @param note a short note describing the state of the
     *        operation.  As the operation progresses, you can call
     *        setNote to change the note displayed.  This is used,
     *        for example, in operations that iterate through a
     *        list of files to show the name of the file being processes.
     *        If note is initially null, there will be no note line
     *        in the dialog box and setNote will be ineffective
     * @param min the lower bound of the range
     * @param max the upper bound of the range
     */
    public ProgressMonitor(Window parentComponent,
                           String message,
                           String note,
                           int min,
                           int max) {
        this(parentComponent, message, note, min, max, null);
    }


    private ProgressMonitor(Window parentComponent,
                            String message,
                            String note,
                            int min,
                            int max,
                            ProgressMonitor group) {
        this.max = max;
        this.min = min;
        this.parentComponent = parentComponent;


        this.message = message;
        this.note = note;
        if (group != null) {
            root = (group.root != null) ? group.root : group;
            T0 = root.T0;
            dialog = root.dialog;
        }
        else {
            T0 = System.currentTimeMillis();
        }
    }




    /**
     * Indicate the progress of the operation being monitored.
     * If the specified value is &gt;= the maximum, the progress
     * monitor is closed.
     * @param nv an int specifying the current value, between the
     *        maximum and minimum specified for this component
     * @see #setMinimum
     * @see #setMaximum
     * @see #close
     */
    public void setProgress(int nv) {
        if (nv >= max) {
            close();
        }
        else {
            if (myBar != null && max!=Integer.MAX_VALUE) {
                myBar.setProgress((float)nv/max);
            }
            else {
                long T = System.currentTimeMillis();
                long dT = (int)(T-T0);
                if (dT >= millisToDecideToPopup) {
                    int predictedCompletionTime;
                    if (nv > min) {
                        predictedCompletionTime = (int)(dT *
                                                        (max - min) /
                                                        (nv - min));
                    }
                    else {
                        predictedCompletionTime = millisToPopup;
                    }
                    if (predictedCompletionTime >= millisToPopup) {
                        myBar = new ProgressBar();
                        myBar.setMaxWidth(Double.MAX_VALUE);
                        VBox vbar=new VBox();
                        vbar.setFillWidth(true);
                        if (max!=Integer.MAX_VALUE) myBar.setProgress((float)nv/max);
                        vbar.getChildren().add(myBar);
                        if (note != null) {
                        	noteLabel = new Label(note);
                        	vbar.getChildren().add(noteLabel);
                        }
                        Platform.runLater(() -> {
                            dialog=new Dialog<Boolean>();
                            dialog.setTitle(message);
                            dialog.setHeaderText(message);
                    		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                    		dialog.setResultConverter(dialogButton -> {
                    			if (dialogButton == ButtonType.OK)
                    				return Boolean.TRUE;
                    			return Boolean.FALSE;
                    		});
                            dialog.getDialogPane().setContent(vbar);
                            resultProperty.bind(dialog.resultProperty());
                            dialog.show();
                        });
                    }
                }
            }
        }
    }


    /**
     * Indicate that the operation is complete.  This happens automatically
     * when the value set by setProgress is &gt;= max, but it may be called
     * earlier if the operation ends early.
     */
    public void close() {
        if (dialog != null) {
        	resultProperty.unbind();
        	Platform.runLater(() -> {
        		if (dialog!=null) dialog.close();
        		dialog=null;
        	});
            myBar = null;
        }
    }


    /**
     * Returns the minimum value -- the lower end of the progress value.
     *
     * @return an int representing the minimum value
     * @see #setMinimum
     */
    public int getMinimum() {
        return min;
    }


    /**
     * Specifies the minimum value.
     *
     * @param m  an int specifying the minimum value
     * @see #getMinimum
     */
    public void setMinimum(int m) {
        min = m;
    }


    /**
     * Returns the maximum value -- the higher end of the progress value.
     *
     * @return an int representing the maximum value
     * @see #setMaximum
     */
    public int getMaximum() {
        return max;
    }


    /**
     * Specifies the maximum value.
     *
     * @param m  an int specifying the maximum value
     * @see #getMaximum
     */
    public void setMaximum(int m) {
        max = m;
    }


    /**
     * Returns true if the user hits the Cancel button in the progress dialog.
     */
    public boolean isCanceled() {
    	try {
        	if (resultProperty!=null && resultProperty.getValue()!=null) {
        		return !resultProperty.getValue();
        	}
        	else {
        		return false;
        	}
		} catch (NullPointerException e) {
			throw e;
		}
    }


    /**
     * Specifies the amount of time to wait before deciding whether or
     * not to popup a progress monitor.
     *
     * @param millisToDecideToPopup  an int specifying the time to wait,
     *        in milliseconds
     * @see #getMillisToDecideToPopup
     */
    public void setMillisToDecideToPopup(int millisToDecideToPopup) {
        this.millisToDecideToPopup = millisToDecideToPopup;
    }


    /**
     * Returns the amount of time this object waits before deciding whether
     * or not to popup a progress monitor.
     *
     * @see #setMillisToDecideToPopup
     */
    public int getMillisToDecideToPopup() {
        return millisToDecideToPopup;
    }


    /**
     * Specifies the amount of time it will take for the popup to appear.
     * (If the predicted time remaining is less than this time, the popup
     * won't be displayed.)
     *
     * @param millisToPopup  an int specifying the time in milliseconds
     * @see #getMillisToPopup
     */
    public void setMillisToPopup(int millisToPopup) {
        this.millisToPopup = millisToPopup;
    }


    /**
     * Returns the amount of time it will take for the popup to appear.
     *
     * @see #setMillisToPopup
     */
    public int getMillisToPopup() {
        return millisToPopup;
    }


    /**
     * Specifies the additional note that is displayed along with the
     * progress message. Used, for example, to show which file the
     * is currently being copied during a multiple-file copy.
     *
     * @param note  a String specifying the note to display
     * @see #getNote
     */
    public void setNote(String note) {
        this.note = note;
        if (noteLabel != null) {
            noteLabel.setText(note);
        }
    }


    /**
     * Specifies the additional note that is displayed along with the
     * progress message.
     *
     * @return a String specifying the note to display
     * @see #setNote
     */
    public String getNote() {
        return note;
    }

	@Override
	public void start(Stage primaryStage) throws Exception {
	}

}
