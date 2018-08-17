package com.example.wiser.ILOSDataCollection;

//TODO changes here are not recommended. Filters results from the accelerometer data to predict step events

public class SensorFilter {
    /**
     * Blank constructor
     */
    private SensorFilter() {
    }

    /**
     * Filters out data from the pedometer
     * @param array
     * @return
     */
    public static float sum(float[] array) {
        float retval = 0;
        for (int i = 0; i < array.length; i++) {
            retval += array[i];
        }
        return retval;
    }

    /**
     * Finds cross product of A and B matrices that are inputs
     * @param arrayA
     * @param arrayB
     * @return
     */
    public static float[] cross(float[] arrayA, float[] arrayB) {
        float[] retArray = new float[3];
        retArray[0] = arrayA[1] * arrayB[2] - arrayA[2] * arrayB[1];
        retArray[1] = arrayA[2] * arrayB[0] - arrayA[0] * arrayB[2];
        retArray[2] = arrayA[0] * arrayB[1] - arrayA[1] * arrayB[0];
        return retArray;
    }

    /**
     * Finds matric magnitude
     * @param array
     * @return
     */
    public static float norm(float[] array) {
        float retval = 0;
        for (int i = 0; i < array.length; i++) {
            retval += array[i] * array[i];
        }
        return (float) Math.sqrt(retval);
    }

    /**
     * Applies dot product to each component of the two matrices
     * @param a
     * @param b
     * @return
     */
    public static float dot(float[] a, float[] b) {
        float retval = a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
        return retval;
    }

    /**
     * Normalizes a matrix a
     * @param a
     * @return
     */
    public static float[] normalize(float[] a) {
        float[] retval = new float[a.length];
        float norm = norm(a);
        for (int i = 0; i < a.length; i++) {
            retval[i] = a[i] / norm;
        }
        return retval;
    }
}
