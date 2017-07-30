package com.tg.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.util.StringTokenizer;

/**
 * Created by twogoods on 2017/7/28.
 */
public class XmlUtils {


    public static Element generateMybatisXmlFrame(String daoName) throws SAXException, DocumentException {
        String frame = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "\n" +
                "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
                "\n" +
                "<mapper namespace=\"" + daoName + "\">\n" +
                "</mapper>\n";
        Document document = parseText(frame);
        Element rootElement = document.getRootElement();
        return rootElement;
    }


    public static Document parseText(String text) throws DocumentException, SAXException {
        SAXReader reader = new SAXReader(false);
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        reader.setFeature("http://apache.org/xml/features/validation/schema", false);
        String encoding = getEncoding(text);

        InputSource source = new InputSource(new StringReader(text));
//        source.setEncoding(encoding);
        Document result = reader.read(source);
//        if (result.getXMLEncoding() == null) {
//            result.setXMLEncoding(encoding);
//        }
        return result;
    }

    private static String getEncoding(String text) {
        String result = null;
        String xml = text.trim();
        if (xml.startsWith("<?xml")) {
            int end = xml.indexOf("?>");
            String sub = xml.substring(0, end);
            StringTokenizer tokens = new StringTokenizer(sub, " =\"\'");

            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();
                if ("encoding".equals(token)) {
                    if (tokens.hasMoreTokens()) {
                        result = tokens.nextToken();
                    }
                    break;
                }
            }
        }
        return result;
    }
}
