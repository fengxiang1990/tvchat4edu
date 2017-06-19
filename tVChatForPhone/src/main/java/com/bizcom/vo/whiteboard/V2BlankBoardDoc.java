package com.bizcom.vo.whiteboard;

import com.bizcom.vo.Group;
import com.bizcom.vo.User;

public class V2BlankBoardDoc extends V2Doc {

	public V2BlankBoardDoc(String id, String docName, Group mGroup, int mBType, User mSharedUser) {
		super(id, docName, mGroup, mBType, mSharedUser);
		this.mDocType = DOC_TYPE_BLANK_BOARD;
	}

}
