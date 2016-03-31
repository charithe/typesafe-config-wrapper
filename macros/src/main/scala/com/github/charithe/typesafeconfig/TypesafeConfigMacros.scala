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

import com.typesafe.config.Config

import scala.reflect.macros.whitebox

object TypesafeConfigMacros {

  def implicitsImpl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    // TypesafeConfigValueExtractor implementation for primitive types
    def genPrimitiveExtractor(tpe: c.Type, typeString: String): c.Expr[() => TypesafeConfigValueExtractor[_]] = {
      val implicitMethodName = TermName(s"extract$typeString")
      val configMethodName = TermName(s"get$typeString")
      c.Expr {
        q"""
           implicit def $implicitMethodName = new com.github.charithe.typesafeconfig.TypesafeConfigValueExtractor[$tpe] {
             def extract(config: com.typesafe.config.Config, key: String): Option[$tpe] = {
               if(config.hasPath(key)){
                 Some(config.$configMethodName(key))
               } else {
                 None
               }
             }
           }
          """
      }
    }

    // TypesafeConfigValueExtractor implementation for list types. Since getXList methods return java.util.List objects,
    // we need to convert the Java list to a Scala list with type conversions such as java.lang.Long -> scala.Long
    def genListExtractor(tpe: c.Type, typeString: String, conv: TermName): c.Expr[() => TypesafeConfigValueExtractor[_]] = {
      val implicitMethodName = TermName(s"extract${typeString}List")
      val configMethodName = TermName(s"get${typeString}List")
      c.Expr {
        q"""
          implicit def $implicitMethodName = new com.github.charithe.typesafeconfig.TypesafeConfigValueExtractor[$tpe] {
             import scala.collection.JavaConverters._
             def extract(config: com.typesafe.config.Config, key: String): Option[$tpe] = {
               if(config.hasPath(key)){
                 val x: $tpe = config.$configMethodName(key).asScala.map($conv).toList
                 Some(x)
               } else {
                 None
               }
             }
           }
          """
      }
    }

    // Only allow the Macro annotation to be applied to objects
    val traitName = annottees.map(_.tree) match {
      case List(q"object $name") => name
      case _ => c.abort(c.enclosingPosition, "This annotation can only be used with objects")
    }

    // List of tuples containing supported data types and corresponding Java to Scala conversion functions
    val supportedTypes = Seq(
      (c.typeOf[String], TermName("identity")),
      (c.typeOf[Long], TermName("Long2long")),
      (c.typeOf[Int], TermName("Integer2int")),
      (c.typeOf[Double], TermName("Double2double")),
      (c.typeOf[Boolean], TermName("Boolean2boolean")),
      (c.typeOf[Config], TermName("identity")),
      (c.typeOf[Duration], TermName("identity"))
    )

    val methodList = for ((tpe, conv) <- supportedTypes) yield {
      val typeName = tpe.toString.split("\\.").last // strip package name
      val primitiveExtractor = genPrimitiveExtractor(tpe, typeName)
      val listType = c.typecheck(q"List.empty[$tpe]") // hack to obtain a type of List[X]
      val listExtractor = genListExtractor(listType.tpe, typeName, conv)
      Seq(primitiveExtractor, listExtractor)
    }

    val methods = methodList.flatten
    c.Expr {
      q"""
          object $traitName {
             ..$methods
          }
      """
    }
  }
}
