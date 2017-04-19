package com.microservice.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;



public class ScanPanel implements Serializable
{
	private static final long serialVersionUID = 100062273273110134L;

	//ý�����������ĵ�ʶ���ʶ
	private String contentRegx;
	
	//��ҳ�ı�����򣺷����������ַ�ű���
	private String saveLinkTags;
	//��ҳ��ɨ����򣺷���blackLinks�е����ӽ�����ɨ��
	private String scanLinkTags;
	//ԭ�����ű�ǣ�ֻҪhtmlҳ�����������ʽ������Ϊ��ԭ������
	private String originTags;
	//��վ����ҳ���Ĺ��˴ʵ䣺���дʵ䲢�����˴ʹؼ��ʣ����ϲ����Ա���
	private String contentFilterDic;
	//��ý�������ű������Ч��ʶ
	private String invalidSubjectTags;
	
	//��ý��ɼ��������������Ĭ����200
	private int maxLinks;
	//��ý��ɼ��������ȣ�Ĭ����1
	private int maxDepths;
	//��ý��Ĳɼ����ӷ�Χ ��0:��ͬ��  1��ͬһ���� 2:ͬһĿ¼  3�������ƣ�
	private int webRange;
	
	//�Ƿ���ҪJS��̬���� false������Ҫ,ֻ����HttpClient��ʽ true����Ҫ ����HtmlUnit��ʽ
	private boolean isJSEnabled;
	
	//����Ȩ���ʵ���վ���й���Ȩ��Ϣ����¼�û����������Լ���Ȩ��֤URL��	
	private String authUser;	
	private String authPasswd;
	private String authUrl;	
		
	//��ý���������������ߣ��йص�ʶ���ʶ
	private String authorRegx;
	
	//ԭ��ת�ر�ʶ��ת����Ѷҳ���а���ԭ��ת�ر�ʶ������Ϊ��ת�أ�
	private String crtTags;
 	private ArrayList<String> crtTagList;
 		
	//epaper:��ȡý��������url�ı�ǩ���ƣ���Щ���Ӳ���Anchor������Area��
	private String newsUrlTag = null;
	
	
	//��վ��ʷ��¼
	private ConcurrentHashMap<String,String> historyUrls = new  ConcurrentHashMap<String,String>();
	
	
	public ScanPanel() {
		super();
	}
	
	public String getNewsUrlTag() {
		return newsUrlTag;
	}
	public void setNewsUrlTag(String newsUrlTag) {
		this.newsUrlTag = newsUrlTag;
	}
	
	public String getAuthorRegx() {
		return authorRegx;
	}
	public void setAuthorRegx(String authorRegx) {
		this.authorRegx = authorRegx;
	}
	
	public String getInvalidSubjectTags()
	{
		return this.invalidSubjectTags;
	}
	
	public void setInvalidSubjectTags(String tags)
	{
		this.invalidSubjectTags = tags;
	}
	
	public int getMaxLinks() {
		return maxLinks;
	}
	public void setMaxLinks(int maxLinks) {
		this.maxLinks = maxLinks;
	}
	public int getMaxDepths() {
		return maxDepths;
	}
	public void setMaxDepths(int maxDepths) {
		this.maxDepths = maxDepths;
	}
	public int getWebRange() {
		return webRange;
	}

	public void setWebRange(int webRange) {
		this.webRange = webRange;
	}
	
	public String getAuthUser() {
		return authUser;
	}
	public void setAuthUser(String authUser) {
		this.authUser = authUser;
	}
	public String getAuthPasswd() {
		return authPasswd;
	}
	public void setAuthPasswd(String authPasswd) {
		this.authPasswd = authPasswd;
	}
	public String getAuthUrl() {
		return authUrl;
	}
	public void setAuthUrl(String authUrl) {
		this.authUrl = authUrl;
	}
	public boolean isJsEnabled() {
		return isJSEnabled;
	}
	public void setJsEnabled(boolean jsEnabled) {
		this.isJSEnabled = jsEnabled;
	}
	
	public ConcurrentHashMap<String, String> getHistoryUrls() {
		return historyUrls;
	}
	
	public String getSaveLinkTags() {
		return saveLinkTags;
	}
	public void setSaveLinkTags(String saveLinkTags) {
		this.saveLinkTags = saveLinkTags;
	}
	
	public String getScanLinkTags() {
		return scanLinkTags;
	}
	public void setScanLinkTags(String scanLinks) {
		this.scanLinkTags = scanLinks;
	}
	
	public String getOriginTags() {
		return originTags;
	}

	public void setOriginTags(String originTags) {
		this.originTags = originTags;
	}

	public String getFilterDic() {
		return contentFilterDic;
	}
	public void setFilterDic(String filterDic) {
		contentFilterDic = filterDic;
	}
	
	public String getContentRegx() {
		return contentRegx;
	}
	public void setContentRegx(String contentRegx) {
		this.contentRegx = contentRegx;
	}
	
	public ArrayList<String> getCRTTagsList() {
		return crtTagList;
	}

	public void setCRTTagsList(ArrayList<String> crtTagList) {
		this.crtTagList = crtTagList;
	}
	
	public String getCRTTags() {
		return crtTags;
	}

	public void setCRTTags(String crtTags) {
		this.crtTags = crtTags;
	}
	
	/**
	 * ����Media��InvalidSubject���ж�ָ��linkSubject�Ƿ�����Ч������
	 * 
	 * @param linkSubject
	 * @return
	 */
	public boolean isValidLinkSubject(String linkSubject)
	{
		boolean isValid = true;
		
		if (this.invalidSubjectTags==null || this.invalidSubjectTags.length()==0)
			return true;
		
		String[] invalidTags = this.invalidSubjectTags.split("\\\n");
		for (int i=0;i<invalidTags.length;i++)
		{
			String tagRegx = invalidTags[i].trim();
			
			if (tagRegx.length() == 0)
				continue;
			
			if (linkSubject.matches(tagRegx))
			{
				isValid = false;
				break;
			}
		}
		
		return isValid;
	}
	
	/**
	 * �ж�ָ����URL�Ƿ��ڱ���վ�Ƿ����·���
	 * 
	 * @param url
	 * @return
	 */
	public boolean isNewUrl(String url)
	{
		boolean b = false;
		if(url == null || "".equals(url))
			return b;
		
		if(this.historyUrls.get(url)== null)
			b = true;
		
		return b;
	}
	
	/**
	 * �����website���йػ��棬�Ա�����������
	 */
	public void clearWebsiteMemCache()
	{
		this.historyUrls.clear();
		this.historyUrls = null;
	}
}
