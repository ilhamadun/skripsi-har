package org.elins.aktvtas.human;


import org.elins.aktvtas.sensor.SensorDataSequence;

public class HumanActivityClassifier {
    static {
        System.loadLibrary("classifier");
    }

    public native int classify(SensorDataSequence sequence);
}
