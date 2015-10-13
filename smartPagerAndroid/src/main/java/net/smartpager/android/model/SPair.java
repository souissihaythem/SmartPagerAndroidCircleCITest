package net.smartpager.android.model;

import java.io.Serializable;

/**
 * Serializable Pair
 * @author Roman
 * @param <F> first pair element
 * @param <S> second pair element
 */
public class SPair<F, S> implements Serializable {

	private static final long serialVersionUID = 6441712721693489488L;

	public F first;
	public S second;

	public SPair(F first, S second) {
		this.first = first;
		this.second = second;
	}
}
