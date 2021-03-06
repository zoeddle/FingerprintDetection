package com.example.carola.fingerprintdetection;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import de.hadizadeh.positioning.controller.PositionListener;
import de.hadizadeh.positioning.controller.PositionManager;
import de.hadizadeh.positioning.controller.Technology;
import de.hadizadeh.positioning.exceptions.PositioningException;
import de.hadizadeh.positioning.exceptions.PositioningPersistenceException;
import de.hadizadeh.positioning.model.PositionInformation;

public class MainActivity extends AppCompatActivity implements PositionListener {

    private PositionManager positionManager;
    private NewXMLPersistenceManager xmlPersistenceManager;
    private ImageView image;
    private ListView listView;
    private Bitmap bitmap;
    private Bitmap mutableBitmap;
    private Canvas canvas;
    private static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 3;
    private float x, y;
    String[] permissions;
    int countNode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView) findViewById(R.id.i_floorPlan);
        bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        final Button startScanButton = (Button) findViewById(R.id.b_startScann);
        final Button stoppScanButton = (Button) findViewById(R.id.b_stoppScan);
        listView = (ListView) findViewById(R.id.lv_nodes);

        mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        canvas = new Canvas(mutableBitmap);

        permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        countNode = 0;

        if (!hasPermissions(MainActivity.this, permissions)) {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        } else {
            initializePositioning();
        }

        startScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                positionManager.startPositioning(1000);
            }
        });

        stoppScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                positionManager.stopPositioning();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                Node clickedNode = (Node) listView.getItemAtPosition(position);
                if (positionManager != null) {
                    xmlPersistenceManager.addNodeData(clickedNode);
                    positionManager.map(clickedNode.name);
                    drawRecievedNode(clickedNode.x, clickedNode.y);
                } else {
                    initializePositioning();
                    return;
                }
            }
        });

//        if (checkPermissions()) {
//            //  permissions  granted.
//            initializePositioning();
//        }
//        else{
//            new AlertDialog.Builder(MainActivity.this)
//                    .setTitle("Inform and request")
//                    .setMessage("You need to enable permissions")
//                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            checkPermissions();
//                        }
//                    })
//                    .show();
//        }

        image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //draw a cirle in Bitmap (Canvas)
                    x = event.getX();
                    y = event.getY();

                    if (!hasPermissions(MainActivity.this, permissions)) {
                        ActivityCompat.requestPermissions(MainActivity.this, permissions, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
                    } else {
                        //makeAndDrawNode();
                    }

//                    if (checkPermissions()) {
//                        //  permissions  granted.
//                        makeAndDrawNode();
//                    }
//                    else{
//                        new AlertDialog.Builder(MainActivity.this)
//                                .setTitle("Inform and request")
//                                .setMessage("You need to enable permissions")
//                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        checkPermissions();
//                                    }
//                                })
//                                .show();
//                    }

                }
                return true;
            }

        });
    }


    private boolean checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result;
            List<String> listPermissionsNeeded = new ArrayList<>();
            for (String p : permissions) {
                result = ContextCompat.checkSelfPermission(MainActivity.this, p);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(p);
                }
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
                return false;
            }
            return true;
        } else {
            return true;
        }
    }

    private boolean hasPermissions(Context context, String[] permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void makeAndDrawNode() {

        //add a node to xml file
        Node neighbour = new Node(x, y, "2", "bla", null);
        List<Node> neigbours = new ArrayList<>();
        neigbours.add(neighbour);
        Node test = new Node(x, y, String.valueOf(countNode), "bla", neigbours);
        countNode++;

        if (positionManager != null) {
            xmlPersistenceManager.addNodeData(test);
            positionManager.map(test.name);
            drawNode(x, y);
        } else {
            initializePositioning();
            return;
        }

    }

    private void drawNode(float x, float y) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x, y, 25, paint);

        image.setImageBitmap(mutableBitmap);
    }

    private void drawRecievedNode(float x, float y) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x, y, 25, paint);

        image.setImageBitmap(mutableBitmap);
    }

    private void initializePositioning() {
        Calendar calender = Calendar.getInstance();
        SimpleDateFormat strFormat = new SimpleDateFormat("yyyy-MM-dd HH");
        String strDate = strFormat.format(calender.getTime());
        //File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "myHome2.xml");
        File informationFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), strDate + " og6Information.xml");

        findNodesFromJson();

        try {
            xmlPersistenceManager = new NewXMLPersistenceManager(informationFile);
            positionManager = new PositionManager(xmlPersistenceManager);
            Log.d("positionManager", "initialized");
//            List<String> positions = positionManager.getMappedPositions();
//            if (positions != null){
//                List<Node> actuallyNodes = new ArrayList<Node>();
//                for(String nodeName : positions) {
//                    Node nodeToAdd = xmlPersistenceManager.getNodeData(nodeName);
//                    actuallyNodes.add(nodeToAdd);
//                }
//                for(int i = 0; i<actuallyNodes.size(); i++){
//                    drawNode(actuallyNodes.get(i).x, actuallyNodes.get(i).y);
//                }
//
////                NodeAdapter adapter = new NodeAdapter(this, (ArrayList<Node>) actuallyNodes);
////
////                ListView listView = (ListView) findViewById(R.id.lv_nodes);
////                listView.setAdapter(adapter);
//            }
        } catch (PositioningPersistenceException e) {
            e.printStackTrace();
        }

        List<String> keyWhiteList = new ArrayList<String>();

        keyWhiteList.addAll(getMacAdresses());
        //weihiteList Julian
