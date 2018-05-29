package Main;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Objects;

public class ParseJob {
    private JProgressBar parseProgressBar;
    private JTextField outputFileTextField;
    private JButton copyOutputFilePathButton;
    private JButton copyLogFilePathButton;
    private JPanel panelParseJob;
    private JLabel totalArticlesLabel;
    private JLabel completedArticlesLabel;
    private JLabel failedArticlesLabel;
    private JTextArea logTextArea;
    private String logTextAreaString;
    private JLabel statusLabel;
    private JLabel jobCategoryLabel;
    private JLabel jobDetailLabel;
    private JLabel commentPagesLabel;
    private JLabel commentSectionExpandsLabel;
    private JLabel commentExpandsLabel;
    //private BufferedReader reader = null;
    private LinkedList<String> articleList;
    private int totalArticleCount;

    public int getTotalArticleCount() {
        return totalArticleCount;
    }

    ParseJob() {
        copyOutputFilePathButton.addActionListener(e -> {
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(new StringSelection(outputFileTextField.getText()), null);
        });
        copyLogFilePathButton.addActionListener(e -> {
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(new StringSelection(logTextArea.getText()), null);
        });
    }

    public void open() {
        SwingUtilities.invokeLater(this::buildFrame);
    }

    private void buildFrame() {
        JFrame frame = new JFrame("ParseJob");

        frame.setContentPane(this.panelParseJob);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void start(LinkedList<String> input) {
        articleList = input;
        new Thread(this::startParsing).start();
    }

    public void printToTextArea(String str) {
        SwingUtilities.invokeLater(() -> {
            if (logTextAreaString == null) {
                logTextAreaString = "";
            }
            if (str.startsWith("\r")) {
                if (logTextAreaString.lastIndexOf("\n") > 0) {
                    logTextAreaString = logTextAreaString.substring(0, logTextArea.getText().lastIndexOf("\n")) + "\n" + str;
                }
            } else {
                logTextAreaString = logTextAreaString + "\n" + str;
            }
            logTextArea.setText(logTextAreaString);
        });
    }

    public void setStatus(String str) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(str));
    }

    public void setJobCategoryLabel(String str) {
        SwingUtilities.invokeLater(() -> jobCategoryLabel.setText(str));
    }

    public void setJobDetailLabel(String str) {
        SwingUtilities.invokeLater(() -> jobDetailLabel.setText(str));
    }

    public void setCommentPagesLabel(String str) {
        SwingUtilities.invokeLater(() -> commentPagesLabel.setText(str));
    }

    public void setCommentSectionExpandsLabel(String str) {
        SwingUtilities.invokeLater(() -> commentSectionExpandsLabel.setText(str));
    }

    public void setCommentExpandsLabel(String str) {
        SwingUtilities.invokeLater(() -> commentExpandsLabel.setText(str));
    }

    private void incCompletedArticlesLabel() {
        SwingUtilities.invokeLater(() -> completedArticlesLabel.setText(String.valueOf(Integer.parseInt(completedArticlesLabel.getText()) + 1)));
    }

    private void incFailedArticlesLabel() {
        SwingUtilities.invokeLater(() -> failedArticlesLabel.setText(String.valueOf(Integer.parseInt(completedArticlesLabel.getText()) + 1)));
    }

    private void startParsing() {
        totalArticleCount = articleList.size();
        SwingUtilities.invokeLater(() -> totalArticlesLabel.setText(String.valueOf(totalArticleCount)));

        LinkedList<String[]> failedArticlesList = new LinkedList<>();

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ignored) {
        }

        //root element
        Document doc = Objects.requireNonNull(docBuilder).newDocument();
        Element rootElement = doc.createElement("output");
        doc.appendChild(rootElement);
        rootElement.setAttribute("date", String.valueOf(LocalDateTime.now()));

        SwingUtilities.invokeLater(() -> {
            parseProgressBar.setIndeterminate(false);
            parseProgressBar.setMinimum(0);
            parseProgressBar.setMaximum(totalArticleCount);
            parseProgressBar.setValue(0);
            parseProgressBar.setStringPainted(true);
        });

        int articleCount = 0;
        for (String currentLink : articleList
                ) {
            boolean failed = false;
            Element articleElement = null;
            ArticleObj ao = new ArticleObj(currentLink, this, doc, articleCount++);
            try {
                articleElement = ao.parseToXml();
                rootElement.appendChild(articleElement);//TODO
                incCompletedArticlesLabel();
            } catch (IllegalArgumentException | IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                    ParserConfigurationException | NoSuchFieldException | InterruptedException |
                    SAXException | org.openqa.selenium.WebDriverException e) {
                StackTraceElement[] stack = e.getStackTrace();
                String exception = e.getMessage() + "\n\t\t";
                for (StackTraceElement s : stack) {
                    exception += s.toString() + "\n\t\t";
                }
                printToTextArea("\n");
                failedArticlesList.add(new String[]{currentLink, exception});
                failed = true;
                //e.printStackTrace();
                printToTextArea("\rArticle #" + articleCount + "/" + getTotalArticleCount() + " aborted with error.");
            } finally {
                setStatus("Done.");
            }

            if (failed) {
                incFailedArticlesLabel();
            }

            if (articleElement != null) {
                rootElement.appendChild(articleElement);
            }
            incProgressBar();
        }

        if (failedArticlesList.size() > 0) {
            Element failedArticles = doc.createElement("failedArticles");
            rootElement.appendChild(failedArticles);

            for (String[] aFailedArticlesList : failedArticlesList) {
                Element articleNode = doc.createElement("article");
                //articleNode.setAttribute("id", String.valueOf(articleCount));
                articleNode.setAttribute("url", aFailedArticlesList[0]);
                failedArticles.appendChild(articleNode);

                //Element articleNodeURL = doc.createElement("articleURL");
                //articleNode.appendChild(articleNodeURL);
                //articleNodeURL.appendChild(doc.createTextNode(aFailedArticlesList[0]));

                Element stackTrace = doc.createElement("stackTrace");
                articleNode.appendChild(stackTrace);
                String stackTraceStr = aFailedArticlesList[1];
                stackTraceStr = stackTraceStr.replace("\"", "&quot;");
                stackTraceStr = stackTraceStr.replace("&", "&amp;");
                stackTraceStr = stackTraceStr.replace("\'", "&apos;");
                stackTraceStr = stackTraceStr.replace("<", "&lt;");
                stackTraceStr = stackTraceStr.replace(">", "&gt;");
                stackTrace.appendChild(doc.createTextNode(stackTraceStr));

            }
        }

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            doc.normalize();
            Source source = new DOMSource(doc);
            //System.out.println(doc.getDocumentElement().toString());
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd HH_mm_ss");
            LocalDateTime now = LocalDateTime.now();
            String fileName = "output_" + dtf.format(now) + ".xml";
            Result result = new StreamResult(new File(fileName));
            //System.out.println("file: " + fileName);
            //System.out.println("source:\n" + source.toString());
            //System.out.println("doc:\n" + doc.getTextContent());
            //System.out.println("result: " + result);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(source, result);

            SwingUtilities.invokeLater(() -> outputFileTextField.setText(new File(fileName).getAbsolutePath()));

        } catch (TransformerException e) {
            StackTraceElement[] stack = e.getStackTrace();
            String exception = e.getMessage() + "\n\t\t";
            for (StackTraceElement s : stack) {
                exception += s.toString() + "\n\t\t";
            }
            JOptionPane.showMessageDialog(null, exception);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
    }

    private void incProgressBar() {
        SwingUtilities.invokeLater(() -> parseProgressBar.setValue(parseProgressBar.getValue() + 1));
    }
}
