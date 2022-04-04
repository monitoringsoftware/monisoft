package de.jmonitoring.utils.AnnotationEditor;

import java.awt.BasicStroke;

/**
 * A enum holding the strokes available for annotation lines or outlines
 * @author togro
 */
public enum AnnotationStrokes {

    DASH_1() {
        @Override
        public BasicStroke getStroke() {
            return new BasicStroke(1f);
        }
    },
    DASH_2() {
        @Override
        public BasicStroke getStroke() {
            float dash[] = {4f};
            return new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dash, 0f);
        }
    },
    DASH_3() {
        @Override
        public BasicStroke getStroke() {
            float dash[] = {10f};
            return new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dash, 0f);
        }
    },
    DASH_4() {
        @Override
        public BasicStroke getStroke() {
            float dash[] = {11f, 6f, 2f, 6f};
            return new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dash, 0f);
        }
    };

    public abstract BasicStroke getStroke();
}
