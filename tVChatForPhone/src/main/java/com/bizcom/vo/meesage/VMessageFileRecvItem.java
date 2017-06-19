package com.bizcom.vo.meesage;


public class VMessageFileRecvItem extends VMessageAbstractItem {

	private String fileID;
	private int recvResult;

	public VMessageFileRecvItem(VMessage vm, String fileID, int recvResult) {
		super(vm, ITEM_TYPE_FILE_RECV);
		this.fileID = fileID;
		this.recvResult = recvResult;
	}

	public String getFileID() {
		return fileID;
	}

	public void setFileID(String fileID) {
		this.fileID = fileID;
	}

	public int getRecvResult() {
		return recvResult;
	}

	public void setRecvResult(int recvResult) {
		this.recvResult = recvResult;
	}

	@Override
	public String toXmlItem() {
		String str = "<TFileRecvResultChatItem NewLine=\""
				+ (isNewLine ? "True" : "False") + "\" FileID=\"" + fileID
				+ "\" RecvResult=\"" + recvResult + "\"/>";
		return str;
	}
}
