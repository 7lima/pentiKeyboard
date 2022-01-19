// 30apr21abu
// (c) Software Lab. Alexander Burger

package de.software_lab.pentikeyboard;

import android.view.*;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class PentiIME extends InputMethodService implements SensorEventListener {
   PentiView PV;
   Sensor Gyroscope;
   SensorManager SMan;
   boolean Shown, Click;
   int GyroX, GyroY, GyroZ;

   @Override public View onCreateInputView() {
      return null;
   }

   @Override public View onCreateCandidatesView() {  // http://stackoverflow.com/a/20319466/1160216
      PV = (PentiView)getLayoutInflater().inflate(R.layout.input, null);
      PV.Ime = this;
      PV.reset();
      return PV;
   }

   @Override public void onStartInputView(android.view.inputmethod.EditorInfo info, boolean restarting) {
      super.onStartInputView(info, restarting);
      setCandidatesViewShown(true);
   }

   @Override public boolean onEvaluateFullscreenMode() {
      return false;
   }

   @Override public void onWindowHidden() {
      Shown = false;
   }

   @Override public void onWindowShown() {
      Shown = true;
   }

   public void onAccuracyChanged(Sensor sensor, int accuracy) {}

   public void onSensorChanged(SensorEvent event) {
      int x = (int)(event.values[0] * 1000.0);
      int y = (int)(event.values[1] * 1000.0);
      int z = (int)(event.values[2] * 1000.0);

      if (x != GyroX  ||  y != GyroY  ||  z != GyroZ) {
         PV.text("Δ" +
            Math.abs(x) +
            (x >= 0? '+' : '-') +
            Math.abs(y) +
            (y >= 0? '+' : '-') +
            Math.abs(z) +
            (z >= 0? '+' : '-') );
         GyroX = x;
         GyroY = y;
         GyroZ = z;
      }
   }

   @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
      if (Shown  &&  (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
         if (Click = event.getRepeatCount() == 0) {
            if (SMan == null)
               Gyroscope = (SMan = (SensorManager)getSystemService(Context.SENSOR_SERVICE)).getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            SMan.registerListener(this, Gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
         }
         return true;
      }
      return super.onKeyDown(keyCode, event);
   }

   @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
      if (Shown  &&  (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
         if (Click)
            PV.text("Δ" + (keyCode == KeyEvent.KEYCODE_VOLUME_UP? "+" : "-"));
         SMan.unregisterListener(this);
         return true;
      }
      return super.onKeyUp(keyCode, event);
   }

   @Override public void requestHideSelf(int flags) {
      super.requestHideSelf(flags);
      PV.setBackgroundResource(0);
      PV.Help = null;
      PV.reset();
   }

   @Override public void onFinishInput() {
      setCandidatesViewShown(false);
      super.onFinishInput();
   }
}
