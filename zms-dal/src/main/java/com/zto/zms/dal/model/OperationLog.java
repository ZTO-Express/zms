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

package com.zto.zms.dal.model;

import java.io.Serializable;
import java.util.Date;

public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建日期
     */
    private Date createDate;

    /**
     * 操作人
     */
    private String opeartor;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 操作
     */
    private String operation;

    /**
     * 操作内容
     */
    private String content;

    /**
     * 获取主键
     *
     * @return id - 主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键
     *
     * @param id 主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取创建日期
     *
     * @return create_date - 创建日期
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * 设置创建日期
     *
     * @param createDate 创建日期
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * 获取操作人
     *
     * @return opeartor - 操作人
     */
    public String getOpeartor() {
        return opeartor;
    }

    /**
     * 设置操作人
     *
     * @param opeartor 操作人
     */
    public void setOpeartor(String opeartor) {
        this.opeartor = opeartor;
    }

    /**
     * 获取资源名称
     *
     * @return name - 资源名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置资源名称
     *
     * @param name 资源名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取操作
     *
     * @return operation - 操作
     */
    public String getOperation() {
        return operation;
    }

    /**
     * 设置操作
     *
     * @param operation 操作
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * 获取操作内容
     *
     * @return content - 操作内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置操作内容
     *
     * @param content 操作内容
     */
    public void setContent(String content) {
        this.content = content;
    }
}
