/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package technology.tabula;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.RandomAccess;
import java.util.Stack;

/**
 * An implementation of Quicksort.
 * 
 * @see http://de.wikipedia.org/wiki/Quicksort
 * 
 * @author UWe Pachler
 */
public final class QuickSort {

	private QuickSort() {
		// utility
	}

	/**
	 * Sorts the given list according to natural order.
	 */
	public static <T extends Comparable<? super T>> void sort(List<T> list) {
		sort(list, QuickSort.<T>naturalOrder()); // JAVA_8 replace with Comparator.naturalOrder() (and cleanup)   
	}

	/**
	 * Sorts the given list using the given comparator.
	 */
	public static <T> void sort(List<T> list, Comparator<? super T> comparator) {
		if (list instanceof RandomAccess) {
			quicksort(list, comparator);
		} else {
			List<T> copy = new ArrayList<>(list);
			quicksort(copy, comparator);
			list.clear();
			list.addAll(copy);
		}
	}

	private static <T> void quicksort(List<T> list, Comparator<? super T> cmp) {
		Stack<Integer> stack = new Stack<>();
		stack.push(0);
		stack.push(list.size());
		while (!stack.isEmpty()) {
			int right = stack.pop();
			int left = stack.pop();
			
			if (right - left < 2) continue;
			int p = left + ((right - left) / 2);
			p = partition(list, cmp, p, left, right);

			stack.push(p + 1);
			stack.push(right);

			stack.push(left);
			stack.push(p);
		}
	}

	private static <T> int partition(List<T> list, Comparator<? super T> cmp, int p, int start, int end) {
		int l = start;
		int h = end - 2;
		T piv = list.get(p);
		swap(list, p, end - 1);

		while (l < h) {
			     if (cmp.compare(list.get(l), piv) <= 0) l++;
			else if (cmp.compare(piv, list.get(h)) <= 0) h--;
			else                                         swap(list, l, h);
		}
		int idx = h;
		if (cmp.compare(list.get(h), piv) < 0) idx++;
		swap(list, end - 1, idx);
		return idx;
	}

	private static <T> void swap(List<T> list, int i, int j) {
		T tmp = list.get(i);
		list.set(i, list.get(j));
		list.set(j, tmp);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static final Comparator NATURAL_ORDER = new Comparator() {
		@Override public int compare(Object l, Object r) { return ((Comparable) l).compareTo(r); }
	};
	
	@SuppressWarnings("unchecked")
	private static <T extends Comparable<? super T>> Comparator<T> naturalOrder() {
		return NATURAL_ORDER;
	} 

}
