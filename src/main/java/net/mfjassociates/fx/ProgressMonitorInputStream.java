/*===========================================================================
 * Licensed Materials - Property of IBM
 * "Restricted Materials of IBM"
 * 
 * IBM SDK, Java(tm) Technology Edition, v6
 * (C) Copyright IBM Corp. 2013, 2014. All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 *===========================================================================
 */
/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */



package net.mfjassociates.fx;



import java.io.*;

import javafx.stage.Window;



/** 
 * Monitors the progress of reading from some InputStream. This ProgressMonitor
 * is normally invoked in roughly this form:
 * <pre>
 * InputStream in = new BufferedInputStream(
 *                          new ProgressMonitorInputStream(
 *                                  parentComponent,
 *                                  "Reading " + fileName,
 *                                  new FileInputStream(fileName)));
 * </pre><p>
 * This creates a progress monitor to monitor the progress of reading
 * the input stream.  If it's taking a while, a ProgressDialog will
 * be popped up to inform the user.  If the user hits the Cancel button
 * an InterruptedIOException will be thrown on the next read.
 * All the right cleanup is done when the stream is closed.
 *
 *
 * <p>
 *
 * For further documentation and examples see
 * <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/progress.html">How to Monitor Progress</a>,
 * a section in <em>The Java Tutorial.</em>
 *
 * @see ProgressMonitor
 * @author Mario Jauvin, after James Gosling code
 * @version %I% %G%
 */
public class ProgressMonitorInputStream extends FilterInputStream
{
    private ProgressMonitor monitor;
    private int             nread = 0;
    private int             size = 0;


    /**
     * Constructs an object to monitor the progress of an input stream.
     *
     * @param message Descriptive text to be placed in the dialog box
     *                if one is popped up.
     * @param parentComponent The component triggering the operation
     *                        being monitored.
     * @param in The input stream to be monitored.
     */
    public ProgressMonitorInputStream(Window parentComponent,
                                      String message,
                                      InputStream in) {
        super(in);
        try {
            size = in.available();
        }
        catch(IOException ioe) {
            size = 0;
        }
        monitor = new ProgressMonitor(parentComponent, message, null, 0, size);
    }


    /**
     * Get the ProgressMonitor object being used by this stream. Normally
     * this isn't needed unless you want to do something like change the
     * descriptive text partway through reading the file.
     * @return the ProgressMonitor object used by this object 
     */
    public ProgressMonitor getProgressMonitor() {
        return monitor;
    }


    /**
     * Overrides <code>FilterInputStream.read</code> 
     * to update the progress monitor after the read.
     */
    public int read() throws IOException {
        int c = in.read();
        if (c >= 0) monitor.setProgress(++nread);
        if (monitor.isCanceled()) {
            InterruptedIOException exc =
                                    new InterruptedIOException("progress");
            exc.bytesTransferred = nread;
            throw exc;
        }
        return c;
    }


    /**
     * Overrides <code>FilterInputStream.read</code> 
     * to update the progress monitor after the read.
     */
    public int read(byte b[]) throws IOException {
        int nr = in.read(b);
        if (nr > 0) monitor.setProgress(nread += nr);
        if (monitor.isCanceled()) {
            InterruptedIOException exc =
                                    new InterruptedIOException("progress");
            exc.bytesTransferred = nread;
            throw exc;
        }
        return nr;
    }


    /**
     * Overrides <code>FilterInputStream.read</code> 
     * to update the progress monitor after the read.
     */
    public int read(byte b[],
                    int off,
                    int len) throws IOException {
        int nr = in.read(b, off, len);
        if (nr > 0) monitor.setProgress(nread += nr);
        if (monitor.isCanceled()) {
            InterruptedIOException exc =
                                    new InterruptedIOException("progress");
            exc.bytesTransferred = nread;
            throw exc;
        }
        return nr;
    }


    /**
     * Overrides <code>FilterInputStream.skip</code> 
     * to update the progress monitor after the skip.
     */
    public long skip(long n) throws IOException {
        long nr = in.skip(n);
        if (nr > 0) monitor.setProgress(nread += nr);
        return nr;
    }


    /**
     * Overrides <code>FilterInputStream.close</code> 
     * to close the progress monitor as well as the stream.
     */
    public void close() throws IOException {
        in.close();
        monitor.close();
    }


    /**
     * Overrides <code>FilterInputStream.reset</code> 
     * to reset the progress monitor as well as the stream.
     */
    public synchronized void reset() throws IOException {
        in.reset();
        nread = size - in.available();
        monitor.setProgress(nread);
    }
}
