package com.bizcom.util.nanotasks;

public interface BackgroundWork<T> {
	T doInBackground() throws Exception;
}
