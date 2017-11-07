package com.dimogo.open.myjobs.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ethan Xiao on 2017/4/5.
 */
public class LocalUtils {
	private static final Logger logger = Logger.getLogger(LocalUtils.class);
	public static final String ip = getLocalIp();

	/**
	 * 获取本地IP
	 * @return
	 */
	private static String getLocalIp() {
		//查询当前操作系统
		Properties p = System.getProperties();
		String osName = p.getProperty("os.name");

		try {
			//如果是windows
			if (osName.matches("(?i).*win.*")) {
				return InetAddress.getLocalHost().getHostAddress().toString();//获得本机IP
			} else {
				//根据"systeminfo"命令查询开机时间
				Process process = Runtime.getRuntime().exec("ifconfig");
				InputStream inputStream = process.getInputStream();
				String s = "";
				byte[] b = new byte[1024];
				while (inputStream.read(b) != -1) {
					s = s + new String(b);
				}
				if (!StringUtils.isBlank(s)) {
					Matcher m = Pattern.compile("inet addr:(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})").matcher(s);
					if (m.find()) {
						process.destroy();
						return m.group(1);
					}
				}
			}
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		}
		return "127.0.0.1";
	}
}
