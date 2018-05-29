package Main;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;
import com.machinepublishers.jbrowserdriver.UserAgent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebElement;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ArticleObj {
    private int articleCount;

    private ParseJob callingObject;
    private String articleURL;
    private org.w3c.dom.Document doc;
    private Document rawDoc = null;
    private Document parsedDoc = null;
    private Element commentSection = null;
    private Elements comments = null;
    private Integer timeout;

    private int commentPages = 0;
    private int commentSectionExpands = 0;
    private int commentExpands = 0;

    //declare parsing parameters
    private boolean resolveForumUrl = false;
    private boolean resolveArticleBeforeParsing = false;
    private boolean commentDynamicLoad = false;
    private boolean commentDynamicShowMore = false;

    private LinkedList<String> commentDynamicLoadLinkClass = new LinkedList<>();
    private LinkedList<String> showMoreCommentsDynamicClass = new LinkedList<>();

    private LinkedList<String> resolveURLIdentifier = new LinkedList<>();
    private LinkedList<String> articleHeadlineIntroIdentifier = new LinkedList<>();
    private LinkedList<String> articleHeadlineIdentifier = new LinkedList<>();
    private LinkedList<String> articleIntroIdentifier = new LinkedList<>();
    private LinkedList<String> articleAuthorNameIdentifier = new LinkedList<>();
    private LinkedList<String> articleAuthorIdIdentifier = new LinkedList<>();
    private LinkedList<String> articleBodyIdentifier = new LinkedList<>();
    private LinkedList<String> articleResolveBeforeParsingLink = new LinkedList<>();
    private LinkedList<String> commentSectionIdentifier = new LinkedList<>();
    private LinkedList<String> commentsIdentifier = new LinkedList<>();
    private LinkedList<String> userNameIdentifier = new LinkedList<>();
    private LinkedList<String> userIdIdentifier = new LinkedList<>();
    private LinkedList<String> commentDateIdentifier = new LinkedList<>();
    private LinkedList<String> commentTitleIdentifier = new LinkedList<>();
    private LinkedList<String> commentIdIdentifier = new LinkedList<>();
    private LinkedList<String> rawCommentBodyIdentifier = new LinkedList<>();
    private LinkedList<String> articleNextPageLinkIdentifier = new LinkedList<>();
    private LinkedList<String> commentHasQuoteConditional = new LinkedList<>();
    private LinkedList<String> commentWithoutQuoteIdentifier = new LinkedList<>();
    private LinkedList<String> quoteWithoutCommentIdentifier = new LinkedList<>();
    private LinkedList<String> quoteTargetCommentIdIdentifier = new LinkedList<>();
    private LinkedList<String> pagerIdentifier = new LinkedList<>();
    private LinkedList<String> nextCommentPageButtonIdentifier = new LinkedList<>();
    private LinkedList<String> resolveNextCommentPageIdentifier = new LinkedList<>();
    private LinkedList<String> answerConditionalIdentifier = new LinkedList<>();
    private LinkedList<String> answerTargetIdentifier = new LinkedList<>();

    ArticleObj(String url, ParseJob job, org.w3c.dom.Document doc, int articleCount) {
        this.articleURL = url;
        this.callingObject = job;
        this.doc = doc;
        this.articleCount = articleCount;
    }

    private int getArticleCount() {
        return articleCount;
    }

    private int init() throws IOException, IllegalAccessException, ParserConfigurationException, SAXException, NoSuchFieldException {
        rawDoc = Jsoup.connect(articleURL).get();
        articleCount++;
        boolean found = false;

        //Domainname isolieren
        String domain = articleURL;

        if (domain.contains("//")) {
            domain = domain.substring(domain.indexOf("//") + 2);
        }

        if (domain.contains("/")) {
            domain = (String) domain.subSequence(0, domain.indexOf("/"));
        }

        //init parsing parameters

        File inputFile = new File("siteParameters" + File.separator + "siteParameters.xml");
        org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputFile);
        document.getDocumentElement().normalize();

        NodeList nodeList = document.getElementsByTagName("site");

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);

            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                org.w3c.dom.Element eElement = (org.w3c.dom.Element) currentNode;

                NamedNodeMap attributes = eElement.getAttributes();

                if (attributes.getNamedItem("url").getNodeValue().equals(domain)) {
                    found = true;

                    for (int j = 0; j < attributes.getLength(); j++) {

                        String s = attributes.item(j).toString();

                        if (s.startsWith("url")) {
                            continue;
                        }

                        String key = s.substring(0, s.indexOf("="));
                        String value = s.substring(s.indexOf("=") + 2, s.length() - 1);


                        LinkedList<String> tempList = new LinkedList<>();
                        Boolean tempBool;
                        while (value.contains(";")) {

                            String nextToken = value.substring(0, value.indexOf(";"));
                            value = value.substring(value.indexOf(";") + 1);
                            tempList.add(nextToken);


                        }
                        if (!value.isEmpty()) {
                            tempList.add(value);
                        }
                        tempBool = value.equals("true");

                        Class c = this.getClass();
                        Field field = c.getDeclaredField(key);
                        if (field.getType() == LinkedList.class) {
                            field.set(this, tempList);
                        }
                        if (value.equals("true") || value.equals("false")) {
                            field.setBoolean(this, tempBool);
                            //field.set(this, new Boolean(value));
                        }
                        if (field.getType() == Integer.class) {
                            field.set(this, new Integer(value));
                        }
                        if (field.getType() == String.class) {
                            field.set(this, value);
                        }

                    }
                }

            }

        }
        if (found) {
            return 0;
        } else {
            return 1; //TODO
        }


    }

    public org.w3c.dom.Element parseToXml() throws IOException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, ParserConfigurationException, SAXException, NoSuchFieldException, InterruptedException {

        int initReturnValue = init();

        JBrowserDriver driver = null;
        if (commentDynamicLoad || commentDynamicShowMore) {
            driver = new JBrowserDriver(Settings.builder().loggerLevel(null).userAgent(UserAgent.CHROME).headless(true).build());//TODO
        }

        org.w3c.dom.Element result = doc.createElement("article");

        result.setAttribute("id", String.valueOf(getArticleCount()));
        result.setAttribute("url", articleURL);

        //parse article
        callingObject.setStatus("parsing ...");

        //System.out.println(initReturnValue);
        if (initReturnValue == 1) {
            result.appendChild(doc.createTextNode("no Parameters found for domain in siteParameters.xml"));
            callingObject.printToTextArea("Article #" + articleCount + "/" + callingObject.getTotalArticleCount() + " aborted. No Parameters found.");
            return result;
        }

        org.w3c.dom.Element articleElement = doc.createElement("original-article");

        Document tempDoc = rawDoc;
        if (resolveArticleBeforeParsing) {
            Object obj = resolveDomPath(tempDoc, articleResolveBeforeParsingLink, timeout);
            if (obj != null) {
                tempDoc = Jsoup.connect((String) obj).get();
            }
        }

        //author
        org.w3c.dom.Element author = doc.createElement("author");
        if (articleAuthorNameIdentifier.size() > 0) {
            org.w3c.dom.Element authorName = doc.createElement("authorName");
            authorName.appendChild(doc.createTextNode((String) resolveDomPath(tempDoc, articleAuthorNameIdentifier, timeout))); //TODO resolve[...] gibt manchmal Document zurück
            author.appendChild(authorName);
        }
        if (articleAuthorIdIdentifier.size() > 0) {
            LinkedList<String> temp = articleAuthorIdIdentifier;
            LinkedList<String> last = new LinkedList<>();
            last.add(temp.pollLast());
            Object obj = resolveDomPath(tempDoc, temp, timeout);
            if (obj == null) {
                org.w3c.dom.Element authorId = doc.createElement("authorId");
                author.appendChild(authorId);
                authorId.appendChild(doc.createTextNode("N/A"));
            } else {
                if (obj.getClass() == Element.class) {
                    org.w3c.dom.Element authorId = doc.createElement("authorId");
                    author.appendChild(authorId);
                    authorId.appendChild(doc.createTextNode((String) resolveDomPath(obj, last, timeout)));
                } else {
                    ArrayList arrayList = (Elements) obj;
                    for (Object o : arrayList
                            ) {
                        org.w3c.dom.Element authorId = doc.createElement("authorId");
                        author.appendChild(authorId);
                        authorId.appendChild(doc.createTextNode((String) resolveDomPath(o, last, timeout)));
                    }
                }
            }
        }
        articleElement.appendChild(author);

        //headline
        org.w3c.dom.Element headline = doc.createElement("headline");
        if (articleHeadlineIntroIdentifier.size() > 0) {
            org.w3c.dom.Element headlineIntro = doc.createElement("headline-intro");
            headline.appendChild(headlineIntro);
            headlineIntro.appendChild(doc.createTextNode((String) resolveDomPath(tempDoc, articleHeadlineIntroIdentifier, timeout)));
        }
        if (articleHeadlineIdentifier.size() > 0) {
            org.w3c.dom.Element headlineBody = doc.createElement("headline-body");
            headline.appendChild(headlineBody);
            headlineBody.appendChild(doc.createTextNode((String) resolveDomPath(tempDoc, articleHeadlineIdentifier, timeout)));
        }
        articleElement.appendChild(headline);

        //body
        org.w3c.dom.Element body = doc.createElement("article-body");
        if (articleIntroIdentifier.size() > 0) {
            org.w3c.dom.Element articleIntro = doc.createElement("article-intro");
            body.appendChild(articleIntro);
            articleIntro.appendChild(doc.createTextNode((String) resolveDomPath(tempDoc, articleIntroIdentifier, timeout)));
        }

        if (articleBodyIdentifier.size() > 0) {
            org.w3c.dom.Element articleText = doc.createElement("article-text");
            body.appendChild(articleText);
            articleText.appendChild(doc.createTextNode((String) resolveDomPath(tempDoc, articleBodyIdentifier, timeout)));
        }

        articleElement.appendChild(body);

        //append article
        result.appendChild(articleElement);


        if ((resolveForumUrl) && resolveDomPath(rawDoc, resolveURLIdentifier, timeout)!=null) {
            rawDoc = Jsoup.connect((String) resolveDomPath(rawDoc, resolveURLIdentifier, timeout)).get();
        }

        int commentCount = 0;
        org.w3c.dom.Element commentsElement = doc.createElement("Comments");

        if (resolveDomPath(rawDoc, commentSectionIdentifier, timeout) == null) {
            commentsElement.appendChild(doc.createTextNode("No comment section was found."));
            result.appendChild(commentsElement);
            callingObject.printToTextArea("Article #" + articleCount + "/" + callingObject.getTotalArticleCount() + " completed. No comment section was found.");
            return result;
        }

        //parse comments
        String currentURL = rawDoc.location(); //TODO war [...] = articleURL
        //System.out.println("------------------------------- currentURL:\n" + currentURL);//TODO
        callingObject.printToTextArea("Processing article #" + articleCount + "/" + callingObject.getTotalArticleCount() + " ...");
        while (true) {

            if (commentDynamicShowMore || commentDynamicLoad) {
                driver.get(currentURL);
            }
            int dynamicCounter = 0;
            if (commentDynamicShowMore) {
                List<WebElement> elemList;
                while (true) {
                    Thread.sleep(timeout); //TODO '*3' wurde entfernt
                    try {
                        callingObject.setStatus("Expanding comment section ...");
                        //callingObject.printToTextArea("\rProcessing article #" + articleCount + "/" + callingObject.getTotalArticleCount() + ", expanding comment section ...");//(" + dynamicCounter + ")
                        dynamicCounter++;
                        //System.out.println(showMoreCommentsDynamicClass);//TODO
                        Iterator<String> it = showMoreCommentsDynamicClass.iterator();
                        Thread.sleep(timeout);
                        elemList = driver.findElementsByClassName(it.next());
                        while (it.hasNext()) {
                            elemList.addAll(driver.findElementsByClassName(it.next()));
                        }

                        //System.out.println(elemList); //TODO
                        //elemList = driver.findElementsByXPath(showMoreCommentsDynamicClass);
                        if (elemList != null) {
                            for (WebElement we : elemList
                                    ) {
                                try {
                                    if (we.isDisplayed()) {
                                        //System.out.println(we.getText());
                                        we.click();
                                        callingObject.setCommentSectionExpandsLabel(String.valueOf(++commentSectionExpands));
                                    }
                                } catch (Exception ignored) {

                                }
                            }
                        }
                        Thread.sleep(timeout);
                        if (elemList == null | elemList.isEmpty()) {
                            break;
                        }
                    } catch (InterruptedException e) {
                        //System.out.println("test");
                    }
                }
            }

            dynamicCounter = 0;
            if (commentDynamicLoad) {
                List<WebElement> elemList = null;
                try {
                    Iterator<String> it = commentDynamicLoadLinkClass.iterator();
                    Thread.sleep(timeout);
                    elemList = driver.findElementsByClassName(it.next());
                    while (it.hasNext()) {
                        elemList.addAll(driver.findElementsByClassName(it.next()));
                    }
                    for (WebElement we : elemList
                            ) {
                        if (we.isDisplayed()) {
                            we.click();
                            callingObject.setCommentExpandsLabel(String.valueOf(++commentExpands));
                            callingObject.setStatus("Expanding comments ...");
                            //callingObject.printToTextArea("\rProcessing article #" + articleCount + "/" + callingObject.getTotalArticleCount() + ", expanding comments (" + dynamicCounter + ") ...");
                            dynamicCounter++;
                        }
                        Thread.sleep(timeout);
                    }
                } catch (Exception e) {

                }


            }

            if (commentDynamicShowMore || commentDynamicLoad) {
                rawDoc = Jsoup.parse(driver.getPageSource());
            }

            //System.out.println("------------------------------- rawdoc:\n" + rawDoc); //TODO

            Object commentSection = resolveDomPath(rawDoc, commentSectionIdentifier, timeout);
            //System.out.println("------------------------------- commentsection:\n" + commentSection);//TODO

            Elements comments = (Elements) resolveDomPath(commentSection, commentsIdentifier, timeout);
            //System.out.println("------------------------------- comments:\n" + comments);//TODO

            if (comments != null) {
                for (Element currentElement : comments
                        ) {
                    org.w3c.dom.Element comment = doc.createElement("Comment");
                    commentCount++;

                    //userName
                    if (userNameIdentifier.size() > 0) {
                        org.w3c.dom.Element userName = doc.createElement("userName");
                        userName.appendChild(doc.createTextNode((String) resolveDomPath(currentElement, userNameIdentifier, timeout)));
                        comment.appendChild(userName);
                    }

                    //userID
                    if (userIdIdentifier.size() > 0) {
                        org.w3c.dom.Element userID = doc.createElement("userID");
                        userID.appendChild(doc.createTextNode((String) resolveDomPath(currentElement, userIdIdentifier, timeout)));
                        comment.appendChild(userID);
                    }

                    //commentDate
                    if (commentDateIdentifier.size() > 0) {
                        org.w3c.dom.Element commentDate = doc.createElement("commentDate");
                        commentDate.appendChild(doc.createTextNode((String) resolveDomPath(currentElement, commentDateIdentifier, timeout)));
                        comment.appendChild(commentDate);
                    }

                    //commentTitle
                    if (commentTitleIdentifier.size() > 0) {
                        org.w3c.dom.Element commentTitle = doc.createElement("commentTitle");
                        commentTitle.appendChild(doc.createTextNode((String) resolveDomPath(currentElement, commentTitleIdentifier, timeout)));
                        comment.appendChild(commentTitle);
                    }

                    //commentID
                    if (commentIdIdentifier.size() > 0) {
                        org.w3c.dom.Element commentID = doc.createElement("commentId");
                        commentID.appendChild(doc.createTextNode((String) resolveDomPath(currentElement, commentIdIdentifier, timeout)));
                        comment.appendChild(commentID);
                    }

                    //answer
                    if (answerConditionalIdentifier.size() > 0 && (Boolean) resolveDomPath(currentElement, answerConditionalIdentifier, timeout)) {
                        org.w3c.dom.Element answerElement = doc.createElement("answerTo");
                        String answerTarget = (String) resolveDomPath(currentElement, answerTargetIdentifier, timeout);
                        if (answerTarget != null) {
                            if (answerTarget.contains("#")) {
                                answerTarget = answerTarget.substring(answerTarget.indexOf("#") + 1);
                            }
                            answerElement.appendChild(doc.createTextNode(answerTarget));
                            comment.appendChild(answerElement);
                        }
                    }

                    //commentBody
                    org.w3c.dom.Element rawCommentBody = doc.createElement("commentBody");
                    comment.appendChild(rawCommentBody);

                    if (resolveDomPath(currentElement, commentHasQuoteConditional, timeout) != null && (Boolean) resolveDomPath(currentElement, commentHasQuoteConditional, timeout)) {
                        org.w3c.dom.Element commentQuote = doc.createElement("quote");
                        commentQuote.appendChild(doc.createTextNode((String) resolveDomPath(currentElement, quoteWithoutCommentIdentifier, timeout)));
                        String quoteTarget = (String) resolveDomPath(currentElement, quoteTargetCommentIdIdentifier, timeout);
                        if (Objects.requireNonNull(quoteTarget).contains("#")) { //TODO auf generisch ändern
                            quoteTarget = quoteTarget.substring(quoteTarget.indexOf("#") + 1);
                            commentQuote.setAttribute("target", quoteTarget);
                        }
                        rawCommentBody.appendChild(commentQuote);
                    }
                    org.w3c.dom.Element commentBody = doc.createElement("comment");
                    commentBody.appendChild(doc.createTextNode((String) resolveDomPath(currentElement, commentWithoutQuoteIdentifier, timeout)));
                    rawCommentBody.appendChild(commentBody);

                    commentsElement.appendChild(comment);
                    //callingObject.printToTextArea("\rProcessing article #" + articleCount + "/" + callingObject.getTotalArticleCount() + ", comment #" + commentCount++ + " ...");
                }
            }

            //Konsolenausgabe
            Elements links = new Elements();
            Thread.sleep(1000);
            //System.out.println(nextCommentPageButtonIdentifier.size());
            if (nextCommentPageButtonIdentifier.size() > 0) {
                links = (Elements) resolveDomPath(rawDoc, nextCommentPageButtonIdentifier, timeout);
            }
            //TODO

            if ((links == null) || (links.isEmpty())) {
                callingObject.printToTextArea("\rArticle #" + articleCount + "/" + callingObject.getTotalArticleCount() + " completed. (" + commentCount + " comments)");
                break;
            } else {
                Thread.sleep(timeout);
                currentURL = (String) resolveDomPath(rawDoc, resolveNextCommentPageIdentifier, timeout);
                //System.out.println(currentURL);
                callingObject.setCommentPagesLabel(String.valueOf(++commentPages));
                rawDoc = Jsoup.connect(currentURL).get();
            }

        }

        result.appendChild(commentsElement);
        if (driver != null) {
            driver.quit();
        }
        return result;

    }

    protected static Object resolveDomPath(Object input, LinkedList<String> navigationSteps, int timeout)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException, IOException {
        Iterator<String> it = navigationSteps.iterator();
        if (navigationSteps.size() == 0) {
            return input;
        }
        while (it.hasNext()) {
            String nextElement = it.next();
            String nextMethod = "";
            String nextArg = "";

            nextMethod = nextElement.substring(0, nextElement.indexOf("("));
            nextArg = nextElement.substring(nextElement.indexOf("(") + 1, nextElement.lastIndexOf(")"));
//            System.out.println("e: " + nextElement);
//            System.out.println("m: " + nextMethod);
//            System.out.println("a: " + nextArg);
//            System.out.println(navigationSteps);

            //System.out.println(input);

            if (nextElement.equals("resolve()")) {
                input = Jsoup.connect((String) input).get();
                Thread.sleep(timeout);
                //System.out.println("resolve");
                continue;
            }

            if (nextMethod.equals("get")){
                //System.out.println(((Elements) input).size());
                input = ((Elements) input).get(Integer.parseInt(nextArg));
                continue;
            }

            Class c = null;
            if (input == null) {
                return null;
            }
            /*try {*/
            c = input.getClass();
            /*} catch (Exception e) {
//                System.out.println("e: " + nextElement);
//                System.out.println("m: " + nextMethod);
//                System.out.println("a: " + nextArg);
//                System.out.println(navigationSteps);
            }*/
            Method m = null;

            if (nextArg.isEmpty()) {
                m = c.getMethod(nextMethod, new Class[]{});
            } else {
                m = c.getMethod(nextMethod, new Class[]{String.class});
            }

            if (nextArg.isEmpty()) {
                input = m.invoke(input);
            } else {
                input = m.invoke(input, new Object[]{new String(nextArg)});
            }
        }
        return input;
    }

}
