package com.bizcom.request.jni;

public class RequestConfChairManChange extends JNIIndication {

	public long confID;
	public long chairManID;
	public RequestConfChairManChange(long confID, long chairManID) {
		super(Result.SUCCESS);
		this.confID = confID;
		this.chairManID = chairManID;
	}
}
