package edu.columbia.advancedb.bing.vo;

public class AppTuple<P, Q> {
	
	private P item1;
	private Q item2;
	
	public AppTuple(P item1, Q item2) {
		this.item1 = item1;
		this.item2 = item2;
	}
	
	public P getItem1() {
		return item1;
	}
	
	public Q getItem2() {
		return item2;
	}
	
	public void setItem1(P item1) {
		this.item1 = item1;
	}
	
	public void setItem2(Q item2) {
		this.item2 = item2;
	}
}