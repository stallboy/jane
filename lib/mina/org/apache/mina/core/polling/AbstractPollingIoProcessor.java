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
package org.apache.mina.core.polling;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.file.FileRegion;
import org.apache.mina.core.future.DefaultIoFuture;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.SessionState;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.core.write.WriteToClosedSessionException;
import org.apache.mina.util.ExceptionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract implementation of {@link IoProcessor} which helps transport
 * developers to write an {@link IoProcessor} easily. This class is in charge of
 * active polling a set of {@link IoSession} and trigger events when some I/O
 * operation is possible.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 *
 * @param <S>
 *            the type of the {@link IoSession} this processor can handle
 */
public abstract class AbstractPollingIoProcessor<S extends AbstractIoSession> implements IoProcessor<S> {
	/** A logger for this class */
	private static final Logger LOGGER = LoggerFactory.getLogger(IoProcessor.class);

	/** A timeout used for the select */
	private static final long SELECT_TIMEOUT = 1000L;

	/** The executor to use when we need to start the inner Processor */
	private final Executor executor;

	/** A Session queue containing the newly created sessions */
	private final Queue<S> newSessions = new ConcurrentLinkedQueue<>();

	/** A queue used to store the sessions to be removed */
	private final Queue<S> removingSessions = new ConcurrentLinkedQueue<>();

	/** A queue used to store the sessions to be flushed */
	private final Queue<S> flushingSessions = new ConcurrentLinkedQueue<>();

	/** A queue used to store the sessions which have a trafficControl to be updated */
	private final Queue<S> trafficControllingSessions = new ConcurrentLinkedQueue<>();

	/** The processor thread: it handles the incoming messages */
	private final AtomicReference<Processor> processorRef = new AtomicReference<>();

	private final Object disposalLock = new Object();

	private final DefaultIoFuture disposalFuture = new DefaultIoFuture(null);

	protected final AtomicBoolean wakeupCalled = new AtomicBoolean();

	private volatile boolean disposing;
	private volatile boolean disposed;

	/**
	 * Create an {@link AbstractPollingIoProcessor} with the given
	 * {@link Executor} for handling I/Os events.
	 *
	 * @param executor the {@link Executor} for handling I/O events
	 */
	protected AbstractPollingIoProcessor(Executor executor) {
		if (executor == null) {
			throw new IllegalArgumentException("executor");
		}

		this.executor = executor;
	}

	/**
	 * Initialize the polling of a session. Add it to the polling process.
	 *
	 * @param session the {@link IoSession} to add to the polling
	 * @throws IOException any exception thrown by the underlying system calls
	 */
	protected abstract void init(S session) throws IOException;

	/**
	 * Destroy the underlying client socket handle
	 *
	 * @param session the {@link IoSession}
	 * @throws IOException any exception thrown by the underlying system calls
	 */
	protected abstract void destroy(S session) throws IOException;

	/**
	 * Dispose the resources used by this {@link IoProcessor} for polling the
	 * client connections. The implementing class doDispose method will be called.
	 *
	 * @throws IOException if some low level IO error occurs
	 */
	protected abstract void doDispose() throws IOException;

	/**
	 * In the case we are using the java select() method, this method is used to
	 * trash the buggy selector and create a new one, registring all the sockets on it.
	 *
	 * @throws IOException If we got an exception
	 */
	protected abstract void registerNewSelector() throws IOException;

	/**
	 * Check that the select() has not exited immediately just because of a
	 * broken connection. In this case, this is a standard case, and we just have to loop.
	 *
	 * @return <tt>true</tt> if a connection has been brutally closed.
	 * @throws IOException If we got an exception
	 */
	protected abstract boolean isBrokenConnection() throws IOException;

	/**
	 * poll those sessions for the given timeout
	 *
	 * @param timeout milliseconds before the call timeout if no event appear
	 * @return The number of session ready for read or for write
	 * @throws IOException if some low level IO error occurs
	 */
	protected abstract int select(long timeout) throws IOException;

	/**
	 * Say if the list of {@link IoSession} polled by this {@link IoProcessor} is empty
	 *
	 * @return <tt>true</tt> if at least a session is managed by this {@link IoProcessor}
	 */
	protected abstract boolean isSelectorEmpty();

