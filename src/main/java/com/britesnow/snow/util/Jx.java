/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Jx {
    static private Logger logger = LoggerFactory.getLogger(Jx.class);
                            
    private static final Jx  nullJx        = new Jx((Node) null);

    //set by the constructor and/or setNodeList
    private Node             n             = null;
    //built by the NodeList contructors
    private List<Node>       nList         = null;

    //
    private NamespaceContext nsc           = null;

    private Exception        lastException = null;

    /* --------- Init Methods--------- */
    private Jx(Node n) {
        this.n = n;
    }

    private Jx(NodeList nl) {
        if (nl != null && nl.getLength() > 0) {
            List<Node> tmpNodeList = new ArrayList<Node>(nl.getLength());
            for (int i = 0; i < nl.getLength(); i++) {
                tmpNodeList.add(nl.item(i));
            }
            setNodeList(tmpNodeList);
        }
    }

    private Jx(List<Node> nList) {
        setNodeList(nList);
    }

    private final void setNodeList(List<Node> nList) {
        this.nList = nList;
        if (this.nList != null && this.nList.size() > 0) {
            n = this.nList.get(0);
        }
    }

    private Jx(Exception exception) {
        lastException = exception;
    }

    /* --------- /Init Methods --------- */

    /**
     * Remove all the node for this Jx object form their XML Parent (calling
     * removeChild).
     * 
     * @return this Jx object for chainability
     */
    public final Jx remove() {
        if (nList != null) {
            for (Node n : nList) {
                Node parent = n.getParentNode();
                if (parent != null) {
                    parent.removeChild(n);
                }
            }
        } else if (n != null) {
            Node parent = n.getParentNode();
            if (parent != null) {
                parent.removeChild(n);
            }
        }
        return this;
    }

    /* --------- Children --------- */
    /**
     * Note: This method filters the getChildNodes with only the nodes of type
     * Element.
     * 
     * @return a list of Jx for each childNode of type Element (i.e. not #text)
     *         for the first Node.
     */
    public final List<Jx> children() {
        if (n != null && n instanceof Element) {
            List<Jx> jxList = new ArrayList<Jx>();
            NodeList childNodes = n.getChildNodes();
            for (int i = 0, m = childNodes.getLength(); i < m; i++) {
                Node childNode = childNodes.item(i);
                if (childNode instanceof Element) {
                    jxList.add(new Jx(childNode).nsc(nsc));
                }
            }
            return jxList;
        } else {
            return new ArrayList<Jx>(0);
        }
    }

    /**
     * @return a List of Jx for each childNode of the first node of the Jx
     *         object.
     */
    public final List<Jx> allChildren() {
        if (n != null && n instanceof Element) {
            List<Jx> jxList = new ArrayList<Jx>();
            NodeList childNodes = n.getChildNodes();
            for (int i = 0, m = childNodes.getLength(); i < m; i++) {
                Node childNode = childNodes.item(i);
                jxList.add(new Jx(childNode).nsc(nsc));
            }
            return jxList;
        } else {
            return new ArrayList<Jx>(0);
        }
    }

    /**
     * Add a child Element with the tagName. Return the childJx object.
     * 
     * @param tagName
     *            if null, nothing is done.
     * @return the childJx object for the child created. Return a nullJx if the
     *         tagName or this Jx Object did not have an element.
     */
    public final Jx addChild(String tagName) {
        Element e = e();
        if (e != null && tagName != null) {
            Element child = e.getOwnerDocument().createElement(tagName);
            e.appendChild(child);
            return new Jx(child).nsc(nsc);
        }
        return nullJx;
    }

    /**
     * Find the first child element with this tagName or create the child if not
     * children had this tagName.
     * 
     * @param tagName
     * @return the Jx object for the child name (create the child if not found)
     */
    public final Jx child(String tagName) {
        Jx resultJx = this.find(tagName);

        if (resultJx.e() != null) {
            return x(resultJx.e()).nsc(nsc);
        } else if (e() != null) {
            Element e = e();
            Document doc = e().getOwnerDocument();
            Element childElement = doc.createElement(tagName);
            e.appendChild(childElement);
            return new Jx(childElement).nsc(nsc);
        } else {
            return nullJx;
        }

    }

    /**
     * Replace a oldChild with a newChild. All refered nodes need to be not
     * null, and this Jx Object Node needs to be an Element. Otherwise, nothing
     * is happening.
     * 
     * @param newChildJx
     * @param oldChildJx
     * @return
     */
    public final Jx replaceChild(Jx newChildJx, Jx oldChildJx) {
        Element e = e();
        if (e != null && newChildJx.n() != null && oldChildJx.n() != null) {
            e.getOwnerDocument().adoptNode(newChildJx.n());
            e.replaceChild(newChildJx.n(), oldChildJx.n());
        }

        return this;
    }

    /* --------- /Children --------- */

    /* --------- Jx Get & Set Methods --------- */

    /**
     * @return the [first] node of this Jx object
     */
    public final Node n() {
        return n;
    }

    /**
     * @return the first element of this Jx object. Return null if the first
     *         node is null or not an Element.
     */
    public final Element e() {
        if (n instanceof Element) {
            return (Element) n;
        } else {
            return null;
        }
    }
    
    /**
     * @return the owner document of the first Node. Null if there is no first Node.
     */
    public final Document doc(){
        if (n != null){
            return n.getOwnerDocument();
        }else{
            return null;
        }
    }

    /**
     * @return the text value of the containing xml node. If this Jx object
     *         contains a list of nodes, then, the text value of the first node
     *         is returned.
     */
    public final String value() {
        if (n != null) {
            return n.getTextContent();
        } else {
            return null;
        }
    }

    /**
     * @return true if the text value for the first node is null or empty
     *         (length == 0).
     */
    public final boolean isValueEmpty() {
        String v = value();
        if (v == null || v.length() < 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set the nodeValue for the first node. TODO: Need to think what to do with
     * the other nodes.
     * 
     * @param value
     * @return This Jx object for chainability.
     */
    public final Jx value(String value) {
        if (n != null) {
            if (n instanceof Element) {
                e().setTextContent(value);
            } else {
                n.setNodeValue(value);
            }

        }
        return this;
    }

    /**
     * @return the node name of the first Node. If no Node, then return null.
     */
    public final String nodeName() {
        if (n != null) {
            return n.getNodeName();
        } else {
            return null;
        }
    }

    public final int size() {
        if (nList != null) {
            return nList.size();
        } else if (n != null) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Return a list of Jx Elements. Return a list of one element (this one) if
     * this Jx was only for one element. Create a list of multiple Jx elements
     * if this Jx Element contained a list of nodes (nl)
     * 
     * @return a list of Jx elements. Return an empty list if no element. Never
     *         return null.
     */
    public final List<Jx> list() {
        ArrayList<Jx> list = null;

        // if we have an nodeList, then, create the Jx List for each node
        if (nList != null && nList.size() > 0) {
            list = new ArrayList<Jx>(nList.size());
            for (Node node : nList) {
                list.add(new Jx(node).nsc(nsc));
            }
        }
        // otherwise, create a list and add this Jx Object (no need to create another one)
        else if (n != null) {
            list = new ArrayList<Jx>(1);
            list.add(this);
        }
        // if nothing, then, return an empty list.
        else {
            list = new ArrayList<Jx>(0);
        }

        return list;
    }

    public final Exception lastException() {
        return lastException;
    }

    /* --------- /Jx Get & Set Methods--------- */

    /* --------- Attributes --------- */
    /**
     * 
     * @param attributeName
     * @return The attribute value. Return null if not found or if this Jx
     *         element does not have a node
     */
    public final String attr(String attributeName) {
        if (n != null && n instanceof Element) {
            return ((Element) n).getAttribute(attributeName);
        } else {
            return null;
        }
    }

    /**
     * Set the attribute for this node (assuming it is an element). If this Jx
     * object points to a Node (and not an Element), then, nothing happen.
     * 
     * @param attributeName
     * @param attributeValue
     *            if null, will call removeAttribute.
     * @return This Jx Object for chainability.
     */
    public final Jx attr(String attributeName, Object attributeValue) {
        Element e = e();
        if (e != null) {
            if (attributeValue != null) {
                e.setAttribute(attributeName, attributeValue.toString());
            } else {
                e.removeAttribute(attributeName);
            }
        }
        return this;
    }

    public final <T> T attrAs(String attributeName, Class<T> cls) {
        String value = attr(attributeName);
        return ObjectUtil.getValue(value, cls, null);

    }

    public final <T> T attrAs(String attributeName, Class<T> cls, T defaultValue) {
        String value = attr(attributeName);
        return ObjectUtil.getValue(value, cls, defaultValue);
    }

    /**
     * Note: a new array will be created a each call
     * 
     * @return The attribute names for the first node (if it is an Element,
     *         otherwise, return null.)
     */
    public final String[] attrNames() {
        String[] attrNames = null;
        if (n != null && n instanceof Element) {
            NamedNodeMap nnm = n.getAttributes();
            attrNames = new String[nnm.getLength()];
            for (int i = 0; i < nnm.getLength(); i++) {
                attrNames[i] = nnm.item(i).getNodeName();
            }
        } else {
            attrNames = new String[0];
        }
        return attrNames;
    }

    /* --------- /Attributes --------- */

    /* --------- Jx Formatting Methods --------- */
    /**
     * Pretty formatting (with indent) of a XML document. Use the XSLT way of
     * doing it.
     * 
     * @return
     */
    public final String pretty() {
        String result = null;

        TransformerFactory tfactory = TransformerFactory.newInstance();
        Transformer serializer;
        try {
            serializer = tfactory.newTransformer();
            // Setup indenting to "pretty print"

            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StringWriter sw = new StringWriter();
            serializer.transform(new DOMSource(n), new StreamResult(sw));
            sw.flush();
            result = sw.toString();
        } catch (TransformerException e) {
            logger.error(e.getMessage());
        }

        return result;
    }

    /* --------- Jx Formatting Methods --------- */

    /* --------- Jx Operations --------- */

    /**
     * Add a external Jx node to the first node (as a children). The current
     * node of this Jx Object need to be a ELEMENT (otherwise, nothing happen)
     * 
     * @param nodeToAdd
     * @return This Jx Object for chainability
     */
    public final Jx add(Jx nodeToAdd) {
        for (Jx nodeToAddJx : nodeToAdd.list()) {

            Jx newNode = nodeToAddJx.clone();

            if (n instanceof Element) {
                Document currentDoc = n.getOwnerDocument();

                currentDoc.adoptNode(newNode.n());
                ((Element) n).appendChild(newNode.n());
            }
        }

        return this;
    }

    /**
     * Do a deep close on the node or nodeList and create a new Jx Object.
     * 
     * @see java.lang.Object#clone()
     * @return The newly create Jx
     */
    public final Jx clone() {
        // if we have a list, then, clone all the node of the list.
        Jx newJx = null;

        if (nList != null && nList.size() > 0) {
            List<Node> newNodeList = new ArrayList<Node>();
            for (Node node : nList) {
                newNodeList.add(node.cloneNode(true));
            }
            newJx = new Jx(newNodeList).nsc(nsc);
        } else if (n != null) {
            newJx = new Jx(n.cloneNode(true)).nsc(nsc);
        } else {
            newJx = nullJx;
        }
        return newJx;
    }

    /**
     * Merge a Jx Object with this one. Not that the Jx Object source is not
     * modified, on the caller is. For now, here are the merge rules:
     * <ul>
     * <li>First go through the jxToMerge element attributes and add them to the
     * current Element (overriding the existing one)</li>
     * <li>If jxToMerge has some children element (of type ELEMENT), then, add
     * them.</li>
     * </ul>
     * 
     * Note: When merging, element with children won't merge the #text element
     * 
     * @param jxToMerge
     * @return This Jx for chainability
     */
    public final Jx merge(Jx jxToMerge) {
        // first merge the attributes
        for (String attName : jxToMerge.attrNames()) {
            this.attr(attName, jxToMerge.attr(attName));
        }

        // if jxToMerge has children, then, add them
        List<Jx> children = jxToMerge.allChildren();
        if (children.size() > 0) {
            for (Jx jxChildToMerge : children) {
                this.add(jxChildToMerge);
            }
        }
        return this;
    }

    /* --------- /Jx Operations --------- */

    /* --------- Jx Query Methods --------- */
    /**
     * Perform an xpath search on the current Node, and return a newly created
     * Jx object with the result. If an error if found in the xpath, the a
     * nullJX is returned.
     * 
     * @param xpath
     * @return
     */
    public final Jx find(String xpath) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        if (nsc != null) {
            xPath.setNamespaceContext(nsc);
        }
        NodeList nodes = null;
        try {
            nodes = (NodeList) xPath.evaluate(xpath, n, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            // Yes, do nothing.
            return nullJx;
        }
        return new Jx(nodes).nsc(nsc);
    }

    /**
     * @param xpath
     * @return true if the xpath as at least on match
     */
    public final boolean has(String xpath) {
        if (find(xpath).n() != null) {
            return true;
        } else {
            return false;
        }
    }

    /* --------- /Jx Query Methods --------- */

    /* --------- Jx Static Factories --------- */
    public static final Jx x(Node n) {
        return new Jx(n);
    }

    public static final Jx x(String xmlString) {
        if (xmlString == null) {
            return nullJx;
        } else {
            return createJx(new StringReader(xmlString));
        }
    }

    public static final Jx x(File file) {
        if (file == null) {
            return nullJx;
        } else {
            try {
                return createJx(new FileReader(file));
            } catch (FileNotFoundException e) {
                return new Jx(e);
            }
        }

    }

    private static final Jx createJx(Reader reader) {
        Jx jx = null;

        try {
            Element documentElement = null;

            // get the document build
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setNamespaceAware(true);

            DocumentBuilder docBuilder;

            docBuilder = fac.newDocumentBuilder();

            InputSource contentIS = new InputSource(reader);

            Document document = docBuilder.parse(contentIS);
            if (document != null) {
                documentElement = document.getDocumentElement();
            }
            reader.close();

            jx = new Jx(documentElement);
        } catch (Exception e) {
            jx = new Jx(e);
        }

        return jx;
    }
    
    public static Document createDocument(){
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setNamespaceAware(true);
        DocumentBuilder docBuilder;
        try {
            docBuilder = fac.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Cannot create blank document",e);
        }
        return docBuilder.newDocument();
    }

    /* --------- /Jx Static Factories --------- */

    /*--------- Namespace Context Factory ---------*/
    /**
     * Initialize this Jx object with the namespace contained in the first
     * Element (e()). The namespace will be passed to all selected or created JX
     * Object.
     * 
     * @return This Jx object for Chainability
     */
    public final Jx initNsc(String defaultNSName) {
        nsc = createNsc(e(), defaultNSName);
        return this;
    }

    /**
     * Set the namespace context for this Jx object
     * @param nsc
     * @return
     */
    private final Jx nsc(NamespaceContext nsc) {
        this.nsc = nsc;
        return this;
    }

    /**
     * Create a NamespaceContext for the rootELement.
     * 
     * @param rootElement
     *            Rootelement where the namespaceURI will be extracted from
     * @param defaultNSName
     *            Default namespace prefix
     * @return
     */
    private static NamespaceContext createNsc(Element rootElement, String defaultNSName) {
        if (rootElement != null && defaultNSName != null) {
            Map<String, String> uriByPrefix = new HashMap<String, String>();

            //set the default namespace
            String nameSpaceUri = rootElement.getNamespaceURI();
            //SystemOutUtil.printValue("XMLUtil.buildNamespaceContextFromElement rootEl ns",nameSpaceUri);
            uriByPrefix.put(defaultNSName, nameSpaceUri);

            NamedNodeMap atts = rootElement.getAttributes();
            for (int i = 0; i < atts.getLength(); i++) {
                Node att = atts.item(i);
                String attName = att.getNodeName();
                String attValue = att.getNodeValue();

                int idxCol = -1;
                if (attName.startsWith("xmlns") && (idxCol = attName.indexOf(':')) != -1) {
                    String nsName = attName.substring(idxCol + 1);
                    uriByPrefix.put(nsName, attValue);
                    //SystemOutUtil.printValue("XMLUtil.buildNamespaceContextFromElement rootEl att","nsname: " + nsName + " - value:" + attValue);
                }

            }

            return new MapNamespaceContext(uriByPrefix);
        } else {
            return null;
        }

    }
    /*--------- Namespace Context Factory ---------*/

}

class MapNamespaceContext implements NamespaceContext{
    Map<String,String> uriByPrefix;
    
    public MapNamespaceContext(Map<String,String> m){
        uriByPrefix = m;
    }
    @Override
    public String getNamespaceURI(String prefix) {
        // TODO Auto-generated method stub
        return uriByPrefix.get(prefix);
        
    }

    @Override
    public String getPrefix(String namespaceURI) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        // TODO Auto-generated method stub
        return null;
    }
    
}