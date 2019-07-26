# Resource String Generator
Java program to generate string resource for multiple language using csv 

## Requirments
- Absolute Path of csv file which contain , seprated string where first column should be default (english) strings
- Absolute Path of default string xml file 
- Optinal path to save generated resource files. If not mentioned it will use current dir and create a folder name *string_generated* and save all generated xml file in it
- If csv file contain multiple translation it will generate file with name **index_(column number in csv)_.xml** for example 
index_1_.xml index_2_.xml.

> ### Program also require guava util classes
```
implementation 'com.google.guava:guava:28.0-jre'
```
### Sample Code
```
 String csv = "/example.csv";
 String defStringXml = "/strings.xml";
 String sourceXmlPath = "/generated";
 ResourceMappingHelper abc = new ResourceMappingHelper(csv, defStringXml, sourceXmlPath);
 abc.generateResource();
```

## Format of CSV 
| English | Spanish | Hindi |
| --- | --- |--- |
| Hello | Hola | नमस्ते | 
| Good morning | Buenos días | शुभ प्रभात |