	/**
	 * Interrupt the {@link #select(long)} call.
	 */
	protected abstract void wakeup();

	/**
	 * Get an {@link Iterator} for the list of {@link IoSession} polled by this
	 * {@link IoProcessor}
	 *
	 * @return {@link Iterator} of {@link IoSession}
	 */
	protected abstract Iterator<S> allSessions();

	/**
	 * Get an {@link Iterator} for the list of {@link IoSession} found selected
	 * by the last call of {@link #select(long)}
	 *
	 * @return {@link Iterator} of {@link IoSession} read for I/Os operation
	 */
	protected abstract Iterator<S> selectedSessions();

	/**
	 * Get the state of a session (One of OPENING, OPEN, CLOSING)
	 *
	 * @param session the {@link IoSession} to inspect
	 * @return the state of the session
	 */
	protected abstract SessionState getState(S session);

	/**
	 * Tells if the session ready for reading
	 *
	 * @param session the queried session
	 * @return <tt>true</tt> is ready, <tt>false</tt> if not ready
	 */
	protected abstract boolean isReadable(S session);

	/**
	 * Tells if the session ready for writing
	 *
	 * @param session the queried session
	 * @return <tt>true</tt> is ready, <tt>false</tt> if not ready
	 */
	protected abstract boolean isWritable(S session);

	/**
	 * Tells if this session is registered for reading
	 *
	 * @param session the queried session
	 * @return <tt>true</tt> is registered for reading
	 */
	protected abstract boolean isInterestedInRead(S session);

	/**
	 * Tells if this session is registered for writing
	 *
	 * @param session the queried session
	 * @return <tt>true</tt> is registered for writing
	 */
	protected abstract boolean isInterestedInWrite(S session);

	/**
	 * Set the session to be informed when a read event should be processed
	 *
	 * @param session the session for which we want to be interested in read events
	 * @param isInterested <tt>true</tt> for registering, <tt>false</tt> for removing
	 * @throws Exception If there was a problem while registering the session
	 */
	protected abstract void setInterestedInRead(S session, boolean isInterested);

	/**
	 * Set the session to be informed when a write event should be processed
	 *
	 * @param session the session for which we want to be interested in write events
	 * @param isInterested <tt>true</tt> for registering, <tt>false</tt> for removing
	 * @throws Exception If there was a problem while registering the session
	 */
	protected abstract void setInterestedInWrite(S session, boolean isInterested);

	/**
	 * Reads a sequence of bytes from a {@link IoSession} into the given
	 * {@link IoBuffer}. Is called when the session was found ready for reading.
	 *
	 * @param session the session to read
	 * @param buf the buffer to fill
	 * @return the number of bytes read
	 * @throws IOException any exception thrown by the underlying system calls
	 */
	protected abstract int read(S session, IoBuffer buf) throws IOException;

	/**
	 * Write a sequence of bytes to a {@link IoSession}, means to be called when
	 * a session was found ready for writing.
	 *
	 * @param session the session to write
	 * @param buf the buffer to write
	 * @param length the number of bytes to write can be superior to the number of
	 *            bytes remaining in the buffer
	 * @return the number of byte written
	 * @throws IOException any exception thrown by the underlying system calls
	 */
	protected abstract int write(S session, IoBuffer buf, int length) throws IOException;

	/**
	 * Write a part of a file to a {@link IoSession}, if the underlying API
	 * isn't supporting system calls like sendfile(), you can throw a
	 * {@link UnsupportedOperationException} so the file will be send using
	 * usual {@link #write(AbstractIoSession, IoBuffer, int)} call.
	 *
	 * @param session the session to write
	 * @param region the file region to write
	 * @param length the length of the portion to send
	 * @return the number of written bytes
	 * @throws IOException any exception thrown by the underlying system calls
	 */
	protected abstract int transferFile(S session, FileRegion region, int length) throws IOException;

	@Override
	public final void add(S session) {
		if (disposed || disposing) {
			throw new IllegalStateException("Already disposed.");
		}

		// Adds the session to the newSession queue and starts the worker
		newSessions.add(session);
		startupProcessor();
	}

	@Override
	public final void remove(S session) {
		scheduleRemove(session);
		startupProcessor();
	}

	private void scheduleRemove(S session) {
		if (!removingSessions.contains(session)) {
			removingSessions.add(session);
		}
	}

