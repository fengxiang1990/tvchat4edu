package com.bizcom.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.V2.jni.ind.BoUserInfoBase;
import com.V2.jni.ind.FileJNIObject;
import com.V2.jni.ind.V2Group;
import com.bizcom.request.util.EscapedcharactersProcessing;
import com.bizcom.vo.User;
import com.config.GlobalConfig;
import com.config.V2GlobalConstants;

import android.text.TextUtils;

public class XmlAttributeExtractor {

	public static String extract(String str, String startStr, String endStr) {
		if (startStr == null || endStr == null || startStr.isEmpty()
				|| endStr.isEmpty()) {
			return null;
		}
		int start = str.indexOf(startStr);
		if (start == -1) {
			return null;
		}
		int len = startStr.length();
		int end = str.indexOf(endStr, start + len);
		if (end == -1) {
			return null;
		}

		return EscapedcharactersProcessing.reverse(str.substring(start + len,
				end));
	}
	
	/**
	 * value:{"userid":116,"msgtype":1,"groupid":0}
	 * @param str
	 * @param attribute
	 * @return
	 */
	public static String extractJPushAttribute(String str, String attribute) {
		if (TextUtils.isEmpty(str) || TextUtils.isEmpty(attribute)) {
			return null;
		}

		String key = "\"" + attribute + "\":";
		int start = str.indexOf(key);
		if (start == -1) {
			return null;
		}
		int len = key.length();
		String endFlag = ",";
		int end = str.indexOf(endFlag, start + len);
		if (end == -1) {
			return null;
		}

		String check = str.substring(start + len, end);
		return check;
	}

	/**
	 * <crowd announcement='ss' authtype='0' creatoruserid='12176' id='423'
	 * name='ccc' size='500' summary='bb'/>
	 * 
	 * @param str
	 * @param attribute
	 *            just input creatoruserid
	 * @return
	 */
	public static String extractAttribute(String str, String attribute) {
		if (TextUtils.isEmpty(str) || TextUtils.isEmpty(attribute)) {
			return null;
		}

		String key = " " + attribute + "=";
		int start = str.indexOf(key);
		if (start == -1) {
			return null;
		}
		int len = key.length() + 1;

		String flag = str.substring(start + len - 1, start + len);

		int end = str.indexOf(flag.toString(), start + len);
		if (end == -1) {
			return null;
		}

		String check = str.substring(start + len, end);
		return EscapedcharactersProcessing.reverse(check);
	}

	/**
	 * 
	 * @param xml
	 * @param tag
	 * @return
	 */
	public static List<BoUserInfoBase> parseUserList(String xml, String tag) {
		Document doc = buildDocument(xml);
		List<BoUserInfoBase> listUser = new ArrayList<BoUserInfoBase>();
		NodeList userNodeList = doc.getElementsByTagName(tag);
		Element userElement;

		for (int i = 0; i < userNodeList.getLength(); i++) {
			userElement = (Element) userNodeList.item(i);
			BoUserInfoBase user = null;
			String uid = userElement.getAttribute("id");
			if (uid != null && !uid.isEmpty()) {
				user = new BoUserInfoBase(Long.parseLong(uid));
				String name = userElement.getAttribute("nickname");
				String account = userElement.getAttribute("account");
				user.setNickName_name(name);
				user.setmAccount(account);
				listUser.add(user);
			}
		}

		return listUser;
	}

	public static List<V2Group> parseConference(String xml) {
		Document doc = buildDocument(xml);
		if (doc == null) {
			return null;
		}
		List<V2Group> listConf = new ArrayList<V2Group>();
		NodeList conferenceList = doc.getElementsByTagName("conf");
		Element conferenceElement;
		for (int i = 0; i < conferenceList.getLength(); i++) {
			conferenceElement = (Element) conferenceList.item(i);
			String chairManStr = conferenceElement.getAttribute("chairman");
			long cid = 0;
			if (chairManStr != null && !chairManStr.isEmpty()) {
				cid = Long.parseLong(chairManStr);
			}

			String time = conferenceElement.getAttribute("starttime");
			Long times = Long.valueOf(time) * 1000;
			Date date = new Date(times);
			String name = conferenceElement.getAttribute("subject");
			String uid = conferenceElement.getAttribute("createuserid");
			BoUserInfoBase user = null;
			if (uid != null && !uid.isEmpty()) {
				user = new BoUserInfoBase(Long.parseLong(uid));
			}

			listConf.add(new V2Group(Long.parseLong(conferenceElement
					.getAttribute("id")), name, V2Group.TYPE_CONF, user, date,
					new BoUserInfoBase(cid)));
		}
		return listConf;
	}

