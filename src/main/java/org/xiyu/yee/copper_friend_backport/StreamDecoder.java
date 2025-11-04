package org.xiyu.yee.copper_friend_backport;

@FunctionalInterface
public interface StreamDecoder<I, T> {
	T decode(I object);
}