	@Override
	public void write(S session, WriteRequest writeRequest) {
		session.getWriteRequestQueue().offer(writeRequest);

		if (!session.isWriteSuspended()) {
			flush(session);
		}
	}

	@Override
	public final void flush(S session) {
		// add the session to the queue if it's not already
		// in the queue, then wake up the select()
		if (session.setScheduledForFlush(true)) {
			flushingSessions.add(session);
			wakeup();
		}
	}

	@Override
	public void updateTrafficControl(S session) {
		try {
			setInterestedInRead(session, !session.isReadSuspended());
		} catch (Exception e) {
			session.getFilterChain().fireExceptionCaught(e);
		}

		try {
			setInterestedInWrite(session, !session.getWriteRequestQueue().isEmpty() && !session.isWriteSuspended());
		} catch (Exception e) {
			session.getFilterChain().fireExceptionCaught(e);
		}
	}

	@Override
	public final boolean isDisposing() {
		return disposing;
	}

	@Override
	public final boolean isDisposed() {
		return disposed;
	}

	@Override
	public final void dispose() {
		if (disposed || disposing) {
			return;
		}

		synchronized (disposalLock) {
			disposing = true;
			startupProcessor();
		}

		disposalFuture.awaitUninterruptibly();
		disposed = true;
	}

	private void read(S session) {
		try {
			int readBufferSize = session.getConfig().getReadBufferSize();
			IoBuffer buf = IoBuffer.allocate(readBufferSize);
			int readBytes = read(session, buf);

			if (readBytes > 0) {
				if ((readBytes << 1) < readBufferSize) {
					session.decreaseReadBufferSize();
				} else if (readBytes >= readBufferSize) {
					session.increaseReadBufferSize();
				}
				session.getFilterChain().fireMessageReceived(buf.flip());
			} else {
				// release temporary buffer when read nothing
				buf.free();
				if (readBytes < 0) {
					session.getFilterChain().fireInputClosed();
				}
			}
		} catch (IOException e) {
			scheduleRemove(session);
			session.getFilterChain().fireExceptionCaught(e);
		} catch (Exception e) {
			session.getFilterChain().fireExceptionCaught(e);
		}
	}

	/**
	 * Starts the inner Processor, asking the executor to pick a thread in its pool.
	 * The Runnable will be renamed
	 */
	private void startupProcessor() {
		Processor processor = processorRef.get();

		if (processor == null) {
			processor = new Processor();

			if (processorRef.compareAndSet(null, processor)) {
				executor.execute(processor);
			}
		}

		// Just stop the select() and start it again, so that the processor
		// can be activated immediately.
		wakeup();
	}

