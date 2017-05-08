package net.mfjassociates.fx;

import java.util.function.Function;
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
				e.printStackTrace();
				throw new RuntimeException("get method returned a checked exception: "+e.getLocalizedMessage(), e);
			}
		}
		
		T getThrows() throws ET;
	}
	
	/**
	 * @author Mario Jauvin
	 *
	 * @param <T> - type of the returned object
	 * @param <P> - type of the object passed as the apply parameter
	 * @param <ET> - type of the exception that can be thrown
	 */
	@FunctionalInterface
	public static interface ThrowingFunction<T, P, ET extends Exception> extends Function<P, T> {
		@Override
		default T apply(P parm) {
			try {
				return applyThrows(parm);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("apply method returned a checked exception: "+e.getLocalizedMessage(), e);
			}
		}
		
		T applyThrows(P parm) throws ET;
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
		
		/**
		 * Constructor that accepts the following arguments:
		 * 
		 * @param aCompleted - {@link Callback} whose call method will be invoked when
		 * the Task has completed successfully.
		 * @param aFailed - {@link Callback} whose call method will be invoked when
		 * the Task has failed.
		 * @param aWorker - a {@link ThrowingSupplier} whose get method will be
		 * invoked and who should return the object of type T for this {@link Task}. This is actually the long
		 * running method this Task is processing in the background.
		 * @param aScene - the {@link Scene} whose {@link Cursor} will be set to wait during processing
		 */
		public ResponsiveTask(Callback<ResponsiveTask<T, ET>, Boolean> aCompleted, Callback<ResponsiveTask<T, ET>, 
				Void> aFailed, ThrowingSupplier<T, ET> aWorker, Scene aScene) {
			aScene.setCursor(Cursor.WAIT);
			this.setOnSucceeded(event -> Platform.runLater(() -> {if (aCompleted.call(this)) scene.setCursor(Cursor.DEFAULT);}));
			this.setOnFailed(event -> Platform.runLater(() -> {aFailed.call(this);scene.setCursor(Cursor.DEFAULT);}));
			this.worker=aWorker;
			this.scene=aScene;
		}

		@Override
		protected T call() throws ET {
			return worker.get();
		}
		
	}
	
	/**
	 * This class will add to the {@link ResponsiveTask} functionality the ability to update progress
	 * such that a JavaFX ProgressBar can be updated.  This is implemented by the replacement
	 * of the {@link ThrowingSupplier} contructor argument with a {@link ThrowingFunction} argument whose
	 * apply method will be passed the instance of the {@link ProgressResponsiveTask} so that its
	 * {@link ProgressResponsiveTask#myUpdateProgress(int)} may be called.
	 * @author Mario Jauvin
	 *
	 * @param <T> - type of the object to be returned by the Task.
	 * @param <ET> - type of the exception that can be thrown in the get method
	 * @see ResponsiveTask
	 */
	public static class ProgressResponsiveTask<T, ET extends Exception> extends ResponsiveTask<T, ET> {
		
		private long maxSize;
		private ThrowingFunction<T, ProgressResponsiveTask<T, ET>, ET> fworker;

		public ProgressResponsiveTask(Callback<ResponsiveTask<T, ET>, Boolean> aCompleted, Callback<ResponsiveTask<T, ET>, 
				Void> aFailed, ThrowingFunction<T, ProgressResponsiveTask<T, ET>, ET> aWorker, Scene aScene, long aMaxSize) {
			super(aCompleted, aFailed, null, aScene);
			this.maxSize=aMaxSize;
			this.fworker=aWorker;
		}
		
		public void myUpdateProgress(int workDone) {
			super.updateProgress(workDone, maxSize);
		}

		@Override
		protected T call() throws ET {
			return fworker.apply(this);
		}
		
	}

}
