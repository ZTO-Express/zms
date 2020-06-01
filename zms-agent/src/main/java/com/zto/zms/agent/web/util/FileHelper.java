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

package com.zto.zms.agent.web.util;

import com.zto.zms.utils.Assert;
import com.zto.zms.utils.HttpClient;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/2/28
 **/
public class FileHelper {
	private static Logger logger = LoggerFactory.getLogger(FileHelper.class);

	private FileHelper(){}
	/**
	 * 下载并解压文件
	 *
	 * @param fileUrl
	 * @param destDir
	 * @throws IOException
	 */
	public void uploadUnZipFile(String fileUrl, String destDir) throws IOException {
		HttpResponse httpResponse = HttpClient.getWithHttpResponse(fileUrl, null);

		File entryFile = new File(destDir);
		if (!entryFile.exists()) {
			boolean result = entryFile.mkdirs();
			Assert.that(result, "创建目录失败:" + destDir);
		}
		try (InputStream inputStream = httpResponse.getEntity().getContent();
			 TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(inputStream);
		) {
			while (null != tarArchiveInputStream.getNextTarEntry()) {
				TarArchiveEntry tarArchiveEntry = tarArchiveInputStream.getCurrentEntry();
				String fileName = destDir + "/" + tarArchiveEntry.getName();
				FileOutputStream outputStream = new FileOutputStream(fileName);
				IOUtils.copy(tarArchiveInputStream, outputStream);
				logger.info("upload config:{}", fileName);
			}

		} catch (Exception e) {
			throw new IOException("Unzip download file exception", e);
		}
	}
}