	/**
	 * The main loop. This is the place in charge to poll the Selector, and to
	 * process the active sessions. It's done in - handle the newly created sessions -
	 */
	private final class Processor implements Runnable {
		@Override
		public void run() {
			int nSessions = 0;
			int nbTries = 10;

			for (;;) {
				try {
					// This select has a timeout so that we can manage idle session
					// when we get out of the select every second.
					// (note: this is a hack to avoid creating a dedicated thread).
					long t0 = System.currentTimeMillis();
					int selected = select(SELECT_TIMEOUT);

					long delta;
					if (!wakeupCalled.getAndSet(false) && selected == 0 && (delta = System.currentTimeMillis() - t0) < 100) {
						if (Thread.interrupted()) {
							// Thread was interrupted so reset selected keys and break so we not run into a busy loop.
							// As this is most likely a bug in the handler of the user or it's client library we will also log it.
							// See https://github.com/netty/netty/issues/2426
							LOGGER.error("Selector.select() returned prematurely because Thread.interrupted()");
							break;
						}

						// Last chance: the select() may have been
						// interrupted because we have had an closed channel.
						if (isBrokenConnection()) {
							LOGGER.warn("Broken connection");
						} else {
							// Ok, we are hit by the nasty epoll spinning.
							// Basically, there is a race condition
							// which causes a closing file descriptor not to be
							// considered as available as a selected channel,
							// but it stopped the select. The next time we will
							// call select(), it will exit immediately for the
							// same reason, and do so forever, consuming 100% CPU.
							// We have to destroy the selector, and
							// register all the socket on a new one.
							if (nbTries == 0) {
								LOGGER.warn("Create a new selector. Selected is 0, delta = " + delta);
								registerNewSelector();
								nbTries = 10;
							} else {
								nbTries--;
							}
						}
					} else {
						nbTries = 10;
					}

					// Manage newly created session first
					nSessions += handleNewSessions();

					updateTrafficMask();

					// Now, if we have had some incoming or outgoing events, deal with them
					if (selected > 0) {
						process();
					}

					// Write the pending requests
					flush();

					// And manage removed sessions
					nSessions -= removeSessions();

					// Get a chance to exit the infinite loop if there are no more sessions on this Processor
					if (nSessions == 0) {
						processorRef.set(null);

						if (newSessions.isEmpty() && isSelectorEmpty()) {
							// newSessions.add() precedes startupProcessor
							break;
						}

						if (!processorRef.compareAndSet(null, this)) {
							// startupProcessor won race, so must exit processor
							break;
						}
					}

					// Disconnect all sessions immediately if disposal has been
					// requested so that we exit this loop eventually.
					if (isDisposing()) {
						boolean hasKeys = false;

						for (Iterator<S> i = allSessions(); i.hasNext();) {
							S session = i.next();

							if (session.isActive()) {
								scheduleRemove(session);
								hasKeys = true;
							}
						}

						if (hasKeys) {
							wakeup();
						}
					}
				} catch (ClosedSelectorException cse) {
					// If the selector has been closed, we can exit the loop But first, dump a stack trace
					ExceptionMonitor.getInstance().exceptionCaught(cse);
					break;
				} catch (Exception e) {
					ExceptionMonitor.getInstance().exceptionCaught(e);

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						ExceptionMonitor.getInstance().exceptionCaught(e1);
					}
				}
			}

			try {
				synchronized (disposalLock) {
					if (disposing) {
						doDispose();
					}
				}
			} catch (Exception e) {
				ExceptionMonitor.getInstance().exceptionCaught(e);
			} finally {
				disposalFuture.setValue(true);
			}
		}

		/**
		 * Loops over the new sessions blocking queue and returns the number of
		 * sessions which are effectively created
		 *
		 * @return The number of new sessions
		 */
		private int handleNewSessions() {
			int addedSessions = 0;

			for (S session = newSessions.poll(); session != null; session = newSessions.poll()) {
				if (addNow(session)) {
					// A new session has been created
					addedSessions++;
				}
			}

			return addedSessions;
		}

		/**
		 * Update the trafficControl for all the session.
		 */
		private void updateTrafficMask() {
			int queueSize = trafficControllingSessions.size();

			while (queueSize > 0) {
				S session = trafficControllingSessions.poll();

				if (session == null) {
					// We are done with this queue.
					return;
				}

				SessionState state = getState(session);

				switch (state) {
					case OPENED:
						updateTrafficControl(session);
						break;

					case CLOSING:
						break;

					case OPENING:
						// Retry later if session is not yet fully initialized.
						// (In case that Session.suspend??() or session.resume??() is
						// called before addSession() is processed)
						// We just put back the session at the end of the queue.
						trafficControllingSessions.add(session);
						break;

					default:
						throw new IllegalStateException(String.valueOf(state));
				}

				// As we have handled one session, decrement the number of
				// remaining sessions. The OPENING session will be processed
				// with the next select(), as the queue size has been decreased,
				// even if the session has been pushed at the end of the queue
				queueSize--;
			}
		}

		/**
		 * Process a new session: - initialize it - create its chain - fire the CREATED listeners if any
		 *
		 * @param session The session to create
		 * @return <tt>true</tt> if the session has been registered
		 */
		private boolean addNow(S session) {
			boolean registered = false;

			try {
				init(session);
				registered = true;

				// Build the filter chain of this session.
				session.getService().getFilterChainBuilder().buildFilterChain(session.getFilterChain());

				// DefaultIoFilterChain.CONNECT_FUTURE is cleared inside here
				// in AbstractIoFilterChain.fireSessionOpened().
				// Propagate the SESSION_CREATED event up to the chain
				((AbstractIoService) session.getService()).getListeners().fireSessionCreated(session);
			} catch (Exception e) {
				ExceptionMonitor.getInstance().exceptionCaught(e);

				try {
					destroy(session);
				} catch (Exception e1) {
					ExceptionMonitor.getInstance().exceptionCaught(e1);
				} finally {
					registered = false;
				}
			}

			return registered;
		}

