package com.example.carola.fingerprintdetection;

import java.util.List;
import de.hadizadeh.positioning.controller.Technology;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import de.hadizadeh.positioning.model.SignalInformation;
import java.util.HashMap;
import java.util.Map;

public class WifiTechnology extends Technology {
    private WifiManager wifiManager;

    public WifiTechnology(Context context, String name, List<String> keyWhiteList) {
        super(name, keyWhiteList);
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public Map<String, SignalInformation> getSignalData() {
        Map<String, SignalInformation> signalData =
                new HashMap<String, SignalInformation>();

        wifiManager.startScan();
        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult scanResult : scanResults) {
            signalData.put(scanResult.BSSID, new SignalInformation(scanResult.level));
        }
        return signalData;
    }
}
