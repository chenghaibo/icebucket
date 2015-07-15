/*
 * Copyright (c) 2015 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.snowplowanalytics.model

import spray.json.DefaultJsonProtocol

case class DruidRequest(queryType: String, dataSource: String, granularity: String, intervals: String)


//  {
//    "queryType": "timeseries",
//    "dataSource": "name_of_dynamodb_table_for_now",
//    "granularity": "day",
//    "intervals": [ "2012-01-01T00:00:00.000/2012-01-03T00:00:00.000" ]
//  }

/**
 * Implements spray-json support so DruidRequest case class can be marshalled
 * to/from json when accepting and completing requests.
 */
object DruidRequestJsonProtocol extends DefaultJsonProtocol {
  implicit val druidFormat = jsonFormat4(DruidRequest)
}
