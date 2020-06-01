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

package com.zto.zms.common;

/**
 * Created by superheizai on 2017/7/27.
 */
public class ZmsException extends RuntimeException {

	public static final ZmsException PRODUCER_START_EXCEPTION = new ZmsException("ZMS producer start failed", 1001);

	public static final ZmsException INVALID_NAME_EXCEPTION = new ZmsException("ZMS Producer name can't be empty or DefaultName", 1002);

	public static final ZmsException EMPTY_TOPIC_EXCEPTION = new ZmsException("Topic cant'be empty ", 1003);

	public static final ZmsException NO_ZK_EXCEPTION = new ZmsException("no startup param zms_zk exists ", 1004);

	public static final ZmsException NOT_RUNNNING_EXCEPTION = new ZmsException("Proxy is not running ", 1005);

	public static final ZmsException CONSUMER_START_EXCEPTION = new ZmsException("ZMS consumer start failed", 1006);

	public static final ZmsException CLUSTER_INFO_EXCEPTION = new ZmsException("Cluster metadata is empty or can't be parsed", 1005);

	public static final ZmsException METAINFO_EXCEPTION = new ZmsException("metadata is empty or can't be parsed", 1006);

	public static final ZmsException NO_ZMS_PROFILE_EXCEPTION = new ZmsException("Unable to read the ZMS configuration file", 1007);

	public static final ZmsException CONSUMER_NOT_EXISTS_EXCEPTION = new ZmsException("Proxy is not running ", 1008);

	public static final ZmsException TOPIC_ENV_NOT_EXISTS_EXCEPTION = new ZmsException("Proxy is not running ", 1009);


	public static final ZmsException CHECK_TOPIC_PARTITIONS_EXCEPTION = new ZmsException("check topic partitions error", 2001);

	public static final ZmsException NO_PERMISSION_EXCEPTION = new ZmsException("no permission error", 403);

	public static final ZmsException ILLEGAL_CLUSTER_TYPE = new ZmsException("Illegal cluster type", 2100);

	public static final ZmsException BROADCAST_MUST_BE_FALSE = new ZmsException("broadcast must be false when cluster type is kafka", 1008);

	public static final ZmsException ROCKET_MQ_NOT_SUPPORT_RESET_TO_OFFSET = new ZmsException("RocketMq does not support resetting to the specified offset", 1009);

	public static final ZmsException ROCKET_MQ_NOT_SUPPORT_RESET_TO_LATEST = new ZmsException("RocketMq does not support resetting to the latest offset", 1010);

	public static final ZmsException ROCKET_MQ_NOT_SUPPORT_RESET_TO_EARLIEST = new ZmsException("RocketMq does not support resetting to the earliest offset", 1011);

	public static final ZmsException NO_PERMISSION_MODIFY = new ZmsException("There is no permission to modify the resource", 2101);

    public static final ZmsException SUPERVISORD_INITIALIZATION_EXCEPTION = new ZmsException("Supervisord client initialization error", 2102);

	public static final ZmsException FUTURE_GET_EXCEPTION = new ZmsException("query influxDB failure", 1012);

	public static final ZmsException CONSUMER_CAN_NOT_EXISTS_CHANGE = new ZmsException("consumer name can not change ", 1008);

	public static final ZmsException SIGNATURE_VERIFICATION_EXCEPTION = new ZmsException("Signature verification exception", 2000);

	private int code;

	public ZmsException(String message, int code) {
		super(message);
		this.code = code;
	}
	public ZmsException(String message, Exception exception) {
		super(message,exception);
	}

	public int getCode() {
		return code;
	}
}

