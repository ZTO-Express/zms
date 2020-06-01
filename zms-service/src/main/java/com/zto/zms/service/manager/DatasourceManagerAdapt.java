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

package com.zto.zms.service.manager;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * <p>Class: DatasourceManagerAdapt</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/4/3
 **/
public class DatasourceManagerAdapt<T extends DatasourceManager> {

    private final Map<Integer, T> evnDataSourceMap = Maps.newConcurrentMap();


    /**
     * 获取当前环境数据源
     *
     * @param env
     * @return
     */
    public T getDatasource(Integer env) {
        return evnDataSourceMap.get(env);
    }

    /**
     * 重新加载资源
     *
     * @param env
     * @return
     */
    public T reload(Integer env, T datasourceManager) {
        synchronized (evnDataSourceMap) {
            if (evnDataSourceMap.containsKey(env)) {
                evnDataSourceMap.remove(env).destroy();
            }
            evnDataSourceMap.put(env, datasourceManager);
        }
        return evnDataSourceMap.get(env);
    }

    /**
     * 移除资源
     *
     * @param env
     */
    public void remove(Integer env) {
        synchronized (evnDataSourceMap) {
            if (evnDataSourceMap.containsKey(env)) {
                evnDataSourceMap.remove(env).destroy();
            }
        }
    }

    public void destroy() {
        evnDataSourceMap.forEach((key, value) -> value.destroy());
    }
}

