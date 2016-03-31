Typesafe Config Wrapper
=======================

An experiment in offloading boilerplate code generation to Scala macros.

The Typesafe Config library has different getter methods for different data types as follows:

```scala
val stringValue: String = config.getString("path1")
val longValue: Long = config.getLong("path2")

import scala.collection.JavaConversions._
val stringListValue: List[String] = config.getStringList("path3") 
```

This is pretty cumbersome to use. Wouldn't it be nice instead to write code such as the following:

```scala
val stringValue = config.get[String]("path1")
// or
val stringValue: String = config.get("path1")

val longValue = config.get[Long]("path2")
val stringListValue = config.get[List[String]]("path3")
```

This could be achieved using implicit converters but it requires writing a lot of boilerplate code.

```scala
implicit def stringExtractor = new ConfigExtractor[String]{ 
    def extract(conf: Config, path: String): Option[String] = {
        if(conf.hasPath(path)){
            Some(conf.getString(path))
        } else {
            None
        }
    }
}

implicit def longExtractor = new ConfigExtractor[Long]{ 
    def extract(conf: Config, path: String): Option[Long] = {
        if(conf.hasPath(path)){
            Some(conf.getLong(path))
        } else {
            None
        }
    }
}
 // repeat for other data types

```

This project is an attempt to write a Scala macro to automatically generate the boilerplate code for us. Please note that 
this is purely an experiment and not intended for general use. There are several excellent Scala wrappers available for 
TypeSafe Config here: https://github.com/typesafehub/config#scala-wrappers-for-the-java-library


