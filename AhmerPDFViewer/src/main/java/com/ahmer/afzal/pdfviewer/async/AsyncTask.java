package com.ahmer.afzal.pdfviewer.async;

import java.util.concurrent.ExecutorService;

public abstract class AsyncTask<INPUT, PROGRESS, OUTPUT> {
    private boolean cancelled = false;
    private OnProgressListener<PROGRESS> onProgressListener;
    private OnCancelledListener onCancelledListener;

    public AsyncTask() {

    }

    /**
     * @see #execute(Object)
     */
    public AsyncTask<INPUT, PROGRESS, OUTPUT> execute() {
        return execute(null);
    }

    /**
     * Starts is all
     *
     * @param input Data you want to work with in the background
     */
    public AsyncTask<INPUT, PROGRESS, OUTPUT> execute(final INPUT input) {
        onPreExecute();

        ExecutorService executorService = AsyncWorker.getInstance().getExecutorService();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final OUTPUT output = AsyncTask.this.doInBackground(input);
                    AsyncWorker.getInstance().getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            AsyncTask.this.onPostExecute(output);
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();

                    AsyncWorker.getInstance().getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            AsyncTask.this.onBackgroundError(e);
                        }
                    });
                }
            }
        });

        return this;
    }

    /**
     * Call to publish progress from background
     *
     * @param progress Progress made
     */
    protected void publishProgress(final PROGRESS progress) {
        AsyncWorker.getInstance().getHandler().post(new Runnable() {
            @Override
            public void run() {
                onProgress(progress);
                if (onProgressListener != null) {
                    onProgressListener.onProgress(progress);
                }
            }
        });
    }

    protected void onProgress(final PROGRESS progress) {

    }

    /**
     * Call to cancel background work
     */
    public void cancel() {
        cancelled = true;
    }

    /**
     * @return Returns true if the background work should be cancelled
     */
    protected boolean isCancelled() {
        return cancelled;
    }

    /**
     * Call this method after cancelling background work
     */
    protected void onCancelled() {
        AsyncWorker.getInstance().getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (onCancelledListener != null) {
                    onCancelledListener.onCancelled();
                }
            }
        });
    }

    /**
     * Work which you want to be done on UI thread before {@link #doInBackground(Object)}
     */
    protected void onPreExecute() {

    }

    /**
     * Work on background
     *
     * @param input Input data
     * @return Output data
     * @throws Exception Any uncought exception which occurred while working in background. If
     *                   any occurs, {@link #onBackgroundError(Exception)} will be executed (on the UI thread)
     */
    protected abstract OUTPUT doInBackground(INPUT input);

    /**
     * Work which you want to be done on UI thread after {@link #doInBackground(Object)}
     *
     * @param output Output data from {@link #doInBackground(Object)}
     */
    protected void onPostExecute(OUTPUT output) {

    }

    /**
     * Triggered on UI thread if any uncought exception occurred while working in background
     *
     * @param e Exception
     * @see #doInBackground(Object)
     */
    protected abstract void onBackgroundError(Exception e);

    public void setOnProgressListener(OnProgressListener<PROGRESS> onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    public void setOnCancelledListener(OnCancelledListener onCancelledListener) {
        this.onCancelledListener = onCancelledListener;
    }

    public interface OnProgressListener<PROGRESS> {
        void onProgress(PROGRESS progress);
    }

    public interface OnCancelledListener {
        void onCancelled();
    }
}
