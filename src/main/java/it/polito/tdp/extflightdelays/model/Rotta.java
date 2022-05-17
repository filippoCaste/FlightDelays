package it.polito.tdp.extflightdelays.model;

public class Rotta {

	private Airport a1;
	private Airport a2;
	private int nVoli;
	
	public Rotta(Airport a1, Airport a2, int nVoli) {
		super();
		this.a1 = a1;
		this.a2 = a2;
		this.nVoli = nVoli;
	}

	public Airport getA1() {
		return a1;
	}

	public Airport getA2() {
		return a2;
	}

	public int getnVoli() {
		return nVoli;
	}
}
