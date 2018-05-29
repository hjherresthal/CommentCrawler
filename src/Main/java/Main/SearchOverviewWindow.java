package Main;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;

public class SearchOverviewWindow {
    private JPanel panelMain;
    private JList<String> siteList;
    private JTextField urlTextField;
    private JTextField prefixTextField;
    private JTextField suffixTextField;
    private JTextField articleElementTextField;
    private JTextField articleLinkTextField;
    private JTextField nextPageTextField;
    private JButton importButton;
    private JButton exportButton;
    private JButton newButton;
    private JButton deleteButton;
    private JButton saveButton;
    private JPanel optionsPanel;

    private DefaultListModel<String> siteLinkedList;
    private Document sitesDocument = null;
    private Element selectedSiteElement = null;
    private int selectedSiteIndex = 0;

    private SearchOverviewWindow() {

        siteList.addListSelectionListener(e -> {

            NodeList nodeList = sitesDocument.getElementsByTagName("site");


            for (int i = 0; i < nodeList.getLength(); i++) {
                Node currentNode = nodeList.item(i);

                if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) currentNode;

                    NamedNodeMap attributes = eElement.getAttributes();

                    for (int j = 0; j < attributes.getLength(); j++) {
                        if (attributes.item(j).getNodeName().equals("url") && attributes.item(j).getNodeValue().equals(siteList.getSelectedValue())) {
                            selectedSiteElement = eElement;
                            selectedSiteIndex = i;
                        }


                    }
                }
            }

            //UI elemente setzen
            urlTextField.setText(selectedSiteElement.getAttribute("url"));
            prefixTextField.setText(selectedSiteElement.getAttribute("searchURLPrefix"));
            suffixTextField.setText(selectedSiteElement.getAttribute("searchURLSuffix"));
            articleElementTextField.setText(selectedSiteElement.getAttribute("searchResultArticleElementIdentifier"));
            articleLinkTextField.setText(selectedSiteElement.getAttribute("searchResultArticleLinkIdentifier"));
            nextPageTextField.setText(selectedSiteElement.getAttribute("searchResultNextPageIdentifier"));

        });
        newButton.addActionListener(e -> {
            Element siteElement = sitesDocument.createElement("site");
            String url = JOptionPane.showInputDialog("url:");
            siteElement.setAttribute("url", url);
            siteElement.setAttribute("searchURLPrefix", "");
            siteElement.setAttribute("searchURLSuffix", "");
            siteElement.setAttribute("searchResultArticleElementIdentifier", "");
            siteElement.setAttribute("searchResultArticleLinkIdentifier", "");
            siteElement.setAttribute("searchResultNextPageIdentifier", "");

            sitesDocument.getDocumentElement().appendChild(siteElement);

            siteLinkedList.add(siteLinkedList.size(), url);
        });
        saveButton.addActionListener(e -> {
            selectedSiteElement.setAttribute("url", urlTextField.getText());
            selectedSiteElement.setAttribute("searchURLPrefix", prefixTextField.getText());
            selectedSiteElement.setAttribute("searchURLSuffix", suffixTextField.getText());
            selectedSiteElement.setAttribute("searchResultArticleElementIdentifier", articleElementTextField.getText());
            selectedSiteElement.setAttribute("searchResultArticleLinkIdentifier", articleLinkTextField.getText());
            selectedSiteElement.setAttribute("searchResultNextPageIdentifier", nextPageTextField.getText());

            try {
                StreamResult result = new StreamResult(new File("siteParameters" + File.separator + "searchParameters.xml"));
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(sitesDocument);
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.transform(source, result);
            } catch (TransformerException e1) {
                //TODO
            }

        });
        exportButton.addActionListener(e -> {
            NamedNodeMap attributes = selectedSiteElement.getAttributes();

            StringBuilder result = new StringBuilder();

            for (int i = 0; i < attributes.getLength(); i++) {
                result.append(attributes.item(i).getNodeName()).append(",").append(attributes.item(i).getNodeValue()).append(",");
            }
            result = new StringBuilder(result.substring(0, result.length() - 1));
            System.out.println(result);
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(new StringSelection(result.toString()), null);
        });
        importButton.addActionListener(e -> {
            JFrame inputFrame = new JFrame("Importieren");
            String input = JOptionPane.showInputDialog(inputFrame, "Eingabe:");
            StringTokenizer st = new StringTokenizer(input, ",");

            Element siteNode = sitesDocument.createElement("site");

            while (st.hasMoreElements()) {
                siteNode.setAttribute(st.nextToken(), st.nextToken());
            }
            sitesDocument.getDocumentElement().appendChild(siteNode);
            siteLinkedList.add(siteLinkedList.size(), siteNode.getAttribute("url"));

        });
        deleteButton.addActionListener(e -> {
            sitesDocument.getDocumentElement().removeChild(selectedSiteElement);
            siteLinkedList.remove(selectedSiteIndex);
            siteList.clearSelection();

            for (Component c : optionsPanel.getComponents()
                    ) {

                if (c.getClass() == JTextField.class) {
                    try {
                        c.getClass().getMethod("setText", new Class[]{String.class}).invoke(c, "");
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    private void initSiteList() {
        siteLinkedList = new DefaultListModel<>();
        siteList = new JList<>(siteLinkedList);
        File inputFile = new File("siteParameters" + File.separator + "searchParameters.xml");
        sitesDocument = null;
        try {
            sitesDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputFile);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        sitesDocument.getDocumentElement().normalize();

        NodeList nodeList = sitesDocument.getElementsByTagName("site");

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);

            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) currentNode;

                NamedNodeMap attributes = eElement.getAttributes();

                for (int j = 0; j < attributes.getLength(); j++) {

                    String s = attributes.item(j).toString();

                    if (s.startsWith("url")) {
                        siteLinkedList.add(siteLinkedList.size(), attributes.item(j).getNodeValue());
                    }

                }
            }
        }
    }

    public static void open() {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Search Options");

        frame.setContentPane(new SearchOverviewWindow().panelMain);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        initSiteList();
    }
}
