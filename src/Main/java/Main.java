import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
//import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Stream;

public class Main {

    private JTextField inputFileTextField;
    private JPanel panelMain;
    private JButton browseInputFileButton;
    private JButton startButton;
    private JRadioButton inputFileRadioButton;
    private JRadioButton inputSingleArticleRadioButton;
    private JRadioButton inputSearchRadioButton;
    private JTextField singleArticleTextField;
    private JTextField searchTextField;
    private JComboBox<String> searchSiteComboBox;
    private JButton sitesOverviewButton;
    private JButton searchOptionsButton;
    private JTextField numberArticlesTextField;
    private final JFileChooser fc = new JFileChooser();

    private LinkedList<String> articleList;
    private String jobCategory;
    private String jobDetail;


    private DefaultComboBoxModel<String> searchSites;

    public Main() {
        browseInputFileButton.addActionListener(e -> {
            int returnVal = fc.showOpenDialog(panelMain);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                inputFileTextField.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });
        startButton.addActionListener(e -> startParseJob());
        sitesOverviewButton.addActionListener(e -> new Thread(SitesOverviewWindow::open).start());
        searchOptionsButton.addActionListener(e -> new Thread(SearchOverviewWindow::open).start());
        searchSiteComboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                new Thread(Main.this::initSearchComboBox).start();
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });
    }

    private void startParseJob() {
        try {
            initArticleList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ParseJob parseJob = new ParseJob();
        parseJob.open();
        parseJob.setJobCategoryLabel(jobCategory);
        parseJob.setJobDetailLabel(jobDetail);
        parseJob.start(articleList);
    }

    private void initArticleList() throws IOException {
        if (inputFileRadioButton.isSelected()) {
            String inputFile = fc.getName(fc.getSelectedFile());
            BufferedReader reader = Files.newBufferedReader(Paths.get(inputFile));
            Stream<String> links = reader.lines();
            Iterator<String> linkIterator = links.iterator();

            articleList = new LinkedList<>();
            while (linkIterator.hasNext()) {
                articleList.add(linkIterator.next());
            }
            jobCategory = "Parse from file (" + articleList.size() + " articles)";
            jobDetail = inputFile;

            try {
                reader.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }

        if (inputSingleArticleRadioButton.isSelected()) {
            articleList = new LinkedList<>();
            articleList.add(singleArticleTextField.getText());
            jobCategory = "Single article";
            jobDetail = singleArticleTextField.getText();
        }

        if (inputSearchRadioButton.isSelected()) {
            initArticleListFromSearch();
        }
    }

    private void initArticleListFromSearch() throws IOException {
        int searchModeArticleCount = Integer.parseInt(numberArticlesTextField.getText());

        String url = Objects.requireNonNull(searchSiteComboBox.getSelectedItem()).toString();
        jobCategory = "Search articles: " + url + ", " + searchModeArticleCount + " articles";
        String searchURLPrefix = "";
        String searchURLSuffix = "";
        LinkedList<String> searchResultArticleElementIdentifier = new LinkedList<>();
        LinkedList<String> searchResultArticleLinkIdentifier = new LinkedList<>();
        LinkedList<String> searchResultNextPageIdentifier = new LinkedList<>();
        try {
            File searchSitesFile = new File("siteParameters" + File.separator + "searchParameters.xml");
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(searchSitesFile);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("site");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node currentNode = nodeList.item(i);

                if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) currentNode;


                    NamedNodeMap attributes = eElement.getAttributes();
                    if (eElement.getAttribute("url").equals(url)) {
                        for (int j = 0; j < attributes.getLength(); j++) {
                            switch (attributes.item(j).getNodeName()) {
                                case "searchURLPrefix":
                                    searchURLPrefix = attributes.item(j).getNodeValue();
                                    break;
                                case "searchURLSuffix":
                                    searchURLSuffix = attributes.item(j).getNodeValue();
                                    break;
                                case "searchResultArticleElementIdentifier":
                                    String arg = attributes.item(j).getNodeValue();
                                    while (arg.contains(";")) {
                                        searchResultArticleElementIdentifier.add(arg.substring(0, arg.indexOf(";")));
                                        arg = arg.substring(arg.indexOf(";") + 1);
                                    }
                                    searchResultArticleElementIdentifier.add(arg);
                                    break;
                                case "searchResultArticleLinkIdentifier":
                                    arg = attributes.item(j).getNodeValue();
                                    while (arg.contains(";")) {
                                        searchResultArticleLinkIdentifier.add(arg.substring(0, arg.indexOf(";")));
                                        arg = arg.substring(arg.indexOf(";") + 1);
                                    }
                                    searchResultArticleLinkIdentifier.add(arg);
                                    break;
                                case "searchResultNextPageIdentifier":
                                    arg = attributes.item(j).getNodeValue();
                                    while (arg.contains(";")) {
                                        searchResultNextPageIdentifier.add(arg.substring(0, arg.indexOf(";")));
                                        arg = arg.substring(arg.indexOf(";") + 1);
                                    }
                                    searchResultNextPageIdentifier.add(arg);
                                    break;
                            }
                        }
                    }
                }
            }
        } catch (SAXException | IOException | ParserConfigurationException e1) {
            e1.printStackTrace();
        }
        org.jsoup.nodes.Document searchResultPage = Jsoup.connect(searchURLPrefix + searchTextField.getText() + searchURLSuffix).get();
        jobDetail = "Search for \"" + searchTextField.getText() + "\"";

        articleList = new LinkedList<>();
        int timeout = 2000;

        Elements searchTeaserElements = null;
        try {
            searchTeaserElements = (Elements) ArticleObj.resolveDomPath(searchResultPage, searchResultArticleElementIdentifier, timeout);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //System.out.println(searchTeaserElements);

        if (searchTeaserElements.size() == 0) {
            JOptionPane.showMessageDialog(null, "No articles found, check search term.");
        } else {

            if (searchModeArticleCount <= searchTeaserElements.size()) {
                for (int i = 0; i < searchModeArticleCount; i++) {
                    try {
                        articleList.add((String) ArticleObj.resolveDomPath(searchTeaserElements.get(i), searchResultArticleLinkIdentifier, timeout));
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                int parsedArticles = 0;

                try {
                    while (parsedArticles < searchModeArticleCount) {
                        for (org.jsoup.nodes.Element searchTeaserElement : searchTeaserElements) {
                            if (parsedArticles < searchModeArticleCount) {
                                parsedArticles++;
                                articleList.add((String) ArticleObj.resolveDomPath(searchTeaserElement, searchResultArticleLinkIdentifier, timeout));
                            }

                        }
                        System.out.println((String) ArticleObj.resolveDomPath(searchResultPage, searchResultNextPageIdentifier, timeout));
                        searchResultPage = Jsoup.connect((String) ArticleObj.resolveDomPath(searchResultPage, searchResultNextPageIdentifier, timeout)).get();
                        Thread.sleep(timeout);
                        searchTeaserElements = (Elements) ArticleObj.resolveDomPath(searchResultPage, searchResultArticleElementIdentifier, timeout);
                        //System.out.println(searchTeaserElements);
                    }
                } catch (NoSuchMethodException | InvocationTargetException | InterruptedException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        //System.out.println(articleList);//TODO
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::buildFrame);
    }

    private static void buildFrame() {
        JFrame frame = new JFrame("CommentCrawler");
        frame.setContentPane(new Main().panelMain);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createUIComponents() {
        initSearchComboBox();
        searchSiteComboBox = new JComboBox<>(searchSites);
    }

    private void initSearchComboBox() {
        try {
            File searchSitesFile = new File("siteParameters" + File.separator + "searchParameters.xml");
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(searchSitesFile);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("site");
            searchSites = new DefaultComboBoxModel<>();
            searchSites.removeAllElements();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node currentNode = nodeList.item(i);

                if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) currentNode;

                    NamedNodeMap attributes = eElement.getAttributes();


                    for (int j = 0; j < attributes.getLength(); j++) {

                        if (attributes.item(j).getNodeName().equals("url")) {
                            searchSites.addElement(attributes.item(j).getNodeValue());
                        }
                    }
                }
            }
        } catch (SAXException | IOException | ParserConfigurationException e1) {
            e1.printStackTrace();
        }
    }

}