//        keyWhiteList.add("88:03:55:0b:22:44".toLowerCase());
//        keyWhiteList.add("bc:05:43:b4:3d:72".toLowerCase());
//        keyWhiteList.add("34:81:c4:f9:22:b5".toLowerCase());
//        keyWhiteList.add("5c:35:3b:ef:e0:ec".toLowerCase());
//        keyWhiteList.add("e8:37:7a:1a:56:5b".toLowerCase());
//        keyWhiteList.add("34:81:c4:c7:46:50".toLowerCase());
        //whiteList zuHause
        keyWhiteList.add("58:8b:f3:50:da:b1".toLowerCase());
        keyWhiteList.add("18:83:bf:d1:ff:72".toLowerCase());
        keyWhiteList.add("00:1e:be:8c:d6:a0".toLowerCase());
        //whiteList HTW
//        keyWhiteList.add("00:19:07:c4:91:51".toLowerCase());
//        keyWhiteList.add("00:19:07:97:63:81".toLowerCase());
//        keyWhiteList.add("00:19:07:c4:8f:b1".toLowerCase());
//        keyWhiteList.add("00:19:07:c4:9a:71".toLowerCase());
//        keyWhiteList.add("00:19:07:c4:9d:61".toLowerCase());
//        keyWhiteList.add("00:19:07:97:fb:81".toLowerCase());
//        keyWhiteList.add("00:19:07:c4:9e:d1".toLowerCase());
//        keyWhiteList.add("00:19:07:c4:99:81".toLowerCase());
//        keyWhiteList.add("00:19:07:c4:9b:01".toLowerCase());
//        keyWhiteList.add("00:19:07:c4:9e:d1".toLowerCase());
//        keyWhiteList.add("00:19:07:c4:99:91".toLowerCase());
//        keyWhiteList.add("00:19:07:c4:9e:71".toLowerCase());
//        keyWhiteList.add("00:19:07:c4:9f:e1".toLowerCase());
//        keyWhiteList.add("00:19:07:c4:9d:a1".toLowerCase());
//        keyWhiteList.add("00:19:07:c4:8f:01".toLowerCase());
//        keyWhiteList.add("00:19:07:c4:8d:b1".toLowerCase());
        Technology wifiTechnology = new WifiTechnology(this, "WIFI", keyWhiteList);

        try {
            positionManager.addTechnology(wifiTechnology);
        } catch (PositioningException e) {
            e.printStackTrace();
        }
        positionManager.registerPositionListener(this);
    }


    private List<String> getMacAdresses() {
        List<String> macAddresses = new ArrayList<>();
        try {
            List<String> assetList = Arrays.asList(this.getAssets().list(""));
            for (String fileName : assetList) {
                if (fileName.toLowerCase().contains("bssid")) {
                    //readMacAdressesFormTxT(fileName, macAddresses);
                    readMacAddressesFromXls(fileName, macAddresses);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return macAddresses;
    }

    private void readMacAdressesFormTxT(String fileName, List<String> dest) throws IOException {
        InputStream inputStream = this.getAssets().open(fileName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = bufferedReader.readLine()) != null && line.length() != 0) {
            if (!dest.contains(line.toLowerCase())) {
                dest.add(line.toLowerCase());
            }
        }

    }

    private void readMacAddressesFromXls(String filename, List<String> dest) throws IOException {
        InputStream inputStream = this.getAssets().open(filename);
        POIFSFileSystem poifsFileSystem = new POIFSFileSystem(inputStream);
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(poifsFileSystem);
        HSSFSheet sheet = hssfWorkbook.getSheetAt(0);
        HSSFRow row;
        HSSFCell cell;

        Iterator<Row> rowIterator = sheet.rowIterator();
        //TODO Clean this up quick way to skip the heading row
        rowIterator.next();

        while (rowIterator.hasNext()) {
            row = (HSSFRow) rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                cell = (HSSFCell) cellIterator.next();
                if (cell.getColumnIndex() == 4) {
                    String address = addSeparator(cell.toString());
                    if (! address.equals("")){
                        dest.add(address);
                    }
                }
            }
        }

    }


    private String addSeparator(String macAddress) {

        StringBuilder addressWithSeparator = new StringBuilder();

        for (int i = 0; i < macAddress.length(); i++) {
            if (i % 2 == 0 && i != 0) {
                addressWithSeparator.append(":");
            }
            addressWithSeparator.append(macAddress.charAt(i));
        }

        return addressWithSeparator.toString();
    }

    private void findNodesFromJson() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("og6InformationJson.txt");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

            JSONObject jsonRootObject = new JSONObject(json);

            //Get the instance of JSONArray that contains JSONObjects
            JSONArray jsonArray = jsonRootObject.optJSONArray("nodePoints");

            List<Node> actuallyNodes = new ArrayList<Node>();

            //Iterate the jsonArray and print the info of JSONObjects
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String name = jsonObject.optString("name").toString();
                String searchname = jsonObject.optString("searchName").toString();
                float x = Float.parseFloat(jsonObject.optString("x").toString());
                float y = Float.parseFloat(jsonObject.optString("y").toString());
                ArrayList<String> neighbourStrings = new ArrayList<>();
                for (int j = 0; j < jsonObject.getJSONArray("neighbours").length(); j++) {
                    neighbourStrings.add(jsonObject.getJSONArray("neighbours").get(j).toString());
                }
                actuallyNodes.add(new Node(x, y, name, searchname, neighbourStrings));
            }

            for (int i = 0; i < actuallyNodes.size(); i++) {
                drawNode(actuallyNodes.get(i).x, actuallyNodes.get(i).y);
            }

            NodeAdapter adapter = new NodeAdapter(this, (ArrayList<Node>) actuallyNodes);

            listView.setAdapter(adapter);

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void positionReceived(PositionInformation positionInformation) {
//        Node recievedNode = xmlPersistenceManager.getNodeData(positionInformation.getName());
//        drawRecievedNode(recievedNode.x,recievedNode.y);
    }

    @Override
    public void positionReceived(List<PositionInformation> list) {
        String positionName = list.get(0).getName();
        Node recievedNode = xmlPersistenceManager.getNodeData(positionName);
        drawRecievedNode(recievedNode.x, recievedNode.y);
    }
}
