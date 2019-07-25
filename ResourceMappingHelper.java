package com.example.testapp;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Helper Class which can create XML resource from CSV file
 * where csv contain 2 column first is english word and second is translation in other language
 * it maps source string from default xml and create new xml
 */
public class ResourceMappingHelper {

    private static final String DEFAULT_FILE_PATH  = "string_generated.xml";

    private String csvSourcePath;
    private String defaultStringXmlPath;
    private String xmlDestinationPath;
    private Map<String, String> map;
    private Map<String, List<String>> stringListMap;
    private int totalTranslation;

    /**
     * Constructor
     * @param csvSourcePath full path of csv file which contain translation
     * @param defaultStringXmlPath full path of default string resource xml
     * @param xmlDestinationPath full path where generated file will be saved
     */
    public ResourceMappingHelper(String csvSourcePath, String defaultStringXmlPath, String xmlDestinationPath) {
        this.csvSourcePath = csvSourcePath;
        this.defaultStringXmlPath = defaultStringXmlPath;
        this.xmlDestinationPath = xmlDestinationPath;
        generateStringMapping();
    }

    /**
     * Constructor in case destination path will be current directory
     * @param csvSourcePath full path of csv file which contain translation
     * @param defaultStringXmlPath full path of default string resource xml
     */
    public ResourceMappingHelper(String csvSourcePath, String defaultStringXmlPath) {
        this(csvSourcePath, defaultStringXmlPath, new File(DEFAULT_FILE_PATH).getAbsolutePath());
    }

    /**
     * Create a mapping between default text and translated text
     * for example english(key) --- spanish(value)
     */
    private void generateStringMapping() {
        map = new HashMap<>();
        stringListMap = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(csvSourcePath));
            String line;
            while ((line = br.readLine()) !=null) {
                Iterable<String> iterable = Splitter.on(Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")).split(line);
                String[] data = Iterables.toArray(iterable, String.class);
                if (data.length < 2) {
                    throw new IllegalArgumentException("CSV should contain at least 2 comma separated values");
                }
                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].replaceAll("^\"|\"$", "");
                }
                totalTranslation = data.length > 2 ? data.length -1 : 1;
                stringListMap.put(data[0], Arrays.asList(Arrays.copyOfRange(data, 1, data.length)));
            }
            br.close();
        }catch (IOException ignored){ }
    }

    /**
     * init resource string creation
     */
    public  void generateResource(){
        try {

            for (int i = 0; i < totalTranslation; i++) {
                String generatedFilePath = new File(xmlDestinationPath, String.format("index_%d_.xml", i+1)).getAbsolutePath();
                File fXmlFile = new File(defaultStringXmlPath);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fXmlFile);
                doc.getDocumentElement().normalize();

                NodeList nList = doc.getElementsByTagName("resources");
                Node nNode = nList.item(0);
                for (int index = 0; index < nNode.getChildNodes().getLength(); index++) {
                    Node node = nNode.getChildNodes().item(index);
                    if (node.getNodeType() != Node.COMMENT_NODE && node.getAttributes() != null) {
                        String keyName = node.getAttributes().getNamedItem("name").getNodeValue();
                        boolean isTranslatable = isTranslatable(node.getAttributes().getNamedItem("translatable"));
                        if(isTranslatable){
                            updateThisNode(node, i);
                        }else {
                            nNode.removeChild(node);
                        }
                    }
                }

                // writing updated document
                TransformerFactory transformerFactory = TransformerFactory
                        .newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File(generatedFilePath));
                transformer.transform(source, result);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update Text of nodes recursively (recursive when nested array is used which is rare ;))
     * @param node node for which text is updated
     */
    private void updateThisNode(final Node node, final int stringIndex){
        if (node == null) return;
        if (node.getNodeName().equals("string-array")){
            // iterate every child node when xml tag is string
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                updateThisNode(node.getChildNodes().item(i), stringIndex);
            }
        }
        if (node.getNodeType() == Node.ELEMENT_NODE){
            Element element = (Element) node;
            String key = element.getTextContent();
            if (stringListMap.containsKey(key)) {
                element.setTextContent(stringListMap.get(key).get(stringIndex));
            }
        }
    }

    /**
     * Check weather node is Translatable or not
     * @param node note to check
     * @return true if Translatable false otherwise
     */
    private  boolean isTranslatable(final Node node) {
        if (node != null && node.getNodeValue().equalsIgnoreCase("false")){
            return false;
        }
        return true;
    }
}
