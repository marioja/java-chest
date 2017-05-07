package net.mfjassociates.fx;

import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.util.Callback;

/**
 * Various JavaFX utilities
 * 
 * @author Mario Jauvin
 *
 */
public class FXUtils {
	
	/**
	 * Supplier functional interface that can throw checked exceptions.
	 *  
	 * @author Mario Jauvin
	 *
	 * @param <T> - type of the supplied object
	 * @param <ET> - type of the exception that can be thrown
	 */
	@FunctionalInterface
	public static interface ThrowingSupplier<T, ET extends Exception> extends Supplier<T> {
		@Override
		default T get() {
			try {
				return getThrows();
			} catch (Exception e) {
				throw new RuntimeException("get method returned a checked exception", e);
			}
		}
		
		T getThrows() throws ET;
	}
	
	/**
	 * {@link javafx.concurrent.Task} extension that will maintain the state of the
	 * scene aScene busy while the method {@link net.mfjassociates.fx.FXUtils.ThrowingSupplier#get()}
	 * executes in the background.
	 * 
	 * @author Mario Jauvin
	 *
	 * @param <T> - type of the object to be returned by the Task.
	 * @param <ET> - type of the exception that can be thrown in the get method
	 */
	public static class ResponsiveTask<T, ET extends Exception> extends Task<T> {
		
		private ThrowingSupplier<T, ET> worker;
		private Scene scene;
		
		public ResponsiveTask(Callback<ResponsiveTask<T, ET>, Void> aCompleted, Callback<ResponsiveTask<T, ET>, Void> aFailed, ThrowingSupplier<T, ET> aWorker, Scene aScene) {
			aScene.setCursor(Cursor.WAIT);
			this.setOnSucceeded(event -> Platform.runLater(() -> {aCompleted.call(this);scene.setCursor(Cursor.DEFAULT);}));
			this.setOnFailed(event -> Platform.runLater(() -> {aFailed.call(this);scene.setCursor(Cursor.DEFAULT);}));
			this.worker=aWorker;
			this.scene=aScene;
		}

		@Override
		protected T call() throws ET {
			return worker.get();
		}
		
	}

}
