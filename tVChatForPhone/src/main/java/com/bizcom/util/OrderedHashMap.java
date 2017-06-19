package com.bizcom.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bizcom.vo.whiteboard.V2Doc;

public class OrderedHashMap<K, V> extends HashMap<K, V> {
	private static final long serialVersionUID = 1L;
	private List<K> keylist = null;

	@Override
	public V put(K key, V value) {
		if (key != null) {
			if (keylist == null) {
				keylist = new ArrayList<K>();
			}
			keylist.add(key);
		}
		return super.put(key, value);
	}

	@Override
	public V remove(Object key) {
		if (key != null) {
			if (keylist == null) {
				keylist = new ArrayList<K>();
			} else {
				keylist.remove(key);
			}
		}
		return super.remove(key);
	}
	
	public V2Doc removeAndShowNext(Object key){
		V2Doc mDoc = null;
		if (keylist == null) {
			keylist = new ArrayList<K>();
		} else {
			if(keylist.size() <= 1){
				return null;
			}
			
			int index = keylist.indexOf(key);
			if(index + 1 >= keylist.size()){
				if(index - 1 <= 0){
					return null;
				} else {
					mDoc = (V2Doc) get(keylist.get(index - 1));
				}
			} else {
				mDoc = (V2Doc) get(keylist.get(index + 1));
			}
			keylist.remove(key);
		}
		return mDoc;
	}

	public List<K> keyOrderList() {
		if (keylist == null) {
			keylist = new ArrayList<K>();
		}
		return keylist;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		@SuppressWarnings("unchecked")
		OrderedHashMap<K, V> temp = (OrderedHashMap<K, V>) map;
		List<K> keyOrderList = temp.keyOrderList();
		for (K key : keyOrderList) {
			put(key, temp.get(key));
		}
	}
}
