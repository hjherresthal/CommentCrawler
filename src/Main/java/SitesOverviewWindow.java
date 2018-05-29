import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;

public class SitesOverviewWindow {
    private JButton newSiteButton;
    private JButton deleteSiteButton;
    private JPanel panelMain;
    private JList siteList;
    private JTabbedPane tabbedPane1;
    private JPanel generalTab;
    private JTextField urlTextField;
    private JTextField timeoutTextField;
    private JTextField resolveArticleURLTextField;
    private JTextField resolveForumURLTextField;
    private JTextField dynamicShowMoreCommentstextField;
    private JTextField dynamicExpandCommentTextField;
    private JPanel articleTab;
    private JTextField headlineIntroTextField;
    private JTextField headlineTextField;
    private JTextField authorNameTextField;
    private JTextField authorIDTextField;
    private JTextField articleIntroTextField;
    private JTextField articleBodyTextField;
    private JPanel commentsTab;
    private JTextField commentSectionTextField;
    private JTextField nextCommentPageButtonTextField;
    private JTextField resolveNextCommentPageTextField;
    private JTextField commentsTextField;
    private JTextField commentIDTextField;
    private JTextField userNameTextField;
    private JTextField userIDTextField;
    private JTextField commentDateTextField;
    private JTextField commentTitleTextField;
    private JTextField rawCommentBodyTextField;
    private JTextField quoteConditionalTextField;
    private JTextField withoutQuoteTextField;
    private JTextField withoutCommentTextField;
    private JTextField quoteTargetTextField;
    private JButton saveButton;
    private JButton exportierenButton;
    private JButton importierenButton;
    private JPanel generalPanel;
    private JCheckBox resolveForumURLCheckBox;
    private JCheckBox commentDynamicLoadCheckBox;
    private JCheckBox commentDynamicShowMoreCheckBox;
    private JCheckBox resolveArticleBeforeParsingCheckBox;
    private JPanel commentSectionTab;
    private JPanel articlePanel;
    private JPanel commentSectionPanel;
    private JPanel commentsPanel;
    private JTextField answerConditionalTextField;
    private JTextField answerTargetTextField;

    DefaultListModel siteLinkedList;
    Document sitesDocument = null;
    Element selectedSiteElement = null;
    int selectedSiteIndex = 0;

