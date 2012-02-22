package kr.co.ex.hiwaysnsclient.db;


public class TrOASISMessage {
	private int id;
	private int messageId;
	private String title;
	private String content;
	private String createdTime;
	private int isPopup;
	private int isRead;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getMessageId() {
		return messageId;
	}
	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}
	public int isPopup() {
		return isPopup;
	}
	public void setPopup(int isPopup) {
		this.isPopup = isPopup;
	}
	public int isRead() {
		return isRead;
	}
	public void setRead(int isRead) {
		this.isRead = isRead;
	}
	
	
}
