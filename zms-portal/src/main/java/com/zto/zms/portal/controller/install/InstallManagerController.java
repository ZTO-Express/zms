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

package com.zto.zms.portal.controller.install;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * @author yuhao.zhang
 * @description 安装包, 安装脚本管理
 * @date 2020/2/24
 */
@RestController
@RequestMapping("/api/package")
public class InstallManagerController {


    @RequestMapping(value = "/script/download/**/{fileName}", method = {RequestMethod.GET})
    public void getInstallScript(@PathVariable(value = "fileName") String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
        String downPath = request.getServletPath().substring("/api/package/script/download/".length());

        String path = Paths.get(downPath).toAbsolutePath().toString();
        File file = new File(path);
        if (!file.exists() || file.isDirectory()) {
            response.setStatus(404);
            return;
        }
        try (InputStream inputStream = new FileInputStream(new File(path));) {
            IOUtils.copy(inputStream, response.getOutputStream());
        }
    }


}

