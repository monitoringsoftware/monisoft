/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.Components;

import de.jmonitoring.utils.swing.EDT;
import de.jmonitoring.utils.swing.EDTQuery;
import java.awt.event.ActionListener;

/**
 *
 * @author dsl
 */
public class MoniSoftProgressBar {

    private final MoniSoftProgressBarWidget progressBar;
    
    public MoniSoftProgressBar(final String name) {
        super();
        EDT.whatever();
        this.progressBar = EDT.performQuery(new EDTQuery<MoniSoftProgressBarWidget>() {
            @Override
            protected MoniSoftProgressBarWidget perform() {
                return new MoniSoftProgressBarWidget(name);
            }
        });
    }
    
    public MoniSoftProgressBarWidget asWidget() {
        return bar();
    }
    
    protected MoniSoftProgressBarWidget bar() {
        return this.progressBar;
    }
    
    public void setValue(final int v) {
        EDT.performBlocking(new Runnable() {
            @Override
            public void run() {
                bar().setValue(v);
            }
        });
    }

    public void setText(final String s) {
        EDT.performBlocking(new Runnable() {
            @Override
            public void run() {
                bar().setText(s);
            }
        });
    }

    public void addProgressCancelButtonActionListener(final ActionListener action) {
        EDT.performBlocking(new Runnable() {
            @Override
            public void run() {
                bar().addProgressCancelButtonActionListener(action);
            }
        });
    }

    public void removeProgressCancelButtonActionListener(final ActionListener action) {
        EDT.performBlocking(new Runnable() {
            @Override
            public void run() {
                bar().removeProgressCancelButtonActionListener(action);
            }
        });
    }

    public void setMinMax(final int min, final int max) {
        EDT.performBlocking(new Runnable() {
            @Override
            public void run() {
                bar().setMinMAx(min, max);
            }
        });
    }

    public void setIndeterminate(final boolean indeterminate) {
        EDT.performBlocking(new Runnable() {
            @Override
            public void run() {
                bar().setIndeterminate(indeterminate);
            }
        });
    }

    public void reset() {
        EDT.performBlocking(new Runnable() {
            @Override
            public void run() {
                bar().reset();
            }
        });
    }

    public void remove() {
        EDT.performBlocking(new Runnable() {
            @Override
            public void run() {
                try {
                    ((ProgressBarPanel) bar().getParent()).removeProgressBar(MoniSoftProgressBar.this);
                } catch (NullPointerException e) {
                }
            }
        });
    }
}
