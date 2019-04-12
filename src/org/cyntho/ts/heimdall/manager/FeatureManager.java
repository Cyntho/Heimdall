package org.cyntho.ts.heimdall.manager;

import org.cyntho.ts.heimdall.features.BaseFeature;

import java.util.ArrayList;
import java.util.List;

/**
 * The FeatureManager class, as the name suggests, manages
 * every registered feature (needs to implement BaseFeature).
 *
 */
public class FeatureManager {

    private List<BaseFeature> features;

    public FeatureManager(){
        features = new ArrayList<>();
    }

    public void register(BaseFeature f){
        if (!features.contains(f)){
            features.add(f);
        }
    }

    public void unregister(BaseFeature f){
        if (features.contains(f)){
            f.deactivate();
            features.remove(f);
        }
    }

    public List<BaseFeature> getFeatures(){
        return this.features;
    }

    public BaseFeature getFeatureById(long id){
        for (BaseFeature f : features){
            if (f.getId() == id){
                return f;
            }
        }
        return null;
    }

    public void activateAll(){
        for (BaseFeature f : features){
            f.activate();
        }
    }

    public void deactivateAll(){
        for (BaseFeature f : features){
           f.deactivate();
        }
    }


}
