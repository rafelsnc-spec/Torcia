package it.torcia.app;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private CameraManager cameraManager;
    private String cameraId;
    private TextView stato;

    private final CameraManager.TorchCallback torchCallback = new CameraManager.TorchCallback() {
        @Override
        public void onTorchModeChanged(String id, boolean enabled) {
            if (id.equals(cameraId)) {
                stato.setText(enabled ? R.string.stato_on : R.string.stato_off);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stato = findViewById(R.id.stato);
        Button btnAccendi = findViewById(R.id.btnAccendi);
        Button btnSpegni = findViewById(R.id.btnSpegni);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Toast.makeText(this, R.string.no_flash, Toast.LENGTH_LONG).show();
            btnAccendi.setEnabled(false);
            btnSpegni.setEnabled(false);
            return;
        }

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        cameraId = trovaCameraConFlash();

        btnAccendi.setOnClickListener(v -> impostaTorcia(true));
        btnSpegni.setOnClickListener(v -> impostaTorcia(false));
    }

    private String trovaCameraConFlash() {
        try {
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                Boolean flash = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer facing = c.get(CameraCharacteristics.LENS_FACING);
                if (flash != null && flash
                        && facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    return id;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void impostaTorcia(boolean acceso) {
        if (cameraId == null) {
            Toast.makeText(this, R.string.no_flash, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // setTorchMode non richiede il permesso CAMERA
            cameraManager.setTorchMode(cameraId, acceso);
        } catch (CameraAccessException | IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (cameraManager != null) cameraManager.registerTorchCallback(torchCallback, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cameraManager != null) cameraManager.unregisterTorchCallback(torchCallback);
    }
}
