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

package com.zto.zms.portal.dto.serve;

import java.util.List;

/**
 * 配置对比封装值返回
 *
 * @author sun kai
 * @date 2020/03/04
 */
public class ServicePropertyCompareFileDTO {

    // 文件名
    private String fileName;

    // 减少数量
    private Integer reduceNum;

    // 上次启动数量
    private Integer processNum;

    // 新增加数量
    private Integer addNum;

    // 当前数量
    private Integer currentNum;

    // 对比集合
    private List<ServicePropertyCompareValueDTO> compareList;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getReduceNum() {
        return reduceNum;
    }

    public void setReduceNum(Integer reduceNum) {
        this.reduceNum = reduceNum;
    }

    public Integer getProcessNum() {
        return processNum;
    }

    public void setProcessNum(Integer processNum) {
        this.processNum = processNum;
    }

    public Integer getAddNum() {
        return addNum;
    }

    public void setAddNum(Integer addNum) {
        this.addNum = addNum;
    }

    public Integer getCurrentNum() {
        return currentNum;
    }

    public void setCurrentNum(Integer currentNum) {
        this.currentNum = currentNum;
    }

    public List<ServicePropertyCompareValueDTO> getCompareList() {
        return compareList;
    }

    public void setCompareList(List<ServicePropertyCompareValueDTO> compareList) {
        this.compareList = compareList;
    }
}

