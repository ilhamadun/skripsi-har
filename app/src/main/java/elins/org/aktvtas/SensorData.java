package elins.org.aktvtas;

import java.util.Arrays;
import java.util.List;

public class SensorData {

    private List<Float> values;
    private int sensorType;

    protected SensorData(int SensorType, int numberOfAxis) {
        this.sensorType = SensorType;

        Float[] initialData = new Float[numberOfAxis];
        values = Arrays.asList(initialData);
    }

    public int numberOfAxis() {
        return values.size();
    }

    public int sensorType() {
        return sensorType;
    }

    public void setAxisValue(int axis, Float value) {
        values.set(axis, value);
    }

    public void setValues(List<Float> values) {
        int inputNumberOfAxis = values.size();
        confirmNumberOfAxisMatchesWith(inputNumberOfAxis);

        for (int i = 0; i < this.values.size(); i++) {
            this.values.set(i, values.get(i));
        }
    }

    public void setValues(Float[] values) {
        int inputNumberOfAxis = values.length;
        confirmNumberOfAxisMatchesWith(inputNumberOfAxis);

        for (int i = 0; i < this.values.size(); i++) {
            this.values.set(i, values[i]);
        }
    }

    private void confirmNumberOfAxisMatchesWith(int inputNumberOfAxis) {
        int expectedNumberOfAxis = values.size();
        if (inputNumberOfAxis > expectedNumberOfAxis) {
            throw new IndexOutOfBoundsException("Input number of axis is bigger than expected.");
        } else if (inputNumberOfAxis < expectedNumberOfAxis) {
            throw new IndexOutOfBoundsException("Input number of axis is smaller than expected.");
        }
    }

    public Float getAxisValue(int axis) {
        return values.get(axis);
    }

    public List<Float> getValues() {
        return values;
    }

    public void resetValues() {
        for (int i = 0; i < values.size(); i++) {
            values.set(i, 0f);
        }
    }
}
