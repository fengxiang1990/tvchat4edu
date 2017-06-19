package com.bizcom.util;

import android.text.TextUtils;

import com.V2.jni.ind.JNIObjectInd;
import com.bizcom.request.util.EscapedcharactersProcessing;
import com.bizcom.vo.meesage.VMessage;
import com.bizcom.vo.meesage.VMessageAbstractItem;
import com.bizcom.vo.meesage.VMessageAudioItem;
import com.bizcom.vo.meesage.VMessageFaceItem;
import com.bizcom.vo.meesage.VMessageFileItem;
import com.bizcom.vo.meesage.VMessageImageItem;
import com.bizcom.vo.meesage.VMessageLinkTextItem;
import com.bizcom.vo.meesage.VMessageTextItem;
import com.bizcom.vo.whiteboard.V2Doc;
import com.bizcom.vo.whiteboard.V2Doc.Page;
import com.bizcom.vo.whiteboard.V2Shape;
import com.bizcom.vo.whiteboard.V2ShapeEarser;
import com.bizcom.vo.whiteboard.V2ShapeEllipse;
import com.bizcom.vo.whiteboard.V2ShapeLine;
import com.bizcom.vo.whiteboard.V2ShapeMeta;
import com.bizcom.vo.whiteboard.V2ShapePoint;
import com.bizcom.vo.whiteboard.V2ShapeRect;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XmlParser {

    /**
     * 解析收到的聊天内容(xml形式)，判断是否含有图片或语音留言
     *
     * @param xml 聊天内容
     * @return
     */
    public static int getMessageItemType(String xml) {
        int messageItemType = VMessageAbstractItem.ITEM_TYPE_TEXT;
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            NodeList imgMsgItemNL = doc.getElementsByTagName("TPictureChatItem");
            if (imgMsgItemNL.getLength() > 0) {
                messageItemType = VMessageAbstractItem.ITEM_TYPE_IMAGE;
                return messageItemType;
            }

            NodeList audioMsgItemNL = doc.getElementsByTagName("TAudioChatItem");
            if (audioMsgItemNL.getLength() > 0) {
                messageItemType = VMessageAbstractItem.ITEM_TYPE_AUDIO;
                return messageItemType;
            }

            NodeList linkMsgItemNL = doc.getElementsByTagName("TLinkTextChatItem");
            if (linkMsgItemNL.getLength() > 0) {
                Element item = (Element) linkMsgItemNL.item(0);
                String text = item.getAttribute("LinkType");
                if (text.equals("lteVMsgClass")) {
                    messageItemType = VMessageAbstractItem.ITEM_TYPE_ALL;
                    return messageItemType;
                }
            }
            return messageItemType;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageItemType;
    }

    public static VMessage parseForMessage(VMessage vm) {
        String xml = vm.getmXmlDatas();
        if (xml == null) {
            return vm;
        }
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            Document document = documentBuilder.parse(is);

            document.getDocumentElement().normalize();
            Element element = (Element) document.getElementsByTagName("TChatData").item(0);
            boolean isAutoReply = "True".equals(element.getAttribute("IsAutoReply"));
            vm.setAutoReply(isAutoReply);

            NodeList textMsgItemNL = document.getElementsByTagName("ItemList");
            if (textMsgItemNL.getLength() <= 0) {
                return null;
            }
            Element msgEl = (Element) textMsgItemNL.item(0);
            NodeList itemList = msgEl.getChildNodes();

            for (int i = 0; i < itemList.getLength(); i++) {
                Node n = itemList.item(i);
                if (n instanceof Element) {
                    msgEl = (Element) itemList.item(i);
                    boolean isNewLine = "True".equals(msgEl.getAttribute("NewLine"));
                    VMessageAbstractItem va;
                    if (msgEl.getTagName().equals("TTextChatItem")) {
                        String text = EscapedcharactersProcessing.reverse(msgEl.getAttribute("Text"));
                        if (!TextUtils.isEmpty(text)) {
                            String newText = text.replace("0xD0xA", "\n");
                            va = new VMessageTextItem(vm, newText);
                        } else
                            va = new VMessageTextItem(vm, text);
                        va.setNewLine(isNewLine);
                    } else if (msgEl.getTagName().equals("TLinkTextChatItem")) {
                        String text = msgEl.getAttribute("Text");
                        String url = EscapedcharactersProcessing.reverse(msgEl.getAttribute("URL"));
                        va = new VMessageLinkTextItem(vm, text, url);
                        va.setNewLine(isNewLine);
                    } else if (msgEl.getTagName().equals("TSysFaceChatItem")) {
                        String fileName = msgEl.getAttribute("FileName");
                        int start = fileName.indexOf(".");
                        int index = Integer.parseInt(fileName.substring(0, start));
                        va = new VMessageFaceItem(vm, index);
                        va.setNewLine(isNewLine);
                    } else if (msgEl.getTagName().equals("TPictureChatItem")) {

                        String uuid = msgEl.getAttribute("GUID");
                        if (uuid == null) {
                            V2Log.e("Invalid uuid ");
                            continue;
                        }
                        VMessageImageItem vii = new VMessageImageItem(vm, uuid, msgEl.getAttribute("FileExt"));
                        vii.setNewLine(isNewLine);

                    } else if (msgEl.getTagName().equals("TAudioChatItem")) {

                        String uuid = msgEl.getAttribute("FileID");
                        if (uuid == null) {
                            V2Log.e("Invalid uuid ");
                            continue;
                        }
                        String fileExt = msgEl.getAttribute("FileExt");
                        String seconds = msgEl.getAttribute("Seconds");

                        VMessageAudioItem vii = new VMessageAudioItem(vm, uuid, fileExt, Integer.valueOf(seconds));
                        vii.setNewLine(isNewLine);

                    } else if (msgEl.getTagName().equals("file")) {
                        String uuid = msgEl.getAttribute("id");
                        if (uuid == null) {
                            V2Log.e("Invalid uuid ");
                            continue;
                        }
                        String url = msgEl.getAttribute("url");
                        String filePath = msgEl.getAttribute("name");
                        VMessageFileItem vii = new VMessageFileItem(vm, filePath, 0);
                        vii.setUuid(uuid);
                        vii.setUrl(url);
                        vii.setNewLine(isNewLine);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return vm;
        }
        return vm;
    }

    public static void extraImageMetaFrom(VMessage vm, String xml) {
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            Document doc = dBuilder.parse(is);

            doc.getDocumentElement().normalize();
            NodeList imgMsgItemNL = doc.getElementsByTagName("TPictureChatItem");
            for (int i = 0; i < imgMsgItemNL.getLength(); i++) {
                Element msgEl = (Element) imgMsgItemNL.item(i);
                String uuid = msgEl.getAttribute("GUID");
                if (uuid == null) {
                    V2Log.e("Invalid uuid ");
                    continue;
                }
                boolean isNewLine = "True".equals(msgEl.getAttribute("NewLine"));
                VMessageImageItem vii = new VMessageImageItem(vm, uuid, msgEl.getAttribute("FileExt"));
                vii.setNewLine(isNewLine);
                vii.setState(VMessageAbstractItem.TRANS_WAIT_RECEIVE);
                vii.setFilePath("wait");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void extraAudioMetaFrom(VMessage vm, String xml) {
        InputStream is;
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            Document doc = dBuilder.parse(is);

            doc.getDocumentElement().normalize();
            NodeList audioMsgItemNL = doc.getElementsByTagName("TAudioChatItem");
            for (int i = 0; i < audioMsgItemNL.getLength(); i++) {
                Element audioItemEl = (Element) audioMsgItemNL.item(i);
                String uuid = audioItemEl.getAttribute("FileID");
                if (uuid == null) {
                    V2Log.e("Invalid uuid ");
                    continue;
                }

                VMessageAudioItem vii = new VMessageAudioItem(vm, uuid, audioItemEl.getAttribute("FileExt"),
                        Integer.parseInt(audioItemEl.getAttribute("Seconds")));
                vii.setNewLine(true);
                vii.setReceive(false);
                vii.setState(VMessageAbstractItem.TRANS_WAIT_RECEIVE);
                vii.setReadState(VMessageAbstractItem.STATE_UNREAD);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static VMessage extraTextMetaFrom(VMessage vm, String xml) {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            Document document = documentBuilder.parse(is);

            document.getDocumentElement().normalize();
            Element element = (Element) document.getElementsByTagName("TChatData").item(0);
            boolean isAutoReply = "True".equals(element.getAttribute("IsAutoReply"));
            vm.setAutoReply(isAutoReply);

            NodeList textMsgItemNL = document.getElementsByTagName("ItemList");
            if (textMsgItemNL.getLength() <= 0) {
                return vm;
            }
            Element msgEl = (Element) textMsgItemNL.item(0);
            NodeList itemList = msgEl.getChildNodes();

            for (int i = 0; i < itemList.getLength(); i++) {
                Node n = itemList.item(i);
                if (n instanceof Element) {
                    msgEl = (Element) itemList.item(i);
                    boolean isNewLine = "True".equals(msgEl.getAttribute("NewLine"));
                    VMessageAbstractItem va;
                    if (msgEl.getTagName().equals("TTextChatItem")) {
                        String text = EscapedcharactersProcessing.reverse(msgEl.getAttribute("Text"));
                        if (!TextUtils.isEmpty(text)) {
                            String newText = text.replace("0xD0xA", "\n");
                            va = new VMessageTextItem(vm, newText);
                        } else
                            va = new VMessageTextItem(vm, text);
                        va.setNewLine(isNewLine);
                    } else if (msgEl.getTagName().equals("TLinkTextChatItem")) {
                        String text = msgEl.getAttribute("Text");
                        String url = EscapedcharactersProcessing.reverse(msgEl.getAttribute("URL"));
                        va = new VMessageLinkTextItem(vm, text, url);
                        va.setNewLine(isNewLine);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return vm;
        }
        return vm;
    }

    public static V2Doc.Doc parserDocPage(String docId, String xml) {
        V2Doc.Doc pr = new V2Doc.Doc();
        pr.setDocId(docId);
        InputStream is = null;
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            Document doc = dBuilder.parse(is);

            doc.getDocumentElement().normalize();
            NodeList pageList = doc.getElementsByTagName("page");
            Page[] pages = new Page[pageList.getLength()];
            for (int i = 0; i < pageList.getLength(); i++) {
                Element page = (Element) pageList.item(i);
                String pid = page.getAttribute("id");
                if (pid == null) {
                    continue;
                }
                int no = Integer.parseInt(pid);
                pages[no - 1] = new Page(no, docId, null);
            }
            pr.addPages(pages);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return pr;
    }

    /**
     * 该函数用于解析会议中白板上的绘图
     * FIXME optimze code
     *
     * @param xml
     * @return
     */
    public static V2ShapeMeta parseV2ShapeMetaSingle(String xml) {
        InputStream is = null;
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nodeList = null;
            // FIXME optimize code
            nodeList = doc.getElementsByTagName("TBeelineMeta");
            if (nodeList == null || nodeList.getLength() <= 0) {
                nodeList = doc.getElementsByTagName("TFreedomLineMeta");
            }
            V2ShapeMeta meta = null;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element e = (Element) nodeList.item(i);

                meta = new V2ShapeMeta(e.getAttribute("ID"));

                V2ShapePoint[] points = null;
                NodeList shapeDataList = e.getChildNodes();
                V2ShapeLine shape = new V2ShapeLine(points);

                for (int j = 0; j < shapeDataList.getLength(); j++) {
                    if (shapeDataList.item(j).getNodeType() != Element.ELEMENT_NODE) {
                        continue;
                    }
                    Element shapeE = (Element) shapeDataList.item(j);

                    if (shapeE.getTagName().equals("Points")) {
                        String pointsStr = e.getTextContent().trim();
                        String[] str = pointsStr.split(" ");
                        points = new V2ShapePoint[str.length / 2];
                        for (int index = 0, pi = 0; index < points.length; index++, pi += 2) {
                            points[index] = new V2ShapePoint(Integer.parseInt(str[pi]), Integer.parseInt(str[pi + 1]));
                        }
                        shape.addPoints(points);
                    } else if (shapeE.getTagName().equals("Pen")) {
                        shape.setWidth(Integer.parseInt(shapeE.getAttribute("Width")));
                        shape.setColor(Integer.parseInt(shapeE.getAttribute("Color")));
                    }

                }

                meta.addShape(shape);
            }

            boolean isRect = true;
            nodeList = doc.getElementsByTagName("TRectangleMeta");
            if (nodeList == null || nodeList.getLength() <= 0) {
                nodeList = doc.getElementsByTagName("TEllipseMeta");
                isRect = false;
            }

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element e = (Element) nodeList.item(i);

                meta = new V2ShapeMeta(e.getAttribute("ID"));

                NodeList shapeDataList = e.getChildNodes();
                V2Shape shape = null;

                for (int j = 0; j < shapeDataList.getLength(); j++) {
                    if (shapeDataList.item(j).getNodeType() != Element.ELEMENT_NODE) {
                        continue;
                    }
                    Element shapeE = (Element) shapeDataList.item(j);

                    if (shapeE.getTagName().equals("Points")) {
                        String pointsStr = e.getTextContent().trim();
                        String[] str = pointsStr.split(" ");
                        if (str.length == 4) {
                            if (isRect) {
                                shape = new V2ShapeRect(Integer.parseInt(str[0]), Integer.parseInt(str[1]),
                                        Integer.parseInt(str[2]), Integer.parseInt(str[3]));
                            } else {
                                shape = new V2ShapeEllipse(Integer.parseInt(str[0]), Integer.parseInt(str[1]),
                                        Integer.parseInt(str[2]), Integer.parseInt(str[3]));
                            }
                        } else {
                            V2Log.e("Incorrect data ");
                        }
                    } else if (shapeE.getTagName().equals("Pen")) {
                        shape.setWidth(Integer.parseInt(shapeE.getAttribute("Width")));
                        shape.setColor(Integer.parseInt(shapeE.getAttribute("Color")));
                    }

                }

                meta.addShape(shape);
            }

            nodeList = doc.getElementsByTagName("TEraseLineMeta");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element e = (Element) nodeList.item(i);
                meta = new V2ShapeMeta(e.getAttribute("ID"));

                NodeList shapeDataList = e.getChildNodes();

                V2ShapeEarser earser = new V2ShapeEarser();
                for (int j = 0; j < shapeDataList.getLength(); j++) {
                    if (shapeDataList.item(j).getNodeType() != Element.ELEMENT_NODE) {
                        continue;
                    }
                    Element shapeE = (Element) shapeDataList.item(j);

                    if (shapeE.getTagName().equals("Points")) {
                        String pointsStr = e.getTextContent().trim();
                        String[] str = pointsStr.split(" ");
                        int len = str.length / 4;
                        for (int index = 0; index < len; index += 4) {
                            earser.addPoint(Integer.parseInt(str[index]), Integer.parseInt(str[index + 1]));
                            earser.addPoint(Integer.parseInt(str[index + 2]), Integer.parseInt(str[index + 3]));
                            //
                            // earser.lineToLine(Integer.parseInt(str[index]),
                            // Integer.parseInt(str[index + 1]),
                            // Integer.parseInt(str[index + 2]),
                            // Integer.parseInt(str[index + 3]));
                        }
                    } else if (shapeE.getTagName().equals("Pen")) {

                    }
                }
                meta.addShape(earser);

            }
            return meta;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 该函数只适用于解析每个节点上的属性，并不解析其带的文本内容。并将其属性简单变为get和set方法
     *
     * @param xml
     * @param clazz
     * @return
     */
    public static ArrayList<JNIObjectInd> parseJNICallBackNormalXml(String xml, Class clazz) {
        Field[] attributes = clazz.getFields();
        ArrayList<JNIObjectInd> beans = new ArrayList<>();
        try {
            Object tagObj = clazz.newInstance();
            Field declaredField = tagObj.getClass().getDeclaredField("tag");
            declaredField.setAccessible(true);
            String tagName = (String) declaredField.get(tagObj);

            XmlPullParser pull = XmlPullParserFactory.newInstance().newPullParser();
            pull.setInput(new ByteArrayInputStream(xml.getBytes()), "UTF-8");
            int eventCode = pull.getEventType();
            StringBuilder sb = new StringBuilder();
            while (eventCode != XmlPullParser.END_DOCUMENT) {
                switch (eventCode) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equals(pull.getName())) {
                            Object newInstance = clazz.newInstance();
                            Class temp = newInstance.getClass();
                            for (int i = 0; i < attributes.length; i++) {
                                if (attributes[i].getDeclaringClass() != clazz
                                        || "CREATOR".equals(attributes[i].getName())) {
                                    continue;
                                }

                                String attName = attributes[i].getName();
                                String value = pull.getAttributeValue(null, attName);

                                char firstChar = attName.charAt(0);
                                String entity = attName.substring(1);
                                sb.append("set").append(Character.toUpperCase(firstChar)).append(entity);
                                String methodName = sb.toString();
                                sb.delete(0, sb.length());
                                try {
                                    @SuppressWarnings("unchecked")
                                    Method method = temp.getDeclaredMethod(methodName, attributes[i].getType());
                                    method.invoke(newInstance, value);
                                } catch (Exception e) {
                                    continue;
                                }
                            }
                            beans.add((JNIObjectInd) newInstance);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventCode = pull.next();
            }
            return beans;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> parseStringFindDifferent(InputStream ips) {
        List<String> srcDatas = new ArrayList<>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(ips);
            Element employees = document.getDocumentElement();
            NodeList employeeInfo = employees.getChildNodes();
            for (int j = 0; j < employeeInfo.getLength(); j++) {
                Node node = employeeInfo.item(j);
                if (node.getNodeType() == Node.ELEMENT_NODE
                        && node.getNodeName().equals("string")) {
                    String name = node.getAttributes().getNamedItem("name")
                            .getNodeValue();
                    // String textContent = node.getTextContent();
                    srcDatas.add(name);
                }
            }
            return srcDatas;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return srcDatas;
    }
}
