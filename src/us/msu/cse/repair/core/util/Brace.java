package us.msu.cse.repair.core.util;

public class Brace implements Comparable<Brace> {
	int position;
	char mark;
	
	public Brace(char mark, int position) {
		this.mark = mark;
		this.position = position;
	}
	
	public void setMark(char mark) {
		this.mark = mark;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
	
	public int getPosition() {
		return this.position;
	}
	
	public char getMark() {
		return this.mark;
	}

	@Override
	public int compareTo(Brace o) {
		// TODO Auto-generated method stub
		return this.position - o.getPosition();
	}
}
