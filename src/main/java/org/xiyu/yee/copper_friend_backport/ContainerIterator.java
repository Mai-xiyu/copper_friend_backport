package org.xiyu.yee.copper_friend_backport;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ContainerIterator implements Iterator<ItemStack> {
		private final Container container;
		private int index;
		private final int size;

		public ContainerIterator(Container container) {
			this.container = container;
			this.size = container.getContainerSize();
		}

		public boolean hasNext() {
			return this.index < this.size;
		}

		public ItemStack next() {
			if (!this.hasNext()) {
				throw new NoSuchElementException();
			} else {
				return this.container.getItem(this.index++);
			}
		}
	}