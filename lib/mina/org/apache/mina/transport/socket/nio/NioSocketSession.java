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
 *
 */
package org.apache.mina.transport.socket.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.AbstractSocketSessionConfig;
import org.apache.mina.transport.socket.SocketSessionConfig;

/**
 * An {@link IoSession} for socket transport (TCP/IP).
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public final class NioSocketSession extends NioSession {
	/**
	 * Creates a new instance of NioSocketSession.
	 *
	 * @param service the associated IoService
	 * @param processor the associated IoProcessor
	 * @param channel the used channel
	 */
	public NioSocketSession(IoService service, IoProcessor<NioSession> processor, SocketChannel channel) {
		super(processor, service, channel);
		config = new SessionConfigImpl();
		config.setAll(service.getSessionConfig());
	}

	private Socket getSocket() {
		return ((SocketChannel) channel).socket();
	}

	@Override
	public SocketSessionConfig getConfig() {
		return (SocketSessionConfig) config;
	}

	@Override
	SocketChannel getChannel() {
		return (SocketChannel) channel;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return channel != null ? (InetSocketAddress) getSocket().getLocalSocketAddress() : null;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return channel != null ? (InetSocketAddress) getSocket().getRemoteSocketAddress() : null;
	}

	protected static void destroy(NioSession session) throws IOException {
		@SuppressWarnings("resource")
		ByteChannel ch = session.getChannel();
		SelectionKey key = session.getSelectionKey();
		if (key != null) {
			key.cancel();
		}
		ch.close();
	}

	/**
	 * A private class storing a copy of the IoService configuration when the IoSession is created.
	 * That allows the session to have its own configuration setting, over the IoService default one.
	 */
	private final class SessionConfigImpl extends AbstractSocketSessionConfig {
		@Override
		public boolean isKeepAlive() {
			try {
				return getSocket().getKeepAlive();
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void setKeepAlive(boolean on) {
			try {
				getSocket().setKeepAlive(on);
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean isOobInline() {
			try {
				return getSocket().getOOBInline();
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void setOobInline(boolean on) {
			try {
				getSocket().setOOBInline(on);
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean isReuseAddress() {
			try {
				return getSocket().getReuseAddress();
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void setReuseAddress(boolean on) {
			try {
				getSocket().setReuseAddress(on);
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public int getSoLinger() {
			try {
				return getSocket().getSoLinger();
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void setSoLinger(int linger) {
			try {
				if (linger < 0) {
					getSocket().setSoLinger(false, 0);
				} else {
					getSocket().setSoLinger(true, linger);
				}
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean isTcpNoDelay() {
			if (!isConnected()) {
				return false;
			}

			try {
				return getSocket().getTcpNoDelay();
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void setTcpNoDelay(boolean on) {
			try {
				getSocket().setTcpNoDelay(on);
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public int getTrafficClass() {
			try {
				return getSocket().getTrafficClass();
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void setTrafficClass(int tc) {
			try {
				getSocket().setTrafficClass(tc);
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public int getSendBufferSize() {
			try {
				return getSocket().getSendBufferSize();
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void setSendBufferSize(int size) {
			try {
				getSocket().setSendBufferSize(size);
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public int getReceiveBufferSize() {
			try {
				return getSocket().getReceiveBufferSize();
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void setReceiveBufferSize(int size) {
			try {
				getSocket().setReceiveBufferSize(size);
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
