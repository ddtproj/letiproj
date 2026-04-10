package parser;


//import bpmn.xml.BPMN2XmlParser;
import parser.xml.BPMN2XmlParser;

import java.io.InputStream;
import java.util.List;


public class ParserFactory {
    public ParserFactory() {
    }

    public static IProcessModelParser getProcessModelParserForFiles(List<String> fileNames) {

        IProcessModelParser parser = new BPMN2XmlParser();
        parser.setFiles(fileNames);
        return parser.isApplicable() ? parser : null;
    }

    public static IProcessModelParser getProcessModelParserForStreams(List<InputStream> inputStreams) {
        IProcessModelParser parser = new BPMN2XmlParser();
        parser.setInputStreams(inputStreams);
        return parser.isApplicable() ? parser : null;
    }

    }
