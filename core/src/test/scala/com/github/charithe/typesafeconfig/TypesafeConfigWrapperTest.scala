/*
 * Copyright 2016 Charith Ellawala
 *
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

package com.github.charithe.typesafeconfig

import java.time.Duration
import java.util.Collections

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory.fromAnyRef
import org.scalatest.{FunSuite, Matchers}

import TypesafeConfigWrapper._

class TypesafeConfigWrapperTest extends FunSuite with Matchers {
  val conf = ConfigFactory.empty()
    .withValue("stringKey", fromAnyRef("hello"))
    .withValue("intKey", fromAnyRef(12))
    .withValue("longKey", fromAnyRef(22L))
    .withValue("doubleKey", fromAnyRef(22.4d))
    .withValue("booleanKey", fromAnyRef(true))
    .withValue("durationKey", fromAnyRef(Duration.ofDays(1)))
    .withValue("stringList", fromAnyRef(Collections.nCopies(5, "world")))
    .withValue("intList", fromAnyRef(Collections.nCopies(5, 12)))
    .withValue("longList", fromAnyRef(Collections.nCopies(5, 22L)))
    .withValue("doubleList", fromAnyRef(Collections.nCopies(5, 22.6d)))
    .withValue("booleanList", fromAnyRef(Collections.nCopies(5, true)))
    .withValue("durationList", fromAnyRef(Collections.nCopies(5, Duration.ofDays(1))))

  val wrapper = new TypesafeConfigWrapper(conf)

  test("Get String") {
    val result = wrapper.get[String]("stringKey")
    result should be(Some("hello"))
  }

  test("Get Int") {
    val result = wrapper.get[Int]("intKey")
    result should be(Some(12))
  }

  test("Get Long") {
    val result = wrapper.get[Long]("longKey")
    result should be(Some(22L))
  }

  test("Get Double") {
    val result = wrapper.get[Double]("doubleKey")
    result should be(Some(22.4D))
  }

  test("Get Boolean") {
    val result = wrapper.get[Boolean]("booleanKey")
    result should be(Some(true))
  }

  test("Get Duration") {
    val result = wrapper.get[Long]("durationKey")
    result should be('defined)
    result.get should equal(Duration.ofDays(1).toMillis)
  }

  test("Get String List") {
    val result = wrapper.get[List[String]]("stringList")
    result should be('defined)

    val l = result.get
    l should have size (5)
    l should contain("world")
  }

  test("Get Int List") {
    val result = wrapper.get[List[Int]]("intList")
    result should be('defined)

    val l = result.get
    l should have size (5)
    l should contain(12)
  }

  test("Get Long List") {
    val result = wrapper.get[List[Long]]("longList")
    result should be('defined)

    val l = result.get
    l should have size (5)
    l should contain(22L)
  }

  test("Get Double List") {
    val result = wrapper.get[List[Double]]("doubleList")
    result should be('defined)

    val l = result.get
    l should have size (5)
    l should contain(22.6D)
  }

  test("Get Boolean List") {
    val result = wrapper.get[List[Boolean]]("booleanList")
    result should be('defined)

    val l = result.get
    l should have size (5)
    l should contain(true)
  }

  test("Get Duration List") {
    val result = wrapper.get[List[Duration]]("durationList")
    result should be('defined)

    val l = result.get
    l should have size (5)
    l should contain(Duration.ofDays(1))
  }

  test("Get non-existent") {
    val result = wrapper.get[String]("someKey")
    result should be('empty)
  }
}
