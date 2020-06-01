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

package com.zto.zms.writer;

import com.google.common.collect.Lists;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Class: IniFileWriter</p>
 * <p>Description: </p>
 *
 * @author lidawei
 **/
public class IniFileWriter {
    private Ini ini;

    private IniFileWriter() {
    }

    public static IniFileWriter newInstance() {
        IniFileWriter fileUtil = new IniFileWriter();
        fileUtil.init();
        return fileUtil;
    }

    private void init() {
        this.ini = new Ini();
        ini.getConfig().setEscape(false);
    }


    public IniFileWriter add(String sectionName, List<IniFileEntity> options) {
        Profile.Section section = this.ini.add(sectionName);
        options.forEach(item -> {
            section.put(item.getKey(), item.getValue());
        });
        return this;
    }

    public void store(OutputStream outputStream) throws IOException {
        ini.store(outputStream);
    }

    public static class IniFileEntity {
        private String key;
        private Object value;

        public IniFileEntity(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}


