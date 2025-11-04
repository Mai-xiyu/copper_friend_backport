package org.xiyu.yee.copper_friend_backport;

@FunctionalInterface
public interface StreamEncoder<O, T> {
	void encode(O object, T object2);
}
