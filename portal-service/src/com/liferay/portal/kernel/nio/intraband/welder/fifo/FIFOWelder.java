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

package com.liferay.portal.kernel.nio.intraband.welder.fifo;

import com.liferay.portal.kernel.nio.intraband.Intraband;
import com.liferay.portal.kernel.nio.intraband.RegistrationReference;
import com.liferay.portal.kernel.nio.intraband.welder.BaseWelder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.channels.FileChannel;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Shuyang Zhou
 */
public class FIFOWelder extends BaseWelder {

	public FIFOWelder() throws IOException {
		String tempFolderName = System.getProperty("java.io.tmpdir");

		long id = idCounter.getAndIncrement();

		inputFIFOFile = new File(tempFolderName, "FIFO-INPUT-" + id);
		outputFIFOFile = new File(tempFolderName, "FIFO-OUTPUT-" + id);

		try {
			FIFOUtil.createFIFO(inputFIFOFile);
			FIFOUtil.createFIFO(outputFIFOFile);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	protected void doDestroy() throws IOException {
		readFileChannel.close();
		writeFileChannel.close();
	}

	@Override
	protected RegistrationReference weldClient(Intraband intraBand)
		throws IOException {

		// Write, then read

		FileOutputStream fileOutputStream = new FileOutputStream(inputFIFOFile);

		writeFileChannel = fileOutputStream.getChannel();

		FileInputStream fileInputStream = new AutoRemoveFileInputStream(
			outputFIFOFile);

		readFileChannel = fileInputStream.getChannel();

		return intraBand.registerChannel(readFileChannel, writeFileChannel);
	}

	@Override
	protected RegistrationReference weldServer(Intraband intraBand)
		throws IOException {

		// Read, then write

		FileInputStream fileInputStream = new AutoRemoveFileInputStream(
			inputFIFOFile);

		readFileChannel = fileInputStream.getChannel();

		FileOutputStream fileOutputStream = new FileOutputStream(
			outputFIFOFile);

		writeFileChannel = fileOutputStream.getChannel();

		return intraBand.registerChannel(readFileChannel, writeFileChannel);
	}

	protected static final AtomicLong idCounter = new AtomicLong(
		System.currentTimeMillis());

	protected final File inputFIFOFile;
	protected final File outputFIFOFile;
	protected transient FileChannel readFileChannel;
	protected transient FileChannel writeFileChannel;

	protected static class AutoRemoveFileInputStream extends FileInputStream {

		public AutoRemoveFileInputStream(File file)
			throws FileNotFoundException {

			super(file);

			_file = file;
		}

		@Override
		public void close() throws IOException {
			super.close();

			if (!_file.delete()) {
				_file.deleteOnExit();
			}
		}

		private final File _file;

	}

}