		private int removeSessions() {
			int removedSessions = 0;

			for (S session = removingSessions.poll(); session != null; session = removingSessions.poll()) {
				SessionState state = getState(session);

				// Now deal with the removal accordingly to the session's state
				switch (state) {
					case OPENED:
						// Try to remove this session
						if (removeNow(session)) {
							removedSessions++;
						}

						break;

					case CLOSING:
						// Skip if channel is already closed In any case, remove the session from the queue
						removedSessions++;
						break;

					case OPENING:
						// Remove session from the newSessions queue and remove it
						newSessions.remove(session);

						if (removeNow(session)) {
							removedSessions++;
						}

						break;

					default:
						throw new IllegalStateException(String.valueOf(state));
				}
			}

			return removedSessions;
		}

		/**
		 * Write all the pending messages
		 */
		private void flush() {
			for(;;) {
				S session = flushingSessions.poll(); // the same one with firstSession

				if (session == null) {
					break;
				}

				// Reset the Schedule for flush flag for this session, as we are flushing it now
				session.unscheduledForFlush();

				SessionState state = getState(session);

				switch (state) {
					case OPENED:
						try {
							if (flushNow(session) && !session.getWriteRequestQueue().isEmpty()
									&& !session.isScheduledForFlush()) {
								scheduleFlush(session);
							}
						} catch (Exception e) {
							scheduleRemove(session);
							session.closeNow();
							session.getFilterChain().fireExceptionCaught(e);
						}

						break;

					case CLOSING:
						// Skip if the channel is already closed.
						break;

					case OPENING:
						// Retry later if session is not yet fully initialized.
						// (In case that Session.write() is called before addSession() is processed)
						scheduleFlush(session);
						return;

					default:
						throw new IllegalStateException(String.valueOf(state));
				}
			}
		}

		private boolean flushNow(S session) {
			if (!session.isConnected()) {
				scheduleRemove(session);
				return false;
			}

			final WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();

			// Set limitation for the number of written bytes for read-write
			// fairness. I used maxReadBufferSize * 3 / 2, which yields best
			// performance in my experience while not breaking fairness much.
			final int maxWrittenBytes = session.getConfig().getMaxReadBufferSize()
					+ (session.getConfig().getMaxReadBufferSize() >>> 1);
			int writtenBytes = 0;
			WriteRequest req = null;

			try {
				do {
					// Check for pending writes.
					req = session.getCurrentWriteRequest();

					if (req == null) {
						req = writeRequestQueue.poll();

						if (req == null) {
							break;
						}

						session.setCurrentWriteRequest(req);
					}

					int localWrittenBytes;
					Object message = req.getMessage();

					if (message instanceof IoBuffer) {
						localWrittenBytes = writeBuffer(session, req, maxWrittenBytes - writtenBytes);

						if ((localWrittenBytes > 0) && ((IoBuffer) message).hasRemaining()) {
							// the buffer isn't empty, we re-interest it in writing
							setInterestedInWrite(session, true);
							return false;
						}
					} else if (message instanceof FileRegion) {
						localWrittenBytes = writeFile(session, req, maxWrittenBytes - writtenBytes);

						// Fix for Java bug on Linux
						// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5103988
						// If there's still data to be written in the FileRegion,
						// return 0 indicating that we need
						// to pause until writing may resume.
						if ((localWrittenBytes > 0) && (((FileRegion) message).getRemainingBytes() > 0)) {
							setInterestedInWrite(session, true);
							return false;
						}
					} else {
						throw new IllegalStateException("unknown message type for writting: "
								+ message.getClass().getName() + ": " + message);
					}

					if (localWrittenBytes == 0) {
						// Kernel buffer is full.
						setInterestedInWrite(session, true);
						return false;
					}

					writtenBytes += localWrittenBytes;
					if (writtenBytes >= maxWrittenBytes) {
						// Wrote too much
						scheduleFlush(session);
						setInterestedInWrite(session, false);
						return false;
					}

					if (message instanceof IoBuffer) {
						((IoBuffer) message).free();
					}
				} while (writtenBytes < maxWrittenBytes);

				setInterestedInWrite(session, false);
				return true;
			} catch (Exception e) {
				try {
					setInterestedInWrite(session, false);
				} catch(Exception ex) {
					session.getFilterChain().fireExceptionCaught(ex);
				}
				if (req != null) {
					req.getFuture().setException(e);
				}

				session.getFilterChain().fireExceptionCaught(e);
				return false;
			}
		}

