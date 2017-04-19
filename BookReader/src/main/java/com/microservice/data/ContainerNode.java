package com.microservice.data;

import java.util.ArrayList;

import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

import com.microservice.spider.TitleAnalyzer;

public class ContainerNode {

	public ArrayList<CompositeTag> blockNodeList = new ArrayList<CompositeTag>();

	public String content = "";

	public int wordCount = 0;

	public int lwordCount = 0;

	public int width = 0;

	public int border = 0;

	public int align = 0;

	public void addCompositeTagNode(CompositeTag tag) {
		if (tag == null)
			return;

		blockNodeList.add(tag);
		setContainerNodeAttr(tag);
	}

	private void setContainerNodeAttr(CompositeTag tag) {

		try {

			NodeList textNodes = tag.searchFor(TextNode.class, false);

			if (textNodes != null) {
				int size = textNodes.size();

				if (size > 0) {
					StringBuffer bf = new StringBuffer();
					for (int i = 0; i < size; i++) {
						TextNode textNode = (TextNode) textNodes.elementAt(i);
						String text = textNode.getText().trim();

						if (text.length() > 0) {

							bf.append(text);
						}
					}

					String strTmp = filterInvalidHtmlBlank(bf.toString());
					content = content + strTmp;

					wordCount = wordCount
							+ TitleAnalyzer.getStringLength(strTmp);

					bf = null;
				}

			}

			NodeList linkNodes = tag.searchFor(LinkTag.class, false);

			if (linkNodes != null) {
				int size = linkNodes.size();

				for (int i = 0; i < size; i++) {
					String linkText = ((LinkTag) linkNodes.elementAt(i))
							.getLinkText();
					if (linkText != null) {

						linkText = linkText.trim();
						if (linkText.length() > 0) {

							lwordCount = lwordCount
									+ TitleAnalyzer.getStringLength(linkText);

						}
					}
				}

			}

		} catch (Exception e) {

		}

	}

	public static String filterInvalidHtmlBlank(String content) {
		char[] pp = content.toCharArray();
		int len = pp.length;
		char tmp;

		StringBuffer contentBuf = new StringBuffer(len);
		int i = 0;
		while (i < len) {
			tmp = pp[i];

			if (tmp == '&' && (i + 4) < len) {
				if ((pp[i + 1] == 'n' || pp[i + 1] == 'N')
						&& (pp[i + 2] == 'b' || pp[i + 2] == 'B')
						&& (pp[i + 3] == 's' || pp[i + 3] == 'S')
						&& (pp[i + 4] == 'p' || pp[i + 4] == 'P')) {
					if ((i + 5) < len && pp[i + 5] == ';') {
						i = i + 6;
					} else {
						i = i + 5;
					}
				} else {
					contentBuf.append(tmp);
					i++;
				}

			} else if ((tmp == ' ' || tmp == '　') && (i + 1) < len) {
				char p = pp[i + 1];
				while ((p == ' ' || p == '　') && (i + 1) < len) {
					i++;
					if (i + 1 < len)
						p = pp[i + 1];
				}
				contentBuf.append(' ');
				i++;
			} else if ((tmp == '\n' || tmp == '\r') && (i + 1) < len) {
				char p = pp[i + 1];
				while ((p == ' ' || p == '　' || p == '\n' || p == '\r')
						&& (i + 1) < len) {
					i++;
					if (i + 1 < len)
						p = pp[i + 1];
				}
				contentBuf.append('\n');
				i++;

			} else if ((tmp == '|') && (i + 1) < len) {
				char p = pp[i + 1];
				if ((p == ' ' || p == '　' || p == '\n' || p == '\r')
						&& (i + 1) < len) {
					i++;
				}
				i++;
			} else if ((tmp == '-') && (i + 1) < len) {
				char p = pp[i + 1];
				if ((p == ' ' || p == '　' || p == '-') && (i + 1) < len) {
					i++;
				}
				i++;

			} else if ((tmp == '' || tmp == '')) {
				contentBuf.append(' ');
				i++;

			} else if (tmp == '\t') {
				i++;
			} else {
				contentBuf.append(tmp);
				i++;
			}
		}
		return contentBuf.toString();
	}

}