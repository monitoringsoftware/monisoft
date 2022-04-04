package de.jmonitoring.utils.swing;

public abstract class EDTQuery<TYPE> implements Runnable {

    private TYPE result;

    public EDTQuery() {
        super();
        this.result = null;
    }

    @Override
    public void run() {
        this.result = perform();
    }

    public TYPE getResult() {
        return this.result;
    }

    protected abstract TYPE perform();
}
