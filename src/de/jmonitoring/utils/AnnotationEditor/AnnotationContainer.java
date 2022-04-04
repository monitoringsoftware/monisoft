package de.jmonitoring.utils.AnnotationEditor;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A container class holding all {@link AnnotationElement}s of an annotation in
 * an {@link ArrayList}.
 *
 * @author togro
 */
public class AnnotationContainer {

    private static final long serialVersionUID = 1L;
    private String annotationName = null;
    private ArrayList<AnnotationElement> annotations = new ArrayList<AnnotationElement>();

    /**
     * Creates a new instance with the given name and collection of
     * {@link AnnotationElement}s
     *
     * @param name the name of the annotation
     * @param annotationElements a list of all elements making up the annotation
     */
    public AnnotationContainer(String name, ArrayList<AnnotationElement> annotationElements) {
        this.annotations = annotationElements;
        this.annotationName = name;
    }

    /**
     * Creates a new instance with the given name and an empty collection.
     *
     * @param name the name of the annotation
     */
    public AnnotationContainer(String name) {
        this.annotationName = name;
    }

    /**
     * Returns a list of all {@link AnnotationElement}s of this annotation
     *
     * @return
     */
    public ArrayList<AnnotationElement> getAnnotationElements() {
        return annotations;
    }

    /**
     * Sets the list of {@link AnnotationElement}s to the given collection
     *
     * @return
     */
    public void setAnnotationElements(ArrayList<AnnotationElement> annotationElements) {
        this.annotations = annotationElements;
    }

    /**
     * Returns the name of this annotationContainer
     *
     * @return
     */
    public String getName() {
        return annotationName;
    }

    /**
     * Set the name of this annotationContainer
     *
     * @param Name
     */
    public void setName(String Name) {
        this.annotationName = Name;
    }

    /**
     * Adds a new {@link AnnotationElement} to the list
     *
     * @param element {@link AnnotationElement} to be added
     */
    public void addAnnotationElement(AnnotationElement element) {
        annotations.add(element);
    }
}
