package class_based

import scala.util.Try
import scala.reflect.TypeTest

// If instead of using interfaces we use abstract classes then it compiles
// correctly
object ClassTypes {
  abstract class Node:
    def value: String

  abstract class Uri extends Node
  abstract class BNode extends Node
  abstract class Lit extends Node

  trait IFactory:
    def mkBNode(): BNode
    def mkUri(u: String): Uri
    def mkLit(u: String): Lit

  def getFactory: IFactory = AFactory

  private object AFactory extends IFactory:
    var bnode: Int = 0
    def mkBNode(): BNode =
      bnode = bnode + 1
      new BNode { def value = bnode.toString }
    def mkUri(u: String): Uri =
      new Uri { def value = u }
    def mkLit(u: String): Lit =
      new Lit { def value = u }
}

object ClassRDF extends generic.RDF:
  import class_based.ClassTypes as cz
  import generic.*

  override opaque type rNode <: Matchable = cz.Node
  override opaque type rURI <: rNode = cz.Uri
  override opaque type Node <: rNode = cz.Node
  override opaque type URI <: Node & rURI = cz.Uri
  override opaque type BNode <: Node = cz.BNode
  override opaque type Literal <: Node = cz.Lit

  given rops: generic.ROps[R] with
    override def mkUri(str: String): Try[RDF.URI[R]] = Try(
      cz.getFactory.mkUri(str)
    )
    override def mkBNode(): RDF.BNode[R] = cz.getFactory.mkBNode()
    override def mkLit(str: String): RDF.Literal[R] = cz.getFactory.mkLit(str)
    override protected def nodeVal(node: RDF.Node[R]): String = node.value
    override protected def auth(uri: RDF.URI[R]): Try[String] = 
      Try(java.net.URI.create(nodeVal(uri)).getAuthority())

    given node2Uri: TypeTest[RDF.Node[R], RDF.URI[R]] with
      def unapply(x: RDF.Node[R]): Option[x.type & RDF.URI[R]] =
        x match
          case u: (x.type & cz.Uri) => Some(u)
          case _                    => None

    given node2BN: TypeTest[RDF.Node[R], RDF.BNode[R]] with
      def unapply(x: RDF.Node[R]): Option[x.type & RDF.BNode[R]] =
        x match
          case u: (x.type & cz.BNode) => Some(u)
          case _                      => None

    given node2Lit: TypeTest[RDF.Node[R], RDF.Literal[R]] with
      def unapply(x: RDF.Node[R]): Option[x.type & RDF.Literal[R]] =
        x match
          case u: (x.type & cz.Lit) => Some(u)
          case _                    => None

end ClassRDF

@main def run =
  import generic.Test
  val test = Test[ClassRDF.type]
  println(test.x)
  println("folded=" + test.folded)
  println("matched should be uri" + test.matched)
