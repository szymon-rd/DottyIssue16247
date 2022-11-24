package interf_based

import scala.util.Try
import scala.reflect.TypeTest
import generic.*

// here we use interfaces written in Java 
// todo, put back example using traits only to see if Java is the problem
object TraitBasedRDF extends RDF:
  lazy val factory: rdfscala.ScalaMkNodes = rdfscala.SimpleScalaNodeFactory
  override opaque type rNode <: Matchable = rdfscala.TstNode
  override opaque type rURI <: rNode = rdfscala.URI
  override opaque type Node <: rNode = rdfscala.TstNode
  override opaque type URI <: Node & rURI = rdfscala.URI
  override opaque type BNode <: Node = rdfscala.BNode
  override opaque type Literal <: Node = rdfscala.Lit

  given rops: ROps[R] with
    override def mkUri(str: String): Try[RDF.URI[R]] = Try(
      factory.mkUri(str)
    )
    override def mkBNode(): RDF.BNode[R] = factory.mkBNode()
    override def mkLit(str: String): RDF.Literal[R] = factory.mkLit(str)
    override protected def nodeVal(node: RDF.Node[R]): String = node.value()
    override protected def auth(uri: RDF.URI[R]): Try[String] = 
      Try(java.net.URI.create(nodeVal(uri)).getAuthority())

    given node2Uri: TypeTest[RDF.Node[R], RDF.URI[R]] with
      def unapply(x: RDF.Node[R]): Option[x.type & RDF.URI[R]] =
        x match
          case u: (x.type & rdfscala.URI) => Some(u)
          case _                         => None

    given node2BN: TypeTest[RDF.Node[R], RDF.BNode[R]] with
      def unapply(x: RDF.Node[R]): Option[x.type & RDF.BNode[R]] =
        x match
          case u: (x.type & rdfscala.BNode) => Some(u)
          case _                           => None

    given node2Lit: TypeTest[RDF.Node[R], RDF.Literal[R]] with
      def unapply(x: RDF.Node[R]): Option[x.type & RDF.Literal[R]] =
        x match
          case u: (x.type & rdfscala.Lit) => Some(u)
          case _                         => None

end TraitBasedRDF

@main def run =
  val test = Test[TraitBasedRDF.type]
  println(test.x)
  println("folded=" + test.folded)
  println("matched should be uri" + test.matched)
