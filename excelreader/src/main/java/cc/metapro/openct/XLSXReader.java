package cc.metapro.openct;

/*
 *  Copyright 2016 - 2017 OpenCT open source class table
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XLSXReader {

    private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private File mFile;

    public XLSXReader(String file) throws FileNotFoundException {
        this(new File(file));
    }

    private XLSXReader(File file) throws FileNotFoundException {
        mFile = file;
        if (!mFile.exists()) {
            throw new FileNotFoundException();
        }
    }

    public String[] getSheets() throws IOException, ZipException {
        ZipFile zipFile = new ZipFile(mFile);
        File tmp = new File(mFile.getParentFile(), "xlsx-tmp");
        tmp.mkdir();

        zipFile.extractAll(tmp.getAbsolutePath());
        File worksheets = new File(tmp, "xl/worksheets");
        File sharedStrings = new File(tmp, "xl/sharedStrings.xml");
        Map<String, String> valueMap = null;
        try {
            valueMap = getValueMap(sharedStrings);
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        List<String> sheetsString = new ArrayList<>(1);
        File[] sheets = worksheets.listFiles();

        if (sheets == null) return null;

        for (File sheet : sheets) {
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                sheetsString.add(parseSheet(builder.parse(sheet), valueMap));
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }
        }
        String[] result = new String[sheetsString.size()];
        int i = 0;
        for (String s : sheetsString) {
            result[i++] = s;
        }

        tmp.delete();
        return result;
    }

    private String parseSheet(Document sheet, Map<String, String> valueMap) {
        StringBuilder builder = new StringBuilder();
        NodeList rows = sheet.getElementsByTagName("row");
        for (int i = 0; i < rows.getLength(); i++) {
            Node row = rows.item(i);
            NodeList columns = row.getChildNodes();
            for (int j = 0; j < columns.getLength(); j++) {
                Node cell = columns.item(j).getFirstChild();
                if (cell != null && cell.hasChildNodes()) {
                    cell = cell.getFirstChild();
                    String value = cell.getNodeValue();
                    builder.append(valueMap.get(value)).append("\t");
                }
            }
            builder.delete(builder.length() - 1, builder.length());
            builder.append("\n");
        }
        return builder.toString();
    }

    private Map<String, String> getValueMap(File sharedStrings) throws ParserConfigurationException, IOException, SAXException {
        Map<String, String> valueMap = new HashMap<>();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(sharedStrings);
        NodeList list = document.getElementsByTagName("t");
        for (int i = 0; i < list.getLength(); i++) {
            String s = list.item(i).getFirstChild().getNodeValue();
            valueMap.put(i + "", s);
        }
        return valueMap;
    }
}
