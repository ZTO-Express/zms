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

package com.zto.zms.service.domain.page;

/**
 * 分页查询传入参数
 *
 * @author sun kai
 * @date 2020/02/29
 */
public class Page {
    /**
     * 默认为1
     */
    private Integer currentPage = 1;
    /**
     * 默认为25，和前端保持一致
     */
    private Integer pageSize = 25;

    /**
     * limit第一个参数
     */
    private int offset = 0;

    private String keyWord;

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public int getOffset() {
        return (this.currentPage - 1) * this.pageSize;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}

