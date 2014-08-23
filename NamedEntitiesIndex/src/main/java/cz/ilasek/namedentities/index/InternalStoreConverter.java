package cz.ilasek.namedentities.index;

import info.bliki.htmlcleaner.ContentToken;
import info.bliki.htmlcleaner.TagNode;
import info.bliki.htmlcleaner.Utils;
import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.filter.WPList;
import info.bliki.wiki.filter.WPTable;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.model.ImageFormat;
import info.bliki.wiki.tags.HTMLTag;
import info.bliki.wiki.tags.PTag;
import info.bliki.wiki.tags.WPATag;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class InternalStoreConverter implements ITextConverter {

    private boolean noLinks;
    private final List<Paragraph> paragraphs;
    private Paragraph currentParagraph;

    public InternalStoreConverter() {
        this(false);
    }

    public InternalStoreConverter(boolean noLinks) {
        this.noLinks = noLinks;
        paragraphs = new LinkedList<Paragraph>();
    }
    
    public boolean isNoLinks() {
        return noLinks;
    }
    
    public void setNoLinks(boolean noLinks) {
        this.noLinks = noLinks;
    }

    public List<Paragraph> getParagraphs() {
        return paragraphs;
    }

    @Override
    public void nodesToText(List<? extends Object> nodes,
            Appendable resultBuffer, IWikiModel model) throws IOException {
        if (nodes != null && !nodes.isEmpty()) {
            try {
                int level = model.incrementRecursionLevel();

                if (level > Configuration.RENDERER_RECURSION_LIMIT) {
                    resultBuffer
                            .append("Error - recursion limit exceeded rendering tags in PlainTextConverter#nodesToText().");
                    return;
                }

                Iterator<? extends Object> childrenIt = nodes.iterator();
                while (childrenIt.hasNext()) {
                    Object item = childrenIt.next();
                    if (item != null) {
                        if (item instanceof List) {
                            nodesToText((List) item, resultBuffer, model);
                        } else if (item instanceof ContentToken) {
                            ContentToken contentToken = (ContentToken) item;
                            String content = contentToken.getContent();
                            Utils.escapeXmlToBuffer(content, resultBuffer,
                                    true, true, true);
                        } else if (item instanceof WPList) {
                            ((WPList) item).renderPlainText(this, resultBuffer,
                                    model);
                        } else if (item instanceof WPTable) {
                            ((WPTable) item).renderPlainText(this,
                                    resultBuffer, model);
                        } else if (item instanceof HTMLTag) {
                            nodeToHTML((HTMLTag) item, resultBuffer, model);
                        } else if (item instanceof TagNode) {
                            TagNode node = (TagNode) item;
                            Map<String, Object> map = node
                                    .getObjectAttributes();
                            if (map != null && map.size() > 0) {
                            } else {
                                node.getBodyString(resultBuffer);
                            }
                        }
                    }
                }
            } finally {
                model.decrementRecursionLevel();
            }
        }

    }

    protected void nodeToHTML(HTMLTag item, Appendable resultBuffer,
            IWikiModel model) throws IOException {
        List<Object> children = item.getChildren();

        if (item instanceof PTag) {
            resultBuffer = new StringBuffer();
            item.getBodyString(resultBuffer);
            currentParagraph = new Paragraph(resultBuffer.toString());
            paragraphs.add(currentParagraph);
        }
        
        if (children.size() != 0) {
            nodesToText(children, resultBuffer, model);
        }
        
        if ((item instanceof WPATag) && (currentParagraph != null)) {
            Map<String, String> tagAtttributes = item.getAttributes();
            
            String target = "";
            
            for (Entry<String, String> currEntry : tagAtttributes.entrySet()) {
                String attName = currEntry.getKey();
                if (attName.length() >= 1 && attName.equals("href")) {
                    target = currEntry.getValue();
                }
            }
            Link link = new Link(currentParagraph, target, ((WPATag) item).getBodyString());
            currentParagraph.addLink(link);
        }
    }

    @Override
    public void imageNodeToText(TagNode imageTagNode, ImageFormat imageFormat,
            Appendable resultBuffer, IWikiModel model) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean noLinks() {
        return noLinks;
    }

}
