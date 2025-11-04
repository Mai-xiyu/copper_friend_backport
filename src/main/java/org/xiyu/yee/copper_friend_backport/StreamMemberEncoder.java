package org.xiyu.yee.copper_friend_backport;

@FunctionalInterface
public interface StreamMemberEncoder<O, T> {
	void encode(T object, O object2);
}
