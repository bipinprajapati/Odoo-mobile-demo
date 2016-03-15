package Utils;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by Bipin Prajapati on 4/3/16.
 */
public class Util {

    public static void getStackTrace(final String TAG, final String TAG2) {
        final StackTraceElement[] steArray = Thread.currentThread().getStackTrace();
        int i = 0;
        for (StackTraceElement ste : steArray) {
            if (ste.getClassName().equals(TAG)) {
                break;
            }
            i++;
        }
        if (i >= steArray.length) {
            Log.v(TAG2, Arrays.toString(new String[]{steArray[3].getMethodName(), String.valueOf(steArray[3].getLineNumber())}));
        } else {
            Log.v(TAG2, Arrays.toString(new String[]{steArray[i].getMethodName(), String.valueOf(steArray[i].getLineNumber())}));
        }
    }
}
