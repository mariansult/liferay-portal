/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.kernel.servlet;

import com.liferay.portal.kernel.servlet.filters.compoundsessionid.CompoundSessionIdHttpSession;
import com.liferay.portal.kernel.servlet.filters.compoundsessionid.CompoundSessionIdSplitterUtil;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * <p>
 * See http://issues.liferay.com/browse/LEP-2299.
 * </p>
 *
 * @author Olaf Fricke
 * @author Brian Wing Shun Chan
 * @author Raymond Augé
 */
public class PortletSessionListenerManager
	implements HttpSessionActivationListener, HttpSessionAttributeListener,
			   HttpSessionBindingListener, HttpSessionListener {

	public static void addHttpSessionActivationListener(
		HttpSessionActivationListener httpSessionActivationListener) {

		_httpSessionActivationListeners.add(httpSessionActivationListener);
	}

	public static void addHttpSessionAttributeListener(
		HttpSessionAttributeListener httpSessionAttributeListener) {

		_httpSessionAttributeListeners.add(httpSessionAttributeListener);
	}

	public static void addHttpSessionBindingListener(
		HttpSessionBindingListener httpSessionBindingListener) {

		_httpSessionBindingListeners.add(httpSessionBindingListener);
	}

	public static void addHttpSessionListener(
		HttpSessionListener httpSessionListener) {

		_httpSessionListeners.add(httpSessionListener);
	}

	public static void removeHttpSessionActivationListener(
		HttpSessionActivationListener httpSessionActivationListener) {

		_httpSessionActivationListeners.remove(httpSessionActivationListener);
	}

	public static void removeHttpSessionAttributeListener(
		HttpSessionAttributeListener httpSessionAttributeListener) {

		_httpSessionAttributeListeners.remove(httpSessionAttributeListener);
	}

	public static void removeHttpSessionBindingListener(
		HttpSessionBindingListener httpSessionBindingListener) {

		_httpSessionBindingListeners.remove(httpSessionBindingListener);
	}

	public static void removeHttpSessionListener(
		HttpSessionListener httpSessionListener) {

		_httpSessionListeners.remove(httpSessionListener);
	}

	public void attributeAdded(
		HttpSessionBindingEvent httpSessionBindingEvent) {

		httpSessionBindingEvent = getHttpSessionBindingEvent(
			httpSessionBindingEvent);

		for (HttpSessionAttributeListener httpSessionAttributeListener :
				_httpSessionAttributeListeners) {

			httpSessionAttributeListener.attributeAdded(
				httpSessionBindingEvent);
		}
	}

	public void attributeRemoved(
		HttpSessionBindingEvent httpSessionBindingEvent) {

		httpSessionBindingEvent = getHttpSessionBindingEvent(
			httpSessionBindingEvent);

		for (HttpSessionAttributeListener httpSessionAttributeListener :
				_httpSessionAttributeListeners) {

			httpSessionAttributeListener.attributeRemoved(
				httpSessionBindingEvent);
		}
	}

	public void attributeReplaced(
		HttpSessionBindingEvent httpSessionBindingEvent) {

		httpSessionBindingEvent = getHttpSessionBindingEvent(
			httpSessionBindingEvent);

		for (HttpSessionAttributeListener httpSessionAttributeListener :
				_httpSessionAttributeListeners) {

			httpSessionAttributeListener.attributeReplaced(
				httpSessionBindingEvent);
		}
	}

	public void sessionCreated(HttpSessionEvent httpSessionEvent) {
		httpSessionEvent = getHttpSessionEvent(httpSessionEvent);

		Thread currentThread = Thread.currentThread();

		ClassLoader contextClassLoader = currentThread.getContextClassLoader();

		try {
			for (HttpSessionListener httpSessionListener :
					_httpSessionListeners) {

				Class<?> clazz = httpSessionListener.getClass();

				ClassLoader classLoader = clazz.getClassLoader();

				currentThread.setContextClassLoader(classLoader);

				httpSessionListener.sessionCreated(httpSessionEvent);
			}
		}
		finally {
			currentThread.setContextClassLoader(contextClassLoader);
		}
	}

	public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
		httpSessionEvent = getHttpSessionEvent(httpSessionEvent);

		for (HttpSessionListener httpSessionListener : _httpSessionListeners) {
			httpSessionListener.sessionDestroyed(httpSessionEvent);
		}
	}

	public void sessionDidActivate(HttpSessionEvent httpSessionEvent) {
		httpSessionEvent = getHttpSessionEvent(httpSessionEvent);

		for (HttpSessionActivationListener httpSessionActivationListener :
				_httpSessionActivationListeners) {

			httpSessionActivationListener.sessionDidActivate(httpSessionEvent);
		}
	}

	public void sessionWillPassivate(HttpSessionEvent httpSessionEvent) {
		httpSessionEvent = getHttpSessionEvent(httpSessionEvent);

		for (HttpSessionActivationListener httpSessionActivationListener :
				_httpSessionActivationListeners) {

			httpSessionActivationListener.sessionWillPassivate(
				httpSessionEvent);
		}
	}

	public void valueBound(HttpSessionBindingEvent httpSessionBindingEvent) {
		httpSessionBindingEvent = getHttpSessionBindingEvent(
			httpSessionBindingEvent);

		for (HttpSessionBindingListener httpSessionBindingListener :
				_httpSessionBindingListeners) {

			httpSessionBindingListener.valueBound(httpSessionBindingEvent);
		}
	}

	public void valueUnbound(HttpSessionBindingEvent httpSessionBindingEvent) {
		httpSessionBindingEvent = getHttpSessionBindingEvent(
			httpSessionBindingEvent);

		for (HttpSessionBindingListener httpSessionBindingListener :
				_httpSessionBindingListeners) {

			httpSessionBindingListener.valueUnbound(httpSessionBindingEvent);
		}
	}

	protected HttpSessionBindingEvent getHttpSessionBindingEvent(
		HttpSessionBindingEvent httpSessionBindingEvent) {

		if (CompoundSessionIdSplitterUtil.hasSessionDelimiter()) {
			CompoundSessionIdHttpSession compoundSessionIdHttpSession =
				new CompoundSessionIdHttpSession(
					httpSessionBindingEvent.getSession());

			httpSessionBindingEvent = new HttpSessionBindingEvent(
				compoundSessionIdHttpSession, httpSessionBindingEvent.getName(),
				httpSessionBindingEvent.getValue());
		}

		return httpSessionBindingEvent;
	}

	protected HttpSessionEvent getHttpSessionEvent(
		HttpSessionEvent httpSessionEvent) {

		if (CompoundSessionIdSplitterUtil.hasSessionDelimiter()) {
			CompoundSessionIdHttpSession compoundSessionIdHttpSession =
				new CompoundSessionIdHttpSession(httpSessionEvent.getSession());

			httpSessionEvent = new HttpSessionEvent(
				compoundSessionIdHttpSession);
		}

		return httpSessionEvent;
	}

	private static List<HttpSessionActivationListener>
		_httpSessionActivationListeners =
			new ArrayList<HttpSessionActivationListener>();
	private static List<HttpSessionAttributeListener>
		_httpSessionAttributeListeners =
			new ArrayList<HttpSessionAttributeListener>();
	private static List<HttpSessionBindingListener>
		_httpSessionBindingListeners =
			new ArrayList<HttpSessionBindingListener>();
	private static List<HttpSessionListener> _httpSessionListeners =
		new ArrayList<HttpSessionListener>();

}