package com.example.carola.fingerprintdetection;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.hadizadeh.positioning.exceptions.PositioningPersistenceException;
import de.hadizadeh.positioning.model.PositionInformation;
import de.hadizadeh.positioning.model.SignalInformation;
import de.hadizadeh.positioning.persistence.PersistenceManager;

public class NewXMLPersistenceManager implements PersistenceManager{

    protected File persistenceFile;
    protected SAXBuilder saxBuilder = new SAXBuilder();
    protected Map<String,Node> nodeData = new HashMap<>();

    public NewXMLPersistenceManager(File persistenceFile) {
        this.persistenceFile = persistenceFile;
        if(!persistenceFile.exists()) {
            Document doc = new Document();
            doc.setRootElement(new Element("positionManagerData"));

            try {
                this.save(doc);
            } catch (IOException var4) {
                var4.printStackTrace();
            }
        }

    }

    public Map<String, List<PositionInformation>> getPersistedPositions() throws PositioningPersistenceException {
        HashMap positionInformation = new HashMap();

        try {
            Document e = this.open();
            Element root = e.getRootElement();
            List fingerPrints = root.getChildren();
            Iterator i$ = fingerPrints.iterator();

            while(i$.hasNext()) {
                Element fingerPrint = (Element)i$.next();
                Node node = createNodeFromElement(fingerPrint);
                nodeData.put(fingerPrint.getAttributeValue("name"),node );
                new ArrayList();

                String technologyName;
                HashMap signalInformationData;
                for(Iterator i$1 = fingerPrint.getChildren().iterator(); i$1.hasNext();
                    ((List)positionInformation.get(technologyName)).add(new PositionInformation(fingerPrint.getAttributeValue("name"), signalInformationData))) {
                    Element technology = (Element)i$1.next();
                    technologyName = technology.getAttributeValue("name");
                    signalInformationData = new HashMap();
                    Iterator i$2 = technology.getChildren().iterator();

                    while(i$2.hasNext()) {
                        Element signalInformation = (Element)i$2.next();
                        signalInformationData.put(signalInformation.getAttributeValue("id"), new SignalInformation(Double.parseDouble(signalInformation.getValue())));
                    }

                    if(!positionInformation.containsKey(technologyName)) {
                        positionInformation.put(technologyName, new ArrayList());
                    }
                }
            }

            return positionInformation;
        } catch (Exception var14) {
            throw new PositioningPersistenceException(var14.getMessage());
        }
    }

    public void persistPosition(String technologyName, PositionInformation positionInformation) throws PositioningPersistenceException {
        try {
            Document e = this.open();
            Element root = e.getRootElement();
            root = this.removeOldTechnologyData(root, technologyName, positionInformation);
            Element fingerPrint = null;
            Iterator i$ = root.getChildren().iterator();

            while(i$.hasNext()) {
                Element entry = (Element)i$.next();
                if(positionInformation.getName().equals(entry.getAttributeValue("name"))) {
                    fingerPrint = entry;
                    break;
                }
            }

            if(fingerPrint == null) {
                Node tempNode = nodeData.get(positionInformation.getName());
                fingerPrint = new Element("fingerPrint");
                fingerPrint.setAttribute("name", positionInformation.getName());
                fingerPrint.setAttribute("searchName", tempNode.searchName);
                fingerPrint.setAttribute("x", String.valueOf(tempNode.x));
                fingerPrint.setAttribute("y", String.valueOf(tempNode.y));
                root.addContent(fingerPrint);
            }

            this.addFingerPrint(fingerPrint, technologyName, positionInformation);
            this.save(e);
        } catch (Exception var8) {
            throw new PositioningPersistenceException(var8.getMessage());
        }
    }

    public void removeMappedPosition(String name) throws PositioningPersistenceException {
        try {
            Document e = this.open();
            Element root = e.getRootElement();
            Iterator itr = root.getChildren().iterator();

            while(itr.hasNext()) {
                Element child = (Element)itr.next();
                if(name.equals(child.getAttributeValue("name"))) {
                    itr.remove();
                }
            }

            this.save(e);
        } catch (Exception var6) {
            throw new PositioningPersistenceException(var6.getMessage());
        }
    }

    public void removeMappedPosition(String name, String technology) throws PositioningPersistenceException {
        try {
            boolean e = false;
            Document doc = this.open();
            Element root = doc.getRootElement();
            Iterator i$ = root.getChildren().iterator();

            while(true) {
                Element element;
                do {
                    if(!i$.hasNext()) {
                        this.save(doc);
                        if(e) {
                            this.removeMappedPosition(name);
                        }

                        return;
                    }

                    element = (Element)i$.next();
                } while(!name.equals(element.getAttributeValue("name")));

                Iterator itr = element.getChildren().iterator();

                while(itr.hasNext()) {
                    Element child = (Element)itr.next();
                    if(technology.equals(child.getAttributeValue("name"))) {
                        itr.remove();
                    }
                }

                if(element.getChildren().size() == 0) {
                    e = true;
                }
            }
        } catch (Exception var10) {
            throw new PositioningPersistenceException(var10.getMessage());
        }
    }

    public void removeAllMappedPositions() throws PositioningPersistenceException {
        try {
            Document e = this.open();
            Element root = e.getRootElement();
            root.removeContent();
            this.save(e);
        } catch (Exception var3) {
            throw new PositioningPersistenceException(var3.getMessage());
        }
    }

    protected Element removeOldTechnologyData(Element root, String technologyName, PositionInformation positionInformation) {
        List entries = root.getChildren();
        Iterator i$ = entries.iterator();

        while(true) {
            Element entry;
            do {
                if(!i$.hasNext()) {
                    return root;
                }

                entry = (Element)i$.next();
            } while(!positionInformation.getName().equals(entry.getAttributeValue("name")));

            Iterator itr = entry.getChildren().iterator();

            while(itr.hasNext()) {
                Element child = (Element)itr.next();
                if(technologyName.equals(child.getAttributeValue("name"))) {
                    itr.remove();
                }
            }
        }
    }

    protected void addFingerPrint(Element fingerPrint, String technologyName, PositionInformation positionInformation) {
        Element technology = new Element("technology");
        technology.setAttribute("name", technologyName);
        Iterator i$ = positionInformation.getSignalInformation().entrySet().iterator();

        while(i$.hasNext()) {
            Map.Entry signalInformation = (Map.Entry)i$.next();
            Element signalElement = new Element("signalInformation");
            signalElement.setAttribute("id", (String)signalInformation.getKey());
            signalElement.setText(String.valueOf(((SignalInformation)signalInformation.getValue()).getStrength()));
            technology.addContent(signalElement);
        }

        fingerPrint.addContent(technology);
    }

    protected Document open() throws JDOMException, IOException {
        return this.saxBuilder.build(this.persistenceFile);
    }

    protected void save(Document doc) throws IOException {
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.persistenceFile), "UTF-8"));
        xmlOutput.output(doc, out);
        out.flush();
        out.close();
    }

    public void addNodeData(Node newNode){
        nodeData.put(newNode.name, newNode);
    }

    public Node getNodeData(String nodeName){
        return nodeData.get(nodeName);
    }

    protected Node createNodeFromElement(Element fingerPrint){
        float x = Float.parseFloat(fingerPrint.getAttributeValue("x"));
        float y = Float.parseFloat(fingerPrint.getAttributeValue("y"));
        return new Node(x,y,fingerPrint.getAttributeValue("name"),fingerPrint.getAttributeValue("searchName"),null);
    }
}
