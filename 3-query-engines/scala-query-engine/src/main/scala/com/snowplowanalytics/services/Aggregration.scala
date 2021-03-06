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
package com.snowplowanalytics.services

// Scala
import spray.json._
import awscala.dynamodbv2._
import spray.json.DefaultJsonProtocol._
import com.twitter.algebird.Operators._

// package import
import com.snowplowanalytics.model.{DruidResponse, AggregationDynamoDBJsonProtocol, AggregationDynamoDB, DruidRequest}


/**
 * Aggregation Object holds all the functions counting items
 */
object Aggregation {

  /**
   * Helper Function for converting DynamoDB to AggregationDynamoDB model
   */
  def convertDataStage(dynamoArray: Seq[Seq[awscala.dynamodbv2.Attribute]]): scala.collection.mutable.ArrayBuffer[com.snowplowanalytics.model.AggregationDynamoDB] =  {
    var resultList = scala.collection.mutable.ArrayBuffer.empty[AggregationDynamoDB]
    for (a <- dynamoArray) {
      val result = a.map(unpack)
      println(result)
      resultList += AggregationDynamoDB(Some(result(0).toInt), result(2), result(1), result(0).toInt)
    }
    resultList
  }

  /**
   * Helper Function for converting DynamoDB to AggregationDynamoDB model
   */
  def convertDataStageHour(dynamoArray: Seq[Seq[awscala.dynamodbv2.Attribute]]): scala.collection.mutable.ArrayBuffer[com.snowplowanalytics.model.AggregationDynamoDB] =  {
    var resultList = scala.collection.mutable.ArrayBuffer.empty[AggregationDynamoDB]
    for (a <- dynamoArray) {
      val result = a.map(unpackHour)
      println(result)
      resultList += AggregationDynamoDB(Some(result(0).toInt), result(2), result(1), result(0).toInt)
    }
    resultList
  }

  /**
   * Helper Function for converting DynamoDB to AggregationDynamoDB model
   */
  def convertDataStageDay(dynamoArray: Seq[Seq[awscala.dynamodbv2.Attribute]]): scala.collection.mutable.ArrayBuffer[com.snowplowanalytics.model.AggregationDynamoDB] =  {
    var resultList = scala.collection.mutable.ArrayBuffer.empty[AggregationDynamoDB]
    for (a <- dynamoArray) {
      val result = a.map(unpackDay)
      println(result)
      resultList += AggregationDynamoDB(Some(result(0).toInt), result(2), result(1), result(0).toInt)
    }
    resultList
  }


  /**
   * Function takes collection of AggregationDynamoDBs and returns a JSON DruidResponse
   */
  def countDruidResponse(eventArray: List[com.snowplowanalytics.model.AggregationDynamoDB]):  scala.collection.immutable.Iterable[spray.json.JsObject] = {
    val groupByTimestamp = eventArray.groupBy(_.timestamp)
    val typeAndCountExtracted = groupByTimestamp.mapValues(_.map(x => Map(x.eventType -> x.count)))
    typeAndCountExtracted map {
      keyVal => {
        val k = keyVal._1.toJson
        val v = keyVal._2.toJson
        JsObject("timestamp" -> k, "result" -> v)
      }
    }
  }

  /**
   * Function takes collection of AggregationDynamoDBs and returns a JSON DruidResponse
   */
  def countHourlyDruidResponse(eventArray: List[com.snowplowanalytics.model.AggregationDynamoDB]):  scala.collection.immutable.Iterable[spray.json.JsObject] = {
    val groupByTimestamp = eventArray.groupBy(_.timestamp)
    val typeAndCountExtracted = groupByTimestamp.mapValues(_.map(x => Map(x.eventType -> x.count)))
    val aggregateMaps = typeAndCountExtracted.mapValues( _.reduce(_+_))
    aggregateMaps map {
      keyVal => {
        val k = keyVal._1.toJson
        val v = keyVal._2.toJson
        JsObject("timestamp" -> k, "result" -> v)
      }
    }
  }

  /**
   * Helper Function for custom marshaller for AggregationDynamoDB model
   */
  def serialize(events: List[AggregationDynamoDB]): List[spray.json.JsObject] = {
    for (event <- events)
      yield AggregationDynamoDBJsonProtocol.eventFormat.write(event)
  }

  /**
   * Helper Function for convertDataStage - unpacks DynamoDB table values
   */
  def unpack(x: Any): String = x match {
    case Attribute("Count", value) => value.getN
    case Attribute("EventType", value) => value.getS
    case Attribute("Timestamp", value) => value.getS
    case Attribute("CreatedAt", value) => value.getN
  }

  def unpackHour(x: Any): String = x match {
    case Attribute("Count", value) => value.getN
    case Attribute("EventType", value) => value.getS
    case Attribute("Timestamp", value) => BucketingStrategyHour.downsample(value.getS)
    case Attribute("CreatedAt", value) => value.getN
  }

  def unpackDay(x: Any): String = x match {
    case Attribute("Count", value) => value.getN
    case Attribute("EventType", value) => value.getS
    case Attribute("Timestamp", value) => BucketingStrategyDay.downsample(value.getS)
    case Attribute("CreatedAt", value) => value.getN
  }

}