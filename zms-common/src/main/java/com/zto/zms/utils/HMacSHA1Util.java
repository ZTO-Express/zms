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


import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lidawei
 * @date 2019/11/15
 **/
public class HMacSHA1Util {

    private HMacSHA1Util() {
    }

    private static final String CHER_SET = "utf-8";

    private static final String ALGORITHM = "HmacSHA1";


    /**
     * 拼接参数字符串：“先将参数按照a-z排序,再将参数和值(key=value)用&拼接起来”
     *
     * @param map
     * @return string
     */
    public static String spliceParam(Map<String, String> map) {

        String result = "";
        if (map.size() == 0) {
            return result;
        }
        List<Map.Entry<String, String>> entries = map.entrySet().stream()
                .filter(item -> item.getValue() != null)
                .sorted(Comparator.comparing(Map.Entry::getKey)).collect(Collectors.toList());

        StringBuilder paramBuffer = new StringBuilder();
        //将排序后的参数(key=value)用&拼接起来，并进行URL编码。
        int index = 0;
        for (Map.Entry<String, String> entry : entries) {
            paramBuffer.append(entry.getKey()).append("=").append(entry.getValue());
            index++;
            if (index != entries.size()) {
                paramBuffer.append("&");
            }
        }
        result = paramBuffer.toString();
        return result;
    }


    /**
     * HMAC签名，用于确定消息的不可篡改性
     *
     * @param encryptText 加密的文本字符串
     * @param encryptKey  用于签名的macKey
     * @return 签名结果字符串
     */
    public static String hmacSHA1Encrypt(String encryptText, String encryptKey) {

        try {
            //构建密钥
            byte[] data = encryptKey.getBytes(CHER_SET);

            //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
            SecretKey secretKey = new SecretKeySpec(data, ALGORITHM);

            //生成一个指定 Mac 算法 的 Mac 对象
            Mac mac = Mac.getInstance(ALGORITHM);
            //用给定密钥初始化 Mac 对象
            mac.init(secretKey);

            //构建文本
            byte[] text = encryptText.getBytes(CHER_SET);

            //完成 Mac 操作 ，获得 HMAC签名
            byte[] result = mac.doFinal(text);

            //吧byte[]转为hexDigest
            return byteArrayToHexString(result).toLowerCase();

        } catch (UnsupportedEncodingException | InvalidKeyException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return null;
        }

    }

    public static String hmacSHA1Encrypt(Map<String, String> body, String encryptKey) {
        String spliceParam = spliceParam(body);
        return hmacSHA1Encrypt(spliceParam, encryptKey);

    }

    private static String byteArrayToHexString(byte[] b) {
        int len = b.length;
        StringBuilder data = new StringBuilder(200);

        for (int i = 0; i < len; i++) {
            data.append(Integer.toHexString((b[i] >> 4) & 0xf));
            data.append(Integer.toHexString(b[i] & 0xf));
        }
        return data.toString();
    }
}

