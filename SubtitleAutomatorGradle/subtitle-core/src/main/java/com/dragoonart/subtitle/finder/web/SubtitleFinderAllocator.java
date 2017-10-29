package com.dragoonart.subtitle.finder.web;

import stormpot.Allocator;
import stormpot.Slot;

public class SubtitleFinderAllocator implements Allocator<SubtitleFinder>{

	@Override
	public SubtitleFinder allocate(Slot slot) throws Exception {
		SubtitleFinder finder = new SubtitleFinder(slot);
		return finder;
	}

	@Override
	public void deallocate(SubtitleFinder poolable) throws Exception {
		poolable.shutdown();
	}
}
