package com.microservice.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Category;

public class KeywordParser {
	private boolean sourceDataIsISO = true;

	private static String strFontHtmlTag = "FONTHTMLTAG";

	private static int MaxSentences = 3;

	private static int MaxDigestLength = 200;

	private static int MaxCompareDigestLength = 300;
	private static int MinDigestLength = 10;

	public String parsedKeyword = null;

	public int andCount = 0;
	public int orCount = 0;

	private String keyword = new String();
	private String errMessage = "0";

	private char[] keywordBytes = null;
	private int keywordByteLength = 0;
	private ArrayList<ArrayList<String>> andList = new ArrayList<ArrayList<String>>();
	private ArrayList<ArrayList<String>> notList = new ArrayList<ArrayList<String>>();

	private final char[] symbols = { '。', '?', '？', '!', '！' };

	Category cat = Category.getInstance("KeywordParse");

	public KeywordParser() {

	}

	public KeywordParser(String keywordInput) {
		this.keyword = keywordInput;
	}

	public void setISOSource(boolean isISOSource) {
		sourceDataIsISO = isISOSource;
	}

	public void setMaxSentences(int maxSentences) {
		MaxSentences = maxSentences;
	}

	public void setMaxDigestLength(int maxDigestLength) {
		MaxDigestLength = maxDigestLength;
	}

	public String getLegalExpr() {
		ArrayList<String> keyAddList = new ArrayList<String>();
		ArrayList<String> keyNotList = new ArrayList<String>();

		Hashtable<String, String> tempTable = new Hashtable<String, String>();

		ArrayList<String> tempArrayList = new ArrayList<String>();
		String keyTemp = new String();
		try {

			keyTemp = changeCharSet(keyword).trim();

			if (errMessage.equals("3")) {
				return errMessage;
			}

			tempTable = getMatchQuotation(keyTemp);
			if (errMessage.equals("2"))
				return errMessage;

			keyTemp = (String) tempTable.get("very_keyparse");
			tempTable.remove("very_keyparse");

			keyTemp = delSeriesChars(keyTemp.trim());
			if (errMessage.equals("3")) {
				return errMessage;
			}

			boolean isInclude = false;
			if (keyTemp.indexOf("+") >= 0)
				isInclude = true;
			if (keyTemp.indexOf("-") >= 0)
				isInclude = true;

			keyAddList = SearchList(keyTemp, '+');

			for (int i = 0; i < keyAddList.size(); i++) {
				keyTemp = (String) keyAddList.get(i);
				if (keyTemp.indexOf("-") >= 0) {
					keyAddList.set(i,
							keyTemp.substring(0, keyTemp.indexOf("-")));
					keyTemp = keyTemp.substring(keyTemp.indexOf("-") + 1,
							keyTemp.length());
					keyNotList.addAll(SearchList(keyTemp, '-'));
				}
			}

			if (keyAddList.size() == 1
					&& (((String) keyAddList.get(0)).trim()).length() == 0) {
				for (int i = 0; i < keyNotList.size(); i++) {
					keyTemp = (String) keyNotList.get(i);
					tempArrayList = SearchList(keyTemp.trim(), ',');
					if (tempArrayList.size() > 1) {
						keyNotList.set(i, (String) tempArrayList.get(0));
						keyAddList.addAll(tempArrayList);
						keyAddList.remove(1);
					}
				}
			} else {

				keyNotList = getMatchBracket(keyNotList, 2);
				if (errMessage.equals("2"))
					return errMessage;
			}

			keyAddList = getMatchBracket(keyAddList, 1);
			if (errMessage.equals("2"))
				return errMessage;

			keyAddList = getMatchBlankForLegal(keyAddList, isInclude, 1);
			keyNotList = getMatchBlankForLegal(keyNotList, isInclude, 2);

			keyAddList = delSingleChar(keyAddList, 1);
			keyNotList = delSingleChar(keyNotList, 2);
			if (keyAddList.size() == 0) {
				errMessage = "1";
				return errMessage;
			}

			keyTemp = new String();

			if (keyAddList.size() > 0)
				keyTemp = keyTemp + concat(keyAddList, '+');

			if (keyNotList.size() > 0)
				keyTemp = keyTemp + "-" + concat(keyNotList, "-");

			String strName = new String();
			String strTemp = new String();
			for (int i = 0; i < tempTable.size(); i++) {
				strName = "very_keyparse" + i;
				strTemp = (String) tempTable.get(strName);
				strTemp = strTemp.trim();
				keyTemp = keyTemp.substring(0, keyTemp.indexOf(strName))
						+ strTemp
						+ keyTemp.substring(
								keyTemp.indexOf(strName) + strName.length(),
								keyTemp.length());
			}
		} catch (Exception e) {
			cat.debug("", e);
		}
		return keyTemp;
	}

