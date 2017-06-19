package com.bizcom.request;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.V2.jni.GroupRequest;
import com.V2.jni.ImRequest;
import com.V2.jni.callbacAdapter.GroupRequestCallbackAdapter;
import com.V2.jni.callbacAdapter.ImRequestCallbackAdapter;
import com.V2.jni.ind.BoUserInfoBase;
import com.V2.jni.ind.V2Group;
import com.bizcom.request.jni.JNIResponse;
import com.bizcom.request.util.HandlerWrap;
import com.bizcom.util.XmlAttributeExtractor;
import com.bizcom.vo.SearchedResult;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.config.V2GlobalConstants;

import java.util.List;

/**
 * Used to search
 * 
 * @author 28851274
 * 
 */
public class V2SearchRequest extends V2AbstractHandler {

	private static final int SEARCH = 1;

	private ImRequestCallbackCB imCB;
	private GroupRequestCB grCB;

	public V2SearchRequest() {
		super();
		imCB = new ImRequestCallbackCB(this);
		ImRequest.getInstance().addCallback(imCB);

		grCB = new GroupRequestCB(this);
		GroupRequest.getInstance().addCallback(grCB);
	}

	/**
	 * Search content from server side
	 * 
	 * @param par
	 * @param caller
	 */
	public void search(SearchParameter par, HandlerWrap caller) {
		if (!this.checkParamNull(caller, par)) {
			return;
		}

		initTimeoutMessage(SEARCH, DEFAULT_TIME_OUT_SECS, caller);
		int startNo = (par.mPageNo - 1) * par.mPageSize;
		if (par.mType == V2GlobalConstants.SEARCH_REQUEST_TYPE_GROUP) {
			int gType = V2GlobalConstants.GROUP_TYPE_CROWD;
			GroupRequest.getInstance().GroupSearch(gType, par.text, startNo,
					par.mPageSize);
		} else if (par.mType == V2GlobalConstants.SEARCH_REQUEST_TYPE_USER) {
			ImRequest.getInstance().ImSearchUsers(par.text,
					startNo, par.mPageSize);
		}
	}

	@Override
	public void clearCalledBack() {
		ImRequest.getInstance().removeCallback(imCB);
		GroupRequest.getInstance().removeCallback(grCB);
	}

	class ImRequestCallbackCB extends ImRequestCallbackAdapter {

		private Handler mCallbackHandler;

		public ImRequestCallbackCB(Handler mCallbackHandler) {

			this.mCallbackHandler = mCallbackHandler;
		}
		
		@Override
		public void OnSearchUserCallback(String xmlinfo) {
			super.OnSearchUserCallback(xmlinfo);
			Log.i("tvliao","xmlinfo-"+xmlinfo);
			List<BoUserInfoBase> list = XmlAttributeExtractor.parseUserList(
					xmlinfo, "user");
			SearchedResult result = new SearchedResult();
			for (BoUserInfoBase u : list) {
				User user = GlobalHolder.getInstance().putOrUpdateUser(u);
//				GlobalHolder.getInstance().putOrUpdateUser(u);
				result.addItem(V2GlobalConstants.SEARCH_REQUEST_TYPE_USER, u.mId, u.getNickName());
				Log.i("tvliao","OnSearchUserCallback-"+user.toXml());


			}
			JNIResponse jniRES = new JNIResponse(JNIResponse.Result.SUCCESS);
			jniRES.resObj = result;
			Message.obtain(mCallbackHandler, SEARCH, jniRES).sendToTarget();
		}
	}

	class GroupRequestCB extends GroupRequestCallbackAdapter {
		private Handler mCallbackHandler;

		public GroupRequestCB(Handler mCallbackHandler) {

			this.mCallbackHandler = mCallbackHandler;
		}

		@Override
		public void OnSearchGroup(int eGroupType, String infoXml) {
			super.OnSearchGroup(eGroupType, infoXml);
			List<V2Group> list = XmlAttributeExtractor.parseCrowd(infoXml);
			SearchedResult result = new SearchedResult();
			for (V2Group g : list) {
				User creator = new User(g.creator.mId, g.creator.getNickName());
				result.addCrowdItem(g.id, g.getName(), creator, g.getBrief(), g.authType);
			}
			JNIResponse jniRES = new JNIResponse(JNIResponse.Result.SUCCESS);
			jniRES.resObj = result;
			Message.obtain(mCallbackHandler, SEARCH, jniRES).sendToTarget();
		}
	}

	public SearchParameter generateSearchPatameter(int type, String content,
			int nPage) {
		SearchParameter par = new SearchParameter();
		par.mType = type;
		par.text = content;
		par.mPageNo = nPage;
		par.mPageSize = 200;
		return par;
	}

	public class SearchParameter {
		String text;
		int mType;
		int mPageNo;
		int mPageSize;
	}

	public enum Type {
		CONFERENCE, CROWD, MEMBER, ALL
	}
}
