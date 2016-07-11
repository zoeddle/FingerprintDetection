package com.example.carola.fingerprintdetection;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        canvas = new Canvas(mutableBitmap);

        permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        countNode = 0;

        if(!hasPermissions(MainActivity.this, permissions)){
            ActivityCompat.requestPermissions(MainActivity.this, permissions, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        }
        else {
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


                    if(!hasPermissions(MainActivity.this, permissions)){
                        ActivityCompat.requestPermissions(MainActivity.this, permissions, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
                    }
                    else {
                        //makeAndDrawNode();
                    }

//                    if (checkPermissions()) {
//                        //  permissions  granted.
//                        madAndDrawNode();
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
        Node test = new Node(x,y,String.valueOf(countNode),"bla", null);
        countNode++;

        if(positionManager != null){
            xmlPersistenceManager.addNodeData(test);
            positionManager.map(test.name);
            drawNode(x, y);
        }
        else {
            initializePositioning();
            return;
        }

    }
    private void drawNode(float x, float y){
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x, y, 25, paint);

        image.setImageBitmap(mutableBitmap);
    }

    private void drawRecievedNode(float x, float y){
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x, y, 25, paint);

        image.setImageBitmap(mutableBitmap);
    }

    private void initializePositioning() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "positioningPersistenceHTWog6.xml");

        try {
            //positionManager = new PositionManager(file);
            xmlPersistenceManager = new NewXMLPersistenceManager(file);
            positionManager = new PositionManager(xmlPersistenceManager);
            Log.d("positionManager", "initialized");
            List<String> positions = positionManager.getMappedPositions();
            if (positions != null){
                List<Node> actuallyNodes = new ArrayList<Node>();
                for(String nodeName : positions) {
                    Node nodeToAdd = xmlPersistenceManager.getNodeData(nodeName);
                    actuallyNodes.add(nodeToAdd);
                }
                for(int i = 0; i<actuallyNodes.size(); i++){
                    drawNode(actuallyNodes.get(i).x, actuallyNodes.get(i).y);
                }

//                ArrayAdapter adapter = new ArrayAdapter<Node>(this, R.layout.activity_listview,R.id.tv_listItem, actuallyNodes);
//
//                ListView listView = (ListView) findViewById(R.id.lv_nodes);
//                listView.setAdapter(adapter);


                NodeAdapter adapter = new NodeAdapter(this, (ArrayList<Node>) actuallyNodes);

                ListView listView = (ListView) findViewById(R.id.lv_nodes);
                listView.setAdapter(adapter);
            }
        } catch (PositioningPersistenceException e) {
            e.printStackTrace();
        }

        List<String> keyWhiteList = new ArrayList<String>();
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


    @Override
    public void positionReceived(PositionInformation positionInformation) {
//        Node recievedNode = xmlPersistenceManager.getNodeData(positionInformation.getName());
//        drawRecievedNode(recievedNode.x,recievedNode.y);
    }

    @Override
    public void positionReceived(List<PositionInformation> list) {
        String positionName = list.get(0).getName();
        Node recievedNode = xmlPersistenceManager.getNodeData(positionName);
        drawRecievedNode(recievedNode.x,recievedNode.y);
    }
}