    public SitesOverviewWindow() {

        siteList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {

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
                //general tab
                urlTextField.setText(selectedSiteElement.getAttribute("url"));
                timeoutTextField.setText(selectedSiteElement.getAttribute("timeout"));

                //article tab
                resolveArticleBeforeParsingCheckBox.setSelected(selectedSiteElement.getAttribute("resolveArticleBeforeParsing").equals("true"));
                resolveArticleURLTextField.setText(selectedSiteElement.getAttribute("articleResolveBeforeParsingLink"));
                headlineIntroTextField.setText(selectedSiteElement.getAttribute("articleHeadlineIntroIdentifier"));
                headlineTextField.setText(selectedSiteElement.getAttribute("articleHeadlineIdentifier"));
                authorNameTextField.setText(selectedSiteElement.getAttribute("articleAuthorNameIdentifier"));
                authorIDTextField.setText(selectedSiteElement.getAttribute("articleAuthorIdIdentifier"));
                articleIntroTextField.setText(selectedSiteElement.getAttribute("articleIntroIdentifier"));
                articleBodyTextField.setText(selectedSiteElement.getAttribute("articleBodyIdentifier"));

                //comment section tab
                resolveForumURLCheckBox.setSelected(selectedSiteElement.getAttribute("resolveForumUrl").equals("true"));
                resolveForumURLTextField.setText(selectedSiteElement.getAttribute("resolveURLIdentifier"));
                commentDynamicShowMoreCheckBox.setSelected(selectedSiteElement.getAttribute("commentDynamicShowMore").equals("true"));
                dynamicShowMoreCommentstextField.setText(selectedSiteElement.getAttribute("showMoreCommentsDynamicClass"));
                commentDynamicLoadCheckBox.setSelected(selectedSiteElement.getAttribute("commentDynamicLoad").equals("true"));
                dynamicExpandCommentTextField.setText(selectedSiteElement.getAttribute("commentDynamicLoadLinkClass"));
                commentSectionTextField.setText(selectedSiteElement.getAttribute("commentSectionIdentifier"));
                nextCommentPageButtonTextField.setText(selectedSiteElement.getAttribute("nextCommentPageButtonIdentifier"));
                resolveNextCommentPageTextField.setText(selectedSiteElement.getAttribute("resolveNextCommentPageIdentifier"));
                commentsTextField.setText(selectedSiteElement.getAttribute("commentsIdentifier"));

                //comments tab
                commentIDTextField.setText(selectedSiteElement.getAttribute("commentIdIdentifier"));
                userNameTextField.setText(selectedSiteElement.getAttribute("userNameIdentifier"));
                userIDTextField.setText(selectedSiteElement.getAttribute("userIdIdentifier"));
                commentDateTextField.setText(selectedSiteElement.getAttribute("commentDateIdentifier"));
                commentTitleTextField.setText(selectedSiteElement.getAttribute("commentTitleIdentifier"));
                rawCommentBodyTextField.setText(selectedSiteElement.getAttribute("rawCommentBodyIdentifier"));
                quoteConditionalTextField.setText(selectedSiteElement.getAttribute("commentHasQuoteConditional"));
                withoutQuoteTextField.setText(selectedSiteElement.getAttribute("commentWithoutQuoteIdentifier"));
                withoutCommentTextField.setText(selectedSiteElement.getAttribute("quoteWithoutCommentIdentifier"));
                quoteTargetTextField.setText(selectedSiteElement.getAttribute("quoteTargetCommentIdIdentifier"));
                answerConditionalTextField.setText(selectedSiteElement.getAttribute("answerConditionalIdentifier"));
                answerTargetTextField.setText(selectedSiteElement.getAttribute("answerTargetIdentifier"));
            }
        });
        newSiteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Element siteElement = sitesDocument.createElement("site");
                String url = JOptionPane.showInputDialog("url:");
                siteElement.setAttribute("url", url);
                siteElement.setAttribute("resolveForumUrl", "");
                siteElement.setAttribute("resolveArticleBeforeParsing", "");
                siteElement.setAttribute("commentDynamicLoad", "");
                siteElement.setAttribute("commentDynamicShowMore", "");
                siteElement.setAttribute("timeout", "");
                siteElement.setAttribute("resolveURLIdentifier", "");
                siteElement.setAttribute("articleHeadlineIntroIdentifier", "");
                siteElement.setAttribute("articleHeadlineIdentifier", "");
                siteElement.setAttribute("articleIntroIdentifier", "");
                siteElement.setAttribute("articleAuthorNameIdentifier", "");
                siteElement.setAttribute("articleAuthorIdIdentifier", "");
                siteElement.setAttribute("articleBodyIdentifier", "");
                siteElement.setAttribute("articleResolveBeforeParsingLink", "");
                siteElement.setAttribute("commentSectionIdentifier", "");
                siteElement.setAttribute("commentsIdentifier", "");
                siteElement.setAttribute("userNameIdentifier", "");
                siteElement.setAttribute("userIdIdentifier", "");
                siteElement.setAttribute("commentDynamicLoadLinkClass", "");
                siteElement.setAttribute("commentDateIdentifier", "");
                siteElement.setAttribute("commentTitleIdentifier", "");
                siteElement.setAttribute("commentIdIdentifier", "");
                siteElement.setAttribute("rawCommentBodyIdentifier", "");
                siteElement.setAttribute("commentHasQuoteConditional", "");
                siteElement.setAttribute("commentWithoutQuoteIdentifier", "");
                siteElement.setAttribute("quoteWithoutCommentIdentifier", "");
                siteElement.setAttribute("quoteTargetCommentIdIdentifier", "");
                siteElement.setAttribute("pagerIdentifier", "");
                siteElement.setAttribute("nextCommentPageButtonIdentifier", "");
                siteElement.setAttribute("resolveNextCommentPageIdentifier", "");
                siteElement.setAttribute("showMoreCommentsDynamicClass", "");
                siteElement.setAttribute("answerConditionalIdentifier", "");
                siteElement.setAttribute("answerTargetIdentifier", "");

                sitesDocument.getDocumentElement().appendChild(siteElement);

                siteLinkedList.add(siteLinkedList.size(), url);
            }
        });
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedSiteElement.setAttribute("url", urlTextField.getText());
                if (resolveForumURLCheckBox.isSelected()) {
                    selectedSiteElement.setAttribute("resolveForumUrl", "true");
                } else {
                    selectedSiteElement.setAttribute("resolveForumUrl", "false");
                }
                if (resolveArticleBeforeParsingCheckBox.isSelected()) {
                    selectedSiteElement.setAttribute("resolveArticleBeforeParsing", "true");
                } else {
                    selectedSiteElement.setAttribute("resolveArticleBeforeParsing", "false");
                }
                if (commentDynamicLoadCheckBox.isSelected()) {
                    selectedSiteElement.setAttribute("commentDynamicLoad", "true");
                } else {
                    selectedSiteElement.setAttribute("commentDynamicLoad", "false");
                }
                if (commentDynamicShowMoreCheckBox.isSelected()) {
                    selectedSiteElement.setAttribute("commentDynamicShowMore", "true");
                } else {
                    selectedSiteElement.setAttribute("commentDynamicShowMore", "false");
                }
                selectedSiteElement.setAttribute("timeout", timeoutTextField.getText());
                selectedSiteElement.setAttribute("resolveURLIdentifier", resolveForumURLTextField.getText());
                selectedSiteElement.setAttribute("articleHeadlineIntroIdentifier", headlineIntroTextField.getText());
                selectedSiteElement.setAttribute("articleHeadlineIdentifier", headlineTextField.getText());
                selectedSiteElement.setAttribute("articleIntroIdentifier", articleIntroTextField.getText());
                selectedSiteElement.setAttribute("articleAuthorNameIdentifier", authorNameTextField.getText());
                selectedSiteElement.setAttribute("articleAuthorIdIdentifier", authorIDTextField.getText());
                selectedSiteElement.setAttribute("articleBodyIdentifier", articleBodyTextField.getText());
                selectedSiteElement.setAttribute("articleResolveBeforeParsingLink", resolveArticleURLTextField.getText());
                selectedSiteElement.setAttribute("commentSectionIdentifier", commentSectionTextField.getText());
                selectedSiteElement.setAttribute("commentsIdentifier", commentsTextField.getText());
                selectedSiteElement.setAttribute("userNameIdentifier", userNameTextField.getText());
                selectedSiteElement.setAttribute("userIdIdentifier", userIDTextField.getText());
                selectedSiteElement.setAttribute("commentDynamicLoadLinkClass", dynamicExpandCommentTextField.getText());
                selectedSiteElement.setAttribute("commentDateIdentifier", commentDateTextField.getText());
                selectedSiteElement.setAttribute("commentTitleIdentifier", commentTitleTextField.getText());
                selectedSiteElement.setAttribute("commentIdIdentifier", commentIDTextField.getText());
                selectedSiteElement.setAttribute("rawCommentBodyIdentifier", rawCommentBodyTextField.getText());
                selectedSiteElement.setAttribute("commentHasQuoteConditional", quoteConditionalTextField.getText());
                selectedSiteElement.setAttribute("commentWithoutQuoteIdentifier", withoutQuoteTextField.getText());
                selectedSiteElement.setAttribute("quoteWithoutCommentIdentifier", withoutCommentTextField.getText());
                selectedSiteElement.setAttribute("quoteTargetCommentIdIdentifier", quoteTargetTextField.getText());
                selectedSiteElement.setAttribute("pagerIdentifier", "");
                selectedSiteElement.setAttribute("nextCommentPageButtonIdentifier", nextCommentPageButtonTextField.getText());
                selectedSiteElement.setAttribute("resolveNextCommentPageIdentifier", resolveNextCommentPageTextField.getText());
                selectedSiteElement.setAttribute("showMoreCommentsDynamicClass", dynamicShowMoreCommentstextField.getText());
                selectedSiteElement.setAttribute("answerConditionalIdentifier", answerConditionalTextField.getText());
                selectedSiteElement.setAttribute("answerTargetIdentifier", answerTargetTextField.getText());

                try {
                    StreamResult result = new StreamResult(new File("siteParameters" + File.separator + "siteParameters.xml"));
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(sitesDocument);
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                    transformer.transform(source, result);
                } catch (TransformerException e1) {
                    //TODO
                }
            }
        });
        exportierenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NamedNodeMap attributes = selectedSiteElement.getAttributes();

                String result = "";

                for (int i = 0; i < attributes.getLength(); i++) {
                    result += StringEscapeUtils.escapeXml(attributes.item(i).getNodeName()) + ", " + StringEscapeUtils.escapeXml(attributes.item(i).getNodeValue()) + ",";
                }
                result = result.substring(0, result.length() - 1);
                //System.out.println(result);
                Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                cb.setContents(new StringSelection(result), null);
            }
        });
        importierenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame inputFrame = new JFrame("Import");
                String input = JOptionPane.showInputDialog(inputFrame, "Input:");
                StringTokenizer st = new StringTokenizer(input, ",");

                Element siteNode = sitesDocument.createElement("site");
                while (st.hasMoreElements()) {
                    String str1 = st.nextToken();
                    String str2 = st.nextToken();
                    if (str2.equals(" ")){

                    } else {
                        str2 = str2.substring(1);
                    }
                    //System.out.println("str1 -" + str1 + "-");
                    //System.out.println("str2 -" + str2 + "-");
                    siteNode.setAttribute(StringEscapeUtils.unescapeXml(str1), StringEscapeUtils.unescapeXml(str2));
                }
                sitesDocument.getDocumentElement().appendChild(siteNode);
                siteLinkedList.add(siteLinkedList.size(), siteNode.getAttribute("url"));
            }
        });
        deleteSiteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sitesDocument.getDocumentElement().removeChild(selectedSiteElement);
                siteLinkedList.remove(selectedSiteIndex);
                siteList.clearSelection();

                Component[] components = {generalPanel, articlePanel, commentSectionPanel, commentsPanel};
                try {
                    for (Component c : components
                            ) {
                        Component[] components2 = (Component[]) c.getClass().getMethod("getComponents").invoke(c);

                        for (Component c2 : components2
                                ) {
                            if (c2.getClass() == JTextField.class) {
                                c2.getClass().getMethod("setText", new Class[]{String.class}).invoke(c2, "");
                            }

                        }
                    }
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                }

            }
        });
    }

    public static void open() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                buildFrame();
            }
        });
    }

    private static void buildFrame() {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Site Options");

        frame.setContentPane(new SitesOverviewWindow().panelMain);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        siteLinkedList = new DefaultListModel();
        siteList = new JList(siteLinkedList);
        File inputFile = new File("siteParameters" + File.separator + "siteParameters.xml");
        sitesDocument = null;
        try {
            sitesDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputFile);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
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

}

