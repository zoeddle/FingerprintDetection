package com.example.carola.fingerprintdetection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Carola on 04.06.16.
 */
public class Node {
    public float x;
    public float y;
    public String name;
    public List<Node> neighbours = new ArrayList<>();

    public Node(float x, float y, String name, List<Node> neighbours) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.neighbours = neighbours;
    }

}