	private void parseLegalExpr() {

		if (this.andList.size() > 0)
			return;

		String strGBKKeyword = this.keyword;

		if (sourceDataIsISO)
			strGBKKeyword = StringUtil.iso2gbk(this.keyword);

		this.keywordBytes = strGBKKeyword.toCharArray();
		this.keywordByteLength = keywordBytes.length;

		try {
			int loc = 0;

			loc = jumpSpace(loc);

			if (loc < keywordByteLength && keywordBytes[loc] != '+'
					&& keywordBytes[loc] != '-') {
				int newKeyWordByteLength = keywordByteLength + 1;
				char[] tmpKeyWord = new char[newKeyWordByteLength];
				tmpKeyWord[0] = '+';
				System.arraycopy(keywordBytes, 0, tmpKeyWord, 1,
						keywordByteLength);
				keywordByteLength = newKeyWordByteLength;
				keywordBytes = tmpKeyWord;
			} else if (loc == keywordByteLength) {
				return;
			}

			loc = 0;

			loc = jumpSpace(loc);

			while (loc < keywordByteLength) {
				switch (keywordBytes[loc]) {
				case '+':
					loc++;
					loc = parseKeyword(loc, 1);
					break;
				case '-':
					loc++;
					loc = parseKeyword(loc, 2);
					break;
				default:
					loc++;
				}
			}

			for (int i = 0; i < andList.size(); i++) {
				ArrayList<String> addNode = andList.get(i);

				for (int j = 0; j < addNode.size(); j++) {
					String addKeyword = addNode.get(j);
					for (int m = 0; m < notList.size(); m++) {
						ArrayList<String> notNode = notList.get(m);
						for (int n = 0; n < notNode.size(); n++) {
							String notKeyword = notNode.get(n);
							if (addKeyword.indexOf(notKeyword) != -1) {
								notNode.remove(n);
								n--;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public ArrayList<ArrayList<String>> getANDList() {

		if (this.andList.size() == 0) {
			this.keyword = getLegalExpr();
			parseLegalExpr();
		}

		return this.andList;
	}

	public ArrayList<ArrayList<String>> getNotList() {

		if (this.andList.size() == 0)
			parseLegalExpr();

		return this.notList;
	}

	private int jumpSpace(int beginLoc) {
		int endLoc = beginLoc;

		while (endLoc < keywordByteLength && keywordBytes[endLoc] == ' ') {
			endLoc++;
		}
		return endLoc;
	}

	private int parseKeyword(int beginLoc, int type) {
		int endLoc = beginLoc;
		try {

			endLoc = jumpSpace(endLoc);

			int start = beginLoc;

			ArrayList<ArrayList<String>> tmpList = null;

			if (type == 1) {
				tmpList = andList;
			} else if (type == 2) {
				tmpList = notList;
			}

			switch (keywordBytes[endLoc]) {
			case '\"':
				endLoc++;
				start = endLoc;
				while (endLoc < keywordByteLength
						&& keywordBytes[endLoc] != '\"') {
					endLoc++;
				}

				char[] tmpKeyWord = new char[endLoc - start];
				System.arraycopy(keywordBytes, start, tmpKeyWord, 0, endLoc
						- start);
				ArrayList<String> arr = new ArrayList<String>();
				arr.add(new String(tmpKeyWord));
				tmpList.add(arr);

				while (endLoc < keywordByteLength
						&& keywordBytes[endLoc] != '+'
						&& keywordBytes[endLoc] != '-') {
					endLoc++;
				}
				break;
			case '(':
				endLoc = parseSynKeyword(endLoc, type);

				while (endLoc < keywordByteLength
						&& keywordBytes[endLoc] != '+'
						&& keywordBytes[endLoc] != '-') {
					endLoc++;
				}
				break;
			default:
				start = endLoc;
				while (endLoc < keywordByteLength
						&& keywordBytes[endLoc] != '+'
						&& keywordBytes[endLoc] != '-') {
					endLoc++;
				}

				char[] tmpKeyWord1 = new char[endLoc - start];
				System.arraycopy(keywordBytes, start, tmpKeyWord1, 0, endLoc
						- start);

				ArrayList<String> arr1 = new ArrayList<String>();
				arr1.add(new String(tmpKeyWord1));
				tmpList.add(arr1);
			}
		} catch (Exception e) {
			endLoc = beginLoc;
		}

		return endLoc;
	}

	private int parseSynKeyword(int beginLoc, int type) {
		int endLoc = beginLoc;

		int start = beginLoc;
		ArrayList<ArrayList<String>> tmpList = null;

		if (type == 1) {
			tmpList = andList;
		} else if (type == 2) {
			tmpList = notList;
		}

		ArrayList<String> arr = new ArrayList<String>();
		while (start < keywordByteLength && keywordBytes[endLoc] != ')') {

			endLoc = jumpSpace(endLoc);
			start = endLoc;

			switch (keywordBytes[endLoc]) {
			case ('('):
			case (','):
				endLoc++;

				endLoc = jumpSpace(endLoc);
				start = endLoc;
				if (keywordBytes[endLoc] == '\"') {
					endLoc++;
					start = endLoc;
					while ((endLoc < keywordByteLength)
							&& keywordBytes[endLoc] != '\"') {
						endLoc++;
					}
				} else {
					while ((endLoc < keywordByteLength)
							&& keywordBytes[endLoc] != ')'
							&& keywordBytes[endLoc] != ',') {
						endLoc++;
					}
				}

				char[] tmpKeyWord = new char[endLoc - start];
				System.arraycopy(keywordBytes, start, tmpKeyWord, 0, endLoc
						- start);
				arr.add(new String(tmpKeyWord).trim());
				break;
			default:
				endLoc++;
			}
		}

		tmpList.add(arr);
		if (keywordBytes[endLoc] == ')') {
			endLoc++;
		}
		return endLoc;
	}

	public String getKeywordContent(String strContent) {
		if (strContent == null)
			return "";

		if (this.andList.size() == 0)
			parseLegalExpr();

		ArrayList<ArrayList<String>> keywordANDList = getANDList();

		String content2Mark = strContent;

		ArrayList<String> beFontedKeywords = new ArrayList<String>();
		for (int i = 0; i < keywordANDList.size(); i++) {
			ArrayList<String> keywordBlocks = keywordANDList.get(i);

			for (int k = 0; k < keywordBlocks.size(); k++) {
				String strKey = keywordBlocks.get(k);
				beFontedKeywords.add(strKey);
			}
		}

		beFontedKeywords = getLenRankdedList(beFontedKeywords);

		for (int fk = 0; fk < beFontedKeywords.size(); fk++) {
			String strKey = beFontedKeywords.get(fk);

			if (isEnglishWord(strKey))
				content2Mark = content2Mark.replaceAll("(?i)" + strKey,
						strFontHtmlTag + strKey + "</font>");
			else
				content2Mark = content2Mark.replaceAll(strKey, strFontHtmlTag
						+ strKey + "</font>");
		}

		content2Mark = content2Mark.replaceAll(strFontHtmlTag,
				"<font color=#FF8359>");

		if (sourceDataIsISO)
			content2Mark = StringUtil.gbk2iso(content2Mark);

		return content2Mark;
	}

	public String getKeywordDigest(String strContent) {
		String strDigest = "";

		if (strContent == null)
			return "";

		if (this.andList.size() == 0)
			parseLegalExpr();

		ArrayList<ArrayList<String>> keywordList = getANDList();

		strDigest = getDigest(strContent, keywordList);

		if (strDigest.length() == 0) {
			int contentLen = strContent.length();

			if (contentLen <= MaxDigestLength) {
				strDigest = strContent;
			} else {
				strDigest = strContent.substring(0, MaxDigestLength);
				strDigest = removeInvalidStartChars(strDigest);
				strDigest = getSentenceEnd(strDigest);
			}
		}

		strDigest = getCleanText(strDigest);

		strDigest = strDigest
				.replaceAll(strFontHtmlTag, "<font color=#FF8359>");
		if (sourceDataIsISO)
			strDigest = StringUtil.gbk2iso(strDigest);

		return (strDigest.length() >= MinDigestLength) ? strDigest : "";

	}

	private String getDigest(String strContent,
			ArrayList<ArrayList<String>> KeywordANDList) {
		int tPos = 0;
		int fromIndex = 0;
		int tBegin = 0, tEnd = 0;

		int MaxForeChars = 50;
		int MaxBackChars = 50;

		int iDigestNum = 0;

		String keyword = "";
		String strDigest = "";

		String arrayKeyword[] = new String[MaxSentences];
		Integer arrayDigestIndex[] = new Integer[MaxSentences];

		String strContent4Comapre = strContent.toLowerCase();

		int iANDCounter = 0;
		for (int i = 0; i < KeywordANDList.size(); i++) {

			if (iDigestNum >= MaxSentences)
				break;

			int digestCount = 0;
			ArrayList<String> keywordBlocks = KeywordANDList.get(i);

			int MaxSentences4eachKeyword = 2;

			if (KeywordANDList.size() == 1 && keywordBlocks.size() == 1) {

				MaxSentences4eachKeyword = 3;
			} else if (KeywordANDList.size() == 2 && keywordBlocks.size() == 1) {

				MaxSentences4eachKeyword = 2;
			} else if (KeywordANDList.size() == 3 && keywordBlocks.size() == 1) {

				MaxSentences4eachKeyword = 1;
			} else if (keywordBlocks.size() > 1 && KeywordANDList.size() > 1)
				MaxSentences4eachKeyword = 1;

			keywordBlocks = getAllSameEnglishWord(keywordBlocks);

			for (int k = 0; k < keywordBlocks.size(); k++) {
				keyword = keywordBlocks.get(k);

				fromIndex = 0;

				while (true) {

					if (iDigestNum >= MaxSentences)
						break;

					String keyword4Comapre = keyword.toLowerCase();
					tPos = strContent4Comapre.indexOf(keyword4Comapre,
							fromIndex);
					if (tPos == -1)
						break;

					fromIndex = tPos + keyword.length() + MaxBackChars;

					arrayKeyword[iDigestNum] = keyword;
					arrayDigestIndex[iDigestNum] = new Integer(tPos);

					iDigestNum++;

					digestCount++;
					if (digestCount > MaxSentences4eachKeyword - 1)
						break;
				}
			}

			if (digestCount > 0)
				iANDCounter++;
		}

		if (iANDCounter < KeywordANDList.size() / 2) {
			return strDigest;
		}

		for (int i = 0; i < MaxSentences; i++) {
			for (int j = 0; j < MaxSentences - 1 - i; j++) {
				if (arrayDigestIndex[j] == null)
					break;
				if (arrayDigestIndex[j + 1] == null)
					break;

				if (arrayDigestIndex[j].intValue() > arrayDigestIndex[j + 1]
						.intValue()) {
					Integer k = arrayDigestIndex[j];
					arrayDigestIndex[j] = arrayDigestIndex[j + 1];
					arrayDigestIndex[j + 1] = k;

					String s = arrayKeyword[j];
					arrayKeyword[j] = arrayKeyword[j + 1];
					arrayKeyword[j + 1] = s;
				}
			}
		}

		int countDigest = 0;
		while (countDigest < MaxSentences) {
			if (arrayDigestIndex[countDigest] == null)
				break;

			tPos = arrayDigestIndex[countDigest].intValue();
			if (tPos < 0)
				break;

			countDigest++;
		}

		int tmpDigestLength = 0;

		if (countDigest > 0)
			tmpDigestLength = MaxDigestLength / (countDigest * 2);

		if (tmpDigestLength > MaxForeChars) {
			MaxForeChars = tmpDigestLength;
			MaxBackChars = tmpDigestLength;
		}

		int tContentLen = strContent.length();
		int i = 0;
		while (i < MaxSentences) {
			int j = i;
			if (arrayDigestIndex[i] == null)
				break;

			tPos = arrayDigestIndex[i].intValue();
			if (tPos < 0)
				break;

			tBegin = tPos - MaxForeChars;
			if (tBegin < 0)
				tBegin = 0;

			tEnd = tPos + arrayKeyword[i].length() + MaxBackChars;
			if (tEnd > tContentLen)
				tEnd = tContentLen;

			i++;

			while (i < MaxSentences) {

				if (arrayDigestIndex[i] == null)
					break;

				if (tEnd < arrayDigestIndex[i].intValue() - MaxForeChars)
					break;

				tEnd = arrayDigestIndex[i].intValue()
						+ arrayKeyword[i].length() + MaxBackChars;
				if (tEnd > tContentLen)
					tEnd = tContentLen;
				i++;
			}

			String tDigest = strContent.substring(tBegin, tPos);
			tDigest = getSentenceStart(tDigest);
			tDigest = tDigest + strContent.substring(tPos, tEnd);
			tDigest = getCleanText(tDigest);

			ArrayList<String> beFontedKeywords = new ArrayList<String>();
			for (int k = j; k < i; k++) {
				if (arrayDigestIndex[k] == null)
					break;

				beFontedKeywords.add(arrayKeyword[k]);
			}

			beFontedKeywords = getLenRankdedList(beFontedKeywords);
			String strDigest4Comapre = tDigest.toLowerCase();
			for (int fk = 0; fk < beFontedKeywords.size(); fk++) {
				String strKey = beFontedKeywords.get(fk);

				int indexKey = strDigest4Comapre.indexOf(strKey.toLowerCase());
				if (indexKey != -1) {
					tDigest = tDigest.substring(0, indexKey)
							+ strFontHtmlTag
							+ tDigest.substring(indexKey,
									indexKey + strKey.length()) + "</font>"
							+ tDigest.substring(indexKey + strKey.length());

				}

				strDigest4Comapre = tDigest;
			}

			tDigest = getSentenceEnd(tDigest);
			strDigest = strDigest + tDigest + "...";
		}

		if (strDigest != null && !strDigest.equals(""))
			strDigest = "..." + strDigest.trim();
		else
			strDigest = "";

		return strDigest;
	}

	public ArrayList<String> getLenRankdedList(ArrayList<String> keywordList) {
		ArrayList<String> rankedList = new ArrayList<String>();

		if (keywordList == null || keywordList.size() == 0)
			return rankedList;

		ArrayList<Integer> lenList = new ArrayList<Integer>();
		for (int i = 0; i < keywordList.size(); i++) {
			String strTemp = keywordList.get(i);
			if (strTemp == null)
				lenList.add(0);
			else
				lenList.add(strTemp.length());
		}

		Collections.sort(lenList);

		for (int i = lenList.size() - 1; i >= 0; i--) {
			int kl = lenList.get(i);

			for (int k = 0; k < keywordList.size(); k++) {
				if (keywordList.get(k) == null)
					continue;

				if (keywordList.get(k).length() == kl) {
					rankedList.add(keywordList.get(k));
					keywordList.set(k, null);

					break;
				}
			}
		}

		return rankedList;
	}

	public static String getKeywordWithoutNOT(String keyword) {
		StringBuffer tmpBuffer = new StringBuffer();

		for (int i = 0; i < keyword.length(); i++) {
			char tc = keyword.charAt(i);

			if (tc != '-') {
				tmpBuffer.append(tc);
			} else {
				if (i > keyword.length() - 1)
					break;

				char nextChar = keyword.charAt(i + 1);

				if (nextChar == '(') {
					while (i < keyword.length() - 1) {
						i++;

						char jumpChar = keyword.charAt(i);
						if (jumpChar == ')')
							break;
					}
				} else {

					while (i < keyword.length() - 1) {
						char jumpChar = keyword.charAt(i + 1);
						if (jumpChar == '+' || jumpChar == '-'
								|| jumpChar == ',' || jumpChar == ')')
							break;

						i++;
					}
				}
			}
		}

		return tmpBuffer.toString();
	}

	public String getCleanText(String strContent) {

		String invalidChars = "｜|▲|★|■|◆|☆|　|_|【|】|：|┆|·|\\+|#|:|\\-|\\[|\\]|\\?{2,}|？{2,}";

		int MinLengthParagraph = 5;

		String regChars = invalidChars;
		if (sourceDataIsISO)
			regChars = StringUtil.iso2gbk(invalidChars);

		String digest = strContent.replaceAll(regChars, "");

		String[] splits = digest.split(" ");
		String strTemp = "";
		for (int i = 0; i < splits.length; i++) {
			String s = splits[i];
			if (s != null)
				s = s.trim();

			if (s.length() > MinLengthParagraph)
				strTemp += " " + s;
		}

		return strTemp;
	}

	private String getSentenceStart(String strSentence) {
		String EndFlag[] = new String[8];
		String tmpStr = "";

		EndFlag[0] = "。";
		EndFlag[1] = "？";
		EndFlag[2] = "！";
		EndFlag[3] = "，";
		EndFlag[4] = "?";
		EndFlag[5] = "!";
		EndFlag[6] = ".";
		EndFlag[7] = ",";

		if (strSentence == null)
			return "";

		for (int i = 0; i < 8; i++) {
			try {
				EndFlag[i] = new String(EndFlag[i].getBytes(), "GBK");
			} catch (Exception e) {
				;
			}
		}

		tmpStr = strSentence;

		int tPos = -1;
		int tFlagLen = 0;

		for (int i = 0; i < 8; i++) {
			tPos = strSentence.indexOf(EndFlag[i], 0);
			if (tPos != -1) {
				tFlagLen = EndFlag[i].length();
				tmpStr = strSentence.substring(tPos + tFlagLen);
				if (tmpStr.length() > 0) {
					break;
				}
			}
		}

		if (tPos == -1) {
			tmpStr = strSentence;
		}

		tmpStr = removeInvalidStartChars(tmpStr);

		return tmpStr;
	}

	public static boolean isRightChineseHalf(byte[] byteArray, int begin,
			int end) {

		if (end <= begin) {
			return false;
		}

		int last = end - 1;
		int pos = last;

		while (pos >= begin && byteArray[pos] < 0)
			pos--;

		if ((last - pos) % 2 == 1) {
			return true;
		}

		return false;

	}

	private ArrayList<String> SearchList(String keyTemp, char word)
			throws Exception {
		ArrayList<String> tempArrayList = new ArrayList<String>();

		int keyLength = 0;
		int keyNodeLength = 0;
		boolean isInclude = false;

		while (!isInclude) {

			if (keyTemp.indexOf(word) < 0) {
				keyTemp = keyTemp.trim();
				tempArrayList.add(tempArrayList.size(), keyTemp.trim());
				break;
			}

			keyLength = keyTemp.length();
			keyNodeLength = keyTemp.indexOf(word);
			if (keyNodeLength != 0) {
				tempArrayList.add(tempArrayList.size(),
						(keyTemp.substring(0, keyNodeLength)).trim());
				keyTemp = (keyTemp.substring(keyNodeLength + 1, keyLength))
						.trim();
			} else {
				keyTemp = (keyTemp.substring(0, keyNodeLength)).trim()
						+ (keyTemp.substring(keyNodeLength + 1, keyLength))
								.trim();
			}
		}

		return tempArrayList;
	}

	private String changeCharSet(String keyTemp) throws Exception {

		byte[] arrayTemp = keyTemp.getBytes();
		byte[] targetTemp = new byte[arrayTemp.length];
		int j = 0;
		for (int i = 0; i < arrayTemp.length; i++, j++) {
			if (arrayTemp[i] <= 0) {
				if (arrayTemp.length == i + 1)
					break;

				if (arrayTemp[i] == -95 && arrayTemp[i + 1] == -80) {
					arrayTemp[i] = 34;
					arrayTemp[i + 1] = 32;

				}

				if (arrayTemp[i] == -95 && arrayTemp[i + 1] == -79) {
					arrayTemp[i] = 34;
					arrayTemp[i + 1] = 32;

				}

				if (arrayTemp[i] == -93 && arrayTemp[i + 1] == -94) {
					arrayTemp[i] = 34;
					arrayTemp[i + 1] = 32;

				}

				if (arrayTemp[i] == -95 && arrayTemp[i + 1] == -82) {
					arrayTemp[i] = 39;
					arrayTemp[i + 1] = 32;

				}

				if (arrayTemp[i] == -93 && arrayTemp[i + 1] == -89) {
					arrayTemp[i] = 39;
					arrayTemp[i + 1] = 32;

				}

				if (arrayTemp[i] == -93 && arrayTemp[i + 1] == -85) {
					arrayTemp[i] = 43;
					arrayTemp[i + 1] = 32;

				}

				if (arrayTemp[i] == -93 && arrayTemp[i + 1] == -83) {
					arrayTemp[i] = 45;
					arrayTemp[i + 1] = 32;

				}

				if (arrayTemp[i] == -93 && arrayTemp[i + 1] == -88) {
					arrayTemp[i] = 40;
					arrayTemp[i + 1] = 32;

				}

				if (arrayTemp[i] == -93 && arrayTemp[i + 1] == -87) {
					arrayTemp[i] = 41;
					arrayTemp[i + 1] = 32;

				}

				if (arrayTemp[i] == -93 && arrayTemp[i + 1] == -37) {
					arrayTemp[i] = 40;
					arrayTemp[i + 1] = 32;

				}

				if (arrayTemp[i] == -93 && arrayTemp[i + 1] == -35) {
					arrayTemp[i] = 41;
					arrayTemp[i + 1] = 32;

				}

				if (arrayTemp[i] == -93 && arrayTemp[i + 1] == -5) {
					arrayTemp[i] = 40;
					arrayTemp[i + 1] = 32;

				}

				if (arrayTemp[i] == -93 && arrayTemp[i + 1] == -3) {
					arrayTemp[i] = 41;
					arrayTemp[i + 1] = 32;

				}

				if (arrayTemp[i] == -93 && arrayTemp[i + 1] == -84) {
					arrayTemp[i] = 44;
					arrayTemp[i + 1] = 32;

				}

				if (arrayTemp[i] == -95 && arrayTemp[i + 1] == -93) {
					arrayTemp[i] = 32;
					arrayTemp[i + 1] = 32;

				}

				if (arrayTemp[i] == -95 && arrayTemp[i + 1] == -94) {
					arrayTemp[i] = 32;
					arrayTemp[i + 1] = 32;

				}

				if (arrayTemp[i] == -93 && arrayTemp[i + 1] == -81) {
					arrayTemp[i] = 32;
					arrayTemp[i + 1] = 32;

				}

				if (arrayTemp[i] == -93 && arrayTemp[i + 1] == -69) {
					arrayTemp[i] = 32;
					arrayTemp[i + 1] = 32;

				}

				if ((arrayTemp[i] == -93 && arrayTemp[i + 1] == -70)
						|| (arrayTemp[i] == -93 && arrayTemp[i + 1] == -65)
						|| (arrayTemp[i] == -93 && arrayTemp[i + 1] == -95)) {

				}

				if (arrayTemp[i] == -95 && arrayTemp[i + 1] == -95) {
					arrayTemp[i] = 32;
					arrayTemp[i + 1] = 32;

				}
				targetTemp[j] = arrayTemp[i];
				i++;

				if (targetTemp[j] <= 0) {
					j++;
					targetTemp[j] = arrayTemp[i];
				}

			} else {

				if (arrayTemp[i] == 46) {
					arrayTemp[i] = 32;
				}

				if (arrayTemp[i] == 91) {
					arrayTemp[i] = 40;
				}

				if (arrayTemp[i] == 93) {
					arrayTemp[i] = 41;
				}

				if (arrayTemp[i] == 123) {
					arrayTemp[i] = 40;
				}

				if (arrayTemp[i] == 125) {
					arrayTemp[i] = 41;
				}

				if (arrayTemp[i] == 47) {
					arrayTemp[i] = 32;
				}

				if (arrayTemp[i] == 92) {
					arrayTemp[i] = 32;
				}

				if (arrayTemp[i] == 59) {
					arrayTemp[i] = 32;
				}

				if (arrayTemp[i] == 58 || arrayTemp[i] == 63
						|| arrayTemp[i] == 33) {

				}
				targetTemp[j] = arrayTemp[i];
			}
		}

		keyTemp = new String(targetTemp);
		return keyTemp;
	}

	private String delSeriesChars(String keyTemp) throws Exception {
		byte[] arrayTemp = keyTemp.getBytes();
		boolean flag = false;
		for (int i = 0; i < arrayTemp.length; i++) {

			if (arrayTemp[i] == 40) {
				if (arrayTemp.length == i + 1)
					break;

				while (arrayTemp[i + 1] == 32)
					i++;

				if (arrayTemp[i + 1] == 40 || arrayTemp[i + 1] == 41) {
					errMessage = "3";
					return null;
				}
			}

			if (arrayTemp[i] == 41) {
				if (arrayTemp.length == i + 1)
					break;

				if (arrayTemp[i + 1] != 43 && arrayTemp[i + 1] != 45
						&& arrayTemp[i + 1] != 32) {
					errMessage = "3";
					return null;
				}
			}

		}

		for (int i = 0; i < arrayTemp.length; i++) {
			if (i != 0)
				flag = true;

			if (arrayTemp[i] == 40 && flag) {
				if (arrayTemp.length == i + 1)
					break;
				if (arrayTemp[i - 1] != 45 && arrayTemp[i - 1] != 43
						&& arrayTemp[i - 1] != 32) {
					errMessage = "3";
					return null;
				}
			}

			if (arrayTemp[i] == 41) {
				if (arrayTemp.length == i + 1)
					break;
				int node = i + 1;
				while (arrayTemp[i + 1] == 32)
					i++;
				if (arrayTemp[i + 1] != 45 && arrayTemp[i + 1] != 43) {
					if (arrayTemp[i + 1] == 44) {
						errMessage = "3";
						return null;
					} else {
						arrayTemp[node] = 43;
					}
				}
			}
		}

		if (keyTemp.indexOf("(") >= 0) {
			int i = 0;
			boolean isChar = false;
			while (arrayTemp[i] != 40)
				i++;

			while (arrayTemp[i] != 41) {
				if (arrayTemp[i] == 44) {
					isChar = true;
					while (arrayTemp[i + 1] == 32)
						i++;
				}
				if (isChar) {
					if (arrayTemp[i] == 43)
						arrayTemp[i] = 32;
					if (arrayTemp[i] == 45) {
						errMessage = "3";
						return null;
					}
				} else {
					if (arrayTemp[i] == 43)
						arrayTemp[i] = 44;
					if (arrayTemp[i] == 45) {
						errMessage = "3";
						return null;
					}
				}
				i++;
				if (i == arrayTemp.length)
					break;
				if (arrayTemp[i] == 41)
					isChar = false;
			}
		}

		for (int i = 0; i < arrayTemp.length; i++) {
			if (arrayTemp[i] == 43 || arrayTemp[i] == 44 || arrayTemp[i] == 45) {
				int node = i;
				while (arrayTemp[i + 1] == 32)
					i++;
				if (arrayTemp[i + 1] == 43 || arrayTemp[i + 1] == 44
						|| arrayTemp[i + 1] == 45)
					arrayTemp[node] = 32;
				else
					break;

			}
		}

		return new String(arrayTemp);
	}

	private ArrayList<String> getMatchBracket(ArrayList<String> tempArrayList,
			int type) throws Exception {

		for (int i = 0; i < tempArrayList.size(); i++) {
			String keyTemp = tempArrayList.get(i);

			keyTemp = keyTemp.trim();

			if (keyTemp.indexOf("(") == 0 && keyTemp.indexOf(")") < 0)
				keyTemp = keyTemp + ")";

			if (keyTemp.indexOf("(") < 0 && keyTemp.indexOf(")") > 0
					&& keyTemp.indexOf(")") == keyTemp.length() - 1)
				keyTemp = "(" + keyTemp;

			if (keyTemp.indexOf(",") >= 0 && keyTemp.indexOf(")") < 0
					&& keyTemp.indexOf("(") < 0)
				keyTemp = "(" + keyTemp + ")";

			if (keyTemp.indexOf("(") > 0
					|| (keyTemp.indexOf(")") > 0 && keyTemp.indexOf(")") < keyTemp
							.length() - 1)) {
				errMessage = "2";
				return tempArrayList;
			}

			if (tempArrayList.size() == 0) {
				tempArrayList.remove(i);
				i--;
			} else {
				tempArrayList.set(i, keyTemp);
			}
		}

		return tempArrayList;
	}

	private Hashtable<String, String> getMatchQuotation(String keyTemp)
			throws Exception {
		Hashtable<String, String> tempTable = new Hashtable<String, String>();

		int len1 = 0;
		int len2 = 0;
		int i = 0;
		String strTemp = new String();
		do {
			len1 = keyTemp.indexOf("\"");
			if (len1 > -1) {
				strTemp = keyTemp.substring(keyTemp.indexOf("\"") + 1,
						keyTemp.length());
				len2 = strTemp.indexOf("\"");
				if (len2 > -1) {
					strTemp = "very_keyparse" + i;
					i++;
					tempTable.put(strTemp,
							keyTemp.substring(len1, len2 + len1 + 2));
					keyTemp = keyTemp.substring(0, len1)
							+ strTemp
							+ keyTemp.substring(len2 + len1 + 2,
									keyTemp.length());
				} else {
					errMessage = "2";
					break;
				}
			} else {
				tempTable.put("very_keyparse", keyTemp);
				break;
			}
		} while (len1 > -1);
		return tempTable;

	}

	private ArrayList<String> getMatchBlankForLegal(
			ArrayList<String> tempArrayList, boolean isInclude, int type)
			throws Exception {

		for (int i = 0; i < tempArrayList.size(); i++) {
			String keyTemp = (String) tempArrayList.get(i);
			boolean isChinese = false;
			keyTemp = keyTemp.trim();

			ArrayList<String> arrayList = new ArrayList<String>();

			if (keyTemp.indexOf("(") == 0) {
				keyTemp = keyTemp.substring(keyTemp.indexOf("(") + 1,
						keyTemp.length() - 1);

				if (keyTemp.indexOf(",") >= 0) {
					arrayList = SearchList(keyTemp.trim(), ',');
					for (int j = 0; j < arrayList.size(); j++) {
						String tempString = (String) arrayList.get(j);
						isChinese = getCharSet(tempString);
						if ((tempString.trim()).length() == 0) {
							arrayList.remove(j);
							j--;
						}
						if (isChinese)
							arrayList.set(j,
									getBlankString((String) arrayList.get(j)));
					}
				} else {

					if (isChinese)
						arrayList = SearchList(keyTemp.trim(), ' ');
					else
						arrayList = SearchList(keyTemp.trim(), ' ');
				}

				if (arrayList.size() > 1) {

					if (type == 1) {
						orCount += arrayList.size();
						keyTemp = "(" + concat(arrayList, ',') + ")";
					} else
						keyTemp = concat(arrayList, '-');
				}
				if (arrayList.size() == 1)
					keyTemp = (String) arrayList.get(0);
				if (arrayList.size() == 0)
					keyTemp = "";

			} else {

				isChinese = getCharSet(keyTemp);
				if (isInclude) {
					if (isChinese)
						arrayList
								.add(arrayList.size(), getBlankString(keyTemp));
					else
						arrayList.add(arrayList.size(), keyTemp);
				} else {
					if (isChinese)
						arrayList = SearchList(keyTemp.trim(), ' ');
					else
						arrayList = SearchList(keyTemp.trim(), ' ');
				}

				if (arrayList.size() > 1)
					keyTemp = concat(arrayList, '+');
				if (arrayList.size() == 1)
					keyTemp = (String) arrayList.get(0);
				if (arrayList.size() == 0)
					keyTemp = "";
			}

			if ((keyTemp.trim()).length() > 0) {
				tempArrayList.set(i, keyTemp);
			} else {
				tempArrayList.remove(i);
				i--;
			}

		}
		return tempArrayList;
	}

	public ArrayList<String> getMatchBlankForSql(String keyTemp)
			throws Exception {

		keyTemp = keyTemp.trim();
		ArrayList<String> arrayList = new ArrayList<String>();

		if (keyTemp.indexOf("(") == 0) {
			keyTemp = keyTemp.substring(keyTemp.indexOf("(") + 1,
					keyTemp.length() - 1);
			arrayList = SearchList(keyTemp.trim(), ',');
		} else {
			arrayList.add(arrayList.size(), keyTemp);
		}

		return arrayList;
	}

	private boolean getCharSet(String keyTemp) throws Exception {

		byte[] arrayTemp = keyTemp.getBytes();
		boolean isChinese = false;
		for (int i = 0; i < arrayTemp.length; i++) {
			if (arrayTemp[i] < 0)
				isChinese = true;
		}
		return isChinese;
	}

	private ArrayList<String> delSingleChar(ArrayList<String> tempArrayList,
			int type) throws Exception {
		ArrayList<String> arrayList = new ArrayList<String>();

		boolean singleFlag = false;
		String keyTemp = "";
		boolean isChinese = false;
		int j = 0;

		if (type == 1) {
			for (j = 0; j < tempArrayList.size(); j++) {
				keyTemp = (String) tempArrayList.get(j);
				isChinese = false;
				isChinese = getCharSet(keyTemp);

				byte[] tmpB = keyTemp.getBytes();

				if (isChinese && tmpB.length > 2) {
					singleFlag = true;
					break;
				}
			}
		}

		for (j = 0; j < tempArrayList.size(); j++) {
			keyTemp = (String) tempArrayList.get(j);
			isChinese = false;
			isChinese = getCharSet(keyTemp);
			if (isChinese) {
				if (type == 1) {
					if (singleFlag) {
						arrayList.add(arrayList.size(), keyTemp.trim());
					} else {
						byte[] tmpB = keyTemp.getBytes();
						if (tmpB.length > 2)

							arrayList.add(arrayList.size(), keyTemp.trim());
					}
				} else {
					byte[] tmpB = keyTemp.getBytes();
					if (tmpB.length > 2)

						arrayList.add(arrayList.size(), keyTemp.trim());
				}
			}

			if (!isChinese && keyTemp.length() > 1)
				arrayList.add(arrayList.size(), keyTemp.trim());
		}

		return arrayList;
	}

	private String getBlankString(String keyTemp) throws Exception {

		String key = keyTemp.trim();
		for (int i = 0; i < keyTemp.length(); i++) {
			if (key.indexOf(" ") >= 0)
				key = key.substring(0, key.indexOf(" "))
						+ key.substring(key.indexOf(" ") + 1, key.length());
		}
		return key;
	}

	public String stringReplace(String sourceString, String toReplaceString,
			String replaceString) {
		String returnString = sourceString;
		int stringLength = 0;
		if (toReplaceString != null)
			stringLength = toReplaceString.length();
		if (returnString != null && returnString.length() > stringLength) {
			int max = 0;
			String S4 = "";
			for (int i = 0; i < sourceString.length(); i++) {
				max = i + toReplaceString.length() > sourceString.length() ? sourceString
						.length() : i + stringLength;
				String S3 = sourceString.substring(i, max);
				if (!S3.equals(toReplaceString)) {
					S4 += S3.substring(0, 1);
				} else {
					S4 += replaceString;
					i += stringLength - 1;
				}
			}
			returnString = S4;
		}
		return returnString;
	}

	private String concat(ArrayList<String> tempList, char word)
			throws Exception {

		String key = "";
		for (int i = 0; i < tempList.size(); i++) {
			if (key.length() > 0)
				key = key + word + (String) tempList.get(i);
			else
				key = key + (String) tempList.get(i);
		}
		return key;
	}

	private String concat(ArrayList<String> tempList, String word)
			throws Exception {

		String key = "";
		for (int i = 0; i < tempList.size(); i++) {
			if (key.length() > 0)
				key = key + word + (String) tempList.get(i);
			else
				key = key + (String) tempList.get(i);
		}
		return key;
	}

	public String convert2Lucene(String veryEQueryExpress) {
		String LucuneQueryExpress = "+";
		char tmpCh;
		boolean inSemicolon = false;

		for (int i = 0; i < veryEQueryExpress.length(); i++) {
			tmpCh = veryEQueryExpress.charAt(i);

			if (tmpCh == '"') {
				inSemicolon = !inSemicolon;
				LucuneQueryExpress += tmpCh;
				continue;
			}

			if (inSemicolon) {
				LucuneQueryExpress += tmpCh;
				continue;
			}

			if (tmpCh == '+') {

				if ((LucuneQueryExpress.charAt(LucuneQueryExpress.length() - 1) == '+')
						|| (LucuneQueryExpress.charAt(LucuneQueryExpress
								.length() - 1) == '-')) {
					continue;
				}
			}

			if (tmpCh == '-') {

				if (LucuneQueryExpress.charAt(LucuneQueryExpress.length() - 1) == '-') {
					continue;
				} else {

					if (LucuneQueryExpress
							.charAt(LucuneQueryExpress.length() - 1) == '+') {
						LucuneQueryExpress = LucuneQueryExpress.substring(0,
								LucuneQueryExpress.length() - 1) + "-";
						continue;
					}
				}
			}

			if (tmpCh == ',') {
				if (LucuneQueryExpress.charAt(LucuneQueryExpress.length() - 1) != ' ') {
					LucuneQueryExpress += ' ';
				}

				LucuneQueryExpress += "OR ";

				continue;
			}

			LucuneQueryExpress += tmpCh;
		}
		return LucuneQueryExpress;
	}

	public String getLegalLuceneExpr() {
		String parsedKeyword = getLegalExpr();

		String luceneQueryExpress = null;

		if (parsedKeyword != null) {

			if (parsedKeyword.equals("1") || parsedKeyword.equals("2")
					|| parsedKeyword.equals("3")) {
				return (parsedKeyword);
			} else {
				luceneQueryExpress = convert2Lucene(parsedKeyword);
			}
		}

		return luceneQueryExpress;
	}

	public static boolean isEnglishWord(String str) {

		String englishLetters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (englishLetters.indexOf(ch) == -1) {
				return false;
			}
		}

		return true;
	}

	public static boolean isGBKEncodedEnglishWord(String str) {

		boolean flag = false;

		if (str == null) {
			return false;
		}
		if (str.length() == 0) {
			return false;
		}

		String GBKEnglishLetters = "ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ";
		try {
			GBKEnglishLetters = new String(GBKEnglishLetters.getBytes(), "GBK");
		} catch (Exception e) {
			;
		}

		flag = true;
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);

			if (GBKEnglishLetters.indexOf(ch) != -1) {
				flag = false;
				break;
			}
		}
		return flag;
	}

	public ArrayList<String> getAllSameEnglishWord(ArrayList<String> keywordList) {
		ArrayList<String> allSameEnglishWords = new ArrayList<String>();

		for (int i = 0; i < keywordList.size(); i++) {
			String strKeyword = keywordList.get(i);

			if (strKeyword.trim().length() < 1)
				continue;

			allSameEnglishWords.add(strKeyword);
			if (!isEnglishWord(strKeyword))
				continue;

			String wordLowerCase = strKeyword.toLowerCase();
			if (!strKeyword.equals(wordLowerCase)) {
				if (!allSameEnglishWords.contains(wordLowerCase)) {
					allSameEnglishWords.add(wordLowerCase);
				}
			}

			String wordUpperCase = strKeyword.toUpperCase();
			if (!strKeyword.equals(wordUpperCase)) {
				if (!allSameEnglishWords.contains(wordUpperCase)) {
					allSameEnglishWords.add(wordUpperCase);
				}
			}

			char UpperFirstChar = Character.toUpperCase(strKeyword.charAt(0));
			String wordUpperFirstChar = UpperFirstChar
					+ strKeyword.substring(1).toLowerCase();
			if (!strKeyword.equals(wordUpperFirstChar)) {
				if (!allSameEnglishWords.contains(wordUpperFirstChar)) {
					allSameEnglishWords.add(wordUpperFirstChar);
				}
			}

			String newEnglishKey = covertQuanjiaoEngishWord(strKeyword);
			if (!allSameEnglishWords.contains(newEnglishKey)) {
				allSameEnglishWords.add(newEnglishKey);
			}
		}

		return allSameEnglishWords;
	}

	private static String getSentenceEnd(String strSentence) {

		String tmpStr = "";

		String EndFlag[] = { "。", "？", "！", "，", ".", "?", "!", "," };

		if (strSentence == null) {
			return tmpStr;
		}
		if (strSentence.length() == 0) {
			return tmpStr;
		}

		for (int i = 0; i < EndFlag.length; i++) {
			try {
				EndFlag[i] = new String(EndFlag[i].getBytes(), "GBK");
			} catch (Exception e) {
				;
			}
		}

		tmpStr = strSentence;
		int tPos = -1;
		for (int i = 0; i < EndFlag.length; i++) {
			tPos = strSentence.lastIndexOf(EndFlag[i]);

			if ((tPos != -1) && (tPos != 0)) {
				tmpStr = strSentence.substring(0, tPos);
				break;
			}
		}
		return tmpStr;
	}

	public static String covertQuanjiaoEngishWord(String englishWord) {

		String newEnglishWord = new String();

		if (englishWord == null) {
			return "";
		}
		if (englishWord.length() == 0) {
			return "";
		}

		HashMap<String, String> convertMap = new HashMap<String, String>();
		convertMap.put("a", "ａ");
		convertMap.put("b", "ｂ");
		convertMap.put("c", "ｃ");
		convertMap.put("d", "ｄ");
		convertMap.put("e", "ｅ");
		convertMap.put("f", "ｆ");
		convertMap.put("g", "ｇ");
		convertMap.put("h", "ｈ");
		convertMap.put("i", "ｉ");
		convertMap.put("j", "ｊ");
		convertMap.put("k", "ｋ");
		convertMap.put("l", "ｌ");
		convertMap.put("m", "ｍ");
		convertMap.put("n", "ｎ");
		convertMap.put("o", "ｏ");
		convertMap.put("p", "ｐ");
		convertMap.put("q", "ｑ");
		convertMap.put("r", "ｒ");
		convertMap.put("s", "ｓ");
		convertMap.put("t", "ｔ");
		convertMap.put("u", "ｕ");
		convertMap.put("v", "ｖ");
		convertMap.put("w", "ｗ");
		convertMap.put("x", "ｘ");
		convertMap.put("y", "ｙ");
		convertMap.put("z", "ｚ");
		convertMap.put("A", "Ａ");
		convertMap.put("B", "Ｂ");
		convertMap.put("C", "Ｃ");
		convertMap.put("D", "Ｄ");
		convertMap.put("E", "Ｅ");
		convertMap.put("F", "Ｆ");
		convertMap.put("G", "Ｇ");
		convertMap.put("H", "Ｈ");
		convertMap.put("I", "Ｉ");
		convertMap.put("J", "Ｊ");
		convertMap.put("K", "Ｋ");
		convertMap.put("L", "Ｌ");
		convertMap.put("M", "Ｍ");
		convertMap.put("N", "Ｎ");
		convertMap.put("O", "Ｏ");
		convertMap.put("P", "Ｐ");
		convertMap.put("Q", "Ｑ");
		convertMap.put("R", "Ｒ");
		convertMap.put("S", "Ｓ");
		convertMap.put("T", "Ｔ");
		convertMap.put("U", "Ｕ");
		convertMap.put("V", "Ｖ");
		convertMap.put("W", "Ｗ");
		convertMap.put("X", "Ｘ");
		convertMap.put("Y", "Ｙ");
		convertMap.put("Z", "Ｚ");

		for (int i = 0; i < englishWord.length(); i++) {
			char ch = englishWord.charAt(i);
			String tmpStr = (String) convertMap.get("" + ch);
			if (tmpStr != null) {
				newEnglishWord += tmpStr;
			} else {
				newEnglishWord += ch;
			}
		}

		try {
			newEnglishWord = new String(newEnglishWord.getBytes(), "GBK");
		} catch (Exception e) {
			;
		}

		return newEnglishWord;
	}

	public String removeInvalidStartChars(String inputStr) {

		String inValideBeginChars = ")）}]- ”";

		String gbkChars = inValideBeginChars;
		if (sourceDataIsISO)
			gbkChars = StringUtil.iso2gbk(inValideBeginChars);

		String tmpStr = "";

		int tPos = -1;

		if (inputStr == null) {
			return "";
		}

		tmpStr = inputStr;

		if (tmpStr.length() > 0) {
			char firstWord = tmpStr.charAt(0);
			tPos = gbkChars.indexOf(firstWord, 0);
			while (tPos > -1) {
				if (tmpStr.length() == 1) {
					tmpStr = "";
					break;
				}
				tmpStr = tmpStr.substring(1);
				firstWord = tmpStr.charAt(0);
				tPos = gbkChars.indexOf(firstWord, 0);
			}
		}

		return tmpStr;
	}

	public String getDigestNoKeyword(String strContent) {

		String tmpStr = "";

		if (strContent == null) {
			return "";
		}

		int contentLen = strContent.length();
		if (contentLen <= MaxDigestLength) {
			tmpStr = strContent;
		} else {
			tmpStr = strContent.substring(0, MaxDigestLength);
			tmpStr = removeInvalidStartChars(tmpStr);
			tmpStr = getSentenceEnd(tmpStr);
			tmpStr = tmpStr + "...";
		}

		return (tmpStr.length() >= MinDigestLength) ? tmpStr : "";
	}

	public static String getKeywordWithAreaItem(String keyword, String areaDic) {
		String strFullKeyword = null;

		if (areaDic != null && areaDic.length() > 1) {
			String areaItems[] = areaDic.split("\n");
			if (areaItems != null && areaItems.length > 0) {
				for (int i = 0; i < areaItems.length; i++) {
					if (strFullKeyword == null)
						strFullKeyword = "(" + areaItems[i];
					else
						strFullKeyword += "," + areaItems[i];
				}
			}
		}

		if (strFullKeyword != null)
			strFullKeyword = keyword + "+" + strFullKeyword + ")";
		else
			strFullKeyword = keyword;

		strFullKeyword = getKeywordWithoutNOT(strFullKeyword);

		return strFullKeyword;
	}

	private List<String> getSentences(String content) {
		List<String> sentences = new ArrayList<String>();
		String sentence = "";
		int preIndex = 0;
		for (int i = 0; i < content.length(); i++) {
			if (isSymbolInStr(content.charAt(i), symbols) == 1
					|| i == content.length() - 1) {
				sentence = content.substring(preIndex, i + 1);
				sentences.add(sentence);
				preIndex = i + 1;
			}
		}
		if (sentences.size() == 0)
			sentences.add(content);
		return sentences;
	}

	private int isSymbolInStr(char ch, char[] symbols) {
		int flag = 0;
		for (char symbol : symbols) {
			if (ch == symbol) {
				flag = 1;
				break;
			}
		}
		return flag;
	}

	public String getSentenceDigest(String strContent) {
		StringBuffer strDigestBuff = new StringBuffer();

		if ((strContent == null) || (strContent.trim().length() == 0))
			return "";

		List<String> sentences = getSentences(strContent);

		if (this.andList.size() == 0)
			parseLegalExpr();

		ArrayList<ArrayList<String>> keywordList = getANDList();

		String tempDigest = "";

		for (int i = 0; i < sentences.size(); i++) {
			String sentence = sentences.get(i);
			tempDigest = getSentenceDigest(sentence, keywordList);
			if (tempDigest.trim().length() == 0)
				continue;

			if (strDigestBuff.length() < MaxCompareDigestLength) {
				strDigestBuff.append(tempDigest);
			}
		}

		return strDigestBuff.toString();

	}

	private String getSentenceDigest(String strContent,
			ArrayList<ArrayList<String>> KeywordANDList) {
		for (ArrayList<String> keywordList : KeywordANDList) {
			for (String keyword : keywordList) {
				if (strContent.contains(keyword))
					return strContent;
			}
		}

		return "";
	}

	public static void main(String[] args) {
		String keyword = "\"新三 板\"+(新三板融资,,新三板挂牌，新三板定增,新三板定向增发,新三板融资,调研报告,新三板投资,新三板挂牌公司,新三板细则,新三板制度,新三板市场,新三板企业,新三板活动)";

		String content = ": #新三板# 【 新三板 \"抢食\"区域股权交易市场】 在日前举行的\"2013中国企业新三板挂牌与项目融资年会\"上，记者了解到，已经有一些企业准备从区域股权交易所退出，转入新三板市场。不少企业转投新三板，除了融资需求外，主要是看好未来新三板的交易制度以及对转板的预期。";

		KeywordParser parser = new KeywordParser(keyword);
		parser.setISOSource(false);

		ArrayList<ArrayList<String>> allANDList = parser.getANDList();

		int andCount = 0;
		for (ArrayList<String> andList : allANDList) {
			System.out.println("And Index=" + andCount);

			for (String andKeyword : andList)
				System.out.println("and Keyword=" + andKeyword);

			andCount++;
		}

		System.out.println(parser.getKeywordDigest(content));
	}
}
