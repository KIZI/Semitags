package cz.ilasek.namedentities.set;

import java.util.HashSet;
import java.util.Set;

public class SurfaceFormSet {
    
    private static SurfaceFormSet instance = new SurfaceFormSet();
    
    private Set<String> surfaceForms = new HashSet<String>();
    
    private SurfaceFormSet() {
        
    }
    
    public static SurfaceFormSet getInstance() {
        return instance;
    }
    
    public Set<String> getSurfaceForms() {
        return surfaceForms;
    }

    public synchronized boolean addSurfaceForm(String surfaceForm) {
        return surfaceForms.add(surfaceForm);
    }
    

}
