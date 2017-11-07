package com.dimogo.open.myjobs.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by ethanx on 2017/11/6.
 */
public class GZIPUtils {

	private static final String GZIP_ENCODE_UTF_8 = "UTF-8";

	private static final String GZIP_ENCODE_ISO_8859_1 = "ISO-8859-1";

	/**
	 * GZIP数据压缩为字节数组
	 * @param bytes
	 * @return
	 * @throws IOException
	 */
	public static byte[] compress(byte[] bytes) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gz = new GZIPOutputStream(out);
		gz.write(bytes);
		gz.close();//压缩数据时必须先将流关闭，标志结束后再取压缩后的数据
		return out.toByteArray();
	}

	/**
	 * GZIP字符串按UTF-8压缩为字节数组
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static byte[] compress(String data) throws IOException {
		return compress(data, GZIP_ENCODE_UTF_8);
	}

	/**
	 * GZIP字符串按指定字符集压缩为字节数组
	 * @param data
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static byte[] compress(String data, String encoding) throws IOException {
		if (StringUtils.isBlank(data)) {
			return null;
		}
		return compress(data.getBytes(encoding));
	}

	/**
	 * GZIP解压缩
	 * @param bytes
	 * @return
	 * @throws IOException
	 */
	public static byte[] uncompress(byte[] bytes) throws IOException {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		GZIPInputStream ungz = null;
		try {
			byte[] buffer = new byte[256];
			ungz = new GZIPInputStream(in, buffer.length);
			int n;
			while ((n = ungz.read(buffer)) >= 0) {
				out.write(buffer, 0, n);
			}
			return out.toByteArray();
		} finally {
			if (ungz != null) {
				ungz.close();
			}
		}
	}

	/**
	 * GZIP解压缩为UTF-8编码的字符串
	 * @param bytes
	 * @return
	 * @throws IOException
	 */
	public static String uncompressAsString(byte[] bytes) throws IOException {
		return uncompressAsString(bytes, GZIP_ENCODE_UTF_8);
	}

	/**
	 * GZIP解压缩为指定字符集编码的字符串
	 * @param bytes
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String uncompressAsString(byte[] bytes, String encoding) throws IOException {
		return new String(uncompress(bytes), encoding);
	}
}