package com.cewit.elve.common;

public class RowItem {
	private int style;
	private String title;
	private String content;
	private String note;
	private int icon;
	
		
	public RowItem(int style){
		this.style = style;
	}
	
	public RowItem(int style, String title) {
		this.style = style;
		this.title= title;
		
	}
	public int getStyle() {
		return style;
	}
	public void setStyle(int style) {
		this.style = style;
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
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public int getIcon() {
		return icon;
	}
	public void setIcon(int icon) {
		this.icon = icon;
	}
	
	
}
