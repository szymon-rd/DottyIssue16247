# reproduce dotty Issue 16247 => open #16408

For details on the problem see [Issue 16247: Should Java Interfaces be Matchable?](https://github.com/lampepfl/dotty/issues/16247). That issue was closed as the bug was not
reproducible. The history of this github repository traces my attempts at duplicating it.
This was successful with [commit e26983bec0d1a509cab97ac44f78f45935f4a980](https://github.com/bblfish/DottyIssue16247/commit/e26983bec0d1a509cab97ac44f78f45935f4a980) and led to reopening the issue as [Problem with Traits not being Matchable #16408](https://github.com/lampepfl/dotty/issues/16408)

An intial attempt seemed to reproduce the problem. but as per [26 Oct, discussion on Discord](https://discord.com/channels/632150470000902164/632628489719382036/1034828756063363082) it turns out this seems to have been tied to my OS or java installation...

Currently to run the code make a clean install of java and scala.

I try to use minimal commands and limit what jdk and java compiler I am using

```zsh
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
export SCALA_HOME=/usr/local/scala/scala3-3.2.1/
```

To compile the code look at the two scripts in [bin](bin/) directory.

The RDF code based on Scala Classes works

```zsh
➤  sh bin/compileScalaPure
Success(https://bblfish.net/#i)
folded=<https://bblfish.net/#i>
matched should be uriisURI class_based.ClassTypes$AFactory$$anon$2@358ee631with authority Success(bblfish.net)
```

The code relying on Java interfaces does not.

```zsh
➤  sh bin/compileJava
-- Error: scala/RDF_Interface.scala:18:8 ---------------------------------------
18 |  given rops: ROps[R] with
   |        ^
   |object creation impossible, since protected def auth(uri: generic.RDF.URI[R]): util.Try[String] in trait ROps in package generic is not defined
   |(Note that
   | parameter generic.RDF.URI[R] in protected def auth(uri: generic.RDF.URI[R]): util.Try[String] in trait ROps in package generic does not match
   | parameter testorg.Uri & (testorg.TstNode & (testorg.TstNode & Matchable)) & (testorg.Uri &
   |
   |(testorg.TstNode & Matchable)) in protected override def auth
   |  (uri: testorg.Uri & (testorg.TstNode & (testorg.TstNode & Matchable)) & (
   |    testorg.Uri
   |   & (testorg.TstNode & Matchable))): util.Try[String] in object rops in object IRDF
   | )
-- [E038] Declaration Error: scala/RDF_Interface.scala:25:27 -------------------
25 |    override protected def auth(uri: RDF.URI[R]): Try[String] =
   |                           ^
   |   method auth has a different signature than the overridden declaration
   |----------------------------------------------------------------------------
   | Explanation (enabled by `-explain`)
   |- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   | There must be a non-final field or method with the name auth and the
   | same parameter list in a super class of object rops to override it.
   |
   |   protected override def auth
   |   (uri: testorg.Uri & (testorg.TstNode & (testorg.TstNode & Matchable)) & (
   |     testorg.Uri
   |    & (testorg.TstNode & Matchable))): util.Try[String]
   |
   | The super classes of object rops contain the following members
   | named auth:
   |   protected def auth(uri: generic.RDF.URI[interf_based.IRDF.R]): util.Try[String]
    ----------------------------------------------------------------------------
2 errors found
```

Indeed the code relying on Scala traits does not compile either. Here we use scala-cli as we do
in the github actions that are run whenever a commit is sent to this repository. See [workflows reports](https://github.com/bblfish/DottyIssue16247/actions/workflows/test-scala.yml).

```scala
scala-cli --scala 3.2.1 scala/RDF_Traits.scala scala/RDF_UsingScalaTrait.scala scala/RDF.scala
```

## Notes

The problem was found in [commit e26983bec0d1a509cab97ac44f78f45935f4a980](https://github.com/bblfish/DottyIssue16247/commit/e26983bec0d1a509cab97ac44f78f45935f4a980) after adding a simple method `auth` to the `ROps` trait.

```scala
 protected def auth(uri: RDF.URI[R]): Try[String]
```

The next commit after that was to clean up the code into three sections:
 - generic RDF -- all the code that does not know about implementations
 - class-based -- the implementation that works using scala classes
 - interface-based -- the implementation works using interfaces (Java ones in particular)

using a side by side diff tool such as opendiff on macos, will help show
that the difference between RDF_Class and RDF_Interface is essentially 
just that RDF_Class needs to supply its own classes and factory. 

```zsh
cd scala
opendiff RDF_Class.scala  RDF_Interface.scala
```