	/**
	 * <pre>
	 *  xml示例
	 *  OnGetGroupInfo==>groupType:3 
	 * sXml: < crowd announcement='' authtype='0' creatoruserid='11112130'
	 * 		id='411172' name='zzz' size='500' summary=''/>
	 *  IOS发送：
	 *  OnInviteJoinGroup::==>3:
	 *  	sXml:< crowd id='14000144' name='nmm'/>:
	 *  	userInfos:< user id='110005'/>
	 *  
	 *  其他发送：
	 *  OnInviteJoinGroup::==>3:
	 *  	sXml:< crowd authtype='0' creatoruserid='11000102' id='14000128' name='qazzaq' size='500'/>:
	 *  	userInfos:< user id='11000102'/>
	 *  
	 *  OnRefuseApplyJoinGroup
	 *  
	 *  OnAcceptApplyJoinGroup::==> groupType:3:
	 *  	sXml:< crowd announcement='' authtype='0' creatoruserid='1290' id='492' name='12' size='0' summary=''/>
	 * </pre>
	 * 
	 * @param sXml
	 * @param userInfos
	 * @return
	 */
	public static V2Group parseSingleCrowd(String sXml, String userInfos) {
		if (TextUtils.isEmpty(sXml)) {
			V2Log.e("XmlAttributeExtractor parseSingleCrowd --> parse failed , given xml is null");
			return null;
		}

		try {
			XmlPullParser pull = XmlPullParserFactory.newInstance()
					.newPullParser();
			pull.setInput(new ByteArrayInputStream(sXml.getBytes()), "UTF-8");
			int eventCode = pull.getEventType();
			V2Group v2Group = null;
			while (eventCode != XmlPullParser.END_DOCUMENT) {
				switch (eventCode) {
				case XmlPullParser.START_TAG:
					if ("crowd".equals(pull.getName())) {
						String id = pull.getAttributeValue(null, "id");
						if (TextUtils.isEmpty(id)) {
							V2Log.e("XmlAttributeExtractor parseSingleCrowd --> parse failed ,Unknow group information,"
									+ " parse group id is null , xml is : "
									+ sXml);
							return null;
						}

						String name = pull.getAttributeValue(null, "name");
						v2Group = new V2Group(Long.parseLong(id), name,
								V2GlobalConstants.GROUP_TYPE_CROWD);

						String summary = pull
								.getAttributeValue(null, "summary");
						String announcement = pull.getAttributeValue(null,
								"announcement");
						String authtype = pull.getAttributeValue(null,
								"authtype");
						String creatoruserid = pull.getAttributeValue(null,
								"creatoruserid");
						v2Group.setAnnounce(announcement);
						v2Group.setBrief(summary);
						if (authtype != null) {
							v2Group.authType = Integer.parseInt(authtype);
						}

						if (!TextUtils.isEmpty(creatoruserid)) {
							BoUserInfoBase u = new BoUserInfoBase();
							u.mId = Long.parseLong(creatoruserid);
							v2Group.owner = u;
							v2Group.creator = u;
						}
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				eventCode = pull.next();
			}

			if (v2Group == null) {
				V2Log.e("XmlAttributeExtractor parseSingleCrowd --> Parse Failed! , Unknow Group Information , "
						+ "parse group infos is null , xml is : " + sXml);
				return null;
			}

			if (v2Group.owner == null) {
				if (!TextUtils.isEmpty(userInfos)) {
					String creatorID = XmlAttributeExtractor.extractAttribute(
							userInfos, "id");
					if (TextUtils.isEmpty(creatorID)) {
						V2Log.e("XmlAttributeExtractor parseSingleCrowd --> Parse Failed! , Unknow Group Createor User Information , "
								+ "parse creator id is null , sXml is : "
								+ sXml + " and userInfo: " + userInfos);
						return null;
					}

					BoUserInfoBase u = new BoUserInfoBase();
					u.mId = Long.parseLong(creatorID);
					v2Group.owner = u;
					v2Group.creator = u;
				} else {
					V2Log.e("XmlAttributeExtractor parseSingleCrowd --> Parse Failed! , Unknow Group Createor User Information , "
							+ "sXml is : "
							+ sXml
							+ " and userInfo: "
							+ userInfos);
					return null;
				}
			}
			return v2Group;
		} catch (Exception e) {
			e.printStackTrace();
			V2Log.e("XmlAttributeExtractor parseSingleCrowd --> Parse Failed! 发生异常");
			return null;
		}
	}

	public static List<V2Group> parseCrowd(String xml) {
		Document doc = buildDocument(xml);
		if (doc == null) {
			V2Log.e("XmlAttributeExtractor parseCrowd --> parse xml failed...get Document is null...xml is : "
					+ xml);
			return null;
		}
		List<V2Group> listCrowd = new ArrayList<V2Group>();
		NodeList crowdList = doc.getElementsByTagName("crowd");
		Element crowdElement;

		for (int i = 0; i < crowdList.getLength(); i++) {
			crowdElement = (Element) crowdList.item(i);
			BoUserInfoBase creator = null;
			String uid = crowdElement.getAttribute("creatoruserid");
			if (uid != null && !uid.isEmpty()) {
				creator = new BoUserInfoBase(Long.parseLong(uid),
						crowdElement.getAttribute("creatornickname"));
			}

			String id = crowdElement.getAttribute("id");
			if (TextUtils.isEmpty(id)) {
				V2Log.e("parseCrowd the id is wroing...break");
				continue;
			}

			if (crowdElement.getAttribute("name") == null)
				V2Log.e("parseCrowd the name is wroing...the group is :"
						+ crowdElement.getAttribute("id"));
			long gid = Long.parseLong(crowdElement.getAttribute("id"));

			String crowdName = crowdElement.getAttribute("name");
			crowdName = EscapedcharactersProcessing.reverse(crowdName);

			V2Group crowd = new V2Group(gid, crowdName, V2Group.TYPE_CROWD,
					creator);

			crowd.createTime = new Date(GlobalConfig.getGlobalServerTime());
			crowd.setAnnounce(crowdElement.getAttribute("announcement"));
			crowd.setBrief(crowdElement.getAttribute("summary"));
			crowd.creator = creator;
			String authType = crowdElement.getAttribute("authtype");
			crowd.authType = authType == null ? 0 : Integer.parseInt(authType);
			listCrowd.add(crowd);
		}

		return listCrowd;
	}

	public static List<V2Group> parseContactsGroup(String xml) {
		Document doc = buildDocument(xml);
		if (doc == null) {
			return null;
		}
		if (doc.getChildNodes().getLength() <= 0) {
			return null;
		}
		List<V2Group> list = new ArrayList<V2Group>();
		iterateNodeList(V2Group.TYPE_CONTACTS_GROUP, null, doc.getChildNodes()
				.item(0).getChildNodes(), list);
		return list;
	}

	public static List<V2Group> parseOrgGroup(String xml) {
		Document doc = buildDocument(xml);
		if (doc == null) {
			return null;
		}
		if (doc.getChildNodes().getLength() <= 0) {
			return null;
		}
		List<V2Group> list = new ArrayList<V2Group>();
		iterateNodeList(V2Group.TYPE_ORG, null, doc.getChildNodes().item(0)
				.getChildNodes(), list);

		return list;
	}

	public static List<V2Group> parseDiscussionGroup(String xml) {
		Document doc = buildDocument(xml);
		if (doc == null) {
			return null;
		}
		if (doc.getChildNodes().getLength() <= 0) {
			return null;
		}
		List<V2Group> list = new ArrayList<V2Group>();
		iterateNodeList(V2Group.TYPE_DISCUSSION_BOARD, null, doc
				.getChildNodes().item(0).getChildNodes(), list);

		return list;
	}

	private static void iterateNodeList(int type, V2Group parent,
			NodeList gList, List<V2Group> list) {

		for (int j = 0; j < gList.getLength(); j++) {
			Element subGroupEl = (Element) gList.item(j);
			V2Group group = null;

			group = new V2Group(Long.parseLong(subGroupEl.getAttribute("id")),
					subGroupEl.getAttribute("name"), type);
			group.createTime = new Date(GlobalConfig.getGlobalServerTime());
			// If type is contact and is first item, means this group is default
			if (type == V2Group.TYPE_CONTACTS_GROUP && j == 0) {
				group.isDefault = true;
				group.setName("");
			}

			if (type == V2Group.TYPE_DISCUSSION_BOARD) {
				String uid = subGroupEl.getAttribute("creatoruserid");
				if (uid != null && !uid.isEmpty()) {
					BoUserInfoBase creator = new BoUserInfoBase(
							Long.parseLong(uid), "");
					group.owner = creator;
				} else {
					continue;
				}
			}

			if (parent == null) {
				list.add(group);
			} else {
				parent.childs.add(group);
				group.parent = parent;
			}
			// Iterate sub group
			iterateNodeList(type, group, subGroupEl.getChildNodes(), null);
		}

	}

	/**
	 * <file encrypttype='1' id='C2A65B9B-63C7-4C9E-A8DD-F15F74ABA6CA'
	 * name='83025aafa40f4bfb24fdb8d1034f78f0f7361801.gif' size='497236'
	 * time='1411112464' uploader='11029' url=
	 * 'http://192.168.0.38:8090/crowd/C2A65B9B-63C7-4C9E-A8DD-F15F74ABA6CA/
	 * C2A65B9B
	 * -63C7-4C9E-A8DD-F15F74ABA6CA/83025aafa40f4bfb24fdb8d1034f78f0f7361801
	 * .gif'/>
	 * 
	 * @param xml
	 */
	public static List<FileJNIObject> parseFiles(String xml) {
		Document doc = buildDocument(xml);
		if (doc == null) {
			return null;
		}
		if (doc.getChildNodes().getLength() <= 0) {
			return null;
		}

		List<FileJNIObject> list = new ArrayList<FileJNIObject>();
		NodeList nList = doc.getChildNodes().item(0).getChildNodes();
		if (nList.getLength() <= 0) {
			Element el = (Element) doc.getChildNodes().item(0);
			buildUploadFiles(list, el);
		} else {
			for (int j = 0; j < nList.getLength(); j++) {
				Element el = (Element) nList.item(j);
				buildUploadFiles(list, el);
			}
		}
		return list;
	}

	/**
	 * build upload file Object
	 * 
	 * @param list
	 * @param el
	 */
	private static void buildUploadFiles(List<FileJNIObject> list, Element el) {
		String id = el.getAttribute("id");
		String name = el.getAttribute("name");
		String uploader = el.getAttribute("uploader");
		String url = el.getAttribute("url");
		String size = el.getAttribute("size");
		int index = name.lastIndexOf("/");
		if (index != -1) {
			name = name.substring(index);
		}

		FileJNIObject file = new FileJNIObject(new BoUserInfoBase(
				Long.parseLong(uploader)), id, name, Long.parseLong(size), 1 , url);
		list.add(0, file);
	}

	public static String buildAttendeeUsersXml(User at) {
		String target;
		if (at.getDisplayName() != null) {
			String nickname = EscapedcharactersProcessing.convert(at
					.getDisplayName());
			target = " <user id='" + at.getmUserId() + "' nickname='"
					+ nickname + "' />";
		} else {
			V2Log.e("XmlAttributeExtractor buildAttendeeUsersXml--> when build attendee user list , "
					+ " the user's name is null , id is : " + at.getmUserId());
			target = " <user id='" + at.getmUserId() + "' />";
		}
		return target;
	}

	public static String buildAttendeeUsersXml(List<User> list) {
		StringBuffer attendees = new StringBuffer();
		attendees.append("<userlist> ");
		for (User at : list) {
			attendees.append(buildAttendeeUsersXml(at));
		}
		attendees.append("</userlist>");
		return attendees.toString();
	}

	public static Document buildDocument(String xml) {
		if (xml == null || xml.isEmpty()) {
			V2Log.e(" conference xml is null");
			return null;
		}

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		InputStream is = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			Document doc = dBuilder.parse(is);

			doc.getDocumentElement().normalize();
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 
	 * @param xml
	 * @return
	 */
	public static BoUserInfoBase fromGroupXml(String xml) {
		String id = extractAttribute(xml, "id");
		if (TextUtils.isEmpty(id))
			return null;
		else
			return fromXml(Long.valueOf(id), xml);
	}

	/**
	 * @param xml
	 * @return
	 */
	public static BoUserInfoBase fromXml(long userID, String oldXml) {
		String xml = EscapedcharactersProcessing.reverse(oldXml);
		String nickName = extractAttribute(xml, "nickname");
		String signature = extractAttribute(xml, "sign");
		String job = extractAttribute(xml, "job");
		String telephone = extractAttribute(xml, "telephone");
		String mobile = extractAttribute(xml, "mobile");
		String address = extractAttribute(xml, "address");
		String gender = extractAttribute(xml, "sex");
		String email = extractAttribute(xml, "email");
		String bir = extractAttribute(xml, "birthday");
		String account = extractAttribute(xml, "account");
		String fax = extractAttribute(xml, "fax");
		String commentname = extractAttribute(xml, "commentname");
		String authtype = extractAttribute(xml, "authtype");

		DateFormat dp = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

		BoUserInfoBase boUserBaseInfo = new BoUserInfoBase(userID, nickName);
		boUserBaseInfo.setmSignature(signature);
		boUserBaseInfo.mJob = job;
		boUserBaseInfo.mTelephone = telephone;
		boUserBaseInfo.mMobile = mobile;
		boUserBaseInfo.mAddress = address;
		boUserBaseInfo.mSex = gender;
		boUserBaseInfo.mEmail = email;
		boUserBaseInfo.mFax = fax;
		boUserBaseInfo.mAccount = account;
		boUserBaseInfo.setCommentName(commentname);
		boUserBaseInfo.mAuthtype = authtype;

		if (bir != null && bir.length() > 0) {
			try {
				boUserBaseInfo.mBirthday = dp.parse(bir);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return boUserBaseInfo;
	}

}
