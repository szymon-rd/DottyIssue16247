package interf_based

import scala.util.Try
import scala.reflect.TypeTest
import generic.*

// here we use interfaces written in Java 
// todo, put back example using traits only to see if Java is the problem
object IRDF extends RDF:
  lazy val factory = testorg.impl.SimpleNodeFactory.getInstance()
  override opaque type rNode <: Matchable = testorg.TstNode
  override opaque type rURI <: rNode = testorg.Uri
  override opaque type Node <: rNode = testorg.TstNode
  override opaque type URI <: Node & rURI = testorg.Uri
  override opaque type BNode <: Node = testorg.BNode
  override opaque type Literal <: Node = testorg.Lit

  given rops: ROps[R] with
    override def mkUri(str: String): Try[RDF.URI[R]] = Try(
      factory.mkUri(str)
    )
    override def mkBNode(): RDF.BNode[R] = factory.mkBNode()
    override def mkLit(str: String): RDF.Literal[R] = factory.mkLit(str)
    override protected def nodeVal(node: RDF.Node[R]): String = node.value
    override protected def auth(uri: RDF.URI[R]): Try[String] = 
      Try(java.net.URI.create(nodeVal(uri)).getAuthority())

    given node2Uri: TypeTest[RDF.Node[R], RDF.URI[R]] with
      def unapply(x: RDF.Node[R]): Option[x.type & RDF.URI[R]] =
        x match
          case u: (x.type & testorg.Uri) => Some(u)
          case _                         => None

    given node2BN: TypeTest[RDF.Node[R], RDF.BNode[R]] with
      def unapply(x: RDF.Node[R]): Option[x.type & RDF.BNode[R]] =
        x match
          case u: (x.type & testorg.BNode) => Some(u)
          case _                           => None

    given node2Lit: TypeTest[RDF.Node[R], RDF.Literal[R]] with
      def unapply(x: RDF.Node[R]): Option[x.type & RDF.Literal[R]] =
        x match
          case u: (x.type & testorg.Lit) => Some(u)
          case _                         => None

end IRDF

@main def run =
  val test = Test[IRDF.type]
  println(test.x)
  println("folded=" + test.folded)
  println("matched should be uri" + test.matched)