		private void scheduleFlush(S session) {
			// add the session to the queue if it's not already in the queue
			if (session.setScheduledForFlush(true)) {
				flushingSessions.add(session);
			}
		}

		private int writeFile(S session, WriteRequest req, int maxLength) throws Exception {
			int localWrittenBytes;
			FileRegion region = (FileRegion) req.getMessage();

			if (region.getRemainingBytes() > 0) {
				int length = (int) Math.min(region.getRemainingBytes(), maxLength);
				localWrittenBytes = transferFile(session, region, length);
				region.update(localWrittenBytes);
			} else {
				localWrittenBytes = 0;
			}

			if (region.getRemainingBytes() <= 0) {
				fireMessageSent(session, req);
			}

			return localWrittenBytes;
		}

		private int writeBuffer(S session, WriteRequest req, int maxLength) throws Exception {
			IoBuffer buf = (IoBuffer) req.getMessage();
			int localWrittenBytes = 0;

			if (buf.hasRemaining()) {
				int length = Math.min(buf.remaining(), maxLength);

				try {
					localWrittenBytes = write(session, buf, length);
				} catch (IOException ioe) {
					// We have had an issue while trying to send data to the peer:
					// let's close the session.
					buf.free();
					session.closeNow();
					removeNow(session);

					return 0;
				}
			}

			// Now, forward the original message
			if (!buf.hasRemaining()) {
				// Buffer has been sent, clear the current request.
				fireMessageSent(session, req);
			}

			return localWrittenBytes;
		}

		private boolean removeNow(S session) {
			clearWriteRequestQueue(session);

			try {
				destroy(session);
				return true;
			} catch (Exception e) {
				session.getFilterChain().fireExceptionCaught(e);
			} finally {
				try {
					clearWriteRequestQueue(session);
					((AbstractIoService) session.getService()).getListeners().fireSessionDestroyed(session);
				} catch (Exception e) {
					// The session was either destroyed or not at this point.
					// We do not want any exception thrown from this "cleanup" code
					// to change the return value by bubbling up.
					session.getFilterChain().fireExceptionCaught(e);
				}
			}

			return false;
		}

		private void clearWriteRequestQueue(S session) {
			WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();
			WriteRequest req;

			List<WriteRequest> failedRequests = new ArrayList<>();

			if ((req = writeRequestQueue.poll()) != null) {
				Object message = req.getMessage();

				if (message instanceof IoBuffer) {
					IoBuffer buf = (IoBuffer) message;

					// The first unwritten empty buffer must be forwarded to the filter chain.
					if (buf.hasRemaining()) {
						failedRequests.add(req);
					} else {
						session.getFilterChain().fireMessageSent(req);
					}
				} else {
					failedRequests.add(req);
				}

				// Discard others.
				while ((req = writeRequestQueue.poll()) != null) {
					failedRequests.add(req);
				}
			}

			// Create an exception and notify.
			if (!failedRequests.isEmpty()) {
				WriteToClosedSessionException cause = new WriteToClosedSessionException(failedRequests);

				for (WriteRequest r : failedRequests) {
					r.getFuture().setException(cause);
				}

				session.getFilterChain().fireExceptionCaught(cause);
			}
		}

		private void fireMessageSent(S session, WriteRequest req) {
			session.setCurrentWriteRequest(null);
			session.getFilterChain().fireMessageSent(req);
		}

		private void process() throws Exception {
			for (Iterator<S> i = selectedSessions(); i.hasNext();) {
				process(i.next());
				i.remove();
			}
		}

		/**
		 * Deal with session ready for the read or write operations, or both.
		 */
		private void process(S session) {
			// Process Reads
			if (isReadable(session) && !session.isReadSuspended()) {
				read(session);
			}

			// Process writes
			if (isWritable(session) && !session.isWriteSuspended() && session.setScheduledForFlush(true)) {
				// add the session to the queue, if it's not already there
				flushingSessions.add(session);
			}
		}
	}
}
