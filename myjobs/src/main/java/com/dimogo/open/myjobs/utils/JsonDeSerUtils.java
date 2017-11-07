package com.dimogo.open.myjobs.utils;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Created by ethanx on 2017/11/6.
 */
public class JsonDeSerUtils {

	private static Logger logger = Logger.getLogger(JsonDeSerUtils.class);

	public static byte[] serialize(Object object) {
		try {
			return GZIPUtils.compress(JSON.toJSONString(object));
		} catch (Throwable e) {
			logger.error("serialize json byte error", e);
		}
		return null;
	}

	public static <T> T deserializeAsClass(byte[] bytes, Class<T> clz) {
		String json = deserializeAsJsonString(bytes);
		if (StringUtils.isBlank(json)) {
			return null;
		}
		return JSON.parseObject(json, clz);
	}

	public static String deserializeAsJsonString(byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		try {
			return GZIPUtils.uncompressAsString(bytes);
		} catch (Throwable e) {
			logger.error("deserialize json bytes error", e);
		}
		return null;
	}
}
