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

package com.zto.zms.portal.common;

/**
 * topic、consumer的状态
 * Created by superheizai on 2017/7/26.
 */
public enum ResourceStatus {

    //
    SOFT_DELETED(-2, "已删除"), CREATE_NEW(0, "待审批"), CREATE_APPROVED(1, "已审批"), UPDATE_NEW(2, "待审批"), UPDATE_APPROVED(3, "已审批");

    private Integer status;
    private String showValue;

    ResourceStatus(Integer status, String showValue) {
        this.status = status;
        this.showValue = showValue;
    }

    public String getShowValue() {
        return showValue;
    }

    public void setShowValue(String showValue) {
        this.showValue = showValue;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public static ResourceStatus parse(Integer status) {
        switch (status) {

            case -2:
                return SOFT_DELETED;

            case 0:
                return CREATE_NEW;

            case 1:
                return CREATE_APPROVED;

            case 2:
                return UPDATE_NEW;

            case 3:
                return UPDATE_APPROVED;

            default:
                return CREATE_NEW;

        }
    }
}

