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

import com.google.common.collect.Maps;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author lidawei
 * @date 2020/3/4
 **/
public class PropertiesWriter {
    private Map<String, String> props = Maps.newTreeMap();

    private PropertiesWriter() {
    }

    public static PropertiesWriter newInstance() {
        return new PropertiesWriter();
    }


    public PropertiesWriter add(String key, String value) {
        this.props.put(key, value);
        return this;
    }

    public void store(OutputStream outputStream) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        for (Map.Entry<String, String> entry : props.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            bufferedWriter.write(key + "=" + value);
            bufferedWriter.newLine();

        }
        bufferedWriter.flush();
    }

}

