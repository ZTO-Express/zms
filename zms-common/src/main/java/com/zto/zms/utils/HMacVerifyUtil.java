/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zto.zms.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static com.zto.zms.common.ZmsException.SIGNATURE_VERIFICATION_EXCEPTION;

/**
 * <p>Class: HMacVerifyUtil</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/4/1
 **/
public class HMacVerifyUtil {

	/**
	 * 验证签名
	 *
	 * @param sign
	 * @param dto
	 * @param token
	 * @return
	 */
	public static boolean verifySignature(String sign, Object dto, String token) {
		Map<String, String> bodyMap;
		try {
			bodyMap = objectToMap(dto);
		} catch (IllegalAccessException e) {
			throw SIGNATURE_VERIFICATION_EXCEPTION;
		}
		String signature = HMacSHA1Util.hmacSHA1Encrypt(bodyMap, token);
		return sign.compareToIgnoreCase(signature) == 0;
	}

	/**
	 * 生成签名
	 *
	 * @param dto
	 * @param token
	 * @return
	 */
	public static String generateSign(Object dto, String token) {
		Map<String, String> bodyMap;
		try {
			bodyMap = objectToMap(dto);
		} catch (IllegalAccessException e) {
			throw SIGNATURE_VERIFICATION_EXCEPTION;
		}
		return HMacSHA1Util.hmacSHA1Encrypt(bodyMap, token);
	}


	private static Map<String, String> objectToMap(Object obj) throws IllegalAccessException {
		Map<String, String> map = new HashMap<>();
		Field[] fields = obj.getClass().getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			map.put(field.getName(), String.valueOf(field.get(obj)));
		}
		return map;
	}
}

