/*
 * Copyright 2019 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.scio.bigquery.types

import com.google.api.services.bigquery.model.{TableFieldSchema, TableSchema}
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

import scala.jdk.CollectionConverters._

class SchemaUtilTest extends AnyFlatSpec with Matchers {

  "toPrettyString()" should "support indent" in {
    val schema = new TableSchema().setFields(
      List(
        new TableFieldSchema().setName("field").setType("BOOLEAN").setMode("REQUIRED")
      ).asJava
    )

    SchemaUtil.toPrettyString(schema, "Row", 0) should equal("""@BigQueryType.toTable
        |case class Row(field: Boolean)""".stripMargin)
    SchemaUtil.toPrettyString(schema, "Row", 2) should equal("""@BigQueryType.toTable
        |case class Row(
        |  field: Boolean)""".stripMargin)
    SchemaUtil.toPrettyString(schema, "Row", 4) should equal("""@BigQueryType.toTable
        |case class Row(
        |    field: Boolean)""".stripMargin)
  }

  it should "support all primitive types" in {
    val schema = SchemaProvider.schemaOf[Schemas.Required]
    SchemaUtil.toPrettyString(schema, "Row", 2) should equal(
      """
        |@BigQueryType.toTable
        |case class Row(
        |  boolF: Boolean,
        |  intF: Long,
        |  longF: Long,
        |  floatF: Double,
        |  doubleF: Double,
        |  stringF: String,
        |  byteArrayF: ByteString,
        |  byteStringF: ByteString,
        |  timestampF: Instant,
        |  dateF: LocalDate,
        |  timeF: LocalTime,
        |  datetimeF: LocalDateTime,
        |  bigDecimalF: BigDecimal,
        |  geographyF: Geography,
        |  jsonF: Json,
        |  bigNumericF: BigNumeric)
      """.stripMargin.trim
    )
  }

  it should "support nullable primitive types" in {
    val schema = SchemaProvider.schemaOf[Schemas.Optional]
    SchemaUtil.toPrettyString(schema, "Row", 2) should equal(
      """
        |@BigQueryType.toTable
        |case class Row(
        |  boolF: Option[Boolean],
        |  intF: Option[Long],
        |  longF: Option[Long],
        |  floatF: Option[Double],
        |  doubleF: Option[Double],
        |  stringF: Option[String],
        |  byteArrayF: Option[ByteString],
        |  byteStringF: Option[ByteString],
        |  timestampF: Option[Instant],
        |  dateF: Option[LocalDate],
        |  timeF: Option[LocalTime],
        |  datetimeF: Option[LocalDateTime],
        |  bigDecimalF: Option[BigDecimal],
        |  geographyF: Option[Geography],
        |  jsonF: Option[Json],
        |  bigNumericF: Option[BigNumeric])
      """.stripMargin.trim
    )
  }

  it should "support repeated primitive types" in {
    val schema = SchemaProvider.schemaOf[Schemas.Repeated]
    SchemaUtil.toPrettyString(schema, "Row", 2) should equal(
      """
        |@BigQueryType.toTable
        |case class Row(
        |  boolF: List[Boolean],
        |  intF: List[Long],
        |  longF: List[Long],
        |  floatF: List[Double],
        |  doubleF: List[Double],
        |  stringF: List[String],
        |  byteArrayF: List[ByteString],
        |  byteStringF: List[ByteString],
        |  timestampF: List[Instant],
        |  dateF: List[LocalDate],
        |  timeF: List[LocalTime],
        |  datetimeF: List[LocalDateTime],
        |  bigDecimalF: List[BigDecimal],
        |  geographyF: List[Geography],
        |  jsonF: List[Json],
        |  bigNumericF: List[BigNumeric])
      """.stripMargin.trim
    )
  }

  it should "support records" in {
    val fields = List(
      new TableFieldSchema().setName("f1").setType("INTEGER").setMode("REQUIRED"),
      new TableFieldSchema().setName("f2").setType("FLOAT").setMode("REQUIRED")
    ).asJava
    val schema = new TableSchema().setFields(
      List(
        new TableFieldSchema()
          .setName("r1")
          .setType("RECORD")
          .setFields(fields)
          .setMode("REQUIRED"),
        new TableFieldSchema()
          .setName("r2")
          .setType("RECORD")
          .setFields(fields)
          .setMode("NULLABLE"),
        new TableFieldSchema()
          .setName("r3")
          .setType("RECORD")
          .setFields(fields)
          .setMode("REPEATED")
      ).asJava
    )
    SchemaUtil.toPrettyString(schema, "Row", 0) should equal(
      """
        |@BigQueryType.toTable
        |case class Row(r1: R1$1, r2: Option[R2$1], r3: List[R3$1])
        |case class R1$1(f1: Long, f2: Double)
        |case class R2$1(f1: Long, f2: Double)
        |case class R3$1(f1: Long, f2: Double)
      """.stripMargin.trim
    )
  }

  it should "support reserved words" in {
    val expectedFields = SchemaUtil.scalaReservedWords
      .map(e => s"`$e`")
      .mkString(start = "", sep = ": Long, ", end = ": Long")
    val schema = new TableSchema().setFields(SchemaUtil.scalaReservedWords.map { name =>
      new TableFieldSchema()
        .setName(name)
        .setType("INTEGER")
        .setMode("REQUIRED")
    }.asJava)
    SchemaUtil.toPrettyString(schema, "Row", 0) should equal(
      s"""|@BigQueryType.toTable
          |case class Row($expectedFields)""".stripMargin
    )
  }
}
