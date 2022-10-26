# Attempting to reporduce dotty Issue 16247 

For details on the problem see [Issue 16247: Should Java Interfaces be Matchable?](https://github.com/lampepfl/dotty/issues/16247). 

An intial attempt seemed to reproduce the problem. but as per [26 Oct, discussion on Discord](https://discord.com/channels/632150470000902164/632628489719382036/1034828756063363082) it turns out this seems to have been tied to my OS or java installation...

Currently to run the code make a clean install of java and scala.

I try to use minimal commands and limit what jdk and java compiler I am using

```zsh
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
export PATH=/bin:/usr/bin:/sbin:/usr/local/scala/scala3-3.2.1-RC4/bin:/usr/local/scala/sbt-1.7.2/bin
```

First compile the java code

```zsh
javac java/testorg/TstNode.java
```
That produces the class file in the same directory.
Then compile the scala file with

```zsh
 scalac -classpath java -d build -explain scala/RDF_Trait.scala
```

That searches for the java file in the right dir, and outputs all the new files in the build directory.

To run the program
```zsh
/usr/local/scala/scala3-3.2.0/bin/scala -classpath java:build interf_based.run
```

I did get an error on class file major version doing that as I had compiled the program with
java 18 and then ran it with java 17. So be careful which one is available.


## Notes

If I use my scalac version 3.2.0 downloaded on 
```zsh
➤  ls -al "$(which scalac)"                                                                                     1 ↵
-rwxr-xr-x  1 hjs  staff  128931 Sep  8 16:08 /Users/hjs/Library/Application Support/Coursier/bin/scalac
```
I get the error

```scala
➤  scalac -classpath java -d build -explain scala/RDF_Trait.scala
-- Error: scala/RDF_Trait.scala:107:8 ------------------------------------------
107 |  given rops: ROps[R] with
    |        ^
    |object creation impossible, since protected def nodeVal(node: interf_based.RDF.Node[R]): String in trait ROps in package interf_based is not defined
    |(Note that
    | parameter interf_based.RDF.Node[R] in protected def nodeVal(node: interf_based.RDF.Node[R]): String in trait ROps in package interf_based does not match
    | parameter testorg.TstNode & Matchable in protected override def nodeVal(node: testorg.TstNode & Matchable): String in object rops in object IRDF
    | )
-- [E038] Declaration Error: scala/RDF_Trait.scala:113:27 ----------------------
113 |    override protected def nodeVal(node: RDF.Node[R]): String = node.value
    |                           ^
    |method nodeVal has a different signature than the overridden declaration
    |---------------------------------------------------------------------------
    | Explanation (enabled by `-explain`)
    |- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    | There must be a non-final field or method with the name nodeVal and the
    | same parameter list in a super class of object rops to override it.
    |
    |   protected override def nodeVal(node: testorg.TstNode & Matchable): String
    |
    | The super classes of object rops contain the following members
    | named nodeVal:
    |   protected def nodeVal(node: interf_based.RDF.Node[interf_based.IRDF.R]): String
     ---------------------------------------------------------------------------
2 errors found
```

which is very similar to the error in banana-rdf.

but with another version of scalac

```
➤ /usr/local/scala/scala3-3.2.0/bin/scalac -classpath java -d build -explain scala/RDF_Trait.scala


