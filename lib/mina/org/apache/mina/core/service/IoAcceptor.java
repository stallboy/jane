/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.mina.core.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Accepts incoming connection, communicates with clients, and fires events to {@link IoHandler}s.
 * <p>
 * You should bind to the desired socket address to accept incoming connections,
 * and then events for incoming connections will be sent to the specified default {@link IoHandler}.
 * <p>
 * Threads accept incoming connections start automatically when {@link #bind(SocketAddress)} is invoked,
 * and stop when {@link #unbind()} is invoked.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public interface IoAcceptor extends IoService {
	/**
	 * Returns the local address which is bound currently.
	 * If more than one address are bound, only one of them will be returned,
	 * but it's not necessarily the firstly bound address.
	 *
	 * @return The bound LocalAddress
	 */
	InetSocketAddress getLocalAddress();

	/**
	 * Returns a {@link Set} of the local addresses which are bound currently.
	 *
	 * @return The Set of bound LocalAddresses
	 */
	ArrayList<InetSocketAddress> getLocalAddresses();

	/**
	 * Returns <tt>true</tt> if and only if all clients are closed when this
	 * acceptor unbinds from all the related local address (i.e. when the service is deactivated).
	 *
	 * @return <tt>true</tt> if the service sets the closeOnDeactivation flag
	 */
	boolean isCloseOnDeactivation();

	/**
	 * Sets whether all client sessions are closed when this acceptor unbinds from all the related
	 * local addresses (i.e. when the service is deactivated). The default value is <tt>true</tt>.
	 *
	 * @param closeOnDeactivation <tt>true</tt> if we should close on deactivation
	 */
	void setCloseOnDeactivation(boolean closeOnDeactivation);

	/**
	 * Binds to the specified local address and start to accept incoming connections.
	 *
	 * @param localAddress The SocketAddress to bind to
	 *
	 * @throws IOException if failed to bind
	 */
	void bind(SocketAddress localAddress) throws IOException;

	/**
	 * Binds to the specified local addresses and start to accept incoming connections.
	 *
	 * @param localAddresses The local address we will be bound to
	 * @throws IOException if failed to bind
	 */
	void bind(List<? extends SocketAddress> localAddresses) throws IOException;

	/**
	 * Unbinds from all local addresses that this service is bound to and stops to accept incoming connections.
	 * All managed connections will be closed if {@link #setCloseOnDeactivation(boolean) disconnectOnUnbind}
	 * property is <tt>true</tt>. This method returns silently if no local address is bound yet.
	 */
	void unbind();

	/**
	 * Unbinds from the specified local address and stop to accept incoming connections.
	 * All managed connections will be closed if {@link #setCloseOnDeactivation(boolean) disconnectOnUnbind}
	 * property is <tt>true</tt>. This method returns silently if the default local address is not bound yet.
	 *
	 * @param localAddress The local address we will be unbound from
	 */
	void unbind(SocketAddress localAddress);

	/**
	 * Unbinds from the specified local addresses and stop to accept incoming connections.
	 * All managed connections will be closed if {@link #setCloseOnDeactivation(boolean) disconnectOnUnbind}
	 * property is <tt>true</tt>. This method returns silently if the default local addresses are not bound yet.
	 *
	 * @param localAddresses The local address we will be unbound from
	 */
	void unbind(List<? extends SocketAddress> localAddresses);
}
